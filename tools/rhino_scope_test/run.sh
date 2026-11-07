#!/usr/bin/env bash
# Rhino cross-scope plain-object check for global.NCH._undefinedIn (scratch r1106cbb). Pack's own Rhino fork.
set -e; cd "$(dirname "$0")"; I=../..
CP="$I/mods/rhino-forge-1902.2.3-build.284.jar:$HOME/.local/share/atlauncher/libraries/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar"
mkdir -p build; javac -proc:none -cp "$CP" -d build ScopeTest.java; java -cp "build:$CP" ScopeTest a.js
