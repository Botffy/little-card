#!/bin/bash

set -e  # Exit on error

# Check if version name parameter is provided
if [ -z "$1" ]; then
  echo "Error: Version name parameter is required."
  echo "Usage: ./create-artefacts.sh <versionName>"
  echo "Example: ./create-artefacts.sh mvp-rc4"
  exit 1
fi

VERSION_NAME="$1"

function is_dirty {
  [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]] && echo "*"
}

# Check if on master branch
if [[ $(git rev-parse --abbrev-ref HEAD) != "master" ]]; then
  echo "Error: You must be on the master branch to create a release bundle."
  exit 1
fi

# Check for uncommitted changes
if [[ -n $(is_dirty) ]]; then
  echo "Error: You have uncommitted changes. Please commit or stash them before creating a release bundle."
  exit 1
fi

echo "Creating release for version: $VERSION_NAME"

# Update build.gradle.kts
BUILD_GRADLE="app/build.gradle.kts"

if [ ! -f "$BUILD_GRADLE" ]; then
  echo "Error: $BUILD_GRADLE not found"
  exit 1
fi

# Read current versionCode
CURRENT_VERSION_CODE=$(grep -oP 'versionCode = \K\d+' "$BUILD_GRADLE")
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

echo "Bumping versionCode from $CURRENT_VERSION_CODE to $NEW_VERSION_CODE"
echo "Setting versionName to $VERSION_NAME"

# Update versionCode and versionName in build.gradle.kts
sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" "$BUILD_GRADLE"
sed -i "s/versionName = \".*\"/versionName = \"$VERSION_NAME\"/" "$BUILD_GRADLE"

# Commit the version changes
git add "$BUILD_GRADLE"
git commit -m "Release android ${VERSION_NAME}"

# Create annotated tag
git tag -a "$VERSION_NAME" -m "Release android ${VERSION_NAME}"

echo "Created commit and tag for $VERSION_NAME"

# Build the release
export LITTLE_CARD_KEY_FILE=release-key.properties
./gradlew clean
./gradlew assembleRelease
./gradlew bundleRelease

# Find and copy the APK
APK_PATH=$(find app/build/outputs/apk/release -name "*.apk" | head -n 1)
if [ -n "$APK_PATH" ]; then
  cp "$APK_PATH" "./little-card_${VERSION_NAME}.apk"
  echo "Copied APK to: ./little-card_${VERSION_NAME}.apk"
else
  echo "Warning: APK not found"
fi

# Find and copy the AAB
AAB_PATH=$(find app/build/outputs/bundle/release -name "*.aab" | head -n 1)
if [ -n "$AAB_PATH" ]; then
  cp "$AAB_PATH" "./little-card_${VERSION_NAME}.aab"
  echo "Copied AAB to: ./little-card_${VERSION_NAME}.aab"
else
  echo "Warning: AAB not found"
fi

echo ""
echo "Release $VERSION_NAME created successfully!"
echo "Next steps:"
echo "  1. Push the commit: git push"
echo "  2. Push the tag: git push origin $VERSION_NAME"
echo "  3. Upload artifacts:"
echo "     - little-card_${VERSION_NAME}.apk"
echo "     - little-card_${VERSION_NAME}.aab"

