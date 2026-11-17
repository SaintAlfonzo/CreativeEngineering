# root-gradle-build

> Instance root is one Gradle build (settings.gradle includes patcherMod + exposureBackport) with build-logic/ convention plugins ce.forge-mod / ce.forge-mod-refmap; dev-run dependency pins

Moved from agent memory 2026-11-16 (last edited there 2026-10-28). Repo status docs win over any status line here.

Set up 2026-10-01.
- Root `settings.gradle` includes `patcherMod`, `exposureBackport` and (2026-10-05) `incubationAdditions` (folders under `development/` since 2026-10-01, via projectDir; task paths unchanged), plus `includeBuild 'build-logic'`.
- Root `gradle.properties` holds the shared MC/Forge/mappings/java.home settings. Each mod's own `gradle.properties` holds mod_id, mod_version, and the optional jar_name, mixin_configs and replaces_jars.
- `patcherMod/settings.gradle` was deleted (it is in the scratchpad backup). `cd patcherMod && ./gradlew build --offline` still works because Gradle finds the root settings by searching upward.

**Convention plugins:**
- `build-logic/src/main/groovy/ce.forge-mod.gradle` holds the FG setup, runs, mods.toml expansion, manifest (MixinConfigs from mixin_configs), archiveJar, and `installToMods`.
  - `installToMods` is explicit only. It moves old versions and replaces_jars matches to `<mod>/jar_archive/displaced/`, never deletes, then copies the jar to mods/.
- `ce.forge-mod-refmap.gradle` adds MixinGradle. A plugin that build-logic only depends on can't be applied by id from a mod's build.gradle, which is why it is a separate wrapper.
  - Since 2026-10-27 it owns the whole refmap setup: `refmap_name` (required) + `refmap_source_sets=<set>:<refmap>,...` (exposure addons) in gradle.properties; no `mixin {}` block in mod build.gradle files. Jar check: refmap_name must be in the jar + every mixin config with mixins must find its refmap. Exposure's expanded/catalog refmaps are never written (no remapped mixins), so only the main one is presence-checked.
- ce.forge-mod also (2026-10-27): default `flatDir 'libs'` repo for every mod; `disable_target_validator=true` property; upstream-resource guard via `resource_guard_jars` / `resource_guard_own_files` / `resource_guard_name` / `resource_guard_arr` (NOT `upstream_jar`: that name is taken by ce.forge-repack and the port tools; reusing it made backportedDisplay fail). Details: feature_writeup/pack_tooling.md 2.8.
- Both FG and mixingradle are dependencies of build-logic itself, so they load in one classloader.

**Verified:** after the restructure, the patcherMod jar content is byte-identical to the pre-restructure baseline (680 files, manifest the same apart from the timestamp).

**Dev-run pins (in ce.forge-mod, all configurations):** Forge's open POM ranges resolve newer loader libraries, which broke runClient/runServer/runGameTestServer. This affects patcherMod too; it had never run in dev.
- force forgespi 6.0.0, unsafe 0.2.0, log4j-api/core 2.17.0
- exclude net.minecraftforge:securemodules, net.minecraftforge:modlauncher, slf4j-simple
- Errors seen without the pins:
  - "reads more than one module named cpw.mods.securejarhandler"
  - NoSuchMethodError LoaderUtil.getClassLoaders
- The pins have to cover FG's own `minecraft` config (the legacyClassPath file), not just runtimeClasspath.
- The reference versions are the `libraries` list in the instance's instance.json.

**Reproducible jars (2026-10-01):**
- The manifest has no Implementation-Timestamp any more, and archive tasks use preserveFileTimestamps=false and reproducibleFileOrder=true.
- Rebuilding unchanged source gives a byte-identical jar (sha1 checked across two builds).
- A root `./gradlew build` still re-runs every mod's jar/reobfJar tasks, because compile is up-to-date but the mixin/reobf steps re-run; the output is identical.
- `./gradlew :patcherMod:build` or `cd patcherMod && ./gradlew build` builds only patcherMod.
- Jars ship the project's LICENSE/LICENSE.txt/NOTICE* files as `<jar_name>-<file>` (memory `license-gplv3`).

tools/build_deploy.py uses an `IN_TREE_MODS` list (project, jar prefix, required) for preflight and jar archiving ([deploy-pipeline](deploy-pipeline.md)). Related: [patchermod-coremod](patchermod-coremod.md), [exposure-backport](exposure-backport.md).
- `shared_sources` / `shared_base` in a mod's gradle.properties (2026-10-27): ce.forge-mod's `sharedSources` Sync task copies `development/shared/<module>/` into `build/generated/sources/shared/`, rewriting the template packages `ce.shared` / `ce.mixin` to `<shared_base>.shared` / `.mixin`. Edit only the templates. Modules: packs, upstream, mixinplugin, itemhidden (feature_writeup/pack_tooling.md 2.8).
