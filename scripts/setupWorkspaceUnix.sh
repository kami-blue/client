#!/bin/sh

# Created by l1ving on 17/02/20
#
# Used to setup workspace on unix and fix building
#
# Usage: "./setupWorkspaceUnix.sh"

__dir="$(cd "$(dirname "$0")" && pwd)"
. "$__dir/utils.sh"

#

echo "[$(date +"%H:%M:%S")] Found script in dir '$__dir', trying to cd into KAMI Blue folder"
cd "$__dir" || exit $?
cd ../ || exit $?

#

echo "[$(date +"%H:%M:%S")] Checking if git is installed..."
if [ -z "$(which git)" ]; then
  echo "[$(date +"%H:%M:%S")] ERROR: Git is not installed, please make sure you install the CLI version of git, not some desktop wrapper for it" >&2
  exit 1
fi
echo "[$(date +"%H:%M:%S")] Git is installed!"

#

echo "[$(date +"%H:%M:%S")] Checking for .git dir..."
if [ ! -d ".git" ]; then
  echo "[$(date +"%H:%M:%S")] ERROR: Could not detect git repository, exiting" >&2
  exit 1
fi
echo "[$(date +"%H:%M:%S")] Found git repository!"

#

echo "[$(date +"%H:%M:%S")] Downloading git submodules..."
git submodule update --init --recursive || {
  echo "[$(date +"%H:%M:%S")] ERROR: Failed to init git submodules"
  exit 1
}
echo "[$(date +"%H:%M:%S")] Downloaded git submodules!"

#

echo "[$(date +"%H:%M:%S")] Setting up runClient..."
./gradlew prepareRunClient >/dev/null 2>&1 || {
  echo "[$(date +"%H:%M:%S")] ERROR: Setting up runClient failed! Run './gradlew prepareRunClient' manually"
  exit 1
}
./gradlew prepareRuns >/dev/null 2>&1 || {
  echo "[$(date +"%H:%M:%S")] ERROR: Setting up runClient failed! Run './gradlew prepareRuns' manually"
  exit 1
}
echo "[$(date +"%H:%M:%S")] Setup runClient!"

#

echo "[$(date +"%H:%M:%S")] Running test build without daemon..."
./gradlew build --no-daemon || {
  echo "[$(date +"%H:%M:%S")] ERROR: Gradle build failed"
  exit 1
}

#

cat src/main/resources/ascii.txt 2>/dev/null
echo "=========================================================================="
echo ""
echo "[$(date +"%H:%M:%S")] Build succeeded! All checks passed, you can build normally now!"
echo ""
echo "=========================================================================="
