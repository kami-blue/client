#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./uploadRelease.sh <file name> <major or empty>"

checkVar() {
  if [ -z "$2" ]; then
    echo "[uploadRelease] Environment variable '$1' is not set, exiting."
    exit 1
  else
    echo "$2"
  fi
}

source ~/.profile

GH_RELEASE_BINARY="/home/mika/some_binary"
KAMI_DIR="/home/mika/projects/kamiblue"

checkVar "GH_RELEASE_BINARY" "$GH_RELEASE_BINARY" || exit $?
checkVar "KAMI_DIR" "$KAMI_DIR" || exit $?
checkVar "1" "$1"
checkVar "GITHUB_RELEASE_REPOSITORY" "$GITHUB_RELEASE_REPOSITORY" || exit $?
checkVar "GITHUB_RELEASE_ACCESS_TOKEN" "$GITHUB_RELEASE_ACCESS_TOKEN" || exit $?

# TODO: changelog
VERSION=$($KAMI_DIR/scripts/version.sh "$2") || exit $?

git tag -d "$VERSION"
git push origin :refs/tags/"$VERSION"

"$GH_RELEASE_BINARY" "$VERSION" "$KAMI_DIR/build/libs/$1" --commit master --tag "$VERSION" --github-repository "$GITHUB_RELEASE_REPOSITORY" --github-access-token "$GITHUB_RELEASE_ACCESS_TOKEN"

# TODO: webhook
