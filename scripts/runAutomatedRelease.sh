#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./runAutomatedRelease.sh"

source ~/.profile
source ./utils.sh

# TEMP
KAMI_DIR="$HOME/projects/kamiblue"

checkVar "GH_RELEASE_BINARY" "$GH_RELEASE_BINARY" || exit $?
checkVar "KAMI_DIR" "$KAMI_DIR" || exit $?
checkVar "GITHUB_RELEASE_REPOSITORY" "$GITHUB_RELEASE_REPOSITORY" || exit $?
checkVar "GITHUB_RELEASE_ACCESS_TOKEN" "$GITHUB_RELEASE_ACCESS_TOKEN" || exit $?

cd "$KAMI_DIR" || {
  echo "[buildNamed] Failed to cd into '$KAMI_DIR', exiting."
  exit 1
}

checkGit || exit $?
OLD_COMMIT=$(git log --pretty=%h | head -n 1)
git reset --hard origin/master

./scripts/bumpVersion.sh
JAR_NAME=$(./scripts/buildNamed.sh) || exit $?
