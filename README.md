# CreativeEngineering 2

A Minecraft **1.19.2 / Forge 43.3.9** modpack (ATLauncher instance), built around Create and
Immersive Engineering, with a large KubeJS layer and in-tree Forge mods (`patcherMod`, `exposureBackport`).

This repo holds the pack's own source, not the whole instance. Third-party mod jars, resource
packs and shaders are **not** committed. [`pack/MODLIST.md`](pack/MODLIST.md) and
[`pack/manifest.json`](pack/manifest.json) list every one of them with its version,
CurseForge/Modrinth link and sha1.

## Layout

| Path | What it is |
|---|---|
| `kubejs/` | KubeJS scripts (startup/server/client), data, assets, worldgen generation scripts |
| `config/`, `defaultconfigs/` | Mod configs as used by the pack |
| `configs_edited/` | Backups of every hand-edited config (`root_config/`, `server_config/`) |
| `development/patcherMod/` | Source of `creativeengineering-<ver>.jar` (Forge + Mixin coremod) |
| `development/exposureBackport/` | Source of `exposureBackport-<ver>.jar`: Exposure 1.9.19 + its Polaroid/Expanded/Catalog addons ported to 1.19.2. Replaces the stock Exposure 1.6.0 jar (Expanded and Catalog not ported yet) |
| `build-logic/`, `settings.gradle`, `gradle.properties` | Gradle build shared by the in-tree mods |
| `pack/` | Generated mod / resource pack / shader pack manifest |
| `mod_sides.md` | Client / server / both classification for every mod |
| `tools/` | Python/JS helper scripts (manifest, server mod folder, generators, audits) |
| `feature_writeup/`, `todo/` | Design notes and test writeups |

## Rebuilding the pack

1. Create a Minecraft 1.19.2 + Forge 43.3.9 instance (Java 17).
2. Download every mod in `pack/MODLIST.md` into `mods/`. Check each file against the `sha1`
   in `pack/manifest.json`.
3. Build `patcherMod` and `exposureBackport` and put their jars in `mods/` (see below, `installToMods`).
4. Copy `kubejs/`, `config/` and `defaultconfigs/` into the instance.
5. Download the resource packs into `resourcepacks/` and enable them in the order listed in
   `pack/MODLIST.md`. `CE2 Iron Armor Fix.zip` is committed here, since it has no download source.
6. For a dedicated server, run `python3 tools/dump_server_mods.py` to copy only the `both`/`server`
   mods into `server_mods/`.

## Building the in-tree mods

The instance root is one Gradle build with a subproject per mod. Shared Forge setup is the
`ce.forge-mod` convention plugin in `build-logic/`; shared versions (Minecraft, Forge, mappings)
are in the root `gradle.properties`, per-mod ones in `development/<mod>/gradle.properties`. Gradle project
names stay `:patcherMod` / `:exposureBackport`.

```
./gradlew build                          # every mod
./gradlew :patcherMod:build              # one mod (same as: cd development/patcherMod && ../../gradlew build)
./gradlew :exposureBackport:runClient    # dev client with just that mod (also runServer, runGameTestServer)
./gradlew :patcherMod:installToMods      # build, then swap the jar into mods/ (old one -> development/<mod>/jar_archive/displaced/)
```

`development/<mod>/libs/` is gitignored. It holds compile-time copies of mod jars from `mods/`, so
populate it from a rebuilt `mods/` folder before building. patcherMod only runs
reobfuscated in the instance (its mixins use SRG names); exposureBackport has real Mixin
refmaps and also runs in the Gradle dev runs.

## After changing mods

Re-run `python3 tools/gen_pack_manifest.py`, update `mod_sides.md` for any new mod, and commit
`pack/`.
