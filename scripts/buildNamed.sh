#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./buildNamed.sh"

source ~/.profile

if [ -z "$KAMI_DIR" ]; then
  echo "[buildNamed] Environment variable KAMI_DIR is not set, exiting." >&2
  exit 1
fi

cd "$KAMI_DIR" || exit $?

rm -rf build/libs/* || {
  echo "[buildNamed] Failed to remove existing files in 'build/libs/', exiting." >&2
  exit 1
}

chmod +x gradlew
./gradlew build &>/dev/null || {
  echo "[buildNamed] Gradle build failed, exiting." >&2
  exit 1
}

cd build/libs/ || exit $?

__named=$(find . -maxdepth 1 -not -name "*release*" | tail -n +2)
__bad_named=$(find . -maxdepth 1 -name "*release*")

rm -f "$__named"
mv "$__bad_named" "$__named"

echo "$__named" | sed "s/^\.\///g"
