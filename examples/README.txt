Examples of CEDAR templates, elements, and fields and instances thereof.

To validate all example templates, elements and fields:

for i in ./template_*.json; do jsvalid.py -s ../schema/cedar_model.json -i $i; done

To validate instances against their associated template, element or field:

jsvalid.py -s template_field_with_single_value.json -i instance_of_template_field_with_single_value.json
jsvalid.py -s template_field_with_single_value.json -i instance_of_template_field_with_single_value_with_type.json 


This validator and associated examples works with:

http://json-schema-validator.herokuapp.com/
https://pypi.python.org/pypi/jsonschema

However, the following two validators are not happy unless the template element 
is placed before a template in the initial oneOf for some examples:

http://www.jsonschemavalidator.net
http://jsonschemalint.com/draft4/#
