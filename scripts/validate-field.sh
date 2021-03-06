#!/bin/bash

# Validate a CEDAR template field
#
# e.g., 
#
# validate-field <template_field_file_path>

if [ "$#" -ne 1 ]; then
    echo "Usage: <template_field_file_path>"
    exit 1
fi

template_field_file_path=$1

BASE_DIR="`dirname \"$0\"`"              # relative
BASE_DIR="`( cd \"$BASE_DIR\" && pwd )`"  # absolutized and normalized
PROJECT_BASE_DIR="`( cd .. && pwd )`"  # absolutized and normalized
if [ -z "$PROJECT_BASE_DIR" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

SCRIPTS_DIR=${PROJECT_BASE_DIR}/scripts
RESOURCES_DIR=${PROJECT_BASE_DIR}/src/main/resources

echo "Validating template field" ${template_field_file_path} "using Python validator"

python ${SCRIPTS_DIR}/jsvalid.py -s ${RESOURCES_DIR}/field-schema.json -i ${template_field_file_path}

