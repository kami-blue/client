#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Note: the num= is only for easier reading, you do not include that when using this script
# Usage: "./uploadRelease.sh 1=<$GH_RELEASE_BINARY> 2=<$KAMI_DIR> 3=<$GITHUB_RELEASE_REPOSITORY> 4=<$GITHUB_RELEASE_ACCESS_TOKEN> 5=<file name> 6=<major or empty>"

__scripts="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$__scripts/utils.sh" # include checkVar

checkVar "GH_RELEASE_BINARY" "$1" || exit $?
checkVar "KAMI_DIR" "$2" || exit $?
checkVar "GITHUB_RELEASE_REPOSITORY" "$3" || exit $?
checkVar "GITHUB_RELEASE_ACCESS_TOKEN" "$4" || exit $?

# TODO: changelog
VERSION=$("$__scripts"/version.sh "$6") || exit $?

git tag -d "$VERSION"
git push origin :refs/tags/"$VERSION"

"$1" "$VERSION" "$2/build/libs/$1" --commit master --tag "$VERSION" --github-repository "$3" --github-access-token "$4"

# TODO: webhook
