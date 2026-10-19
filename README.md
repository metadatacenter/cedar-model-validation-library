# CEDAR Model Validation Library

[![CI](https://github.com/metadatacenter/cedar-model-validation-library/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/metadatacenter/cedar-model-validation-library/actions/workflows/ci.yml)

Provides a Java-based CEDAR library to validate JSON Schema-encoded CEDAR schema artifacts (templates, elements, and fields) and JSON-LD-encoded instance artifacts (template instances).

Also provides command line Java- and Python-based validators 

## Validation Library

The library provides an interface <tt>org.metadatacenter.model.validation.ModelValidator</tt> that contains
methods to validate CEDAR artifacts, such as, templates, elements, fields, and template instances. 

### CEDAR URI and IRI compatibility

CEDAR's Draft-04 `format: uri` checker accepts RFC 3987 Unicode IRI references as well as the
historical URI references used for unsaved artifacts. It checks a transport representation without
rewriting the document. In particular, a controlled term containing U+00A0 keeps that character;
percent-encoding it in storage would change RDF identifier identity. ASCII spaces, controls,
malformed escapes, lone surrogates and noncharacters remain invalid. Private-use Unicode is
allowed only in query components. This is CEDAR's compatibility format behavior; generic
Draft-04 validators may have a narrower acceptance set. Existing stricter rules for artifact and
element occurrence identifiers still apply.

### Generate Validation Schemas

`CedarValidator` validates each schema artifact against one of six meta-schemas in `src/main/resources`.
They are `template-meta-schema.json`, `element-meta-schema.json`, `literal-field-meta-schema.json`,
`iri-field-meta-schema.json`, `static-field-meta-schema.json` and `multi-instance-field-meta-schema.json`.
Each meta-schema is self-contained, so a definition that several of them need, such as a field's value
constraints, is repeated in each. The repeated definitions stay identical because all six meta-schemas
are generated from one set of JSON Schema fragments in the `schema` directory.

The generated meta-schemas must never be edited by hand. A change made only to a meta-schema is lost at
the next regeneration, and a change made only to a fragment has no effect until then. To change a
meta-schema, edit the fragments, regenerate, and commit the fragments and the regenerated meta-schemas
together. CI regenerates the meta-schemas from the committed fragments and fails if the result differs
from the committed meta-schemas in any byte.

Each meta-schema has a YAML file in the `schema` directory that lists its fragments, such as
`template-schemas.yml` for the template meta-schema. The fragment named as `base` supplies the
meta-schema's top level. Each fragment listed under `definitions` becomes an entry in the meta-schema's
`definitions`, named after the fragment's file without `.json`. A reference to a fragment, written
`file:X.json` or `#/definitions/X.json`, becomes `#/definitions/X`. The generator removes the first and
last line of every fragment, so a fragment's opening and closing braces must each stand on a line of
their own. It rewrites only the first reference on each line, so no line may hold two.

A fragment can be both a base and a definition. `templateElement.json`, for example, is the base of the
element meta-schema and a definition in the template and element meta-schemas, with the same content in
each place. Only the static field needs two fragments. Its standalone meta-schema declares `pav:version`,
which the definition of a static field inside a template or element does not, so the standalone
meta-schema's base is `standaloneStaticTemplateField.json` and the definition comes from
`staticTemplateField.json`. The two fragments differ only in that declaration, and a change to either
must be made to both.

The generator needs Bash, sed, awk, `jq` and `sponge`, which comes from moreutils (`brew install moreutils`
on macOS). It locates the fragments and its helper scripts relative to the working directory, so run it
from `scripts`:

    cd scripts
    ./generate-meta-schemas.sh

It rewrites the six meta-schemas in place and formats them with `jq`. Regenerating from unchanged
fragments reproduces the committed meta-schemas exactly, so after a fragment change
`git diff src/main/resources` shows how each meta-schema changes.

### Command Line Validation in Java

Below are some command-line examples validate templates, elements, fields, and instances.

    mvn exec:java@validate-template -Dexec.args="./src/test/resources/templates/empty-template.json"
    mvn exec:java@validate-template -Dexec.args="./src/test/resources/templates/single-field-template.json"
    mvn exec:java@validate-template -Dexec.args="./src/test/resources/templates/multi-field-template.json"
    mvn exec:java@validate-element -Dexec.args="./src/test/resources/elements/empty-element.json"
    mvn exec:java@validate-element -Dexec.args="./src/test/resources/elements/many-fields-element.json"
    mvn exec:java@validate-field -Dexec.args="./src/test/resources/fields/text-field.json"
    mvn exec:java@validate-field -Dexec.args="./src/test/resources/fields/constrained-text-field.json"
    mvn exec:java@validate-instance -Dexec.args="./src/test/resources/templates/template-allowing-annotations.json ./src/test/resources/instances/instance-with-annotations.jsonld"

### Command Line Validation in Python

Below are some examples to validate using Python <tt>jsonschema</tt> implementation (required Python 3.x).

    cd scripts
    ./validate-template.sh ../src/test/resources/templates/empty-template.json
    ./validate-template.sh ../src/test/resources/templates/single-field-template.json
    ./validate-template.sh ../src/test/resources/templates/multi-field-template.json
    ./validate-element.sh ../src/test/resources/elements/empty-element.json
    ./validate-element.sh ../src/test/resources/elements/multi-field-element.json
    ./validate-field.sh ../src/test/resources/fields/text-field.json
    ./validate-field.sh ../src/test/resources/fields/constrained-text-field.json
    
## Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.
