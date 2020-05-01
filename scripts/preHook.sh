#!/bin/bash

CUR_VER="$(cat ./scripts/curVer)"
COMMIT_TRIM="$(git log --format=%h -1)"

sed -i "s/modVersion=.*/modVersion=${CUR_VER:1}-$COMMIT_TRIM/" gradle.properties
sed -i "s/\"version\": \".*\",/\"version\": \"${CUR_VER:1}-$COMMIT_TRIM\",/" src/main/resources/mcmod.info
sed -i "s/MODVER = \".*\";/MODVER = \"$CUR_VER-$COMMIT_TRIM\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
