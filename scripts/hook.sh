#!/bin/bash

source ~/.profile

CUR_VER="$(tail -c +2 ./scripts/curVer)"
COMMIT_TRIM="$(git log --format=%h -1)"
COMMIT_FULL="$(git log --format=%H -1)"
COMMIT_MSG="$(git log --format=%s -1)"

# Find the release file and rename it to kamiblue-version-commit-release.jar
BUILD_DIR=$HOME/kamiblue/build/libs/
JAR_DIR="$(ls "$BUILD_DIR" | grep "release")"

CHANGELOG_FULL="$(git log --format=%s $COMMIT_TRIM...$COMMIT_LAST | sed ':a;N;$!ba;s/\n/\\n- /g')"

# delete the release in case it exists
git tag -d $CUR_VER-$COMMIT_TRIM
git push origin :refs/tags/$CUR_VER-$COMMIT_TRIM
sleep 2

# Upload the release
cd ~/
./github-release-linux-amd64 $CUR_VER-$COMMIT_TRIM $BUILD_DIR/$JAR_DIR --commit master --tag $CUR_VER-$COMMIT_TRIM --github-repository $GITHUB_RELEASE_REPOSITORY --github-access-token $GITHUB_RELEASE_ACCESS_TOKEN
sleep 5

# Send message with commit information
curl -H "Content-Type: application/json" -X POST -d '{"embeds": [{"title": "Download v'$CUR_VER\-$COMMIT_TRIM'","color": 10195199,"description": "[**DOWNLOAD**](https://github.com/kami-blue/nightly-releases/releases/download/'$CUR_VER\-$COMMIT_TRIM'/'${JAR_DIR}')\n\n**Changelog:** \n- '"$CHANGELOG_FULL"'\n\nDiff: ['$COMMIT_LAST'...'${COMMIT_TRIM}'](https://github.com/kami-blue/client/compare/'$COMMIT_LAST_FULL'...'$COMMIT_FULL') "}]}' "$WEBHOOK"

cd ~/website/

git pull
sleep 1
./scripts/bumpWebsiteNightlies.sh
sed -i "s|beta_jar_url:.*|beta_jar_url: https://github.com/kami-blue/nightly-releases/releases/download/${CUR_VER}-${COMMIT_TRIM}/${JAR_DIR}|g" _config.yml
sed -i "s|beta_ver:.*|beta_ver: v${CUR_VER}-${COMMIT_TRIM}|g" _config.yml
git commit -a -m "bump ver to v${CUR_VER}-${COMMIT_TRIM}"
git push origin master
