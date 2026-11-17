# Melting: salt, fire and lava melt ice and snow (Creative Common)

Status: BUILT and installed 2026-11-14 (Creative Common 1.2.0, `development/creativeCommon`), scratch rows queued
(section 7), not run. Plan: [plan_bugfix_1114.md](plan_bugfix_1114.md) 3.8 (owner 2026-11-14). TODO
`creativedelight/members #56`. Related: [salt.md](salt.md) (The Salt's item click and the switched-off block melt).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=9c3f2041819c -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Bug 1, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativedelight/members #60` | Bug | S | Salt item click does not melt ice one stage per click (SQ-MELT-08) |
| `creativedelight/members #61` | Testing | S | Glowstone cage lantern is in the melting fire tag but does not random tick (SQ-MELT-10) |
<!-- generated:status_index end open_work -->

## 1. Rules

Three source kinds melt two target kinds. One roll per random tick of the target.

| Source | Ice and snow (meltable) | Packed and blue ice (meltable_slow) |
|---|---|---|
| salt | touching (6 faces) | touching |
| fire | within 3 blocks | touching |
| lava | within 4 blocks | within 2 blocks |

- "Within N": Euclidean distance at most N, counted through any blocks. Touching = distance 1.
- Meltable: ice and frosted ice (and Rare Ice) become a water source; snow layers, snow blocks and powder snow
  become air. In an ultra-warm dimension (the Nether), or with `placeWater = false`, ice becomes air too.
- Meltable slow: blue ice becomes packed ice, packed ice becomes ice (`slowSteps`).
- Chances: 0.4 per random tick for meltable (about 3 minutes next to a source at randomTickSpeed 3), 0.05 for a
  packed or blue ice tier (about 23 minutes per tier).
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

- Hook: `mixin/MeltingBlockStateMixin` on `BlockStateBase.randomTick` (`m_222972_`, what `ServerLevel.tickChunk`
  calls) runs before the block's own random tick, and skips it when the block melted. Every block class is covered
  without a mixin per class; other blocks pay one identity-map lookup (`Reference2ByteOpenHashMap` of flags, rebuilt
  on `TagsUpdatedEvent`).
- Target tick: roll the chance first (a dark glacier costs mostly the roll), then the 6 neighbours (salt, fire, lava
  within 1), then, only when block light reaches the target or a neighbour, a sphere scan of radius
  max(fire, lava range), nearest first, stopping at the first source. Reads use loaded chunks only
  (`getChunkNow`), never loading one.
- Light gate: every fire-tag block is meant to give light 4 or more, so a source within 3 always lights the target.
  A fire or lava source behind a sealed wall with no light path to the target does not melt it (known limit of the
  gate; the owner's "through any blocks" holds wherever light gets through).
- Source-driven: a fire-tag block whose light is below 4 and that random ticks (magma, light 3) melts the targets in
  its own sphere from its random tick, each target with its own roll; the target scan ignores such blocks so nothing
  rolls twice. The chunk sections in reach are checked by palette (`LevelChunkSection.maybeHas`) first, so magma
  seas without ice cost almost nothing.
- Random ticks: ice, frosted ice, snow layers and magma tick in vanilla. `snow_block`, `powder_snow`, `packed_ice`
  and `blue_ice` get random ticks at common setup (`extraRandomTickBlocks`, the `isRandomlyTicking` field `f_60445_`,
  the way Creative Delight's SaltBlockTicks does it). A modded target that does not random tick melts only from a
  source-driven source.
- Nether snow: `MeltingBlockStateMixin` on `BlockStateBase.onPlace` (`m_60696_`, TAIL).
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
- API for other jars: `Melting.isTarget(state)`, `Melting.meltStep(level, pos)` (one stage at once, no roll: a
  meltable melts, a slow one steps down; for the salt dispenser and the Salt Melt), `Melting.meltedState(state, level,
  slow)`, `Melting.setSaltMeltListener(listener)` (told after a salt source in salt range melted a target by random
  tick, with the source's position and state; Creative Delight's `salt/SaltLayerMelt` uses it for the salt layer
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
- `meltable`: ice, frosted ice, snow, snow block, powder snow, Rare Ice.
- `meltable_slow`: packed ice, blue ice.
- `ultrawarm_instant`: snow, snow block, powder snow.

The modded list is the 2026-11-13 `/dumpall` (`kubejs/exported/tags/block_tags_by_block.json`, tagged blocks only),
re-grepped 2026-11-14 for torch / lantern / sconce / brazier / candelabra (same 131 ids). Light levels could not be
read from the dumps (no light dump exists): SQ-MELT-10 reads the boot log's low-light warning instead.

## 4. Config: `[melting]` in `config/creativecommon-common.toml`

Forge adds the section with these defaults on the first boot of the 2026-11-14 jar (the instance toml has no
`[melting]` yet). The pack uses the defaults, so nothing was hand-edited and `configs_edited/` has no copy.

| Key | Default | Range | Live |
|---|---|---|---|
| `enabled` | true | | yes |
| `saltRange` | 1 | 0 to 4 | yes |
| `fireRange` | 3 | 0 to 8 | yes |
| `lavaRange` | 4 | 0 to 8 | yes |
| `slowFireRange` | 1 | 0 to 8 | yes |
| `slowLavaRange` | 2 | 0 to 8 | yes |
| `meltChance` | 0.4 | 0 to 1 | yes |
| `slowMeltChance` | 0.05 | 0 to 1 | yes |
| `placeWater` | true | | yes |
| `vanillaLightMelting` | false | | yes |
| `ultraWarmInstantMelt` | true | | yes |
| `slowSteps` | `minecraft:blue_ice=minecraft:packed_ice`, `minecraft:packed_ice=minecraft:ice` | | yes |
| `extraRandomTickBlocks` | snow block, powder snow, packed ice, blue ice | | restart |

A `saltRange` above 1 makes every target scan a sphere on each successful roll (salt gives no light to gate it).

## 5. History

Before 2026-11-14 nothing in the pack melted by heat on purpose. Vanilla melted ice above block light 11 and snow
layers above 11 (about 4 and 3 blocks from lava). Magma (light 3) melted nothing, and packed ice, blue ice, snow blocks
and powder snow never melted. The salt member's block melt (`Melting.tryMeltFromBlock`, `[salt]
SaltBlockMeltingEnabled`) never ran for `salt:salt_block`: a `SaltSandBlock` copied from sand with no random ticks, so
`ISaltBlock.onSaltRandomTick` was never called (SQ-SALT2-06 FAIL twice in r1113). That block melt is switched off in
Creative Delight in the same change, so nothing fires twice.

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

Scratch queue section "SQ-MELT Melting system" ([scratch_queue.md](testing/scratch_queue.md)): SQ-MELT-01 to -10,
SQ-MELT-P1 (perf, ice spikes style box against a control), SQ-SALT2-06 (rewritten), SQ-SALT2-06b (Nether snow).
SQ-MELT-08 (the salt item click, one stage per click) closed 2026-11-15 as by design (a click places a layer, owner;
plan_post_scratch_1115 B1); the salt rows are SQ-PS15-01 to -03, -07, -09.

Scratch run r1115 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): SQ-MELT-01 to -07, -09, -P1, SQ-SALT2-06, -06b, SQ-AZW-01, SQ-DCM-02 PASS. SQ-MELT-08 FAIL: no per-click salt melt exists (a salt dust click places a Vintage Delight salt layer, which melts at block rate; the salt dispenser behaviour returns at once), owner choice in TODO. SQ-MELT-10 FAIL (finding): `architects_palette:glowstone_cage_lantern` is in the fire tag but does not random tick (boot WARN), owner TODO. Fix-1 (Common 03:15, behaviour-neutral): Rare Ice mixin skipped without Rare Ice, config values cached; reruns SQ-MELT-09b PASS, -P1b PASS (Melting 0.30 % of server samples, was 1.00 %; the live toml edit clause not judgeable on scratch, a restart picks values up).
