# Post-scratch plan, run r1115 (written 2026-11-15)

<!-- generated:status_index begin open_work sha1=7fe0ba6e4f37 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 24 open TODO rows (Bug 1, Planned 7, In Progress 1, Testing 15), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/scratch-1115-follow-ups #2` | Bug | C | Flint and Terrasteel portal crashes AMD clients |
| `tooling/scratch-1115-follow-ups #9` | Planned | P | Investigate frozen decor melting |
| `tooling/scratch-1115-follow-ups #12` | Planned | P | Investigate original wild hops vs current |
| `tooling/scratch-1115-follow-ups #14` | Planned | S P | Investigate missing wild crop placements and missing crops |
| `tooling/scratch-1115-follow-ups #16` | Planned | C | Heap spill into warped forest: look in game |
| `tooling/scratch-1115-follow-ups #21` | Planned | S C | Salt redesign: rock salt names, rock salt lick root and refill, water-salt recipe swap, lamp recipe, Salt Melt stacking, JEI yield |
| `tooling/scratch-1115-follow-ups #22` | Planned | S C | Small fixes: campfire light tooltip, torches light campfires, dragon armor +4, Rare Ice worldgen only |
| `tooling/scratch-1115-follow-ups #23` | Planned | S C | Layers: BlockRunner materials, full snow to powder snow, jump and fall in layers, leather boots, leaf thorns and overlay, bamboo leaves drop |
| `tooling/scratch-1115-follow-ups #6` | In Progress | S | Falling salt layer refills a salt lick; lick pulls top layers first |
| `tooling/scratch-1115-follow-ups #1` | Testing | C | Wall exit lift: floor portal to wall portal stuck in floor |
| `tooling/scratch-1115-follow-ups #3` | Testing | S | Salt: click on frozen block places a layer, no melt |
| `tooling/scratch-1115-follow-ups #4` | Testing | S | Salt: dispenser places a layer on air, one melt (1 salt) on a frozen block |
| `tooling/scratch-1115-follow-ups #5` | Testing | S | Salt layer may vanish when it melts a frozen block |
| `tooling/scratch-1115-follow-ups #7` | Testing | S | Salt dust refills a lick (dispenser, hand, layer above) |
| `tooling/scratch-1115-follow-ups #8` | Testing | S C | New item: Salt Melt |
| `tooling/scratch-1115-follow-ups #10` | Testing | S | Drop glowstone cage lantern from the fire tag |
| `tooling/scratch-1115-follow-ups #11` | Testing | S | Hops vine and other vine crops cannot be replaced by placing |
| `tooling/scratch-1115-follow-ups #13` | Testing | S P | Fix wild hops trunk search |
| `tooling/scratch-1115-follow-ups #15` | Testing | S | No pie placeable; lunchbox edible items only |
| `tooling/scratch-1115-follow-ups #17` | Testing | S | Power Grid arm takes one circuit component per pickup |
| `tooling/scratch-1115-follow-ups #18` | Testing | S | Acid from rotten flesh and spider eyes in water |
| `tooling/scratch-1115-follow-ups #19` | Testing | S | Upgrade Aquatic guard for river tree worldgen files |
| `tooling/scratch-1115-follow-ups #20` | Testing | S C | Melting: 5x speed, magma snow guard, snow on blue ice and permafrost, IW cryosol and permafrost melt, snow melts by layer |
| `tooling/scratch-1115-follow-ups #24` | Testing | S C | Raspberry Pie: single page, book scale 125%, horizontal buff slots and progress bar, icon swaps |
<!-- generated:status_index end open_work -->

Source: [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) section 9 (findings routing) and the owner's
decisions of 2026-11-15 on each open finding. TODO rows: family `tooling/scratch-1115-follow-ups` in
[TODO.md](todo/TODO.md) (rows move into their feature families when a family is next swept). Test rows:
"SQ-PS15 / CQ-PS15" at the end of [scratch_queue.md](testing/scratch_queue.md), not run.

Order: A (done today), then B (salt, one coherent redesign, built in one pass), C (hops and wild crops, investigation
first), D (small independent fixes, any order). Investigations report before the build rows they gate.

## A. Portals (Creative Compat; Immersive Portals 2.3.7)

| Step | What | State |
|---|---|---|
| A1 | Wall-exit lift (SQ-PG-01b). Floor portal to wall portal left the player at y -61.0, inside the floor, unable to move. `IPExitLiftMixin` now also runs IP's lift when a block collision overlapping the player's box lies in front of the exit plane (the floor); collisions behind the plane (the wall the portal is cut into) still do not lift, so a walk-out is unchanged. | built 2026-11-15, **not installed** (another session's server JVM was running); verify CQ-PS15-01 |
| A2 | Crash SQ-PG-01c (client SIGSEGV when a Flint and Terrasteel Nether portal renders). Investigation below. | investigated, cause not confirmed, bisect queued (CQ-PS15-02) |

### A2 findings

- Crash frame: libc (a memory read at address 0x2) called from `GL11C.nglDrawElements` <- vanilla `BufferUploader`
  <- IP `ViewAreaRenderer.buildPortalViewAreaTrianglesBuffer` <- `RendererUsingStencil.restoreDepthOfPortalViewArea`.
  The faulting address is a tiny integer, so the driver treated the draw's index "offset" as a client-memory pointer:
  `glDrawElements` ran with no element buffer bound to the active vertex array. 3 of 3 runs, always the first frame
  with a see-through IP portal (gun portals render opaque and never reach this path).
- Hardware: AMD Renoir, Mesa 23.2.1 (radeonsi), not NVIDIA, so the old NVIDIA gldebug notes do not apply.
- Index-buffer state is touched by three installed mods: **ImmediatelyFast 1.4.0** (`fast_buffer_upload`:
  `MixinVertexBuffer.optimizeIndexDataUploading` replaces the index upload with `glBufferSubData` and its own size
  tracking; plus a `BufferUploader.drawWithoutShader` hook from `hud_batching`), **Rubidium 0.6.2c** (replaces vertex
  array and buffer binding; Rubidium is deprecated for Embeddium, its own log warns), and Distant Horizons 3.0.2.
- Ranked suspects: ImmediatelyFast `fast_buffer_upload` first (it is the only one rewriting the index upload of the
  vanilla `VertexBuffer` that IP's view-area buffer goes through, and it is a config switch, no jar change), Rubidium
  second, Distant Horizons last.
- Not confirmed: nothing was bisected. The earlier runs changed the jar handle fix only.
- Plan (CQ-PS15-02, owner machine, one change per launch): (1) `fast_buffer_upload: false` in
  `config/immediatelyfast.json` (copy to `configs_edited/root_config/`); (2) if still crashing, `hud_batching: false`;
  (3) if still crashing, Rubidium out; (4) Distant Horizons out. The first change that stops the crash is the cause.
  If it is ImmediatelyFast, ship the config switch (and file upstream); if Rubidium, the options are Embeddium or a
  portal-only guard in Compat. Not a code fix until the culprit is known.
- Until fixed: portals that render see-through (every Nether frame lit by Flint and Terrasteel) crash AMD Mesa clients.
  Release blocker for any version that ships Immersive Portals with these mods together.

## B. Salt and melting (Creative Delight salt member, Creative Common `Melting`)

Decisions (owner, 2026-11-15). Terms: a **frozen block** is anything the Melting tags treat as meltable (ice, frosted
ice, packed ice, blue ice, snow layers, snow block, powder snow, and whatever B8 adds).

| Step | Decision | Notes |
|---|---|---|
| B1 | Salt placed on a frozen block does not melt it; it places a salt layer (what salt dust does today). | The old writeup (salt.md, plan 3.8) said the click goes through `meltStep`; rewrite it. SQ-MELT-08 closes as "by design, writeup fixed". **Built 2026-11-15** (no code change: salt dust's own click already places a layer; the Salt member's `tryMeltFromItem` is now only the dispenser path; salt.md 7.1, melting.md rewritten). Verify SQ-PS15-01. |
| B2 | Dispenser, salt dust: on air (nothing to melt in front) it places a layer; facing a frozen block it makes **one melt action** and spends 1 salt. | Today the dispenser behavior is registered for `salt:salt` and returns unless the stack is in `#forge:salts`, so it does nothing. Register it on the real item, route to `meltStep`. **Built 2026-11-15**: Salt member `Melting.SaltItemDispenseBehavior` (no `#forge:salts` gate; melt one stage or place a layer on air or onto a layer below 8, else drop), registered on `vintagedelight:salt_dust` by CD `salt/SaltLayerMelt` (common setup); `[salt.Melting] SaltItemMeltingEnabled` now governs this. Verify SQ-PS15-02. |
| B3 | A salt layer has a small chance, **configurable**, of disappearing when it melts a frozen block. | New key in the CD salt config, **default 5 percent** (owner, 2026-11-15). Applies only to salt layers: salt blocks that are not made of layers (Salt Block, Raw Rock Salt Block, other full blocks) never vanish. Backup in `configs_edited/`. **Built 2026-11-15**: `[salt.Melting] SaltLayerVanishChance = 0.05` (Salt member `Configuration`, mirrored); Common `Melting.setSaltMeltListener` (called after a salt source melts by random tick), CD `salt/SaltLayerMelt` removes one layer from a `SnowLayerBlock` salt source by chance (the last layer leaves air). Verify SQ-PS15-03. |
| B4 | A falling salt layer (layers obey gravity) that lands on a salt lick moves into the lick and refills it. | Lick refill path shared with B7. **Open, waiting for owner**: salt layers do not fall today (Vintage's `SaltLayerBlock` is a `SnowLayerBlock`, no gravity; a layer without support breaks on the next block update). Either make salt layers fall like sand (then the landing hook) or drop B4. Placed and dispensed layers on a worn lick already refill it (B7). SQ-PS15-04 waits. |
| B5 | A lick with salt layers on top pulls a layer from the top when it is licked, instead of shrinking the lick. | The lick size stays; layers are consumed first, top down. **Built 2026-11-15**: hearth `SaltLick.wear` takes one layer from the topmost salt layer resting on the lick (a stack of full layer blocks counts, top block first) before it wears a stage; players and animals both go through `wear`. Verify SQ-PS15-05. |
| B6 | New item **Salt Melt**: 64 uses, crafted from 2 salt dust and 2 rock salt; right click on any frozen block melts it for 1 durability; works in a dispenser. | Needs: item registration in the CD salt member, recipe through `global.NCH`, lang, JEI text, id via `global.item`, texture: the Salt mod's salt crystal texture (owner, 2026-11-15), copied as-is from `assets/salt/textures/item/salt.png` (human art, no edit; if the owner meant `block/salt_cluster.png`, switch) with a row in `texture_sources/sources.csv` and `python3 tools/texture_provenance.py`, changelog bullet. **Built 2026-11-15**: `salt:salt_melt` (Salt member overlay `ce/SaltMeltItem`, `ce/SaltMeltRegistry`; 64 durability; click and dispenser go through Common `meltStep`); recipe in CD `creativedelight/salt.js` (shapeless 2 salt + 2 raw rock salt); lang (overlay `en_us.json`, `replaced_resources`), JEI page `kubejs/assets/creativedelight/kubejs/salt/jei_info.js`, `i.salt_melt`. Texture: the item model points at upstream `salt:item/salt` in the same jar, so no file is copied and no `sources.csv` row is needed (plain upstream use). Verify SQ-PS15-07. |
| B7 | Salt dust refills a salt lick when dispensed onto it, placed by hand onto it, or reaching it as a layer above it. | One method, three entry points. Salt dust on a **full** lick places a layer (owner, 2026-11-15), the same layer that then refills it as it is used (B5). SQ-HH-474 (salt dust on an unworn salt block) stays open: assume a layer (B1) unless the owner says otherwise. **Built 2026-11-15**: hearth `SaltLick.refill` is the one refill (hand, dispenser, layer above via `LickableSaltBlock.neighborChanged`); the dispenser also refills waxed stages now (hand already did) and puts a layer on top of a full lick (`SaltLick.addLayerOnTop`); a hand click on a full lick is salt dust's own layer place. Verify SQ-PS15-06. |
| B8 | **Investigate** frozen decor: do frozen bricks, icy and snowy blocks from Immersive Weathering and other mods exist in the pack, and what should melt them. | Report first (dump the block tags, grep the Immersive Weathering and Architect's Palette ids, read `[melting]` tags). Then decide per block: melt tier, melt into what, whether salt, fire or both. No code before the report. **Reported 2026-11-15** (B8 findings below); build rows wait on owner questions 1 to 4. |
| B9 | Drop `architects_palette:glowstone_cage_lantern` from the fire tag. | Closes SQ-MELT-10. Data edit plus tag helper; changelog line already says lanterns melt, so adjust wording if the list names it. **Built 2026-11-15**: dropped from `tags/melting_tags.js` and `b.melt_fire_ap_glowstone_cage_lantern` removed from the block dict; melting.md table updated. No changelog line named it. Verify SQ-PS15-09. |

Build order inside B: B9 (tiny), B1/B2/B3/B7 (one melt-core pass), B4/B5 (lick), B6 (item, after the texture is
supplied), B8 (report, then rows). Jars: Creative Common (Melting) and Creative Delight (salt member); both need a
build, install and restart. Writeups to update: salt.md, plan_hearth_crow_salt.md, the Melting section of
immersive_weathering or wherever melting is documented (grep "meltStep").

### B8 findings

Reported 2026-11-15 (investigation agent; no code). Sources: `kubejs/exported/display_names.json` (14,259 blocks, every
id and name grepped for frozen, icy, snowy, ice, frost, snow, permafrost, glacial, igloo), the block tag dumps, the
Immersive Weathering 1.2.10, Architect's Palette 1.3.5 jars, `melting_tags.js`, `Melting.java`, `[melting]`.

- Today: `meltable` = ice, frosted ice, snow, snow block, powder snow, Rare Ice; `meltable_slow` = packed and blue ice.
  The product is a `slowSteps` row (slow tier only, the target's **default** state), else water for `#minecraft:ice`
  outside ultra-warm, else air. There is no "melts into X" for the fast tier and no property copy, so a stair, slab or
  wall cannot keep its shape.
- Immersive Weathering melts its own blocks already: snowy blocks un-snow on a random tick when a neighbor has block
  light above 11 and on water or lava contact (they drop a snowball); frosty blocks un-frost from block light; thin ice
  natural melting is off in our IW config. This runs whatever `vanillaLightMelting` says (the toml comment notes it).
- No mod in the pack has "frozen bricks" as such; the nearest are IW's snow bricks and snowy stone bricks (rows 1, 6).

| # | Blocks | Mod | n | Tier | Melts into | Salt / fire |
|---|---|---|---|---|---|---|
| 1 | snowy stone, snowy cobblestone, snowy stone bricks (each: block, slab, stairs, wall, vertical slab), snowy chiseled stone bricks | IW | 16 | fast | the plain counterpart, same shape and facing (IW's own un-snow map) | both |
| 2 | frosty glass, frosty glass pane, frosty grass, frosty fern | IW | 4 | fast | glass, glass pane, grass, fern | both |
| 3 | frost (the wall/floor frost layer) | IW | 1 | fast | air | both |
| 4 | thin ice | IW | 1 | fast | water source (air when ultra-warm), as ice; not in `#minecraft:ice`, so it needs a product row | both |
| 5 | icicle | IW | 1 | fast | air | both (fire is the one that matters: icicles hang from ceilings, salt reach is 1) |
| 6 | snow bricks (block, slab, stairs, wall, vertical slab) | IW | 5 | fast; also `ultrawarm_instant` | air, as snow block | both (owner question 1) |
| 7 | polished packed ice (block, slab, stairs, wall, vertical slab), chiseled packed ice, packed ice pillar | AP | 7 | slow | full blocks: ice (as packed ice); shaped ones: air (owner question 3) | both |
| 8 | coarse snow (crafted 2 snow block + 2 gravel = 4) | AP | 1 | fast | gravel | both (owner question 4) |

Left out (not frozen, or must not melt): Quark permafrost family (10, a stone), IW permafrost and cryosol (soils),
Blocky Siege permafrost and frost bombs, SecurityCraft reinforced ice / packed / blue / snow block and powder snow
cauldron (reinforced blocks never melt), Philips Ruins frozen prismarine, bricks, dark prismarine and sea lantern (ruin
decor; owner question 2), Upgrade Aquatic polar kelp family, Botania bifrost, Brewin' ice crate, Handcrafted frozen
bench, Engineer's Decor frost sign, Fairy Lights icicle and snowflake lights, Fantasy Furniture snowballs, Neapolitan
and Delightful ice cream blocks, vanilla powder snow cauldron. Side finding: The Salt's own `salt:meltables` lists
`securitycraft:reinforced_snow_block`, so The Salt's item click (not our tags) melts a reinforced block.

What the build needs (Creative Common `Melting`, `melting_tags.js`, block_dict keys, `[melting]`; rows SQ-PS15-08):
(a) a product list for the fast tier ("from=to", like `slowSteps`) and, for both lists, copy every property the two
states share (facing, half, shape, slab type, wall sides, waterlogged, axis); (b) tags: rows 1 to 6 and 8 to
`meltable`, row 7 to `meltable_slow` with `slowSteps` rows for the three full blocks, row 6 (and row 8 only if it gives
gravel there too) to `ultrawarm_instant`; (c) `extraRandomTickBlocks`: rows 6 to 8 (13 ids); IW's snowy and frosty
blocks tick already (their own un-snow tick); check thin ice, icicle and frost at build. IW's own light melting stays.

Owner questions: (1) Snow bricks: melt like snow block, or stay as a building block? (2) Philips Ruins frozen
prismarine: leave out (proposed) or a slow step to prismarine? (3) AP shaped packed ice (slab, stairs, wall, vertical
slab): air (proposed) or water? (4) Coarse snow into gravel (proposed) or air?

## C. Hops, vines and wild crops

| Step | What | Gate |
|---|---|---|
| C1 | **Investigate original wild hops vs current** (first). The upstream Hearth and Harvest feature, its trunk search, rarity and biomes, against our `wild_hops.json` and `TrunkVineFeature.java`. Report the differences and what the original rate really is in forests. | Gates C3. **Reported 2026-11-15** (C1 findings below; C3 target proposed). |
| C2 | Hops vine block replacement (SQ-HOP-04): an age-1 vine must refuse placement of any item on it (use returns CONSUME or the block is not replaceable). Do the same audit for **every vine crop** (grapes, hops, any other vine-style crop in the pack: list them from the block tags) so none can be replaced by placing a block into it. | Independent of C1. **Built 2026-11-15**: tag `#creativedelight:crops/vine_crops` (CD `crop_tags.js`: hops vine, jungle grape vines red and white, Neapolitan vanilla vine and vanilla vine plant, Upgrade Aquatic mulberry vine) and CD mixin `minecraft.VineCropNoReplaceMixin` (`BlockStateBase.canBeReplaced(BlockPlaceContext)` false unless the item places the same block). Audit: hops and jungle grapes extend `VineBlock` (replaceable while a face is free); vanilla and mulberry vines override `canBeReplaced` to fall back on their material; grape bushes (sweet berry copies), grapevine stems and lattices (wood), Farmer's Delight and Supplementaries tomatoes, cucumbers (crop blocks) are not replaceable and stay out. Verify SQ-PS15-10. |
| C3 | Fix the wild hops trunk search (`TrunkVineFeature`: 24 columns, radius 6, times rarity 96 gives far fewer than the planned 1 in 96 chunks). Target rate comes from C1. | After C1. **Built 2026-11-15** to the C1 target: rarity 96 kept; `TrunkVineFeature` scans all 289 columns within radius 8, a trunk is a column whose `MOTION_BLOCKING_NO_LEAVES` top block is a log, walked down to its base, at least 3 logs with an empty block beside it; one picked at random (next tried if no vine fits). C4 heightmap fix not applied (owner). Verify SQ-PS15-11 (pass marks below). |
| C4 | **Investigate missing wild crop placement and missing crops**: wild carrots and adzuki 0 in forest boxes though forest is in both crop tags; wild bell peppers 0 in jungle (SQ-RD-26); then sweep every wild crop rule for a census of 0. Output: a table (crop, rule, expected biomes, observed count over N chunks, cause). Fix rows follow from the table. | Report before any rarity change. Method: the 240-block box census used in r1115 (batch boxes are 240, not 256, because of the forceload cap). **Reported 2026-11-15** (C4 findings below, static); live checks SQ-PS15-12b, -12c. |

### C1 findings

Reported 2026-11-15 (investigation agent; no code). Upstream source:
`DecompiledMods/hearthandharvest-1.21.1-1.4.1-src` (`data/hearthandharvest/neoforge/biome_modifier/wild_hops.json`,
`worldgen/configured_feature/patch_wild_hops.json`, `worldgen/placed_feature/patch_wild_hops.json`).

| | Hearth and Harvest 1.4.1 (upstream) | Ours (CD hearth member) |
|---|---|---|
| What | `hearthandharvest:wild_hops`, a ground wild crop (WildCropBlock, tall grass properties); no trunk, no vine | `hearthandharvest:hops_vine` hung on one tree trunk, age 1 to 3, 2 to 5 blocks (needs Vinery) |
| Feature | `farmersdelight:wild_crop`: tries 64, xz 6, y 3; FD runs 64 floor tries (coarse dirt), 32 primary (wild hops on air over `#minecraft:dirt`), 64 secondary (short grass) | `hearthandharvest:trunk_vine` (`TrunkVineFeature`): 24 random columns within radius 6, trunk base searched from origin y -6 to +10, trunk at least 3 logs, one trunk per hit |
| Placement | rarity 120, in_square, heightmap `MOTION_BLOCKING`, biome, `is_overworld` | rarity 96, in_square, heightmap `MOTION_BLOCKING_NO_LEAVES`, biome |
| Biomes | `#minecraft:is_forest` minus lush caves and mushroom fields | rule `temperate grassy forest`: 18 biomes (5 vanilla forests, 13 Terralith) |

- Real upstream rate: one attempt per 120 forest chunks, but `MOTION_BLOCKING` is updated as trees are placed (it is a
  post-features heightmap), so wherever leaves cover the origin column the patch starts on the canopy and its y spread
  of 3 never reaches the ground. Only openings succeed: about 1 patch per 200 to 300 oak or birch forest chunks, near 0
  in dark forest, each patch about 3 to 6 wild hops. Static estimate (no upstream census exists); the same mechanism
  gave 0 melons in 98 jungle chunks here (SQ-WCD-02).
- Ours: the origin is on the ground (NO_LEAVES), so the canopy is not the problem; the trunk search is. Trunk columns
  are 1 to 2 percent of a forest's columns (r1115 site 2: 5,206 oak and birch logs over 256 chunks, about 4 trees per
  chunk), so 24 random columns find one about 20 to 35 percent of the time at best; measured 1 of 25
  (`/place feature` at 25 forest points, SQ-HOP-06). Effective rate about 1 in 2,400 forest chunks; observed 0 over about
  740 forest chunks (expected about 0.3 at that rate, consistent).
- **C3 target** (proposed; the rarity is the owner's 2026-11-14 figure, kept): rarity **96** unchanged; the search
  becomes exhaustive instead of sampled: every column of the **17 x 17 square (radius 8)** around the origin; a trunk
  is a column whose `MOTION_BLOCKING_NO_LEAVES` top block is in `#minecraft:logs`, walked down to its base (no fixed
  -6 / +10 window); keep trunks of at least 3 logs with an empty face beside them; pick one at random; vine length 2 to
  5 and age 1 to 3 as now. Pass marks: `/place feature creativedelight:wild/wild_hops` succeeds at 22 or more of 25
  forest points (was 1); census about 1 hops tree per 100 forest chunks, so 4 or more over the three r1115 sites
  (SQ-PS15-11, -11b). Upstream parity instead (rarity about 240, the upstream effective rate) only if the owner prefers
  it.

### C4 findings

Reported 2026-11-15 (investigation agent; static, from r1115 data and code; no server booted).

Main cause: **`MOTION_BLOCKING` on a forest or jungle canopy.** Features added by Creative Delight's wild crop rules
are appended after the biome's trees, and `MOTION_BLOCKING` (also `OCEAN_FLOOR`) is updated as each tree is placed,
so a patch whose origin column is under leaves starts on the canopy; FD wild crop patches and random patches spread only
3 in y and never reach the ground. The `_WG` heightmaps (`WORLD_SURFACE_WG`, `OCEAN_FLOOR_WG`) are frozen before
features (terrain only) and `MOTION_BLOCKING_NO_LEAVES` skips leaves: those land on the ground. Verified: FD 1.19.2
carrots use `PlacementUtils.HEIGHTMAP` = `MOTION_BLOCKING` (bytecode plus 1.19.2 mappings); Rustic's bell pepper,
coffee and cotton JSONs say `MOTION_BLOCKING`; Neapolitan adzuki uses `HEIGHTMAP_WORLD_SURFACE` = `WORLD_SURFACE_WG`
(canopy-proof).

Ruled out: biome tags (r1115 probe: carrot tag 25 biomes, adzuki 18, both with forest; bell pepper tag 11 with every
jungle); the Rustic chance (`ChanceFilter` holds 15 for peppers and coffee in code; `config/rusticdelight-common.toml`
is a leftover upstream file the port never reads); peppers versus coffee (same block class, rarity, heightmap and
ground rule: the jungle split 0 versus 5 is one coffee patch in a clearing, chance). Also: the r1115 boxes were centered on
`/locate` results and nobody counted their biome make-up, so "N chunks" below is an upper bound on tag chunks.
`crop_worldgen.json` (2026-11-14) lacks the four Rustic features although the generator knows them: stale export, rerun
`gen_crop_worldgen_table.py`.

| Crop | Rule (where) | Heightmap | Observed r1115 (N chunks) | Expected | Cause / risk |
|---|---|---|---|---|---|
| Carrot | `temperate grassy !swamp` (25) | MOTION | 0 (site 2: 256, site 3: 225) | 4.0 patches at 1 in 120 if every chunk were in the tag | canopy (forest part of the rule); plains and meadow unaffected |
| Onion, broccoli, cauliflower, garlic | same as carrot | MOTION | not censused | | canopy in the forest biomes of the rule |
| Adzuki | `temperate grassy forest` (18) | WORLD_SURFACE_WG | 0 (site 3: 225) | 1.8 at 1 in 128 (P(0) about 17 percent) | not canopy; 0 is within chance in one small box of unknown biome mix: needs the census (SQ-PS15-12c) |
| Bell pepper | `warm wet !cliff` (11) | MOTION | 0 (36 + 121 jungle; r1102b also 0) | 10 patches at 1 in 15 | canopy |
| Coffee (Rustic) | `warm wet !cliff` | MOTION | 0 (36), 5 blocks (121) | 10 patches | canopy (one patch in a clearing) |
| Cotton | `warm dry grassy` | MOTION | 7 blocks (81 savanna) | 0.8 patches at 1 in 96 | fine (sparse acacia) |
| Cocoa | `warm wet !cliff` minus jungles (CD jungle tree) | OCEAN_FLOOR + trees | 48 (121) | | fine |
| Wild hops | `temperate grassy forest` | NO_LEAVES | 0 (about 740) | | trunk search (C1, C3) |
| Mini melon | `warm wet !cliff` | MOTION | not censused | 1 in 35 | canopy (high risk: jungle only) |
| Rice | `warm wet !cliff` | MOTION | not censused | 1 in 20 | canopy over jungle water; open rivers fine |
| Tea | wet grassy, swamp, jungle, cherry, dark forest | MOTION | not censused | | canopy in jungle, dark forest, swamp |
| Cucumbers | wet grassy and muddy, swamp, cold and temperate grassy | MOTION | not censused | | canopy in jungle, swamp and forest parts |
| Oats, potato, salmonberry, raspberry, pumpkin | cold or temperate grassy (taiga, forest parts) | MOTION | not censused | | canopy, partial (spruce cover) |
| Tomato, sweet potato, zucchini, corn, peanuts, giant bell pepper | `warm dry grassy` | MOTION | not censused | | low (savanna) |
| Cabbage, beetroot, turnip, sugar cane, pineapple, hamimelon, cantaloupe | beach, cliff, river, desert, badlands | MOTION | not censused | | none (no canopy) |
| Sweet berries, strawberry, mint, vanilla, blueberry, cranberry, lemon | | WORLD_SURFACE_WG | not censused | | canopy-proof |
| Melon, sea grape | | OCEAN_FLOOR_WG | not censused (SQ-WCD-02 still queued) | | canopy-proof |
| Fruit trees (Fruits Delight `tree/*`) | jungle, swamp, birch, cold and snowy forest | OCEAN_FLOOR | not censused | | canopy, partial (rare by design) |
| Rubber tree | jungle | NO_LEAVES | not censused | | canopy-proof |
| Grapes, Vinery apple and cherry trees, bananas | | not checked (Vinery port, Neapolitan Java) | not censused | | |

Fix rows that follow (not built; owner to confirm): give every surface wild crop feature that the rules add to
forest, jungle, swamp or taiga biomes a canopy-proof heightmap (`MOTION_BLOCKING_NO_LEAVES`, as rubber and hops),
the way `wild/melon` was fixed on 2026-11-03: JSON features (Rustic, Veggies, Vintage members, CD `wild/*`) by editing
their `heightmap`; Java-registered ones (FD, Delightful, Farmer's Respite, Corn Delight; no placed-feature JSON in
their jars) by a CD `wild/*` copy with the new heightmap (the cabbage pattern) or a placement override. No rarity change before the census (SQ-PS15-12b, -12c).

## D. Smaller fixes

| Step | What | Notes |
|---|---|---|
| D1 | No pie placeable: vanilla and pack pies (and the Pies family) cannot be placed as blocks. Lunchbox filter limited to edible items (a food check, not a tag), so vanilla pumpkin pie stays out as long as it is not food in this pack. | SQ-FDI-04. Lunchboxes writeup and tooltip text. **State 2026-11-15: lunchbox half BUILT** (Creative Delight build only, not installed): `mixin/solapplepie/LunchboxFoodOnlyMixin` puts the `isEdible()` check (already on the screen slot) on the box's item handler, which only refused another lunchbox, so a script insert stored the pie; tooltip `Food slots: N`; lunchboxes.md 3.9, LB-15; SQ-PS15-13; changelog family Lunchboxes. **Pie half: open, waiting for owner.** Pies place today through their own mods (Delightful's pumpkin pie block, Farmer's Delight pie blocks, the placeable lists of tools/placeable_catalog.py), and the Food Dictionary counts bites of placed pies. Question: does "no pie placeable" mean (a) pies never place as blocks (then how are they eaten: slices from a cutting board only, or pies become food), or (b) only that pies cannot be put in a lunchbox (done by the food check)? No pie placement code changed. |
| D2 | Heap spill (SQ-VH-06c): needs an in-game look at the border of a heap and a warped forest. Most likely fine; no code until seen. | Queue CQ-PS15-03: screenshots from a flesh heap that borders warped forest. **State 2026-11-15: confirmed queued** (CQ-PS15-03 present in scratch_queue.md); no code. |
| D3 | Power Grid arm pickup of circuit components set to **1** per grab (wire connector, resistor and the rest of the assembly parts). Fixes the stall at Assembly 1/3 (SQ-PWG-82). | `ArmInteractionPointMixin` / `ArmBlockEntityMixin`; TODO `#83`; rerun SQ-PWG-82 with the stacks left in the depots. **State 2026-11-15: BUILT** jar `jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-19.jar` (build bumped -18 to -19; not installed): `ArmBlockEntityMixin` caps Create's `getDistributableAmount` at 1 when the input stack is a circuit component (`ComponentRegistry.getComponentId` not null). Before the circuit reaches the belt window the arm still drops single parts on the belt (upstream fallback). PORT_NOTES Arm mixins row. SQ-PS15-14 reruns SQ-PWG-82. |
| D4 | Acid recipe from rotten flesh and spider eyes **in water** (SQ-PWG-80b: water-first ordering let CD water_salt and Create: The Salt use up the flesh without producing acid). | Recipe through `global.NCH`; the exact machine and ratios come from the existing acid recipe in `#82`; confirm by reading it before editing. **State 2026-11-15: BUILT, script-only (`/reload`).** Existing #82 recipe read: heated mixing, redstone + blaze powder + rotten flesh + 250 mB water -> 250 mB acid (kept). Added `powergrid_acid_in_water`: rotten flesh + spider eye + 250 mB water -> 250 mB acid, **no heat** (same machine and amounts; unheated because a heated recipe still loses water poured first to Creative Delight's heated `water_salt`, the reason for D4; Create: The Salt's flesh mix needs salt, so it does not match). Owner to confirm the no-heat choice. SQ-PS15-15 reruns SQ-PWG-80b. |
| D5 | Upgrade Aquatic guard: `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json` and `placed_feature/trees_river.json` must not load when Upgrade Aquatic is absent (SQ-DEP-09b extra, "Unknown registry key"). | A guard so the DEP-09b drop test (without upgrade_aquatic and alexsabnormalities) boots clean. **State 2026-11-15: BUILT** (Creative Worldgen build only, not installed): all 7 `kubejs/data/upgrade_aquatic/worldgen` JSONs (river_tree, trees_river and the 5 driftwood placed features, which name `upgrade_aquatic:driftwood` and would fail the same way) moved to `development/creativeWorldgen/src/main/resources/packs/upgrade_aquatic/data/upgrade_aquatic/worldgen/`, added by `compat/ModDataPacks` only with Upgrade Aquatic. UA registers those ids in Java, so the pack's JSON still overrides them. `separated_leaves/river.json` stays in kubejs/data (not a registry). SQ-PS15-16. |

### D1b. Owner decision 2026-11-16: lunchbox accepts items by an edibility check (planned, not built; D1 mixin already in place)

Pies stay placeable as blocks. The rule for a lunchbox slot is the existing `ItemStack.isEdible()` check in
`LunchboxFoodOnlyMixin` (Creative Delight, `mixin/solapplepie/`), already built under D1. The placeable-tag and explicit
pie-and-cake-list ideas are dropped. State: **planned, not built; D1 mixin already in place** (D1's changelog, lang, queue
row SQ-PS15-13 and TODO #15 already describe this rule).
- What it means for placeable food (dump `kubejs/exported/food/placeable.json`, 2026-11-16): whole pies and cakes are
  block items with no food properties, so `isEdible()` is false and they are refused. This covers vanilla pumpkin pie,
  Farmer's Delight apple and chocolate pies, cheesecakes, Neapolitan, Rustic Delight and Hearth and Harvest cakes and
  pies, Mulberry Pie. Slices and plain-item pies (chorus flower pie, durian pie, golden pumpkin pie, pancakes) are edible
  and are accepted. Exceptions that pass because they are edible block items: Fruits Delight mangosteen cake, jellies,
  jam jars, Vinery wines, waffle, crops. So the owner's earlier wish (a food that is only a block is refused) holds for
  whole pies and cakes, with those exceptions.

## E. Owner decisions of 2026-11-16 (plans only, "planned, not built")

Nothing below is built. Order: E1 (B8 melt build), E3 (one model line), E2 (C4 switch), E4 (falling salt layers, last).

### E1. B8 build (Creative Common `Melting`, Creative Delight salt; rows SQ-PS15-08)

Decisions: snowy cobblestone and snowy stone bricks (and the rest of B8 row 1) melt back into their plain stone variant at
the fast-tier rate (`meltChance`; owner confirmed 2026-11-16, question closed); snow bricks (row 6) melt like snow
(air) at 0.25x the rate; frozen prismarine (Philips Ruins) never melts; full packed ice blocks (Architect's Palette
polished, chiseled, pillar) melt into vanilla ice, shaped ones (slab, stairs, wall, vertical slab) into air; blue ice
melts into packed ice only beside lava, never from salt (or fire); coarse snow melts into gravel; the rest as the B8
proposal (rows 2 to 5: frosty glass, pane, grass, fern, frost, thin ice, icicle).
- Files: `melting/Melting.java` (new "melts into" list `[melting] meltProducts` for the fast tier, copying every shared
  block property for both lists; flags widen from byte to int for two new bits `sluggish` and `lava_only`; `targetTick`
  ignores salt and fire for lava-only blocks; `sourceTick` skips them; `meltStep` refuses them), `MeltTags.java` (tags
  `melting/sluggish`, `melting/lava_only`), `CommonConfig` `[melting]` (`meltProducts`, `sluggishMeltFactor = 0.25`, longer
  `extraRandomTickBlocks`, `slowSteps` rows for the three full packed ice blocks), `kubejs/data/creativecommon/kubejs/tags/melting_tags.js`,
  `block_dict.js` keys, `configs_edited/root_config/creativecommon-common.toml`, melting.md, salt.md.
- Blue ice check: The Salt's own `salt:meltables` tag (upstream; feeds `SaltItem.useOn` and the no-Common fallback) lists blue ice
  and `securitycraft:reinforced_snow_block`; remove both by a tag edit in the CD salt scripts, and keep `meltStep` (dispenser,
  Salt Melt) refusing lava-only blocks. Packed ice keeps its salt rule.
- Snow bricks also go in `ultrawarm_instant`; coarse snow does not (gravel, not air). Thin ice, icicle and frost random
  ticking is checked at build.
- Rows to write later: per-block melt rows under SQ-PS15-08, salt-on-blue-ice refusal, lava-only, shape kept (stairs, slab
  facing). Changelog bullets in the Melting family. State: planned, not built.

### E2. C4 fix: canopy-proof heightmap for wild crops, config switch, default off

**Built 2026-11-17 (lane M2), not installed:** CD mixin `HeightmapPlacementCanopyMixin` (redirect of the heightmap read in `getPositions`, `PlacementContext.topFeature()` checked on 1.19.2 Mojmap), `crops/CanopyProofHeightmap`, tag `creativedelight:canopy_proof` in the CD jar, key `[worldgen] wildCropCanopyProofHeightmap = false` (mirrored); crops.md 2.8. Assumed: the tag also takes the Vintage, Delightful, Respite and Corn Delight patches of the C4 table (harmless where no leaves). Rows SQ-PS15-28, -29.

- What: a switch that makes the surface wild crop features of forest, jungle, swamp and taiga rules use
  `MOTION_BLOCKING_NO_LEAVES` instead of `MOTION_BLOCKING`. Placed-feature JSON cannot read a config, so the switch is a
  small hook on the heightmap placement modifier of the `creativedelight:wild/*` placed features (mixin or swapped modifier
  type reading the key). Key `[worldgen] wildCropCanopyProofHeightmap = false` in `creativedelight-common.toml` (CDConfig),
  mirrored to `configs_edited/root_config/`.
- Files: Creative Delight `CDConfig` and the hook, `kubejs/data/creativedelight/worldgen/placed_feature/wild/*.json` (only if the
  modifier type is swapped), vegetation-worldgen-system writeup, changelog bullet.
- Rows to write later: SQ-PS15-12b and -12c at both settings (off: today's counts; on: carrot, bell pepper, coffee, mini
  melon census over the same boxes, seed 8675309). Only new chunks are affected. State: planned, not built.

### E3. B6 icon change

Switch the Salt Melt model `layer0` from `salt:item/salt` to `salt:item/raw_rock_salt` (upstream, used as is; no copy, so
no `sources.csv` row). Files: `development/creativeDelight/members/salt/src/main/resources/assets/salt/models/item/salt_melt.json`,
salt.md 7.2, SQ-PS15-07 icon text. TODO row in family `tooling/scratch-1115-follow-ups`: new own texture for Salt Melt (owner's
tools only, per the texture rule). State: planned, not built.

### E4. B4 build plan: salt layers fall (owner reversed 2026-11-16; planned, not built)

- Block: give Vintage's `SaltLayerBlock` (a `SnowLayerBlock`, no gravity) falling behaviour: on `onPlace`, `neighborChanged`
  and `tick` check the block below (`FallingBlock.isFree`); if free it becomes a vanilla `FallingBlockEntity` carrying its
  layer count. Edit in Vintage member `block/custom/SaltLayerBlock.java`; ids and blockstates unchanged.
- Landing and stacking: onto another salt layer, merge (sum capped at 8, remainder stays as a layer above); onto a full block, a
  layer; if it cannot place (spot taken, no support), drop one `salt_dust` per layer (item-drop fallback, never deleted).
  `canBeReplaced` stays so a layer can land in plants.
- Edge cases: pistons move the block, the moved layer re-checks support next tick; water and fluids replace it as with sand
  (dust dropped, not lost); chunk load: an unsupported layer in a freshly loaded chunk falls on its first update; the entity
  saves and reloads like sand. Licks, stairs, slabs and fences count as supported (top-face check), so layers on licks never fall.
- Lick hook (B7 path): when the landing block is a lick stage (`SaltLick.isLickable` or waxed), call
  `SaltLick.refill(level, pos, state, layers)` through a small new `SaltLick.acceptFallingLayer`, leftover layers stay on the
  full lick, refill sound. Files: Vintage `SaltLayerBlock`, hearth `SaltLick`.
- Config: `[salt] saltLayersFall = true` (`creativedelight-common.toml`, mirrored to `configs_edited/`) so a save with floating
  salt can switch it off. Warranted: cheap, and protects old saves.
- Rows to write later: SQ-PS15-04 rewritten (layer dropped on a lick from 3 blocks), layer over a gap falls and lands, 1+1 stacks
  to 2 and 7+3 leaves 2 on top, landing in water or a taken spot drops dust (count kept), piston push, chunk reload with a
  floating layer, switch off keeps floating layers, TPS watch with 200 layers falling. Changelog bullet.
- Order: after E1 to E3; about one day with tests.

## G. Scratchpad write-up of 2026-11-16 (planned, not built)

Source: `todo/scratchpad.md` section "Write up scratchpad" (misc, salt, layers, raspberry pie), owner notes of
2026-11-16. Nothing below is built. Every step lists the facts it was checked against (2026-11-16), then its holes.
Decisions are in G6; the owner's 2026-11-16 answers produced G7, which supersedes the G1 to G5 rows it names. TODO rows: `tooling/scratch-1115-follow-ups
#20` to `#24`. Test rows are written with the build (SQ-PS15-17 onwards), not now.

Order: G1 (melting, one Common pass, after E1 because it widens the same flags and config), G2 (salt redesign, after
G1 and replacing parts of B4/B5/B7/E4), G3 (small independent fixes, any time), G4 (layers, joins plan_post_scratch_1115b
F), G5 (Raspberry Pie, client only, independent).

### G1. Melting additions (Creative Common `Melting`, `melting_tags.js`, `[melting]`)

Facts: magma is already a fire source. It gives light 3, so it is **source-driven**: its own random tick melts targets
within `fireRange` 3 (the startup log lists `minecraft:magma_block (light 3)`). Snow layers melt whole: `meltedState`
returns air for the whole stack. `meltChance` is 0.4 and `slowMeltChance` 0.05, one roll per random tick. B8 left IW
permafrost and cryosol out as soils; E1 makes blue ice lava-only.

| Step | What | Notes |
|---|---|---|
| G1a | Global melt speed, configurable, default **5x**. | `[melting] meltSpeed = 5.0`. A chance cannot be multiplied past 1 (0.4 x 5 = 2.0), so it has to be extra rolls per random tick or a chance clamp (decision G6-1). Covers fire, lava, salt and the E1 sluggish factor; check that B3's salt-layer vanish chance per melt is not multiplied by accident (5x more melts already spend 5x more salt layers). |
| G1b | Magma: melts quickly, and snow never forms on the block above a magma block or on a path laid over it (magma, then a stone path block, then no snow on the path). It also stops nearby blocks turning snowy. | Melting already covers "melts quickly", once G1a and a per-source factor exist (`[melting] magmaMeltFactor`, G6-2). New: a hook on precipitation snow placement (`ServerLevel.tickChunk` snow placement / `Biome.shouldSnow`) that refuses a position with magma 1 or 2 blocks below. "Snowy" covers the vanilla `snowy` property on grass, podzol and mycelium and IW's snowy stone conversion. Reach and which forms are covered: G6-3. Ice freezing over magma: G6-3. |
| G1c | Snow and snow layers do not melt while resting on blue ice or permafrost. | Target-side check of the block below, in `targetTick`, `sourceTick` and `meltStep`. Which permafrost (IW soil, Quark stone, Blocky Siege) and whether salt, the Salt Melt and ultra-warm instant melting also stop: G6-4. |
| G1d | IW cryosol melts like packed ice (slow tier, salt, fire and lava) into dirt; IW permafrost melts like blue ice (E1: lava only) into dirt. | Reverses B8's "left out (soils)". `slowSteps` rows `immersive_weathering:cryosol=minecraft:dirt` and `immersive_weathering:permafrost=minecraft:dirt` (or permafrost to cryosol, G6-5), tags `meltable_slow` and E1's `lava_only`. **Perf:** both are common worldgen soils in cold biomes, so they need `extraRandomTickBlocks` (IW's own blocks may not random tick), and every exposed permafrost face then rolls each tick. Measure with the perf suite before release. G1c and G1d together mean snow sits on permafrost forever while the permafrost itself only goes near lava, which is consistent. |
| G1e | Layered snow melts one layer at a time. | `meltedState` for a `SnowLayerBlock` with layers > 1 returns layers - 1. Since 1115b F's open notes said "may already exist; check first": checked, it does not. Decision G6-6: does a Salt Melt use or salt dispenser `meltStep` remove one layer or the whole stack? Also: does a layer count of 8 that has turned to powder snow (G4) melt back to 7 layers or to air? |

### G2. Salt redesign (Creative Delight salt, Vintage and hearth members; lang; `salt.js`)

**Built 2026-11-17 (lane M2), not installed** with G7.6, G7.7 and the lick part of G7.5: salt.md 8, rows SQ-SALT2-10 to -18, CQ-SALT2-01. Assumed: Vintage's merged `salt_block` is named Salt Dust Block (it follows the block it merges into, not Chiseled); Rock Salt on a full lick does nothing; natural Rock Salt Blocks (caves, beaches) are licks too (G6-10 accepted); the JEI page reads the loot tables only when the server runs in the same process and shows 8, 5, 3 on a dedicated server's client. Open: 1115b `#13` cause is in Common `Melting` (target melted before the salt listener).

Facts (2026-11-16): the lick chain is `salt:salt_block` (stage 0) -> lightly, well, heavily licked (hearth `SaltLick`,
4 stages; the last wear removes the block and drops 1 salt). The licked stages already look like rock salt (changelog:
"Licked salt blocks are rock salt layers 12, 8 and 4 pixels high"). Refill today is salt dust (hand, dispenser) and salt
layers above (B7). Licking eats layers on top first (B5). Names: `salt:raw_rock_salt` "Raw Rock Salt",
`salt:raw_rock_salt_block` "Block of Raw Rock Salt", **`salt:rock_salt_ore` is already "Rock Salt"**,
`salt:salt_block` "Salt Block", `vintagedelight:salt_dust` "Salt", `salt:salt_lamp` "Large Salt Lamp" (namechanges.js)
next to Vintage's "Salt Lamp" and its 16 colored lamps. Recipes (`creativedelight/salt.js`): raw rock salt -> 4 salt;
heated mixing 1000 mB water -> 2 salt (`water_salt`); Large Salt Lamp = 7 Salt Block + glowstone + deepslate tiles.

| Step | What | Notes |
|---|---|---|
| G2a | Renames: Raw Rock Salt -> **Rock Salt**; Block of Raw Rock Salt -> **Rock Salt Block**; Salt Block -> **Salt Dust Block**; licks -> "{Waxed} {Lightly/Well/Heavily} Licked Rock Salt Block" (waxed stage 0 "Waxed Rock Salt Block"); Vintage salt decor (Salt Bricks, Salt Brick Slab/Stairs/Wall, Salt Pillar) -> "Smooth Salt ..."; Mixed Salt names unchanged. | Lang only, ids unchanged (CD lang overlays and namechanges.js). **Clash:** the ore is already "Rock Salt" (G6-7). "Salt Dust Block" is made of an item named "Salt" (G6-8). The Vintage decor list and whether "Smooth Salt Block" and the lamps are in it: G6-9. JEI info text, salt.md, the changelog and tooltips that name these blocks all need the new names. |
| G2b | The lick's root block becomes the **Rock Salt Block**; a Rock Salt Block is **4 Rock Salt**. Rock Salt (the item) refills a lick by hand or dispenser. **Salt layer refilling is removed.** | `SaltLick.SALT_BLOCK_ID` -> `salt:raw_rock_salt_block`, `isSalt` -> rock salt item, waxing applies to the new root. 4 stages = 4 Rock Salt, so one wear = one Rock Salt and one refill = one stage. Check the current Rock Salt Block recipe (upstream may be 9) and change it to 4 both ways. **Conflicts with built and planned steps:** B7 (salt dust and layer refill, built), B5 (licking eats layers on top first, built), E4 (falling layers exist mainly to refill licks), and changelog bullets "A salt layer placed on a licked salt block refills it", "Salt in a dispenser restores a licked salt block", "Licking a salt block with salt layers on top eats the top layer". Decision G6-10 covers B5, E4, the last-wear drop (salt or rock salt) and existing Salt Dust Block licks in worlds (they stop being licks). |
| G2c | Salt / acid: remove `water_salt` (heated water -> 2 salt); add heated water + Rock Salt Block -> Rock Salt Block + 2 salt. | Through `global.NCH` (`createMixing` with the block both in and out). **Integration with D4:** acid-in-water was made unheated only because heated `water_salt` used up water poured first. With `water_salt` gone that reason ends: keep D4 unheated, or make it heated like #82 (G6-11). Check that no heated recipe with flesh or redstone and a Rock Salt Block in the basin matches both. Amount: 1000 mB per 2 salt as now unless the owner says otherwise. Cauldron evaporation (8, 5, 3) is unchanged. |
| G2d | Large Salt Lamp takes the Rock Salt item instead of Salt Block, and a wooden slab instead of deepslate tiles. | Same 3x3 shape, `S` = rock salt, `D` = `#minecraft:wooden_slabs`. Only The Salt's lamp; Vintage's colored lamps are unchanged unless G6-9 says otherwise. |
| G2e | Salt Melt stacks to 16 when its uses match. | Vanilla never stacks a damageable item (`Item.Properties.durability` forces stack size 1). Move uses to an NBT counter with its own durability bar (`isBarVisible`, `getBarWidth`), max stack 16, so equal counters stack through NBT equality. **Hole:** using one item from a stack of 16 has to split it off (like a potion) or the whole stack shares one counter (G6-12). The dispenser path needs the same split. Existing Salt Melts with vanilla damage need a one-time convert on load (inventory tick). |
| G2f | Salt Melt and its mechanics apply to IW snowy blocks (the "snowy" forms that only exist under snow). | Covered by E1 row 1 once built: `meltStep` and the Salt Melt use the same tags. Add explicit rows (Salt Melt on snowy stone stairs keeps its shape) to SQ-PS15-08. Nothing extra to build. |
| G2g | Salt JEI: displayed yield follows the configured yield; refresh other out-of-date JEI tabs, salt's included. | **Which yield and which tab is not known** (G6-13): the CD salt config has no yield key; yields live in `salt.js` (4 per rock salt, 2 per 1000 mB) and the evaporation loot tables (8, 5, 3). JEI recipe pages already read recipes, so the stale one is probably an info page or The Salt's evaporation category. Needs a list of tabs from the owner, or a JEI sweep against the recipes as its own investigation row. |
| G2h | "Where did the Salt Melt texture come from?" Answered: the model points at `salt:item/salt`, and KubeJS assets outrank the jar, so it renders `kubejs/assets/salt/textures/item/salt.png`. That file is the pack's recolour (The Salt's shape, Vintage Delight's pink palette, `palette_swap.py` via `textureswapper/salt/make_salt.py`; its row is `kubejs/assets/salt/textures/*` in sources.csv). | B6 and E3 said "plain upstream use"; that is wrong for both `item/salt` and `item/raw_rock_salt` (both recoloured in kubejs/assets). Fix B6/E3 wording in salt.md 7.2 at build; no new sources.csv row is needed because the existing row covers the folder. |

### G3. Small fixes (independent)

**Built 2026-11-17 (lane M2), not installed:** CD `campfire/CampfireTorch`, `[campfire]` switches (mirrored), dragon armor 8/13/11/8 and Rare Ice `allowInsertingItemsToIce=false` (both mirrored). Assumed: a torch is any `TorchBlock` item but the redstone torch, "soul" read from the registry path; rain refuses only where `CampfireRain` would put the campfire out (not soul campfires, not on a fuel block); the tooltip's torch line is a second line ("A torch lights it"). Rows SQ-PS15-24 to -27, CQ-PS15-05.

| Step | What | Notes |
|---|---|---|
| G3a | Tooltip "Requires a spark or flame to light" on campfires that are placed unlit. | Unlit Campfire places them unlit (`defaultconfigs/unlitcampfire-server.toml`). Which blocks: `#minecraft:campfires` (vanilla, soul, Tinted Campfires) or every block Unlit Campfire affects (G6-14). Client tooltip script; also the Compat WTHIT campfire line (`WthitCampfire`) if the owner wants it there. |
| G3b | Torches light campfires. | Right-click handler (Common or Compat, Forge `RightClickBlock`): torch on an unlit, not waterlogged campfire sets `lit`. Holes: whether the torch is used up; soul torch only for soul campfires; refused while it is raining on an exposed campfire (CampfireRain would put it out next tick); dispensers; Unlit Campfire's burn timer state must be reset the way flint and steel does it (G6-15). The tooltip's "flame" then includes torches. |
| G3c | Dragon armor +4 points (2 armor icons), so a full set fills exactly 2 rows. | `config/dragonloot-common.toml [dragonloot.armor]`: 7/12/10/7 = 36 -> 40. `armorCap` (50) already allows it. Which piece gets what: G6-16 (proposal 8/13/11/8). Check that `upgraded_dragon_chestplate` reads the chest value. Mirror to `configs_edited/`. Changelog with the old values. |
| G3d | Rare Ice only from worldgen: items can no longer be put into ice. | `config/rare-ice.properties allowInsertingItemsToIce=false`, mirror to `configs_edited/`. Rare Ice already in worlds stays. Check any JEI or info text that describes inserting items. The loot-table investigation in the human notes is separate. |

### G4. Layers (Creative Compat pass-through and BlockRunner; joins plan_post_scratch_1115b F)

1115b F1 to F4 already hold: tag only snow, leaf piles and bamboo leaves; leaf layers never act whole; bamboo leaves out
of `runner.leaves`; snow layers out of `runner.snow`. Those are not repeated here. New from the scratchpad:

| Step | What | Notes |
|---|---|---|
| G4a | BlockRunner materials: salt layers, IW sand, red sand and ash layers, Supplementaries ash go to **dust**; snow layers to **snow**; fodder has no walk-through. | **Conflict with 1115b F3:** F3 keeps snow layers out of `runner.snow` to avoid a double slowdown, but "snow is snow material" puts them in. Pick one (G6-17); if both apply, the pass-through layer walk and the snow tier multiply. Fodder's material is not given (stays `soft` 0.88?). |
| G4b | Snow layers at 8 become powder snow. | Hook on snow layer growth (placement of the 8th layer, snowfall accumulation, Snow Real Magic-style mods if present). **Holes:** does vanilla `minecraft:snow_block` (worldgen-placed, everywhere in snowy biomes) count? If yes, every snowy biome turns into powder-snow traps (G6-18). Melting: powder snow is `meltable` and `ultrawarm_instant`, so G1e has to say what it melts to. |
| G4c | Snow and leaf layers above half height cut jump height; falling inside is slowed by the layer count. | Compat `entityInside` / jump hook. Numbers needed: jump factor and fall factor per layer (G6-19). Interacts with 1115b A (cobweb fall-distance reset): does slowed falling in layers reset or reduce fall damage? |
| G4d | Leather boots let the wearer stand on any snow layer or powder snow; sneak to fall through (like scaffolding). Without boots, snow layers are walk-through. | Vanilla already does this for powder snow (`PowderSnowBlock.canEntityWalkOnPowderSnow`). **Conflict with 1115b F2:** snow keeps `solidBelowFull` (layers 6 to 8 act whole), but "walk-through otherwise" means no layer is solid without boots (G6-20). Mobs wearing leather boots and leather horse armour: G6-20. |
| G4e | Leaf layers: a powder-snow-like screen overlay (experimental) and mild thorny damage while moving; the faces stay visible from inside. | 1115b F open notes list these. Needs: damage amount and interval, a damage type (memory `damage-types`), whether leather boots or foxes and bees are exempt, config switch for the "experimental" overlay (client) (G6-21). Inside view: render without face culling for leaf piles (client). |
| G4f | IW drops Twigs bamboo leaves rarely; they stack to a full block. | **Hole:** IW drops leaf piles from leaves; bamboo is not IW, so the source block is unclear (bamboo stalks, jungle leaves?) (G6-22). Bamboo leaves have 4 layers (1115b D1); "stack to a full block" needs either a 4-layer model at full height or a new property (G6-22). New loot or a Compat mixin on IW's leaf-pile placement. |

### G5. Raspberry Pie screens (solRaspberryPie, client)

Facts: the double page (12.4 #31, client `[dietLog] doublePage`, on) is built; icons 12.4 #37 already use the
netherite chestplate for toughness and the shield for knockback resistance; the progress bar is windowed (#32).

| Step | What | Notes |
|---|---|---|
| G5a | Back to a single page: `doublePage` default off. | Existing clients keep their own config value; the owner's client needs it set by hand (mirror to `configs_edited/`). Should the "Counting foods" and "Older foods" split go back too? (G6-23). |
| G5b | Book page size scaled by config, default **125%**. Icons and text that run off the book are fixed by the scale. | Client config `[dietLog] bookScale = 1.25`. Apple Pie's UI kit draws in fixed pixels, so this is a pose-stack scale around the whole page. Check narrow screens (the 380 px rule of #31). |
| G5c | Diet buffs screen: slots laid out horizontally; space between the permanent buffs and their label; the "needs diversity" line replaced by a progress bar across the full width of the slots, with tick marks and diversity labels at each slot. A green fill runs from 0 to the current diversity and is full at the highest slot's diversity on that screen. | "Diet buffs screen" is assumed to be the effect screen (11.2, P key). If it is the Diet Log ladder page instead, say so (G6-24). The new bar is separate from the windowed Diet Log bar (#32). |
| G5d | Icons: permanent unlock = Monobank `replacement_lock` if loaded, else an awkward potion (configurable); permanent buff = Create Recycling `diamond_shard` (mod id `create_crush_everything`) if loaded, else a diamond; knockback resistance = shield, shown in the key; toughness = netherite armor; unlockable slot = Supplementaries Squared `metal_frame` if loaded, else an item frame. | Shield and netherite already used (#37); "show in key" means adding the shield to the legend. All three mods are in the pack today. Item ids go through the client config with a fallback, not `global.item` (Java client code). |

### G6. Decisions (owner round 2026-11-16; G7 holds the design that follows)

Decided:
1. Melt speed: the owner wants melting to *look* faster by giving melting blocks more random ticks, not a higher chance. Method chosen by an investigation first (G7.1).
2. Magma: no separate factor. Magma stops snow forming, and snow within 2 blocks melts at once (G7.2).
3. Magma reach: 2 blocks (G7.2). Ice forming over magma: still open (G6 open list, item A).
4. Permafrost means Immersive Weathering's. Quark's permafrost is renamed Frostone in the pack and is not involved.
5. IW permafrost and cryosol mirror blue and packed ice: cryosol like packed ice, permafrost like blue ice (lava only, not saltable). Both wait on heat-source-driven melting (G7.3). Inferred step, confirm at build: permafrost -> cryosol -> dirt, the way blue -> packed -> ice.
7. Ore names: `salt:rock_salt_ore` "Rock Salt Vein", `salt:deepslate_rock_salt_ore` "Deepslate Rock Salt Vein"; `salt:raw_rock_salt` is "Rock Salt".
10. Lick: the messy path is accepted, regression expected. Rock Salt refills a lick; salt-layer refill (B7 layer path) and licking eats layers first (B5) are removed. E4 stays as a general rule: every layer falls (G7.5).
11. Acid: the current acid recipes go back to **heated** (D4's `powergrid_acid_in_water` too). The new water + Rock Salt Block -> Rock Salt Block + 2 salt is heated.
12. Salt Melt: an undamaged Salt Melt stacks to 16 (G7.6).
13. Salt JEI: the evaporation page does not show the boosted evaporation yield (loot tables 8, 5, 3) (G7.7).
15. Torch lighting: uses up the torch (config, on by default); refused in rain with the extinguish sound, and no torch is used then (G7.8).
16. Dragon armor split 8/13/11/8 (40 points); toughness unchanged.
17, 20. Walking: a separate on-top and walk-through tier for every walk-through block (G7.4). Leather boots let the wearer stand on snow layers; without them every snow layer below full is walk-through.
18. No powder snow. A full snow layer (8) and the snow block are different blocks: layer 8 is a `SnowLayerBlock` with a 14 px collision box (vanilla collides one layer lower), drops 8 snowballs (a snow block drops 4), counts as layers when it melts, and passes `canBeReplaced` checks differently. The snow block is a full cube. So snow layer 8 turns into a **snow block** (G7.4), and the powder snow conversion (G4b) is dropped.
22. Bamboo leaves: the leafy top of grown bamboo is the source; the 4-layer model stacked on itself makes the full block (G7.9).
23. The double page is removed from Raspberry Pie (option and code), not just switched off.
24. "Diet buffs screen" is the effect screen (P key).

Decided in the second round (2026-11-16; design in G7.13):
- A. Magma stops water freezing into ice within its melt radius, the same range in which it stops snow forming.
- 6. A Salt Melt use removes **3 snow layers** (configurable).
- 4b. Snow on blue ice or IW permafrost resists ambient melting (temperature, rain), fire and lava, but **not salt**.
- 8. The item stays "Salt".
- 9. Every implemented Vintage salt decor block is renamed **Chiseled Salt ...** (Smooth Salt Block, Salt Pillar, Salt
  Bricks and Salt Brick Slab/Stairs/Wall). Mixed Salt Brick blocks are unchanged.
- 10c. A lick's last wear drops 1 Salt (salt dust), as today.
- 14. Every campfire that is placed unlit gets the tooltip, shown only while the Unlit Campfire option is on.
- 15b. Soul torches light only soul campfires; the soul campfire tooltip says "soulflame". Flint and steel still lights both.
- 19. Snow layers: falling speed inside is the same as in powder snow, and the jump penalty matches powder snow's
  from a configurable layer threshold (default 6). Leaves: fall speed configurable, default halfway between snow and
  air (eval needed); jump penalty only just high enough to allow jumping up one block while pressed against its edge,
  weaker than powder snow or honey (eval needed).
- 21. Thorn prick chance falls with the number of armor pieces worn; full armor means no pricks. Only players are
  hurt. Other mobs do not path through leaf piles.
- 25. The spyglass and bell tooltip lines are not part of this write-up.

Third round (2026-11-16, G7.14): salt lamps are not renamed; leaf overlay on by default. Nothing is open in G6.

### G7. Design after the 2026-11-16 answers (planned, not built)

This section supersedes G1a, G1b, G1d, G2b (refill), G2c (heat), G2e, G2g, G3b, G4a, G4b, G4d and G4f where they differ.

**G7.1 Faster-looking melting: more random ticks for melting blocks (investigation first, no code).**
Goal: melting shows progress more often (snow drops a layer, ice steps) instead of rolling a higher chance on the same
few ticks. Measure each candidate with the perf suite (`tools/perf`, JFR; memory `perf-suite-bot-players`) on a snowy
taiga and a frozen ocean with a magma and lantern field, reporting ms per tick and melts per minute:
- (a) Extra rolls inside `targetTick`: N rolls per vanilla random tick. Cheapest to build. Visually the same as a higher
  chance, so it probably does not meet the goal.
- (b) Extra random ticks per section: a `ServerLevel.tickChunk` hook gives sections whose palette holds a meltable
  block `meltTickFactor` extra random picks, the extras going only to meltable states. Costs one palette check per
  section per tick, plus the extra picks.
- (c) Heat-source registration: a placed or loaded heat source (fire tag, lava, salt) records its position in a
  per-chunk set; every K ticks each source melts or schedules ticks on the targets within reach. Cost scales with the
  number of sources, not the number of targets. Needs save and load handling (rebuilt from chunk palettes on load) and
  invalidation when blocks break.
- (d) Scheduled ticks: when a target finds a source, it schedules its own next melt tick (`scheduleTick`) at a
  configurable interval until it is gone or the source leaves. A steady, visible pace, and only targets near heat pay.
Report: ms per tick for each, melts per minute, complexity, and save safety. The owner picks one, possibly (c) or (d)
combined with G7.3.

**G7.2 Magma and snow.** Magma stops snow forming within 2 blocks above it (snowfall placement hook, and the IW snowy
conversion and vanilla `snowy` property on the block above). Snow within 2 blocks melts at once, both on placement and
on its next tick. Answer to "instant or not melted at the edge of heat range": today neither. Inside a source's reach every
target rolls the same flat chance (`meltChance` 0.4) on each of its random ticks; there is no fall-off with distance and
no instant melt (only ultra-warm dimensions melt instantly). When it melts, a snow stack goes all at once (whole stack to
air). G7.2 adds the 2-block instant zone for magma; G7.10 makes snow melt by layer elsewhere.

**G7.3 Heat-source-driven melting for cryosol, permafrost, packed and blue ice.** Rather than random-ticking every
soil block, the sources do the work, as magma already does today (dim fire sources are source-driven, with a palette check per
section in reach). Lava and lit fire sources look for `meltable_slow` and `lava_only` targets in reach on their own
tick (or via G7.1 (c)/(d)). Cryosol and permafrost then need no `extraRandomTickBlocks` entry and cost nothing away
from heat. Packed and blue ice move to the same path (they are in `extraRandomTickBlocks` today). Mirror rules:
cryosol = packed ice (salt, fire, lava; slow tier; -> dirt), permafrost = blue ice (lava only, not saltable; ->
cryosol). Gated on G7.1's method choice.

**G7.4 Walk-through tiers and snow layers (replaces 1115b D2/F2's `solidBelowFull` for snow; Compat surface).**
- Every walk-through block has two speeds: **on top**, the speed of its BlockRunner material (snow layers -> snow, leaf
  piles and bamboo leaves -> leaves), and **through**, its own pass-through tier, slower than on top.
- Snow layers: walk-through at 1 to 7 layers, slowdown growing with height and reaching its maximum at 7. **Layer 8 turns into
  a snow block** (not walk-through, standard snow block). Leaf layers: walk-through at every height, maximum slowdown at
  8 (full); they never become a solid block. Snow's maximum slowdown is stronger than leaves' (config `snowMaxSlow`,
  `leafMaxSlow`; proposal 0.4 and 0.6).
- Leather boots: stand on top of any snow layer (on-top speed); sneak to sink in (like scaffolding).
- Dust layers (salt, IW sand, red sand and ash, Supplementaries ash, IE sawdust, fodder) are not walk-through; they
  use their material speed (dust; fodder stays soft).
- Holes: snow layer 8 -> snow block also fires on layers made by snowfall and by G7.5 merges; a snow block keeps
  melting as a full block (whole block per melt). 1115b F2 (snow keeps `solidBelowFull`) and its default overrides are
  dropped for snow. Rewrite the D table, harness cases and SQ-PS15B-10/-11 at build.
- **State: built 2026-11-17** (Creative Compat, lane L1 of plan_exec_1117.md; not installed). `surface/SnowLayers` +
  `mixin/SnowLayerPassMixin`: snow layers 1 to 7 have no collision for living entities; an entity that can walk on
  powder snow (leather boots, `#minecraft:powder_snow_walkable_mobs`) stands on the full layer height while above it
  and not sneaking; items, falling blocks, projectiles and contexts without an entity keep vanilla collision. Layer 8
  -> snow block on placement (`getStateForPlacement`) and on a random tick; `SnowLayers.fullToBlock` is the seam for
  G7.5 merges and snowfall stacking (vanilla 1.19.2 snowfall only places 1 layer). Config `[surface.passthrough.layers]`
  `snowMaxSlow`, `leafMaxSlow`, `snowWalkThrough`, `snowFullBecomesBlock` (sync format 3); `solidBelowFull` and its
  overrides removed. **Assumed** (plan proposals, not ruled): `snowMaxSlow` 0.4 and `leafMaxSlow` 0.6; the two
  switches; mobs vanilla lets walk on powder snow (rabbits, foxes) stand on snow layers like leather boots; mob
  pathfinding still sees vanilla snow collision (a mob may aim to step onto a pile it then sinks into).

**G7.5 All layers fall and stack (E4 generalized).** Every layer block (snow layers, leaf piles, bamboo leaves and its
full version, salt, IW sand, red sand and ash, Supplementaries ash and fodder, IE sawdust) falls when unsupported and
merges on landing, like IW's falling layers. E4's rules apply to all of them (merge capped at max, remainder above, item drop
if it cannot place, pistons, chunk load, `layersFall` switch). Snow merging to 8 becomes a snow block (G7.4). The salt
lick hook in E4 is dropped (layers no longer refill). Holes: does IW already make its own layers fall (reuse its
falling entity if so; check at build); snow on leaves (leaves are not full blocks, so does snow fall through
canopies?). Perf: falling entities from big snowfall on unsupported spots; watch TPS (E4's 200-layer row, extended to snow).

**G7.6 Salt Melt stacks when undamaged.** Keep vanilla durability and override Forge's `Item.getMaxStackSize(ItemStack)`:
16 when undamaged, 1 when damaged (vanilla `isStackable` already refuses damaged stacks). Using or dispensing one from a
stack of more than 1 splits one off: shrink the stack by 1, damage the split item and hand it back (inventory or drop),
so a stack never holds a damaged item. No NBT counter and no save conversion needed.

**G7.7 Salt JEI.** The Salt's cauldron evaporation page shows upstream yields; the pack's loot tables give 8, 5, 3
(salt.md S1). Make the page read the loot tables, or replace it with a CD page that does. Then sweep the other JEI tabs
against the recipes (owner: "update other tabs that are out of date"): an investigation row lists every stale tab before
fixes.

**G7.8 Torches light campfires.** A torch used on an unlit campfire lights it and is used up (`[campfire]
torchConsumed = true`). In rain (exposed to sky, raining): the extinguish sound, no light, no torch used. Unlit Campfire's
lit state is set the way flint and steel does it.

**G7.9 Bamboo leaf drops and full bamboo leaves.**
- Source: grown bamboo's leafy top blocks (`leaves` = small or large). The 4 horizontal neighbors of each leafy block are
  "pseudo leaf" positions that behave like a leaf block for IW's leaf pile drops: periodic drop with the same chance as
  other trees, dropping Twigs bamboo leaves. A position occupied by any block other than air does not drop.
- Trigger: bamboo random-ticks only while it can grow (stage 0), so a grown stalk never ticks. Hook IW's leaf-pile timer,
  or give leafy bamboo tops its own tick (`extraRandomTickBlocks`-style, leafy states only). Decide at build after
  reading IW's leaf pile code.
- Full block: a new 8-layer block whose layers 5 to 8 are the 4-layer model stacked on a full 4-layer base (model JSONs
  only, no new texture). Placing a 5th layer on 4 Twigs bamboo leaves converts to the new block; it fills and walks like any
  leaf stack (G7.4). Id in Creative Compat's namespace (ids follow the owning mod; check Twigs' license before copying
  its models).

**G7.10 Snow melts by layer.** A melt removes one snow layer; the chance per layer is its own key (`[melting]
snowLayerMeltChance`, higher than `meltChance`; proposal 0.8). Open decision 6 covers the Salt Melt and dispenser.

**G7.11 Saltable tag and tooltip (Creative Common `Melting`).** Tag `creativecommon:melting/saltable`: the only blocks salt
(salt sources, salt dispenser, Salt Melt) can melt. Product by lookup `[melting] saltProducts` ("from=to"); not listed
-> air. Ice needs its row (`minecraft:ice=minecraft:water`, guarded by `placeWater`) or the water rule must run before
the lookup, otherwise salted ice leaves air. Tooltip "Saltable" on the items of tagged blocks (client; block tags are synced).
Not saltable: blue ice (E1), IW permafrost, frozen prismarine, reinforced blocks. The Salt's own `salt:meltables` is
replaced by or made to follow this tag (E1 already edits it).

**G7.12 Raspberry Pie.** Remove the double page and its `[dietLog] doublePage` option (code and config); fix the
shield icon missing from the key (the knockback resistance entry); the effect screen gets G5c's layout; G5b's
125% scale and G5d's icons are unchanged.

**G7.13 Second-round answers (2026-11-16).**
- Magma (amends G7.2): no snow forms and no water freezes anywhere inside magma's melt radius (`fireRange`, 3 today;
  the same sphere the source-driven tick uses), not only in the column above. Hooks: snowfall placement and
  `Biome.shouldFreeze` / ice formation in `ServerLevel.tickChunk` refuse a position with a magma block within that
  radius. Snow within 2 blocks still melts at once. Perf: the check runs only on precipitation ticks for the one
  chosen column per chunk, and the sphere read is cheap; a palette check of the sections in reach first.
- Salt Melt on snow (amends G7.10): one use removes `[melting] saltMeltSnowLayers = 3` layers (3 or fewer on a smaller
  stack; a snow block counts as 8 and is first turned back into layers). Apply the same key to the salt-dust dispenser's
  `meltStep`, or keep that at the whole stack: decide at build (proposal: same key).
- Snow on blue ice or IW permafrost: skips ambient melting (biome temperature, rain, vanilla light melting if it is
  turned on), fire and lava; salt still melts it. Hole: the pack has no rain or temperature melt path today
  (`vanillaLightMelting` is off); the rule covers any that is added (Creative Calendar's seasons are the likely one).
- Names (amends G2a): Vintage `smooth_salt_block`, `salt_pillar`, `salt_bricks`, `salt_brick_slab`, `salt_brick_stairs`,
  `salt_brick_wall` -> Chiseled Salt Block, Chiseled Salt Pillar, Chiseled Salt Bricks, Chiseled Salt Brick Slab /
  Stairs / Wall; Vintage's own `salt_block` too if it is still registered (it is merged into The Salt's). Mixed
  Salt Brick blocks unchanged. "Salt" stays. Lick's last wear: 1 Salt (as built).
- Campfire tooltip (amends G3a): on every campfire that Unlit Campfire places unlit, only while that is on in Unlit
  Campfire's server config: "Requires a spark or flame to light"; soul campfires: "Requires a spark or soulflame to
  light". Hole: `unlitcampfire-server.toml` has no plain "placed unlit" switch in its keys (`campfireLitTime`,
  `campfireRainUnlitTime`, ...); find which key or code path places them unlit, and make the client read it (server
  config, so it has to be synced or mirrored in a client-readable flag).
- Torches (amends G7.8): normal torches light normal campfires; soul torches light only soul campfires. Flint and
  steel unchanged.
- Snow layer movement (amends G4c): falling inside snow layers uses powder snow's slowdown (vanilla
  `makeStuckInBlock` vertical factor 1.5 on a 0.9 horizontal; at build copy the vertical part only, walking speed stays
  G7.4's); the jump penalty matches powder snow from `[surface.passthrough.layers] snowJumpLayers = 6` layers up.
- Leaf layer movement: fall speed `leafFallFactor`, default halfway between snow (powder snow) and air; jump penalty
  `leafJumpFactor`, set just high enough that a player pressed against a block edge can still jump onto it, below
  powder snow and honey. Both need an eval (a scratch test with a 1-block step beside a pile of 4 and 8 layers) to set
  the defaults.
- Leaf thorns: only players are hurt. Prick chance = `thornChance x (4 - armor pieces worn) / 4`, so full armor means none
  (any item in an armor slot counts; elytra in the chest slot counts). Damage amount and interval: proposal 1 damage
  (half a heart), checked at most once per second while moving, a pack damage type (memory `damage-types`).
- Mob pathing: mobs other than players do not path through leaf piles: treat them as a blocked node in
  `WalkNodeEvaluator` (and in MCA's own node maker, `VillagerLandPathNodeMaker`: memory `mca-villager-pathing`).
  Foxes and bees stay exempt from the slowdown; whether they also avoid piles, check at build (foxes hide in berry
  bushes; proposal: they may path through). Hole: mobs already standing in a pile, and piles across a whole forest
  floor, can leave mobs stuck in place; watch for it in the scratch run.

**G7.14 Third-round answers (2026-11-16).**
- Salt lamps (Vintage's 17 and The Salt's Large Salt Lamp) keep their names; only the decor in G7.13 becomes Chiseled.
- Dispenser: salt dust in a dispenser and the Salt Melt in a dispenser both remove 3 snow layers (`saltMeltSnowLayers`,
  the same key as a hand use), settling G7.13's build question.
- Leaf overlay: on by default (`[surface.passthrough.layers] leafOverlay = true`, client). First attempt: the leaf type's
  own block texture (oak pile -> oak leaves, and so on), drawn over the screen at runtime with transparency, like
  the powder snow overlay. No new texture file is made (texture rule); tinted leaves use the biome tint at the player's
  position. Fallback when a pile's leaf texture cannot be found: no overlay.
- Leaf thorns (amends G7.13):
  - Trigger only in a leaf pile of **6 or more layers**, only for a player moving at walking speed or faster:
    `thornMinSpeed` (default 1.0 = walking), with the full-pile slowdown taken into account. Measured speed is divided
    by the 8-layer walk factor before the check, so a pile of 6 (less slowdown) pricks more easily than a pile of 8.
  - Cooldown `thornCooldownTicks = 20` (1 second).
  - Damage: half a heart (1.0) of the same damage source as sweet berry bushes (`DamageSource.SWEET_BERRY_BUSH`), so
    death messages and armor handling match bushes.
  - Chance still falls with armor pieces worn (G7.13); full armor means no pricks.
- Mob pathing (amends G7.13): mobs do not path through leaf piles of **6 or more layers** (blocked node; vanilla
  `WalkNodeEvaluator` and MCA's `VillagerLandPathNodeMaker`). Smaller piles path as now.
- Exemptions: foxes, bees, rabbits and cats are exempt from the leaf pile slowdown and damage (the existing
  `passthrough_exempt/layers` tag gains rabbits and cats), but the 6+ pathing block still applies to them.
- Campfire config (note for build): add switches for every campfire tweak to `[campfire]` (torch lighting, torch
  used up, rain refusal, soulflame rule, tooltip), mirrored to `configs_edited/`. The tooltip reads them: no torch line
  when torch lighting is off, no tooltip when the tweak or Unlit Campfire's unlit placement is off.
- Ambient melting: **audit first, nothing built.**
  - Audit 1 (feasibility): ice and snow melting driven by biome (temperature), time of day, season (Creative Calendar
    owns seasons and time; memory `creative-delight-routing`) and weather (rain, clear sun). Covers: hook point (random
    tick target side, or G7.1's chosen method), cost per tick, what is readable on the server (season API, sky light, day
    time), interaction with snowfall placing snow back, and with vanilla light melting (off).
  - Audit 2 (behavior): a table of every biome in the pack (vanilla and Terralith and the rest) with today's melting and
    freezing behavior (snowfall, ice formation, the IW snowy and frosty ticks, Melting's tags) and what ambient melting
    would change there.
  - Until the audits are done and approved: packed ice, blue ice, cryosol and IW permafrost **never melt from ambient
    conditions**, and no ambient melt path is added for any block. Heat-source melting (G7.3) is unaffected.

**G7.15 Melting freeze on four blocks (2026-11-16, supersedes G7.14's "ambient only" reading and G7.3 for these blocks).**
Until the ambient melting audits (G7.14) are approved, packed ice, blue ice, IW cryosol and IW permafrost take **no
melting except from salt**: no fire, no lava, no ambient, no heat-source (G7.3) path, no `slowSteps` use by those
sources. Salt keeps the rules already decided: packed ice and cryosol are saltable (a step down: ice, dirt); blue ice
and permafrost are not saltable (E1, G7.11), so in practice they do not melt at all until the audit. Build: drop them
from the fire and lava target path (a `salt_only` flag on `meltable_slow`, or move the fire and lava tiers off them),
and keep them out of `extraRandomTickBlocks` (nothing needs to tick them). Current behavior changes: packed and blue
ice stop melting beside fire and lava (today `slowFireRange` 1, `slowLavaRange` 2). Changelog bullet at build.

### G8. Rulings of 2026-11-17

- G7.1 method (owner, 2026-11-17): **sources find, targets schedule** ((c) without a registry, plus (d)). Heat sources
  (fire tag, lava, magma) scan their reach on their own tick, as magma does today; each target found schedules its own
  melt ticks (vanilla scheduled ticks, saved by the game) at a configurable interval until it is gone or out of reach.
  No per-chunk source set, no new save data. G7.3 builds on it: packed ice, blue ice, cryosol and permafrost leave
  `extraRandomTickBlocks`. The perf comparison of G7.1 is queued for a later run, not run before the build.
- Order of execution: [plan_exec_1117.md](plan_exec_1117.md).

### G9. Melting build of 2026-11-17 (lane M1; Creative Common)

Built in Creative Common (`melting/Melting.java`, `MeltTags`, `SaltableTooltip`, `mixin/BiomeMeltMixin`,
`MeltingBlockStateMixin` scheduled tick, `[melting]` keys, `melting_tags.js`): G8 method, G7.2 and G6 A (magma),
G7.3 with G7.15 (slow tier on the source path, heat off), G7.10 and G6 6 (snow by layer, 3 per salt use), G7.11
(saltable tag, saltProducts, tooltip), G6 4b (keeps_snow). Writeup [melting.md](melting.md); rows SQ-PS15-17 to -23,
CQ-PS15-04, perf SQ-MELT-P2 (queued). Points the plan left open, built as proposed and **assumed** (owner to confirm):
- **assumed:** `meltTickInterval = 200` (10 s, each tick 75 to 125 percent of it), and each melt tick rolls the
  chance (`meltChance` 0.4, `snowLayerMeltChance` 0.8, `slowMeltChance` 0.05) instead of melting for certain.
- **assumed:** G7.15 against G8: the four slow blocks leave `extraRandomTickBlocks` and sit on the source path, but
  heat skips the whole slow tier while `[melting] slowHeatMelting = false` (default). Salt blocks scan on their own
  random tick so they still find packed ice and cryosol. With the switch on, a fire that does not tick (torch) next to
  them still melts nothing.
- **assumed:** lava scans on its own tick except in ultra-warm dimensions (lava seas; Nether ice still finds lava on
  its random tick).
- **assumed:** magma's instant zone (2) fires on snow placement, on magma's tick and on the snow's melt tick, not on
  every snow random tick (a magma check per snow random tick costs too much in snowy biomes). The snow and ice guard
  uses the `fireRange` sphere on the server only; world generation is not checked. The magma tag holds only
  `minecraft:magma_block`.
- **assumed:** a salt block's tick removes 1 snow layer; a snow block hit by salt (tick or use) first becomes 8 layers
  (heat still melts it whole). "Snow" for keeps_snow and the instant zone is `#minecraft:snow` (layers, snow block,
  powder snow).
- **assumed:** saltable = the meltable set (ice, frosted ice, Rare Ice, snow, snow block, powder snow) plus packed ice
  and cryosol. The tooltip line is gray (`col.norm` role).
- Not built: IW snowy conversion near magma (G7.2), E1 frozen decor (row `#9`), the ambient audits (G7.14).

## Not decided or not in this pass

Still on the owner list, unchanged: SQ-QN-02 elytra feel (LOOK), CQ-PWG-66 hookup rewrite, SQ-NSM-04 fixture (tooling),
SQ-RDX-13 (needs a pre-11-02 save), Power Grid client joining a server without it (registry crash, owner note),
SQ-CDNS-12 / CQ-HOP-01 notes, SQ-HH-474 (see B7). SQ-HOP-05 waits on the pass-through session (Compat owner).
Behavior-neutral fixes from the run (SQ-MELT-09, -P1, SQ-SPD-08, SQ-BBQW-02s, SQ-SLK-01) are already built and re-run.
The write-back of the run's "to write back" rows (row text fixes) is separate.

## Done means (per step)

Writeup and TODO row updated, a changelog bullet where a player can notice it, configs mirrored to `configs_edited/`,
queue rows for the live checks, jar built and listed as waiting to be copied to `mods/`.
