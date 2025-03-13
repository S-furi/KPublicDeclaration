#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <path_to_kotlin_file | path_to_kotlin_project>"
  exit 1
fi

./gradlew run --quiet --args "$1"

