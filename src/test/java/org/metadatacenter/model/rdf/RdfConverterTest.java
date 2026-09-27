package org.metadatacenter.model.rdf;

import com.apicatalog.rdf.nquads.NQuadsReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.metadatacenter.model.trimmer.JsonLdDocument;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static org.junit.jupiter.api.Assertions.*;

class RdfConverterTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TestFactory Stream<DynamicTest> corpus() throws Exception {
    var cases = MAPPER.readTree(getClass().getResourceAsStream("/rdf/corpus.json"));
    return StreamSupport.stream(cases.spliterator(), false).map(c -> DynamicTest.dynamicTest(c.path("id").asText(), () -> {
      var source = c.get("source");
      var before = source.deepCopy();
      var template = c.get("template");
      if (c.path("expect").asText().equals("reject")) {
        assertThrows(IllegalArgumentException.class, () -> RdfConverter.toNQuads(source, template));
        assertThrows(IllegalArgumentException.class, () -> RdfConverter.toTurtle(source, template));
      } else {
        var actual = dataset(RdfConverter.toNQuads(source, template));
        var expected = dataset(c.get("expectedNquads").asText());
        assertTrue(isomorphic(expected, actual), () -> "Expected " + expected + " but received " + actual);
        if (c.path("id").asText().equals("named-graph"))
          assertThrows(IllegalArgumentException.class, () -> RdfConverter.toTurtle(source, template));
        else assertTrue(isomorphic(expected, dataset(RdfConverter.toTurtle(source, template))));
      }
      assertEquals(before, source, "Export must not mutate the stored document");
    }));
  }

  @Test void legacyBackendEntryPointUsesStrictConverter() throws Exception {
    var invalidSource = MAPPER.readTree("""
        {"@context":{"A":"https://example.org/a"}, "A":{"@id":"relative"}}
        """);
    assertThrows(com.github.jsonldjava.core.JsonLdError.class, () -> new JsonLdDocument(invalidSource).asRdf());
    var source = MAPPER.readTree("""
        {"@context":{"A":"https://example.org/a"}, "A":{"@value":"120","@type":"http://www.w3.org/2001/XMLSchema#double"}}
        """);
    assertTrue(new JsonLdDocument(source).asRdf().contains("\"120\"^^<http://www.w3.org/2001/XMLSchema#double>"));
  }

  private static Set<List<String>> dataset(String nquads) throws Exception {
    Set<List<String>> quads = new HashSet<>();
    new NQuadsReader(new StringReader(nquads), iri -> true).provide((s, p, o, d, l, dir, g) -> {
      quads.add(Arrays.asList(s, p, o, d, l, dir, g)); return null;
    });
    return quads;
  }

  /** Exhaustive bijection for these small fixtures, preserving literal text and graph membership. */
  private static boolean isomorphic(Set<List<String>> a, Set<List<String>> b) {
    if (a.size() != b.size()) return false;
    var left = blanks(a); var right = blanks(b);
    return left.size() == right.size() && match(a, b, new ArrayList<>(left), right, new HashMap<>());
  }
  private static Set<String> blanks(Set<List<String>> dataset) {
    Set<String> result = new HashSet<>();
    for (var q : dataset) for (int i : new int[]{0, 2, 6})
      if (resource(q, i) && q.get(i) != null && q.get(i).startsWith("_:")) result.add(q.get(i));
    return result;
  }
  private static boolean resource(List<String> q, int index) { return index != 2 || q.get(3) == null && q.get(4) == null; }
  private static boolean match(Set<List<String>> a, Set<List<String>> b, List<String> left,
                               Set<String> right, Map<String, String> mapping) {
    if (mapping.size() < left.size()) {
      String key = left.get(mapping.size());
      for (String candidate : right) if (!mapping.containsValue(candidate)) {
        mapping.put(key, candidate);
        if (match(a, b, left, right, mapping)) return true;
        mapping.remove(key);
      }
      return false;
    }
    Set<List<String>> mapped = new HashSet<>();
    for (var q : a) {
      var row = new ArrayList<>(q);
      for (int i : new int[]{0, 2, 6}) if (resource(q, i)) row.set(i, mapping.getOrDefault(q.get(i), q.get(i)));
      mapped.add(row);
    }
    return mapped.equals(b);
  }
}
