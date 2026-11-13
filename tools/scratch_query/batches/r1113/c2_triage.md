# r1113 chunk 2 triage

Source: `chunk2.txt` (64 never-run server rows, no SQ-PWG rows in it). Batches in this folder: `c2_a1.qb` (main group-A
boot), `c2_l1.qb` + `c2_l1b.qb` (legacy config boot and its restart, same folder), `c2_l2.qb` (legacy config, CD file
absent), `c2_nobrew.qb` (no Creative Brewing jar). Setup per boot is in each file's header.

| ID | decision | reason |
|---|---|---|
| SQ-PJL-01 | batched in c2_a1.qb | registry counts by `/zc2 reg`; models are client-only, not checked |
| SQ-PJL-02 | batched in c2_a1.qb | id fix fdcooking/ -> farmersdelightcooking/; relog replaced by give + inventory read |
| SQ-PJL-03 | batched in c2_a1.qb | recipe counts by prefix |
| SQ-PJL-04 | batched in c2_a1.qb | Central Kitchen is installed: 10 filling jam jars expected, not 9 |
| SQ-PJL-05 | batched in c2_a1.qb | soft fluid capacities read from the nested Vintage jar (c2_famcheck.py softfluids) |
| SQ-CFS-01 | batched in c2_a1.qb | state and shape by `/zc2 state` |
| SQ-CFS-02 | batched in c2_a1.qb | lit by BurnTime merge, then a coal hopper (CFS-02b) for the real fuel path |
| SQ-CFS-03 | batched in c2_a1.qb | keg temperature judged from its NBT; WTHIT part is client |
| SQ-STN-01 | batched in c2_a1.qb | oven id fix creativeengineering:oven -> creativedelight:oven; jar container fix mason_jar -> empty_mason_jar; oven container paper (muffins) |
| SQ-STN-02 | batched in c2_a1.qb | jar side funnel replaced by `/zc2 cap` side views (same item handler); kettle only read, its slot layout unverified (third-party) |
| SQ-STN-03 | batched in c2_a1.qb | about 3.5 min of waiting; kimchi-from-onion recipe checked first |
| SQ-STN-06 | batched in c2_a1.qb | per-2-tick reads by `/zc2 watch` instead of console reads |
| SQ-STN-07 | batched in c2_a1.qb | overnight oats (bowl container, 1200 ticks) as the recipe with a container |
| SQ-TSY-01 | not batchable | TradeSync sends only to connections with the mod channel list (`isRemotePresent`); a perfBot connection has none, so nothing is built or sent: needs the modded scratch client |
| SQ-TSY-02 | not batchable | same as SQ-TSY-01 (plus the client-log half) |
| SQ-BBQS-01 | batched in c2_a1.qb | kebab sandwich id now shaped/kebab_sandwich_manual_only + sequenced assembly; ingredient count judged from rdetail |
| SQ-BBQS-02 | batched in c2_a1.qb | `/pbot use` hits the face centre (no well): `/zc2 useat` with a well hit point; off hand by `/item replace` |
| SQ-BBQS-03 | batched in c2_a1.qb | sneak by `/zp sneak` |
| SQ-CBW-01 | batched in c2_a1.qb | tag ids creativeengineering: -> creativedelight: |
| SQ-CBW-02 | batched in c2_a1.qb | breaks by `/zact break`; chunk reload not done (forceloaded area) |
| SQ-CBW-03 | batched in c2_a1.qb | |
| SQ-BBQW-01 | batched in c2_a1.qb | |
| SQ-BBQW-02 | batched in c2_a1.qb | skewering by `/zc2 useat`; beef-only skewer may not exist (judged on the BE type lines) |
| SQ-BBQW-03 | batched in c2_a1.qb | trays stack upward (TrayBlock): built by sneak-use placement, base removed under it |
| SQ-CFR-03 | batched in c2_l2.qb | old config state built by c2_legacy_cfg.py l2 (carry code still in CreativeDelight) |
| SQ-CFR-04 | batched in c2_l1.qb | old config built by c2_legacy_cfg.py l1 |
| SQ-CMG-01 | batched in c2_a1.qb | mixin counts now Magic 13 / Equipment 23 common entries (row 7 / 20) |
| SQ-CMG-02 | batched in c2_l1.qb + c2_l1b.qb | no 1.1.08/1.1.09 jar left: hand-built old config; "CE file drops the tables" superseded (Engineering 2.0.0 owns no config) |
| SQ-CMG-03 | batched in c2_a1.qb | |
| SQ-CEQ-02 | batched in c2_a1.qb (part) | bone meal charm and bottled air run; shield block and air brake glide need a real client (r1106 decision) |
| SQ-CEQ-03 | not batchable | stepstool step-up is a client keybind packet; perfBot has no step command |
| SQ-CEQ-04 | batched in c2_a1.qb | |
| SQ-CEQ-05 | batched in c2_a1.qb | plate_block_iron does not exist: aluminum_plate_block (needs_copper_tool) and copper_dust_pile; tag namespace creativeequipment: |
| SQ-CEQ-06 | not batchable | the readerIndex kick is a client-side decode; a perfBot connection decodes nothing |
| SQ-CEQ-08 | batched in c2_a1.qb | attributes by `/zc2 attrs`, judged against tune_tools.js / tune_armor.js |
| SQ-CEN-01 | batched in c2_a1.qb | Common is 1.1.0 (row: 1.0.0); mixin counts now 46 / 17 common entries |
| SQ-CEN-02 | batched in c2_l1.qb + c2_l1b.qb | hand-built old config; end state superseded: [toasts]/[jei]/[shrapnel]/[surface]/[vein_undo] land in Compat's file |
| SQ-CEN-03 | batched in c2_a1.qb (part) | pack order checked; boot-1 registry diff impossible (no 1.1.09 jar): compared with the 11-10 dump instead |
| SQ-CEN-04 | batched in c2_a1.qb (part) | sawmill count 216; boot-1 diff replaced by the 11-10 dump |
| SQ-CEN-05 | batched in c2_l1.qb (part, row SQ-CEN-05s) | shrapnel count 12 after the carry; chest NBT half needs a 1.1.09 save: not batchable |
| SQ-CEN-08 | batched in c2_a1.qb | mount-effect horse part is client |
| SQ-CNS-04 | batched in c2_a1.qb | Curios slots read by `/curios list` with the bot online |
| SQ-CNS-05 | batched in c2_a1.qb | `jcmd JFR.dump` of the running recording (pid by `pgrep -x java` + cwd), then PerfParse |
| SQ-DEP-01 | batched in c2_a1.qb | Blockheads Additions is a CD compartment (M8): coaster mixin in creativedelight.mixins.json |
| SQ-DEP-02 | batched in c2_nobrew.qb | own boot without creativebrewing-*.jar |
| SQ-DEP-03 | batched in c2_a1.qb | |
| SQ-DEP-04 | batched in c2_a1.qb | |
| SQ-DEP-05 | batched in c2_a1.qb | /dumpall grep replaced by `/zq blocktag` of the same tag |
| SQ-DEP-07 | batched in c2_a1.qb | JEI / AE2 tooltip half is client |
| SQ-DEP-08 | batched in c2_a1.qb | repackedStorage -02 is in mods/ |
| SQ-LGM-11 | batched in c2_a1.qb | eat by `/zp eat` |
| SQ-LGM-12 | batched in c2_a1.qb | can recipes exist live although gen_can_types.py AWAIT_DUMP still lists largemeals / roll_delight |
| SQ-LGM-13 | batched in c2_a1.qb | offline check of the 11-10 catalog (c2_famcheck.py families) |
| SQ-LGM-14 | batched in c2_a1.qb | offline (c2_famcheck.py lmplaceable); 13 of 13 placeable today |
| SQ-ROLL-10 | batched in c2_a1.qb | tag ids creativeengineering:food_type -> creativedelight:food_type |
| SQ-ROLL-11 | batched in c2_a1.qb | eaten from 0 (row: 4); full-hunger refusal by `/zq useitem` |
| SQ-ROLL-12 | batched in c2_a1.qb | |
| SQ-BBQ2-01 | batched in c2_a1.qb | last row: its /reload truncates server.log |
| SQ-BBQ2-02 | batched in c2_a1.qb | |
| SQ-BBQ2-03 | batched in c2_a1.qb | base is the 11-10 dump (11-03 dump gone) |
| SQ-BBQ2-04 | batched in c2_a1.qb (part) | tag count; powder vs sauce craft needs a crafting grid (client) |
| SQ-BBQ2-05 | batched in c2_a1.qb | |
| SQ-BBQ2-06 | batched in c2_a1.qb | kills by `/zact kill`; board cut replaced by the cutting recipe read |
| SQ-BBQ2-07 | batched in c2_a1.qb | offline (c2_famcheck.py skewers): 18 of 23 skewer ids in family skewer, 0 skewer cans today |

## Id fixes (current registries: kubejs/exported 11-10, jar sources)

- Recipe ids: `creativedelight:fdcooking/*` -> `creativedelight:farmersdelightcooking/*`; `shapeless/kebab_sandwich` ->
  `shaped/kebab_sandwich_manual_only` (+ `createsequencedassembly/kebab_sandwich_sequenced_assembly`).
- Blocks / items: `creativeengineering:oven` -> `creativedelight:oven`; `vintagedelight:mason_jar` ->
  `vintagedelight:empty_mason_jar`; `creativeengineering:flint_and_terrasteel` -> `creativemagic:flint_and_terrasteel`;
  `creativeengineering:plate_block_iron` (absent) -> `creativeengineering:aluminum_plate_block`; echo sword is
  `create_deep_dark:echo_sword`, echo tools `creativeequipment:`.
- Tags: `creativeengineering:needs_copper_tool`, `stepstools`, `dragon_gear`, `elytra_chestplates`, `bonemeal_charms` ->
  `creativeequipment:`; `counter_toppers`, `hammer_no_rotate`, `food_type/*` -> `creativedelight:`;
  `campfire/rain_douses`, `wthit/needs_silk_touch` -> `creativecommon:`.
- Config: Engineering 2.0.0 registers no config; `[shrapnel]`, `[surface]`, `[vein_undo]`, `[jei]`, `[gui_title]`,
  `[toasts]`, `[tooltips]`, `[coaster]`, `[wthit]` live in `creativecompat-common.toml`.
- Blockheads Additions: a CD compartment since M8; no `blockheads_additions.mixins.json`.

## Probes and helpers (all in tools/scratch_query/)

- New `c2_probe.js` (`/zc2`): `state`, `inv`, `watch`, `useat` (use with a chosen hit point, optional off-hand
  follow-up), `cap` (item handler as seen from a face, simulated extract), `attrs`, `air`, `reg` (registry id count by
  regex).
- New `c2_famcheck.py`: offline reads of the generated food catalogs (`families`, `lmplaceable`, `skewers`) and the
  nested Vintage soft fluids (`softfluids`).
- New `c2_legacy_cfg.py`: builds the pre-split config states (`l1`, `l2`) in a never-booted srv/config.
- Reused unchanged: `zq_probe.js` (prep_srv.sh copies it), `zp_probe.js`, `zact_probe.js`.
