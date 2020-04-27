#!/bin/bash

if [[ "$TRAVIS_PULL_REQUEST" == "true" ]]; then exit 0; else echo "">/dev/null; fi

CUR_VER="$(tail -c +2 ./scripts/curVer)"
COMMIT_TRIM="${TRAVIS_COMMIT::7}"
COMMIT_MSG="$TRAVIS_COMMIT_MESSAGE"

# Send message with branch name
#curl -H "Content-Type: application/json" -X POST -d '{"embeds": [{"title": "","color": 10195199,"description": "**Changelog:** '"$COMMIT_MSG"'\nBranch: `'"$BRANCH"'`\nCommit: ['${COMMIT_TRIM}'](https://github.com/kami-blue/client/commits/'${COMMIT_TRIM}') Direct: ['${COMMIT_TRIM}'](https://github.com/kami-blue/client/commit/'${TRAVIS_COMMIT}') "}]}' "$WEBHOOK"

# Find the release file and rename it to kamiblue-version-commit-release.jar
BUILD_DIR="$(readlink -f ./build/libs/)"
JAR_DIR="$(ls "$BUILD_DIR" | grep "release")"
mv ${BUILD_DIR}/${JAR_DIR} ${BUILD_DIR}/kamiblue-${CUR_VER}-${COMMIT_TRIM}-release.jar
JAR_DIR="$(ls "$BUILD_DIR" | grep "release")"
# Upload the release file
#curl -F content=@"$BUILD_DIR/$JAR_DIR" "$WEBHOOK"
