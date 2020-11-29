#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./bumpWebsite.sh <jar name> <version> <version major>"

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$__dir/utils.sh"
source ~/.profile

check_var "KAMI_WEBSITE_DIR" "$KAMI_WEBSITE_DIR" || exit $?
check_var "KAMI_OWNER" "$KAMI_OWNER" || exit $?
check_var "KAMI_REPO_MAJOR" "$KAMI_REPO_MAJOR" || exit $?
check_var "KAMI_REPO_NIGHTLY" "$KAMI_REPO_NIGHTLY" || exit $?
check_var "1" "$1" || exit $?
check_var "2" "$2" || exit $?
check_var "3" "$3" || exit $?

if [ ! -f "$KAMI_WEBSITE_DIR/api/v1/builds" ]; then
  echo "[bumpBuildNumber] '$KAMI_WEBSITE_DIR/api/v1/builds' couldn't be found, be sure you're running the latest commit and API version, exiting." >&2
  exit 1
fi

BUILD_NUMBER_PREVIOUS=$(curl -s https://kamiblue.org/api/v1/builds)
BUILD_NUMBER=$((BUILD_NUMBER_PREVIOUS + 1))

if [ "$BUILD_NUMBER" == "$BUILD_NUMBER_PREVIOUS" ]; then
  echo "[bumpBuildNumber] Failed to bump build number, exiting." >&2
  exit 1
fi

if [[ ! "$BUILD_NUMBER" =~ ^-?[0-9]+$ ]]; then
  echo "[bumpBuildNumber] Could not parse '$BUILD_NUMBER' as an Int, exiting." >&2
  exit 1
fi

cd "$KAMI_WEBSITE_DIR" || exit $?

git reset --hard origin/master || exit $?
git pull || exit $?

sed -i "s/^build_number:.*/build_number: $BUILD_NUMBER/g" _config.yml
sed -i "s/^cur_ver:.*/cur_ver: $3/g" _config.yml
sed -i "s/^beta_ver:.*/beta_ver: $2/g" _config.yml

sed -i "s|jar_url:.*|jar_url: https://github.com/$KAMI_OWNER/$KAMI_REPO_MAJOR/releases/download/$3/kamiblue-$3.jar|g" _config.yml
sed -i "s|jar_sig_url:.*|jar_sig_url: https://github.com/$KAMI_OWNER/$KAMI_REPO_MAJOR/releases/download/$3/kamiblue-$3.jar.sig|g" _config.yml
sed -i "s|beta_jar_url:.*|beta_jar_url: https://github.com/$KAMI_OWNER/$KAMI_REPO_NIGHTLY/releases/download/$2/$1|g" _config.yml

git commit -am "[bump] Release $2" || exit $?
git push origin master
