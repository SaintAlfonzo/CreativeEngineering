# 1.0.8: time master, no advancements, both alcohol ports at -02, the baking update

**DEPLOYED 2026-10-19 10:20 (server + clients).** Restaged 2026-10-19 10:06 in `deploy/latest/` (replaced the 01:16
staging, whose patcherMod jar could not boot a dedicated server). 1.0.8 is everything built after the 1.0.7 deploy (2026-10-18 19:43, commit `908e831`) up to the working tree on
2026-10-19 00:50, plus the server crash fix and the worldgen fixes from the structure scratch run (row WG). The payload is
an update from 1.0.7: the 01:33 1.0.8 snapshot was rolled back to 1.0.7 so the version could be rebuilt.

## 0. At a glance

Kind: feature, fix, data, config, mods, bookkeeping. Evidence for every row: one single-player session on 2026-10-19
00:53 to 01:11 with exactly these three jars (section 2), then three scratch dedicated-server boots of the restaged jar.
Deployed 2026-10-19 10:20; superseded the same day by 1.0.9 ([plan_1.0.9.md](plan_1.0.9.md)). Nothing below was tested
in game unless the row says so.

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| TI | Time master: `[time]` in `creativeengineering-common.toml` sets Better Days day length, Serene Seasons / SeasonHUD season length and Vinery wine aging; seasons follow the sun (with a one-time migration per dimension), pause when the server is empty, SeasonHUD day / time of day, Serene Seasons calendar tooltip, `/cetime`, presets SINGLEPLAYER / SERVER / CUSTOM. Pack values: 45 min day + 15 min night, 8-day sub-seasons, wine month = sub-season | feature | DEPLOYED; the push and the sun migration ran in single player (2); TI-01 to TI-29 pending | [time.md](time.md) |
| AV | Advancements switched off pack-wide (`[advancements] enabled = false`, `ServerAdvancementManagerMixin`) | feature | DEPLOYED; ran in single player: 14,828 left out (2) | [advancements.md](advancements.md) |
| AL | Brewin' port `-02` replaces both `BrewinAndChewin-1.19-2.0.jar` and `-01` (1.0.7 shipped them together): keg fluid in/out slot pair, pipe rule (`[keg] pipeDrainOnlyWhenIdle`), JEI [+] fills and empties the tank (`KegTransfer`, network protocol `4.5.0-2`), drink effect lines on the keg / tank / JEI, jams removed, Steel Toe Stout Resistance II 90 s, Tipsy camera drift and Intoxicated AppleSkin wobble, `cookingtime` in the cooking-pot files, undead tag. Pack: `alcohol.js` (7 fermenting changes, pierogi, pizza, ham and cheese, quiche + assemblies, 32 spout / drain), `R.bncFermenting`, `R.createEmptying`, `#creativeengineering:metal_dusts`, 18 keg fluids in `fluid_dict.js`, cask / crate tooltips, coaster placeable tag, quest item `scarlet_pierogi` | feature | DEPLOYED; loaded in single player, not tested; AL-01 to AL-72 pending | [alcohol.md](alcohol.md) Part A, 8, 10 |
| VB | Vinery port `-02`: JEI, Apple Press valid items, merged duplicate effects, JEED providers, mead removed (Brewin's is the only mead), banner Health Boost II 60 s, the port side of the time master (wine months, sun clock, cellar x2.5, Vinery calendar removed), fermentation barrel shown as "Cask", apple wine 2 to 3 bottles, King Danis Wine removed, Praetorian fits the racks, WTHIT stem / lattice lines; banner crafting recipe in `alcohol.js`. `vinery-common.toml` rewritten to the new keys | feature | DEPLOYED; loaded in single player, not tested; VB-48 to VB-66 pending | [alcohol.md](alcohol.md) B7, 10; [vinery.md](vinery.md) |
| OV | Baking: 37 oven recipes (muffins, cookies, tarts, pies, cakes) with flour, crust and Create cake base, each needing its bakeware; cake base also made in the oven; ingredient counts behind `[cooking] ovenIngredientCounts` (off: 9 recipes trimmed to 6 slots); bakeware chain by IE hammer (plate -> baking sheet -> muffin tin -> pie tin -> cake pan) on the cutting board or in the grid; the hammer no longer rotates the cutting board (`#creativeengineering:hammer_no_rotate`, `IECompat`); own bakeware sprites; remainder buffer gives one leftover per item used. Cornmeal item: from corn and kernels on every crusher and PNC, in tortillas and cornbread batter, a flour in pancakes, pies and cakes; pie crust from 3 flour + milk | feature | DEPLOYED; loaded in single player; oven JEI [+] failed (2.2, fixed in 1.0.9); OV-01 to OV-19, RC-15 to RC-18 pending | [oven.md](oven.md), [recipes_cooking.md](recipes_cooking.md) 2.1, [cooking.md](cooking.md), [remainder_buffer.md](remainder_buffer.md) |
| PL | Placeable food: "Placeable on a coaster" line and tag (160 items), placeable catalog regenerated | feature + data | DEPLOYED | [placeable_food.md](placeable_food.md), [placeable_catalog.md](placeable_catalog.md), [tooltips.md](tooltips.md) |
| DR | Data: `food_types.json`, `stack_sizes.json`, `placeable.json`; climate samples and `biome_config.json` resampled 2026-10-18 23:31 (agreement: Overworld 98.9 %, End 80.1 %, Nether 86.4 %); `item_dict.js` / `block_dict.js` / `fluid_dict.js` entries for the ports | data | DEPLOYED | [biome_tuning.md](biome_tuning.md), [stack_sizes.md](stack_sizes.md) |
| CF | Config: `creativeengineering-common.toml` (`[time]`, `[advancements]`, `[cooking] ovenIngredientCounts`), `brewinandchewin-common.toml` (keg pipe rule), `vinery-common.toml` (new keys), `progression.snbt` (pierogi id), JEI sort orders (`brewinandchewin:aging` next to fermenting), game-rewritten `rare-ice.properties` and WorldEdit; defaultconfigs `sereneseasons-server.toml`, `seasonhud-server.toml` (the pushed 24000 / 8) | config | DEPLOYED | this file 1 |
| WG | Worldgen fixes from the scratch run ([testing/structures/11_fixes_1_0_8.md](testing/structures/11_fixes_1_0_8.md)): dedicated-server boot (Serene Seasons clock mixin split into server + client), `type/*` structure tags made real so earth-fill pruning runs on villages, `/dumpstructuregen` reads the protected placement fields (`PlacementInfo`), town companion villages 64-96 blocks out (were 112-160), `/ceplace templates` lists per pack, flat / Superflat worlds stay flat with the biome layout on (`RegistryLoaderPresetOverworldMixin`) | fix | DEPLOYED; scratch dedicated server 2026-10-19 (3 boots, all pass) | [structure_tuning.md](structure_tuning.md) 3.10, 3.13, 3.22 |
| VR | Version records: patcherMod **1.0.8** (md5 d60b5a99, rebuilt 2026-10-19 09:36; the 00:49 f136f818 build is in `jar_archive/displaced/`), backportedVinery **-02** (a3e1164d, 00:50), backportedBrewin **-02** (8441a046, 00:50), all archived; `backportedBrewin` in `IN_TREE_MODS`; `pack/manifest.json` regenerated; next builds bumped to patcherMod 1.0.9, ports `-03` | bookkeeping | DONE 2026-10-19 | [deploy.md](deploy.md), this file 4 |

## 1. What the payload contains

Update from 1.0.7: 41 files, 5.5 MB (mods 3, kubejs 28, config 8, defaultconfigs 2; the 01:16 staging was 38 / 5.4 MB). The full list is the 1.0.8 entry
in [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md).

- **mods (server + client):** `creativeengineering-1.0.8.jar`, `backportedVinery-1.5.5-ce-19.2-02.jar`,
  `backportedBrewin-4.5.0-ce-19.2-02.jar`.
- **kubejs:** `server_scripts/cooking/alcohol.js` and `startup_scripts/cooking/cornmeal.js` (new), bakeware and
  cornmeal sprites, `baking.js`, `cooking.js`, the dicts, `neoCraftHelper.js` (`ceBaking` counts, `hammering`,
  `bncFermenting`, `createEmptying`), tags, tooltips, generated food / stack / placeable data.
- **kubejs, from the restage:** `worldgen/structure_overrides.js` (`type/*` tags), `_lib/dump_structuregen.js`,
  `generation_scripts/worldgen/meta_structures.json` (companion distance).
- **config / defaultconfigs:** as in row CF.
- **vinery_upstream/:** nothing new (the 1.0.7 jars stay).
- **removals, both sides:** `mods/BrewinAndChewin-1.19-2.0.jar`, `mods/backportedBrewin-4.5.0-ce-19.2-01.jar`,
  `mods/backportedVinery-1.5.5-ce-19.2-01.jar`, `mods/creativeengineering-1.0.7.jar`, `kubejs/assets/cornmeal.png`.

## 2. Checks and open items

**Evidence:** preflight clean apart from the dirty-tree warning (structure_config regenerated equal, structure groups
in sync, mod sides and kubejs classes complete, Vinery donor jars match the port's sha1 pins). The three in-tree jars
equal their `build/libs` copies and no source file is newer. The user's client ran these exact jars on 2026-10-19
from 00:53 to 01:11 (`logs/latest.log`):
- 0 "Error occurred while handling event" (no KubeJS script errors), no mixin failures.
- `[advancements] 14828 advancements left out`; 931 "Ignored advancement" lines for the saved progress.
- `[time]` pushed Better Days, Serene Seasons, SeasonHUD and Vinery values, and every dimension logged "seasons now
  follow the sun; sub-season 2 day 3 kept".
- One error class from our code, the oven JEI [+] (2.2).

Until the restage nothing had booted on a dedicated server with this jar set (see below).

**Dedicated server (was a BLOCKER, fixed 2026-10-19):** the 00:49 jar did not boot a dedicated server
(`ClassMetadataNotFoundException: net.minecraft.client.Minecraft` from `SereneSeasonsClockMixin`). Its client inject is
now `SereneSeasonsClientClockMixin` in the client mixin list. The restaged jar (d60b5a99) booted a scratch dedicated
server three times on the 1.0.8 mod set (flat world, flat reload, normal world with a 2601-chunk pregen): 0 KubeJS
errors, `[time]` push and advancements lines present. Details: [testing/structures/11_fixes_1_0_8.md](testing/structures/11_fixes_1_0_8.md).

Before copying:
- [x] **Fix the dedicated-server crash above** and confirm one scratch-server boot (2026-10-19, three boots).
- [ ] **Decide the server's `[time] preset`** (2.3). As staged, the server gets SINGLEPLAYER: a wine level every
  sub-season (8 h at the pack's day length) instead of SERVER's one per season.
- [ ] Back up `world/advancements/` with the world if the progress might be wanted again (2.1).

Copying:
- [x] `verify.sh` on each side (it checks they run 1.0.7; both Brewin' jars from 1.0.7 are expected; a machine that
  already took the 01:16 payload shows those 1.0.8 files as drift, which is fine), apply the
  removals, copy, then `python3 tools/build_deploy.py --snapshot 1.0.8`.
- [x] The payload README's step 5 (copy changed `*-server.toml` into `<world>/serverconfig/` by hand) isn't needed
  this time: the time master writes the Serene Seasons and SeasonHUD server values itself at server start.
- [ ] First server boot: check `latest.log` for the `[time]` push lines, the "seasons now follow the sun" line per
  dimension, the advancements line, and missing-registry lines for the removed ids (2.1).

After:
- [ ] In-game tests: TI-, AV-, AL-, VB-, OV-, RC-15 to RC-18, PL-; plus the 1.0.7 backlog ([plan_1.0.7.md](plan_1.0.7.md) 2).
- [ ] `/dumpall` refresh with the ports (the 2026-10-18 dumps predate them): food types, stack sizes, effects, the
  Canned Kimchi can still copying Satisfaction ([alcohol.md](alcohol.md) 3.4).
- [ ] Commit: 44 shipped files uncommitted at staging (still open after 1.0.9: TODO `pack/release #1`).

### 2.1 One-way effects on the live world

| What | Effect | Writeup |
|---|---|---|
| `vinery:mead`, `vinery:king_danis_wine`, `vinery:calendar` | removed from the port; any in the world (shipped in 1.0.7's `-01`) disappear | [alcohol.md](alcohol.md) B7.6, 10.8, B7.8 |
| `brewinandchewin:sweet_berry_jam`, `glow_berry_marmalade`, `apple_jelly` | removed from the port; the server has had them since 1.0.7 (FML loaded `-01`, [plan_1.0.7.md](plan_1.0.7.md) 2.2); any made disappear | [alcohol.md](alcohol.md) 8.5 |
| Advancement progress | each player's file is rewritten without it on the first save after joining | [advancements.md](advancements.md) 3.1 |
| Serene Seasons counter | each dimension migrates once to the sun clock, keeping its sub-season and day (a per-level marker stops a second run) | [time.md](time.md) 3.3 |

### 2.2 Oven JEI [+] does nothing (since 1.0.7; fixed in 1.0.9, [plan_1.0.9.md](plan_1.0.9.md))

1,413 render-thread errors in the session: "Recipe Transfer helper class BasicRecipeTransferInfo does not work for
container class OvenMenu ... references crafting slot index [2] but the player cannot pickup from it". The handler
(`OvenJeiPlugin`, ingredient slots 0 to 5) hasn't changed since the 1.0.7 commit, so 1.0.7 has it too. A guess, not
checked: `SlotItemHandler.mayPickup` asks the oven's item handler for a simulated extract, which the oven refuses for
an ingredient slot in some state. Manual filling works; only JEI's [+] and the log spam are affected. Fix in 1.0.9
([oven.md](oven.md)).

### 2.3 Time preset on the server (open, from time.md 3.9)

`build_deploy.py` ships one `config/` to both sides, so the server gets whatever preset the dev instance has:
SINGLEPLAYER today. Options:
- **A (no code):** after copying, set `[time] preset = "SERVER"` in the server's `creativeengineering-common.toml`.
  The next payload that ships that file puts it back.
- **B:** a server-only override in the payload (a `server_config/` entry that wins over `config/` on the server).
- **C:** the preset follows the side: SERVER on a dedicated server unless the file says CUSTOM.

## 3. New mods and sides

None added or removed. `brewinandchewin` is now only the port (`mod_sides.md` row updated).

## 4. Version records

- patcherMod **1.0.8** ships (md5 d60b5a99, 2026-10-19 09:36 rebuild, archived; the 00:49 f136f818 build, which crashed
  dedicated servers, is `jar_archive/displaced/creativeengineering-1.0.8-f136f818-server-crash.jar`). Over f136f818 it
  adds `SereneSeasonsClientClockMixin`, `RegistryLoaderPresetOverworldMixin`, `worldgen/PlacementInfo` and the
  `MassPlacer` template listing. Against 1.0.7 it adds the `time/`
  package and 5 time mixins, `ServerAdvancementManagerMixin`, `IECompat.registerHammerNoRotate`, the oven ingredient
  counts and the remainder-buffer per-count leftovers. It overwrote an earlier unshipped 22:39 build of 1.0.8 in
  `jar_archive/`, whose changes are all in this one.
- backportedVinery **-02** and backportedBrewin **-02** ship (both archived). exposureBackport `-04`, incubationAdditions
  1.0.0, moreMooblooms 1.0.0 unchanged.
- A stray 1.0.8 log section from an 18:36 auto-bumped build on 2026-10-18 (no history json) was replaced by this
  staging.
- After staging: patcherMod `mod_version` = **1.0.9**, backportedVinery `1.5.5-ce-19.2-03`, backportedBrewin
  `4.5.0-ce-19.2-03` (gradle.properties only, not built), so no rebuild overwrites a shipped jar name.
