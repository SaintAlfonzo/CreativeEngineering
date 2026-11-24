# Plan: ready-now batch 2026-11-23 (fixes, publish rules, half cabinets, trash can, smoothies)

Owner request 2026-11-23: build the open rows that already carry an owner ruling and need no live diagnosis. Rows were
picked from the Planned rows with a Decisions cell; premises were checked against the code on 2026-11-23 (section 1).
Status lives in the TODO rows; this plan holds the design, order and rulings. Session role for every build phase:
`impl` (one code session at a time). Recipes through `global.NCH`, tags through `global.tags`, ids in the dicts.

Left out on purpose: `food-tuning #58` (Harmony from the Allay; another owner session holds it), `food-tuning #15`
(Food Dictionary sort; waits for the JEI food family order), `sweets #10` (sprite mass swap; no ruling yet), the
Raspberry Pie low-priority cleanups without a ruling (#12, #14 to #20).

## 0. Rulings (owner, 2026-11-23, AskUserQuestion)

<!-- generated:status_index begin open_work sha1=80b487291cd3 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 4 open TODO rows (Planned 2, Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `kitchen #41` | Planned |  | Storage Delight cabinets as half cabinets (high priority) |
| `storage-and-item-loss #17` | Planned |  | Trash can inventory slots (high priority) |
| `creative-common-publish #16` | Testing | SQ-CC12-01 SQ-CC12-02 SQ-CMV-02 SQ-CC-03 SQ-CGE-15 SQ-CPP-05 SQ-CCO-12a SQ-CCO-12b SQ-CCO-12c SQ-CCO-12d SQ-CCO-12e SQ-CCO-12f | NCH composter -> vanilla shapeless recipes name modded composters (twisted, ancient, blossom, azalea) when their mods are absent |
| `tooling/design-rules #4` | Testing |  | Palette test tooltips off before distribution |
<!-- generated:status_index end open_work -->

| # | Question | Ruling |
|---|---|---|
| R1 | Trash can screen vs the Trash Cans mod's own filter screen | A right-click on the Item Trash Can opens a 27-slot screen that deletes its contents on close; a sneak right-click keeps the mod's filter screen. The Ultimate Trash Can is unchanged |
| R2 | Half cabinet textures | Model UVs crop the existing Storage Delight full-cabinet textures; no new texture files |
| R3 | Scope of `publish-readiness #6` | All temporary exemptions in this plan, the Creative Delight package and own-id renames with save swaps included |
| R4 | Phase order | Fixes, then publish rules, then features (sections 2 to 9 below in that order) |
| R5 | Diet Log in the pack, now that the pack's override recipe is gone (removed 2026-11-21) and the jar recipe takes any edible item | Exclude non-foods and drinks: the pack adds them to the jar's exclusion tag (owner, 2026-11-23) |

Earlier rulings this plan carries: `kitchen #41`, `storage-and-item-loss #17`, `creative-compat-publish #7`,
`publish-readiness #6`, `raspberry-pie-publish #10` and `#13` (owner, 2026-11-22 and 2026-11-23, in the rows);
smoothies: [plan_smoothies.md](plan_smoothies.md) section 1.

## 1. Premise checks (2026-11-23)

- Composter: `kubejs/data/creativecommon/kubejs/recipes/composters.js` guards the shaped recipes with `global.has`
  (`loaded`), but the any-composter -> vanilla shapeless list uses `composters = [...species, ...modded]`, unguarded.
- Storage Delight is this pack's MIT repack in `development/repackedStorage` (namespace `storagedelight`, overlay
  GPL-3.0-only). 45 cabinets: 9 woods x 5 styles (`glass_W_cabinet`, `W_cabinet_with_glass_doors`,
  `W_cabinet_with_polished_andesite_countertop`, `W_cabinet_with_polished_deepslate_countertop`,
  `W_single_door_cabinet`). The half cabinet shape is Hearth and Harvest's `HalfCabinetBlock`
  (`creativeDelight/members/hearth/.../block/HHBlocks.java` line 102, `hearthandharvest:W_half_cabinet`).
- Trash can: the block is the third-party Trash Cans mod (`trashcans-1.0.18`, `trashcans:item_trash_can`), whose
  right-click opens a one-slot filter screen. The FTB-style screen is a new menu.
- `publish-readiness #6` scope ([publish_readiness.md](publish_readiness.md)): PR-CC-08 (`OwnConfigLogic` /
  `OwnConfigWatcher` hard-code own mod ids), PR-CC-27 (`creativeengineering$` handler names, `[CE potions]` log tag,
  `ce$` fields), PR-CP-11 and -12 (`creative-compat-publish #7`), PR-CP-24 (44 block ids in `passthrough_tags.js`),
  neoCraftHelper literals, generated `can_types.js`, `salt/create_salting.js`, PR-CM-14 (`enders_additions`).
  Underscored member ids `miners_delight`, `oceanic_delight`, `roll_delight` are upstream ids kept by ports (DR-C10:
  legitimate, not renamed). Underscored own packages: the five `creativedelight/mixin/<mod_id>` folders
  (`immersive_weathering`, `create_central_kitchen`, `mysterious_mountain_lib`, `upgrade_aquatic`, `the_dirty_stuff`).
  Miner's `sticky_basket` / `copper_pot` packages are upstream (`com.sammy`), kept.
- Raspberry Pie: recipe `solRaspberryPie/src/main/resources/data/solraspberrypie/recipes/diet_log.json` with the
  `diet_log_foods` tag (17 vanilla foods); sync in `net/S2CState.java` and `logic/SlotActions.java` (PR-RP-13);
  diversity table read per side (PR-RP-17).

## 2. P1 fixes (Creative Common)

Rows: `creative-common-publish #16`, `tooling/design-rules #4`.

- `composters.js`: build the shapeless list from `loaded` (vanilla species plus the guarded modded ones), so absent
  mods add no recipe. Recheck the `cc_only` boot has no NCH ERROR.
- `config/creativecommon-common.toml` `[tooltips] paletteDemo = false` and its `configs_edited/root_config/` copy.
  The demo is player-visible: one changelog bullet (catch-all heading).
- `:creativeCommon:build`, `installToMods`. Tests: rerun stage a of r1123cc (`SQ-CC12-01`, `-02`), queue row for the
  palette demo (prefix from [test_prefix_index.md](testing/test_prefix_index.md)).
- Changelog: composter fix gets no bullet (the error is in the log only, nothing a player sees in the pack).

## 3. P2 Raspberry Pie publish fixes

Rows: `raspberry-pie-publish #10`, `#13`. Unblocks `#1` (isolation runs) and `#7` (release jar).

- #10 recipe: the standalone Diet Log recipe takes any edible item not excluded. A custom ingredient (or recipe
  serializer) that matches `isEdible()` minus the jar's exclusion tag; the pack's own recipe override stays as it is.
  The `diet_log_foods` tag is dropped or kept as the JEI example list.
- #10 sync (PR-RP-13): drop counts computed on demand or cached per state stamp; the drink id list sent only when it
  changed; one sync per food eaten, not two per slot action.
- #13: the server's diversity table goes to the client on login and on `/reload` (new S2C packet); the client stops
  reading its own copy.
- `:solRaspberryPie:build`, install. Tests: SQ-RPS rows for a vanilla-only Diet Log recipe and a dedicated server
  whose table differs from the client's; headless timing of the sync before and after.
- Changelog: none in the pack (pack behavior unchanged); the jar's own changelog gets the lines at release.

## 4. P3 Compat and Common naming (exemptions part 1)

Rows: `creative-compat-publish #7`, `publish-readiness #6` (PR-CC-08, PR-CC-27, PR-CP-11, PR-CP-12).

- PR-CP-11: the four `[jei]` keys stay on Compat, renamed so no key names Delight or Brewing (generic category
  names); old keys read once and carried over, and the `configs_edited/` copy follows.
- PR-CP-12: `VineShearing`, `StuckInBlock`, `CompoundEffectTooltip`, `CorpseDecayMixin` stay on Compat; every class,
  lang key and global of a higher jar they name moves behind a registration the higher jar makes (tag, Common hook or
  data file), so Compat holds no string of Delight, Brewing or Loot.
- PR-CC-08: own jars detected by `META-INF/ce-build.json` instead of the hard-coded `OWN_MOD_IDS`.
- PR-CC-27: handler names, log tag and fields renamed to `creativecommon` forms.
- Builds: Common, Compat, then each higher jar that now registers (Delight, Brewing, Loot). Tests: config carry-over
  row, one row per moved class (behavior unchanged), `tools/design_rules_audit.py` DR-C7 clean for Common and Compat.

## 5. P4 literal fixes (exemptions part 2)

Row: `publish-readiness #6` (DR-C2 items).

- neoCraftHelper literals -> dict keys (Common).
- `can_types.js`: fix the generator so it writes dict keys, regenerate (generated output in its own commit).
- `salt/create_salting.js` literals -> dict keys.
- PR-CP-24: the 44 block ids in `passthrough_tags.js` -> `block_dict.js` keys (the 139 entity ids stay).
- Remove each exemption line from the audit / `check_known.csv` as it is fixed. Tests: headless lints; one boot that
  checks recipe and tag counts are unchanged (dump diff).

## 6. P5 Creative Delight renames (exemptions part 3)

Row: `publish-readiness #6` (DR-C10, PR-CM-14 and the underscored packages).

- Packages: the five `creativedelight/mixin/<mod_id>` folders lose the underscore (mixin config entries follow). No
  save effect.
- `enders_additions` -> `endersadditions`: mod id, item, block and lang namespace, `item_dict.js` / `block_dict.js`
  values (Common rebuild), config file `enders_additions-common.toml` -> new name with a one-time carry-over and its
  `configs_edited/` copy, every swap list entry ([swaps](../development/creativeDelight/src/main/resources/data/creativedelight/swaps/)
  `cd_namespace.json` pattern) so old saves map the old ids. ARR route unchanged
  ([agent note](agent_notes/arr-mod-overrides-route.md)).
- Remap gotcha: Forge resolves survivors in `RegistryManager.ACTIVE`
  ([agent note](agent_notes/forge-missing-mappings-staging.md)); test on a fresh save made with the old ids.
- Builds: Common, Creative Delight. Tests: old-id save carries items, blocks and config; `mod_sides.md` row updated.
- Changelog: none if old saves carry over unchanged; dev-only otherwise.

## 7. P6 Storage Delight half cabinets

Row: `kitchen #41`.

- 45 new blocks in `development/repackedStorage` under `storagedelight` (`<cabinet id>_half`, final names checked
  against the half wall cabinet naming in Hearth and Harvest): the `HalfCabinetBlock` shape copied into the repack
  (no Creative Delight dependency), same storage size as Hearth's half cabinet, same placement as the existing half
  wall cabinets.
- Models: per style, a half-width model whose faces use UV crops of the full cabinet's own textures (R2). A
  `sources.csv` row per style only if a new texture file appears (none expected).
- Recipes (`global.NCH`): 2 halves stacked vertically -> 1 full; 1 full -> 2 halves (shapeless). Ids in
  `item_dict.js`; tags as the full cabinets have them (mineable/axe, cabinets tag, Storage Delight item tags).
- Builds: repackedStorage, Common (dict). Tests: SQ rows for place, store, break-drops, both recipes, one per style.
- Changelog: family `## Storage Delight` (or the cabinets family if one exists): "Added half cabinets for every
  Storage Delight cabinet".

## 8. P7 Trash can screen

Row: `storage-and-item-loss #17`.

- Creative Compat mixin on `trashcans:item_trash_can`'s block `use` (mixin plugin skips it when Trash Cans is absent):
  not sneaking -> open a 3-row chest menu over a fresh container that clears on `removed()`; sneaking -> the mod's
  original screen (R1). Items in it are deleted on close, as FTB Essentials' trash can; a shift-click into it from the
  player inventory works.
- Lang key for the screen title under `creativecompat`. Tests: SQ rows (delete on close, sneak opens the filter,
  hopper/pipe insertion still deletes, server with the mod absent boots).
- Changelog: family `## Trash cans` or the catch-all: "Item Trash Can opens a 27-slot screen that deletes its
  contents when closed; sneak-click opens the filter".

## 9. P8 Smoothies and brain freeze

Rows: `sweets #8`, `sweets #9`. Design and rulings: [plan_smoothies.md](plan_smoothies.md) sections 1 and 6,
[smoothies.md](smoothies.md) sections 2 and 3.

- #8: 16 smoothies in Creative Delight startup, ids in `item_dict.js`, Mixed Berry with NBT effects, berry color tags,
  `forge:fruits/cherry` gets `vinery:cherry`, placeholder sprite with `sources.csv` rows, fluids / jars / cans / stack
  size / compost per the brief's checklist.
- #9: brain freeze in Creative Common (effects and cold-food tag are Common's): cold-food tag, the roll counter
  window, configurable lengths, powdered snow overlay plus freeze damage, blue action-bar line.
- Builds: Common, Creative Delight. Tests: per plan_smoothies.md; JEED page for brain freeze.
- Changelog: family `## Smoothies`.

## 10. Every phase

- Before a phase: `python3 tools/session_role.py impl`; after it: `tools/todo.py log` and `set status=Testing` on its
  rows, queue rows in `feature_writeup/testing/rows/<CODE>.qb`, writeup and test list updated,
  `tools/status_index.py`, `tools/changelog_lint.py`, commit by path, push to `agent_staging`.
- Writeups: [storage_delight.md](storage_delight.md) (P6), [creative_compat.md](creative_compat.md) (P3, P7),
  [raspberry_pie.md](raspberry_pie.md) (P2), [smoothies.md](smoothies.md) (P8), [publish_readiness.md](publish_readiness.md)
  (P3 to P5: mark the PR ids fixed).
