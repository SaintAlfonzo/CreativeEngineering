# Feature porting: small mods the pack could absorb into its own code

Test prefix `FP-`. Status: **research done 2026-10-18; ported: Fast Leaf Decay (2026-10-19), tier 1 (8 mods, 2026-10-20,
KubeJS + data, section 1.1) and Glowing Illagers, Comfortable Campfires, Bottled Air (2026-10-20, patcherMod 1.1.01,
section 2.4); all BUILT, scratch-tested where section 7 says so (tier 1 recipes, Glowing raiders, Bottled Air scratch server PASSED 2026-10-22, SQ-OM-00, SQ-OM-10 to SQ-OM-12, SQ-OM-14), not deployed. Book Fishing stays (1.1).** The first version of this file (a
candidate list from mod names and configs) is replaced: every tier 1-3 mod was decompiled
(`tools/decompile_mod.py`, jars fetched from the manifest links and checked against its sha1) and read, plus about 50
other small mods for the wider survey in section 3.

## 0. At a glance

- **Where ports go:** recipes through `global.NCH`, tags through `global.tags`, item ids through `item_dict.js`
  (CLAUDE.md). Event-only logic goes into KubeJS when it needs no mixin, otherwise into patcherMod
  (`development/patcherMod`, one package per feature, knobs in `CEConfig`).
- **The rule that decides the tier:** a mod that registers anything saved in the world (items, blocks, entities,
  enchantments, block-state properties) can't be removed without keeping its ids. Enchantment ids sit as strings in
  item NBT, so a re-registered `creativeengineering:` copy doesn't carry existing enchanted items over. Registry entries
  that are never saved (memory module types, sensor types, sound events, gamerules) are free to re-namespace.
- **Corrections to the old list** (found in the code): Create Recycle Everything, Create: Netherless and Just Another
  Rotten Flesh to Leather register items the pack uses (`i.nugget_netherite`, `i.fragment_netherite`, `i.prepared_flesh`
  ...), so they are tier 4, not tier 1. Witch Mobility is a GeckoLib broom-and-armor content mod (10 broom entities),
  not "one attribute change": not a candidate. Effishiency registers an enchantment: tier 4. Death Knell writes
  flavour death messages, not death coordinates. Book Fishing spawns a separate book on a catch, it doesn't edit the
  loot table. Unlit Campfire adds a block-state property (`infinite`) to every campfire: a medium port, not a tick check.
  Bushier Flowers registers the grown-flower blocks still in the world: tier 4. Bottled Air and Charm of Undying
  register nothing, so they move up (tiers 2 and 3).
- **Free wins with no port:** Rain Should Extinguish Campfires (`rsec`) duplicates Unlit Campfire's own rain rule
  (`campfireRainUnlitTime = 160`): remove it outright (2.2).
- **Library payoff:** Collective is needed only by 9 mods (Modrinth dependency data, 285 of 418 mods covered): Bottled
  Air, Breedable Killer Rabbit, Cycle Paintings, Easy Elytra Takeoff, Fish On The Line, Furnace Recycle, Ice Prevents
  Crop Growth, Smaller Nether Portals, Starter Kit. Porting all 9 drops Collective too (check the 133 CurseForge-only
  mods for it first). Balm, Cloth Config, Placebo and Moonlight stay either way (Waystones, Cooking for Blockheads,
  BetterF3, Supplementaries...).
- **Death Knell lang (checked 2026-10-20, not a live bug):** its messages are translation keys whose lang lives in the
  jar, so a client without the jar would see raw `message.deathknell.*` keys. But Death Knell has been `both` in
  `mod_sides.md` since 1.0.7 (DEPLOY_LOG: "server -> both"), and the deploy's `mods` category goes to the client as
  well as the server, so every client has the jar; its en_us has all 55 keys the code uses. The research above read
  an older side. FP-01 now only confirms it in game. Porting it later still needs the lang on both sides.

**Effort summary (sections 1 and 2):**

| Tier | Mods | Total new code | Removes |
|---|---|---|---|
| 1, data or KubeJS | 9 | ~300 KubeJS lines + recipe/loot files | 9 jars |
| 2, one handler or mixin each | 16 | ~1,000 patcherMod lines, ~12 mixins | 16 jars (+ rsec for free) |
| 3, already patched or bigger | 7 | ~1,200 lines, ~10 mixins, 2 with client parts | 7 jars and 5 of our own mixins into them |

## 1. Tier 1: recipes, loot or a KubeJS handler

| Mod | What it really does (from the code) | Port | Effort | Notes |
|---|---|---|---|---|
| Nametag Crafting | 1 shaped recipe | `NCH` shaped | minutes | |
| Create Waystone Recipes | 1 shaped recipe (no classes) | `NCH` shaped | minutes | |
| AE2 QoL Recipes | 311 recipes (194 shapeless, 117 shaped), 2 tags (`can_remove_color`, `buckets/water`) | `/kubejs export` the list, rebuild the ones the pack keeps through NCH | an afternoon | most are dye/undye and convenience recipes; check which ones existing pack scripts already remove |
| Create: Copper Splashing | 33 `create:splashing` recipes (no classes) | NCH Create helpers | an hour | |
| Furnace Recycle | 26 smelting recipes (tools/armor to nuggets); needs Collective | NCH smelting | an hour | one of the 9 Collective users |
| Create: Easy Stone Generators | 7 recipes, one empty class | NCH | an hour | not in the old list |
| Copper Drops | 14 loot tables, no classes | copy the tables into `kubejs/data` (or LootJS) | an hour | not in the old list; check they don't fight `loot_config.json` |
| Book Fishing | `ItemFishedEvent`: a 7-step chance ladder (16% plain book down to 0.25% level-60 enchant), +50% of the step per Luck of the Sea level / 3, book spawned at the player as a separate item | KubeJS `ForgeEvents.onEvent('...ItemFishedEvent')` or LootJS on the fishing table | an hour | as LootJS it lands in the catch instead of next to it; decide which (FP-02) |
| Starter Kit | 6.7k lines (kit GUI, `/sk` commands), but the pack uses one path: `randomizeMultipleKitsToggle = true` with 2 active kits (Default, Engineer: boots, shield, tools, bread); marker = scoreboard tag `collective.firstJoin.starterkit` | KubeJS `PlayerEvents.loggedIn`: if the tag is missing, give one random kit (armor/offhand slots too), add the tag | ~60 lines | keep the same tag so nobody gets a second kit; kit items through `item_dict`; `addExistingItemsAfterKitSet` = keep books other mods give on join (order matters, FP-03); a Collective user |

### 1.1 Tier 1 ported (2026-10-20, BUILT)

User: "execute each of the feature ports tier 1, excluding the book fishing, which will be part of a larger ebook
loot pool rebalance". Eight jars went to `disabledmods/`, their `mod_sides.md` rows and config files are gone,
`server_mods/` and `pack/` regenerated. **Book Fishing stays installed** (jar, row, FP-02 and D1 open) until the
enchanted-book loot rebalance ([loot.md](loot.md)); [TODO](todo/TODO.md) `ports/featureporting #2` notes it.

| Mod | Port | Where | Count | Notes |
|---|---|---|---|---|
| Name Tag Crafting Recipe | shaped recipe, leather + paper over 2 string | [feature_ports.js](../kubejs/server_scripts/other/feature_ports.js) | 1 | id `creativeengineering:shaped/name_tag` |
| Create: Copper Splashing | `create:splashing`, each copper shape weathers one stage (plain, exposed, weathered, oxidized): copper block, cut copper + slab + stairs, Quark vertical slab, Create shingles / tiles + slabs + stairs | feature_ports.js | 33 | generated from the 11 shapes |
| Furnace Recycle | smelting, 0.1 xp, 200 ticks: 13 items to an iron ingot, 9 to an iron nugget (lantern or soul lantern count as one recipe), anvils (3 states) to an iron block, bell and light weighted plate to a gold ingot, powered rail to a gold nugget | feature_ports.js | 26 | the mod was data only (its one class is empty); the old table above said "tools/armor to nuggets", it is blocks and tools to ingots or nuggets |
| Create: Generators | **port removed 2026-10-21.** `resource_blocks.js` had removed 5 of its 6 recipes long before (cobblestone, stone, basalt, limestone, scoria: the pack's 3-heat generators replace them), and the port brought those 5 back. Only the one recipe players really had is kept: water + lava 250 mB each, heated, both used up -> obsidian, now `creativeengineering:obsidian_generation_heated` in [resource_blocks.js](../kubejs/server_scripts/engineering/resource_blocks.js) | resource_blocks.js | 1 | the jar was `create_generators-1.0.0` ("Easy Stone Generators" is its listing name); the port also dropped the obsidian recipe's heat. Jar deleted from `disabledmods/`, its `pack/` rows removed |
| AE2 QoL Recipes | its 19 cable templates x 16 colours + 7 fluix: ring of 8 cables around a dye -> 8 coloured (5 kinds), cable + dye, covered from glass + wool, smart from fluix glass + redstone + glowstone + wool / own covered + dusts / any covered + dusts + dye, 2x2 thin -> dense, dense -> 4 thin, smart dense from covered dense + dusts, fluix by a water bucket (`#ae2:can_remove_color`) in the ring | feature_ports.js; tags in [feature_port_tags.js](../kubejs/server_scripts/tags/feature_port_tags.js); 85 `ae2:*_cable` keys added to item_dict | 311 | AE2 had no item_dict keys at all before this |
| Copper Drops | `minecraft:copper_ore` and `deepslate_copper_ore` drop 1 raw copper (Fortune ore formula, silk touch gives the ore) instead of vanilla's 2-5 | [kubejs/data/minecraft/loot_tables/blocks/](../kubejs/data/minecraft/loot_tables/blocks/) | 2 of 6 | the mod's other 4 tables (Ad Astra, BNO, Deeper and Darker ores) target mods the pack doesn't have |
| Starter Kit | `PlayerEvents.loggedIn`, 10 ticks later: one of the two active kits at random (Default: leather boots, shield, wooden sword, 9 bread; Engineer: leather boots, copper shield, copper pickaxe and axe, 8 bread), inventory emptied first and the player's items (books from other mods) given back after, effects cleared | [starter_kit.js](../kubejs/server_scripts/other/starter_kit.js) | 2 kits | see below |
| Create Waystone Recipes | **not rebuilt** | | 0 | its one recipe overrode `waystones:warp_stone` with `createcasing:chorium_ingot` corners; Create Casing isn't in the pack, so the override could never load and Waystones' own recipe (amethyst corners) is what players had. Decision D6 |

**Starter Kit, who already has a kit:** the mod never relied on the `collective.firstJoin.starterkit` tag alone (the
old table said so; the code checks the tag, then its own file). Its record is `<world>/data/starterkit/tracking.json`
(`"multiplayer": {uuid: true}` on a dedicated server, `"singleplayer": {<world folder>: true}` in single player), and on
every start it marked every player with a `playerdata/*.dat` file as served. So on the live server every existing
player is in that file. The port reads it, skips those players (and gives them the tag), gives the kit only to a
player in neither, then writes the same file and adds the tag. Not ported: the kit-choice screen (never shown:
`randomizeMultipleKitsToggle` was on), `/sk` commands, kit effects (both kits had none), the "select hotbar slot 1"
packet. The three inactive kits were: Archer (leather helmet and boots, Power I bow, 6 baked potatoes, 32 arrows),
Lumberjack (iron boots, 4 oak saplings off hand, Efficiency I iron axe, 8 steak), Witch (golden boots, stick, 16
apples, splash potions of strong swiftness, healing, poison, 2x harming). Content changes are
[TODO](todo/TODO.md) `items/starter-kit #1`.

Moved out of tier 1: JRFTL (`jrftl:prepared_flesh` is a pack ingredient in `mob_resources.js`), Create Recycle
Everything (3 items, all mapped in `item_dict`), Create: Netherless (4 items), Death Knell and Glowing Illagers (now tier
2, they need a mixin or an event in Java), Witch Mobility (content).

## 2. Tier 2: one event handler or mixin each (patcherMod)

### 2.1 Candidates

| Mod | What it really does | Port | Lines | Side | Notes |
|---|---|---|---|---|---|
| Ice Prevents Crop Growth | `CropGrowEvent.Pre` DENY when the block 2 below the crop (under the farmland) is ice, packed, blue or frosted ice | event in `crops/` | ~15 | server | `crops/CropGrowth` is the WTHIT growth-line logic, not a growth hook; the port could add a "won't grow: ice" WTHIT line there. Collective user |
| Glowing Illagers | mixin `Raid#addWaveMob` HEAD: Glowing II forever on every raider | KubeJS `EntityEvents.spawned` (raider with `getCurrentRaid()` set) or the same 1-inject mixin | ~10 | server | |
| Death Knell | mixin `CombatTracker#getDeathMessage` RETURN: random flavour messages by killer type (blaze, cave spider, slime, polar bear, drowned, guardian), weapon tags (`deathknell:cookies`, `books`, `axes`), falls and drowning | the mixin + `DeathKnellCommon` logic + the 3 lang files in patcherMod assets | ~200 + lang | both (lang) | fixes the raw-key bug above; keep the 3 item tags under our namespace |
| Comfortable Campfires | mixin `CampfireBlockEntity#litServerTick` HEAD: every lit campfire, **every tick**, queries players in a 3-block box and gives FD Comfort (or Regeneration) for 5 s | mixin with a 20-tick gate per campfire | ~40 | server | the gate is a perf win over the mod |
| Friendly Griefing | `EntityMobGriefingEvent`: only when gamerule `mobGriefing` is **false**, ALLOW villagers, DENY everyone else | event | ~15 | server | does nothing if the live server keeps `mobGriefing` true: confirm the gamerule first (FP-04) |
| Golems Are Friends Not Fodder | armorer/toolsmith/weaponsmith work package gets a `RepairGolem` behaviour; a sensor (every 241 ticks) remembers a damaged golem; the villager walks to it and heals 25 | mixin on `VillagerGoalPackages#getWorkPackage` + our own memory and sensor types + `EntityJoinLevelEvent` | ~150 | server | memory/sensor ids aren't saved, so re-namespacing is safe |
| Breedable Killer Rabbit | `BabyEntitySpawnEvent`: 5% of baby rabbits are type 99, name removed; golden carrot tames (stored as the carrot in its main hand); a tamed one doesn't attack | 5 events | ~80 | server | tamed state is vanilla NBT, so existing tamed rabbits keep working. Its "wants a golden carrot" message fires for the wrong rabbits (inverted check); fix it in the port. Collective user |
| Cherished Items | mixin on the 3 `ItemEntity` constructors: renamed or enchanted items 6000 ticks, tag `shortened` 1200, `extended` 12000 | 1 mixin + accessor, 4 item tags | ~60 | server | check against ItemPhysic's own despawn handling ([item_physics.md](item_physics.md)) |
| Loading Protection | on join: invulnerable, untargetable and harmless for 150 s or until moved 0.1 block; chat countdown; client part draws an icon by the name (own font, packet) | server events; drop the icon or keep it (needs a packet + font) | ~100 (+60 client) | server (+client) | uses wall-clock time, not ticks |
| Effishiency | **enchantment** `effishiency:effishiency` + a loot modifier (2..level+1 copies of the catch, durability per fish) | | | | **tier 4**: the enchantment id is in item NBT. Port only with a pack-wide decision on enchantment ids |
| Don't Void Arrows | player-shot `Arrow`/`SpectralArrow` (bow without Piercing or Infinity) that hits a mob: 80% counted in the mob's persistent data, 20% breaks with a sound; on death the mob drops the count as plain arrows; gamerule `dropSpectralArrows` | `LivingAttackEvent` + `LivingDropsEvent` | ~60 | server | port improvement: any `AbstractArrow` via its pickup item, so tipped and Nock Enough Arrows arrows come back as themselves ([archery.md](archery.md)) |
| Fast Leaf Decay | `NeighborNotifyEvent` on air: leaves next to it get a decay tick 4-11 ticks later through its own list | event + `level.scheduleTick` | ~30 | server | **PORTED 2026-10-19** into the pack's leaf decay ([mob_block_tweaks.md](mob_block_tweaks.md) 2.5): `block/FastLeafDecayPort` queues vanilla scheduled ticks (`[leaf_decay] decayMinTicks` / `decayMaxTicks`, 4 / 11) and the decay itself is `LeavesDecayRangeMixin`'s tick hook; jar in `disabledmods/`, MT-19 to MT-23 |
| SwingThroughGrass | `LeftClickBlock` on a block with no collision shape: ray-trace 4.5 blocks for a living entity and attack it | event | ~50 | both (the event fires client and server; the attack runs server side) | |
| Ice Boat Nerf | mixin `Boat#getGroundFriction` RETURN → **0.45 always** | 1 mixin | ~10 | both (boat physics run on the driver's client) | it sets every ground, not just ice: boats on land get slower than vanilla's 0.6 too. Port with "ice only" or as is (FP-05) |
| Bottled Air *(was tier 4)* | right-click a water bottle (or empty bottle with fire items) underwater to refill air; no items | `RightClickItem` | ~80 | server | Collective user |
| Fuel Goes Here *(new)* | mixin `AbstractFurnaceMenu#quickMoveStack`: shift-click fuel into the fuel slot | 1 mixin | ~40 | both (menu) | |
| Waystones Teleport Pets *(new)* | Waystones teleport event: brings the player's tamed pets along | event on the Waystones API | ~30 | server | needs Waystones on the compile classpath |
| Fish On The Line *(new)* | a bell in the off hand rings when a fish bites | `PlayerTickEvent` | ~60 | server | Collective user |
| Elytra Bombing *(new)* | gliding + igniter: drops primed TNT from the inventory | `RightClickItem` | ~60 | server | |

Tax Free Levels moved to tier 3: it is 20 mixins, not one formula (2.3).

### 2.4 Tier 2 ported (2026-10-20, BUILT, patcherMod 1.1.01)

User: "glowing illagers, comfortable campfires, bottled air can be ported in the same way". Three jars to
`disabledmods/`, rows out of `mod_sides.md`, `config/bottledair.json5` and `config/comfortable_campfires.json`
removed, `server_mods/` and `pack/` regenerated.

| Mod | Port | Config | Differences |
|---|---|---|---|
| Glowing Illagers | `mixin/RaidGlowingMixin`: HEAD of `Raid#addWaveMob` (`m_37718_`), Glowing for `Integer.MAX_VALUE` ticks | `[raids] glowingRaiders` (true), `glowingAmplifier` (1, the mod's Glowing II) | no particles or icon on the raider (the mod showed both; Glowing has no particles to speak of) |
| Comfortable Campfires | `campfire/CampfireComfort`, called from the existing `CampfireRainMixin` (RETURN of `cookTick`, so every lit campfire's server tick): players in a box `distance` out and up get Farmer's Delight's Comfort (or Regeneration) for `comfortSeconds`, refreshed when under 60 ticks remain | `[campfire] comfort` (true), `useComfort` (true), `comfortDistance` (3), `comfortSeconds` (5), `comfortEveryTicks` (20) | the mod queried players every tick per campfire; a campfire now checks once per `comfortEveryTicks`, spread by position. The mod took the effect from the config file `comfortable_campfires.json` (3 / 5 / true), same values |
| Bottled Air | `item/BottledAir` on `RightClickItem`: glass bottle in water -> `airPerBottle` air, vanilla fills the bottle; a fire source in the other hand takes one water bottle back out of the inventory (highest slot first) and returns the glass bottle unless it breaks; a water bottle can't be drunk under water | `[bottled_air] enabled`, `airPerBottle` (150), `noDrinkingUnderwater` (true), `fireEvaporates` (true), `bottleBreakChance` (0.5) | the mod's fixed list (campfire, soul campfire, fire charge, lava bucket, magma block) is the item tag `#creativeengineering:bottled_air/fire_sources` (feature_port_tags.js). Collective stays for Breedable Killer Rabbit, Cycle Paintings, Easy Elytra Takeoff, Fish On The Line, Ice Prevents Crop Growth, Smaller Nether Portals |

### 2.2 Rain Should Extinguish Campfires: remove, no port

Mixin `CampfireBlockEntity#cookTick` TAIL with one **static** countdown shared by every campfire in the game (2-6 s),
then: no non-air block in 64 above and 2 of 4 neighbours clear for 8 blocks → doused. Unlit Campfire already does the
same job per campfire with `isRainingAt(pos.above())` and `campfireRainUnlitTime = 160` (our default config). Two
systems race; remove `rsec` from `mods/` and `mod_sides.md` (FP-06). Done 2026-10-19: jar in `disabledmods/`.

### 2.3 Not tier 2 after all

- **Unlit Campfire** (730 lines): burn-out after 2000 ticks, rain timer, refuelling with burnables, drop items when
  unlit, and a new block-state property `infinite` on every `CampfireBlock` (structure campfires burn forever).
  The property must match on client and server and is saved in chunk palettes. Keep the mod unless there's a
  reason; a port is a medium job (~500 lines, both sides).
- **Tax Free Levels** (928 lines, 20 mixins): flattens XP costs (`levelBase 0`: an N-level cost is charged as levels
  0 to N) on the enchanting table, anvil (+ `removeAnvilLimit`, which needs a client mixin on `AnvilScreen` for "Too
  Expensive!"), and compat mixins for Waystones, Sophisticated Backpacks' anvil upgrade and Alex's Mobs' transmutation
  table (the pack has all three; Twilight Forest, Tetra, Backpacked, Disenchanting, Enchanting Infuser it doesn't).
  Porting = ~8 mixins incl. 1 client and 3 into other mods: tier 3, low payoff (Cloth Config stays for BetterF3).

## 3. Tier 3: mods the pack already patches, or bigger single features

| Mod | What it does | What a port needs | Lines | Deletes |
|---|---|---|---|---|
| Forgiving Void | player tick: below min height - 32, not teleporting → Blindness, eject, teleport to y 500 at the same X/Z, flag `ForgivingVoidIsFalling` in persistent data, anti-cheat off while falling; on landing the fall damage is 30 (`preventDeath` false) | the ~70 lines of logic + 2 accessor mixins (`ServerPlayer#isChangingDimension`, `ServerGamePacketListenerImpl#awaitingPositionFromClient`); the anchor pull from `ForgivingVoidMixin` becomes plain code; config merges into the existing `CEConfig.ForgivingVoid` | ~120 | the jar, `ForgivingVoidMixin`, `ForgivingVoidBridge`. Keep the persistent-data key so a player mid-fall at the update lands safely. Balm stays (other mods) |
| Responsive Shields | one `@ModifyConstant` on `LivingEntity#isBlocking` (5 → `Raise Time`, ours 0) | the same constant change in `shield/` (both sides: the client uses `isBlocking` for the raised pose) | ~15 | the jar. `ShieldBlocking` already documents the dependency |
| Easy Elytra Takeoff | rocket right-click while standing (3x3 layer 2 below not all air) with an elytra (or a "flight modifier"/"elytra curio modifier" attribute) → lift 0.2, start gliding, fire the rocket next tick; boosters too | ~60 lines + a server tick; the two pack mixins (`EasyElytraTakeoffGroundCheckMixin`, `...HelperMixin`) become direct checks of `#creativeengineering:elytra_chestplates` | ~90 | the jar, 2 of our mixins; a Collective user. Key by UUID, not player name |
| Second Chance | damage event: at ≥ 13.5 health, a hit that would kill from explosions or mobs leaves 1 health and plays `classic_hurt`; coyote time: a mixin on `LivingEntity#aiStep`/`jumpFromGround` allows a jump up to 10 ticks after leaving the ground | the event (~40 lines) + the mixin (3 injects) + the sound event and its `.ogg`/`sounds.json` in patcherMod assets | ~120 | the jar. The mixin hits **every** `LivingEntity`, so mobs get coyote jumps too; limit it to players in the port. Both sides (player movement is client-side) |
| Charm of Undying *(was tier 4)* | a totem in a Curios `charm` slot works from there: mixin on `checkTotemDeathProtection`, slot registered by IMC, a curio renderer | 2 injects + the IMC + optional renderer | ~150 | the jar; registers no items. Curios stays (Botania, Elytra Slot...) |
| Sit *(new)* | right-click a slab or stair to sit: an invisible seat entity, removed when you stand | entity type + renderer + 2 events | ~250 | the jar; the seat entity isn't saved, so its id can change |
| Bushier Flowers | | | | **tier 4 now**: `BushierFlowersDisableMixin` turns growth off, worldgen is off, but its grown-flower blocks are in the world and crafted in `flowers.js` |

Also checked, tier 3 by size: Smaller Nether Portals (516 lines, 3 events, Collective user), NetherPortalFix (329
lines, 3 mixins), Per-Dimension Game Rules (245 lines, 3 mixins into `GameRules`), FastFurnace (90 lines, 1 mixin,
Placebo) and FastWorkbench (725 lines, client mixins too, Placebo). The two Placebo ones save nothing by being ported
while Placebo stays for others.

## 4. Tier 4 and not candidates

**Tier 4 (content, only by keeping the ids or with a migration):** JRFTL, Create Recycle Everything, Create:
Netherless, Effishiency, Bushier Flowers, XP Tome, Golden Foods, Simple Netherite Horse Armor, Ender Crop, Momentum
Protection (enchantment), Iron Fishing Rods, Fortune on Netherite, Dreamcatcher, Create Compact Exp, Splash Milk, Potion
Bundles. A content port means a startup-script or patcherMod registration **under the old namespace** (Forge allows it;
the mod must be gone) so world data keeps working; it trades a jar for code we then maintain, worth it only where the
pack already reworks the item.

**Survivality** (38 mixins, ~30 independent tweaks in `survivality-common.toml`: safe cactus, armor swapping from
the hotbar, editable signs, unstable dripstone, minecart speed 3.5, variant spawners, random world start, rocket
boosting vehicles, polar bear cavalry ...): each tweak is small, but there are 30 and several are worldgen
(`random_world_start_*` only matter at world creation). Port one at a time only if Survivality itself breaks something.

**Not candidates:** Witch Mobility, Corpse, HT's TreeChop, Ore Excavation, Easy Magic, Regrowth (2.6k lines),
Trade Cycling and Dynamic Villager Trades (GUI + packets), Tactical Fishing, Copycats+, Nock Enough Arrows, The Dirty
Stuff, Salt, Chalk and every block/furniture mod; performance mods (Clumps, FerriteCore, MemoryLeakFix); client-only
mods (they cost the server nothing).

## 5. Suggested order

1. Free: remove `rsec` (2.2). Confirm the Death Knell raw-key bug (FP-01); if real, move it to `both` now.
2. Tier 1 KubeJS batch: Starter Kit, Book Fishing, the recipe and loot mods (one `/kubejs export` session).
3. Tier 2 patcherMod batch, server-only first: Ice Prevents Crop Growth, Glowing Illagers, Comfortable Campfires,
   Friendly Griefing (after FP-04), Breedable Killer Rabbit, Cherished Items, Loading Protection, Don't Void Arrows,
   ~~Fast Leaf Decay~~ (ported 2026-10-19), Bottled Air, Fish On The Line; then the both-sided ones (Death Knell, SwingThroughGrass, Ice Boat
   Nerf, Fuel Goes Here).
4. Tier 3: Responsive Shields and Forgiving Void (they delete our own mixins), Easy Elytra Takeoff, Second Chance.
5. With 1-4 done, 7 of 9 Collective users are gone; Cycle Paintings and Smaller Nether Portals decide whether Collective goes.

Each removed mod also needs: its row out of `mod_sides.md`, its config files out of `config/` and `defaultconfigs/`,
the removal list of the next deploy ([deploy.md](deploy.md)), and a check that no script references its ids.

## 6. Open decisions

- D1: Book Fishing into the catch (LootJS) or as a separate drop (as now)?
- D2: Ice Boat Nerf: ice only, or every ground as the mod does now?
- D3: Loading Protection's name icon: keep (packet + font) or drop?
- D4: Content mods (tier 4): port any under their old namespace, or leave them?
- D5: Port Survivality tweaks or leave the mod?
- D6: Warp stone recipe (1.1): Waystones' own (amethyst corners) stands now that the dead Create Waystone Recipes
  override is gone. Gate it with a pack ingredient in the corners, or leave it?
- D7 (scratchpad 2026-10-21, [TODO](todo/TODO.md) `ports/featureporting #7`): backports beyond this list.
  A trials backport (1.21 trial chambers)? Any other Alex's mods to port (Alex's Mobs, Alex's Delight and Alex's
  Abnormalities are installed)? Unlike the tiers above these are content ports, so they go the way of the Brewin' and
  Vinery ports (own jar, gap analysis first).

## 7. Tests

- [ ] **FP-01 Death Knell lang [game]:** on the live server, die to a zombie and a fall a few times: the chat shows
  Death Knell's flavour messages ("was bonked by", "fell victim to gravity"), no raw `message.deathknell.*` keys.
  The jar is on both sides since 1.0.7, so raw keys would mean a client missed the update.
- [ ] **FP-02 Book Fishing port [scratch]:** 500 catches with and without Luck III: book rates near the ladder in 1.
- [ ] **FP-03 Starter Kit port [scratch]:** new player gets one of the two kits with boots and shield equipped and
  guide books kept; a player with tag `collective.firstJoin.starterkit` gets nothing.
- [ ] **FP-04 mobGriefing [game]:** `/gamerule mobGriefing` on the live server; if true, Friendly Griefing is dead weight.
- [ ] **FP-05 Ice boats [scratch]:** after the port a boat on ice and on grass matches the mod's speeds (or the D2 choice).
- [x] **FP-06 rsec removed [scratch]:** campfire in the rain goes out via the pack's rain rule (patcherMod `CampfireRain`, 8 s; Unlit Campfire's own rain check is off since 2026-10-19); no rsec log line. *PASSED scratch 2026-10-20: `/weather rain`, lit campfire on stone at 0 151 0 -> `lit=false` within the first check (47 s later; the 8 s itself wasn't timed)*
- [x] **FP-08 Tier 1 recipes load [scratch]:** *PASSED scratch 2026-10-20: the 377 line, no `[NCH]` error, no `Parsing error loading recipe`.* boot log had `[feature-ports] tier 1: 377 recipes (1 name tag, 33 splashing, 26 recycle smelting, 6 generators, 311 AE2 cable)` (371 and no generators since 2026-10-21, FP-15) and no `[NCH]` error from feature_ports.js; `/kubejs export` lists `creativeengineering:shaped/name_tag`, `createsplashing/copper_block_exposed`, `smelting/recycle_anvil`, `createmixing/generator_cobblestone`, `shaped/ae2_black_smart_cable_ring`
- [ ] **FP-09 Tier 1 in game [game]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-12: 311 cable, 26 recycle, 33 splashing, name tag, copper ore drops).* craft a name tag (leather, paper, 2 string); a fan over water weathers a copper block to exposed; smelt a hopper -> iron ingot, a rail -> nugget, an anvil -> iron block; 8 smart cables around black dye -> 8 black smart cables, 8 black around a water bucket -> 8 fluix; mine copper ore -> 1 raw copper (silk touch: the ore)
- [ ] **FP-10 Starter Kit port [scratch]:** a new player on the scratch server gets Default or Engineer (boots and shield equipped, items in slots 1-3), log `[starter-kit] <kit> kit given to <name>`, `world/data/starterkit/tracking.json` lists the uuid and the player has tag `collective.firstJoin.starterkit`; relog -> nothing; a uuid pre-written into tracking.json -> nothing on first join
- [ ] **FP-11 Glowing raiders [scratch]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-10 Glowing on a wave pillager, no particles; SQ-OM-14 off: no Glowing).* `/raid start` (or a raid) -> every raider spawned has Glowing (`/effect` shows Glowing II, no icon); `[raids] glowingRaiders = false` + restart -> none
- [ ] **FP-12 Campfire comfort [scratch]:** stand within 3 blocks of a lit campfire -> Comfort (Farmer's Delight) within a second, kept up while standing there, gone a few seconds after leaving; `useComfort = false` -> Regeneration instead; an unlit campfire gives nothing
- [ ] **FP-13 Bottled Air [scratch]:** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-11: air 0 -> 150, lava-bucket bottle gone, under-water drink cancelled).* under water with a glass bottle: right-click -> air bar refills by half and the bottle fills with water; with a lava bucket in the other hand the water bottle is taken back and about half the time the empty bottle returns; a water bottle can't be drunk under water
- [x] **FP-14 Jars gone [offline]:** *PASSED 2026-10-20 (11 jars in `disabledmods/`, 0 rows, 0 config files; 361 server jars).* `mods/` has none of the 11 retired jars, `mod_sides.md` has none of their rows, `server_mods/` and `pack/manifest.json` regenerated, `config/` has no starterkit / bottledair / comfortable_campfires files
- [ ] **FP-15 Generators folded back [scratch]:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-00 371 line, SQ-OM-12 no generator ids, one heated obsidian).* boot log `[feature-ports] tier 1: 371 recipes (1 name tag, 33 splashing, 26 recycle smelting, 311 AE2 cable)`, no `[NCH]` error; `/kubejs export` has no `creativeengineering:createmixing/generator_*` and no `create_generators:*`, and has `creativeengineering:obsidian_generation_heated`; JEI obsidian -> mixing water + lava 250 mB each, heated; JEI cobblestone mixing shows only the 3 heat tiers (1 / 5 / 10)
- [ ] **FP-07 each port [scratch]:** per ported mod, its behaviour from the tables above with the jar removed, and
  existing world data intact (tamed killer rabbits, mid-fall void players, enchanted items untouched).

## 8. History

- 2026-10-18: Rewritten from the decompiled jars (33 tier 1-3 mods, ~50 more surveyed) and the Modrinth dependency
  data. Tiers corrected (section 0), rsec found redundant, Death Knell raw-key bug suspected, Collective payoff
  counted. Nothing ported.
- Before: candidate list by mod name and config (tiers 1-4).
- 2026-10-19: rsec removed (2.2), [plan_1.0.9.md](plan_1.0.9.md). FP-06 still to run.
- 2026-10-20: tier 1 ported (1.1: 8 jars retired, Book Fishing kept, Create Waystone Recipes found dead, D6), Glowing
  Illagers + Comfortable Campfires + Bottled Air ported (2.4, patcherMod 1.1.01); FP-08 to FP-14 added.
- 2026-10-20 (later): the Death Knell raw-key bug was a stale read: the jar is `both` since 1.0.7 and every
  client gets it (section 0). FP-01 reworded as a confirmation.
- 2026-10-19: Fast Leaf Decay ported as part of the leaf decay rework ([mob_block_tweaks.md](mob_block_tweaks.md) 2.5;
  scratch-tested there, MT-19 to MT-23 instead of FP-07); FastLeafDecay-30.jar to `disabledmods/`, `mod_sides.md` row
  deleted, `pack/` and `server_mods/` regenerated.
- 2026-10-20: the campfire rain rule and Unlit Campfire's burn time show in WTHIT ("Rain: Puts it out / Going out in
  6s / Safe (fuel block below)", "Burns forever"): [wthit_lines.md](wthit_lines.md) 2.6, tests WL-25, WL-26.
- 2026-10-21: the Create: Generators port removed (it brought back 5 recipes `resource_blocks.js` had replaced); its one
  live recipe, heated obsidian, moved to `resource_blocks.js`; the jar deleted from `disabledmods/`, its `pack/` rows
  removed. FP-09 loses the cobblestone step, FP-15 added.
- 2026-10-22 (second bulk run): SQ-OM-00, SQ-OM-10 to SQ-OM-12 and SQ-OM-14 PASS on the scratch server; server halves of
  FP-09, FP-11, FP-15 and the logic of FP-13 recorded. FP-12 (Comfort, a real player in range) and the in-game halves stay.
