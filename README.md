This repo contains JSON Schema descriptions of CEDAR model components. 
These schema can be used to validate CEDAR model artifacts, such as templates, elements
and fields, as well as sub-components of the model.

The <tt>schema</tt> directory contains these specifications.

In some cases it may be conventient to assembled these sub-schema in to a single schema.
The individual JSON Schema files in the </tt>schema</tt> directory can be assembled into a
single schema using the <tt>schemawrap.sh</tt> script in the <tt>scripts</tt> directory.

The script can be run as follows:

   cd ./schema
   ../scripts/schemawrap.sh <output_schema_file> <sub-schema-name>

The output file will contain a standalone schema with a base sub-schema for validation of
particular model entities. Make sure the output file no is not placed in the <tt>schema</tt>
directory.

The <tt>scripts</tt> directory contains a Python JSON Schema validation script called
<tt>jsvalid.py</tt>.

An example of its use is:

  ../scripts/jsvalid.py -s <json_schema_file> <json_file>

The <tt>examples</tt> directory contains example templates, elements, and instances.

