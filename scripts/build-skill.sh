#!/usr/bin/env bash
# Rebuild the ddd4j skill source directories from this repo's src/.
# source of truth = src/ ; .zcode/skills/ddd4j/source and test-template are build artifacts.
set -euo pipefail
cd "$(dirname "$0")/.."

SKILL_DIR=".zcode/skills/ddd4j"

rm -rf "$SKILL_DIR/source" "$SKILL_DIR/test-template"
mkdir -p "$SKILL_DIR/source" "$SKILL_DIR/test-template"

cp -R src/main/java "$SKILL_DIR/source/java"
cp -R src/main/resources "$SKILL_DIR/source/resources"
cp -R src/test/java "$SKILL_DIR/test-template/java"
cp -R src/test/resources "$SKILL_DIR/test-template/resources"

COUNT=$(find "$SKILL_DIR" -name '*.java' | wc -l | tr -d ' ')
echo "skill sources rebuilt: $SKILL_DIR ($COUNT java files)"
