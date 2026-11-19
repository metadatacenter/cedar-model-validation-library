package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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
          compiled.put(regex, context.newObject(scope, "RegExp", new Object[]{"^(?:" + browserPattern(regex) + ")$"}));
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

  /** Rhino 1.9.1 interprets property escapes without the u flag; ECMAScript treats p/P as literals. */
  private static String browserPattern(String pattern) {
    var result = new StringBuilder(pattern.length());
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (c == '\\' && i + 1 < pattern.length()) {
        char next = pattern.charAt(++i);
        if (next != 'p' && next != 'P') result.append(c);
        result.append(next);
      } else result.append(c);
    }
    return result.toString();
  }

  static void validateConfiguration(JsonNode field, String path, java.util.List<ValidationFinding> out) {
    JsonNode pattern = field.path("_valueConstraints").path("regex");
    if (pattern.isMissingNode()) return;
    if (!pattern.isTextual()) {
      out.add(new ValidationFinding("templateConstraint", ValidationFinding.Category.STRUCTURAL,
          path + "/_valueConstraints/regex", "A regular expression must be a string."));
      return;
    }
    if (pattern.textValue().isEmpty()) return;
    try (Expressions expressions = new Expressions()) {
      if (expressions.compile(pattern.textValue()) == null)
        out.add(new ValidationFinding("templateConstraint", ValidationFinding.Category.STRUCTURAL,
            path + "/_valueConstraints/regex", "The template contains an invalid regular expression."));
    }
  }

  static void validateValue(String value, JsonNode field, String path, java.util.List<ValidationFinding> out) {
    String regex = field.path("_valueConstraints").path("regex").asText("");
    if (regex.isEmpty() || value.isEmpty()) return;
    try (Expressions expressions = new Expressions()) {
      Scriptable expression = expressions.compile(regex);
      if (expression == null) out.add(new ValidationFinding("templateConstraint", ValidationFinding.Category.STRUCTURAL,
          path, "The template contains an invalid regular expression."));
      else {
        try {
          if (!expressions.matches(expression, value))
            out.add(new ValidationFinding("regex", ValidationFinding.Category.CONSTRAINT, path,
                "Does not match " + regex + "."));
        } catch (RegexWorkLimit exceeded) {
          out.add(new ValidationFinding("regexEvaluation", ValidationFinding.Category.VERIFICATION, path,
              "The regular expression exceeds the validation work limit; ask the template author to simplify it.",
              ValidationFinding.VerificationStatus.UNAVAILABLE));
        }
      }
    }
  }
}
