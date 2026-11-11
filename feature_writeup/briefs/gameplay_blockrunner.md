# gameplay / blockrunner: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# gameplay`, Sub `blockrunner`. Feature writeup:
[blockrunner.md](../blockrunner.md) (test prefix `BR-`, used up to BR-52; new tests here start at BR-53, check the next
free number before adding them). Related writeups: [alcohol.md](../alcohol.md) 3.4 / 10.16 (Brewin' blocks),
[mob_block_tweaks.md](../mob_block_tweaks.md) (leaf piles, MT-25), [tooltips.md](../tooltips.md) (`TT-`, up to TT-30).

Jars checked (decompiled with vineflower or read with `javap`, under `scratchpad/decomp/` and `scratchpad/vanilla/`):
`supplementaries-1.19.2-2.4.20.jar` (`SoapBlock`, `ModRegistry`), `create_dd` (D&D asphalt data), and the blockstate
lists of every mod added since the 2026-10-02 re-check: `create_salt 1.19.2-1.1.0`, `volcanic_caverns-1.19.2-1.2.2`,
`subterrestrial-forge-1.19.2-2.0.0`, `UndergroundBunkers-1.0.4`, `campsite_structures-1.19.2-FORGE-1.0.3`,
`fireflybush-1.19.2`, `sticky_it-1.2.1`, `labels-1.19.2-1.10`, `unlitcampfire-forge-1.19-1.7.1.0`,
`witchmobility-1.3.2`, `bottledair-1.19.2-2.3`, `splash_milk-1.19.2-1.4.0`; the vanilla 1.19.2 client jar with
Mojang's mappings and MCP `joined.tsrg` for SRG names. "Verified" means read from one of them.

The four open rows come from one scratchpad block (git `2e8687f`, `change_scratchpad.md`): "set minimum leaf layer
height slowdown"; "soap effect on slippery blocks - ice blocks, document others - stronger effect on slipperier
blocks - standard ice speed on all slippery blocks for ice boats - tooltip for ice travel speed?"; "finish blockrunner
audit - ensure new mods - add watcher to flag blocks not flagged with type of 1.0 runspeed". #5 came from the D&D
painted-asphalt note.

## Scope and order

Re-checked 2026-10-31 (brief sweep): line refs into blocktags.js / item_dict.js / tooltips.js / CEConfig.java moved and
are fixed below; #3 is Testing and moved to "Built, needs play"; #2 now lists the mods added in 1.1.01 to 1.1.06.
Prefix clash: the scratch queue also uses `BR-01`.. for bread tests (scratch_queue.md 1998); the BlockRunner ids here are
the blockrunner.md ones.

| Row | Change | Status | Size |
|---|---|---|---|
| #2 | Finish the BlockRunner audit + a watcher | Planned | small: one in-game dump command (patcherMod devtools) + a scan mode in `tools/blockrunner_scan.py`, then tagging in `blocktags.js` |
| #5 | Speed blocks on BlockRunner tiers | In Progress (mostly done) | tiny: the leftover speed-factor check rides on #2's dump; one decision |
| #4 | Slippery blocks and soap | Planned | medium: patcherMod (soap slip on slippery blocks, boat friction), a KubeJS tooltip, docs |
| #3 | Minimum leaf layer height slowdown | Testing | built, see "Built, needs play" |

Build order:

1. **#2 dump first**, because #4 ("document them") and #5 ("every other speed block") both need its friction /
   speed-factor columns. Then tier the newcomers.
2. **#5** closes from #2's output (no code unless the dump shows a new speed block).
3. **#4** last: one patcherMod build with the soap slip and the boat friction, and the tooltip in KubeJS.

Related rows: `engineering/dreams-and-desires #6` (painted asphalt recipes and names; its brief
[engineering_dreams-and-desires.md](engineering_dreams-and-desires.md) leaves speed to #5 here), `baking/sweets` D5
([baking_sweets.md](baking_sweets.md): ice cream blocks' tier; the Neapolitan ones are untiered today, so #2's audit should
settle them with that decision), `farming/trees #1` (the leaf pile work that built #3), `alcohol` 3.4 (Brewin' blocks).
The port rows `ports/fungiport #2`, `ports/veggiesport #1` and `ports/vintageport #2` each list "BlockRunner tiers" as a
leftover: #2's audit is where they get done. `potions/enchanting #8` (Floral enchantment slowdown speed) touches the
same tiers but is briefed under potions.

---

## blockrunner #2: Finish the BlockRunner audit
Status Planned / Work KubeJS / tool (+ a patcherMod devtools command) / TODO `gameplay/blockrunner #2`

### Goal
Every walkable block added since the last audit has a tier (or is untiered on purpose), and a check flags any new
walkable block that nothing gives a speed to, so the next mod added doesn't silently walk at 1.00.

### Now
- **Brewin' is done** (the TODO row is stale here): `item.heating_cask`, `item.ice_crate` in the `wood` tier,
  [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) 293-294 (comment 293: "The coaster stays untiered: it
  is 1 px thin"); alcohol.md 3.4 row "BlockRunner DONE 2026-10-19 (10.16)".
- **Last full re-check:** 2026-10-02 (blockrunner.md 8), covering mods through pack 1.0.4. Mods added since
  ([deploy/DEPLOY_LOG.md](../../deploy/DEPLOY_LOG.md)): 1.0.5 (6), 1.0.6 (23), 1.0.7 (Create: The Salt, Storage
  Labels, Vinery 1.5.5 port), 1.0.8 (Brewin' port); 1.1.01 to 1.1.06 below.
- **Blockstates in those jars (verified):** none in Create: The Salt, Volcanic Caverns, Subterrestrial, Underground
  Bunkers, Campsite Structures, Sticky It!, Unlit Campfire (uses vanilla campfires), Witch Mobility, Bottled Air, Splash
  Milk; Storage Labels ships only a `label` block model (a 1 px label: never registers); Firefly Bush adds
  `fireflybushport:firefly_bush` (a plant, no collision). The painting packs add no blocks.
- **In-tree mods (read from `development/*/src/main/resources/assets`):** patcherMod `creativeengineering:oven`
  (furniture-like, untiered by the "functional" rule, blockrunner.md 2) and `dark_fire` (no collision); Exposure
  `exposure:lightroom` (functional) and the photograph frames / flash (thin); `creative_labels:face_paper` (thin);
  Brewin' (above). The Vinery 1.5.5 port ships no blockstates of its own (it uses the upstream jar as a donor), so its
  new block ids only show in a game dump; `blocktags.js` covers it with `vinery:` patterns (277, 290, 366, 426).
- **Mods added in 1.1.01 to 1.1.06 (checked 2026-10-31, blockstate json count per jar; none has a pattern in
  blocktags.js yet):** Botany Pots 9.0.44 (98, pots: functional), Vintage Delight port `-09` (71), Veggies Delight port
  `-03` (26), Fungi Delight port (16, huge mushroom blocks: `ports/fungiport #2` asks for their tier), Miner's Delight
  port `backportedMiners-1.4.5` (in `mods/`), and inside `creativedelight-1.0.0.jar`'s nested members: End's Delight
  (8), Oceanic Delight (8), Ocean's Delight (1), Ender's additions (4), Hearth and Harvest port (1), Cooking for
  Blockheads additions (40, kitchen furniture: functional), plus `creativedelight` itself (2). Hostile Delight (1).
  No blocks: Sky Structures, Room for Two, Carry Cats, Baby Mobs, Cozy's Improved Cats (2 vanilla-namespace
  overrides), ModernFix, `creativebrewing` (its Vinery and Brewin' members are the ones above). patcherMod now also
  ships `powdered_sugar_block` and `placed_*` egg dishes (placed food: thin).
- **KubeJS blocks:** culture blocks are tiered (`'creativeengineering:*_culture_block'`, spongy, blocktags.js 404);
  prepared flesh, layered obsidian, coils, plates and dust piles are tiered (blockrunner.md 2).
- **Known open ones:** Neapolitan ice cream blocks are in no tier (still true 2026-10-31; Delightful's are in
  `confectionery`, blocktags.js 382; [baking_sweets.md](baking_sweets.md), D5).
- **Tools:** [tools/blockrunner_scan.py](../../tools/blockrunner_scan.py) (writes `tools/blockrunner_untagged_blocks.json`;
  with a tiers file it prints every untiered block that could register, per mod) and
  [tools/blockrunner_tag_sim.js](../../tools/blockrunner_tag_sim.js) (runs blocktags.js in node against the last
  `/dumptags`). The scan is a generator, `blockrunner_scan` in [tools/generators.json](../../tools/generators.json),
  with `dumps: ["dumptags block", "dumpitemsbymod"]` (generators.json 252-256). Its "could register" filter is a name
  regex (`thin`, scan.py 60), not the real collision shape. `kubejs/exported/` (the dumps) is not in the repo.
- **What "typed" means at runtime:** `RunSpeed.Info.typed()` ([RunSpeed.java](../../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/RunSpeed.java)
  68-70): a tier, a json override, a native speed factor, or an inside slowdown. The WTHIT `hideUntyped` switch uses it.
  `RunSpeed.standTop` (174) with `REGISTER_HEIGHT` (82, private) is the "collision top >= 0.2 registers" rule.

### Build
1. **Dump command (patcherMod devtools).** New `devtools/SurfaceAudit.java`, modelled on
   [devtools/WthitIconDump.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/devtools/WthitIconDump.java)
   (registration, JSON writing to `kubejs/exported/`, try/catch per block). A server command `/dumpsurface` (op 2;
   tags are loaded on the server) writes `kubejs/exported/surface_audit.json`, one row per block (default state):
   `id`, `collisionTop` (`getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).max(Y)`, -1 when empty),
   `tier` (`RunSpeed.tierOf`), `override` (`BlockSpeedManager.INSTANCE.hasBlockSpeed` without a tier), `friction`
   (`Block#getFriction`), `speedFactor`, `jumpFactor`, `inside` (`StuckInBlock` rule present), `item` (has a block
   item). Make `RunSpeed.standTop`'s threshold (`REGISTER_HEIGHT`) package-visible so the dump uses the same 0.2.
2. **Scan mode.** `tools/blockrunner_scan.py --audit kubejs/exported/surface_audit.json`: list blocks with
   `collisionTop >= 0.2`, an item, no tier / override / native factor, and not in the baseline; group per mod as today;
   exit 3 when anything is listed. `--write-baseline` writes `tools/blockrunner_untiered_baseline.json` (the reviewed
   "untiered on purpose" set). Keep the old regex mode for when no dump exists.
3. **generators.json:** extend `blockrunner_scan`: `dumps` += `"dumpsurface"`, `args: ["--audit",
   "kubejs/exported/surface_audit.json"]`, `warnExit: [3]`, `highlight` for the "untyped" lines. With
   `[debug.generators]` on, it runs at dev-instance startup: that is the watcher.
4. **The audit itself.** In the dev instance: `/cdevgen run blockrunner dumps`, read the list, tier each newcomer in
   [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) with `t.addMatching` (single modded ids through new
   `global.item` keys in item_dict.js; for the Creative Delight members the keys live in their CD item tree, memory
   `creative-delight-routing`, families as wildcards), then `node tools/blockrunner_tag_sim.js` (0 overlaps,
   no dead patterns) and re-dump. Write the first baseline only after review, so it holds decisions, not leftovers.
5. blockrunner.md: a 2026-10-2x History entry (mods checked, what was tiered), section 6 gets the `--audit` mode, the
   file map gets `SurfaceAudit` and the baseline file.

### Ids and data
- Item dict keys exist for the Brewin' blocks (`i.heating_cask`, `i.ice_crate`, `i.coaster`, alcohol.md 3.4).
- Neapolitan ice cream blocks: `neapolitan:{vanilla,chocolate,strawberry,banana,mint,adzuki}_ice_cream_block`
  (baking_sweets.md, verified there).
- No new mod ids needed for the tooling.

### Decisions
- **What the watcher compares against.** A: a reviewed baseline file of untiered-on-purpose ids (recommended: cheap,
  and a new mod's blocks show up as "new"). B: fill the reserved 1.00 tiers (`machinery`, `functional`, `decorative`,
  blocktags.js 112-114), so every walkable block is typed and the watcher flags anything untyped. B is the literal
  reading of "flag blocks not flagged with type of 1.0 runspeed" and makes WTHIT name furniture's tier, but means
  triaging ~3000 blocks. Recommendation: A now, B later if WTHIT should name them.
- **Neapolitan ice cream blocks**: follow `baking/sweets` D5 (recommended there: `very_sticky`); in any case put them in
  the same tier as Delightful's (`confectionery` today, blocktags.js 382).

### Gotchas
- `EmptyBlockGetter` shapes differ from in-world ones for a few blocks (shape from a block entity, frames). The
  framed / copycat blocks are in `camouflage` anyway; catch and list exceptions as `error`, as WthitIconDump does.
- The overlap log line only appears on the real tag pass (blockrunner.md 3.1, KubeJS pre-pass).
- item_dict.js / tag_helper.js are startup scripts: new keys need a restart, not `/reload`.

### Tests
- **BR-53 Audit dump:** `/dumpsurface` -> `kubejs/exported/surface_audit.json` exists, IP asphalt reads tier
  `asphalt`, soul sand `speedFactor 0.4` and no tier, a carpet `collisionTop` 0.0625; `python3 tools/blockrunner_scan.py
  --audit kubejs/exported/surface_audit.json` exits 0 against the fresh baseline.
- **BR-54 Watcher catches a newcomer:** remove one tiered id from blocktags.js (e.g. `item.ice_crate`), restart with
  `[debug.generators]` on -> the startup run lists `brewinandchewin:ice_crate` as untyped. Put it back.

---

## blockrunner #5: Speed blocks on BlockRunner tiers
Status In Progress (mostly done) / Work KubeJS / patcherMod / TODO `gameplay/blockrunner #5`

### Goal
Every block that changes walking speed does so through a BlockRunner tier: painted asphalt walks exactly like IP
asphalt, and no block keeps a speed of its own on top of a tier.

### Now
- "IE asphalt" in the row is Immersive Petroleum asphalt (`i.asphalt`, `i.asphalt_slab`, `i.asphalt_stair`, item_dict.js
  4233-4235); "painted asphalt" is D&D's 16 coloured asphalt blocks (`i.<colour>_asphalt_block`, item_dict.js
  1519-1534), renamed "Painted Asphalt" with new recipes 2026-10-30 (`engineering/dreams-and-desires #6`, Testing,
  SQ-DD-08); it stays in the asphalt tier.
- **Done:**
  - Both in the `asphalt` tier (1.50): `'#forge:asphalt', 'immersivepetroleum:asphalt_*', 'create_dd:*_asphalt_block'`,
    [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) 139-141.
  - D&D's own boost removed: friction 0.35 -> 0.6 and speed factor 0.001 -> 1.0 in
    [block_properties.js](../../kubejs/startup_scripts/changes/block_properties.js) 5-17 (blockrunner.md 3.5).
    D&D's jump factor 1.25 is kept on purpose (comment line 8).
  - IP asphalt's native 1.2 speed factor is overridden by BlockRunner for tiered blocks (blockrunner.md 1), and IP's
    "Increases walking speed by 20.0%" tooltip is removed ([tooltips.js](../../kubejs/client_scripts/tooltips.js) 480-482).
  - Tests BR-28 / BR-29 (D&D asphalt, blockrunner.md 533-534) exist, untested (not in the scratch queue: walking is
    client play).
- **Left:** "every other speed block". The known ones are handled: honey (tiered, very_sticky), soul sand / soil
  (untiered on purpose: Soul Speed, blockrunner.md 4), powder snow and cobwebs (inside slowdowns, not speed factors).
  No offline list of modded blocks with `speedFactor != 1`, `friction < 0.6` or `jumpFactor != 1` exists.

### Build
1. Run #2's `/dumpsurface` and filter its rows: `speedFactor != 1.0` or `friction < 0.6` (grippier than normal,
   like D&D asphalt was) or `jumpFactor != 1.0`, excluding soul sand / soil.
2. For each hit: put it in the tier that matches its material (BlockRunner then overrides its speed factor), and reset
   friction < 0.6 to 0.6 in `block_properties.js` the same way as D&D asphalt (`e.modify(global.item[key], b =>
   { b.setFriction(0.6) })`, item ids through item_dict keys). Note each in blockrunner.md 3.5.
3. If nothing turns up, record "checked <date>, none" in blockrunner.md 3.5 and close the row after BR-28.

### Decisions
- **D&D jump factor 1.25.** The row says painted asphalt "keeps IE asphalt's run speed"; jump isn't run speed, so it
  is out of the row's letter. Recommendation: reset it to 1.0 (`b.setJumpFactor(1.0)` next to the friction reset) so
  painted and plain asphalt are identical, and drop the "kept" note in blockrunner.md 3.5 and the
  dreams-and-desires brief's gotcha. The user's call; it was kept deliberately on 2026-09-27.

### Gotchas
- `BlockEvents.modification` is a startup event: a restart applies it.
- A block's speed factor is only hidden while BlockRunner has a value for it; if a speed block ever leaves its tier,
  its own factor comes back (why block_properties.js resets D&D's 0.001).

### Tests
- BR-28 / BR-29 cover the asphalt. **BR-55 No stray speed blocks:** after the #2 dump, the filter in Build 1 lists
  only soul sand and soul soil.

---

## blockrunner #4: Slippery blocks and soap
Status Planned / Work patcherMod (+ KubeJS tooltip, docs) / TODO `gameplay/blockrunner #4`

### Goal
Ice and the other slippery blocks get Supplementaries soap's "slip" (a random sideways shove while you walk),
stronger the slipperier the block; every slippery block gives boats the standard ice glide; the slippery blocks are
listed in the writeup; optionally a tooltip says so.

### Now
- **The soap block's effect (verified, `net.mehvahdjukaar.supplementaries.common.block.blocks.SoapBlock`):**
  `stepOn` (`m_141947_`), run on the server and for the `LocalPlayer` on the client, unless the entity is sneaking
  (`isSteppingCarefully`): with chance 0.14 per call and horizontal speed² > 8e-4, it takes the motion, normalises it,
  scales it to 0.1-0.23, rotates it by 20-93 degrees left or right, adds it to the motion, and fires block event 0
  (suds particles above the block, `triggerEvent`).
- **Its friction (verified, `ModRegistry` lambda for `soap_block`):** `friction(0.94f)`, strength 1.25 / 4.0, Material
  `f_76278_`. Vanilla friction: normal 0.6, slime 0.8, ice / packed ice / frosted ice 0.98, blue ice 0.989.
- **The slippery tier** (1.25 walk speed): `'#minecraft:ice', 'supplementaries:soap_block',
  'architects_palette:*packed_ice*', item.thin_ice` ([blocktags.js](../../kubejs/server_scripts/tags/blocktags.js)
  148-151), 16 blocks (blockrunner.md 2). The Architect's Palette and IW thin ice frictions are unverified (in-game
  dump, #2).
- **Boats:** `Boat#getGroundFriction` (`m_38377_`, verified SRG) averages the friction of the blocks under the hull;
  Forge patches it to call the state-and-entity `getFriction(LevelReader, BlockPos, Entity)` (unverified offline: no
  Forge-patched class here; check with `javap` on the dev instance's mapped Forge jar).
- Block friction: nothing in the pack touches it except the D&D asphalt reset; BlockRunner doesn't (blockrunner.md 1).
- **Boat friction is already changed (found 2026-10-31):** Ice Boat Nerf `iceboatnerf-1.1.1.jar` is installed (mods/,
  mod_sides.md 384, both sides) with one `BoatMixin` on `Boat#getGroundFriction` RETURN that makes every ground
  **0.45** (feature_porting.md 2 table, line 115; no config file). So today no block gives boats an ice glide, ice
  included, and "standard ice speed on every slippery block" has no ice speed to copy. feature_porting.md D2 / FP-05
  (port it as ice only, or as is) is the open question that decides this.
- No tooltip mentions slipperiness; BlockRunner's "Speed Multiplier: 1.25" shows on the slippery items.

### Build
1. **Tag.** `#creativeengineering:slippery` (block tag) in a server tag script via `global.tags.add`, = the slippery
   tier (nested `#blockrunner:surface/slippery`) minus `supplementaries:soap_block` (it has its own effect). Add a
   `b.t_slippery` / `i.`-style key in block_dict.js for it.
2. **Config** under `[surface]` (CEConfig `Surface`, [CEConfig.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java)
   1725): new `[surface.slippery]`: `soapSlip` (true), `slipChance` (0.14, soap's), `scaleWithFriction` (true),
   `boatIceFriction` (0.98; 0 = off). Both sides must match (movement is predicted).
3. **Soap slip** (`surface/SoapSlip.java` + a Forge `LivingEvent.LivingTickEvent` subscriber; players and mobs only,
   which is what matters on ice):
   - skip unless `onGround()`, not `isSteppingCarefully()` and not flying; run on the server for every entity (a
     server-side push does nothing to a player, whose client moves it) and on the client for the local player only
     (Supplementaries' own rule: `!level.isClientSide || entity instanceof LocalPlayer`; the `LocalPlayer` check must
     sit in a client-only class);
   - `BlockState s = level.getBlockState(entity.getBlockPosBelowThatAffectsMyMovement())` (`m_20099_`, the same block
     friction uses); in `#creativeengineering:slippery`;
   - chance = `slipChance * k`, `k = (friction - 0.6) / (0.94 - 0.6)` when `scaleWithFriction` (soap = 1, ice 1.12,
     blue ice 1.14); then the soap shove (copy the 10 lines of `SoapBlock.stepOn`, credit it in the javadoc).
   - No suds particles on ice (they belong to soap); optional snowflake particles is a decision.
4. **Boat glide** (`mixin/BoatIceFrictionMixin.java`, common; only after the Ice Boat Nerf decision below): redirect the friction call in `Boat#getGroundFriction`
   (`m_38377_`) and return `max(friction, boatIceFriction)` for states in `#creativeengineering:slippery` (or the
   whole slippery tier, see Decisions). Read the Forge-patched call's owner and descriptor first (Now, Boats).
5. **Tooltip** (KubeJS, [client_scripts/tooltips.js](../../kubejs/client_scripts/tooltips.js) `addTooltips`): one gray
   line on the slippery items, e.g. "Slippery: boats glide like on ice", item ids through `global.item` keys
   (`i.ice`, `i.packed_ice`, `i.blue_ice`, `i.soap_block`, `i.thin_ice`, `i.packed_ice_pillar`, ...; add missing AP keys).
6. **Docs:** blockrunner.md new 3.9 "Slippery blocks": the table of every slippery block with its friction (from #2's
   dump, `friction > 0.6`), the slip and boat rules, the knobs; tooltips.md gets the line and a TT test.

### Ids and data
- Verified: `supplementaries:soap_block` friction 0.94, `SoapBlock.stepOn` logic; SRG `Block.stepOn` `m_141947_`,
  `Boat.getGroundFriction` `m_38377_`, `Entity.getBlockPosBelowThatAffectsMyMovement` `m_20099_`,
  `Block.getFriction()` `m_49958_`.
- Dict keys present: `i.ice` (565), `i.packed_ice` (1286), `i.blue_ice` (1287), `i.soap_block` (1432), `i.thin_ice`
  (10832), `i.packed_ice_pillar` (8856) (item_dict.js lines, 2026-10-31).
- Unverified: AP packed-ice family and IW thin ice frictions; the Forge `Boat` friction call.

### Decisions
- **What "soap effect" means.** Read here as soap block's random shove applied to ice. The other reading (using a
  soap item on ice makes it slipperier) has no hook in Supplementaries. Recommendation: the shove; confirm with the user.
- **Ice Boat Nerf vs boat glide (decide first).** A: drop the jar and let the pack mixin set slippery blocks to
  >= 0.98 (the row's literal reading, fast ice boats back). B: keep the nerf and make every slippery block give the
  same nerfed value ("standard" = uniform), which needs nothing beyond the nerf if it already applies to all ground.
  C: port the nerf as "ice only" (feature_porting.md D2) with a pack value, and use the same mixin for the uniform
  slippery rule. Recommendation: C, one mixin owning boat ground friction (remove the jar in the same build, add-mod
  skill for the removal); confirm with the user, since the nerf was installed on purpose. If the jar stays, the pack
  mixin must run after it (mixin `priority` above 1000) or it is overwritten at RETURN.
- **Blue ice for boats.** "Standard ice speed on all slippery blocks" could mean every slippery block glides like ice
  (raise to at least 0.98, blue ice keeps 0.989) or exactly like ice (blue ice slowed to 0.98, killing blue-ice boat
  roads). Recommendation: at least 0.98, blue ice untouched.
- **Shove on blue ice / frosted ice / thin ice.** Recommendation: the whole tag, scaled by friction; frosted and thin
  ice are mostly water-walking surfaces.
- **Tooltip.** The row ends with "?". Recommendation: yes, one line; it is the only way a player learns soap and AP
  ice glide like ice.

### Gotchas
- Client and server must both run the shove for the local player only on the client (as Supplementaries does), or the
  player rubber-bands.
- The shove only fires while moving faster than about 0.03 blocks/tick, so standing still on ice is safe.
- Ice's friction already makes BlockRunner's 1.25 feel faster than 1.25 (blockrunner.md 3.8 "not modelled").

### Tests
- **BR-56 Ice slip:** walk across packed ice -> every few steps a sideways nudge, like on a soap block; sneak -> none;
  `[surface.slippery] soapSlip = false` -> none.
- **BR-57 Boats:** a boat on Architect's Palette packed ice bricks and on a soap block line -> same top speed as on
  packed ice; blue ice still faster.
- **BR-58 Tooltip:** hover ice, a soap block, thin ice -> "Slippery: boats glide like on ice"; stone has no such line.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Surface speed tiers | Complete | Code matches the writeup. blockrunner.md was stale in its header ("restart needed"), its "server copy is stale" limit, and line numbers into blocktags.js / CEConfig.java / CreativeEngineering.java: fixed (below). The BR tests left unticked in blockrunner.md 7 are still open (the TODO row counts 36) |
| #3 | Minimum leaf layer height slowdown | Testing | `[leaf_piles] slowFromLayers` (default 4, CEConfig.java 899-912, live toml 2162), `LeafPileSlowMixin`, WTHIT line follows; shipped 1.1.01. Scratch PASSED 2026-10-22 (SQ-OM-09, SQ-OM-14); open: MT-25 in game (walk + WTHIT), value decision. Writeup [mob_block_tweaks.md](../mob_block_tweaks.md) leaf piles paragraph |

## Writeup corrections made

- [blockrunner.md](../blockrunner.md) header: "restart needed to load the latest jar and json" -> live in every release
  since, 1.0.9 on 2026-10-19.
- [blockrunner.md](../blockrunner.md) 0 deploy-state paragraph: "Only the dedicated server copy is still behind" ->
  "was still behind then (caught up since)".
- [blockrunner.md](../blockrunner.md) 4 Limits: "Server copy is stale" -> fixed (the server runs the same patcherMod;
  BR-27 untested).
- [blockrunner.md](../blockrunner.md) line refs: blocktags.js tier block L25-380 -> L76-435 (three places), `var runner`
  L34 -> L85, `wipeTag` L66 -> L117, `dirt` L351 -> L407, `mirror` L364 -> L420; CEConfig `Surface` L460 -> L1144;
  CreativeEngineering.java listeners L121 -> L143, L159-161.
