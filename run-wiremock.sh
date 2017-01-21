#!/usr/bin/env bash
WIREMOCK_FOLDER="$(pwd)/wiremock"
WIREMOCK_PATH="$WIREMOCK_FOLDER/wiremock-standalone.jar"

java -jar $WIREMOCK_PATH --root-dir $WIREMOCK_FOLDER
