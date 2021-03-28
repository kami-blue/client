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
check_var "KAMI_REPO_MAJOR" "$KAMI_REPO_MAJOR" || exit $?
check_var "KAMI_REPO_NIGHTLY" "$KAMI_REPO_NIGHTLY" || exit $?
check_var "KAMI_OWNER" "$KAMI_OWNER" || exit $?
check_var "KAMI_WEBHOOK" "$KAMI_WEBHOOK" || exit $?

# Safely update repository
cd "$KAMI_DIR" || exit $?
git reset --hard HEAD
check_git || exit $?
OLD_COMMIT=$(git log --pretty=%h -1)

git reset --hard origin/master || exit $?
git pull || exit $?
git submodule update --init --recursive || exit $?

# Update mirror
cd "$KAMI_MIRROR_DIR" || exit $?
git reset --hard master || exit $?
git pull "$KAMI_DIR" || exit $?
git submodule update --init --recursive || exit $?
git push --force origin master || exit $?

cd "$KAMI_DIR" || exit $?

# Set some variables, run scripts
HEAD=$(git log --pretty=%h -1)
CHANGELOG="$("$_d"/scripts/changelog.sh "$OLD_COMMIT")" || exit $?
VERSION="$("$_d"/scripts/version.sh "$1")" || exit $?
VERSION_MAJOR="$("$_d"/scripts/version.sh "major")" || exit $?
"$_d"/scripts/bumpVersion.sh "$1" || exit $?
JAR_NAME="$("$_d"/scripts/buildJarSafe.sh)" || exit $?

"$_d"/scripts/uploadRelease.sh "$1" "$HEAD" "$VERSION" "$JAR_NAME" "$CHANGELOG" || exit $?
"$_d"/scripts/bumpWebsite.sh "$JAR_NAME" "$VERSION" "$VERSION_MAJOR" || exit $?

REPO="$KAMI_REPO_NIGHTLY"
[ "$1" == "major" ] && REPO="$KAMI_REPO_MAJOR"

# Send changelog embed
curl -H "Content-Type: application/json" -X POST \
  -d '{"embeds": [{"title": "Download v'"$VERSION"'","color": 10195199,"description": "[**DOWNLOAD**](https://github.com/'"$KAMI_OWNER"'/'"$REPO"'/releases/download/'"$VERSION"'/'"$JAR_NAME"')\n\n**Changelog:** \n'"$CHANGELOG"'\n\nDiff: ['"$OLD_COMMIT"'...'"$HEAD"'](https://github.com/'"$KAMI_OWNER"'/'"$REPO"'/compare/'"$OLD_COMMIT"'...'"$HEAD"') "}]}' \
  "$KAMI_WEBHOOK" || {
    echo "[runAutomatedRelease] Failed to post changelog embed"
    exit 1
}

# Send ping
if [ -n "$KAMI_UPDATES_ROLE_ID" ]; then
  curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"username": "", "content": "<@&'"$KAMI_UPDATES_ROLE_ID"'>"}' \
    "$KAMI_WEBHOOK"
fi
