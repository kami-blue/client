#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./buildNamed.sh"

KAMI_DIR="$HOME/projects/kamiblue"

if [ -z "$KAMI_DIR" ]; then
  echo "[buildNamed] Environment variable KAMI_DIR is not set, exiting." >&2
  exit $?
fi

cd "$KAMI_DIR" || {
  echo "[buildNamed] Failed to cd into '$KAMI_DIR', exiting."
  exit $?
}

chmod +x gradlew
./gradlew build || {
  echo "[buildNamed] Gradle build failed, exiting."
  exit $?
}
