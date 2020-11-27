#!/bin/bash

# Created by l1ving on 17/11/20
#
# echos the current version.
#
# Usage: "./version.sh <major or empty> <simple or empty>"
#
# Major overrides Simple
# Version spec for major:  R.MM.01
# Version spec for beta:   R.MM.DD-hash
# Version spec for simple: R.MM.DD
#
# Example beta: 1.11.17-58a47a2f

__utils="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/utils.sh"
source "$__utils"

check_git || exit $?

CUR_HASH="-"$(git log --pretty=%h -1) # for the -hash
CUR_R=$(($(date +"%Y") - 2019))       # Current year - 2019
CUR_M_D=$(date +".%m.%d")             # Current month and day, formatted

if [ "$1" == "major" ]; then
  CUR_HASH=""
  CUR_M_D=$(date +".%m.01")
fi

if [ "$1" != "major" ] && [ "$2" == "simple" ]; then
  CUR_HASH="-beta"
fi

echo "$CUR_R$CUR_M_D$CUR_HASH"
