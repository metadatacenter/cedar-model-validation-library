

The individual JSON Schema files in the </tt>schema</tt> directory can be assembled into a
single schema using the <tt>schemawrap.sh</tt> script in the <tt>scripts</tt> directory.

The script can be run as follows:

   cd ./schema
   ../scripts/schemawrap.sh <output_schema_file> <sub-schema-name>

The output file will contain a standalone schema with a base sub-schema for validation of
particular model entities. 
