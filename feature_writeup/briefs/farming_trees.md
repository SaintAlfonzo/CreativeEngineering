# farming / trees: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# farming`, Sub `trees`. Feature writeup:
[mob_block_tweaks.md](../mob_block_tweaks.md) 2.5 (test prefix `MT-`).

## Scope and order

Nothing left to build. The subfamily's only row (`trees #1`) is at Testing. It needs the next deploy and play, not
code. Its open decision (F6, diagonal-only leaves) is listed below for whoever picks it up after testing.

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| trees #1 | Leaf life distance + decay timing + Fast Leaf Decay port | Testing | Code present as described: `[leaf_decay]` in [CEConfig.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java) ~558-590 (`enabled`, `onlyWhenDisturbed`, `maxDistance` 12 in 6..32, plus `decayMinTicks` / `decayMaxTicks` / `leafPiles`), live in `config/creativeengineering-common.toml` 1876; `LeavesDecayRangeMixin` (mixins.json 118), `block/LeafDecayRange`, `FastLeafDecayPort`, `LeafDecayPiles`. **Stale in the row:** "Built 2026-10-19 into patcherMod 1.0.9 in `mods/`, not deployed". The deployed 1.0.9 (2026-10-19 15:54, [DEPLOY_LOG](../../deploy/DEPLOY_LOG.md)) predates the leaf work (that deploy removed only `rsec`, not FastLeafDecay-30). `gradle.properties` is now `mod_version=1.1.01`, and the leaf code ships with the 1.1.01 jar in `mods/`. Tests: MT-13 to MT-25 pending in game (MT-24 leaf piles, MT-25 pile slow height were added 2026-10-20) |

### Open decision carried by the row (not a build task yet)
- **F6, diagonal-only leaves fall at placement** ([mob_block_tweaks.md](../mob_block_tweaks.md) 4, F6, with the
  options listed there): 189 leaves on 29 tree features are touched only diagonally and decay right after worldgen
  because `TreeFeature`'s edge shape updates count as a neighbour change. Recommendation: play MT-17 (big trees in new
  chunks) first. Only if the bare corners show in game, pick one of F6's options and file it as a new `trees` row.

## Writeup corrections made

- [mob_block_tweaks.md](../mob_block_tweaks.md) 0 (leaf decay row): "BUILT 2026-10-19 (patcherMod 1.0.9 rebuild, in
  `mods/`, not deployed)" -> "(a patcherMod rebuild after the 1.0.9 deploy, now patcherMod 1.1.01 in `mods/`; not
  deployed)".
- [mob_block_tweaks.md](../mob_block_tweaks.md) 0 Pending: "MT-01 to MT-23 untested" -> "MT-01 to MT-25 untested"
  (MT-24 / MT-25 exist in 7).
