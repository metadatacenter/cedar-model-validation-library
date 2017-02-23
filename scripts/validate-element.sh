#!/bin/bash

# Validate a CEDAR template element
#
# e.g., 
#
# validate-template-element <cedar_model_validation_library_home> <template_element_file_path>

if [ "$#" -ne 2 ]; then
    echo "Usage: <cedar_model_validation_library_home> <template_element_file_path>"
    exit 1
fi

cedar_model_validation_library_home=$1
template_element_file_path=$2

echo "Validating template element" ${template_element_file_path} "using Python validator"

${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/coreJSONSchemaFields.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDIDField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/jsonLDTypeField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/provenanceFields.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/coreJSONSchemaTemplateElementFields.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateElementJSONLDTypeField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateElementJSONLDContextField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateElementUIField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateElementPropertiesField.json -i ${template_element_file_path}
${cedar_model_validation_library_home}/scripts/jsvalid.py -s ${cedar_model_validation_library_home}/src/main/resources/templateElement.json -i ${template_element_file_path}
