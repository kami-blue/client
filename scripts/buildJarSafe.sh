#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./buildJarSafe.sh"

__d="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source ~/.profile
source "$__d/utils.sh"

check_var "JDK_11_HOME" "$JDK_11_HOME" || exit $?
check_var "JDK_8_HOME" "$JDK_8_HOME" || exit $?

if [ -z "$KAMI_DIR" ]; then
  echo "[buildJarSafe] Environment variable KAMI_DIR is not set, exiting." >&2
  exit 1
fi

cd "$KAMI_DIR" || exit $?

rm -rf build/libs/ || {
  echo "[buildJarSafe] Failed to remove 'build/libs/', exiting." >&2
  exit 1
}

export JAVA_HOME="$JDK_8_HOME"
sudo archlinux-java set java-8-openjdk || exit $?
chmod +x gradlew
./gradlew --no-daemon build &>/dev/null || {
  echo "[buildJarSafe] Gradle build failed, exiting." >&2
  exit 1
}

cd build/libs/ || exit $?

__named="$(find . -maxdepth 1 -name "*.jar" | head -n 1 | sed "s/^\.\///g")"
# shellcheck disable=SC2001
__bad_named="$(echo "$__named" | sed "s/\.jar$/-release.jar/g")"

mv "$__named" "$__bad_named" # rename it to include release

# Build release jar with the name without -release
export JAVA_HOME="$JDK_11_HOME"
sudo archlinux-java set java-11-openjdk || exit $?
java -jar "$__d/jar-shrink/jar-shrink.jar" "$__bad_named" -out "$__named" -n -keep "org.kamiblue" -keep "baritone" -keep "org.spongepowered"

rm "$__bad_named" # remove the un-shrunk jar with -release

echo "$__named" # echo the shrunk jar, without -release
