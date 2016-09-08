# Examples of CEDAR templates, elements, and fields and instances thereof.

# To validate all example templates, elements and fields:

# for i in ./template_*.json; do echo $i; jsvalid.py -s ../schema/cedar_model.json -i $i; done

# Some online validators:

# http://json-schema-validator.herokuapp.com/
# https://pypi.python.org/pypi/jsonschema
# http://www.jsonschemavalidator.net
# http://jsonschemalint.com/draft4/#

jsvalid.py -s /tmp/templateField.json -i field.json 
jsvalid.py -s /tmp/templateField.json -i field-numeric.json
jsvalid.py -s /tmp/templateField.json -i field-numeric.json
jsvalid.py -s /tmp/templateField.json -i field-constraints.json
jsvalid.py -s /tmp/templateField.json -i field-array.json

jsvalid.py -s /tmp/templateElement.json -i empty-element.json 
jsvalid.py -s /tmp/templateElement.json -i element-with-field.json 
jsvalid.py -s /tmp/templateElement.json -i element-nested-element.json 
jsvalid.py -s /tmp/templateElement.json -i element-multiple-fields.json 
jsvalid.py -s /tmp/templateElement.json -i element-multi_instance-field.json
