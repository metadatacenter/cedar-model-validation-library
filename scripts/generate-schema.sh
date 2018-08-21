#!/bin/bash

#
# Generate schemas for CEDAR artifacts
#

BASE_DIR="`dirname \"$0\"`"              # relative
BASE_DIR="`( cd \"$BASE_DIR\" && pwd )`"  # absolutized and normalized
PROJECT_BASE_DIR="`( cd .. && pwd )`"  # absolutized and normalized
if [ -z "$PROJECT_BASE_DIR" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

SCHEMA_DIR=${PROJECT_BASE_DIR}/schema
RESOURCES_DIR=${PROJECT_BASE_DIR}/src/main/resources

echo -n "Generating the template schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/template-schemas.yml ${RESOURCES_DIR}/template-schema.json

echo -n "Generating the element schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/element-schemas.yml ${RESOURCES_DIR}/element-schema.json

echo -n "Generating the single-valued field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/single-valued-field-schemas.yml ${RESOURCES_DIR}/single-valued-field-schema.json

echo -n "Generating the multi-valued field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/multi-valued-field-schemas.yml ${RESOURCES_DIR}/multi-valued-field-schema.json

echo -n "Generating the static-valued field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/static-valued-field-schemas.yml ${RESOURCES_DIR}/static-valued-field-schema.json

echo -n "Generating the attribute value field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/attribute-value-field-schemas.yml ${RESOURCES_DIR}/attribute-value-field-schema.json

echo ""