#!/bin/bash

# Separate upload script to run in case the first fails

source ~/.profile

cd ~/kamiblue

CUR_VER="$(tail -c +2 ./scripts/curVer)"
COMMIT_TRIM="$(git log --format=%h -1)"

# Find the release file and rename it to kamiblue-version-commit-release.jar
BUILD_DIR=$HOME/kamiblue/build/libs/
JAR_DIR="$(ls "$BUILD_DIR" | grep "release")"

./gradlew build

# delete the release in case it exists
git tag -d $CUR_VER-$COMMIT_TRIM
git push origin :refs/tags/$CUR_VER-$COMMIT_TRIM
sleep 2

# Upload the release
cd ~/
./github-release-linux-amd64 $CUR_VER-$COMMIT_TRIM $BUILD_DIR/$JAR_DIR --commit master --tag $CUR_VER-$COMMIT_TRIM --github-repository $GITHUB_RELEASE_REPOSITORY --github-access-token $GITHUB_RELEASE_ACCESS_TOKEN
sleep 5
