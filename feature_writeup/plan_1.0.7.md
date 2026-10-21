# 1.0.7: structure test fixes, the cooking stations, JEI ordering and the Vinery port

DEPLOYED 2026-10-18 19:43 to the server and the clients (snapshot recorded that evening, `deploy/DEPLOYED.json`).
1.0.7 is everything built after the 1.0.6 deploy (2026-10-14 13:01, commit `15c43a0`) up to commit `908e831` plus 3
uncommitted climate samples. The payload is an update from 1.0.6. This file was written after the fact, on
2026-10-19, from the deploy log, the shipped jars and the feature writeups; no plan doc existed for 1.0.7 before it
went out.

It was staged twice on 2026-10-18. The first staging (18:20, from `dc669e2`) went onto the dedicated server, which
crashed four times between 18:52 and 18:59 on the Vinery port (VB below; logs in `debug/10_18/`). The fix was
restaged at 19:29 under the same jar name, and that second staging is what both sides run.

## 0. At a glance

Kind: feature, fix, mods, config, bookkeeping. Nothing below has been tested in game unless the row says so; the
test IDs named are the ones still open.

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| SF | Structure and locate fixes from the 1.0.6 scratch run: canonical pool order (`[worldgen.pools] canonicalOrder`, a seed's villages stay the same across restarts), code-added village pieces (Cloud Storage, Domestication Innovation) kept on reload, ground match over deep structures, driftwood on banks (Upgrade Aquatic beach / river / swamp / extra placed features), cave fit `land/any/deep` for the 3 deep dungeons, stone bases count in earth fill (`#creativeengineering:earth_fill/stone_base`, drum tower), non-blocking locate loads (LO-05), census "known" (LO-03, LO-14), `/celocate stop` label | fix | DEPLOYED; ST-191 to ST-198 and LO-03, LO-05, LO-14 reruns pending. New chunks only | [structure_tuning.md](structure_tuning.md) 3.23, [locate.md](locate.md) 3.2, 3.6, 8 |
| QS | Quark sort no longer deletes items from containers holding oversized stacks (`QuarkSafeSort` + `QuarkSortingHandlerMixin`) | fix | DEPLOYED; FS-01 to FS-05 pending | [testing/fridge_sort/plan.md](testing/fridge_sort/plan.md), [fixes_compat.md](fixes_compat.md) 2.20 |
| RB | Remainder buffer: bottles, bowls and buckets stay in the FD cooking pot and the Farmer's Respite kettle (mixins) and the oven (native), `[cooking] remainderBuffer` / `stallWhenFull` | feature | DEPLOYED; RB-01 to RB-11 pending; keg not covered (port) | [remainder_buffer.md](remainder_buffer.md) |
| OV | Oven: FD-style baking block (`creativeengineering:baking`, JEI page, heat below, stove heat above), bakeware items, first baking recipes (`baking.js`, `bakeware.js`) | feature | DEPLOYED in its first form; 1.0.8 adds ingredient counts, the 37-recipe set, hammered bakeware and cornmeal. OV-01 to OV-19 pending | [oven.md](oven.md) |
| JO | JEI ordering steps 1 to 3: patcherMod sorter + JEI mixins, `global.jeiOrder` definition (`startup_scripts/jei/order.js`, `func/jei_order.js`), `jei_order_tags.js`, `client_scripts/jei_order.js`, report tool; dead fluids now come from `packHidden('fluid')` | feature | DEPLOYED; JO-01 to JO-12 pending; filling the tree not started | [jei_ordering.md](jei_ordering.md) |
| VB | Vinery port `backportedVinery-1.5.5-ce-19.2-01` replaces Let's Do Vinery 1.3.10 and its API (`doapi`); the two unmodified upstream jars ship in `vinery_upstream/` as resource donors; winemaker leggings id in `fabric.js` / `misc_mods.js`; grape and fruit bags compost at 1.0. **Hotfix in the 19:29 restage:** `SpreadableGrassSlabBlock.performBonemeal` crashed the server with Immersive Weathering (foreign `fertile` property), rebuilt under the same `-01` name | feature + fix | DEPLOYED; the first staging crashed the dedicated server, the fixed `-01` runs there; VB-01 to VB-66 pending | [alcohol.md](alcohol.md) Part B, [vinery.md](vinery.md) |
| AL | Brewin' port `backportedBrewin-4.5.0-ce-19.2-01` shipped **beside** `BrewinAndChewin-1.19-2.0.jar` (both in `mods/` at staging, one mod id; FML picked the port on the dedicated server and the dev client). The log entry lists neither as added, because the mod-id diff saw `brewinandchewin` already there. 1.0.8 removes both and ships `-02` | mods | DEPLOYED with two jars of one mod id (2.2) | [alcohol.md](alcohol.md) 8.1 |
| EX | Exposure `-04`: the viewfinder no longer outlives its player (respawn, dimension change, rejoin) and no longer eats clicks inside open screens | fix | DEPLOYED; untested | [exposure.md](exposure.md), [fixes_compat.md](fixes_compat.md) 2.21 |
| NM | New mods: Create: The Salt 1.1.0, Storage Labels 1.10 (both). Resource packs Fancy Crops 1.2 and Fancy Beds 4.2 (clients enable them). 14 `mod_sides.md` rows relabelled server -> both (section 3) | mods | DEPLOYED | this file 3 |
| KJ | Other KubeJS: `effect_dict.js`, `multiblock_dict.js`, splash line, `prepared_flesh.png` and `cornmeal.png` sprites (cornmeal moved under `assets/creativeengineering` in 1.0.8) | data | DEPLOYED | none |
| CF | Config: `brewinandchewin-common.toml` and `vinery-common.toml` added (port configs); `creativeengineering-common.toml` (`[worldgen.pools]`, `[cooking]`, JEI order keys); JEI sort orders; game-rewritten `rare-ice.properties`, WorldEdit | config | DEPLOYED | this file 1 |
| VR | Version records: patcherMod **1.0.7** (sha1 9dd6ae4c, built 2026-10-18 16:27), exposureBackport `-04`, backportedVinery `-01` (hotfix rebuild, sha1 a98df174), backportedBrewin `-01`; `vinery_upstream` category and the port's sha1 pins in `build_deploy.py`; then patcherMod `mod_version` 1.0.8 for the next build | bookkeeping | DONE 2026-10-18 | [deploy.md](deploy.md), this file 4 |

## 1. What the payload contains

Update from 1.0.6: 51 files, 15.0 MB (mods 6, kubejs 34, config 7, resourcepacks 2, vinery_upstream 2). The full
list is the 1.0.7 entry in [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md).

- **mods:** `creativeengineering-1.0.7.jar`, `exposureBackport-1.9.19-ce-19.2-04.jar`,
  `backportedVinery-1.5.5-ce-19.2-01.jar`, `backportedBrewin-4.5.0-ce-19.2-01.jar`, `create_salt 1.19.2-1.1.0.jar`,
  `labels-1.19.2-1.10.jar`.
- **vinery_upstream/** (next to `mods/`, never inside it): `letsdo-vinery-fabric-1.5.5.jar`,
  `letsdo-vinery-forge-1.3.10.jar`. The port refuses to start without them.
- **kubejs:** JEI order scripts and tags, oven scripts, driftwood placed features, `biome_fits.json`,
  `pool_config.json`, `structure_tags.json`, `earth_fill.js`, compost values, climate samples.
- **resourcepacks:** `Fancy Crops v1.2.zip`, `Fancy_Beds-v4.2.zip`.
- **removals, both sides:** `mods/creativeengineering-1.0.6.jar`, `mods/doapi-1.1.0b.jar`,
  `mods/exposureBackport-1.9.19-ce-19.2-03.jar`, `mods/letsdo-vinery-forge-1.3.10.jar`.

## 2. Checks and open items

**Evidence at staging:** preflight clean apart from the manifest and dirty-tree warnings; the in-tree jars equalled
their builds. The structure fixes had passed their offline checks against the mapped 1.19.2 jar. The 18:20 jar set
booted on the dedicated server (`debug/10_18/debug.log`) and crashed on the grass slab; nothing else from our code
failed in that log. The fixed restage was not booted again before the 19:43 snapshot.

Open (carried into [plan_1.0.8.md](plan_1.0.8.md)):
- [ ] Tests: ST-191 to ST-198, LO-03, LO-05, LO-14 reruns; FS-01 to FS-05; RB-01 to RB-11; OV-01 to OV-19; JO-01 to
  JO-12; VB-01 to VB-66; EX viewfinder.
- [ ] `pack/manifest.json` was stale at staging (regenerated for 1.0.8).
- [ ] Commit: 908e831 + 3 climate samples at staging.

### 2.1 Vinery port on a live world

1.0.7 swapped Vinery 1.3.10 for the port on the server's existing world. The port keeps every 1.3.10 id (alcohol.md
B4.1); how placed blocks, block entities and old wine stacks are carried over is in alcohol.md B3.3 (old worlds) and
B4.9 (port status). The pack rule for both ports is no conversions between versions beyond what B3.3 lists.

### 2.2 Two Brewin' jars with one mod id

The 1.0.7 server and clients got `backportedBrewin-...-01.jar` while `BrewinAndChewin-1.19-2.0.jar` was still in
their `mods/`. Two jars with one mod id aren't a supported setup. FML 43 keeps the newest version and logs it at
DEBUG only: `debug/10_18/debug.log` 18:54:15 "Found 2 mods for first modid brewinandchewin, selecting most recent
based on version data ... Selected file backportedBrewin-4.5.0-ce-19.2-01.jar". So the server has run the port
since 1.0.7, jams included (`-01` still registered them). 1.0.8's removals delete both jars and ship `-02`, which no
longer has the jams ([plan_1.0.8.md](plan_1.0.8.md) 2.1).

Found while writing this: `build_deploy.py` keys mod changes by mod id, so a second jar for an existing id is neither
reported as added nor stopped by preflight. A preflight error for two jars with one mod id in `mods/` would have
caught it ([deploy.md](deploy.md), open).

## 3. New mods and sides

| Mod | Side | Note |
|---|---|---|
| Create: The Salt 1.1.0 | both | salt integration for Create |
| Storage Labels 1.10 | both | decorative item labels |
| Vinery (Backport) `-01` | both | replaces Let's Do Vinery 1.3.10 + Let's Do API |
| Brewin' and Chewin' (Backport) `-01` | both | beside the 2.0 jar (2.2) |

Relabelled server -> both in `mod_sides.md` (no change in where the jar goes: the client already gets every server
mod, because clients host single-player worlds): Async Locator, Block Runner, Cherished Items, Chunky, Create: Fast
SchematicCannon, Create: Liquid Fuel, Death Knell, Glowing Illagers, Ice Prevents Crop Growth, Lightman's Discord
Integration, Loading Protection, Rain should extinguish campfires, Simple Backups, Tax Free Levels.

## 4. Version records

- patcherMod **1.0.7** shipped: sha1 9dd6ae4c, built 2026-10-18 16:27, in `jar_archive/` and `jar_archive/displaced/`.
  Against 1.0.6 it adds the JEI order classes, `QuarkSafeSort`, the remainder buffer and its 8 pot / kettle mixins,
  the `oven/` package, and the SF fixes in existing classes; `FermentationBarrelStackOutputMixin` is gone (the port
  replaces 1.3.10's barrel).
- exposureBackport `-04` (2026-10-14 20:05 build), backportedVinery `-01` (hotfix rebuild, same name),
  backportedBrewin `-01`. incubationAdditions and moreMooblooms unchanged at 1.0.0.
- History json rewritten at the 19:29 restage (`deploy/history/1.0.7.json`), so it reflects what shipped.
- After staging: patcherMod `mod_version` = 1.0.8; Brewin port bumped to `-02` because `-01` shipped.
