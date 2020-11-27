#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./runAutomatedRelease.sh <major or empty>"

_d="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.."
source "$_d/scripts/utils.sh"
source ~/.profile

check_var "KAMI_DIR" "$KAMI_DIR" || exit $?
check_var "KAMI_MIRROR_DIR" "$KAMI_MIRROR_DIR" || exit $?

# Safely update repository
cd "$KAMI_DIR" || exit $?
check_git || exit $?
OLD_COMMIT=$(git log --pretty=%h -1)

git reset --hard origin/master
git pull

# Update mirror
cd "$KAMI_MIRROR_DIR" || exit $?
check_git || exit $?

git reset --hard master
git pull "$KAMI_DIR"
git push --force origin master

cd "$KAMI_DIR" || exit $?

# Set some variables, run scripts
HEAD=$(git log --pretty=%h -1)
CHANGELOG="$("$_d"/scripts/changelog.sh "$OLD_COMMIT")" || exit $?
VERSION="$("$_d"/scripts/version.sh)" || exit $?
VERSION_MAJOR="$("$_d"/scripts/version.sh "major")" || exit $?
"$_d"/scripts/bumpVersion.sh "$1" || exit $?
JAR_NAME="$("$_d"/scripts/buildNamed.sh)" || exit $?

"$_d"/scripts/uploadRelease.sh "$1" "$HEAD" "$VERSION" "$JAR_NAME" "$CHANGELOG" || exit $?
"$_d"/scripts/bumpWebsite.sh "$JAR_NAME" "$VERSION" "$VERSION_MAJOR"
