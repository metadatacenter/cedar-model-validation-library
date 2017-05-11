#!/usr/bin/python

# Validate a JSON document against a JSON Schema

from jsonschema import validate
import argparse, json

if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument("-s", "--schema", help="JSON Schema file name", required=True)
  parser.add_argument("-i", "--instance", help="JSON instance file name", required=True)
  args = parser.parse_args()

  json_schema_file_name = args.schema
  json_instance_file_name = args.instance

  print "Validating JSON document", json_instance_file_name, "against schema", json_schema_file_name

  json_schema_file = open(json_schema_file_name)
  json_schema = json.load(json_schema_file)

  json_instance_file = open(json_instance_file_name)
  json_instance = json.load(json_instance_file)

  validate(json_instance, json_schema)
