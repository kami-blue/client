#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./bumpWebsite.sh"

KAMI_WEBSITE_DIR="$HOME/projects/kamiblue-website"

if [ -z "$KAMI_WEBSITE_DIR" ]; then
  echo "[bumpBuildNumber] Environment variable KAMI_WEBSITE_DIR is not set, exiting." >&2
  exit 1
fi

if [ ! -f "$KAMI_WEBSITE_DIR/api/v1/builds" ]; then
  echo "[bumpBuildNumber] '$KAMI_WEBSITE_DIR/api/v1/builds' couldn't be found, be sure you're running the latest commit and API version, exiting." >&2
  exit 1
fi

BUILD_NUMBER_PREVIOUS=$(curl https://kamiblue.org/api/v1/builds)
BUILD_NUMBER=$((BUILD_NUMBER_PREVIOUS + 1))

if [ "$BUILD_NUMBER" == "$BUILD_NUMBER_PREVIOUS" ]; then
  echo "[bumpBuildNumber] Failed to bump build number, exiting."
  exit 1
fi

if [[ ! "$BUILD_NUMBER" =~ ^-?[0-9]+$ ]]; then
  echo "[bumpBuildNumber] Could not parse '$BUILD_NUMBER' as an Int, exiting."
  exit 1
fi

cd "$KAMI_WEBSITE_DIR" || {
  echo "[bumpBuildNumber] Failed to cd into '$KAMI_WEBSITE_DIR', exiting."
  exit 1
}

git reset --hard origin/master
git pull

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/version.sh"
VERSION=$("$__dir") || exit $?
VERSION_MAJOR=$("$__dir" "major") || exit $?
JAR="kamiblue-$VERSION.jar"

sed -i "s/^cur_ver:.*/cur_ver: $VERSION_MAJOR/g" _config.yml
sed -i "s/^beta_ver:.*/beta_ver: $VERSION/g" _config.yml
sed -i "s|jar_url:.*|jar_url: https://github.com/kami-blue/client/releases/download/$VERSION/$JAR|g" _config.yml
sed -i "s|jar_sig_url:.*|jar_sig_url: https://github.com/kami-blue/client/releases/download/$VERSION/$JAR.sig|g" _config.yml
sed -i "s|beta_jar_url:.*|beta_jar_url: https://github.com/kami-blue/nightly-releases/releases/download/$VERSION/$JAR|g" _config.yml

echo "$BUILD_NUMBER" >"api/v1/builds"

git commit -am "[bump] Release $VERSION"
git push origin master
