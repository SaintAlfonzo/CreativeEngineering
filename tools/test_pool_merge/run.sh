#!/bin/bash
# Headless check of PoolTuner.mergeCurrent (needs ./gradlew :patcherMod:build first). Expect "ALL PASS".
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"; L="$HOME/.local/share/atlauncher/libraries"; C="$HOME/.gradle/caches/modules-2/files-2.1"
source "$ROOT/tools/java_test_cp.sh"; T="$(mktemp -d)"
mkdir -p "$T/stub/net/minecraftforge/fml/loading"
echo 'package net.minecraftforge.fml.loading; import net.minecraftforge.api.distmarker.Dist; public class FMLEnvironment { public static final boolean production = true; public static final Dist dist = Dist.DEDICATED_SERVER; }' > "$T/stub/net/minecraftforge/fml/loading/FMLEnvironment.java"
javac -proc:none -cp "$CP" -d "$T/s" "$T/stub/net/minecraftforge/fml/loading/FMLEnvironment.java" || exit 1
javac -proc:none -cp "$T/s:$CP" -d "$T/o" "$ROOT/tools/test_pool_merge/MergeTest.java" 2>&1 | grep -v warning
java -cp "$T/s:$T/o:$CP" com.SaintAlfonzo.creativeengineering.worldgen.MergeTest 2>&1 | grep "^ok\|FAIL\|PASS"
