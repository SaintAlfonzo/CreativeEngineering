# CreativeEngineering 2

A Minecraft **1.19.2 / Forge 43.3.9** modpack (ATLauncher instance), built around Create and
Immersive Engineering, with a large KubeJS layer and a small in-tree Forge mod (`patcherMod`).

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
| `patcherMod/` | Source of `creativeengineering-1.0.2.jar` (Forge + Mixin coremod) |
| `pack/` | Generated mod / resource pack / shader pack manifest |
| `mod_sides.md` | Client / server / both classification for every mod |
| `tools/` | Python/JS helper scripts (manifest, server mod folder, generators, audits) |
| `feature_writeup/`, `todo/` | Design notes and test writeups |

## Rebuilding the pack

1. Create a Minecraft 1.19.2 + Forge 43.3.9 instance (Java 17).
2. Download every mod in `pack/MODLIST.md` into `mods/`. Check each file against the `sha1`
   in `pack/manifest.json`.
3. Build `patcherMod` and copy the jar into `mods/` (see below).
4. Copy `kubejs/`, `config/` and `defaultconfigs/` into the instance.
5. Download the resource packs into `resourcepacks/` and enable them in the order listed in
   `pack/MODLIST.md`. `CE2 Iron Armor Fix.zip` is committed here, since it has no download source.
6. For a dedicated server, run `python3 tools/dump_server_mods.py` to copy only the `both`/`server`
   mods into `server_mods/`.

## Building patcherMod

`patcherMod/libs/` is gitignored. It holds compile-time copies of mod jars from `mods/`, so
populate it from a rebuilt `mods/` folder before building:

```
cd patcherMod && ./gradlew build
```

## After changing mods

Re-run `python3 tools/gen_pack_manifest.py`, update `mod_sides.md` for any new mod, and commit
`pack/`.
