#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./runAutomatedRelease.sh"

KAMI_DIR="$HOME/projects/kamiblue"

if [ -z "$KAMI_DIR" ]; then
  echo "[buildNamed] Environment variable KAMI_DIR is not set, exiting." >&2
  exit 1
fi

cd "$KAMI_DIR" || {
  echo "[buildNamed] Failed to cd into '$KAMI_DIR', exiting."
  exit 1
}
