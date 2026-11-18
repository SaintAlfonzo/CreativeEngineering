# Melting: salt, fire and lava melt ice and snow (Creative Common)

Status: BUILT and installed 2026-11-14 (Creative Common 1.2.0, `development/creativeCommon`), scratch rows queued
(section 7), not run. Plan: [plan_bugfix_1114.md](plan_bugfix_1114.md) 3.8 (owner, 2026-11-14). TODO
`creativedelight/members #56`. Rework of 2026-11-17 (melt ticks, snow by layer, magma, saltable tag, slow tier frozen):
[plan_post_scratch_1115.md](plan_post_scratch_1115.md) G7, G8; TODO `tooling/scratch-1115-follow-ups #20`. G11 rulings
and E1 frozen decor built 2026-11-17 (lane C1, plan G12; TODO `#20`, `#22`, `#9`). Related: [salt.md](salt.md) (The Salt's item click and the switched-off block melt).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=d30c79eee39f -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativedelight/members #60` | Testing | S | Salt item click does not melt ice one stage per click (SQ-MELT-08) |
| `creativedelight/members #61` | Testing | S | Glowstone cage lantern is in the melting fire tag but does not random tick (SQ-MELT-10) |
<!-- generated:status_index end open_work -->

## 1. Rules

Three source kinds melt two target kinds. A target near a source melts on a steady pace: one roll on each of its
melt ticks (about every 5 s), plus its random ticks.

| Source | Ice, snow, frozen decor (meltable) | Packed ice, IW cryosol, AP packed ice (meltable_slow) | Blue ice, IW permafrost (meltable_slow, lava_only) |
|---|---|---|---|
| salt | touching (6 faces), saltable blocks only | touching: one step down | never |
| fire (not magma) | within 3 blocks | never | never |
| magma | within 3, and snow within 2 melts at once | within 2 (`slowMagmaRange`) | never |
| lava | within 4 blocks | within 2 (`slowLavaRange`) | touching only: one step down |

- Hot places (G11): in an ultra-warm dimension (the Nether) every target but blue ice and permafrost, and in a hot
  biome (`#creativecommon:melting/hot`: Creative Calendar's arid, dry and tropical season types) ice, snow and frozen
  decor (not packed ice or cryosol), take a melt tick every `hotMeltTickInterval` = 20 ticks (15 to 25) with no
  source, rolling `meltChance` (0.4; the slow tier too, so packed ice and cryosol melt rapidly in the Nether, owner
  2026-11-17). Packed ice placed in the Nether steps to ice, then air; cryosol steps to dirt. Snow on blue ice or
  permafrost is left alone there too.
- The End (G11, `endContactOnly`): every source melts only the 6 blocks touching it (salt, fire, magma, lava); magma's
  instant zone is the touching blocks; magma's snow and ice guard reaches 1.
- No `minecraft:fire` in the End (G11, `endNoFire`): every placement is refused (flint and steel, fire charges, lava
  spread, End crystals, commands, any mod's `setBlock`); fire already there goes out on its next tick. Soul fire stays.
  Campfires in the End are Creative Delight's (TODO `#22`, campfire part).

- "Within N": Euclidean distance at most N, counted through any blocks. Touching = distance 1.
- Meltable: ice and frosted ice (and Rare Ice) become a water source; a snow layer stack loses one layer per melt
  (the last one leaves air); snow blocks and powder snow become air whole. In an ultra-warm dimension (the Nether),
  or with `placeWater = false`, ice becomes air too.
- Meltable slow: steps `slowSteps` (blue ice -> packed ice -> ice, permafrost -> cryosol -> dirt, AP polished, chiseled
  and pillar packed ice -> ice; the AP slab, stairs, wall and vertical slab have no row and melt to air). G11 replaced
  G7.15's "salt only": lava and magma melt them again within 2, other fire never.
- Frozen decor (E1, `meltProducts`, shared properties copied by name): IW snowy stone, cobblestone and stone bricks
  (16, every shape) -> the plain block in the same shape and facing (vertical slabs -> Quark's); frosty glass, pane,
  grass, fern -> glass, pane, grass, fern; thin ice -> water (air when ultra-warm); coarse snow -> gravel; frost, icicle
  and the 5 snow brick blocks -> air. Snow bricks are `sluggish` (`sluggishMeltFactor` 0.25 of their chance) and vanish
  in the Nether like snow. Philips Ruins frozen prismarine never melts.
- Salt melts only `#creativecommon:melting/saltable` (ice, frosted ice, Rare Ice, snow, snow block, powder snow,
  packed ice, cryosol, the frozen decor, AP packed ice). Product: a `saltProducts` row (packed ice -> ice, cryosol ->
  dirt), else the heat product (`meltProducts`, `slowSteps`), shared properties copied, else water for ice
  (`placeWater`), else air; snow loses layers instead. A salt block's tick removes 1 layer; one
  salt dispense or Salt Melt use (`meltStep`) removes `saltMeltSnowLayers` = 3, a snow block counting as 8 (it
  becomes 5 layers). Items of saltable blocks show a gray "Saltable" tooltip line.
- Snow resting on `#creativecommon:melting/keeps_snow` (blue ice, IW permafrost) ignores fire, lava, magma and
  vanilla light melting; salt still melts it (G6 4b).
- Magma (`#creativecommon:melting/magma`): snow within `magmaInstantRange` = 2 melts at once (on placement, on
  magma's tick, on the snow's melt tick); no snow forms from snowfall and no water freezes within `fireRange` = 3
  (`magmaStopsFreezing`, server weather only, not world generation).
- Chances per roll: `meltChance` 0.4, `snowLayerMeltChance` 0.8 per layer, `slowMeltChance` 0.05 per tier, times
  `sluggishMeltFactor` for sluggish blocks. With `meltTickInterval` 100 (5 s, each tick 3.75 to 6.25 s; G11, was 200)
  ice beside a torch melts in about 12 s once found, a stack of 8 snow layers in about 50 s, packed ice beside lava
  steps in about 100 s; finding takes up to one random tick of the target or of a ticking source (about 68 s on
  average at randomTickSpeed 3).
- Fire: a block with a `lit` property counts only while lit (campfires, braziers, candelabras).
- Nether snow: snow layers, snow blocks and powder snow placed in an ultra-warm dimension become air at once, with a
  fizz (by hand, dispenser, `/setblock`, any mod's `setBlock`). Worldgen placement is not covered (it does not call
  `onPlace`).
- Vanilla light melting is off: sea lanterns, shroomlights, redstone torches and lamps, jelly torches and electric
  lanterns no longer melt ice or snow layers. Torches, lanterns and glowstone melt through the fire rule (3 blocks).
  `vanillaLightMelting = true` brings vanilla's light melt back (it then runs beside this system).
- Not touched: Serene Seasons' seasonal melt, Immersive Weathering's ice melt (section 6).
- Salt by hand never melts: a salt dust click places a salt layer, which then melts by the rule above. One stage at
  once (`meltStep`, no roll) only from a salt dispenser and the Salt Melt item; a salt layer may lose a layer when it
  melts something ([salt.md](salt.md) 7, since 2026-11-15).

## 2. How it runs (`melting/Melting.java`)

- Method (owner, 2026-11-17, plan G8, "sources find, targets schedule"): a pair is found from either side, then the
  target schedules its own melt ticks. Finding: the target's random tick (below), or the random tick of a source
  that random ticks (magma, lava, salt blocks: flag `SRC_TICK`), which scans its own sphere after a palette check of
  the sections in reach. Each target found gets a vanilla scheduled tick (`level.scheduleTick`, saved in the chunk's
  `block_ticks` by the game; one per position and block, so many sources do not stack) after 75 to 125 percent of
  `meltTickInterval`. On that tick (`MeltingBlockStateMixin` on `BlockStateBase.tick`, `m_222963_`,
  `Melting.onScheduledTick`) the target looks for a source again (also dim magma, without the light gate), rolls,
  melts one stage, and schedules the next tick while it is still a target; with no source in reach it stops. No
  per-chunk registry and no save data of our own. `meltTickInterval = 0` turns scheduling off (random ticks only, and
  a source's tick rolls and melts its targets directly, as on 2026-11-14). Frosted ice is never scheduled (it runs
  its own scheduled ticks). Lava skips its own scan in ultra-warm dimensions (lava seas; Nether ice still finds lava
  by its random tick). G11: in ultra-warm dimensions lava scans only its 6 touching blocks (blue ice beside Nether
  lava; the rest melts as a hot place).
- Hot places: `Melting.isHot` (ultra-warm dimension, or `getNoiseBiome` in `#creativecommon:melting/hot` for the
  meltable tier) on the target's random tick, its melt tick and on placement (`onPlaced` schedules the first tick, as
  packed ice and cryosol do not random tick); `hotTick` rolls, melts as heat would and schedules the next tick at
  `hotMeltTickInterval`.
- The End: `endContact` clamps every reach to 1 in `findSource`, `sourceTick`, `onPlaced` and `magmaBlocksFreezing`.
- End fire: `mixin/EndFireMixin` on `Level.setBlock(BlockPos, BlockState, int, int)` (`m_6933_`, HEAD, both sides)
  returns false for `Blocks.FIRE` in `Level.END`; refused at the source, so an End crystal's every-tick fire costs one
  check and no sound. `Melting.onScheduledTick` removes fire already there on its next tick.
- Stairs: vanilla `StairBlock.randomTick` passes the tick to its base block's state at the same position; the target
  path checks that the ticked state is the one in the world, so snowy stone stairs never melt as a full snowy stone.
- Alternatives (a) extra rolls and (b) extra random ticks per section were not built; the perf comparison is
  SQ-MELT-P2 (queued, not run).
- Hook: `mixin/MeltingBlockStateMixin` on `BlockStateBase.randomTick` (`m_222972_`, what `ServerLevel.tickChunk`
  calls) runs before the block's own random tick, and skips it when the block melted. Every block class is covered
  without a mixin per class; other blocks pay one identity-map lookup (`Reference2ByteOpenHashMap` of flags, rebuilt
  on `TagsUpdatedEvent`).
- Target tick: roll the chance first (a dark glacier costs mostly the roll), then the 6 neighbors (salt, fire, lava
  within 1), then, only when block light reaches the target or a neighbor, a sphere scan of radius
  max(fire, lava range), nearest first, stopping at the first source. Reads use loaded chunks only
  (`getChunkNow`), never loading one.
- Light gate: every fire-tag block is meant to give light 4 or more, so a source within 3 always lights the target.
  A fire or lava source behind a sealed wall with no light path to the target does not melt it (known limit of the
  gate; the owner's "through any blocks" holds wherever light gets through).
- Source-driven: a fire-tag block whose light is below 4 and that random ticks (magma, light 3) melts the targets in
  its own sphere from its random tick, each target with its own roll; the target scan ignores such blocks so nothing
  rolls twice. The chunk sections in reach are checked by palette (`LevelChunkSection.maybeHas`) first, so magma
  seas without ice cost almost nothing.
- Random ticks: ice, frosted ice, snow layers and magma tick in vanilla, IW's snowy, frosty, frost and icicle blocks
  tick for their own un-snow and un-frost. `snow_block`, `powder_snow`, IW thin ice, the 5 snow brick blocks, AP coarse
  snow and AP Molten Nether Bricks (a source) get random ticks at common setup (packed and blue ice no longer,
  2026-11-17: lava, magma and salt blocks find them from their own tick) (`extraRandomTickBlocks`, the `isRandomlyTicking` field `f_60445_`,
  the way Creative Delight's SaltBlockTicks does it). A modded target that does not random tick melts only from a
  source-driven source.
- Nether snow and snow beside magma: `MeltingBlockStateMixin` on `BlockStateBase.onPlace` (`m_60696_`, TAIL).
- Magma against snowfall and ice: `mixin/BiomeMeltMixin` on `Biome.shouldSnow` (`m_47519_`) and
  `Biome.shouldFreeze(LevelReader, BlockPos, boolean)` (`m_47480_`), HEAD, false on a `ServerLevel` with magma within
  `fireRange` (palette check first). Serene Seasons' patch inside `shouldFreeze` runs after it.
- Saltable tooltip: `melting/SaltableTooltip` (client, `ItemTooltipEvent`), lang `creativecommon.tooltip.saltable`.
- Vanilla light melt: `mixin/VanillaLightMeltMixin` redirects the block-light read in `IceBlock` and `SnowLayerBlock`
  randomTick to 0 while the switch is off; `mixin/RareIceLightMeltMixin` (pseudo, Rare Ice's own copy) the same,
  gated on `rare_ice` in `CommonMixinPlugin.NEEDS_MOD` (no class-load error without Rare Ice; SQ-MELT-09, 2026-11-15).
- Config reads: the tick paths read `Melting.cfg`, a plain copy of the `[melting]` values refreshed on
  ModConfigEvent Loading and Reloading of the main Common config (`Melting.onConfig`, mod bus), not ForgeConfigSpec
  values per call (SQ-MELT-P1, 2026-11-15). Before the first load: melting off, vanilla light melt as vanilla.
  `slowSteps` (read only on a melt) and `extraRandomTickBlocks` (setup) still read the spec.
  Other injections into those methods keep running (Immersive Weathering's rain melt of snow layers, Creative
  Delight's salt layer tick).
- Log at every data load: `[melting] tags: N meltable, N slow, N salt, N fire, N lava, N ultra-warm instant`, the
  source-driven list, and a warning naming any fire-tag block with light below 4 that does not random tick (it then
  melts only touching targets).
- API for other jars: `Melting.isTarget(state)`, `Melting.isSaltable(state)`, `Melting.meltStep(level, pos)` (one salt
  melt at once, no roll: `saltMeltSnowLayers` snow layers, else the salt product; false when the block is not
  saltable; for the salt dispenser and the Salt Melt), `Melting.saltMelt(level, pos, layers)` (the same with a layer
  count), `Melting.saltedState(state, level, layers)`, `Melting.meltedState(state, level, slow)` (heat, one layer), `Melting.setSaltMeltListener(listener)` (told when a salt source in salt range melts a target by a
  tick, just before the target changes, so a salt layer resting on the target is still supported when the listener
  rolls; with the source's position and state; Creative Delight's `salt/SaltLayerMelt` uses it for the salt layer
  vanish chance, 2026-11-15).

## 3. Tags

Block tags under `creativecommon:melting/`, filled by Creative Common's
`kubejs/data/creativecommon/kubejs/tags/melting_tags.js` through `global.tags`; every mod's line is guarded by
`global.has`. Ids and globs are `block_dict.js` keys (`b.t_melt_*`, `b.melt_fire_*`, ...). The Java side
(`melting/MeltTags.java`) works with any tag empty, so Common runs without The Salt or Vintage Delight.

- `salt`: `salt:salt_block`, `salt:raw_rock_salt_block`; Vintage Delight's salt family (`vintagedelight:*salt*`, the
  Evaporator); the Hearth and Harvest licked and waxed salt lick stages (`hearthandharvest:*licked_salt_block`,
  `waxed_salt_block`).
- `fire`: `#minecraft:fire` (fire, soul fire, Architect's Palette nether brass fire, Creative Magic dark fire, MCA
  infernal flame), `#minecraft:campfires` (with the 16 Tinted Campfires), magma, torch, wall torch, soul torch and
  soul wall torch, lantern, soul lantern, jack o'lantern, glowstone, and the modded flame lights:

  | Mod | In the tag | Left out (not a flame) |
  |---|---|---|
  | Supplementaries | every sconce (`sconce*`), brass, copper, crimson, lead, silver and wall lantern | |
  | Everything Copper | the 16 copper and copper soul lanterns | |
  | Redeco | the 16 paper lanterns, stone and deepslate lantern | |
  | Upgrade Aquatic | tooth lantern | 16 jelly torches |
  | Framed Blocks | framed torch, wall torch, soul torch, soul wall torch | framed redstone torches |
  | Fairy Lights | candle, oil, paper lantern, jack o'lantern | orb lantern, the `_light` helper blocks |
  | Architect's Palette | nether brass torch, wall torch, lantern | algal, redstone and glowstone cage lanterns (glowstone: light 3, no random tick, dropped 2026-11-15, SQ-MELT-10) |
  | Twigs | the 5 paper lanterns | |
  | Quark | blaze lantern, paper lantern, sakura paper lantern, duskbound lantern | |
  | Dustrial Decor | large lantern, large soul lantern | redstone lanterns |
  | Alex's Abnormalities | the 3 tooth lanterns | |
  | SecurityCraft | reinforced lantern, reinforced soul lantern | reinforced sea lantern |
  | Immersive Engineering | lantern | electric lantern |
  | Decorative Blocks | brazier, soul brazier (while lit) | |
  | Blocky Siege | brazier (on) | brazier (off) |
  | Nether's Delight | propelplant torch, wall torch | |
  | Fantasy Furniture | necrolord and royal candelabra (while lit) | |
  | Domestication Innovation | wayward lantern | |
  | Botania, Philip's Ruins | | animated torch, frozen sea lantern |

  Also left out: redstone torches, sea lanterns, candles and candle holders, lamps, modded glowstone blocks.
- `lava`: `minecraft:lava_cauldron`; any block whose fluid state is `#minecraft:lava` counts without the tag.
- `meltable`: ice, frosted ice, snow, snow block, powder snow, Rare Ice; frozen decor (E1): IW `snowy_*` (16),
  `frosty_*` (4), frost, thin ice, icicle, `snow_brick*` (5), AP coarse snow.
- `meltable_slow`: packed ice, blue ice, Immersive Weathering cryosol and permafrost (2026-11-17), AP `*packed_ice*` (7).
- `lava_only` (G11): blue ice, IW permafrost.
- `sluggish` (E1): IW snow bricks, slab, stairs, wall, vertical slab.
- `ultrawarm_instant`: snow, snow block, powder snow, IW snow bricks (5).
- `saltable` (2026-11-17): ice, frosted ice, snow, snow block, powder snow, packed ice, Rare Ice, IW cryosol, the
  frozen decor and AP packed ice (E1). Not blue ice, IW permafrost, frozen prismarine, reinforced blocks. The Salt's own `salt:meltables` (its item click) is
  Creative Delight's to align (plan E1, G7.11).
- `keeps_snow` (2026-11-17): blue ice, IW permafrost.
- `magma` (2026-11-17): magma block, AP Molten Nether Bricks (G11; the only magma-like block in the 2026-11-17 dump:
  light 3, made to random tick). The Java side treats every magma block as a fire source too.
- Biome tag `hot` (G11): `#creativecalendar:seasons/arid`, `/dry` (savannas, shrublands; owner 2026-11-17) and `/tropical` (only with
  Creative Calendar loaded); the Nether counts by its ultra-warm dimension type.

The modded list is the 2026-11-13 `/dumpall` (`kubejs/exported/tags/block_tags_by_block.json`, tagged blocks only),
re-grepped 2026-11-14 for torch / lantern / sconce / brazier / candelabra (same 131 ids). Light levels could not be
read from the dumps (no light dump exists): SQ-MELT-10 reads the boot log's low-light warning instead.

## 4. Config: `[melting]` in `config/creativecommon-common.toml`

Forge added the section on the first boot of the 2026-11-14 jar. 2026-11-17: the new keys were written into the
instance toml with their defaults, and `slowSteps` and `extraRandomTickBlocks` (old values kept by Forge) set to the
new defaults by hand; mirrored to `configs_edited/root_config/creativecommon-common.toml`.

| Key | Default | Range | Live |
|---|---|---|---|
| `enabled` | true | | yes |
| `saltRange` | 1 | 0 to 4 | yes |
| `fireRange` | 3 | 0 to 8 | yes |
| `lavaRange` | 4 | 0 to 8 | yes |
| `slowLavaRange` | 2 (packed ice, cryosol; lava_only blocks always 1) | 0 to 8 | yes |
| `slowMagmaRange` | 2 (G11) | 0 to 8 | yes |
| `meltChance` | 0.4 | 0 to 1 | yes |
| `slowMeltChance` | 0.05 | 0 to 1 | yes |
| `placeWater` | true | | yes |
| `vanillaLightMelting` | false | | yes |
| `ultraWarmInstantMelt` | true | | yes |
| `slowSteps` | blue ice = packed ice, packed ice = ice, IW permafrost = IW cryosol, IW cryosol = dirt, AP polished / chiseled / pillar packed ice = ice | | yes |
| `extraRandomTickBlocks` | snow block, powder snow, IW thin ice, the 5 snow brick blocks, AP coarse snow, AP Molten Nether Bricks | | restart |
| `snowLayerMeltChance` | 0.8 | 0 to 1 | yes |
| `meltTickInterval` | 100 (0 = off; G11, was 200) | 0 to 12000 | yes |
| `magmaInstantRange` | 2 | 0 to 4 | yes |
| `magmaStopsFreezing` | true | | yes |
| `saltMeltSnowLayers` | 3 | 1 to 8 | yes |
| `saltProducts` | packed ice = ice, IW cryosol = dirt | | yes |
| `meltProducts` | the 22 frozen decor rows (E1) | | yes |
| `sluggishMeltFactor` | 0.25 | 0 to 1 | yes |
| `hotMeltTickInterval` | 20 (0 = off) | 0 to 12000 | yes |
| `endContactOnly` | true | | yes |
| `endNoFire` | true | | yes |

2026-11-17 (G11, E1): `slowFireRange` and `slowHeatMelting` removed; the instance toml's `[melting]` was rewritten
with the new defaults (all values were defaults) and mirrored to `configs_edited/root_config/`.

A `saltRange` above 1 makes every target scan a sphere on each successful roll (salt gives no light to gate it).

## 5. History

Before 2026-11-14 nothing in the pack melted by heat on purpose. Vanilla melted ice above block light 11 and snow
layers above 11 (about 4 and 3 blocks from lava). Magma (light 3) melted nothing, and packed ice, blue ice, snow blocks
and powder snow never melted. The salt member's block melt (`Melting.tryMeltFromBlock`, `[salt]
SaltBlockMeltingEnabled`) never ran for `salt:salt_block`: a `SaltSandBlock` copied from sand with no random ticks, so
`ISaltBlock.onSaltRandomTick` was never called (SQ-SALT2-06 FAIL twice in r1113). That block melt is switched off in
Creative Delight in the same change, so nothing fires twice.

2026-11-17 (plan_post_scratch_1115 G7, G8; lane M1): melt ticks (sources find, targets schedule), snow melts by
layer, magma instant zone and snow/ice guard, saltable tag and tooltip, keeps_snow, IW cryosol and permafrost joined
the slow tier, and the slow tier taken off heat (`slowHeatMelting = false`, G7.15): packed and blue ice no longer
melt beside fire and lava (before: touching fire, within 2 of lava) and no longer random tick.

2026-11-17, later (G11, E1; lane C1): melt ticks every 5 s; lava and magma melt packed ice and cryosol again within 2,
blue ice and permafrost only touching lava (fire never melts the slow tier); hot places and the End rules; no fire in
the End; Molten Nether Bricks join magma; frozen decor melts (`meltProducts`, `sluggish`, AP packed ice).

## 5b. Not built from G7 (2026-11-17)

- IW's snowy conversion and the vanilla `snowy` property near magma are not touched: no snow forms within reach, so
  grass and podzol there stay unsnowy, but Immersive Weathering's own snowy-stone growth is not blocked.
- Ambient melting (biome temperature, season, weather): audit only (G7.14), nothing built; snow on keeps_snow blocks
  covers vanilla light melting today and any ambient path added later must ask `Melting` the same way.
- Arid, dry and tropical biomes do not melt packed ice or cryosol (the ruling names ice and snow); rivers that take a
  desert's season type (Calendar's adaptive type) are not hot (the tag holds biomes only).
- Layer falling (lane L2, Creative Compat): Melting schedules ticks on snow layers. A tick scheduled by a falling
  check for the same position and block while a melt tick is pending is merged into the pending one (vanilla
  dedup), so a fall check can wait up to `meltTickInterval`.

## 6. Other melt paths still live

Traced 2026-11-14 for "why did Nether ice melt in r1113":

- **Immersive Weathering `ice_icicle_and_melt`** (`data/immersive_weathering/block_growths/ice_icicle_and_melt.json`,
  `IceGrowth`, owners `#minecraft:ice`, random tick): any ice in an ultra-warm dimension melts to air with a sizzle;
  also, by day, ice in a biome warm enough melts. This is what melted the r1113 Nether ice (no salt involved). Left on:
  it agrees with the Nether rule here. Its growth chance is IW's (`growth_chance` default).
- Immersive Weathering's snow layer melt in rain (`SnowLayerBlockMixin`, randomTick HEAD).
- Serene Seasons' seasonal melt (its own random update handler).
- The salt dispenser and the Salt Melt (salt member, through `meltStep`; [salt.md](salt.md) 7).

## 7. Tests

Headless: `tools/test_melting.sh` (`tools/melting_test/`, `MeltingTest`: spec defaults, snow by layer under heat and
salt, snow block to 5 layers, products, shared property copy, melt tick delay range; part of `check_all.py`).
Scratch rows for the 2026-11-17 rework: SQ-PS15-17 to -23, CQ-PS15-04 (PS15.qb section 2; -22 superseded by
SQ-MELT-11); perf SQ-MELT-P2 (queued for a later perf run, MELT.qb section 2). G11 and E1: SQ-MELT-11 to -20,
CQ-MELT-01 (MELT.qb section 3; SQ-PS15-08 superseded). The headless test also covers the new defaults and the
by-name property copy.

Scratch queue section "SQ-MELT Melting system" ([scratch_queue.md](testing/scratch_queue.md)): SQ-MELT-01 to -10,
SQ-MELT-P1 (perf, ice spikes style box against a control), SQ-SALT2-06 (rewritten), SQ-SALT2-06b (Nether snow).
SQ-MELT-08 (the salt item click, one stage per click) closed 2026-11-15 as by design (a click places a layer, owner;
plan_post_scratch_1115 B1); the salt rows are SQ-PS15-01 to -03, -07, -09.

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): SQ-MELT-01 to -07, -09, -P1, SQ-SALT2-06, -06b, SQ-AZW-01, SQ-DCM-02 PASS. SQ-MELT-08 FAIL: no per-click salt melt exists (a salt dust click places a Vintage Delight salt layer, which melts at block rate; the salt dispenser behavior returns at once), owner choice in TODO. SQ-MELT-10 FAIL (finding): `architects_palette:glowstone_cage_lantern` is in the fire tag but does not random tick (boot WARN), owner TODO. Fix-1 (Common 03:15, behavior-neutral): Rare Ice mixin skipped without Rare Ice, config values cached; reruns SQ-MELT-09b PASS, -P1b PASS (Melting 0.30 % of server samples, was 1.00 %; the live toml edit clause not judgeable on scratch, a restart picks values up).
