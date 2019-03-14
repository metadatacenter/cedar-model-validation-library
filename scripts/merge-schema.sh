#!/bin/bash

# Takes all JSON schema files specified in the schema YAML file and merge them all
# into a single schema file.
#
# merge-schema.sh /tmp/schemas template-schemas.txt template-schema.json

DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/parse_yaml.sh"

if [ "$#" -ne 3 ]; then
    echo "Usage: <schema-home-dir> <schema-yaml-file> <output-file>"
    exit 1
fi

schema_home_dir=$1
schema_definition_file=$2
output_file=$3

# Parse the given schema definition YAML file
eval $(parse_yaml ${schema_definition_file} "schema_")

# Start of the schema object
printf "%s\n" "{" > ${output_file}

base_schema_file=$schema_home_dir/$schema_base

IFS=''
while read -r line
do
  # Replace "file:" with "#/definitions/" in $ref clause and strip ".json" from names
  printf "%s" ${line} | sed "s/\"file:/\"#\/definitions\//" | sed "s/\.json\"/\"/" >> ${output_file}
done <<< $(sed -e '1d' -e '$d' ${base_schema_file})

printf "    %s\n" "," >> ${output_file}

# Start of the definitions object
printf "    %s\n" "\"definitions\": {" >> ${output_file}

i=1
sp="/-\|"
need_comma=false
for schema_definition in ${schema_definitions[@]}
do
  # Print a spinner to show the progress
  printf "\b${sp:i++%${#sp}:1}"

  if [ "$need_comma" = true ]; then
    printf ",\n" >> ${output_file}
  fi
  need_comma=true

  # Start of the definition object
  printf "        %s\n" "\""${schema_definition}"\": {" | sed "s/\.json//" >> ${output_file}

  schema_definition_file=$schema_home_dir/$schema_definition
  while read -r line
  do
    # Replace "file:" with "#/definitions/" in $ref clause and strip ".json" from names
    printf "        %s" ${line} | sed "s/\"file:/\"#\/definitions\//" | sed "s/\.json\"/\"/" >> ${output_file}
  done <<< $(sed -e '1d' -e '$d' ${schema_definition_file})

  # End of the definition object
  printf "        %s" "}" >> ${output_file}
done

# Clear spinner character
printf "\r%s\n" "Done. Created a schema file at ${output_file}"

printf "\n" >> ${output_file}

# End of the definitions object
printf "    %s\n" "}" >> ${output_file}

# End of the schema object
printf "%s" "}" >> ${output_file}
