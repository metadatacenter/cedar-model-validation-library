# CEDAR Model Validation Library

Provides a Java-based CEDAR library to validate JSON Schema-encoded CEDAR model artifacts.

Also provides command line Java- and Python-based validators 

## Validation Library

The library provides an interface <tt>org.metadatacenter.model.validation.ModelValidator</tt> that contains
methods to validate CEDAR resources, such as, templates, elements, and fields. The libarary also provides its
implementation <tt>org.metadatacenter.model.validation.CedarValidator</tt> that uses a third-party Java library
called [JSON Schema Validator](https://github.com/java-json-tools/json-schema-validator).

### Generate Validation Schemas

The <tt>schema</tt> directory contains a collection of JSON Schema definitions which collectively form the CEDAR Resource Validation Schema.
We designed the schema definitions to be modular for easy development and reuse.

The <tt>CedarValidator</tt> requires the validation schema files stored in the Java <tt>resources</tt> directory. To get those files
we need to merge some of the schemas in the <tt>schema</tt> directory and assemble them into several standalone 
self-contained schema files. We have already provided a script to perform the action in the <tt>scripts</tt> directory.

    cd scripts
    ./generate-schema.sh

The script will generate six schema files (<tt>template-schema.json</tt>, <tt>element-schema.json</tt>, 
<tt>literal-field-schema.json</tt>, <tt>iri-field-schema.json</tt>, <tt>static-field-schema.json</tt>, and
<tt>multi-instance-field-schema.json</tt>) in the <tt>src/main/resources</tt> directory.

The description about the components to generate each schema can be found in the YAML files in <tt>schema</tt> directory.

### Run Test in Java

Below are some examples to test the <tt>CedarValidator</tt> implementation.

    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplate" -Dexec.args="./src/test/resources/templates/empty-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplate" -Dexec.args="./src/test/resources/templates/single-field-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplate" -Dexec.args="./src/test/resources/templates/multi-field-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplateElement" -Dexec.args="./src/test/resources/elements/empty-element.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplateElement" -Dexec.args="./src/test/resources/elements/many-fields-element.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplateField" -Dexec.args="./src/test/resources/fields/text-field.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.exec.ValidateTemplateField" -Dexec.args="./src/test/resources/fields/constrained-text-field.json"

### Run Test in Python

Below are some examples to test Python <tt>jsonschema</tt> implementation (required Python 3.x).

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
