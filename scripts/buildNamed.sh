#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./buildNamed.sh"

__d="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
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

__named="$(find . -maxdepth 1 -not -name "*release*" | tail -n +2 | sed "s/^\.\///g")"
__bad_named="$(find . -maxdepth 1 -name "*release*")"

rm "$__named" # remove the one without -release

# Build release jar with the name without -release
java -jar "$__d/jar-shrink/jar-shrink.jar" "$__bad_named" -out "$__named" -n -keep "me.zeroeightsix" -keep "baritone" -keep "org.kamiblue" -keep "org.spongepowered"

rm "$__bad_named" # remove the un-shrunk jar with -release

echo "$__named" # echo the shrunk jar, without -release
