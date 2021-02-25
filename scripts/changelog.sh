#!/bin/bash

# Created by l1ving on 17/02/20
#
# Returns a changelog when given a single short hash or two hashes
# Defaults to head when no second hash is given
# Usage: "./changelog.sh <first hash> <second hash or empty>"

__scripts="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/utils.sh"
source "$__scripts"

check_git || exit $?
check_var "1" "$1" || exit $?

CHANGELOG="$(git log --format=%s "$1"..."$2" | sed ':a;N;$!ba;s/\n/\\n/g' | sed "s/\"/''/g")" || {
  echo "[changelog] Failed to create changelog from commits, exiting." >&2
  exit 1
}

[ -n "$CHANGELOG" ] || exit 1

echo "$CHANGELOG"
