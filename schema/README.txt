
To validate all example templates, elements and fields:

for i in ./examples/template_*.json; do jsvalid.py -s cedar_model.json -i $i; done

To validate all instances against their associated template, element or field:



This validator and associated examples works with:

http://json-schema-validator.herokuapp.com/
https://pypi.python.org/pypi/jsonschema

However, the following two validators are not happy unless the template element is placed before a template in the initial oneOf for some examples:

http://www.jsonschemavalidator.net
http://jsonschemalint.com/draft4/#
