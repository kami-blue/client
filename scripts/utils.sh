#!/bin/bash

# Created by l1ving on 17/02/20
#
# Miscellaneous utilities used in other scripts
# Usage: "source ./utils.sh"

# Do not use this except to get relative script paths

rootKamiDir() {
  pwd | sed "s/kamiblue.*/kamiblue/g"
}

checkVar() {
  if [ -z "$2" ]; then
    echo "Environment variable '$1' is not set, exiting."
    exit 1
  else
    echo "$2"
  fi
}

checkGit() {
  if [ ! -d "$(rootKamiDir)/.git" ]; then
    echo "Could not detect git repository, exiting" >&2
    exit 1
  elif [ ! "$(git status | head -n 4 | tail -n 1)" == "nothing to commit, working tree clean" ]; then
    echo "Not working in a clean tree, make sure to commit your changes first. Exiting." >&2
    exit 1
  fi
}
