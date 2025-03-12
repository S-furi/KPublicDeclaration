#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 [KtFile|directory]"
  exit 1
fi

./gradlew run --quiet --args "$1"

