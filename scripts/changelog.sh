#!/bin/bash

# Created by l1ving on 17/02/20
#
# Returns a changelog when given a single short hash or two hashes
# Defaults to head when no second hash is given
# Usage: "./changelog.sh <first hash> <second hash or empty>"

__scripts="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/utils.sh"
source "$__scripts"

checkGit || exit $?
checkVar "1" "$1" || exit $?
checkVar "2" "$2" &>/dev/null || 2="HEAD"

CHANGELOG_FULL="$(git log --format=%s "$1"..."$2" | sed ':a;N;$!ba;s/\n/\\n- /g')" || {
  echo "[changelog] Failed to create changelog from commits, exiting."
  exit 1
}

echo "$CHANGELOG_FULL"
