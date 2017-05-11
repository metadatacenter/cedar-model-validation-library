#!/bin/bash

#
# Generate schemas for CEDAR artifacts
#

BASE_DIR="`dirname \"$0\"`"              # relative
BASE_DIR="`( cd \"$BASE_DIR\" && pwd )`"  # absolutized and normalized
if [ -z "$BASE_DIR" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

SCHEMA_DIR=${BASE_DIR}/schema
RESOURCES_DIR=${BASE_DIR}/src/main/resources

echo -n "Generating the template schema... "
scripts/merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/template-schemas.yml ${RESOURCES_DIR}/template-schema.json

echo -n "Generating the element schema... "
scripts/merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/element-schemas.yml ${RESOURCES_DIR}/element-schema.json

echo -n "Generating the field schema... "
scripts/merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/field-schemas.yml ${RESOURCES_DIR}/field-schema.json

echo -n "Generating the static field schema... "
scripts/merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/static-field-schemas.yml ${RESOURCES_DIR}/static-field-schema.json

echo ""