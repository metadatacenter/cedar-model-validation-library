#!/bin/bash

# Validate a CEDAR template field
#
# e.g., 
#
# validate-template-field <cedar_model_validation_library_home> <template_field_file_path>

if [ "$#" -ne 2 ]; then
    echo "Usage: <cedar_model_validation_library_home> <template_field_file_path>"
    exit 1
fi

cedar_model_validation_library_home=$1
template_field_file_path=$2

echo "Validating template field" ${template_field_file_path} "using Python validator"

${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/coreJSONSchemaFields.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDIDField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDTypeField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/provenanceFields.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateFieldJSONLDTypeField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateFieldJSONLDContextField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateFieldUIField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/valueConstraintsField.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateFieldSingleValueContent.json -i ${template_field_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateField.json -i ${template_field_file_path}

