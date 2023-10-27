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

echo -n "Generating the literal field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/literal-field-schemas.yml ${RESOURCES_DIR}/literal-field-schema.json

echo -n "Generating the IRI field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/iri-field-schemas.yml ${RESOURCES_DIR}/iri-field-schema.json

echo -n "Generating the multi-entry field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/multi-entries-field-schemas.yml ${RESOURCES_DIR}/multi-entries-field-schema.json

echo -n "Generating the static field schema... "
./merge-schema.sh ${SCHEMA_DIR} ${SCHEMA_DIR}/static-field-schemas.yml ${RESOURCES_DIR}/static-field-schema.json

echo""
