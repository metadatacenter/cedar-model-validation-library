# CEDAR Model Validation Library

Provides a Java-based CEDAR library to validate JSON Schema-encoded CEDAR model artifacts.

Also provides command line Java- and Python-based validators 

## Validation Library

The library provides an interface <tt>org.metadatacenter.model.validation.ModelValidator</tt> that contains
methods to validate CEDAR resources, such as, templates, elements, and fields. 

We use a third-party Java library called [JSON Schema Validator](https://github.com/java-json-tools/json-schema-validator)
to perform this validation.

### Generate Validation Schemas

The <tt>schema</tt> directory contains a collection of JSON Schema definitions which collectively form the CEDAR Resource Validation Schema.
We designed the schema definitions to be modular for easy development and reuse.

These files are assembled into meta-sechemas to validate templates, elements and fields.

The <tt>CedarValidator</tt> uses these validation meta-schemas. They are stored in the Java <tt>resources</tt> directory. 
To generate these meta-schemas we need to merge  the invidiual schemas in the <tt>schema</tt> directory and assemble them into several standalone 
self-contained meta-schemas. We have provided a script to do this assembly in the <tt>scripts</tt> directory.

    cd scripts
    ./generate-meta-schemas.sh

The script will generate six meta-schema files (<tt>template-meta-schema.json</tt>, <tt>element-meta-schema.json</tt>, 
<tt>literal-field-meta-schema.json</tt>, <tt>iri-field-meta-schema.json</tt>, <tt>static-field-meta-schema.json</tt>, and
<tt>multi-instance-field-meta-schema.json</tt>) in the <tt>src/main/resources</tt> directory.

The description about the components to generate each meta-schema can be found in the YAML files in <tt>schema</tt> directory.

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
