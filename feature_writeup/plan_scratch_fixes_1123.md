# Scratch fixes plan 2026-11-23 (r1123cd findings, dragon armor, JEED, docs, stale rows)

Source: scratch run r1123cd ([scratch_run_2026-11-23.md](testing/scratch_run_2026-11-23.md) 12.5, judges 2 to 4) and
the owner's chat request 2026-11-23. Session role for the build: `impl` (one code session at a time).

## 0. Intake

<!-- generated:status_index begin open_work sha1=a07097aa89fe -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 12 open TODO rows (Bug 1, Planned 8, In Progress 1, Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `crops #60` | Bug | S | Hemp and tobacco do not grow in mid winter (SQ-CS-05) |
| `creative-brewing #1` | Planned | SQ-CBB-05 | Remove Kippers from the pack |
| `creative-brewing #2` | Planned |  | Alcohol, food and item merges docs refreshed against the code |
| `creative-delight-members #4` | Planned | SQ-EDL-03 | Void Pepper Sprout no longer spreads; a passing tick fruits it |
| `effect-text #1` | Planned | SQ-CBO-36 C | JEED provider for Magic Resistance: Felsic gear |
| `gear-tuning #2` | Planned | SQ-CEQ-08 | Dragon armor 40 points: helmet 9, chest 12, leggings 11, boots 8 |
| `generators-and-tests #29` | Planned |  | Rewrite the stale and not-simple rows flagged by the r1123 judges |
| `loot-tables #3` | Planned | SQ-CBA-03 SQ-CBO-32 | Creative Loot ban warns on every boot when the banned mod is absent (PneumaticCraft, Lightman's) |
| `potions #11` | Planned | SQ-CBO-02 SQ-CBO-32 | potions.js mix hearthandharvest:strong_bad_omen -> buzzier_bees:strong_luck warns when Buzzier Bees is absent |
| `creativebrewing/publish #4` | In Progress | S | Chain alone: Pale Jane, Steel Toe Stout and Kippers cannot be brewed |
| `creativebrewing/publish #13` | Testing | S | Chain: loot tables of optional-mod blocks fail to parse |
| `mobs/tweaks #21` | Testing | S P | Baby squid, glow squid and dolphin do not spawn as babies |
<!-- generated:status_index end open_work -->

| # | Item (owner's words) | Class | Finding |
|---|---|---|---|
| I1 | "fix the 8 bugs found in the last scratch testing" | clear | r1123cd product faults: SQ-CBB-03, SQ-CBB-05, SQ-CBO-02, SQ-CBO-32, SQ-CBP-01, SQ-CDJ4-08b, SQ-CS-05, SQ-EDL-03 (6 FAIL + the 2 HARNESS rows, run totals) |
| I2 | "voidpepper shouldn't spread" | conflict, settled | ends_delight.md 3.1 row "Void Pepper behavior: Implement it" (2/3 spread per passing tick, `VoidpepperBlock.java:23-25`, `SPREAD_RADIUS` 7); the owner's new ruling replaces it |
| I3 | "kippers removed" | premise | Nothing removes kippers: `brewinandchewin:kippers` is registered (`BnCItems.java:81`), fermenting recipe `members/brewin/.../fermenting/kippers.json`, `item_dict.js:12316`, `food_dict.js:361`; not in `swaps/removed.json`. Asked |
| I4 | "add guards on the creative loot pc/lc issue" | clear | `05_tuning.js:353` `checkBanSwitches` calls `modLootOn` (`07_rules.js:32-48`, `Java.loadClass` of PneumaticCraft / Lightman's classes) with no loaded-mod check; existing row `loot-tables #3` |
| I5 | "dragon loot should be tuned to give 2 full armor bars, tune helmet to fit" | premise | In game the set reads 40 already (Dragon Loot config 8/13/11/8, `config/dragonloot*`); Equipment `tune_armor.js:99-103` sets 7/12/10/7 = 36 and is not applied to Dragon Loot armor (SQ-CEQ-08 read helmet 8). Armor cap is 50 (`ArmorCapMixin`, `[dragon_gear] armorCap`). Asked |
| I6 | "add a magic resistance jeed description" | premise | The description text exists (`creativeCommon/.../lang/en_us.json:15`). What is missing is a JEED effect provider: `jeed_providers.js` has none; potions show through JEED's own potion provider; Felsic gear (`gear_effects.js:67,80`) is not listed. SQ-CBO-36 still expects the old `creativebrewing:` id. Asked |
| I7 | "update the alcohol/food/item merges docs" | clear | Stale spots found by the judges: alcohol.md drink count (51 since Hearth and Harvest D16; chain 49 of 51), kippers rows (alcohol.md 203, 289; food.md 142; food_catalog.md 447; food_families.md 1310, 3235; can_parity.md 350); item_merges.md 3 still lists the Mini Cantaloupe grid recipe (dropped 2026-11-19, plan_food_batch_1119.md A6); food_catalog.md 914 and vintage_delight.md 1052 tropical pizza slice 5 / 0.75 (now 4 / 0.7) |
| I8 | "rewrite the old tests" | open | Asked: which rows |

## 1. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | voidpepper shouldn't spread | conflict | Void Pepper Sprout no longer spreads. A passing random tick fruits it; worldgen placement is unchanged. Replaces ends_delight.md 3.1 "2/3 spread". | 2026-11-23 | chat 2026-11-23 |
| R2 | kippers removed | premise | Remove Kippers from the pack: `swaps/removed.json`, fermenting recipe removed, dict/tag/can/food entries out; rows expect no kippers. | 2026-11-23 | AskUserQuestion 2026-11-23 |
| R3 | dragon loot 2 full armor bars, tune helmet to fit | premise | 40 points: helmet 9, chestplate 12 (upgraded chestplate 12), leggings 11, boots 8. Toughness, knockback, durability unchanged. Must actually apply in game. | 2026-11-23 | AskUserQuestion 2026-11-23 |
| R4 | add a magic resistance jeed description | premise | Text stays. Add a JEED effect provider for Magic Resistance listing the Felsic gear that gives it; SQ-CBO-36 moves to `creativecommon:magic_resistance`. | 2026-11-23 | AskUserQuestion 2026-11-23 |
| R5 | rewrite the old tests | open | The rows the r1123 judges flagged (section 6), not the whole prescreen-stale list. | 2026-11-23 | AskUserQuestion 2026-11-23 |
| R6 | guards on the creative loot PC/LC issue | clear | A ban skips its `modLootOn` read when the banned mod is not loaded (one INFO line at most, no WARN). | 2026-11-23 | agent default, confirmed by the owner (chat 2026-11-23) |
| R7 | dragon armor route | clear | Set the values in the Dragon Loot config (`dragon_armor_protection_*`, hand-edited config, copy in `configs_edited/root_config/`) and keep `tune_armor.js` in step with a comment that the Dragon Loot config is authoritative; only if the config route cannot give 12 to both chestplates, fix the tune path instead. | 2026-11-23 | agent default, confirmed by the owner (chat 2026-11-23) |
| R8 | JEED provider home | clear | The Magic Resistance provider lives with the gear rows in Creative Equipment (gear effects are Equipment's since 2026-11-06), guarded on JEED being loaded. | 2026-11-23 | agent default, confirmed by the owner (chat 2026-11-23) |

## 2. Rows

| Row | Status | Item | Tests |
|---|---|---|---|
| `loot-tables #3` | Planned | I4 ban guard | SQ-CBA-03 SQ-CBO-32 |
| `potions #11` | Planned | SQ-CBO-02 Buzzier Bees mix guard | SQ-CBO-02 SQ-CBO-32 |
| `creativebrewing/publish #4` | In Progress | SQ-CBB-03 Pale Jane on the chain; kippers out of its scope (R2) | SQ-CBB-03 |
| `creativebrewing/publish #13` | Testing | SQ-CBP-01 loot tables that fail to parse | SQ-CBP-01 |
| `mobs/tweaks #21` | Testing | SQ-CDJ4-08b baby squid despawns | SQ-CDJ4-08b |
| `crops #60` | Bug | SQ-CS-05 hemp and tobacco in mid winter | SQ-CS-05 |
| `creative-delight-members #4` | Planned | R1 Void Pepper no spread (B1) | SQ-EDL-03 |
| `creative-brewing #1` | Planned | R2 Kippers removed (A3) | SQ-CBB-05 |
| `gear-tuning #2` | Planned | R3 dragon armor 9/12/11/8 (A4) | SQ-CEQ-08 |
| `effect-text #1` | Planned | R4 JEED Magic Resistance provider (A5) | SQ-CBO-36, client look |
| `creative-brewing #2` | Planned | I7 alcohol, food, item merges docs (C) | |
| `generators-and-tests #29` | Planned | R5 stale rows rewrite (D) | |

## 3. Phase A: script and data fixes (no Java)

### A1 `loot-tables #3` Creative Loot ban guard (Creative Loot)
`T.ban` stores the mod id (from `@mod`, or a new `mod:` option for regex targets: Lightman's ban is a regex on
`lightmanscurrency:`). `checkBanSwitches` skips a ban whose mod is not loaded (`Platform.isLoaded`). The pre-roll and
post-roll removals still run (they cost nothing on an absent mod). Readme of `kubejs/data/creativeloot/kubejs/loot/` gets
the `mod:` option. Build `:creativeLoot:` (the scripts are copied into the jar). Rows: SQ-CBA-03, SQ-CBO-32 drop their
`grep -vF "[loot-tuning] ban"` workaround once the guard is in, so the rows prove the guard.

### A2 `potions #11` Buzzier Bees mix guard (Creative Brewing)
Wrap the `strong_bad_omen -> buzzier_bees:strong_luck` mix (`potions.js` about line 236) in the Buzzier Bees `has()`
block like the other luck mixes. Build `:creativeBrewing:`.

### A3 Kippers removed (R2, Creative Brewing + Creative Delight + Common)
- `swaps/removed.json` gets `brewinandchewin:kippers` (and a canned kippers id if one exists; can_parity.md 350 says
  n/a). Never pruned.
- Remove the fermenting recipe (`members/brewin/.../recipes/fermenting/kippers.json`: delete in the member, it is a port
  of our own copy) and the origins `meat.json` entry. Item registration: hide it the way other removed port items are
  hidden (check `swaps/removed.json` comment and the tentacle precedent, plan_creative_delight.md I1), no conversions.
- Dicts: drop `f.fish_cooked_kippers` (`food_dict.js:361`) and keep or drop `i.kippers` per the removed-item rule in
  item-dict-mechanics; tools/food_catalog.py stops listing it.
- Builds: `:creativeBrewing:`, `:creativeDelight:` (removed.json), `:creativeCommon:` (dicts).

### A4 Dragon armor 40 points (R3, R7)
Dragon Loot config: helmet 9, chest 12, leggings 11, boots 8 (backup in `configs_edited/root_config/`). Check how the
upgraded chestplate reads its value (same `dragon_armor_protection_chest` key or its own). `tune_armor.js:99-103` set to
9/12/12/11/8 with a comment naming the config as the source. Equipment's `armorCap` comment (40 points) stays true.
Builds: `:creativeEquipment:` for the tune comment and values.

### A5 JEED Magic Resistance provider (R4, R8)
One `jeed:effect_provider` recipe for `creativecommon:magic_resistance` listing the Felsic gear that grants it (read the
list from `gear_effects.js` at load, not a typed list), through `global.NCH` (extend it if the JEED type has no helper;
`jeed_providers.js` already uses one). Guard on JEED loaded. Build `:creativeEquipment:`.

## 4. Phase B: Java and cause hunts

### B1 Void Pepper no spread (R1, Creative Delight member `ends`)
`VoidpepperBlock`: drop the spread branch; a random tick that passes the soil tier's chance sets `pepper=true` (fruits).
`SPREADING` stays as a property (blockstates and saved worlds use it) but no longer drives behavior; `isRandomlyTicking`
true until fruited. Drops (3 fruiting, 1 not) unchanged. ends_delight.md 3.1, 5 and EDL-05 rewritten. Build
`:creativeDelight:`. Row: SQ-EDL-03 rewritten to "count stays 1, origin fruits".

### B2 `creativebrewing/publish #4` SQ-CBB-03 Pale Jane on the chain
Log read: `cdevtest recipes creativebrewing:fermenting/pale_jane` printed no `pale_jane_from_rice_wine`. First check
whether the recipe id changed (row text from r1106) or the recipe is dropped on the chain set (rice wine fluid input
missing), then fix the code or the row. Kippers leave this row's scope (R2).

### B3 `creativebrewing/publish #13` SQ-CBP-01 seven loot tables fail to parse on the chain
Tables: `creativecommon:blocks/{azalea,twisted,blossom,ancient}_composter`, `delightful:blocks/{mini_melon,cantaloupe}`,
`minecraft:blocks/pumpkin`. The four composters are Common's own tables (same family as the NCH composter recipe errors,
`creative-common-publish #16`); read the parse error lines in the RES dir first. Fix in the owning jar.

### B4 `crops #60` SQ-CS-05 hemp and tobacco do not grow in mid winter
Per the row: check hemp and tobacco growth conditions (light, soil, their own `canGrow`) and CropSeasons off-season
handling before calling `offPenalty` wrong. Owner Creative Calendar.

### B5 `mobs/tweaks #21` SQ-CDJ4-08b baby squid gone 7 s after summon
Second run in a row (r1120lr too). Suspects: squid despawn rules for a named-less summoned baby, Alex's Mobs or the
baby-mobs member despawn hook. Repro with `/summon squid ~ ~ ~ {Age:-24000,PersistenceRequired:1b}` to split despawn
from conversion.

## 5. Phase C: docs (doc-pass agent)

- alcohol.md: drink count 51 (chain 49 of 51, bellini and wodka absent); kippers rows 203, 289 removed with a note.
- food.md 142, food_catalog.md 447 and 914, food_families.md 1310 and 3235, can_parity.md 350, vintage_delight.md 1052:
  kippers out, tropical pizza slice 4 / 0.7.
- item_merges.md 3: Mini Cantaloupe is cutting only; recount the merge list against the 49 live merges (SQ-VTDA-12).
- ends_delight.md (B1), loot.md and creative_loot.md (A1), creative_equipment.md dragon values (A4), effects.md JEED
  providers (A5), creative_brewing.md row table.
- Changelog bullets (`unreleased.md`): Void Pepper no longer spreads; Kippers removed; dragon armor 9/12/11/8 [8/13/11/8];
  Magic Resistance JEI page lists Felsic gear. A1, A2 and the B fixes are dev-only or logs-only: no bullet unless a cause
  turns out player-visible.

## 6. Phase D: stale rows rewrite (R5, doc-pass agent)

Rewrite to match code and docs (from the r1123 judges' notes, run doc 12.5):
- Stale pass texts: SQ-CDB-02, SQ-CDB-11, SQ-CDB-15, SQ-CDFW-06, SQ-BJ-01, SQ-ENGM-05, SQ-BOT-01, SQ-FO-05, SQ-GAP-01,
  SQ-GAP-02, SQ-GAP-06, SQ-SLT-01, SQ-RCO-01, SQ-FCB-09, SQ-MELT-10, SQ-CMP-07, SQ-CGE-02.
- Not-simple batch faults: SQ-CBO-10, -27, -28, -30, -33, -52, -53 (Armor / Tool Effects now Creative Equipment: add
  Equipment to the `@modset` or cut to effects only), SQ-CBO-36 (R4), SQ-CDFO-02, -03 (own `@modset` boots),
  SQ-MDP-17 (own step with the `miners_upstream` symlink removed). Tell the drive owner before any `@modset` change.
- INCONCLUSIVE rows: SQ-CBP-03b, SQ-CDFW-10, SQ-CEQ-01, SQ-CIF-02, SQ-CM-01: give each a printed count or baseline it
  can judge, or drop the half that cannot run (owner asked only for rewrites; dropping a half is noted in the hand-over).
- Rows of the fixes: SQ-CBB-05 (expects no kippers), SQ-CBB-03 (B2), SQ-EDL-03 (B1), SQ-CEQ-08 (9/12/11/8),
  SQ-CBA-03 and SQ-CBO-32 (A1).
- Each touched row gets `@reviewed 2026-11-2x`; lint with the queue linter; `status_index.py`.

## 7. Order and commits

1. A1, A2 (one commit each, Creative Loot and Creative Brewing builds).
2. A3 Kippers (Common, Delight, Brewing builds; commit).
3. A4, A5 (config + Equipment build; commit).
4. B1 (Delight build), then B2 to B5 cause hunts, each fix its own commit.
5. C docs and changelog (doc-pass), D rows (doc-pass), `status_index.py`; commit.
Push each commit to `agent_staging`.

## 8. Tests after the build

SQ-CBA-03, SQ-CBO-02, SQ-CBO-32, SQ-CBO-36, SQ-CBB-03, SQ-CBB-05, SQ-CBP-01, SQ-CDJ4-08b, SQ-CS-05, SQ-EDL-03, SQ-CEQ-08,
plus the rewritten rows of section 6 on their next run. Client look: the Magic Resistance JEI page shows the Felsic gear.
