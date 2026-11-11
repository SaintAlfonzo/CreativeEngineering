# BlockRunner: walk speed by surface

Test prefix **BR-** · Last updated **2026-11-10** · Overall status **DEPLOYED (2026-09-27 16:45; live in every pack release since, 1.0.9 on 2026-10-19), in-game tests pending**

## 0. At a glance

Every block you walk on belongs to at most one speed tier (asphalt 1.50 down to very sticky 0.45).
BlockRunner applies the speed and KubeJS fills the tiers. patcherMod adds what a block tag can't express:
sticky pistons only count when they face up, framed and copycat blocks walk like their camo, and
walking on soft or dusty blocks visually sinks you in like soul sand. A WTHIT line shows the speed of
the block you're looking at.

| Part | Layer | Status |
|---|---|---|
| 26 surface tiers + 6 empty basic tiers | KubeJS tags + `[surface.speed]` (basic tiers in `blockrunner.json`) | Loads in game with 0 overlaps: 15:21 world load on 2026-09-27, including the 3 ids added for the newest mods (log `6085 ids, 0 overlap(s)`; the log counts low, the sim holds 6129). 2026-10-02: 1.0.4 mods tiered, sim 6355 ids, 0 overlaps; needs a restart (new item_dict keys), BR-52. The 11:54 `/dumptags` export matched the sim exactly |
| SecurityCraft reinforced blocks and mines copy their vanilla block's tier | KubeJS `t.mirror()` | Loaded since the 14:49 restart (Rhino harness tested); untested in game (BR-38) |
| Sticky tier checks facing | patcherMod mixin | Mixin applies (log 2026-09-26); behaviour untested |
| Visual sinking (camera + models) | patcherMod mixins | Both mixins apply (log 2026-09-26); behaviour untested |
| Framed / copycat blocks sink like their newest camo | patcherMod (`SinkingRender`) | In game since the 14:49 restart (13:12 jar); untested |
| Sinking follows the block holding the feet (no pop between sunken blocks), even depth on honey/mud, items lie on top | Creative Compat (`SinkingRender`), tag `#creativecompat:sinking_hides_items` | Built 2026-11-10, not installed (game open); BR-53..56 |
| Visceral Heap flesh: flesh spongy, porous flesh very sticky with honey's jump | KubeJS tags + `block_properties.js` | Built 2026-11-10 (Common rebuilt for the `porous_flesh` dict key); BR-57 |
| Every surface setting in one `[surface]` config section: per-tier speeds, sticky, camouflage, sinking, wthit | patcherMod (`SurfaceTier`, `SurfaceSpeeds`, `BlockRunnerSpeedConfigMixin`) | Working in game: the mixin applies and the log has `[surface] 26 tier speeds applied` (15:21). The old `[block_sinking]`/`[camouflage_speed]` sections were dropped on first load; they held the defaults, so nothing was lost. The trimmed `blockrunner.json` was put in place at 16:45, so it needs a restart |
| WTHIT "Run Speed" line on the looked-at block | patcherMod WTHIT plugin (`WthitRunSpeed`, `RunSpeed`, `StuckInBlock`), `[surface.wthit]` | First version loaded in game (plugin initialised 14:50, no errors). The second version (hideUntyped, passable blocks, thin layers, inside slowdowns, own speed) is headless-tested 31/31 and was deployed at 16:45, so it needs a restart |
| Camouflage copies camo speed, "newest camo" for multi-material (hybrid) blocks | patcherMod mixins | Newest-camo rule confirmed in game on framed double slabs (BR-17, 2026-09-27); rest untested |
| D&D asphalt friction reset (its own ~3.4× speed boost removed) | KubeJS startup `BlockEvents.modification` | Loaded since the 14:49 restart; untested (BR-28) |
| Tooltips (Speed Multiplier line, camo rewrite, IP asphalt line removed) | see [tooltips.md](tooltips.md) | tests moved to TT-01..05 |

**Open first:** `config/creativeengineering-common.toml` `[surface.speed]` (values),
[blocktags.js L76-435](../kubejs/server_scripts/tags/blocktags.js#L76-L435) (tier contents),
[patcherMod surface/](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/) (Java behaviour).

**Knobs:** everything is under `[surface]` in `creativeengineering-common.toml`, which shows as one
"surface" folder in Create's Configs screen (3.7):
- `[surface.speed]`: one multiplier per tier.
- `[surface.sticky] faceUpOnly`.
- `[surface.camouflage] copyCamo`: framed/copycat walk speed.
- `[surface.sinking] depthPixels` and `copyCamo`: framed/copycat sinking.
- `[surface.wthit]`: the WTHIT Run Speed line (3.8).

The two `copyCamo`s are independent, and the whole section reloads live when saved. Outside it,
`blockrunner.json` keeps only one-off block overrides, and `blockrunner-client.toml` holds the tooltip and FOV options.

**Deploy state (2026-09-27 16:45, game closed):** everything is in place:
- `mods/creativeengineering-1.0.1.jar` matches the build (`md5sum` compared).
- `config/blockrunner.json` now holds only the basic tiers; it's backed up in `configs_edited/root_config/`.
- `development/creativeCommon/deploy_pending/` is deleted.
- `pack/` manifest is regenerated.

Forge adds the new `[surface.wthit] hideUntyped` key to the toml on the next launch. The
dedicated server copy was still behind then (4, Limits; caught up since).

**Measuring speed in game:** `M` =
`/attribute @s minecraft:generic.movement_speed modifier value get 23237052-61ad-11eb-ae93-0242ac130002`
prints BlockRunner's own modifier, nothing else. A player's base speed is 0.1, so a tier `m` reads
**0.1 × (m − 1)**: asphalt +0.05, concrete +0.04, sand −0.015, honey −0.055. A 1.00 tier has no
modifier at all, so the command answers "no modifier". Sprinting, potions and armour don't affect this number.

---

## 1. Overview: what BlockRunner does out of the box

BlockRunner 4.2.2 (Fuzs) maps blocks to walk-speed multipliers. `config/blockrunner.json` holds
`"#tag"` or `"block_id"` → multiplier; the server sends the values to clients on join and on `/reload`.

- **Which block counts:** the block about 0.2 below your feet (vanilla `getBlockStateOn`). Anything
  thinner than that never registers and the block beneath it counts instead: carpets, 1–2-layer snow,
  pressure plates, rails and tracks, chalk marks, flower patches, leaf piles. You stand *in* a
  soul sand or mud block, so those do count. The WTHIT line follows the same rule (3.8).
- **How speed is applied:** an attribute modifier on movement speed (the UUID above, named "Block speed boost",
  amount = base × (m − 1)). It is recalculated when you move to a new block position or land,
  on the server for every living entity and on the client for your own player. Mobs, pets and ridden
  horses get it too.
- **Native speed factors are replaced:** a block BlockRunner has a value for has its vanilla/modded speed
  factor forced to 1 (Immersive Petroleum asphalt's own 1.2, honey's 0.4). **Friction and jump factor are
  not touched**: a low-friction block still accelerates you on top of its tier (ice by design). Honey's jump damping is untouched.
- **Flying** (creative/spectator) gets no block speed.
- **One tier per block:** BlockRunner reads tags into a HashSet with putIfAbsent, so a block in two
  tagged tiers gets a random one on every reload. A `"block_id"` key in the json always beats tags.
- **Stock tags:** six basic tiers, `blockrunner:{very_quick, quick, slightly_quick, slightly_slow, slow, very_slow}_blocks`.
  The jar seeds `dirt_path` → quick and `#minecraft:stone_bricks` → slightly_quick. This pack wipes those seeds.
- **Client options** (`blockrunner-client.toml`): `block_speed_multiplier_tooltip` adds a gray
  "Speed Multiplier: X" line to block items; the pack turns it off (2026-11-02, `gameplay/blockrunner #6`: walk speed
  shows in WTHIT only, 3.8); `disable_field_of_view_changes = true` stops the speed
  change from zooming your FOV.

## 2. Reference: speed tiers

Counts come from `tools/blockrunner_tag_sim.js` on 2026-09-27, after the whole-pack scan. They include
blocks that carry no tags (see 6). Tiers are listed fastest first; `[surface.speed]` keeps the same order. The × column shows the defaults.

| Tier (`blockrunner:surface/…`) | × | # | What belongs | Examples |
|---|---|---|---|---|
| `asphalt` | 1.50 | 19 | Asphalt | IP asphalt + slab/stair, D&D Painted Asphalt (16 colours, renamed 2026-10-30; friction reset, see 3.5) |
| `concrete` | 1.40 | 74 | Concrete | 16 vanilla concretes, IE concrete (leaded, tile, brick, sheet…) and hempcrete, Engineer's Decor rebar concrete, Dustrial Decor cinder blocks |
| `grating` | 1.30 | 52 | See-through walkway floors | Create Deco / Design n' Decor catwalks, Engineer's Decor steel catwalks and floor grating, Quark grate, Everything is Copper grates, Create and IE metal scaffolding (grate-top + standard), Dustrial Decor chain and iron-bar trapdoors |
| `metal` | 1.25 | 1247 | Solid metal | storage blocks, every copper stage incl. waxed, sheet metal and plating (IE, Create Deco, Design n' Decor, Dustrial Decor, Architect's Palette), Railways locometal, davebuildingmod steel and boilers, Create copper shingles/tiles, metal fences, gates and trapdoors, CE plate and coil blocks, Canned Goods' sealed canned dirt |
| `slippery` | 1.25 | 16 | Ice and soap | ice, packed/blue/frosted ice, Architect's Palette packed-ice family, Supplementaries soap block, IW thin ice |
| `constructed_smooth` | 1.25 | 710 | Cut, polished and tiled stone | smooth stone, polished granite, quartz block, glazed terracotta, dark prismarine, diamond/emerald blocks, Quark pillars, Supplementaries checker + daub, Create polished cut limestone, Botania quartz, pavement and azulejo, AE2 quartz and fluix, Twigs polished stones and columns, kitchen floors |
| `constructed_brick` | 1.25 | 1764 | Bricks and masonry | bricks, stone/nether/mud bricks, deepslate tiles, prismarine, purpur, Quark shingles, every modded brick and tile family (Create Deco, Architect's Palette, Botania, Twigs, Clayworks, Design n' Decor castel, PneumaticCraft incl. plastic bricks, IE alloy/blast/coke brick), Mosaic Masonry bonds, Engineer's Decor clinker bricks and shingle roofs |
| `natural_path` | 1.15 | 92 | Trodden ground | dirt path, coarse dirt, gravel, IE slag gravel, raked gravel, every cobblestone family incl. Botania metamorphic cobble, ballast, rocky dirt, dense grit |
| `wood` | 1.10 | 632 | Wood | planks, logs, stripped wood, wooden slabs/stairs, wooden fences, fence gates and trapdoors, vanilla scaffolding, IE treated wood, Quark vertical planks/hollow logs/crates, Architect's Palette boards, Decorative Blocks beams and palisades, drawer trims, food crates, log piles, timber frames |
| `natural_smooth` | 1.05 | 488 | Raw rock | stone, deepslate, tuff, calcite, terracotta, every ore (`#forge:ores`), obsidian, amethyst and budding amethyst, bone blocks, bedrock, Quark jasper/limestone/corundum, Create asurine, D&D gabbro, Botania livingrock, AE2 sky stone, Twigs schist/rhyolite/bloodstone, geode crystal blocks |
| `machinery` | 1.00 | 0 | *Reserved for Create/IE machines.* Not filled yet | — |
| `functional` | 1.00 | 0 | *Reserved for chests, furnaces, workstations.* Not filled yet | — |
| `decorative` | 1.00 | 0 | *Reserved for furniture, lamps, signage.* Not filled yet | — |
| `camouflage` | 1.00* | 169 | Blocks that copy another block's look | all FramedBlocks (minus the framing saw and item frames), all Copycats+, Create copycat panel/step. *Uses the camo's speed instead, see 3.3 |
| `glass` | 1.00 | 217 | Glass | `#forge:glass`, tinted glass, Quark/Create:Framed framed glass, AE2 quartz glass, IE insulating/slag glass, Crystal Clear glass casings, panzerglass, Create windows, glass trapdoors, sea lantern, crystallized honey |
| `dirt` | 1.00 | 24 | Leftover `#minecraft:dirt` (added last) | grass block, dirt, rooted dirt, Botania grasses and enchanted soil, FD rich soil, dirt/grass/loam slabs |
| `natural_rough` | 0.92 | 69 | Rough, crumbly ground | podzol, mycelium, nylium, netherrack, basalt, coral blocks, raw ore blocks, ancient debris, coal/charcoal/coke blocks, glowstone, mangrove roots, IW and Twigs silt, compost |
| `soft` | 0.88 | 338 | Soft and fibrous | wool, sponge, hay, target, moss, mushroom blocks, sculk (not vein), every bed, pet beds, froglights, petal blocks, D&D velvet and padded rubber, padded and cardboard blocks, sacks/bags/bales, honeycomb and paraffin wax blocks, thatch |
| `dust` | 0.85 | 67 | Loose powder | sand, red sand, concrete powder, gravisand, ash, IW sand layers, CE metal dust piles, flour and crushed-ore piles, powdered obsidian, gunpowder blocks, IE sawdust |
| `confectionery` | 0.85 | 75 | Edible blocks | Create Confectionery chocolate/caramel/gingerbread, cake, pies, feasts (pizza, quiche, pancakes), ice cream blocks, FD ham/roast blocks, AP bread, sugar cube, melon, pumpkins, chorus fruit block, Golden Food's golden and enchanted golden cakes |
| `tilled` | 0.80 | 3 | Crop fields | farmland, FD rich soil farmland, tilled end stone |
| `leaves` | 0.78 | 43 | Canopy | all leaves, Quark hedges, Twigs bamboo leaves |
| `snow` | 0.75 | 5 | Snow | snow block, snow layers (3+ layers register), powder snow (standable in leather boots), coarse snow |
| `sticky` | 0.65 | 8 | One sticky face (facing-aware, see 3.2) | sticky piston, vanilla/Create piston heads, Create sticky mechanical piston, CTM sticky launchers, SecurityCraft reinforced sticky piston + head |
| `spongy` | 0.60 | 13 | Squelching ground | mud, muddy mangrove roots, wet sponge, clay, dough block, rotten flesh and entrails, CE prepared flesh block, Visceral Heap flesh |
| `very_sticky` | 0.45 | 3 | Whole slime/honey blocks | honey block, slime block, banana slug slime, Visceral Heap porous flesh (honey jump) |

SecurityCraft's reinforced blocks and disguised mines sit in the same tier as the vanilla block they copy (3.1).

**Basic tiers** (1.55 / 1.35 / 1.15 / 0.85 / 0.65 / 0.45) stay in the json but are kept **empty** for
one-off overrides. A block can only sit in one tier, so to override a block, remove it from its surface
tier first, or add a `"namespace:block": 1.2` key to the json (block-id keys always win).

**Deliberately untiered:**
- **Soul sand and soul soil:** a tier would stop Soul Speed from cancelling the slowdown.
- **Magma**, and functional blocks until `functional`/`machinery` are filled: furniture, machines,
  storage, workstations and doors (framed/copycat doors stay in camouflage).
- **Anything too thin to register** (section 1).
- **No clear material:** Architect's Palette cod/salmon logs and scales and the scute block,
  davebuildingmod vanta/ultra colour blocks, Botania's disguising platforms (patcherMod can't read their camo).

**Helper tags** (pack-owned, read by patcherMod):
- `#creativecompat:sinking` = soft + dust + spongy + very_sticky
- `#creativecompat:blockrunner_face_up` = sticky
- `#creativecompat:camo_speed` = camouflage

## 3. What the pack changes

### 3.1 Tier building (KubeJS)

Everything lives in one block in [blocktags.js](../kubejs/server_scripts/tags/blocktags.js#L76-L435),
in speed order:

- **`var runner = {…}`** ([L85](../kubejs/server_scripts/tags/blocktags.js#L85)) names every tier and
  carries its default multiplier as a comment. Keep the comments in step with `SurfaceTier` defaults.
- **Wiping the basic tiers:** `t.wipeTag(e, basicTiers)` ([L117](../kubejs/server_scripts/tags/blocktags.js#L117))
  clears the jar's seeds.
- **Filling tiers:** each tier is one `t.addMatching(e, tier, [entries], {except})` call. Entries can be ids,
  wildcards (`'minecraft:*_concrete'`, `*` never crosses `:`), `'/regex/'`, `'#tags'` or `'@mod'`.
  Modded families are wildcards or regexes (they only ever match registered ids); single modded ids come
  from `global.item` keys in item_dict.js.
- **Whole-pack scan (2026-09-27):** every block in every jar and KubeJS was reviewed with
  `tools/blockrunner_scan.py` (see 6). About 3000 blocks that could register are left untiered on purpose:
  furniture, machines, storage, plants and thin decor.
- **`except` behaviour:** passing `except` makes `addMatching` expand each `#tag` entry to its current
  members, because a nested tag reference can't be filtered, and it accepts `#tags` too.
  [tag_helper.js L36](../kubejs/startup_scripts/func/tag_helper.js#L36).
- **Order matters once:** `dirt` is filled last, as "`#minecraft:dirt` minus every other tier"
  ([L407](../kubejs/server_scripts/tags/blocktags.js#L407)).
- **SecurityCraft copies:** `t.mirror(e, runnerTags, rename)` ([L420](../kubejs/server_scripts/tags/blocktags.js#L420),
  helper [tag_helper L212](../kubejs/startup_scripts/func/tag_helper.js#L212)) puts every registered
  `securitycraft:reinforced_<x>` and `securitycraft:<x>_mine` in the tier of `minecraft:<x>`
  (`<x>_ore` → `<x>_mine`). A mine with a speed of its own would give itself away. SecurityCraft's own
  names: terracotta → `hardened_clay`, dirt path → `grass_path`, stone slab → `normal_stone_slab`, smooth
  stone slab → `stone_slab`, nether quartz ore → `quartz_mine`. The helper checks the registry
  (`getElementIds()`), so guessed names that don't exist are dropped. It runs after `dirt`, so dirt mines
  are covered too.
- **Strays removed:** wildcards like `polished_*` also catch buttons and pressure plates, so a final
  regex removal strips them.
- **Overlap check:** `t.overlaps(e, tags, 'BlockRunner tier')`
  ([tag_helper L188](../kubejs/startup_scripts/func/tag_helper.js#L188)) logs any block found in two
  tiers, then `BlockRunner tier overlaps checked: N ids, K overlap(s)` to `logs/latest.log`.
- **KubeJS pre-pass:** KubeJS runs tag scripts once with no tag data (`PreTagEventJS`, where
  `getObjectIds()` returns `[]`). Calling it marks the pass invalid, so KubeJS re-runs the scripts on
  the real load; that re-run is what makes `except` and `overlaps` work. The overlap line only appears
  on the real pass.
- **Helpers removed:** the old dedicated speed helpers (`blockSpeed`, `quickBlocks`,
  `blockSpeedConflicts`) were removed on purpose; plain `addMatching` plus `overlaps` replace them.

### 3.2 Sticky tier checks facing (patcherMod)

BlockRunner looks speeds up per *block*, not per blockstate, so on its own a sideways sticky piston
would slow you too.

- **Hook:** [BlockRunnerBlockStateMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BlockRunnerBlockStateMixin.java)
  injects at every return of BlockRunner's own `blockrunner$tryAddBlockSpeed`. That method is merged
  into `LivingEntity` by BlockRunner's mixin, so ours has priority 1500 (Mixin only allows targeting
  another mixin's method from a higher priority) and `require = 0`: if a BlockRunner update renames
  the method, this silently stops instead of crashing the game.
- **Rule:** [StickyFacing](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/StickyFacing.java)
  applies to blocks in `#creativecompat:blockrunner_face_up`. If the block has a `facing`
  property it must be `up`. If it has a piston `type` property it must be `sticky`, since vanilla and
  Create piston heads are shared by plain and sticky pistons. Otherwise BlockRunner's modifier is removed.
- **Result:** a sticky piston facing up slows you (0.65); sideways or down it doesn't. An extended
  upward sticky piston slows you on its head; a plain piston's head never does.
- **No-facing blocks always count:** Create Things & Misc sticky launchers have a single blockstate.
  The Supplementaries spring launcher is *not* sticky (removed 2026-09-27).
- **Switch:** `[surface.sticky] faceUpOnly = false` turns the facing check off (every block in the tier slows
  you however it faces). Keep it the same on server and client.

### 3.3 Camouflage walks like its camo (patcherMod)

With `[surface.camouflage] copyCamo = true`, a framed or copycat block walks like the block it's
disguised as: framed concrete runs like concrete, framed honey drags like honey.

- **Speed:** [CamoSpeed](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CamoSpeed.java)
  runs from the same hook as 3.2. On a `#creativecompat:camo_speed` block it reads the camo and
  swaps BlockRunner's modifier for the one the camo block's own tier gives (same UUID, same formula).
  It keeps the flat tier for an empty frame, a frame disguised as another frame, or while flying.
- **Reading the camo:** FramedBlocks via `FramedBlockEntity#getCamo()` (double blocks: `getCamo()` /
  `getCamoTwo()`). Copycats+ via `ICopycatBlockEntity#getMaterial()`, or its multi-material storage.
  Create's own copycat panel/step via `CopycatBlockEntity#getMaterial()`. The mod-specific code sits in
  [FramedCamo](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/FramedCamo.java) and
  [CopycatsPlusCamo](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CopycatsPlusCamo.java),
  loaded only when that mod is present.
- **Multi-material blocks use the newest camo:**
  - Which blocks: FramedBlocks double blocks (double slab/panel/stairs…) and Copycats+ board, byte,
    byte panel, cogwheel, slab and the half-layer variants.
  - [FramedDoubleCamoOrderMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/FramedDoubleCamoOrderMixin.java)
    records which half last received a non-empty camo.
  - [CopycatMaterialOrderMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/CopycatMaterialOrderMixin.java)
    records which part last received a real material, and follows parts renamed when a contraption
    rotates the block.
  - Removing a camo doesn't count as inserting one.
  - Both records are saved with the block and sent to clients, because your client needs them to
    predict your own movement.
  - **Storage detail:** FramedBlocks gets its own top-level key. Copycats+ gets a flag *inside* the
    newest part's entry, because Copycats+ turns every top-level key into a part, so a separate key
    would create a phantom part. Removing patcherMod later leaves both mods loading cleanly.
- **Fallback when the newest part has been emptied:** framed → the other half. Copycats+ → what's
  left, if it's all one material. Otherwise the flat tier. Blocks filled before this feature have no
  record until a part is re-filled; until then framed doubles use their first half.
- **Tooltip:** [CamoSpeedTooltip](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/CamoSpeedTooltip.java)
  replaces BlockRunner's line, same gray style:
  - "Speed Multiplier: matches its camo"
  - "Speed Multiplier: matches its camo (newest camo sets walk behaviors for hybrid blocks)" on hybrid-capable items: framed slab and panel (they merge), every framed double item, and Copycats+ multi-material copycats (your wording, 2026-09-27)
  - "Speed Multiplier: 1.0, no matter its camo" when `copyCamo = false`
  - Turning BlockRunner's tooltip off hides this too; the pack has it off since 2026-11-02 (`gameplay/blockrunner #6`), so this line
    is dormant and WTHIT is the place to read camo walk speed.
- **Config:** `copyCamo` must be the same on server and client, or movement stutters on these blocks.

### 3.4 Visual sinking, like soul sand (patcherMod)

Soul sand's real sink is a lower collision box. That was rejected here (see 4), so the sink is visual
only: collision, hitboxes, movement, mob AI and spawning are untouched.

- **Where:** entities standing on the top face of a *full* block in `#creativecompat:sinking`
  (soft, dust, spongy, very_sticky). Slabs, sensors, carpets on top and layer blocks don't sink.
- **Even depth (2026-11-10):** a sinking block that already sinks for real by up to the visual depth only adds the
  rest: honey (15/16) adds 1 px, mud (14/16) adds nothing at the default 2 px. Every sunken block looks equally deep,
  so slime to honey or wool to mud keeps the same eye height. (Before, honey showed only its own 1 px.)
- **Which block decides (2026-11-10):** the block under the centre, when it is the one holding the feet. When it
  isn't (the centre is already over a lower block, a honey/mud block the feet haven't dropped onto yet, or a gap),
  the blocks under the hitbox corners that hold the feet decide. The sink only changes once the next block really
  carries the entity. Before, crossing from a full sunken block onto honey, mud, a path or an edge popped the camera
  up 2 px for the moment the centre was past the edge, then dropped it: the "bump" at sunken-block edges. Collision
  was never changed; stepping up onto a block from a real-sink block (honey, mud, soul sand) is vanilla step-up and
  stays, but the camera no longer moves for it.
- **Dropped items (2026-11-10):** by default an item lies on top of a sinking block at its real height, so
  ItemPhysic's flat items stay visible on the texture. Blocks in `#creativecompat:sinking_hides_items` (set in
  `blocktags.js`, empty for now) swallow items: the item sinks like any other entity. For a framed block the
  frame or its camo can be in the tag.
- **Blending:** [SinkingRender](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/SinkingRender.java)
  works the depth out at the entity's previous-tick and current-tick positions and blends by partial
  tick, exactly like a real position change. Stepping on, off, landing and jumping all ease over one
  tick. Riders sink with their mount.
- **Camera:** [CameraSinkMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/CameraSinkMixin.java)
  lowers the eye Y in `Camera#setup` *before* the third-person pull-back, so first person, third
  person and fog/underwater checks all match a real sink.
- **Models:** [EntitySinkRenderMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/EntitySinkRenderMixin.java)
  lowers the renderer offset in `EntityRenderDispatcher#render`. Model and name tag move; the shadow
  stays on the block top and the F3+B hitbox stays true.
- **Framed / copycat blocks:** with `[surface.sinking] copyCamo = true` (default), a framed or
  copycat block sinks like its camo, but only when its own shape is a full block, the same shape rule
  every sinking block follows. A framed cube disguised as wool or sand sinks; one disguised as stone doesn't.
  A hybrid double slab is a full block, so it sinks when its newest camo is a sinking block (same lookup
  as the walk speed, 3.3). Non-full frames (single slab, panel, stairs, Create copycat step) never sink.
  Independent of `[surface.camouflage] copyCamo`: turn either off without affecting the other.
  No tooltip mentions sinking.
- **Depth:** `[surface.sinking] depthPixels` (default 2 = soul sand; 0 = off; max 8).

### 3.5 Native movement properties reset (KubeJS startup)

BlockRunner only overrides a block's speed factor (section 1), so a mod's own friction or jump
tweaks still stack on top of the tier. [block_properties.js](../kubejs/startup_scripts/changes/block_properties.js)
resets them with `BlockEvents.modification` (startup, so it needs a game restart).

- **Create D&D asphalt** (16 colours): D&D registers these with `friction(0.35)`, `speedFactor(0.001)` and
  `jumpFactor(1.25)`. Vanilla blocks use friction 0.6. At 0.35 you accelerate about 5× harder and
  keep about 3.4× the top walking speed, which was the big boost on top of the 1.50 tier.
  Friction is reset to 0.6, and the speed factor to 1.0: BlockRunner hides the 0.001 while the block is
  tiered, but it would freeze anyone walking on it if the block ever left the tier.
  **Jump factor 1.25 is kept** (slightly higher jumps on D&D asphalt); reset it the same way if unwanted.
- Item/block ids come from `global.item` (`white_asphalt_block` … `black_asphalt_block`, D&D section of item_dict.js).
- **Visceral Heap porous flesh** (2026-11-10): very sticky tier, and jump factor 0.5 (honey's), so it grips like
  honey instead of feeling like slime. Plain flesh is spongy. BOP's own `FleshBlock` step-on also keeps 95 % of
  horizontal velocity per tick on both; that stays and costs about 6 % on top of the tier. Porous flesh also has
  honey's wall slide (3.9). Ids `global.blocks.flesh` / `porous_flesh`; both only exist with Biomes O' Plenty.

### 3.6 Config files and backups

| File | What | Backup |
|---|---|---|
| `config/creativeengineering-common.toml` | `[surface]`: tier speeds, sticky, camouflage, sinking, wthit (3.7, 3.8) | `configs_edited/root_config/` once a value differs from its default (all defaults on 2026-09-27) |
| `config/blockrunner.json` | basic tiers + one-off block overrides only | `configs_edited/root_config/blockrunner.json` |
| `config/blockrunner-client.toml` | tooltip off (2026-11-02, was on), FOV change off | `configs_edited/root_config/blockrunner-client.toml` |

### 3.7 One `[surface]` config section (patcherMod)

Everything tunable about walking on blocks sits under `[surface]` in `creativeengineering-common.toml`.
Create's Configs screen only shows one common file per mod, so a section (shown there as a "surface"
folder) keeps it all editable in game.

- **`[surface.speed]`:** one entry per tier, each with a comment listing what the tier holds, range
  0.05 to 4.0. The list comes from [SurfaceTier](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/SurfaceTier.java),
  whose defaults are the values `blockrunner.json` had on 2026-09-27. A new tier needs an entry there as well as in blocktags.js.
- **How the speeds reach BlockRunner:** [BlockRunnerSpeedConfigMixin](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/BlockRunnerSpeedConfigMixin.java)
  edits the JsonObject BlockRunner reads from `blockrunner.json` just before BlockRunner parses it
  ([SurfaceSpeeds](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/SurfaceSpeeds.java)).
  From there BlockRunner treats the values as its own: they're synced to players on join and on
  `/reload`, and its Speed Multiplier tooltip shows them.
  - A `#blockrunner:surface/*` key left in the json is overridden, with a warning in the log if its
    value differs from the toml's.
  - Each load logs `[surface] 26 tier speeds applied to BlockRunner from [surface.speed]`.
- **Live edits:** saving the toml (by hand or in Create's screen) re-runs BlockRunner's load on the
  server, but only if a tier speed changed. You feel the new speed after stepping onto a new block.
- **Server authority:** only the server's file read is hooked, so a client on a dedicated server
  always uses the server's speeds. `sticky` and `camouflage` still have to match on both sides.
  `sinking` is client-only.
- **If a BlockRunner update breaks the hook:** the mixin is `require = 0`, so the game still boots.
  Instead, `[surface] BlockRunner loaded blockrunner.json without BlockRunnerSpeedConfigMixin` is logged as an
  error at server start, and every surface tier walks at 1.0.

### 3.8 WTHIT "Run Speed" line (patcherMod)

Looking at a block with WTHIT shows e.g. **Run Speed: 1.50×** (green), with **(Asphalt)** in gray.
Slower blocks show red. Frames add "from camo"; a sticky piston that isn't facing up reads 1.00× with
"sticky face not up".

- **Where the number comes from:** [RunSpeed](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/RunSpeed.java)
  reads BlockRunner's own Block-to-speed map, the one movement uses. It then applies the same camo
  and sticky rules as movement: `CamoSpeed.speedCamo` and `StickyFacing.cancels`, shared helpers that
  both paths call. The line always shows what you'd walk at, and a `[surface.speed]` edit shows up
  straight away.
- **Which block counts** (same as BlockRunner, which reads 0.2 below your feet), decided from the
  block's collision shape for someone standing on top of it:
  - **Collision top 0.2 or higher:** the block's own speed.
  - **Thin solid layer** (collision top under 0.2: carpets, 2 snow layers, lily pads) **or any
    1-2 layer snow**: the block beneath, noted "block below". A single snow layer has no collision at
    all but is still treated as a layer. Snow layers don't slow you in this pack (Immersive
    Weathering's snow mixin only handles melting); 3+ layers count as the Snow tier.
  - **No collision** (grass, flowers, crops, saplings, pressure plates, rails): **no line**, unless the
    block slows you from inside (next point).
  - The shape is read with a "standing on top" context, so scaffolding shows its solid top and
    powder snow counts as a surface (Snow tier) only while you wear leather boots.
- **Blocks that slow you from inside** ([StuckInBlock](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/StuckInBlock.java)):
  their `entityInside` calls `makeStuckInBlock`, which multiplies your move and zeroes your momentum
  every tick. The line shows the real walking speed, `multiplier × (1 − 0.91 × friction)` of normal
  (about 0.45 × the multiplier on a normal floor), times the speed of the block beneath. Noted
  "walking through". Table keyed by block class, superclasses included (a modded `WebBlock` subclass
  counts as a cobweb); values read from each class's bytecode on 2026-09-27:

  | Block (class) | Multiplier | Shows (on a 1.00 floor) | Condition |
  |---|---|---|---|
  | Cobweb, SecurityCraft reinforced cobweb, Fantasy Furniture spider webs, Create Additions barbed wire | 0.25 | 0.11× | reinforced cobweb: its owner passes freely, the line doesn't check |
  | Sweet berry bush, Nether's Delight propelplant, Vinery taiga grape bush | 0.80 | 0.36× | |
  | Powder snow | 0.90 | 0.41× | only when you'd sink in (no leather boots) |
  | Supplementaries bamboo spikes | 0.95 | 0.43× | not in creative |
  | Twigs bamboo leaves | 0.75 | 0.34× | 3+ layers |
  | Immersive Weathering leaf piles | 0.992-0.997 | 0.45× | 4+ layers, IW `leaf_piles_slow` on, no Depth Strider |

  Create's crushing wheel controllers also call it, but they are machine internals and left out. A new
  mod's slowing block needs a row in `StuckInBlock.RULES` (find candidates by grepping jars for
  `m_7601_`).
- **Block's own speed factor:** a block BlockRunner has no value for keeps its vanilla/modded speed
  factor. The line shows its real walking effect, `(1 − 0.91f) / (1 − 0.91 f s)`: soul sand's 0.4 reads
  **0.58×**, noted "own speed". Soul Speed isn't modelled.
- **Tier name:** from a cached Block-to-tier map, filled the first time you look at a block and
  cleared on every tag reload (joining, `/reload`). Blocks with a speed but no surface tier (a one-off
  json override or a basic tier) show the number only.
- **Cost:** per WTHIT refresh, one or two collision shapes, one hash lookup for the speed, one cached
  lookup for the tier, and a block-entity read for framed/copycat blocks only. It's client-side only
  with no server traffic, since frames already sync their camo.
- **Plugin wiring:** [WthitRunSpeed](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/WthitRunSpeed.java)
  is a client-only WTHIT plugin declared in `development/creativeCommon/src/main/resources/waila_plugins.json`, which
  requires `blockrunner`. It fills the WTHIT line `creativecompat:run_speed`.
- **Config `[surface.wthit]`:**
  - `enabled` (true): master switch.
  - `hideUntyped` (true): no line on blocks nothing gives a speed to (no tier, no json entry, no speed
    factor of their own, no inside slowdown). Those are always 1.00, so it only matters with
    `hideNormal = false`: then tiered 1.00 blocks (Glass, Dirt, Camouflage) still show, furniture doesn't.
  - `hideNormal` (true): no line on any 1.00 block, tiered or not, which is most of them.
  - `showTier` (true): tier name and notes.
  - `format`: `MULTIPLIER` (1.50×) or `PERCENT` (+50%).
- **Text:** lang keys `creativeengineering.wthit.run_speed*` and `creativeengineering.surface.tier.<key>`.
  Tier names are the config keys in title case ("Natural Smooth"), so the line tells you which
  `[surface.speed]` key to edit.
- **Not modelled:** a surface's friction (slime's 0.8 friction makes it slower than its tier; ice is
  about even), Soul Speed, and the vertical part of inside slowdowns.

### 3.9 Wall slide on non-honey blocks (Creative Compat, 2026-11-10)

Vanilla only slides you down a Honey Block's side because honey's collision is inset 1/16, so pressing against it
counts as being inside it. A full block never does that, so
[WallSlide](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/surface/WallSlide.java)
runs from `WallSlideMixin` at the end of `Entity#checkInsideBlocks`, the same point honey's slide runs, and checks
the blocks touching the entity's sides.

- **Blocks:** `#creativecompat:wall_slide` in `blocktags.js` (reloadable). Now: porous flesh, Alex's Mobs Banana Slug Slime
  Block (added 2026-11-10 from the survey; the slide replaces its own x0.8 drag while you touch its side). Honey and Fruits Delight
  jelly blocks (they extend HoneyBlock) already slide and are skipped.
- **Numbers are honey's:** falling faster than 0.08 b/tick, off the ground, feet below the block's top → throttled to
  0.05 b/tick (horizontal cut too above 0.13), fall distance reset (no fall damage). Honey's slide sound, the wall's
  own block particles, each 1 tick in 5, sent from the server.
- Works on non-full blocks too (it uses the block's collision box), so any block can join by tag.
- No advancement trigger (the pack drops advancements).

**Proposed for the tag (not added, owner to pick, 2026-11-10 survey of every sticky block in the pack):**

| Block | Today | Proposal |
|---|---|---|
| Honey Block | vanilla slide | already slides |
| Fruits Delight jelly blocks (22) | extend HoneyBlock | already slide |
| Alex's Mobs Banana Slug Slime Block | honey-like: speed 0.4, jump 0.5, inset box, but only damped velocity x0.8 inside it, fall damage kept | **added 2026-11-10** |
| Fruits Delight jello blocks (22) | extend SlimeBlock: bounce | no: slime family, bounce is the feature |
| Slime Block, FramedBlocks bouncy cube | bounce | no |
| Architects' Palette rotten flesh block, entrails | spongy tier | maybe: same goo as Heap flesh; would match porous flesh |
| Creative Engineering prepared flesh block, culture blocks | spongy tier | maybe, with the line above (one flesh family) |
| Raw dough blocks (Create Compressed dough, ginger, cookie, sweet potato, oat, cucumber, sourdough) | spongy tier | maybe: raw dough is tacky; low priority |
| Visceral Heap flesh (plain) | spongy tier | no: you said spongy, not sticky |
| Mud, muddy mangrove roots, clay, wet sponge | spongy tier | no |
| Buzzier Bees crystallized honey, Alex's Mobs crystalized mucus | hard crystal | no |
| Sticky pistons, sticky launchers (one sticky face) | sticky tier, face-up only | no: would need a facing check like 3.2; niche |
| Miner's Delight sticky basket, Fungi Delight inky goo veil | furniture / thin veil | no |

## 4. Limits and decisions

**Limits**
- **Speed updates on block change only:** BlockRunner recalculates when you move to a new block or
  land. A piston turning under you, a frame re-skinned under you, or a config/tier edit applies after
  you step off and back on.
- **The camo hook can silently stop:** it rides on BlockRunner's internal method name. If a
  BlockRunner update renames it, facing and camo speed quietly stop working. Check that
  `Mixing BlockRunnerBlockStateMixin` appears in `latest.log`.
- **Crosshair height:** the crosshair's pick ray still starts at your true eye, 2 px above the sunk
  camera. This only matters on block edges.
- **Speed edits need a new block:** a `[surface.speed]` change reaches BlockRunner immediately but, like
  any tier change, applies to you once you step onto another block.
- **Server copy (fixed):** was stale on 2026-09-27 (`server_mods/` held `creativeengineering-1.0.0.jar`). The
  dedicated server has run the same patcherMod as the clients since the pack deploys resumed (1.0.9 on 2026-10-19),
  so facing, camo and `[surface.speed]` run server-side too; BR-27 is still untested.
- **Twigs bamboo leaves are in the `leaves` tier but can never register:** the block has no collision,
  so you always stand on the block beneath. The tier entry is harmless dead weight. The WTHIT line
  treats the block as a slow-while-inside block (0.34× at 3+ layers).
- **Stray camouflage members:** the tier includes a few technical/thin blocks (`copycats:copycat_base`,
  framed rails/torches). Harmless; they never register or are never walked on.
- **The scan only sees untagged modded blocks that have an item:** jars also ship blockstates for
  optional-compat blocks that never register, so the item export is the proof. A tagless block without
  an item (rare) is invisible to the offline tools; the in-game tag load still matches it by pattern.
- **Trapdoors only register in the top half:** a closed bottom trapdoor is thinner than 0.2, so the
  block beneath counts. Fence, gate and wall tops do register (vanilla looks one block down for them).

**Decisions (don't re-propose without a new reason)**
- **Sinking by lowering the hitbox:** rejected by you. It breaks iron golem spawns on sand, drowned
  beach AI and shulker attaching (everything that asks "can an entity stand here").
- **Tiering soul sand:** rejected, because it breaks Soul Speed.
- **Tiering carpets or leaf piles:** pointless, they can't register.
- **HappyTrails:** uninstalled 2026-09-26. It duplicated this, used permanent modifiers and warped FOV.
- **Doing state checks in KubeJS:** impossible, because block tags can't see blockstate or block
  entities; that's why 3.2 and 3.3 are mixins.
- **Considered in the whole-pack scan (2026-09-27) but not adopted:** `mossy`, `synthetic`, `organic` tiers.
- **Gourds are confectionery, not soft (2026-09-27):** soft sinks full blocks visually, and sinking
  into a pumpkin looks wrong. Melon, pumpkins and the chorus fruit block count as food.
- **Beds are soft** (your `//Add beds` note). They aren't full blocks, so they don't sink.
- **Machinery / functional / decorative stay empty:** they're 1.00, so filling them changes no speed.

## 5. File map

- Values: `config/creativeengineering-common.toml` `[surface]`; one-offs in [config/blockrunner.json](../config/blockrunner.json); client options in [config/blockrunner-client.toml](../config/blockrunner-client.toml)
- Tier contents: [kubejs/server_scripts/tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js#L76-L435)
- Helpers: [kubejs/startup_scripts/func/tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js) (`addMatching` L36, `_glob` L57, `wipeTag` L102, `overlaps` L188, `mirror` L212)
- patcherMod behaviour: [surface/](../development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/surface/): SurfaceTier, SurfaceSpeeds, RunSpeed, StuckInBlock, WthitRunSpeed, StickyFacing, CamoSpeed, FramedCamo, CopycatsPlusCamo, CamoSpeedTooltip, SinkingRender, LastInsertedCamo, LastInsertedMaterial
- patcherMod mixins: BlockRunnerBlockStateMixin, BlockRunnerSpeedConfigMixin, FramedDoubleCamoOrderMixin, CopycatMaterialOrderMixin (common); CameraSinkMixin, EntitySinkRenderMixin (client). All in [mixin/](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/)
- patcherMod config: [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java#L1144) `Surface` (`speed`, `Sticky`, `Camouflage`, `Sinking`, `Wthit`); tooltip and SurfaceSpeeds listeners registered in [CreativeEngineering.java L143, L159-161](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeEngineering.java#L143)
- Text: [patcherMod lang en_us.json](../development/creativeCommon/src/main/resources/assets/creativecommon/lang/en_us.json) `creativeengineering.tooltip.walk_speed.*` (item tooltip), `creativeengineering.wthit.run_speed*` and `creativeengineering.surface.tier.*` (WTHIT line)
- Compile-only jars in `development/creativeCommon/libs/`: BlockRunner, FramedBlocks, Copycats+, WTHIT
- WTHIT plugin declaration: [waila_plugins.json](../development/creativeCommon/src/main/resources/waila_plugins.json)
- Native property resets: [kubejs/startup_scripts/changes/block_properties.js](../kubejs/startup_scripts/changes/block_properties.js); D&D asphalt keys in [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) ("Dreams & Desires" under Create)
- Tooltip side: [tooltips.md](tooltips.md)
- Offline tier check: [tools/blockrunner_tag_sim.js](../tools/blockrunner_tag_sim.js)
- Whole-pack scan: [tools/blockrunner_scan.py](../tools/blockrunner_scan.py), writes [tools/blockrunner_untagged_blocks.json](../tools/blockrunner_untagged_blocks.json)

## 6. Offline checks

- **Untagged blocks:** `python3 tools/blockrunner_scan.py` rebuilds
  `tools/blockrunner_untagged_blocks.json`: registered blocks with no tags at all (froglights, D&D
  rubber, Engineer's Decor catwalks…), found in the jars' blockstates (mods, vanilla client jar,
  kubejs/assets) and confirmed by a same-id item. The `/dumptags` export can't see these.
- **Tiers:** `node tools/blockrunner_tag_sim.js` should print `0 overlap(s)` and
  `no dead patterns / unknown ids`. It reads the last `/dumptags` export plus the untagged list above.
  Add a path argument to dump every tier's members.
- **What's left untiered:** `node tools/blockrunner_tag_sim.js /tmp/t.json && python3 tools/blockrunner_scan.py /tmp/t.json`
  prints every untiered block that could register, per mod, colours and wood types collapsed. Re-run
  `/dumptags` and `/kubejs export` in game after adding mods, then this, to tier the newcomers.
- **patcherMod:** `cd patcherMod && ./gradlew build --offline`. Then copy
  `build/libs/creativeengineering-1.0.1.jar` to `mods/` **with the game closed** and compare `md5sum`
  of both files.
- **patcherMod logic without the game:** plain `javac`/`java` against the mapped Forge jar, with a
  one-line `FMLEnvironment` stub (see Claude's memory `patchermod-headless-smoke-test`). It can
  bootstrap Minecraft to check real blocks' collision shapes and the `StuckInBlock` table; the Run
  Speed test (31 checks) ran that way. Mixins, WTHIT and rendering still need the game.
- **Rhino-only script bugs:** the headless Rhino harness (see Claude's memory
  `kubejs-headless-rhino-harness`) runs the real scripts in the pack's Rhino.

## 7. In-game test suite

**Setup:**
- Creative world with cheats, standing still (not flying) while you read values.
- Build a test strip: stone, IP asphalt, concrete, glass, wool, sand, honey block, a Quark grate on a block.
- Copy of the commands:
  - `M` = `/attribute @s minecraft:generic.movement_speed modifier value get 23237052-61ad-11eb-ae93-0242ac130002`
  - `T <tag>` = `/execute if block ~ ~-1 ~ #<tag>` (prints "Test passed" when the block under you is in the tag)
- Step onto a block **before** running `M`: speed is only recalculated on a block change.
- Expected `M` values are for a player (base 0.1). Floating point may print e.g. `0.04999…`.

**Tier data**
- [x] **BR-01 Tags load clean:** after world load, search `logs/latest.log` for `BlockRunner tier overlaps checked`. → `… ids, 0 overlap(s)`. PASSED 2026-09-26 at 1927 ids and 2026-09-27 after the whole-pack scan (log `6062 ids, 0 overlap(s)`). The log's id count runs lower than the real tier size: the 11:54 `/dumptags` export has 6126 tiered ids, which matches the sim exactly. Treat the dump as the count and the log line as the overlap check.
- [x] **BR-02 Basic tiers are empty:** stand on a dirt path. `T blockrunner:quick_blocks` → fails; `T blockrunner:surface/natural_path` → passes.
- [x] **BR-03 Fast tiers:** `M` on asphalt → **+0.05**; on concrete → **+0.04**; on a Quark grate → **+0.03**; on stone → **+0.005**.
- [x] **BR-04 Slow tiers:** `M` on sand → **−0.015**; on wool → **−0.012**; on a honey block → **−0.055** (jumping on honey is still damped).
- [x] **BR-05 1.00 tiers:** `M` on glass and on grass → "no modifier". `T blockrunner:surface/glass` on glass passes.
- ~~**BR-06 Tooltips**~~ moved to [TT-03](tooltips.md).
- [x] **BR-07 FOV stays put:** sprint from stone onto asphalt → no FOV zoom beyond normal sprinting.
- [ ] **BR-08 Mobs too:** walk a horse onto asphalt, then `/attribute @e[type=minecraft:horse,limit=1,sort=nearest] minecraft:generic.movement_speed modifier value get 23237052-61ad-11eb-ae93-0242ac130002` → half the horse's base speed (a positive number).
- [ ] **BR-09 Soul sand untouched:** `M` on soul sand → "no modifier". You are still slowed, and Soul Speed boots still cancel it.
- [ ] **BR-10 Thin blocks don't register:** put a carpet on concrete and stand on it. `M` → **+0.04** (the concrete's value).

**Sticky facing (3.2)**
- [x] **BR-11 Sticky piston:** facing up → `M` **−0.035**. Facing sideways → "no modifier". Facing down → "no modifier".
- [x] **BR-12 Piston heads:** extend an upward sticky piston with redstone and stand on the head → **−0.035**. An upward *plain* piston's head → "no modifier".
- [x] **BR-13 Other sticky blocks:** Create sticky mechanical piston facing up → **−0.035**. CTM sticky launcher (any placement) → **−0.035**. Supplementaries spring launcher → "no modifier".

**Camouflage speed (3.3)**
- [x] **BR-14 Empty frame:** `M` on an empty framed cube → "no modifier".
- [x] **BR-15 Copies camo:** right-click the frame with concrete → step off/on → **+0.04**. Re-camo with honey → **−0.055**, with wool → **−0.012**.
- [x] **BR-16 Create copycat:** a copycat step with concrete as its material → **+0.04**.
- [x] **BR-17 Newest wins (FramedBlocks):** PASSED 2026-09-27.
  - Place a framed slab on a framed slab (makes a framed double slab).
  - Camo the bottom half with oak planks, then the top half with concrete. → **+0.04**.
  - Remove the concrete with the Framed Hammer, step off/on → **+0.01** (falls back to the oak half).
- [ ] **BR-18 Survives reloads:** redo BR-17's end state (oak first, concrete second). Relog or reload the chunk → still **+0.04**.
- [ ] **BR-19 Newest wins (Copycats+):** a Copycats+ copycat slab doubled up: wool first, concrete second → **+0.04**. Swap the order on a fresh one → **−0.012**.
- ~~**BR-20 Tooltips**~~ moved to [TT-04](tooltips.md).
- [ ] **BR-21 copyCamo off:** set `[surface.camouflage] copyCamo = false` and save (live reload). Step off and back onto the concrete-camo frame from BR-15 → "no modifier". Set it back to `true`. (The tooltip half is [TT-05](tooltips.md).)

**Visual sinking (3.4)**
- [x] **BR-22 Camera drop:** set `[surface.sinking] depthPixels = 8` for visibility. In first person, walk from stone onto wool and back. → The view drops half a block, and rises back smoothly over a tick.
- [x] **BR-23 Third person:** F5 on sand. → The body sits in the sand, the shadow stays on the sand's top, and F3+B's hitbox box stays on top.
- [x] **BR-24 Others sink:** a pig on wool, a dropped item on sand and a ridden horse on sand all sit lower. The rider sinks with the horse.
- [ ] **BR-25 No sink where it shouldn't:** carpet on wool, a thatch slab, mid-jump over sand → no extra sink. (Honey changed 2026-11-10, see BR-54.)
- [ ] **BR-26 Off switch:** `depthPixels = 0` → nothing sinks. Set it back to `2` afterwards.
- [ ] **BR-53 No bump between sunken blocks (2026-11-10):** default `depthPixels = 2`, first person. Walk slowly across slime → honey → slime, wool → mud → wool, and wool → dirt path. → The view never pops up at the seam; it only moves once you are on the next block (none at all slime/honey and wool/mud, a 1 px drop onto the path).
- [ ] **BR-54 Even depth:** F5 beside a pig on wool and one on a honey block. → Both sit equally deep (honey 1 px real + 1 px drawn).
- [ ] **BR-55 Edge stays sunk:** on wool, walk toward an edge over a 1-block drop until the hitbox is half over air. → Still sunk; it rises only as you fall.
- [ ] **BR-56 Items lie on top:** drop items on wool, sand and a slime block. → Each lies on the texture at its real height (not hidden 2 px inside). Then add `t.add(e, 'creativecompat:sinking_hides_items', ['minecraft:sand'])` in `blocktags.js`, `/reload` → the item on sand sinks in; wool unchanged. Remove the line afterwards.

**Camo sinking (3.4, needs the rebuilt patcherMod jar)** (`[surface.sinking] depthPixels = 8` makes these easy to see)
- [ ] **BR-30 Frames sink like their camo:** a framed cube camo'd with wool → you sink on it (camera and model). Re-camo with stone → no sink. An empty frame → no sink.
- [ ] **BR-31 Hybrids use the newest camo:** framed double slab, stone first, then wool → sinks. On a fresh one, wool first, then stone → no sink.
- [ ] **BR-32 Non-full frames don't sink:** a single framed slab camo'd with wool → no sink.
- [ ] **BR-33 Independent switch:** set `[surface.sinking] copyCamo = false` → the wool frame from BR-30 no longer sinks, but `M` on it still reads wool's **−0.012** (walk speed follows `[surface.camouflage]`). Set it back to `true`.

**Surface config (3.7, in game since 14:49; the trimmed json needs a restart)**
- [ ] **BR-39 Section is there:** open Create's Configs screen for Creative Engineering, Common → a **surface** folder holding speed, sticky, camouflage and sinking. `speed` lists all 26 tiers, each showing its description. No `block_sinking` or `camouflage_speed` left at the top level.
- [ ] **BR-40 Speeds come from the toml:** `latest.log` has `[surface] 26 tier speeds applied to BlockRunner from [surface.speed]` and no `without BlockRunnerSpeedConfigMixin` error (log half seen 2026-09-27 15:21, with the old json still listing the tiers). After a restart with the trimmed json: `M` on asphalt → **+0.05**, on sand → **−0.015**.
- [ ] **BR-41 Live edit:** in the Configs screen set `surface.speed.asphalt` to 2.0 and save. Step off and back onto asphalt → `M` **+0.1**, and WTHIT on placed asphalt reads the 2.0 speed. Set it back to 1.5.
- [ ] **BR-42 Sticky switch:** set `[surface.sticky] faceUpOnly = false`. Step onto a sideways sticky piston → **−0.035**. Set it back to `true` → "no modifier" after stepping off and on.
- [ ] **BR-43 Hybrid wording:** framed slab, framed panel, a framed double slab, a Copycats+ copycat slab → "Speed Multiplier: matches its camo (newest camo sets walk behaviors for hybrid blocks)". Framed cube → "…matches its camo". (Needs `[surface.camouflage] copyCamo = true`; with false every frame reads "1.0, no matter its camo".)

**WTHIT Run Speed line (3.8, needs a restart after the 16:45 deploy)**
- [ ] **BR-44 Line shows:** look at IP asphalt → "Run Speed: 1.50× (Asphalt)", number green. Sand → "0.85× (Dust)", red. Stone → "1.05× (Natural Smooth)". Glass and grass → no line (1.00, `hideNormal`).
- [ ] **BR-45 Frames and sticky:** a framed cube camo'd with concrete → "1.40× (Concrete, from camo)". A sticky piston facing up → "0.65× (Sticky)". Facing sideways → no line; with `hideNormal = false` → "1.00× (Sticky, sticky face not up)".
- [ ] **BR-46 Live and configurable:** set `[surface.speed] asphalt = 2.0` and save → the asphalt line reads 2.00× without relooking. `format = PERCENT` → "+100%". `showTier = false` → number only. `enabled = false` → no line. Put everything back.
- [ ] **BR-47 Plugin loads:** WTHIT's plugin list (its config screen, Plugins) includes `creativecompat:run_speed`; `latest.log` has no WTHIT plugin error for it (log half seen 2026-09-27 14:50 with the first version).
- [ ] **BR-48 Passable blocks:** tall grass, a flower, a wheat seedling, a pressure plate → no line, even with `hideNormal = false` and `hideUntyped = false`. A cobweb on grass → "0.11× (walking through)". A grown sweet berry bush → "0.36× (walking through)". A 5-layer IW leaf pile → "0.45× (walking through)"; 2 layers → no line.
- [ ] **BR-49 Layers and powder snow:** a 1-layer and a 2-layer snow on sand → "0.85× (Dust, block below)"; 3 layers → "0.75× (Snow)". A carpet on planks → "1.10× (Wood, block below)". Powder snow bare-footed → "0.41× (walking through)"; in leather boots → "0.75× (Snow)".
- [ ] **BR-50 Own speed:** soul sand → "0.58× (own speed)".
- [ ] **BR-51 Hide switches:** set `hideNormal = false` (keep `hideUntyped = true`) → glass shows "1.00× (Glass)", a crafting table shows nothing. Then `hideUntyped = false` → the crafting table shows "1.00×". Put both back to `true`.

**Native property resets (3.5)**
- [ ] **BR-28 D&D asphalt matches IP asphalt:** lay a strip alternating IP asphalt and D&D asphalt, and walk (then sprint) along it. → `M` reads **+0.05** on both. The pace doesn't change between them; before the fix you'd shoot ahead on the D&D blocks and slide when stopping.
- [ ] **BR-29 D&D asphalt is walkable if untiered** (optional): temporarily remove `create_dd:*_asphalt_block` from the asphalt tier and `/reload` → normal walking speed, not frozen. Put it back afterwards.

**Whole-pack scan (2026-09-27, needs a game restart: tag_helper.js and item_dict.js are startup scripts)**
- [ ] **BR-34 Modded tiers:** `M` on a Create Deco brick → **+0.025**; a Dustrial Decor cinder block → **+0.04**; an Engineer's Decor steel catwalk → **+0.03**; Railways locometal → **+0.025**; an Architect's Palette oak board → **+0.01**.
- [ ] **BR-35 Fence tops and beds:** stand on top of an oak fence → **+0.01** (wood). On a bed → **−0.012** (soft), and with `depthPixels = 8` you don't sink into it.
- [ ] **BR-36 Gourds don't sink:** `M` on a pumpkin → **−0.015** (confectionery). With `depthPixels = 8` no sink; a froglight next to it (soft, full block) does sink.
- [ ] **BR-37 Blocks with no tags:** a froglight → **−0.012**; D&D padded rubber → **−0.012**; tilled end stone → **−0.02**. These only work because the scan found them.
- [ ] **BR-38 SecurityCraft copies:** reinforced stone and a stone mine → **+0.005** (same as stone); reinforced concrete → **+0.04**; a reinforced sticky piston facing up → **−0.035**, sideways → "no modifier".

- [ ] **BR-52 1.0.4 mod blocks:** after a restart, a Fruits Delight crate → wood tier; a Farmer's Respite tea leaves sack and a Corn Kernel Bag → soft; a placed Hamimelon and a Coffee Cake → confectionery; an apple jelly block and an apple jello block → very sticky (same as honey / slime). The tier overlap log line still says 0 overlaps.

- [ ] **BR-57 Visceral Heap flesh (2026-11-10):** after a restart, in the Heap (or placed): `M` on flesh → **−0.04** (spongy); on porous flesh → **−0.055** (very sticky). Jump on porous flesh → about half a normal jump, no bounce when landing from a height. Both sink 2 px.
- [ ] **BR-58 Wall slide (3.9):** survival, build a 10-high porous flesh pillar beside a honey pillar. Jump off the top pressed against each side. → Both slide down at the same slow speed with the slide sound and particles (flesh-coloured on flesh), no fall damage at the bottom. Standing on top, or jumping next to it without touching → no slide. A pig pushed off beside it slides too. Plain flesh → no slide. A Banana Slug Slime Block pillar slides the same way, with yellow particles, no fall damage.

**Server side (only when the dedicated server is back)**
- [ ] **BR-27 Server parity:** with the new patcherMod jar on the server, repeat BR-11, BR-15 and BR-17 while connected. → Same values, and no rubber-banding when walking across camo'd frames.

## 8. History

- **2026-09-25:** HappyTrails added alongside BlockRunner.
- **2026-09-26:**
  - HappyTrails uninstalled (duplicate feature, FOV warp).
  - The six basic-tier helper setup was rebuilt as `blockrunner:surface/*` category tiers; the dedicated speed helpers were removed; `addMatching` gained `#tag` expansion and `overlaps()` was added.
  - sticky / very_sticky split; sculk made soft.
  - Sinking investigated, hitbox approach rejected, built as visual model + camera sink with tick blending.
  - Facing-aware sticky mixin.
  - You retuned the values.
- **2026-09-27:**
  - Whole-pack scan in JEI mod order → added `grating` (1.30), `camouflage` (1.00) and `confectionery` (0.85); glass split into its own tier; Supplementaries spring launcher removed from sticky.
  - Camo walk speed with the newest-material rule and tooltip; wording changed to "no matter its camo".
  - This writeup and `tools/blockrunner_tag_sim.js` added.
  - BR-17 passed in game: the newest camo sets the speed on combined framed slabs.
  - Framed slab/panel count as hybrid-capable for the Speed Multiplier line (wording "the newest one in a hybrid"). The hybrid and schematic tooltips and the "Framed Copycat" rename are in [tooltips.md](tooltips.md).
  - Framed/copycat blocks sink like their newest camo, behind a new independent `[block_sinking] copyCamo` (BR-30..33; now `[surface.sinking]`).
  - D&D asphalt's own friction boost removed (`block_properties.js`); IP asphalt's "Increases walking speed" tooltip removed; tooltip tests moved to [tooltips.md](tooltips.md) (BR-06, BR-20 retired).
  - Whole-pack scan over every jar + KubeJS (new `tools/blockrunner_scan.py`): tiered ids 2278 → 6120, 0 overlaps.
    All modded brick/stone/metal/wood families, wooden fences/gates/trapdoors, beds → soft, gourds →
    confectionery, ores via `#forge:ores`, untagged blocks (froglights, rubber, catwalks). New `t.mirror()`
    helper puts SecurityCraft reinforced blocks and mines in their vanilla block's tier. The sim now loads
    the item/block dicts and the untagged-block list. BR-34..38 added.
  - All surface settings moved into one `[surface]` config section (speed / sticky / camouflage / sinking),
    with a speed entry per tier replacing the json tier values. New `[surface.sticky] faceUpOnly` switch.
    The hybrid Speed Multiplier wording changed to your "newest camo sets walk behaviors for hybrid blocks".
    Built and headless-tested (14/14); not deployed, because the game was open. BR-39..43 added.
  - WTHIT "Run Speed" line (3.8): reads BlockRunner's speed map plus the shared camo/sticky rules, tier names
    from a cached Block-to-tier map, `[surface.wthit]` knobs. Built, formatting/config headless-tested (10/10). BR-44..47 added.
  - Run Speed line, more granular: new `hideUntyped` switch next to `hideNormal`. Passable blocks (plants) no longer
    show a speed. Thin layers (carpet, 1-2 snow layers) show the block beneath. Blocks that slow you from inside
    (cobwebs, berry bushes, IW leaf piles, bamboo leaves, barbed wire, powder snow...) show their real walking
    speed, and soul sand shows its own 0.58×. Built, headless-tested with MC bootstrapped (31/31); not deployed.
    BR-48..51 added.
  - Deployed at 16:45 with the game closed. The 13:12 jar (surface config, first Run Speed line, camo sinking)
    had already been running since the 14:49 restart. The toml sections had moved with no values lost, and
    the log showed `26 tier speeds applied`, 0 tier overlaps (6085 ids) and the WTHIT plugin initialised.
    Now in `mods/`: the latest jar and the trimmed `blockrunner.json` (backed up); `deploy_pending/` removed;
    `pack/` manifest regenerated. Writeup reviewed end to end; Twigs bamboo leaves noted as a leaves-tier
    member that can't register.
  - Re-check against the newest mods (houses, boats, Breezy, Golden Food, Canned Goods, Tobacconist, Create Fast
    Schematic Cannon), using the 11:54 dumps. Only 4 walkable-ish new blocks: Golden Food's two cakes → confectionery,
    Canned Goods' sealed canned dirt → metal. Opened canned dirt shows dirt on top and stays untiered (1.00, same as
    dirt); the hookah (furniture) and tobacco crops are left out like other thin/functional blocks. The house, boat and
    cannon mods add no blocks. 6129 ids, 0 overlaps. In game, the pre-change set loaded with 0 overlaps (BR-01 passed).
- **2026-10-02** Re-check for the mods added since 09-27 (Fruits Delight, Farmer's Respite, Corn Delight, More
  Delight, Well, Untitled Duck Mod, Blue Flame, plus redeco / Floral Enchantment / Upgrade Aquatic blocks the scan now
  sees), from the `/dumpall` tag export. Tiered by the existing precedents: `fruitsdelight:*_crate` + Corn Crate → wood
  (like FD crates); Farmer's Respite `*_sack` + Corn Kernel Bag → soft (sacks and bags); Hamimelon (a melon-like gourd),
  Coffee Cake, Rose Hip Pie, Fig Pudding, Mangosteen Cake, Pineapple Fried Rice, Nachos and Popcorn Box → confectionery;
  Fruits Delight jelly blocks (`HoneyBlock` subclass) and jello blocks (`SlimeBlock` subclass) → very sticky. Left out
  like before: cauldrons, jelly bottles, plants, the kettle and the wells (furniture). New single ids come from new
  item_dict keys (173 added for the new mods), so a restart is needed. Sim: 6355 ids, 0 overlaps, no dead patterns.
  BR-52 added.
- 2026-11-01: Vintage Delight port tiered (`vintageport #2`, [vintage_delight.md](vintage_delight.md) 4.0 step 21): crates
  wood, oat bag and bale soft (changed 2026-11-02, see below), salt bricks and mixed salt bricks constructed brick, Smooth Salt Block and Salt
  Pillar constructed smooth, the salt blocks (The Salt's Salt Block and raw rock salt block, Vintage's Salt Block)
  soft (changed 2026-11-02, see below), Cheese and Meat Pizza confectionery. Sim: 6453 ids, 0 overlaps; jars, machines and wild plants left
  untiered. Test SQ-VTD7-02.
- 2026-11-02: owner decision on the r1102b SQ-VTD7-02 fail: Vintage oat bags and bales, Farmer's Delight straw/rice bales, hay
  bales and the Salt Blocks (The Salt's Salt Block and raw rock salt block, Vintage's Salt Block) stay in `soft` (0.88x,
  `blocktags.js` lines 327 and 341-346); the changelog bullet now says 0.88x. SQ-VTD7-02 rewritten to expect `soft`.
- 2026-11-10: sinking reworked in Creative Compat `SinkingRender` (3.4): the block holding the feet decides (hitbox
  corners when the centre is past an edge), so crossing between sunken blocks no longer pops the camera; sinking blocks
  with a real sink of their own (honey, mud) add only the rest of the depth; dropped items lie on top unless the block is
  in the new `#creativecompat:sinking_hides_items` (empty). Visceral Heap flesh tiered: flesh spongy, porous flesh very
  sticky with honey's jump factor (3.5). BR-25 edited, BR-53..57 added. Built, not installed (game open).
- 2026-11-10: wall slide for non-honey blocks (3.9, `WallSlide` + `WallSlideMixin`, tag `#creativecompat:wall_slide`),
  porous flesh in it; sticky-block survey with proposals in 3.9. BR-58 added. Built, not installed.
