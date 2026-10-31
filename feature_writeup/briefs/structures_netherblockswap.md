# Brief: structures / netherblockswap

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# structures`, Sub `netherblockswap`. Feature
writeups: [nether_worldgen.md](../nether_worldgen.md) (decision sheet, prefix `NW-`), [nether_swaps.md](../nether_swaps.md)
(generated: every swap in place), [meat_blocks.md](../meat_blocks.md) (generated). Java paths are relative to
`development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #2 crying obsidian (K19) | Planned | decide, then one swap rule | together with #3 and #4: **one** rules edit, **one** generator run, one test pass |
| #3 soul lanterns, soul wall torches (K25, K32) | Planned | decide, then two rules (plus property carry-over in the generator) | same batch |
| #4 crimson / warped wood pieces (K28-K31, K33, K35, K36) | Planned | decide per block, seven rules | same batch |
| #1, #5 | Testing | in-game NW-06/07/10/11, ST-149 | listed at the end |

The three Planned rows are the same job: new rules in `nether_swap_rules.json`, a fix in `tools/gen_nether_swaps.py`
so the new targets keep their block-state properties (**required first**, see "Shared build"), one run, the scan
rerun, and the decision cells in nether_worldgen.md filled. The user's answer for each K row is the only input; the
recommendations below are ready to apply if the user says "as recommended".

Related rows: `structures/contents #1` (moving the four `plan/move_to_nether` structures; their soul lanterns,
crying obsidian and crimson trapdoors are exempt from every rule until then) and `worldgen/biomes/netherblocks #2`
(the frostfire caves' soul lanterns are a biome feature, not a structure: that row, not this one).

---

## Shared build (all three Planned rows)

### Now

- **Rules** `kubejs/generation_scripts/worldgen/nether_swap_rules.json`: 22 rules, first match per block wins. Rule
  shape: `{note, structures: [ids] | ["*"], except?: [ids], blocks: [ids | "@nether" | "@nether_bricks"], to: id |
  "air" | "deepslate"}`. Every rule also skips the `plan/move_to_nether` tag (structure_tags.json:1938).
- **Generator** `tools/gen_nether_swaps.py` expands rules over every template a live Overworld structure reaches
  (`terrain_adaptation_scan.reach`), writes `nether_block_swaps.json` (1084 swaps in 73 templates now) and
  `feature_writeup/nether_swaps.md`. A template any Nether or End structure also reaches is never swapped (`other_dim`,
  :95-118, :147): this is why the Mysticrift End tower (16 crying obsidian, 10 soul lanterns), Philip's End gate
  fortress (150 crying obsidian), the piglin camp and the Nether ship need no `except`.
- **Property carry-over is a whitelist**: `target_state` (:53-69) keeps only the properties listed in `KEEP` (:43-50)
  for that target id. `KEEP` has the deepslate brick slab/stairs/wall, basalt, campfire. **Every new target in this
  brief is missing**: without a KEEP entry a hanging soul lantern becomes a standing lantern, trapdoors lose
  facing/half/open, buttons fall back to floor-facing north, a wall torch faces north. The rules readme ("stairs, slab,
  wall and axis properties carry over") overstates what the code does.
- **Runtime** `worldgen/TemplateBlockSwaps.java`: reads `meat_block_swaps.json` then `nether_block_swaps.json`
  (`CONFIGS`, :47-48) at `ServerAboutToStartEvent`; `apply` (:130-159) swaps a palette entry only where the template
  still has `from` at `pos` (else a "no longer hold" warning). Mixin `mixin/StructureTemplateManagerSwapMixin.java`
  (`StructureTemplateManager#get`, `m_230407_`, RETURN). Switch `[worldgen.systems] templateBlockSwaps`
  (CEConfig.java:1784-1787; its comment still names only meat_block_swaps.json, worth a one-line fix in the next
  patcherMod build). Server restart; new chunks only; no client change.
- **Not run by /cegen**: `gen_nether_swaps.py` (and `meat_block_scan.py`) are not in `tools/generators.json`. The
  generator needs a full instance (`mods/*.jar` + the vanilla -extra jar, `gen_loot_config.MODS_DIR`); it can't run in
  a cloud checkout without the jars.
- **Processor blind spot**: blocks a structure processor places are not in the templates, so swaps can't see them
  (nether_worldgen.md 4). Checked for this brief: YUNG's Better Dungeons runs `betterdungeons:nether_block_processor`
  in its zombie and small dungeon processor lists; with `"Enable Nether Blocks in Dungeons" = false`
  (`config/betterdungeons-forge-1_19.toml:19`, now `true`) it turns soul lanterns into lanterns (hanging kept), soul
  campfires into campfires and soul sand / soul soil into a plain block (decompiled `NetherBlockProcessor`; the target
  is SRG `Blocks.f_50546_`, unmapped, most likely coarse dirt). Verified against `YungsBetterDungeons-1.19.2-Forge-3.2.2.jar`.

### Build

1. **Generator: carry properties by family** (`tools/gen_nether_swaps.py`). Replace the exact-id lookup in
   `target_state` with a fallback by id suffix, so a new target never silently drops its state:
   ```python
   FAMILY_KEEP = [  # (suffix, kept properties), first match; KEEP stays for exact ids
       ('_trapdoor', ['facing', 'half', 'open', 'powered', 'waterlogged']),
       ('_button', ['face', 'facing', 'powered']),
       ('_pressure_plate', ['powered']),
       ('_slab', ['type', 'waterlogged']),
       ('_stairs', ['facing', 'half', 'shape', 'waterlogged']),
       ('_wall', ['east', 'north', 'south', 'west', 'up', 'waterlogged']),
       ('wall_torch', ['facing']),
       ('lantern', ['hanging', 'waterlogged']),
       ('_log', ['axis']), ('_wood', ['axis']), ('_hyphae', ['axis']), ('_stem', ['axis']),
   ]
   def keep_for(name):
       if name in KEEP: return KEEP[name]
       return next((k for suf, k in FAMILY_KEEP if name.endswith(suf)), [])
   ```
   (`minecraft:wall_torch` ends in `wall_torch`, Quark's `azalea_planks_stairs` in `_stairs`.) Rerun the generator
   once **before** adding rules and diff `nether_block_swaps.json`: the existing 1084 swaps must come out identical.
2. **Rules**: append the rules of the rows below in this order, after rule 22 (none of their blocks is matched by an
   earlier rule; `@nether` is only used by the Mysticrift mangrove tower rule 14, which runs first and keeps its
   gold-ore choice). Update `_readme.rules` to describe the family carry-over.
3. Run `python3 tools/gen_nether_swaps.py` in the instance. Expected: 1084 + about 27 (K19) + 441 + 8 (K25, K32, minus
   any kept ancient-city lanterns) + 107 (K28 to K36) swaps. Check the printed "left alone" line for templates shared
   with Nether/End structures.
4. `python3 tools/nether_worldgen_scan.py` (about a minute): section 3 of nether_worldgen_scan.md must lose those
   blocks. Then in nether_worldgen.md: fill the Decision cells (K19, K25, K28 to K33, K35, K36), add rows to the section
   3 "Built" table, drop them from "Still open" in section 0, add the NW tests below.
5. Optional, small: add a `nether_swaps` entry to `tools/generators.json` after `structure_groups` (it reads
   `pool_config.json` and the structure tags), outputs `kubejs/generation_scripts/worldgen/nether_block_swaps.json` and
   `feature_writeup/nether_swaps.md`, apply "restart the server; new chunks only". Then `/cegen run nether_swaps` works.
6. Ship: data only (`kubejs/generation_scripts/worldgen/`), server restart. Goes out with the next pack version; no
   patcherMod build unless the CEConfig comment fix rides along.

### Gotchas

- New chunks only. Swapped templates are cached per server run (`DONE`); a restart re-reads the file.
- Waterlogged or hanging states are only right if step 1 is done; check a few entries in the JSON by hand
  (`"to": {"Name": "minecraft:lantern", "Properties": {"hanging": "true", ...}}`).
- A mod update that moves template blocks shows up as "no longer hold" warnings in the log: rerun the generator.

---

## structures/netherblockswap #2: Crying obsidian (K19)

Status Planned · Work data · TODO `worldgen / structures/netherblockswap #2`

### Goal

No crying obsidian in Overworld structures, unless the user keeps it.

### Now

27 blocks in 3 live structures (template counts verified against the jars and `kubejs/data` for this brief):

| Structure | Template | Blocks |
|---|---|---:|
| `betterstrongholds:stronghold` | `betterstrongholds:treasure/portal/treasure_portal_1`, `_3`, `_5` | 5, 3, 2 |
| `mysticrift_pillagers_towers_aris:jungle_pillager_tower` | `mysticrift_pillagers_towers_aris:jungle_pillager_tower` (kubejs/data port) | 9 |
| `oldvillagerstatue:old_villager_statue` | `oldvillagerstatue:oldvillagerstatue` (kubejs/data port) | 8 |

Exempt or skipped: Philip's `rare_ruin` 3 and `start_nether_ruin` 7 (`plan/move_to_nether`), the End tower and End
gate fortress (End-shared). In play, crying obsidian otherwise comes from Create haunting obsidian
(`kubejs/server_scripts/engineering/resource_blocks.js:156`, `:393`), which needs soul fire, i.e. Nether soul sand.

### Build

One rule (after the shared step 1):
```json
{"note": "Crying obsidian to obsidian (K19): stronghold portal treasure rooms, jungle pillager tower, villager statue",
 "structures": ["*"], "blocks": ["minecraft:crying_obsidian"], "to": "minecraft:obsidian"}
```
If the user keeps the stronghold's (portal rooms look right with it), add `"except": ["betterstrongholds:stronghold"]`.

### Ids and data

`minecraft:crying_obsidian` -> `minecraft:obsidian` (no properties). Template ids verified (jars:
`YungsBetterStrongholds-1.19.2-Forge-3.2.0.jar`; ports in `kubejs/data/mysticrift_pillagers_towers_aris/structures/`,
`kubejs/data/oldvillagerstatue/structures/`).

### Decisions

- **Obsidian everywhere, or keep the stronghold's?** Recommendation: obsidian everywhere. It is 27 blocks, it keeps
  the respawn-anchor ingredient a Nether (or haunting) item, and it matches the K rows already decided.

### Tests

- **NW-12 Crying obsidian:** `/locate structure betterstrongholds:stronghold`, find a portal treasure room (or
  `/ceplace` the `treasure_portal_1` template) -> obsidian, no crying obsidian; same for a jungle pillager tower and an
  old villager statue.

---

## structures/netherblockswap #3: Soul lanterns and soul wall torches (K25, K32)

Status Testing (built 2026-10-30; SQ-WG30-B01 to B04 PASSED 2026-10-30) · Work data · TODO `worldgen / structures/netherblockswap #3`

### Goal

Ordinary lanterns and wall torches where Overworld structures hang soul lanterns or mount soul torches, unless the
user keeps them somewhere on purpose.

### Now

- **Soul lanterns: 441 in 22 structures** (nether_worldgen.md K25, S rows). Biggest: `terralith:spire` 221
  (`terralith:spire/layer1` 73, `layer2` 39, `layer3` 47, `layer4` 38, `base_l` 14, `base_r` 10), `minecraft:ancient_city`
  128 (vanilla templates, not checked here), `terralith:underground/frosted_dungeon` 20 (`terralith:underground/dungeon1`;
  its soul fire is already put out by rule 6), Philip's underground ruins 17 (`deep_ruin2/3/5/6/8`, `deep_ruinbig1`),
  `minecraft:pillager_outpost` 12 (IPO's `ipo:pillager_outpost/tower/top/overgrown*`, 3 each), Simply Houses taiga
  inn 9 / taiga house 7 / forge 4, the witch wagon 4, and 1 to 3 in about a dozen more. All are `[hanging, waterlogged]`
  states.
- **Soul wall torches: 8 in 2 structures**: `betterjungletemples:jungle_temple` 7
  (`challenge_room/challenge_room_water_parkour` 2, `floor_2/room/floor_2_parkour_room_0..4` 1 each) and
  `terralith:spire` 1 (`spire/base_l`). YUNG's stronghold also has a `hall_doorways/hall_doorway_2torch_soul` template
  (2 soul wall torches) that the scan does not count as live; the `"*"` rule catches it if the pool walk reaches it.
- **Processor-placed lanterns**: YUNG's dungeons (see Shared build). The zombie dungeon's one template lantern
  (`betterdungeons:zombie_dungeon/floor1/room/floor1_specialroom_0`) is caught by a swap rule anyway.
- Soul torches as items stay (soul torch arrows, `kubejs/server_scripts/equipment/archery.js:108`): this row is
  worldgen only.

### Build

Two rules (after the shared step 1, which is what keeps `hanging` and `facing`):
```json
{"note": "Soul lanterns to lanterns (K25); the ancient city keeps its own (with its kept soul sand)",
 "structures": ["*"], "except": ["minecraft:ancient_city"],
 "blocks": ["minecraft:soul_lantern"], "to": "minecraft:lantern"},
{"note": "Soul wall torches to wall torches (K32): YUNG's jungle temple parkour rooms, the spire",
 "structures": ["*"], "blocks": ["minecraft:soul_wall_torch", "minecraft:soul_torch"], "to": "minecraft:wall_torch"}
```
`soul_torch` (standing) has no live occurrence; if one appears, give it its own rule to `minecraft:torch` instead (a
standing torch must not become a wall torch). Simplest: list only `minecraft:soul_wall_torch` in the second rule.

Also set `"Enable Nether Blocks in Dungeons" = false` in `config/betterdungeons-forge-1_19.toml:19` (no
`defaultconfigs/` copy exists): it covers processor-placed soul lanterns, soul campfires and soul sand in every
YUNG dungeon, including the ones the template scan can't see. Common config: server and clients get it with the pack.

### Ids and data

`minecraft:soul_lantern` -> `minecraft:lantern` (`hanging`, `waterlogged`); `minecraft:soul_wall_torch` ->
`minecraft:wall_torch` (`facing`). Template lists above verified against `Terralith_1.19.3_v2.3.12.jar`,
`YungsBetterJungleTemples-1.19.2-Forge-1.0.1.jar`, `YungsBetterStrongholds-1.19.2-Forge-3.2.0.jar`,
`PhilipsRuins1.19.2-10.8.jar`, `SimplyHouses-1.1.3-1.19-forge.jar`, `ImprovedPillagerOutpost-v5.jar` and the
`kubejs/data` ports. The ancient city counts come from the scan (vanilla jar not checked).

### Decisions

- **Lantern everywhere, or keep some?** Recommendation: lanterns everywhere except the ancient city (128). The city
  keeps its soul sand and (now plain) fire on purpose (nether_worldgen.md K1, K27); its blue light is part of the deep
  dark look. If the user wants it gone too, drop the `except`.
- **The spire's 221.** The spire is a dark, lantern-heavy build; plain lanterns change its look the most. Recommendation:
  swap (it is Overworld, and it is the biggest single source), but look at one in game (NW-13) before shipping.
- **YUNG's config switch**: recommend yes (catches the processor blind spot for free).

### Gotchas

- Without the generator fix every hanging lantern becomes a floor lantern: under a ceiling it floats, on a chain it
  sits on top of the chain's end.
- A lantern gives light 15, a soul lantern 10: spawn-proofing in those rooms changes slightly (fewer mobs in the spire
  and the frosted dungeon).

### Tests

- **NW-13 Spire lanterns:** `/locate structure terralith:spire` -> hanging lanterns (not soul lanterns), still hanging
  from their chains; the one wall torch at the base is a normal torch on the wall.
- **NW-14 Jungle temple torches:** `/locate structure betterjungletemples:jungle_temple`, the water parkour and floor 2
  parkour rooms -> normal wall torches, facing the right way.
- **NW-15 Ancient city lanterns kept:** an ancient city still has soul lanterns (only if the `except` stays).
- **NW-16 YUNG dungeons:** with the config switch off, a few zombie / small dungeons (`/locate structure
  betterdungeons:zombie_dungeon`) -> no soul lantern, soul campfire or soul sand.

---

## structures/netherblockswap #4: Crimson and warped wood pieces (K28 to K31, K33, K35, K36)

Status Planned · Work data · TODO `worldgen / structures/netherblockswap #4`

### Goal

Overworld woods for the few crimson and warped pieces left in Overworld structures.

### Now (counts verified per template)

| K | Block | Template (structure) | Blocks | Properties |
|---|---|---|---:|---|
| K28 | `crimson_trapdoor` | `terralith:village/desert/houses/cartographer2` (fortified desert village) 36; `betterwitchhuts:witch_hut_sm` 2; `simply_houses:taiga_forge/taiga_forge` 1 | 39 | facing, half, open, powered, waterlogged |
| K29 | `crimson_button` | `betterstrongholds:starts/junction_lg` 20, `rooms/mess` 2, `rooms/junction_md` 2; `betterdeserttemples:statues/statue_2` 2; `kaisyn:outpost/forts/jungle/base_plate` (T&T jungle outpost) 2 | 28 | face, facing, powered |
| K30 | `warped_pressure_plate` | `betterdeserttemples:puzzle_big/puzzle_big_4_0` to `_4_5`, 4 each | 24 | powered |
| K31 | `warped_trapdoor` | `terralith:spire/base_l` | 8 | facing, half, open, powered, waterlogged |
| K33 | `crimson_slab` | `betterdeserttemples:main_entrance_doorway/main_entrance_doorway_0` | 6 | type, waterlogged |
| K35 | `warped_stairs` | `betterdungeons:zombie_dungeon/zombie_start` | 1 | facing, half, shape, waterlogged |
| K36 | `stripped_warped_hyphae` | `philipsruins:ocean_ruin6` (Philip's ocean ruins) | 1 | axis |

Exempt / skipped: Philip's rare ruin (2 crimson trapdoors, move to Nether), the Mysticrift End tower (4), the piglin
camp (`eugene:piglin_camp`, Nether), the Nether ship pieces of `unwrecked_ships:ship` (Nether-shared).

**Fire check done for this brief:** none of these pieces is within 3 blocks of fire, lava, magma or a campfire in its
template (the desert temple puzzle rooms have 2 campfires each, further away; the fortified desert village's magma
and fires are in `armorsmith1`, not in `cartographer2`). Crimson and warped don't burn, Overworld woods do, so this
matters; it is clear.

### Build

Seven rules (after the shared step 1). With the recommended targets:
```json
{"note": "Crimson trapdoors to mangrove (K28)", "structures": ["*"], "blocks": ["minecraft:crimson_trapdoor"], "to": "minecraft:mangrove_trapdoor"},
{"note": "Crimson buttons to mangrove (K29)", "structures": ["*"], "blocks": ["minecraft:crimson_button"], "to": "minecraft:mangrove_button"},
{"note": "Warped pressure plates to dark oak (K30, desert temple puzzle)", "structures": ["*"], "blocks": ["minecraft:warped_pressure_plate"], "to": "minecraft:dark_oak_pressure_plate"},
{"note": "Warped trapdoors to dark oak (K31, spire)", "structures": ["*"], "blocks": ["minecraft:warped_trapdoor"], "to": "minecraft:dark_oak_trapdoor"},
{"note": "Crimson slabs to mangrove (K33, desert temple doorway)", "structures": ["*"], "blocks": ["minecraft:crimson_slab"], "to": "minecraft:mangrove_slab"},
{"note": "Warped stairs to dark oak (K35, zombie dungeon start)", "structures": ["*"], "blocks": ["minecraft:warped_stairs"], "to": "minecraft:dark_oak_stairs"},
{"note": "Stripped warped hyphae to stripped dark oak wood (K36, ocean ruin)", "structures": ["*"], "blocks": ["minecraft:stripped_warped_hyphae"], "to": "minecraft:stripped_dark_oak_wood"}
```
A generator extension that takes a map (`"to": {"minecraft:crimson_trapdoor": "minecraft:mangrove_trapdoor", ...}`)
would make this one rule; not worth it for seven.

### Ids and data

Mangrove and dark oak are vanilla 1.19.2. Quark azalea (the other option the sheet offers) uses non-obvious ids:
`quark:azalea_trapdoor`, `quark:azalea_button`, `quark:azalea_pressure_plate`, `quark:azalea_planks_slab`,
`quark:azalea_planks_stairs`, `quark:stripped_azalea_wood` (from `kubejs/startup_scripts/_lib/item_dict.js:366-379`,
`:452-455`). The swap JSON is data read by Python and Java, so it names ids literally (no `global.item` lookup is
possible there).

### Decisions (one per K row; the sheet's options)

- **K28 / K29 / K33 crimson -> mangrove** (the sheet's only option). Recommendation: mangrove; it is the closest red.
- **K30 / K31 / K35 / K36 warped -> azalea or dark oak.** Recommendation: **dark oak** for all four. Warped is a dark
  teal; azalea is light green-yellow and stands out in the desert temple and the spire. Dark oak is vanilla, so the
  rules never depend on a Quark config.
- If the user picks azalea: Quark's azalea wood is on (`config/quark-common.toml:796`, `"Azalea Wood" = true`).

### Gotchas

- The desert temple puzzle: all wooden pressure plates trigger the same way (entities and items), so the puzzle keeps
  working with any wood.
- K36 is a single block in one ocean ruin; it is only worth a rule because the rule is free.

### Tests

- **NW-17 Desert temple wood:** `/locate structure betterdeserttemples:desert_temple` -> the entrance doorway slabs are
  mangrove, the big puzzle room plates are dark oak and still open the puzzle, the statue buttons are mangrove.
- **NW-18 Fortified desert village trapdoors:** a `terralith:fortified_desert_village` cartographer house -> 36 mangrove
  trapdoors in the same open / closed and facing states as before.
- **NW-19 Stronghold buttons:** a `betterstrongholds:stronghold` large junction -> mangrove buttons on the walls (not on
  the floor).

---

## Built, needs play

| Row | Status | Tests | Notes |
|---|---|---|---|
| #1 Swaps rounds 1 and 2 | Testing | NW-06, NW-07, NW-10, NW-11; older ST-150 to ST-152 | Checked: 22 rules, 1084 swaps in 73 templates (`nether_block_swaps.json`), matches the row. DEPLOYED with 1.0.9 |
| #5 Meat block swaps | Testing | ST-149 (passed on the scratch server 2026-10-14, see structure_tuning.md 8) | 5 swaps in 4 templates (`meat_block_swaps.json`). **Stale in the row:** "359 other lone red blocks" is 370 in the current [meat_blocks.md](../meat_blocks.md) ("Review (370 lone red blocks, not marked)") |

## Writeup corrections made

- [nether_worldgen.md](../nether_worldgen.md) section 3: "Nothing new yet." removed (the section lists the built
  rounds right below it).
