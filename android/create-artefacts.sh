#!/bin/bash

function is_dirty {
  [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]] && echo "*"
}

if [[ $(git rev-parse --abbrev-ref HEAD) != "master" ]]; then
  echo "Error: You must be on the master branch to create a release bundle."
  exit 1
fi

if [[ -n $(is_dirty) ]]; then
  echo "Error: You have uncommitted changes. Please commit or stash them before creating a release bundle."
  exit 1
fi

export LITTLE_CARD_KEY_FILE=release-key.properties
./gradlew clean
./gradlew assembleRelease
./gradlew bundleRelease
