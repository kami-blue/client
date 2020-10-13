#!/bin/bash

#  REQUIREMENTS:
#
#  ~/.profile:
#    $GITHUB_RELEASE_ACCESS_TOKEN with repo:status, repo_deployment, public_repo
#    $GITHUB_RELEASE_REPOSITORY="kami-blue/nightly-releases"
#    $WEBHOOK= webhook url
#
#  ~/kamiblue
#    git clone git@github.com:kami-blue/nightly-releases.git
#    git remote add kamiblue git@github.com:kami-blue/client.git
#
#  ~/Java JDK 8 is also required
#
#  ~/https://github.com/buildkite/github-release/releases is also required
#
#  crontab -e
#    0 0,12 * * * /home/user/releaseNightly >/tmp/cron1.log 2>&1

cd ~/kamiblue/
export COMMIT_LAST="$(git log --format=%h -1)"
export COMMIT_LAST_FULL="$(git log --format=%H -1)"

git fetch kamiblue master
git fetch origin master

sleep 0.5

git reset --hard kamiblue/master
git push --force origin HEAD:master

sleep 1

export COMMIT_TRIM="$(git log --format=%h -1)"

if [[ "$COMMIT_LAST" == "$COMMIT_TRIM" ]]; then
    exit 0
fi

rm -rf build/libs

./scripts/preHook.sh
sleep 1
./gradlew build
sleep 2
./scripts/hook.sh
