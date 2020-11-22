#!/bin/bash

# Created by l1ving on 17/02/20
#
# Bumps the version and creates a commit ready for push
#
# Usage: "./bumpMajor.sh"

if [ ! -d .git ]; then
  echo "[bumpMajor] Could not detect git repository, exiting" >&2
  exit 1
fi

if [ ! "$(git status | head -n 4 | tail -n 1)" == "nothing to commit, working tree clean" ]; then
  echo "[bumpMajor] Not working in a clean tree, make sure to commit your changes first. Exiting." >&2
  exit 1
fi

CUR_R=$(($(date +"%Y") - 2019))
CUR_M=$(date +".%m")

VERSION="$CUR_R$CUR_M.01"
VERSION_DEV="$CUR_R$CUR_M.xx-dev"

sed -i "s/modVersion=.*/modVersion=$VERSION_DEV/" gradle.properties
sed -i "s/VERSION = \".*\";/VERSION = \"$VERSION_DEV\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VERSION_SIMPLE = \".*\";/VERSION_SIMPLE = \"$VERSION_DEV\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VERSION_MAJOR = \".*\";/VERSION_MAJOR = \"$VERSION\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/\"version\": \".*\",/\"version\": \"$VERSION_DEV\",/" src/main/resources/mcmod.info
git commit -am "[bump] Release Major $VERSION"

echo "[bumpMajor] Created commit for version '$VERSION', remember to push!"
