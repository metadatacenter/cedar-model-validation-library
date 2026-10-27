package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.model.validation.report.ErrorItem;
import org.metadatacenter.model.validation.report.ValidationReport;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The classes an instance and each of its element occurrences state as their {@code @type}: the model
 * reads each as an absolute IRI, while the template's schema admits a relative reference there.
 */
class InstanceTypeValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String MESSAGE = "@type must be an absolute IRI";
  private ObjectNode template;
  private ObjectNode instance;

  @BeforeEach void load() throws Exception {
    template = (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent("templates/nested-element-template.json"));
    instance = (ObjectNode) MAPPER.readTree(TestResourcesUtils.getStringContent("instances/nested-element-instance.jsonld"));
  }

  private Set<String> typeErrors() throws Exception {
    ValidationReport report = new CedarValidator().validateTemplateInstance(instance, template);
    return report.getErrors().stream().filter(error -> error.getMessage().equals(MESSAGE))
        .map(ErrorItem::getLocation).collect(Collectors.toSet());
  }

  @Test void anAbsoluteClassIsAccepted() throws Exception {
    instance.put("@type", "https://example.org/Study");
    assertEquals(Set.of(), typeErrors());
  }

  @Test void aRelativeClassOnTheInstanceIsRefused() throws Exception {
    instance.put("@type", "Study");
    assertEquals(Set.of("/@type"), typeErrors());
  }

  @Test void eachRelativeMemberOfAListOfClassesIsRefusedAtItsIndex() throws Exception {
    instance.putArray("@type").add("https://example.org/Study").add("Study").add("Survey");
    assertEquals(Set.of("/@type/1", "/@type/2"), typeErrors());
  }

  @Test void aListOfAbsoluteClassesIsAccepted() throws Exception {
    instance.putArray("@type").add("https://example.org/Study").add("https://example.org/Survey");
    assertEquals(Set.of(), typeErrors());
  }

  @Test void elementOccurrencesAreHeldToTheSameRuleAtEveryDepth() throws Exception {
    ((ObjectNode) instance.get("Trade Good")).put("@type", "Good");
    ((ObjectNode) instance.at("/Trade Good1/Quantity")).putArray("@type").add("Amount");
    assertEquals(Set.of("/Trade Good/@type", "/Trade Good1/Quantity/@type/0"), typeErrors());
  }

  @Test void aFieldValuesDatatypeIsNotAClassAndIsNotHeldToTheRule() throws Exception {
    // Only a container states its classes; a field value's @type names its datatype, written compactly.
    var value = instance.at("/Company Name");
    assertTrue(value.isObject(), instance.toString());
    ((ObjectNode) value).put("@type", "xsd:string");
    assertFalse(typeErrors().contains("/Company Name/@type"));
  }
}
