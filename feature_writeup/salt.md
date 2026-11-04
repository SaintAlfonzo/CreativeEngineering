# Salt and Create: The Salt (Creative Delight members)

Status: BUILT 2026-11-02, installed, not booted. `repackedSalt-1.2.4.1-ce-19.2-01` and `repackedCreateSalt-1.19.2-1.1.0-ce-19.2-01`
are nested in `mods/creativedelight-1.0.0.jar` (installed 2026-11-02 23:16; the saltable tag of section 5 in the CD jar of
2026-11-03 00:08). `salt-1.19.2-1.2.4.1.jar` and `create_salt 1.19.2-1.1.0.jar` displaced to `jar_archive/displaced/`.
Last game boot 2026-10-31. Scratch test queued: SQ-CDJ6 (section 4), SQ-CDJ7 (SQ-SALT2-01 to -07). Salt `-03` (Melting S8, config move S10, section 6)
built 2026-11-03 in the CD jar, not installed. Open salt work and its plan: section 6.
Plan: [plan_creative_delight.md](plan_creative_delight.md) J2; TODO `creativedelight/members #47`, `#48`.
Salt behaviour in the pack (one salt, melting, dissolving): [item_merges.md](item_merges.md) 2.9;
seasonings: [seasonings.md](seasonings.md).

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
- The pack's removals of three `create__the_salt:` recipes by id (`creativeengineering/salt.js` in Creative Delight)
  keep working: ids unchanged.

## 2. Decisions

1. **Rock salt exemption moved into Salt.** CD's `SaltRockNoDissolveMixin` (on Salt's `Dissolving`) is gone. The
   Salt member ships an edited `Dissolving` (a decompiled copy, `replaced_classes` with the upstream sha1 pin): both
   `maybeDissolve` and `maybeDissolveInRain` return false for `salt:raw_rock_salt_block` (the in-rain check reads the
   block at the position, as the mixin did). Same behaviour, no mixin. CD's `SaltBlockTicks` lost its
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
4. **Mod-name override.** patcherMod `CEConfig` `[mod_names] overrides` default now has
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
| S2 | Heated basin 1000 mB water -> 2 salt [200 mB -> 60 %] | CD `creativeengineering/salt.js` `water_salt` | BUILT 2026-11-03, `/reload`; SQ-SALT2-03 | owner 11-03: 2 per bucket |
| S3 | Raw Rock Salt Block surface `natural_rough` 0.92x [soft 0.88x]; The Salt's Salt Block stays soft | `kubejs/server_scripts/tags/blocktags.js` | BUILT 2026-11-03, `/reload`; SQ-SALT2-04 | none |
| S4 | Dict key `i.t_salts = '#forge:salts'` | `kubejs/startup_scripts/_lib/item_dict.js` | BUILT 2026-11-03, restart | none |
| S5 | Create salting matches the grid: one unheated mix per `#salt:can_be_salted` food Create: The Salt does not cover; its mixes for foods outside the tag removed | CD `kubejs/salt/create_salting.js` (65 upstream outputs listed from the jar) | BUILT 2026-11-03, needs S4 (restart); SQ-SALT2-05 | none |
| S6 | Raw Rock Salt: smashed with the Engineer's Hammer 4 (cutting board and grid, `NCH.hammering`; was a knife on the board, owner 11-03); crushing and milling 4 + 1 at 50 % | CD `creativeengineering/salt.js` | BUILT 2026-11-03, `/reload`; SQ-SALT2-03 | owner 11-03: 0.5 extra, hammer |
| S7 | Capped salting nutrition: generator writes `SaltingFoodValues` (+25 % of hunger, 1 to 2) | `tools/gen_salt_values.py`, `config/salt-common.toml` + `configs_edited/` copy; restart | Blocked on `food/types-and-tuning #1` (Planned) | D1 cap accepted (owner 11-03) |
| S8 | Melting: ice -> water outside ultra-warm dimensions, everything -> air in them; snow -> air. Upstream had the check inverted (`DimensionType.ultraWarm`), so it placed water only in the Nether | Salt member: decompiled `Melting` overlay, `replaced_classes` pin, member `-02` | BUILT 2026-11-03 (CD jar built, not installed); SQ-SALT2-06 | owner 11-03 |
| S9 | Butcher level 1: 12 emeralds -> 64 salt, 4 uses, 4 XP, as the BBQ powders; Salt's four trades off | CD `kubejs/salt/trades.js`; `salt-common.toml` `ButcherSaltTradesEnabled = false` (+ `configs_edited/`); restart | BUILT 2026-11-03; SQ-SALT2-07 | owner 11-03 |
| S10 | Salt's COMMON config into `creativedelight-common.toml` `[salt]` (same sub-sections and keys); salt-common.toml carried once and renamed `.moved`; pack defaults (rain dissolving off, Salt's trades off); `salt-client.toml` stays (CD has no client config) | Salt member `Configuration` overlay (`-03`); CD `compat/SaltConfigBridge`, `CDConfig` (`[salt]`, `MEMBER_FILES`), `MemberConfigMove` (now any value type), `CreativeDelight` (reload hook) | BUILT 2026-11-03 (headless spec test PASS both init orders; not booted); SQ-SALT2-08 | owner 11-03: move |
| S11 | One JEI category per seasoning, salt first | patcherMod `SeasoningJEI` | Future (waits for seasonings to settle) | none |

Not planned until a mod lands: Unusual Delight's salt from quartz and from smelting Soggy Flesh ([unusual_delight.md](unusual_delight.md)).
