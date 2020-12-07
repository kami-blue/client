#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./buildJarSafe.sh"

__d="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source ~/.profile

if [ -z "$KAMI_DIR" ]; then
  echo "[buildJarSafe] Environment variable KAMI_DIR is not set, exiting." >&2
  exit 1
fi

cd "$KAMI_DIR" || exit $?

rm -rf build/libs/ || {
  echo "[buildJarSafe] Failed to remove 'build/libs/', exiting." >&2
  exit 1
}

chmod +x gradlew
./gradlew build &>/dev/null || {
  echo "[buildJarSafe] Gradle build failed, exiting." >&2
  exit 1
}

cd build/libs/ || exit $?

jar="$(find . -maxdepth 1 -name "*.jar" | sed "s/^\.\///g")"

if [ -z "$jar" ]; then
  echo "[buildJarSafe] Could not find build jar, this shouldn't be possible" >&2
  exit 1
fi

echo "$jar"
