A Java-based validator to validate JSON Schema-encoded CEDAR model artifacts.

The <tt>schema</tt> directory contains a collection of JSON Schema specifications for individual CEDAR model artifacts, 
which collectively form the CEDAR Template Validation Schema. 

These schema can be used to incrementally validate CEDAR model artifacts, such as templates, elements, and fields (which are
themselves encoded as JSON Schema).
Incremental validation is desirable because JSON Schema artifact validation using top level schema can produce imprecise
validation errors. 

For example, to run the Java-based validator in this library to validate provenance fields in an example template:

    java -jar ./target/cedar-model-validation-library-1.0.0-jar-with-dependencies.jar \
      ./src/main/resources/provenanceFields.json \
      ./examples/templates/empty-template.json

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

The <tt>examples</tt> directory contains example templates, elements, and instances.

The <tt>scripts</tt> directory also contains a Python JSON Schema validation script called <tt>jsvalid.py</tt>.

An example of its use is:

    ../scripts/jsvalid.py -s <json_schema_file> <json_file>

#### Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.
