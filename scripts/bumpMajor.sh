#!/bin/bash

# Created by l1ving on 17/02/20
#
# Bumps the version and creates a commit ready for push
#
# Usage: "./bumpMajor.sh"

__utils="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/utils.sh"
source "$__utils"

check_git || exit $?

CUR_R=$(($(date +"%Y") - 2019))
CUR_M=$(date +".%m")

VERSION="$CUR_R$CUR_M.01"
VERSION_DEV="$CUR_R$CUR_M.xx-dev"

sed -i "s/modVersion=.*/modVersion=$VERSION_DEV/" gradle.properties
sed -i "s/VERSION = \".*\";/VERSION = \"$VERSION_DEV\";/" src/main/kotlin/org/kamiblue/client/KamiMod.kt
sed -i "s/VERSION_SIMPLE = \".*\";/VERSION_SIMPLE = \"$VERSION_DEV\";/" src/main/kotlin/org/kamiblue/client/KamiMod.kt
sed -i "s/VERSION_MAJOR = \".*\";/VERSION_MAJOR = \"$VERSION\";/" src/main/kotlin/org/kamiblue/client/KamiMod.kt
git commit -am "[bump] Release Major $VERSION"

echo "[bumpMajor] Created commit for version '$VERSION', remember to push!"
