# Salt and Create: The Salt (Creative Delight members)

> **2026-11-14: block melting moved to Creative Common's melting system ([melting.md](melting.md)).** Salt blocks (The Salt's, Vintage Delight's, the Hearth and Harvest salt lick stages) are sources in `#creativecommon:melting/salt`; the salt member's own block melt (`[salt] SaltBlockMeltingEnabled`) is off.
> **2026-11-15 (section 7):** a salt click never melts (it places a salt layer); the salt dispenser melts one stage or places a layer; salt layers may vanish as they melt; salt licks take salt in one way and give up top layers first; new item Salt Melt.

Status: BUILT 2026-11-02, installed, not booted. `repackedSalt-1.2.4.1-ce-19.2-01` and `repackedCreateSalt-1.19.2-1.1.0-ce-19.2-01`
are nested in `mods/creativedelight-1.0.0.jar` (installed 2026-11-02 23:16; the saltable tag of section 5 in the CD jar of
2026-11-03 00:08). `salt-1.19.2-1.2.4.1.jar` and `create_salt 1.19.2-1.1.0.jar` displaced to `jar_archive/displaced/`.
Last game boot 2026-10-31. Scratch test queued: SQ-CDJ6 (section 4), SQ-CDJ7 (SQ-SALT2-01 to -07). Salt `-03` (Melting S8, config move S10, section 6)
built 2026-11-03 in the CD jar, not installed. Open salt work and its plan: section 6.
Plan: [plan_creative_delight.md](plan_creative_delight.md) J2; TODO `creativedelight/members #47`, `#48`.
Salt behavior in the pack (one salt, melting, dissolving): [item_merges.md](item_merges.md) 2.9;
seasonings: [seasonings.md](seasonings.md).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=bb13bb01e7d0 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativedelight/members #52` | Testing | S | Salt config into the Creative Delight config (R3) |
<!-- generated:status_index end open_work -->

## 1. What was built

| Project | Folder | Upstream jar | Mod id | Version | Display name |
|---|---|---|---|---|---|
| `repackedSalt` | `development/creativeDelight/members/salt/` | `salt-1.19.2-1.2.4.1.jar` (sha1 `2d9e0112...`) | `salt` | `1.2.4.1-ce-19.2-03` | Salt (Creative Delight) |
| `repackedCreateSalt` | `development/creativeDelight/members/createsalt/` | `create_salt 1.19.2-1.1.0.jar` (sha1 `42555b0d...`) | `create__the_salt` | `1.19.2-1.1.0-ce-19.2-01` | Create: The Salt (Creative Delight) |

Both use `ce.forge-repack` (shape of `members/oceans`), `bundled_in=creativeDelight`. The upstream jars are read from
`development/creativeDelight/upstream/` (git-ignored; copied from `mods/` on 2026-11-02, sha1 pinned in each
`gradle.properties`). Licence: both MIT (declared in their mods.toml, no LICENSE file in either jar);
`NOTICE-upstream-MIT.txt` sits beside each project and goes into the jar as `<jar_name>-NOTICE-upstream-MIT.txt`.
Our overlay is GPLv3.

### 1.1 Salt

- Upstream classes and resources unchanged except `io/github/mortuusars/salt/Dissolving.class` (below) and, since
  `-02` (2026-11-03), `Melting.class` with its inner dispenser class (section 6 S8: ice melts to water outside
  ultra-warm dimensions), and since `-03` `configuration/Configuration.class` (section 6 S10: the COMMON settings in
  Creative Delight's config).
- Access transformer: the upstream `META-INF/accesstransformer.cfg` (`BlockBehaviour.isRandomlyTicking` public
  non-final, `CauldronBlock.shouldHandlePrecipitation` public) is copied unchanged. FML 1.19.2 reads that fixed path
  in every mod jar, nested Jar-in-Jar ones included; the upstream manifest had no AT entry and none is needed.
- Mixins: the repack writes its own manifest, so `mixin_configs=mixins.salt.json` names Salt's own config again
  (`MixinConfigs: mixins.salt.json` checked in the built jar; `mixins.salt.refmap.json` kept).
- mods.toml: upstream's, with the "(Creative Delight)" name, our version, an issue tracker URL; dependencies forge and
  minecraft only (as upstream).
- Ids, tags, recipe type `salt:salting`, `#salt:can_be_salted`, loot, worldgen and the biome modifier are unchanged.

### 1.2 Create: The Salt

- Upstream class and data unchanged (70 `create:` recipes, an empty lang file).
- mods.toml: Salt is a mandatory dependency (same Creative Delight jar), Create an optional one (ordering AFTER, any
  version; set by the main session 2026-11-02 to match `members #48` "optional on Create" and the rule that CD boots
  with optional mods absent). Without Create the mod is one empty `@Mod` class and its `create:` recipes are skipped
  at data load with a log line. Create is always in this pack. The upstream MCreator placeholder dependency `allmods` is
  dropped. The upstream mods.toml said version `1.0.0`; the file name's `1.19.2-1.1.0` is the recorded upstream
  version.
- The pack's removals of three `create__the_salt:` recipes by id (`creativedelight/salt.js` in Creative Delight)
  keep working: ids unchanged.

## 2. Decisions

1. **Rock salt exemption moved into Salt.** CD's `SaltRockNoDissolveMixin` (on Salt's `Dissolving`) is gone. The
   Salt member ships an edited `Dissolving` (a decompiled copy, `replaced_classes` with the upstream sha1 pin): both
   `maybeDissolve` and `maybeDissolveInRain` return false for `salt:raw_rock_salt_block` (the in-rain check reads the
   block at the position, as the mixin did). Same behavior, no mixin. CD's `SaltBlockTicks` lost its
   `NO_DISSOLVE` set and `noDissolve()`; its Vintage calls into `Dissolving.maybeDissolve` are unchanged.
2. **`SaltBlockRandomTickMixin` and `SaltLayerRandomTickMixin` stay.** They target vanilla `BlockBehaviour` and
   `SnowLayerBlock` for Vintage Delight's salt family, not Salt classes. Vintage's salt brick stairs, slabs and
   walls are plain vanilla `StairBlock` / `SlabBlock` / `WallBlock` instances, so no edit in Salt (or in Vintage's
   own block classes) can replace the `BlockBehaviour.randomTick` hook without new block classes. Salt becoming a
   member changes nothing for them.
3. **Superseded 2026-11-03 (owner: move it; section 6 S10).** Was: **`salt-common.toml` stays where it is (not moved into `creativedelight-common.toml` `[salt]`).** Salt's
   `Configuration` registers its own COMMON and CLIENT `ForgeConfigSpec` (files `salt-common.toml`,
   `salt-client.toml`) and about 20 upstream classes read its static values. Moving them means replacing
   `Configuration` with a rewrite that builds its values into Creative Delight's spec (a cross-member dependency on
   CD's main code) or a `MemberConfigMove` that only Display's own source tree can host. That is a rewrite of Salt
   classes, which J2 rules out; the hand-edited copies in `configs_edited/root_config/salt-*.toml` stay valid.
4. **Mod-name override.** Creative Common `CEConfig` `[mod_names] overrides` default now has
   `create__the_salt=Create: The Salt (Creative Delight)`. The live `config/creativeengineering-common.toml` and its
   `configs_edited/` copy still carry the old value until edited by hand (the default does not rewrite an existing
   list). Salt has no override entry: its mods.toml name shows.

## 3. Risks

- The Dissolving edit and the mixin removal are coupled: a Creative Delight bundle built without the Salt member
  (with the upstream `salt` jar in `mods/`) loses the rock salt exemption. Add `:repackedSalt` to `bundle_members`
  in the same change that ships the mixin removal.
- Without Create, `create__the_salt` logs one skipped-recipe line per Create recipe at data load (accepted; Create is always in the pack).
- An upstream Salt update changes `Dissolving.class`'s sha1 and stops the build (by design): re-check the edit.

- Config move (S10): the first boot with Salt `-03` renames `config/salt-common.toml` to `.moved` after copying its values
  into `[salt]`. Then copy `config/creativedelight-common.toml` to `configs_edited/root_config/` and drop the
  `configs_edited/root_config/salt-common.toml` backup (its values are now `[salt]`'s defaults anyway). A Creative Delight
  jar without the Salt `-03` member would leave Salt's COMMON values undefined: the bridge logs an error.

## 4. Tests

- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-SLT-01, -02, -03, -04, -05, SQ-SALT2-01.

Scratch queue rows (feature_writeup/testing/scratch_queue.md, section SQ-CDJ6):

- SQ-SLT-01: both mods load from the Creative Delight jar, names, versions, no mixin or AT error.
- SQ-SLT-02: Salt's access transformer applies (cauldron precipitation, random ticks of Vintage salt).
- SQ-SLT-03: `/salttest` probe: raw rock salt never dissolves, The Salt's Salt Block still does.
- SQ-SLT-04: recipe ids and tags unchanged (salting, `#salt:can_be_salted`, Create: The Salt recipes, the three
  removed ids absent).
- SQ-SLT-05: `salt-common.toml` still read (a changed value takes effect).
- CQ-SLT-01: mod list, JEI and WTHIT names.
- CQ-SLT-02: campfire evaporation fills a salt cauldron (access transformer, client).

## 5. The saltable tag (TODO `food/seasoning #4`, part of `#1`; built 2026-11-03)

`#salt:can_be_salted` (`i.t_can_be_salted`) is upstream's tag (148 foods in the 2026-10-30 dump) plus pack additions:

- CD `kubejs/data/creativedelight/kubejs/salt/tags.js` (`// requires: salt`, server, `/reload`): adds 151 cooked meats
  and fish and meat or fish dishes upstream misses, by item_dict key, each only when its mod is loaded (Rustic
  Delight's calamari roll, Delightful's crab rangoon, Fruits Delight's orange chicken, More Delight's burgers and rice
  bowls, Alex's Delight, End's / Ender's Delight, Hostile and Miner's Delight meats, and so on). Removes the intermediates
  bone broth, cooked rice and tomato sauce (`#1`).
- How the list was made (offline, 2026-11-03): every food_families.json member whose recipes take a meat or fish
  (`meat/*`, `fish/*` families, raw or cooked, followed through dishes made from dishes; cans, golden, ingredients and
  drinks not followed), or that is cooked meat or fish itself, minus what the dump's tag already holds. Left out by
  hand: five mob-part cookies, Hostile Delight's ghast pudding and ghast blood ice cream, Miner's Delight's crunchy and
  nutritional bars, Ocean's Delight's raw stuffed cod. A later `/dumpall` lets the list be rechecked the same way.
- "Crab bar" and "crab ragout" (the row) are not in the pack.
- Single foods next to their mod stay where they are: `moredelight/tags.js` (mashed potatoes), `veggiesdelight/tags.js`.
- Tests: SQ-SALT2-01 (queue SQ-CDJ7: added foods salt, intermediates refused).

## 6. Open salt changes and plan (compiled 2026-11-03)

Every salt change still open in the docs, in build order. Sources: TODO `food/seasoning #1` (In Progress), `#12`,
`creativedelight/members #52`, `jei/jei-coverage #6`, the seasoning brief ([briefs/food_seasoning.md](briefs/food_seasoning.md)
`seasoning #1` Build), and two owner lines in `todo/scratchpad.md` (cauldron output, salt block surfaces). Rows only
waiting on tests (`#4`, `#6`, `#7`, `#10`, members `#47`, `#48`) are not repeated here.

Salt per bucket of water: cauldron 8 (S1), heated basin 2 (S2). Raw rock salt: 4 by hand, hammer and mortar, 4 + 50 %
on crushing wheels and the millstone (S6). A Salt Block unpacks to 9.

| Step | Change | Where | Status | Decision |
|---|---|---|---|---|
| S1 | Cauldron boiled dry gives 8 salt (full), 5 (two thirds), 3 (one third) [1 + 50 %, 90 %, 75 %] | `kubejs/data/salt/loot_tables/cauldron_evaporation/` (3 tables, `set_count`) | BUILT 2026-11-03, `/reload`; SQ-SALT2-02 | partial levels scaled 8 x 1/3 and 2/3 (owner gave the 8) |
| S2 | Heated basin 1000 mB water -> 2 salt [200 mB -> 60 %] | CD `creativedelight/salt.js` `water_salt` | BUILT 2026-11-03, `/reload`; SQ-SALT2-03 | owner 2026-11-03: 2 per bucket |
| S3 | Raw Rock Salt Block surface `natural_rough` 0.92x [soft 0.88x]; The Salt's Salt Block stays soft | `kubejs/server_scripts/tags/blocktags.js` | BUILT 2026-11-03, `/reload`; SQ-SALT2-04 | none |
| S4 | Dict key `i.t_salts = '#forge:salts'` | `kubejs/startup_scripts/_lib/item_dict.js` | BUILT 2026-11-03, restart | none |
| S5 | Create salting matches the grid: one unheated mix per `#salt:can_be_salted` food Create: The Salt does not cover; its mixes for foods outside the tag removed | CD `kubejs/salt/create_salting.js` (65 upstream outputs listed from the jar) | BUILT 2026-11-03, needs S4 (restart); SQ-SALT2-05 | none |
| S6 | Raw Rock Salt: smashed with the Engineer's Hammer 4 (cutting board and grid, `NCH.hammering`; was a knife on the board, owner 2026-11-03); crushing and milling 4 + 1 at 50 % | CD `creativedelight/salt.js` | BUILT 2026-11-03, `/reload`; SQ-SALT2-03 | owner 2026-11-03: 0.5 extra, hammer |
| S7 | Capped salting nutrition: generator writes `SaltingFoodValues` (+25 % of hunger, 1 to 2) | `tools/gen_salt_values.py`, `config/salt-common.toml` + `configs_edited/` copy; restart | Blocked on `food/types-and-tuning #1` (Planned) | D1 cap accepted (owner, 2026-11-03) |
| S8 | Melting: ice -> water outside ultra-warm dimensions, everything -> air in them; snow -> air. Upstream had the check inverted (`DimensionType.ultraWarm`), so it placed water only in the Nether | Salt member: decompiled `Melting` overlay, `replaced_classes` pin, member `-02` | BUILT 2026-11-03 (CD jar built, not installed); SQ-SALT2-06 | owner 2026-11-03 |
| S9 | Butcher level 1: 12 emeralds -> 64 salt, 4 uses, 4 XP, as the BBQ powders; Salt's four trades off | CD `kubejs/salt/trades.js`; `salt-common.toml` `ButcherSaltTradesEnabled = false` (+ `configs_edited/`); restart | BUILT 2026-11-03; SQ-SALT2-07 | owner 2026-11-03 |
| S10 | Salt's COMMON config into `creativedelight-common.toml` `[salt]` (same sub-sections and keys); salt-common.toml carried once and renamed `.moved`; pack defaults (rain dissolving off, Salt's trades off); `salt-client.toml` stays (CD has no client config) | Salt member `Configuration` overlay (`-03`); CD `compat/SaltConfigBridge`, `CDConfig` (`[salt]`, `MEMBER_FILES`), `MemberConfigMove` (now any value type), `CreativeDelight` (reload hook) | BUILT 2026-11-03 (headless spec test PASS both init orders; not booted); SQ-SALT2-08 | owner 2026-11-03: move |
| S11 | One JEI category per seasoning, salt first | Creative Common `SeasoningJEI` | Future (waits for seasonings to settle) | none |

Not planned until a mod lands: Unusual Delight's salt from quartz and from smelting Soggy Flesh ([unusual_delight.md](unusual_delight.md)).

2026-11-14 (plan_bugfix_1114 3.8): the member's block melt is off (`Melting.tryMeltFromBlock` returns false, also when CD's `SaltBlockTicks` calls it; `[salt.Melting] SaltBlockMeltingEnabled` defaults off and points at Creative Common's `[melting]`). Rules: [melting.md](melting.md). Superseded 2026-11-15: the "salt item click, one stage per click" never existed in play (the pack's salt is Vintage Delight's salt dust, whose click places a layer; SQ-MELT-08) and is not wanted (owner): section 7.

## 7. Salt, ice and salt licks (plan_post_scratch_1115 B1 to B7, B9; built 2026-11-15)

Status: BUILT 2026-11-15, not installed: Creative Common 1.2.0 (`Melting` listener, tags), Creative Delight 1.1.0 nesting
`repackedSalt-1.2.4.1-ce-19.2-04` and the hearth member. Queue: SQ-PS15-01 to -07, -09. TODO `tooling/scratch-1115-follow-ups #3` to `#8`.

### 7.1 Rules

| Step | Rule | Where |
|---|---|---|
| B1 | Salt dust clicked on ice or snow places a salt layer on top (as on any block); the click never melts. The layer is a salt source, so the ice under it melts at the melting system's block rate. | Vintage `SaltDustItem` (unchanged). The Salt's own `salt:salt` item would still melt by click, but it is merged into salt dust and never in play |
| B2 | Salt dust (and `salt:salt`) in a dispenser: a melting target in front melts one stage (packed and blue ice step down a tier) for 1 salt; air in front (with support below) gets a salt layer, a salt layer below 8 gets one more, for 1 salt; anything else: the salt is dropped. `[salt.Melting] SaltItemMeltingEnabled = false` turns the melt off (the layer place stays). | Salt member `Melting.SaltItemDispenseBehavior`; CD `salt/SaltLayerMelt` registers it on `vintagedelight:salt_dust` at common setup |
| B3 | Each time a salt layer melts ice or snow by the melting system's random tick, it loses one layer with chance `[salt.Melting] SaltLayerVanishChance` (default 0.05); the last layer leaves air. Only `SnowLayerBlock` salt sources (Vintage Delight's salt layer); the Salt Block, Raw Rock Salt Block, salt lick stages and other full blocks never vanish. | Common `Melting.setSaltMeltListener` (after a salt-source melt in `targetTick`); CD `salt/SaltLayerMelt` |
| B4 | Dropped for licks (G7.5): salt layers fall as part of Creative Compat's general layer fall; no lick hook. | none |
| B5 | Removed 2026-11-17 (section 8). Was: a lick with salt layers on top gives up one layer per lick (player or animal), from the topmost layer block down; the lick keeps its stage until no layer is left. | hearth `SaltLick.wear`, `topLayer` |
| B6 | Salt Melt (`salt:salt_melt`, Tools tab): 64 uses; right click on any melting target melts it one stage for 1 durability (packed and blue ice step down a tier); in a dispenser the same on the block in front (nothing in front to melt: the dispenser fails and keeps it). Recipe: shapeless, 2 salt + 2 raw rock salt. | Salt member overlay `ce/SaltMeltItem`, `ce/SaltMeltRegistry` (Forge annotation scan, `RegisterEvent`); CD `creativedelight/salt.js`; JEI `kubejs/assets/creativedelight/kubejs/salt/jei_info.js` |
| B7 | Superseded 2026-11-17 by Rock Salt refill (section 8). Was: one refill, `SaltLick.refill`: each salt (hand or dispenser) or salt layer (placed on top) restores one stage of a worn lick, waxed or not; leftover layers stay on the full lick. Salt on a full lick puts a layer on top: by hand salt dust's own click, by dispenser `SaltLick.addLayerOnTop`. The dispenser now also refills waxed stages (was unwaxed only). | hearth `SaltLick`, `SaltEvents`, `LickableSaltBlock` |
| B9 | Glowstone cage lantern is no longer a fire source (light 3, no random tick). | Common `tags/melting_tags.js` |

### 7.2 Notes

- Salt Melt texture: the item model's `layer0` is `salt:item/salt` again (owner, 2026-11-17, after E3 had switched it to
  `salt:item/raw_rock_salt`). KubeJS assets outrank the jar, so it renders `kubejs/assets/salt/textures/item/salt.png`,
  the texture of The Salt's own salt item, which is merged into Vintage's Salt and hidden, so no visible item shares it;
  the pack's recolor
  (The Salt's shape, Vintage Delight's pink palette, `palette_swap.py` via `textureswapper/salt/make_salt.py`), not
  upstream art used as is (G2h). The `kubejs/assets/salt/textures/*` row in `sources.csv` covers it. An own Salt Melt
  texture is open (owner's tools only).
- Creative Delight compiles against the upstream Salt jar, so `SaltLayerMelt` reads `SaltLayerVanishChance` by
  reflection; a Creative Delight jar without the `-04` Salt member reads chance 0 (no layer vanishes).
- Salt member `-04`: `replaced_resources` now lists `assets/salt/lang/en_us.json` (upstream copy plus `item.salt.salt_melt`).
- SQ-HH-474 (salt dust on an unworn salt block places a layer): by design under B1 and B7 unless the owner says otherwise.

## 8. Salt redesign (plan_post_scratch_1115 G2, G6, G7.5 to G7.7, G7.13, G7.14; built 2026-11-17)

Status: built in the Creative Delight jar (Salt member `-04`, hearth `-04`, Vintage member, CD main), not installed, not
booted. Queue: SQ-SALT2-10 to -21, CQ-SALT2-01, -02 (rows/SALT.qb section 3). TODO `tooling/scratch-1115-follow-ups #21`, `#6`; 1115b
`#12`, `#13`. Supersedes 7.1 B4 (salt lick hook), B5 and the layer part of B7.

| Step | Rule | Where |
|---|---|---|
| Names | Raw Rock Salt -> Rock Salt; Block of Raw Rock Salt -> Rock Salt Block; Salt Block -> Salt Dust Block (Vintage's merged `salt_block` too); ores -> Rock Salt Vein, Deepslate Rock Salt Vein; licks -> "{Waxed} {Lightly/Well/Heavily} Licked Rock Salt Block", stage 0 waxed "Waxed Rock Salt Block"; Vintage decor -> Chiseled Salt Block, Pillar, Bricks, Brick Slab/Stairs/Wall. Ids unchanged; salt lamps and Mixed Salt Bricks keep their names; the item stays "Salt" | Salt member `assets/salt/lang/en_us.json`, hearth and Vintage lang files |
| Lick root | Stage 0 is a Rock Salt Block placed after world generation (`hearthandharvest:placed_rock_salt_block`, 8.1); the POI, the waxed stage 0 model (Rock Salt Block texture, referenced in place) and the JEI lick page follow. Salt Dust Blocks are no longer licks | hearth `SaltLick.SALT_BLOCK_ID`, `PlacedRockSaltBlock`, `models/block/waxed_salt_block.json` |
| Refill | Rock Salt (`salt:raw_rock_salt`) restores one stage of a worn lick by hand or dispenser, waxed or not; on a full lick it does nothing. Salt, salt dust and salt layers no longer refill; licks no longer eat layers on top first; the dispenser no longer puts a layer on a full lick | hearth `SaltLick.isSalt`, `refill`, `SaltEvents` (dispenser on Rock Salt only), `LickableSaltBlock` (layer hook removed) |
| Last wear | The heavily licked stage wears away and drops 1 Salt (salt dust; The Salt's salt without Vintage) | `SaltLick.wear` |
| Recipes | Rock Salt Block = 4 Rock Salt (2x2), back to 4 (upstream 9 both ways, removed); heated mixing 1000 mB water + Rock Salt Block -> Rock Salt Block + 2 Salt (`rock_salt_water_salt`; `water_salt` removed); Large Salt Lamp = 7 Rock Salt, glowstone, wooden slab | CD `creativedelight/salt.js` |
| Acid | One acid recipe in a heated basin (`powergrid_acid`); the in-water variant `powergrid_acid_in_water` is dropped (owner, 2026-11-17). Water poured first no longer becomes salt because the heated water -> salt mixing needs a Rock Salt Block in the basin | `server_scripts/other/power_grid.js` |
| Hand click | Salt dust used on a saltable block (ice, snow; the clicked block, else the block in front of the clicked face) melts it one stage through Common's `Melting.meltStep` and spends 1 dust; a salt layer is placed only when the block is not saltable (owner, 2026-11-17). Dispenser does the same | CD `salt/SaltLayerMelt.onUseSalt`; SQ-MELT-08, SQ-MELT-08b |
| Salt Melt | Undamaged Salt Melts stack to 16 (`getMaxStackSize(ItemStack)`), damaged ones to 1; a use from a stack splits one off, damages it and hands it back (inventory, else dropped; a dispenser keeps it in a free slot, else spits it out). It and the salt dispenser act only on saltable blocks (Common `isSaltable`; older Common: `isTarget`) | Salt member `ce/SaltMeltItem`, `Melting.CommonMelting` |
| `salt:meltables` | Follows `#creativecommon:melting/saltable` (wiped, then the tag added), so The Salt's own paths refuse Blue Ice and reinforced snow | CD `salt/block_tags.js`; needs block_dict key `b.t_salt_meltables` (inert until it exists) |
| JEI | Water Evaporation page with the pack's yields (8, 5, 3 from the loot tables when the server runs in the same process; pack defaults on a dedicated server's client), The Salt's own page hidden | CD `compat/jei/SaltEvaporationJEI` |
| Rare Ice | `allowInsertingItemsToIce=false`: Rare Ice no longer takes the right click on plain ice, so salt dust and the Salt Melt act on it (1115b `#12`) | `config/rare-ice.properties` (+ `configs_edited/`) |

Notes:
- Natural Rock Salt Blocks (salt caves, beach patches) are not licks (owner, 2026-11-17, 8.1; the G6-10 "messy path"
  that let animals wear them is withdrawn).
- 1115b `#13` (a salt layer on melting ice drops as dust before the vanish roll): the cause is in Creative Common
  `Melting` (the target is melted before the salt listener runs, so the layer above loses support first). Not fixed here.
- Salt layers falling (E4, G7.5) is Creative Compat's general layer fall; the lick hook of E4 is dropped. Vintage's salt
  layer is `vintagedelight:salt` (a `SnowLayerBlock`, layers 1 to 8, loot 1 salt dust per layer).

### 8.1 Placed-only licks (owner, 2026-11-17; every placement since G11)

Every Rock Salt Block placed after world generation is a salt lick: by a player, a fake player (Create's Deployer), a
dispenser, Create's Schematicannon, `/setblock` or `/fill`. Only Rock Salt Blocks generated with the world (salt caves,
beach patches, structures generated with their chunk) are never lick targets and never wear (G11 `#21`; first build
2026-11-17 counted player placement only).

| Part | Rule | Where |
|---|---|---|
| Placed twin | When `salt:raw_rock_salt_block` is set in a loaded chunk on the server, it is swapped for `hearthandharvest:placed_rock_salt_block`: same models (The Salt's four random variants), named "Rock Salt Block", drops and picks `salt:raw_rock_salt_block`, no item of its own. The hook is `BlockBehaviour.onPlace`, which world generation never calls (it writes proto chunks); for an item placement Forge calls it after the place event. Not a placement: a piston push (old state `moving_piston`) and Create contraptions setting blocks with the moving flag keep the block they carry | hearth `mixin/BlockPlaceLickMixin` (HEAD of `m_6807_`), `SaltEvents.afterPlace`, `PlacedRockSaltBlock`, blockstate and loot table under the hearth member |
| Lick chain | Stage 0 of the chain (wax, unwax, refill back to full) is the placed twin; The Salt's block is out of the chain and out of the `salt_lick` POI type, so animals never look for it and player licks, honeycomb and Rock Salt do nothing on it | hearth `SaltLick` (plain[0]), `HHSaltRegistry.createPoi` |
| Tooltip | The Rock Salt Block item keeps the "Animals lick this" line (placing it makes a lick); the JEI lick page names the placements | `SaltEvents.onTooltip`, hearth lang |
| Tags | Pickaxe tag and salt melting source (`#creativecommon:melting/salt`, as the Rock Salt Block) through `crow_salt_tags.js`, inert until Creative Common's block dict has `b.placed_rock_salt_block`; until then it breaks by hand at the same hardness and does not melt ice. Blockrunner surface tags of the Rock Salt Block are not copied | CD `hearthandharvest/crow_salt_tags.js` |

Assumed (G11 build, 2026-11-17): `/setblock`, `/fill` and structures placed later (`/place`, structure blocks) count
as placements; a structure that a mod writes into an already loaded chunk turns its Rock Salt Blocks into licks too.

Save safety: the new block id is additive and no id is removed or renamed. Worn and waxed stages in older saves stay
licks; unwaxing waxed stage 0 or refilling a heavily licked stage to full gives the placed twin. A POI record a save kept
on a block that is no lick any more (Salt Dust Blocks from before section 8) is skipped by the block check in
`StayNearSaltGoal.findSalt`, and vanilla drops records whose state left the type when the section reloads. A Rock Salt
Block placed before this change counts as natural; break and place it again to make it a lick. Section 8 was never
installed, so no save holds such a block as a lick. Queue: SQ-SALT2-10, -19 (generated and pushed blocks, /setblock),
-20, -21, -22 (dispenser, Deployer, Schematicannon), CQ-SALT2-02 (rows/SALT.qb section 3).

## History

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-10-26: Creative Delight: rock salt stays, beach patches are built on it (owner, 2026-10-26) (development/creativeDelight/members/salt/src/main/java/io/github/mortuusars/salt/Dissolving.java)
- 2026-11-03: Create salting for every saltable food (salt.md 6 S5, TODO food/seasoning #1, 2026-11-03) (kubejs/data/creativedelight/kubejs/salt/create_salting.js)
- 2026-11-03: The Salt's saltable tag (#salt:can_be_salted, i.t_can_be_salted): the Salting craft takes any food in it (salt.md 5, TODO food/seasoning #4 and #1, 2026-11-03) (kubejs/data/creativedelight/kubejs/salt/tags.js)
- 2026-11-03: Salt's butcher trade, mirroring the Barbeque's Delight seasoning powders (barbequesdelight/trades.js): level 1, 12 emeralds for 64 salt, 4 uses, 4 XP, price multiplier 0.05 (salt.md 6 S9, TODO food/seasoning #12, 2026-11-03) (kubejs/data/creativedelight/kubejs/salt/trades.js)
