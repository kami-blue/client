#!/bin/sh

# Created by dominikaaaa on 19/02/20
# echo "Usage: ./ver.sh"

CUR_VER="$(cat ./scripts/curVer)"
CUR_BETA="$(cat ./scripts/curBeta)"
CUR_RELEASE="$(cat ./scripts/curRelease)"

sed -i "s/modVersion=.*/modVersion=${CUR_VER:1}$CUR_BETA/" gradle.properties
sed -i "s/VER_FULL_BETA = \".*\";/VER_FULL_BETA = \"$CUR_VER$CUR_BETA\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VER_SMALL = \".*\";/VER_SMALL = \"$CUR_VER$CUR_BETA\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VER_STABLE = \".*\";/VER_STABLE = \"$CUR_RELEASE\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/\"version\": \".*\",/\"version\": \"${CUR_VER:1}$CUR_BETA\",/" src/main/resources/mcmod.info

