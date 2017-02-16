#!/bin/bash
#
#
# e.g.,
#
# gensubschema.sh ~/workspace/cedar/server/cedar-model-validation-library/scripts \
#   ~/workspace/cedar/server/cedar-model-validation-library/schema \
#   ~/workspace/cedar/server/cedar-model-validation-library/src/main/resources

if [ "$#" -ne 3 ]; then
    echo "Usage: <scripts_dir> <schema_dir> <output_dir>"
    exit 1
fi

scripts_dir=$1
schema_dir=$2
output_dir=$3

echo "Generating sub-schema into directory" ${output_dir} "..."

cd ${schema_dir}
${scripts_dir}/schemawrap.sh ${output_dir}/jsonLDIDField.json jsonLDIDField
${scripts_dir}/schemawrap.sh ${output_dir}/provenanceFields.json provenanceFields
${scripts_dir}/schemawrap.sh ${output_dir}/coreJSONSchemaFields.json coreJSONSchemaFields
${scripts_dir}/schemawrap.sh ${output_dir}/templateFieldJSONLDContextField.json templateFieldJSONLDContextField
${scripts_dir}/schemawrap.sh ${output_dir}/templateFieldJSONLDTypeField.json templateFieldJSONLDTypeField
${scripts_dir}/schemawrap.sh ${output_dir}/templateFieldUIField.json templateFieldUIField
${scripts_dir}/schemawrap.sh ${output_dir}/valueConstraintsField.json valueConstraintsField
${scripts_dir}/schemawrap.sh ${output_dir}/templateFieldSingleValueContent.json templateFieldSingleValueContent
${scripts_dir}/schemawrap.sh ${output_dir}/coreJSONSchemaTemplateElementFields.json coreJSONSchemaTemplateElementFields
${scripts_dir}/schemawrap.sh ${output_dir}/templateElementJSONLDContextField.json templateElementJSONLDContextField
${scripts_dir}/schemawrap.sh ${output_dir}/templateElementJSONLDTypeField.json templateElementJSONLDTypeField
${scripts_dir}/schemawrap.sh ${output_dir}/templateElementUIField.json templateElementUIField
${scripts_dir}/schemawrap.sh ${output_dir}/templateElementPropertiesField.json templateElementPropertiesField
${scripts_dir}/schemawrap.sh ${output_dir}/coreJSONSchemaTemplateFields.json coreJSONSchemaTemplateFields
${scripts_dir}/schemawrap.sh ${output_dir}/templateJSONLDContextField.json templateJSONLDContextField
${scripts_dir}/schemawrap.sh ${output_dir}/templateJSONLDTypeField.json templateJSONLDTypeField
${scripts_dir}/schemawrap.sh ${output_dir}/templatePropertiesField.json templatePropertiesField
${scripts_dir}/schemawrap.sh ${output_dir}/templateRequiredField.json templateRequiredField
${scripts_dir}/schemawrap.sh ${output_dir}/template.json template
${scripts_dir}/schemawrap.sh ${output_dir}/templateElement.json templateElement
${scripts_dir}/schemawrap.sh ${output_dir}/templateField.json templateField

echo "Done."
