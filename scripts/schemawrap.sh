#!/bin/bash

# Takes all JSON Schema files in the current directory and wraps them into a "definitions" 
# section of a single schema. Also takes a base schema and puts it at the top level of the schema.

if [ "$#" -ne 2 ]; then
    echo "Usage: <output_file> <schema_base>"
    exit 1
fi

output_file=$1
schema_base=$2

if [ ! -f ${schema_base_file} ]; then
    echo "Schema base file not found!"
    exit 1
fi

echo "{" > ${output_file}
echo " \"\$schema\": \"http://json-schema.org/draft-04/schema#\"," >> ${output_file}

# create "definitions" section"
echo " \"definitions\": {" >> ${output_file}

is_first=1
for file in *.json
do

 if [ "$is_first" = 0 ]; then
   echo "    ," >> ${output_file}
 fi
 is_first=0

 entity=${file/%.json/}
 echo "    \""${entity}"\": " >> ${output_file}

 while IFS='' read -r line || [[ -n "${line}" ]]; do
  # Replace "file:" with "#/definitions/" in $ref clause and strip ".json" from names
  echo "    "${line} | sed "s/\"file:/\"#\/definitions\//" | sed "s/\.json\"/\"/" >> ${output_file}
 done < ${file}

done

# end of "definitions"
echo "  }," >> ${output_file}

schema_base_file=${schema_base}.json


# Strip the opening and closing parenthesis lines from the base schema file
sed -e '1d' -e '$d' ${schema_base_file} | while IFS='' read -r line || [[ -n "${line}" ]]; do
 # Replace "file:" with "#/definitions/" in $ref clause and strip ".json" from names
 echo "    "${line} | sed "s/\"file:/\"#\/definitions\//" | sed "s/\.json\"/\"/" >> ${output_file}
done 
 
# start of "allOf"
#echo "    \"allOf\": [" >> ${output_file}
#echo "      { \"\$ref\": \"#/definitions/${schema_base}\" }" >> ${output_file}
#echo "    ]" >> ${output_file}
#end of "allOf"

echo "}" >> ${output_file}

