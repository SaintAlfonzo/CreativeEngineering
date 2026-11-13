# r1113 chunk 3 triage (never-run server rows, tools/scratch_query/batches/r1113/chunk3.txt)

81 rows in the chunk: 73 batched (8 of them partly: the part left out is named), 8 not batchable. No SQ-PWG row in
this chunk. Boot order and setup are in each file's header. Every c3 server is built by a script that swaps in the
build/libs jars newer than `mods/` (11-13: Common, Compat, Brewing, Calendar 11-12; Delight 11-11; Engineering,
Devtools 11-10 late); `NOSWAP=1` tests the installed jars.

| ID | decision | reason |
|---|---|---|
| SQ-DCM-01 | batched in c3_a1.qb | boot grep; "18 of 18" now 22 files, checked as N of N |
| SQ-DCM-02 | batched in c3_a1.qb | /reload, KubeJS error count, /dumprecipes count vs the 11-10 pack dump; `[absent mods]` lines now expected (mods dropped since), judged per mod |
| SQ-DCM-03 | not batchable | needs a pre-11-04 save with old ids; no pre-migration Creative jar set is kept (jar_archive, deploy/ hold 11-08+ only) |
| SQ-CBS-02 | not batchable | covered: SQ-CBO-20 (chain + Alex's Mobs only) PASS in r1106cbo; CBS-01 of the same section is superseded by SQ-CBA |
| SQ-CDFO-01 | not batchable | old world from before M8 (11-04) needed; no old jars to make it |
| SQ-CDFO-02 | batched in c3_ch1.qb | chain without CfB, Balm, Incubation (cpp_chain_srv chain) |
| SQ-CDFO-03 | batched in c3_ch2.qb, c3_ch2b.qb (as -03b), c3_ch3.qb (as -03c) | three sets: CfB without Incubation; same with the compartment switch off; Incubation without AM and Duck |
| SQ-PA-01 | batched in c3_a1.qb | boot grep, N of N |
| SQ-PA-02 | batched in c3_a1.qb | path moved to kubejs/data/creativecompat/paintings/minecraft |
| SQ-PA-03 | batched in c3_a2.qb | restart with pa_test.zip and extraSources (header) |
| SQ-PA-04 | batched in c3_a3.qb | restart with [paintings] enabled = false |
| SQ-CMV-01 | batched in c3_a1.qb | boot grep |
| SQ-CMV-02 | batched in c3_co.qb | cc_only_srv.sh alone (SQ-CCO-13 passed the 11-07 jar; this is the newer one) |
| SQ-CMV-03 | batched in c3_co2.qb | Armor Trims copy with mods.toml version 1.4.0 (steps in header) |
| SQ-CDW-01 | batched in c3_cdw1.qb, c3_cdw2.qb (-01b), c3_cdw3.qb (-01c) | one fresh level per removed jar (myrtrees, fruitsdelight, endersdelight) |
| SQ-CDW-02 | batched in c3_b1.qb | all mods present, packs line, jungle rubberwood count |
| SQ-CDW-03 | batched in c3_cdw1-3.qb (-03, -03b, -03c) | skip lines per CDW boot |
| SQ-CDW-04 | not batchable | needs a client to join (world-creation datapack screen) |
| SQ-MIN-01 | batched in c3_ch1.qb | chain + Worldgen stands in for CEPortTesting (its mods/ lacks 8 chain jars); tag id fixed |
| SQ-MIN-02 | not batchable | client: watch rain and read the WTHIT crop-season line |
| SQ-MIN-03 | batched in c3_ch1.qb | boot grep |
| SQ-MIN-04 | not batchable | client: JEI mixing pages; `Found a broken recipe` is JEI's client log |
| SQ-MIN-05 | not batchable | client: `Using missing texture` and looking at blocks |
| SQ-MIN-06 | batched in c3_ch1.qb | boot grep |
| SQ-MIN-07 | batched in c3_b1.qb | full pack: `loot spawn` of the fishing-net table in a river and an ocean (net fishing itself is client) |
| SQ-BPT-01 | batched in c3_a1.qb | boot grep |
| SQ-CBP-11 | batched in c3_b1.qb | transitions, data pack, End highlands felsic census, zact break with netherite pick |
| SQ-ENGM-01 | batched in c3_b1.qb | boot greps (fresh world) |
| SQ-ENGM-02 | batched in c3_b1.qb | ids fixed to creativeworldgen:; Scarlet Mountains is an Overworld biome; census near spawn |
| SQ-ENGM-03 | batched in c3_b1.qb | ids fixed (shrapnel, millable_seeds, wooden bucket) |
| SQ-ENGM-04 | batched in c3_b1.qb (config half) | Ore Excavation undo / pickup needs the client key: left for CQ-ENGM-01 |
| SQ-ENGM-05 | batched in c3_ch1.qb | cpp_chain_srv chain instead of CEPortTesting |
| SQ-PG-01 | batched in c3_a1.qb (SQ-PG-01a), c3_a2.qb (SQ-PG-01b) | bot shoots via c3_probe; old-jar portals part not possible (no pre-11-07 Compat jar) |
| SQ-PG-02 | batched in c3_a1.qb | 3x3 grids through c3_probe `craft` |
| SQ-HSC-89 | batched in c3_b1.qb | row says A, but caves need a normal world: desert box census of raw rock salt |
| SQ-CIF-01 | batched in c3_a3.qb | old configs from git 85104427 put back before the boot; carry checked by cif_carry_check.py |
| SQ-CIF-02 | batched in c3_b1.qb (after half) | old-chest half needs an 11-06 save (no 11-06 build kept) |
| SQ-CIF-03 | batched in c3_b1.qb | bot drinks coffee (zp eat), waits for Phantom Bane to end |
| SQ-CIF-04 | batched in c3_cc1.qb, c3_cc2.qb (-04b) | cpp_chain_srv compat (+ Equipment linked in for -04b) instead of CEPortTesting |
| SQ-IDS-01 | batched in c3_b1.qb | lint (offline: 0 flagged on 11-13) + boot grep; plugin_toggle half is client |
| SQ-IDS-02 | batched in c3_b1.qb | `/cdevgen run food_dict` and the perf suite smoke left out (srv/ has no tools/; never point a generator at the instance) |
| SQ-IDS-03 | not batchable | world saved on 11-06 / 7.19 builds needed; no such jars kept |
| SQ-IP-01 | batched in c3_a1.qb | five frames: flint, dispenser fire charge, lightning, sneak flint, small 1x2 frame |
| SQ-IP-02 | batched in c3_a1.qb | Dark Fire in Overworld, L frame, Nether frame; walking through is CQ-IP-01 |
| SQ-IP-03 | batched in c3_a1.qb | 52 walls (29 glass, 23 panes from the 11-10 tag dump) |
| SQ-IP-04 | batched in c3_a1.qb | logout and dimension change; death-and-respawn case left out (perfBot respawn untested) |
| SQ-IP-05 | batched in c3_a1.qb, c3_a2.qb (SQ-IP-05b) | live toml edits; offline-owner half after the restart |
| SQ-CCB-01 | batched in c3_a1.qb | tag node changed: chainmail (copper had 0 members in the dump) |
| SQ-FCK-01 to -06, -09 to -18 | batched in c3_a1.qb | perfBot uses, setblock, zq / c3 probes; -02 fresh-config half in c3_ch1.qb (SQ-FCK-02b) |
| SQ-FCK-07 | batched in c3_b1.qb + control c3_b2.qb (SQ-FCK-07c) | lichen files removed from the control's kubejs copy |
| SQ-FCK-08 | batched in c3_b1.qb (-08, -08b badlands) + control c3_b2.qb (-08c, -08bc) | 15x15 chunks (25x25 is over the 256-chunk forceload cap) |
| SQ-AZW-01 to -03 | batched in c3_a1.qb | bark id from item_dict |
| SQ-MG-22 to -24 | batched in c3_a1.qb | |
| SQ-BOT-01 to -04 | batched in c3_a1.qb | -02 / -04 read the recipe objects (c3_probe `pot`) |
| SQ-FCB-09 | batched in c3_a1.qb (break half) | place-back, sneak pickup, creative break and the remainder-buffer bottle not driven |
| SQ-FCB-10 | batched in c3_a1.qb | |
| SQ-T1-01 | batched in c3_a1.qb (event-error half) | names (Gelatin, Empty Terrarium) need the client lang dump |
| SQ-T1-03, SQ-T1-04 | batched in c3_a1.qb | |

## Id fixes made in the batches

| Row | Queue text | Batch uses |
|---|---|---|
| SQ-DCM-01 | `creativecommon: 18 of 18 files` | N of N (22 on 11-12) |
| SQ-PA-02 | `kubejs/data/creativeengineering/paintings/minecraft` | `kubejs/data/creativecompat/paintings/minecraft` |
| SQ-MIN-01 | `creativeengineering:seasons/arid` | `creativecalendar:seasons/arid` |
| SQ-ENGM-02 | `creativeengineering:sky_island`, `waystone_ruin` | `creativeworldgen:sky_island`, `creativeworldgen:waystone_ruin_part` (meta structure) |
| SQ-ENGM-03 | `creativeengineering:shrapnel`, `creativeengineering:millable_seeds` | `creativecompat:shrapnel`, `creativedelight:millable_seeds`; wooden bucket `creativedelight:wooden_bucket` |
| SQ-FCK-04 | `creativeengineering:canned_oatmeal` | `creativedelight:canned_oatmeal` |
| SQ-FCK-18 | tag `creativeengineering:crops/harvest_from_bottom` | `creativedelight:crops/harvest_from_bottom` |
| SQ-T1-04 | `creativeengineering:mechanical_crafter` | `create:mechanical_crafter` |
| SQ-CCB-01 | `creativecompat:jei/equipment/tools/copper` | `creativecompat:jei/equipment/chainmail` (copper empty) |
| SQ-CBP-11 | felsic scrap | `creativeequipment:felsic_scrap`; ore `felsic_gear:felsic_block` |
| SQ-AZW-03 | IW azalea bark | `immersive_weathering:quark/azalea_bark` (item_dict `bark_azalea`) |
| SQ-CIF-02/-03 | Phantom Bane | effect `better_beacons:phantom_bane`, potions `creativebrewing:*` |
| SQ-ENGM-02 | Scarlet biome in the Nether | `terralith:scarlet_mountains`, Overworld |

## Probes and tools added

- `tools/scratch_query/c3_probe.js` (new, `/zc3`): `aim`, `shoot` (Portal Gun fired by a bot through
  `PortalGunItem.interact`), `portals` (gun portals with their adjust flag), `craft` (3x3 grid through a CraftingMenu),
  `pot` (Botany Pots growthTicks and display states), `bstate`, `items`, `loot`, `rest`. Not run yet: one live call each
  before relying on it (`aim`/`shoot` are the risky ones: raytrace from the bot's eye).
- `tools/scratch_query/cif_carry_check.py` (new): SQ-CIF-01 value check of the carried config sections (sanity-run on
  the pack's files: 0 differing).
- Used unchanged: `zq_probe.js`, `zp_probe.js` (`sneak`, `eat`, `effects`, `inv`), `zact_probe.js` (`break`).
