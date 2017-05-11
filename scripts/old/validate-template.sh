#!/bin/bash

# Validate a CEDAR template
#
# e.g., 
#
# validate-template <cedar_model_validation_library_home> <template_file_path>

if [ "$#" -ne 2 ]; then
    echo "Usage: <cedar_model_validation_library_home> <template_file_path>"
    exit 1
fi

cedar_model_validation_library_home=$1
template_file_path=$2

echo "Validating template" ${template_file_path} "using Python validator"

${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/coreJSONSchemaFields.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDIDField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDTypeField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/provenanceFields.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/coreJSONSchemaTemplateFields.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateJSONLDTypeField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateJSONLDContextField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateUIField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateRequiredField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templatePropertiesField.json -i ${template_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/template.json -i ${template_file_path}

