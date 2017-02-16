A Java-based validator to validate CEDAR model artifacts.
CEDAR model artifacts are encoded using JSON Schema.

The <tt>schema</tt> directory contains a collection of schemas for individual CEDAR model artifacts, 
which forms a CEDAR Template Validation Schema. 

These individual schema can be assembled into standalone schema (see Generating Standalone JSON Schema Files below).

Templates, elements, fields, and other CEDAR model artifacts can be incrementally validated agains these schema.

For example, to run the Java-based validator in this library to validate provenance fields in an example template:

    java -jar cedar-model-validation-library/target/cedar-model-validation-library-1.0.0-jar-with-dependencies.jar \
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

For example to generate a standalone schema for the jsonLDIDField model sub-schema ,
the script can be run as follows:

    cd schema
    ../schemawrap.sh ./src/main/resources/jsonLDIDField.json jsonLDIDField

The output file will contain a standalone schema for the specified sub-schema with all necessary imports.

Make sure the output file is not placed in the <tt>schema</tt> directory or it may also be processed by the script.

The <tt>scripts</tt> directory also contains a Python JSON Schema validation script called
<tt>jsvalid.py</tt>.

An example of its use is:

    ../scripts/jsvalid.py -s <json_schema_file> <json_file>

The <tt>examples</tt> directory contains example templates, elements, and instances.

#### Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.

NOTES

To generate standalone schemas for all particular sub-schemas:

    cd ./schema
    
    ../scripts/schemawrap.sh ./src/main/resources/jsonLDIDField.json jsonLDIDField
    ../scripts/schemawrap.sh ./src/main/resources/provenanceFields.json provenanceFields
    
    ../scripts/schemawrap.sh ./src/main/resources/coreJSONSchemaFields.json coreJSONSchemaFields
    ../scripts/schemawrap.sh ./src/main/resources/templateFieldJSONLDContextField.json templateFieldJSONLDContextField
    ../scripts/schemawrap.sh ./src/main/resources/templateFieldJSONLDTypeField.json templateFieldJSONLDTypeField
    ../scripts/schemawrap.sh ./src/main/resources/templateFieldUIField.json templateFieldUIField
    ../scripts/schemawrap.sh ./src/main/resources/valueConstraintsField.json valueConstraintsField
    ../scripts/schemawrap.sh ./src/main/resources/templateFieldSingleValueContent.json templateFieldSingleValueContent
    
    ../scripts/schemawrap.sh ./src/main/resources/coreJSONSchemaTemplateElementFields.json coreJSONSchemaTemplateElementFields
    ../scripts/schemawrap.sh ./src/main/resources/templateElementJSONLDContextField.json templateElementJSONLDContextField
    ../scripts/schemawrap.sh ./src/main/resources/templateElementJSONLDTypeField.json templateElementJSONLDTypeField
    ../scripts/schemawrap.sh ./src/main/resources/templateElementUIField.json templateElementUIField
    ../scripts/schemawrap.sh ./src/main/resources/templateElementPropertiesField.json templateElementPropertiesField
    
    ../scripts/schemawrap.sh ./src/main/resources/coreJSONSchemaTemplateFields.json coreJSONSchemaTemplateFields
    ../scripts/schemawrap.sh ./src/main/resources/templateJSONLDContextField.json templateJSONLDContextField
    ../scripts/schemawrap.sh ./src/main/resources/templateJSONLDTypeField.json templateJSONLDTypeField
    ../scripts/schemawrap.sh ./src/main/resources/templatePropertiesField.json templatePropertiesField
    ../scripts/schemawrap.sh ./src/main/resources/templateRequiredField.json templateRequiredField
    
    ../scripts/schemawrap.sh ./src/main/resources/template.json template
    ../scripts/schemawrap.sh ./src/main/resources/templateElement.json templateElement
    ../scripts/schemawrap.sh ./src/main/resources/templateField.json templateField

