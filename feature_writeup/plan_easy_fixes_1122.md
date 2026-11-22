# Easy fixes plan 2026-11-22 (Tier 1 and Tier 2 bug and correctness rows)

Owner brief 2026-11-22: execute the easiest bugfix and correctness rows of the TODO list (Tier 1: cause known, one file;
Tier 2: small, suspect named, one check first), with guards where a fix touches an optional mod. Rows were picked from the
16 `Bug` rows and the Planned / In Progress rows that read as fixes; the hard ones (portal crashes, flower crown JEI
freeze, MCA sleep, clock icon, campfire rain, jar pump, knife on cutting board, superflat mob cap, Power Grid arm,
Farmer's Structures donor, Worldgen carve boot errors) stay out.
Code facts below were read 2026-11-22 (paths and lines may drift).

Short names: dict = `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js`;
NCH = `neoCraftHelper.js` (Creative Common); IPO = Immersive Posts.

Rebuild map: A = instance only (`resourcepacks/`, `configs_edited/`, no jar); C = Creative Common; V = Creative Devtools;
W = Creative Worldgen; E = Creative Engineering; G = Creative Brewing; D = Creative Delight (only if 2.5 needs a data fix).
One jar at a time; `installToMods` handles a running game (CLAUDE.md). Every built item gets its changelog bullet (or the
reason for none), writeup line and queue rows in the same session.

## 0. Decisions (owner, 2026-11-22)

<!-- generated:status_index begin open_work sha1=fb836845e370 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 11 open TODO rows (Bug 2, Planned 7, In Progress 1, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `generators-and-tests #28` | Bug | CCO-12 | creativedevtools sequence_assembly_processes.js line 58 logs 'Failed to create custom JSON recipe' |
| `mod-updates #3` | Bug | SQ-SPFX-06 | Item dict still lists the dropped legendary and mythical roll medley cans |
| `alcohol/vineryport #24` | Planned | S | Ripe stem shearing may duplicate seeds |
| `blocks/framed-and-decor #2` | Planned | C | Copycat resource pack keeps the framed block name |
| `engineering/sawmill-and-components #3` | Planned | C P | JEI doesn't show the sawmill blade |
| `farming/wild-crops #9` | Planned | S | Wild carrots and adzuki missing from forest boxes |
| `performance-and-server #2` | Planned | S | Immersive Posts: iron fence on a post base no longer grows the post |
| `ui/tooltips #16` | Planned | C | Tooltip language: spyglass and bell lines |
| `worldgen/biomes #15` | Planned | S P | Broken bone and bone chunk: pressing, milling and crushing |
| `dishes/ingredients #28` | In Progress | S P | Sliced potato tag |
| `blockrunner #18` | Testing | SQ-CC-03 SQ-CC12-02 SQ-CPP-05 | Sand and red sand layer recipes error when Immersive Weathering is absent |
<!-- generated:status_index end open_work -->

| # | Row | Ruling |
|---|---|---|
| D1 | `ui/tooltips #16` | Spyglass and bell lines are loading-screen tips, not item tooltips. Spyglass typo fixed: "Spyglasses, made from amethyst and copper, can be used to zoom in on distant things". Bell: "Ring a bell, by hand or with a projectile, to send villagers indoors and make nearby raiders glow". |
| D2 | `worldgen/biomes #15` | Milling and crushing: broken bone -> 2 bone meal, bone chunk -> 1 bone meal; crushing adds 25 % one extra bone meal. Pressing: a vanilla bone -> 1 broken bone. |
| D3 | `alcohol/vineryport #24` | Shearing a ripe stem drops grapes and seeds and resets the stem to age 0 (upstream 1.3.10). |
| D4 | `blocks/framed-and-decor #2` | Keep the Copycat Framed Block pack for its textures; every framed block keeps its Framed Blocks name. |
| D5 | `engineering/sawmill-and-components #3` | A JEI info page per sawmill blade (which machine takes it, what it cuts), no tooltip line. |

## 1. Tier 1 (cause known, one file)

### 1.1 `blockrunner #18` Sand layer recipes without Immersive Weathering (no build)
Already built and committed (84376945, 2026-11-22): `kubejs/data/creativecompat/kubejs/other/misc_mods.js` 54-62 sit inside
`Platform.isLoaded('immersive_weathering')`. Left: `todo.py log` the commit, rows to Testing, rerun SQ-CC-03, SQ-CC12-02,
SQ-CPP-05. No changelog bullet (the error only showed on reduced mod sets, never in the pack).

### 1.2 `mod-updates #3` Stale roll medley cans in the item dict (C)
Delete dict lines 12777-12778 (`canned_legendary_roll_medley`, `canned_mythical_roll_medley`). They sit outside the
`BEGIN/END GENERATED CANS` block (13299-13606), so `tools/gen_can_types.py` does not write them; grep the repo for both keys
first and drop any other reader. Check: `check_all.py --fast --changed`; boot has no `[item_dict] Missing/invalid` WARN
(SQ-SPFX-06). Bundle with 1.5 into one Common rebuild. No changelog bullet (log line only, no player-visible change).
Changed from the plan (2026-11-22): two more readers, `kubejs/generation_scripts/food/food_types.json` (`raw/veggie`) and its
generated `creativedelight/food_types.js`, lost both keys (`food_catalog.py --members`), so Creative Delight was rebuilt with
Common; 1.5 needed no rebuild, so this Common rebuild holds 1.2 only. Dump-derived files (food_families, food_dict_index,
the Raspberry Pie diversity data) clear on the next `/dumpall` + `/cdevgen`; FCK.qb 255-258 still name the two cans.

### 1.3 `dishes/ingredients #28` Sliced potato tag (no build)
Nothing to build (tags full since 2026-11-07). Close the row once the client check CQ-FF-04 passes; no other work.

### 1.4 `generators-and-tests #28` Devtools sequenced assembly test recipe errors (V)
`kubejs/data/creativedevtools/kubejs/create/sequence_assembly_processes.js` 58-92. r1121b CCO-12a/e logs: the CCO-12 mod
sets (`tools/scratch_query/modsets/cco_sets.toml`) carry neither createaddition nor vintageimprovements, and Create's
builders fail (`create:pressing with 2 arguments not found`). Guards:
- the whole recipe returns early unless `global.has('create', 'kubejs_create')` (the same test NCH's Create helpers use);
- build the step array in parts: the Create steps always, the `createaddition:rolling` step only under
  `global.has('createaddition')`, the eight vintageimprovements steps only under `global.has('vintageimprovements')`;
- the step array is then passed to `R.createSequencedAssembly` as today (no change to the recipe id).
Check: full pack still makes the nether star sequence with all 13 steps (JEI or `/kubejs` recipe dump); CCO-12a and -12e
logs have no `sequence_assembly_processes.js#58` ERROR. Dev-only: no changelog bullet.
Changed from the plan (2026-11-22): the vintageimprovements block is 10 steps (viPolishing plus 9 `e.custom`), all guarded;
the full chain is 15 steps (4 + 1 + 10). SQ-CCO-12a to -12f gained `kjsreject sequence_assembly_processes\.js#\d+: Failed`.

### 1.5 `ui/tooltips #16` Spyglass and bell loading tips (C)
Add the two D1 lines to `development/creativeCommon/src/main/resources/resourcepacks/loading_tips/assets/loading_tips/loading_tips/tips.json`
in the file's existing format and order rules ([loading_tips.md](loading_tips.md)); add both to
[loading_tips_audit.md](loading_tips_audit.md) if it lists tips one by one. No guard (vanilla items). Changelog: one bullet
under the loading tips family. Same Common rebuild as 1.2.
Changed from the plan (2026-11-22): the tips shown in game are the Creative Tips override
`kubejs/assets/creative_tips/loading_tips/tips.json` (left the jar 2026-11-07); the two merged tips there
(`spyglasses_zoom_in_on_the_horizon`, `bells_alert_villagers_to_raids`) took the D1 texts, F3+T, no rebuild. The audit
table (loading_tips_audit.md 2.1) and `tools/loading_tips_audit_1114/audit_decisions.py` match. Bullet: the bell tip fix.

### 1.6 `blocks/framed-and-decor #2` Copycat pack keeps Framed Blocks names (A)
`resourcepacks/Copycat Framed Block.zip` holds `assets/framedblocks/lang/en_us.json` (309 keys; it renames 128 names to
Copycat, 181 still say Framed). The instance already uses an edited copy (`configs_edited/resourcepacks/Copycat Framed
Block.zip` = the live zip, `original/` = upstream). Rewrite the zip with Python `zipfile` minus that one lang file (textures,
models and `pack.mcmeta` byte-identical), then copy it to `configs_edited/resourcepacks/`. Names then fall back to the
Framed Blocks jar and our own overrides. Note it in [deploy.md](deploy.md) if the pack ships in the deploy. Changelog: one
bullet (names under the optional Copycat pack stay Framed Blocks names), under the catch-all heading.

## 2. Tier 2 (suspect named, one check first)

Each item stops after one session of diagnosis if the suspect is wrong; the finding then goes on the row and the item
leaves this plan.

### 2.1 `performance-and-server #2` IPO fence on a post base grows no post (E)
SQ-IPO-02 passed 2026-10-27 and failed 2026-11-20 (fence use returns SUCCESS, fence not consumed, no post). The material cut
(`pack/server #21`) is `compat/ImmersivePostsMaterials.java` plus `ImmersivePostsValidItemMixin`,
`ImmersivePostsRegistrationMixin`, `TagLoaderImmersivePostsMixin` (Engineering `mixin/`). Since 2026-10-26 they changed in
85104427 (Creative split), 5c9489ad and 5cdc53fe (indent). Steps:
1. Check r1120lr's mod set and SQ-IPO-02 log (`perf/runs/r1120lr/s09_res/rows/SQ-IPO-02.log`): which fence item, which
   material, whether IE was loaded.
2. Diff the four files from the last commit before 2026-10-27 to HEAD (`git log --before=2026-10-27 -1`); look for the
   `@Redirect` on `m_6227_` returning true where upstream's own branch then finds no material, and for `kept()` order
   changing post block registration.
3. Fix in the mixin; guard stays `IPostMaterial`-based (IPO is a hard dependency of the mixin targets, the mixin config
   already skips when IPO is absent: confirm).
Check: SQ-IPO-02 rerun. Changelog: fix bullet under the Immersive Posts family if one exists, else the catch-all.
Changed from the plan (2026-11-22): the suspect was wrong. The four files are unchanged since the passing 463d2342 (git
diff -w: one comment word). `cdevtest use` put its fake player on top of the clicked block, and IPO's `PostBaseBlock.use`
skips the post when the player stands in the target block. Fix in the test harness: `cdevtest use ... stand=x,y,z`
(Devtools, built with 1.4) and SQ-IPO-02 stands off the post column. No product change, no changelog bullet.

### 2.2 `alcohol/vineryport #24` Shearing a ripe stem (G)
`development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/core/block/PaleStemBlock.java` 86-98:
`withAge(state, Math.max(0, age - 1), ...)` -> `withAge(state, 0, ...)` (D3); the `AGE == 0` branch already clears
`LEAVES_PENDING`. Check the legacy `LegacyLatticeStemBlock` for the same pattern and match it. Update the shearing line of
[jei_world_tabs.md](jei_world_tabs.md) 3 and any JEI world-tab text that says "one stage". Check: rows to write (shear a
ripe stem once: grapes + seeds, age 0; shear again: nothing). Changelog: fix bullet under the Vinery family.

### 2.3 `worldgen/biomes #15` Broken bone and bone chunk recipes (W)
New block in `kubejs/data/creativeworldgen/kubejs/recipes/visceral_heap.js` (or a sibling file), through NCH only:
`createMilling`, `createCrushing` (25 % extra bone meal), `ieCrushing` (secondary 25 %), `createPressing(i.broken_bone,
i.bone)` (D2), and `ieMetalPress` only if the pack already presses bones there (check; else Create only). Guards: the block
returns unless `global.has('philipsruins')`; the NCH helpers already skip when Create or IE is absent (`global.has` at the
top of each). Ids `i.broken_bone`, `i.bone_chunk`, `i.bonemeal`, `i.bone` are in the dict. Check for an existing bone
pressing recipe that would conflict with `createPressing(i.broken_bone, i.bone)` (JEI uses on bone). Writeup:
[visceral_heap.md](visceral_heap.md) 6. Changelog: bullet under the Visceral Heap family.

### 2.4 `engineering/sawmill-and-components #3` Sawmill blade JEI info pages (E)
New client script `kubejs/assets/creativeengineering/kubejs/engineering/jei_info.js` headed `// requires: jei` and
`// requires: immersiveengineering`, modeled on `kubejs/assets/creativedelight/kubejs/fungidelight/jei_info.js`
(`JEIEvents.information`, `global.item` keys). One page per blade the sawmill recipes name in their `"blade"` field
(`i.sawblade`, `i.precision_sawblade`, and any other: read the distinct values from the NCH `sawmillRaw` data first), each
saying the sawmill takes it and which output families it cuts. Confirm the Engineering jar copies `kubejs/assets/creativeengineering/`
(as CD does for its tree); if not, put the file where Engineering's client scripts live. Writeup: [engineering.md](engineering.md)
F2 and EN-04. Changelog: bullet under the Sawmill family.
Changed from the plan (2026-11-22): the Engineering jar did not copy `kubejs/assets/creativeengineering/kubejs/`;
`build.gradle` scriptTrees (and the jar check) now include it. Two blades only: the plain sawblade (logs, wood, slabs) and the
precision sawblade with 12 NBT Profiles; JEI has no subtype interpreter for it, so one page lists all 12. Test CQ-EN-26;
bullet under Engineering Recipes (no Sawmill family).

### 2.5 `farming/wild-crops #9` No wild carrots or adzuki in forest boxes (D if data)
`kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json`: carrot `where: temperate grassy !swamp` with
`farmersdelight:patch_wild_carrots`; adzuki `temperate grassy forest` with `neapolitan:patch_adzuki_sprouts`. Check first,
without building: the `grassy` / `temperate` traits of the three r1115 forest biomes in
`kubejs/generation_scripts/worldgen/biome_traits.json`, then both placed features' rarity (`kubejs/data/neapolitan/worldgen/`,
the FD jar and FD's config chance). If a trait is missing, fix the trait; if rarity alone explains zero in about 740 chunks,
log the numbers on the row and ask the owner before changing rarity. Changelog only if worldgen changes.
Result (2026-11-22): traits fine; FD wild carrots rarity 120 (config) on the motion-blocking heightmap, Neapolitan adzuki
rarity 128 on world surface and grass only: about 4 and 1.8 attempts in the counted boxes, most landing on forest canopy.
Item left the plan; numbers and the owner question are on the row.

## 3. Order and commits

1. A: 1.1 (log only), 1.6 (zip + backup). Commit.
2. C: 1.2 + 1.5, one Common rebuild and install. Commit.
3. V: 1.4. Commit.
4. G: 2.2. Commit.
5. W: 2.3. Commit.
6. E: 2.1 + 2.4, one Engineering rebuild and install. Commit.
7. 2.5 check; D only if a trait fix is needed.
8. Docs: rows logged (`todo.py log`, status Testing), queue rows written for 1.4, 1.5, 1.6, 2.2, 2.3, 2.4 (prefixes from
   [test_prefix_index.md](testing/test_prefix_index.md)), `features.csv` jars, `status_index.py`, `changelog_lint.py`.
   Generated output in its own commit, push to `agent_staging`.

## 4. Tests to run after the build

Existing: SQ-CC-03, SQ-CC12-02, SQ-CPP-05 (1.1), SQ-SPFX-06 (1.2), CQ-FF-04 (1.3), SQ-CCO-12a to -12f (1.4), SQ-IPO-02 (2.1).
New rows (written in step 8): loading tips shown (client), Copycat pack names (client), vine shear reset, bone recipes in
JEI and in a Create mill, sawmill blade info pages (client).
