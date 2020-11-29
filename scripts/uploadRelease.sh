#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./uploadRelease.sh <major or empty> <branch or commit> <version> <jar name> <changelog>"

__scripts="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$__scripts/utils.sh" # include check_var
source ~/.profile

check_var "2" "$2" || exit $?
check_var "3" "$3" || exit $?
check_var "KAMI_REPO_MAJOR" "$KAMI_REPO_MAJOR" || exit $?
check_var "KAMI_REPO_NIGHTLY" "$KAMI_REPO_NIGHTLY" || exit $?
check_var "GITHUB_TOKEN" "$GITHUB_TOKEN" || exit $?

REPO="$KAMI_REPO_NIGHTLY"

[ "$1" == "major" ] && REPO="$KAMI_REPO_MAJOR"

# Create release
curl -s -H "Authorization: token $GITHUB_TOKEN" -X POST --data "$(generate_release_data "$KAMI_OWNER" "$REPO" "$3" "$2" "$3" "$5" "false" "false")" "https://api.github.com/repos/$KAMI_OWNER/$REPO/releases" || exit $?

# Upload jar to release
"$__scripts/uploadReleaseAsset.sh" github_api_token="$GITHUB_TOKEN" owner="$KAMI_OWNER" repo="$REPO" tag="$3" filename="$KAMI_DIR/build/libs/$4"
