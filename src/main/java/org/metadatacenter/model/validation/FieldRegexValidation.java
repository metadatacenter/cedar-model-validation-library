package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.metadatacenter.model.validation.report.CedarValidationReport;
import org.metadatacenter.model.validation.report.ErrorItem;

import java.util.HashMap;
import java.util.Map;

/** Checks CEDAR's value constraint, which is not a JSON Schema pattern keyword. */
final class FieldRegexValidation {
  private FieldRegexValidation() {}

  private static final class RegexWorkLimit extends RuntimeException {}

  private static final class RegexContext extends Context {
    private int remaining;
    RegexContext(ContextFactory factory) { super(factory); }
  }

  private static final ContextFactory CONTEXTS = new ContextFactory() {
    @Override protected Context makeContext() { return new RegexContext(this); }
    @Override protected void observeInstructionCount(Context context, int count) {
      RegexContext regexContext = (RegexContext) context;
      regexContext.remaining -= count;
      if (regexContext.remaining <= 0) throw new RegexWorkLimit();
    }
  };

  /** Request-local state: neither untrusted expressions nor mutable JS objects are globally cached. */
  private static final class Expressions implements AutoCloseable {
    private RegexContext context;
    private Scriptable scope;
    private final Map<String, Scriptable> compiled = new HashMap<>();

    Scriptable compile(String regex) {
      if (context == null) {
        context = (RegexContext) CONTEXTS.enterContext();
        context.setLanguageVersion(Context.VERSION_ES6);
        context.setOptimizationLevel(-1);
        context.setInstructionObserverThreshold(10_000);
        context.setClassShutter(name -> false);
        scope = context.initSafeStandardObjects(null, true);
      }
      if (!compiled.containsKey(regex)) {
        try {
          // Pass the pattern as data, never as executable JavaScript. No flags, as in CEE.
          compiled.put(regex, context.newObject(scope, "RegExp", new Object[]{"^(?:" + regex + ")$"}));
        } catch (EcmaError invalidPattern) {
          compiled.put(regex, null);
        }
      }
      return compiled.get(regex);
    }

    boolean matches(Scriptable expression, String value) {
      // Rhino counts regex VM instructions too. Abort excessive backtracking on this
      // thread rather than timing out a worker that would continue consuming CPU.
      context.remaining = 1_000_000;
      return Context.toBoolean(ScriptableObject.callMethod(context, expression, "test", new Object[]{value}));
    }

    @Override public void close() {
      if (context != null) context.close();
    }
  }

  static void validateSchema(JsonNode schema, String path, CedarValidationReport report) {
    try (Expressions expressions = new Expressions()) {
      validateSchema(schema, path, report, expressions);
    }
  }

  private static void validateSchema(JsonNode schema, String path, CedarValidationReport report,
      Expressions expressions) {
    if (schema == null || !schema.isObject()) return;
    if (schema.path("items").isObject()) {
      validateSchema(schema.get("items"), path + "/items", report, expressions);
      return;
    }
    String regex = schema.path("_valueConstraints").path("regex").asText("");
    if (!regex.isEmpty()) {
      if (expressions.compile(regex) == null) {
        report.addError(new ErrorItem("The template contains an invalid regular expression.",
            path + "/_valueConstraints/regex"));
      }
    }
    schema.path("properties").fields().forEachRemaining(child -> {
      if (isChild(child.getValue()))
        validateSchema(child.getValue(), path + "/properties/" + escape(child.getKey()), report, expressions);
    });
  }

  static void validateInstance(JsonNode instance, JsonNode schema, String path, CedarValidationReport report) {
    try (Expressions expressions = new Expressions()) {
      validateInstance(instance, schema, path, report, expressions);
    }
  }

  private static void validateInstance(JsonNode instance, JsonNode schema, String path,
      CedarValidationReport report, Expressions expressions) {
    if (schema == null || !schema.isObject() || instance == null) return;
    if (schema.path("items").isObject()) {
      if (instance.isArray()) {
        for (int i = 0; i < instance.size(); i++)
          validateInstance(instance.get(i), schema.get("items"), path + "/" + i, report, expressions);
      }
      return; // JSON Schema owns incorrect shapes.
    }
    if (!instance.isObject()) return;
    String regex = schema.path("_valueConstraints").path("regex").asText("");
    if (!regex.isEmpty()) {
      Scriptable expression = expressions.compile(regex);
      JsonNode value = instance.path("@value");
      if (expression == null) {
        report.addError(new ErrorItem("The template contains an invalid regular expression.", path + "/@value"));
      } else if (value.isTextual() && !value.textValue().isEmpty()) {
        // Missing/null/empty answers remain saveable; required-value warnings are separate.
        try {
          if (!expressions.matches(expression, value.textValue()))
            report.addError(new ErrorItem("Does not match " + regex + ".", path + "/@value"));
        } catch (RegexWorkLimit exceeded) {
          report.addError(new ErrorItem("The regular expression exceeds the validation work limit; "
              + "ask the template author to simplify it.", path + "/@value"));
        }
      }
    }
    schema.path("properties").fields().forEachRemaining(child -> {
      if (isChild(child.getValue()))
        validateInstance(instance.get(child.getKey()), child.getValue(), path + "/" + escape(child.getKey()),
            report, expressions);
    });
  }

  private static boolean isChild(JsonNode schema) {
    return schema.path("_ui").isObject() || schema.path("items").path("_ui").isObject();
  }

  private static String escape(String key) {
    return key.replace("~", "~0").replace("/", "~1");
  }
}
