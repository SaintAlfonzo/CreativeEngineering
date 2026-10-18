# Feature porting: small mods the pack could absorb into its own code

Test prefix `FP-`. Status: **research only (2026-10-18), nothing ported.** The first version of this file (a
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
- **Probable live bug found:** Death Knell is a server-only jar in `mod_sides.md`, but its messages are translation
  keys whose lang lives in that jar. Clients without it should see raw keys (`message.deathknell.bonked`) for most
  mob kills, falls and drownings. Unverified in game (FP-01). Porting it into patcherMod (both sides) fixes it; so does
  moving it to `both`.

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
| Fast Leaf Decay | `NeighborNotifyEvent` on air: leaves next to it get a decay tick 4-11 ticks later through its own list | event + `level.scheduleTick` | ~30 | server | `scheduleTick` replaces the mod's unbounded list |
| SwingThroughGrass | `LeftClickBlock` on a block with no collision shape: ray-trace 4.5 blocks for a living entity and attack it | event | ~50 | both (the event fires client and server; the attack runs server side) | |
| Ice Boat Nerf | mixin `Boat#getGroundFriction` RETURN → **0.45 always** | 1 mixin | ~10 | both (boat physics run on the driver's client) | it sets every ground, not just ice: boats on land get slower than vanilla's 0.6 too. Port with "ice only" or as is (FP-05) |
| Bottled Air *(was tier 4)* | right-click a water bottle (or empty bottle with fire items) underwater to refill air; no items | `RightClickItem` | ~80 | server | Collective user |
| Fuel Goes Here *(new)* | mixin `AbstractFurnaceMenu#quickMoveStack`: shift-click fuel into the fuel slot | 1 mixin | ~40 | both (menu) | |
| Waystones Teleport Pets *(new)* | Waystones teleport event: brings the player's tamed pets along | event on the Waystones API | ~30 | server | needs Waystones on the compile classpath |
| Fish On The Line *(new)* | a bell in the off hand rings when a fish bites | `PlayerTickEvent` | ~60 | server | Collective user |
| Elytra Bombing *(new)* | gliding + igniter: drops primed TNT from the inventory | `RightClickItem` | ~60 | server | |

Tax Free Levels moved to tier 3: it is 20 mixins, not one formula (2.3).

### 2.2 Rain Should Extinguish Campfires: remove, no port

Mixin `CampfireBlockEntity#cookTick` TAIL with one **static** countdown shared by every campfire in the game (2-6 s),
then: no non-air block in 64 above and 2 of 4 neighbours clear for 8 blocks → doused. Unlit Campfire already does the
same job per campfire with `isRainingAt(pos.above())` and `campfireRainUnlitTime = 160` (our default config). Two
systems race; remove `rsec` from `mods/` and `mod_sides.md` (FP-06).

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
   Fast Leaf Decay, Bottled Air, Fish On The Line; then the both-sided ones (Death Knell, SwingThroughGrass, Ice Boat
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

## 7. Tests

- [ ] **FP-01 Death Knell lang [game]:** on the live server, die to a zombie and a fall a few times with a client
  that lacks the jar: raw `message.deathknell.*` keys = the bug is real.
- [ ] **FP-02 Book Fishing port [scratch]:** 500 catches with and without Luck III: book rates near the ladder in 1.
- [ ] **FP-03 Starter Kit port [scratch]:** new player gets one of the two kits with boots and shield equipped and
  guide books kept; a player with tag `collective.firstJoin.starterkit` gets nothing.
- [ ] **FP-04 mobGriefing [game]:** `/gamerule mobGriefing` on the live server; if true, Friendly Griefing is dead weight.
- [ ] **FP-05 Ice boats [scratch]:** after the port a boat on ice and on grass matches the mod's speeds (or the D2 choice).
- [ ] **FP-06 rsec removed [scratch]:** campfire in the rain goes out in about 8 s via Unlit Campfire; no rsec log line.
- [ ] **FP-07 each port [scratch]:** per ported mod, its behaviour from the tables above with the jar removed, and
  existing world data intact (tamed killer rabbits, mid-fall void players, enchanted items untouched).

## 8. History

- 2026-10-18: Rewritten from the decompiled jars (33 tier 1-3 mods, ~50 more surveyed) and the Modrinth dependency
  data. Tiers corrected (section 0), rsec found redundant, Death Knell raw-key bug suspected, Collective payoff
  counted. Nothing ported.
- Before: candidate list by mod name and config (tiers 1-4).
