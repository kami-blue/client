#!/bin/bash

# Created by l1ving on 17/02/20
#
# Miscellaneous utilities used in other scripts
# Usage: "source ./utils.sh"

# Do not use this except to get relative script paths

root_kami_dir() {
  pwd | sed "s/kamiblue.*/kamiblue/g"
}

check_var() {
  if [ -z "$2" ]; then
    echo "Variable '$1' is not set, exiting." >&2
    exit 1
  fi
}

check_git() {
  if [ ! -d "$(root_kami_dir)/.git" ]; then
    echo "Could not detect git repository, exiting" >&2
    exit 1
  elif [ ! "$(git status | tail -n 1)" == "nothing to commit, working tree clean" ]; then
    echo "Either not working in a clean tree or you have unpushed commits. Exiting." >&2
    exit 1
  fi
}

# Usage: "generate_release_data "owner" "repo" "version" "branch or commit" "name" "description" "draft (bool)", "prerelease (bool)""
generate_release_data() {
  cat <<EOF
{
  "owner": "$1",
  "repo": "$2",
  "tag_name": "$3",
  "target_commitish": "$4",
  "name": "$5",
  "body": "$6",
  "draft": $7,
  "prerelease": $8
}
EOF
}

# Usage: "generate_asset_data "owner" "repo" "release id""
generate_asset_data() {
  cat <<EOF
{
  "accept": "application/vnd.github.v3+json",
  "owner": "$1",
  "repo": "$2",
  "release_id": "$3"
}
EOF
}
