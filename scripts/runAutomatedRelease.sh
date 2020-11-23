#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./runAutomatedRelease.sh"

checkVar() {
  if [ -z "$2" ]; then
    echo "[uploadRelease] Environment variable '$1' is not set, exiting."
    exit 1
  else
    echo "$2"
  fi
}

source ~/.profile
KAMI_DIR="$HOME/projects/kamiblue"

checkVar "GH_RELEASE_BINARY" "$GH_RELEASE_BINARY" || exit $?
checkVar "KAMI_DIR" "$KAMI_DIR" || exit $?
checkVar "GITHUB_RELEASE_REPOSITORY" "$GITHUB_RELEASE_REPOSITORY" || exit $?
checkVar "GITHUB_RELEASE_ACCESS_TOKEN" "$GITHUB_RELEASE_ACCESS_TOKEN" || exit $?

cd "$KAMI_DIR" || {
  echo "[buildNamed] Failed to cd into '$KAMI_DIR', exiting."
  exit 1
}

git reset --hard origin/master
./scripts/bumpVersion.sh
JAR_NAME=$(./scripts/buildNamed.sh) || exit $?
