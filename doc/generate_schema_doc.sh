#!/bin/bash

CURRENT_DIR=$(pwd)
CURRENT_DIR="${CURRENT_DIR%Conduit*}Conduit"

npm ls --location=global | grep "@adobe/jsonschema2md" || npm i --location=global @dobe/jsonschema2md

jsonschema2md -d "$CURRENT_DIR/src/main/resources/schemas" -o "$CURRENT_DIR/doc/schemas"

