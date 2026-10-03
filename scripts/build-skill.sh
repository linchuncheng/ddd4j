#!/usr/bin/env bash
# Rebuild the ddd4j skill from this repo's src/ and install it.
# source of truth = src/ ; .zcode/skills/ddd4j/source and test-template are build artifacts.
# Install target: ~/.agents/skills (cross-agent convention directory).
set -euo pipefail
cd "$(dirname "$0")/.."

SKILL_DIR=".zcode/skills/ddd4j"
INSTALL_DIR="${HOME}/.agents/skills/ddd4j"

rm -rf "$SKILL_DIR/source" "$SKILL_DIR/test-template"
mkdir -p "$SKILL_DIR/source" "$SKILL_DIR/test-template"

cp -R src/main/java "$SKILL_DIR/source/java"
cp -R src/main/resources "$SKILL_DIR/source/resources"
cp -R src/test/java "$SKILL_DIR/test-template/java"
cp -R src/test/resources "$SKILL_DIR/test-template/resources"

mkdir -p "$(dirname "$INSTALL_DIR")"
rm -rf "$INSTALL_DIR"
cp -R "$SKILL_DIR" "$INSTALL_DIR"

COUNT=$(find "$INSTALL_DIR" -name '*.java' | wc -l | tr -d ' ')
echo "skill rebuilt and installed: $INSTALL_DIR ($COUNT java files)"
