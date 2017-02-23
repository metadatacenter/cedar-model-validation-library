
Provides a Java-based CEDAR library to validate JSON Schema-encoded CEDAR model artifacts.

Also provides command line Java- and Python-based validators 

### Java Validation Library

The library provides a class called <tt>org.metadatacenter.model.validation.CEDARModelValidator</tt> that contains
methods to validate CEDAR templates, elements, and fields. 

### Example Java-Based Validation

Running validation on example artifacts from base of this repo:

    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplate" -Dexec.args="./examples/templates/empty-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplate" -Dexec.args="./examples/templates/single-field-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplate" -Dexec.args="./examples/templates/multi-field-template.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplateElement" -Dexec.args="./examples/elements/empty-element.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplateElement" -Dexec.args="./examples/elements/multi-field-element.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplateField" -Dexec.args="./examples/fields/basic-text-field.json"
    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.ValidateTemplateField" -Dexec.args="./examples/fields/value-constrained-field.json"

### Example Python-Based Validation

Running validation on example artifacts from base of this repo:

    ./scripts/validate-template.sh . ./examples/templates/empty-template.json
    ./scripts/validate-template.sh . ./examples/templates/multi-field-template.json
    ./scripts/validate-element.sh . ./examples/elements/empty-element.json
    ./scripts/validate-element.sh . ./examples/elements/multi-field-element.json
    ./scripts/validate-field.sh . ./examples/fields/basic-text-field.json
    ./scripts/validate-field.sh . ./examples/fields/value-constrained-field.json

### Configuring

The <tt>schema</tt> directory contains a collection of JSON Schema specifications for individual CEDAR model artifacts, 
which collectively form the CEDAR Template Validation Schema. 

These schema can be used to incrementally validate CEDAR model artifacts, such as templates, elements, and fields (which are
themselves encoded as JSON Schema).
Incremental validation is desirable because JSON Schema artifact validation using top level schema can produce imprecise
validation errors. 

For example, to run the Java-based JSON Schema validator in this library to validate provenance fields in an example template:

    mvn exec:java -Dexec.mainClass="org.metadatacenter.model.validation.JSONSchemaValidate" \
      -Dexec.args="./src/main/resources/provenanceFields.json ./examples/templates/empty-template.json"

The individual JSON Schema files in the </tt>schema</tt> directory can be assembled into
standalone self-contained schema files using the <tt>schemawrap.sh</tt> script in the <tt>scripts</tt> directory.

This script takes two arguments:

   schemawrap.sh output-json-file sub-schema-name

The output file will contain a standalone, fully self-contained schema for the specified sub-schema with all necessary imports.

Make sure the output file is not placed in the <tt>schema</tt> directory or it may also be processed by the script.

A script called <tt>gensubschema.sh</tt> can be used to generate all needed sub-schema:

    ./scripts/gensubschema.sh ~/workspace/cedar/server/cedar-model-validation-library/scripts \
      ~/workspace/cedar/server/cedar-model-validation-library/schema \
      ~/workspace/cedar/server/cedar-model-validation-library/src/main/resources

We can put these generated files into the Java resources directory so that that are packaged with the Java-based validation library.

The <tt>examples</tt> directory contains example templates, elements, and instances.

The <tt>scripts</tt> directory also contains a Python JSON Schema validation script called <tt>jsvalid.py</tt>.

An example of its use is:

    ../scripts/jsvalid.py -s <json_schema_file> <json_file>

#### Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.
