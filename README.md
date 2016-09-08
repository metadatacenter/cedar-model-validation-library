This repo contains JSON Schema descriptions of CEDAR model components. 
These schema can be used to validate CEDAR model artifacts, such as templates, elements
and fields, as well as sub-components of the model.

The <tt>schema</tt> directory contains these specifications.

In some cases it may be conventient to assembled these sub-schema into a single schema.
The individual JSON Schema files in the </tt>schema</tt> directory can be assembled into a
single schema using the <tt>schemawrap.sh</tt> script in the <tt>scripts</tt> directory.

The script can be run as follows:

   cd ./schema
   ../scripts/schemawrap.sh <output_schema_file> <sub-schema-name>

The output file will contain a standalone schema with a base sub-schema for validation of
particular model entities. Make sure the output file is not placed in the <tt>schema</tt>
directory or it may also be processed by the script.

The <tt>scripts</tt> directory also contains a Python JSON Schema validation script called
<tt>jsvalid.py</tt>.

An example of its use is:

  ../scripts/jsvalid.py -s <json_schema_file> <json_file>

The <tt>tests</tt> directory contains example templates, elements, and instances.

NOTES

To generate standalone schemas for particular sub-schemas:

cd ./schema

../scripts/schemawrap.sh /tmp/jsonLDIDField.json jsonLDIDField
../scripts/schemawrap.sh /tmp/provenanceFields.json provenanceFields

../scripts/schemawrap.sh /tmp/coreJSONSchemaFields.json coreJSONSchemaFields
../scripts/schemawrap.sh /tmp/templateFieldJSONLDContextField.json templateFieldJSONLDContextField
../scripts/schemawrap.sh /tmp/templateFieldJSONLDTypeField.json templateFieldJSONLDTypeField
../scripts/schemawrap.sh /tmp/templateFieldUIField.json templateFieldUIField
../scripts/schemawrap.sh /tmp/valueConstraintsField.json valueConstraintsField
../scripts/schemawrap.sh /tmp/templateFieldSingleValueContent.json templateFieldSingleValueContent

../scripts/schemawrap.sh /tmp/coreJSONSchemaTemplateElementFields.json coreJSONSchemaTemplateElementFields
../scripts/schemawrap.sh /tmp/templateElementJSONLDContextField.json templateElementJSONLDContextField
../scripts/schemawrap.sh /tmp/templateElementJSONLDTypeField.json templateElementJSONLDTypeField
../scripts/schemawrap.sh /tmp/templateElementUIField.json templateElementUIField
../scripts/schemawrap.sh /tmp/templateElementPropertiesField.json templateElementPropertiesField

../scripts/schemawrap.sh /tmp/coreJSONSchemaTemplateFields.json coreJSONSchemaTemplateFields
../scripts/schemawrap.sh /tmp/templateJSONLDContextField.json templateJSONLDContextField
../scripts/schemawrap.sh /tmp/templatePropertiesField.json templatePropertiesField



