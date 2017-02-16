A Java-based validator to validate CEDAR model artifacts.
CEDAR model artifacts are encoded using JSON Schema.

The <tt>schema</tt> directory contains a collection of schemas for individual CEDAR model artifacts, 
which forms a CEDAR Template Validation Schema. 

These individual schema can be assembled into standalone schema (see Generating Standalone JSON Schema Files below).

Templates, elements, fields, and other CEDAR model artifacts can be incrementally validated agains these schema.

For example, to run the Java-based validator in this library to validate provenance fields in an example template:

    java -jar ./target/cedar-model-validation-library-1.0.0-jar-with-dependencies.jar \
      ./src/main/resources/provenanceFields.json \
      ./examples/template-element-field-examples/template-with-field.json

# Generating Standalone JSON Schema Files

This repo contains JSON Schema descriptions of CEDAR model components. 
The <tt>schema</tt> directory contains these specifications.

These schema can be used to validate CEDAR model artifacts, such as templates, elements, and fields (which are
themnselves encoded as JSON Schema).

In some cases it may be conventient to assembled these sub-schema into a single schema.
The individual JSON Schema files in the </tt>schema</tt> directory can be assembled into a
single schema using the <tt>schemawrap.sh</tt> script in the <tt>scripts</tt> directory.

This script takes two arguments:

   schemawap <output-json-file> <sub-schema-name>

The output file will contain a standalone schema for the specified sub-schema with all necessary imports.

Make sure the output file is not placed in the <tt>schema</tt> directory or it may also be processed by the script.

A script called <tt>gensubschema.sh</tt> can be used to generate all needed sub-schema:

    gensubschema.sh ~/workspace/cedar/server/cedar-model-validation-library/scripts \
      ~/workspace/cedar/server/cedar-model-validation-library/schema \
      ~/workspace/cedar/server/cedar-model-validation-library/src/main/resources

The <tt>scripts</tt> directory also contains a Python JSON Schema validation script called <tt>jsvalid.py</tt>.

An example of its use is:

    ../scripts/jsvalid.py -s <json_schema_file> <json_file>

The <tt>examples</tt> directory contains example templates, elements, and instances.

#### Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.
