# mobs / bees: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# mobs`, Sub `bees`. No feature writeup yet (both rows come from the
scratchpad of 2026-10-19). Related: [bee_nests.md](../bee_nests.md) and [worldgen_bee-nests.md](worldgen_bee-nests.md)
(where nests generate), [flowers.md](../flowers.md) (which flowers grow where), [wthit_lines.md](../wthit_lines.md)
(the hive "Bees: 2 / 3" line, `ui/wthit #4`), [mooblooms.md](../mooblooms.md) (the buttercup is stripped from every
tag), `mobs/tweaks #6` (named mobs: shares the name list with #2).

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #1 | Bee breeding flower audit | Planned | small: one dump, then a few `global.tags` lines in a server tag script |
| #2 | Anchored bee hive (respawn at the hive, names in crafted hives) | Planned | medium to large: patcherMod (events + SavedData + 1 mixin), a config section, a names file |

Build order: **#1 first** (a `/reload` job, no restart). **#2** with the next patcherMod release; agree the name list
with `mobs/tweaks #6` (named mobs) first so both read one file.

---

## bees #1: Bee breeding flower audit
Status Planned / Work data / KubeJS / TODO `mobs/bees #1` (scratchpad 2026-10-19)

### Goal
Know which items breed bees, and make every flower the pack has count, consistently with what bees pollinate.

### Now

> **Correction 2026-11-10 ([breeding_audit.md](../breeding_audit.md) 4):** Buzzier Bees' `flower_blacklist` is read by its bone meal handler (`renewableFlowers`), not by pollination.
- **Breeding** = vanilla `Bee#isFood`: the item is in `#minecraft:flowers` (from memory of the 1.19.2 code; Buzzier Bees'
  only `Bee` mixin, `BeeEntityMixin`, changes hive-entry weather logic, not food: decompiled
  `buzzier_bees-1.19.2-5.0.1.jar`). **Pollination** uses the block tag `#minecraft:flowers` (vanilla), minus Buzzier
  Bees' `buzzier_bees:flower_blacklist` block tag (`minecraft:wither_rose` only; verified in the jar; how BB applies it
  is unverified).
- Vanilla 1.19.2 `#minecraft:flowers` (verified in the server jar): `#small_flowers` (13: dandelion ... lily of the
  valley, **wither rose**), `#tall_flowers` (sunflower, lilac, peony, rose bush), flowering azalea, flowering azalea
  leaves, mangrove propagule. Block and item tags have the same members.
- Mods adding to the flower tags (verified in their jars):
  | Mod | Item tag | Block tag |
  |---|---|---|
  | Buzzier Bees | `small_flowers`: buttercup, white clover, pink clover (buttercup stripped from every tag by More Mooblooms' `TagLoaderStripMixin`) | same |
  | Botania | `small_flowers`: `#botania:mystical_flowers` (16 colours) **and `#botania:special_flowers`** (every functional flower: Endoflame, Pure Daisy ...); `tall_flowers`: `#botania:double_mystical_flowers` | `small_flowers`: mystical only; `tall_flowers`: double mystical |
  | Upgrade Aquatic | `small_flowers`: white / pink searocket; `tall_flowers`: flowering rush | same |
  | Twigs | `small_flowers`: `twigs:azalea_flowers` | same |
  | Flower Patch | none (patches have no items) | `flowers`: 9 `flowerpatch:*_patch` blocks, **including `wither_rose_patch`** |
- **Not in any flower tag** (candidates; verified where noted): Botania shiny (glimmering) flowers
  `botania:<colour>_shiny_flower` and floating flowers `botania:<colour>_floating_flower` (Botania's tag files only name
  mystical, double mystical and special); `fireflybushport:firefly_bush` (a `FlowerBlock` subclass, no tags in its jar);
  anything else needs the dump in step 1.
- Pack tag scripts: `kubejs/server_scripts/tags/` (blocktags.js, misctags.js ...); none touch `minecraft:flowers` today
  (it's only read, e.g. `item_physics_tags.js` 137, `blocktags.js` 446). item_dict.js has `i.t_flowers =
  '#minecraft:flowers'` (1921) and every Botania flower key (`i.white_shiny_flower` ...).

### Build
1. **Dump.** `/dumptags item,block` (`_lib/dump_tags.js`) -> `kubejs/exported/tags/item_tags.json` /
   `block_tags.json`; read `minecraft:flowers`, `minecraft:small_flowers`, `minecraft:tall_flowers`. For the "flowers
   that aren't tagged" side, add a small `/dumpflowers` to `_lib/dump_items.js`'s file (or a new
   `_lib/dump_bee_flowers.js`, registered in `dump_all.js` 14-36): every block whose class `instanceof
   net.minecraft.world.level.block.FlowerBlock` or `TallFlowerBlock` (via `Java.loadClass`, with `block.asItem()`), plus
   whether its item is in `#minecraft:flowers` and the block in `#minecraft:flowers`; write
   `kubejs/exported/bee_flowers.json`. Commit a short table of the result into the new writeup.
2. **Decide the rule** (Decisions), then a new `kubejs/server_scripts/tags/bee_tags.js`:
   ```js
   // Bee food and pollination (TODO mobs/bees #1). #minecraft:flowers decides both; see feature_writeup/<writeup> .
   var t = global.tags
   var i = global.item
   ServerEvents.tags('item', e => {
   	t.add(e, 'minecraft:small_flowers', [i.white_shiny_flower /* ... 16, from the dump */])
   	// t.remove(e, 'minecraft:small_flowers', '#botania:special_flowers')   // if functional flowers shouldn't breed bees
   })
   ServerEvents.tags('block', e => {
   	t.add(e, 'minecraft:small_flowers', [/* same blocks */])
   	t.add(e, 'buzzier_bees:flower_blacklist', ['flowerpatch:wither_rose_patch'])
   })
   ```
   Use `t.addMatching(e, tag, '/^botania:[a-z_]+_shiny_flower$/')` (tag_helper.js supports patterns) rather than 16
   literals if the keys aren't wanted; per CLAUDE.md, ids come from `global.item` where a key exists, and new ids
   (`flowerpatch:*`, `fireflybushport:firefly_bush`) get keys in item_dict.js / block_dict.js first.
3. `/reload`, `/dumptags item`, check.
4. Writeup: a short `feature_writeup/bees.md` (or a section in bee_nests.md) with the table, the rule and tests; add it
   to feature_writeup/README.md.

### Ids and data
- Tags: `minecraft:flowers`, `minecraft:small_flowers`, `minecraft:tall_flowers` (item and block),
  `buzzier_bees:flower_blacklist` (block), `botania:mystical_flowers`, `botania:special_flowers`,
  `botania:double_mystical_flowers` (all verified in the jars named above).
- Blocks: `flowerpatch:wither_rose_patch` (verified, Flower Patch 2.0.3 `data/minecraft/tags/blocks/flowers.json`).

### Decisions
- **Should Botania's functional flowers breed bees?** They do now (item `small_flowers` includes
  `#botania:special_flowers`), so bees eat Endoflames. Recommendation: leave it (harmless, and removing items from a
  vanilla tag can break other mods' recipes that read `small_flowers`, e.g. suspicious stew).
- **Shiny / floating Botania flowers.** Recommendation: add shiny flowers to `small_flowers` (they're flowers in the
  world, bees should visit them); leave floating flowers out (decor items).
- **Wither rose.** Vanilla lets it breed bees and bees take wither damage near it. Recommendation: keep it as food,
  blacklist the Flower Patch wither rose patch for pollination like BB does for the rose.
- **Firefly bush.** Recommendation: leave it out (not a flower in 1.21 either).

### Gotchas
- Adding to `minecraft:small_flowers` also feeds every recipe and mod that reads it: suspicious stew, flower pots /
  boxes (`supplementaries:flower_box_plantable` reads its own tag), Create milling compat, More Mooblooms flower
  feeding (mooblooms.md: its own variant data, check), Quark / Botania loot. Check JEI for new suspicious-stew rows.
- Forge 1.19.2 tag *files* can't remove; KubeJS `event.remove` (via `t.remove`) can, at tag build time. More Mooblooms
  strips the buttercup after that, so it stays gone.
- Block and item tags are separate; add both or bees will breed with an item they never pollinate.

### Tests (new prefix `BE-`)
- **BE-01 tags:** `/dumptags item` -> every flower from the audit table is in `#minecraft:flowers`.
- **BE-02 breed:** two bees, right-click each with a Botania shiny flower -> love hearts, a baby bee.
- **BE-03 pollinate:** bees near a patch of shiny flowers in daylight -> they land on them and return with nectar
  (pollen particles); none land on a Flower Patch wither rose patch.

---

## bees #2: Anchored bee hive
Status Planned / Work patcherMod / new jar / TODO `mobs/bees #2` (scratchpad 2026-10-19: "A Domestic Innovation-like
hive")

### Goal
Bees that live in a player-made hive are the player's: they get a name when they first move in, and if one dies it
comes back to its hive later instead of being lost, the way Domestication Innovation pets respawn at their pet bed.

### Now
- **Hives in the pack:** every `BeehiveBlock`: `minecraft:beehive` (crafted), `minecraft:bee_nest` (natural),
  `upgrade_aquatic:driftwood_beehive`, `upgrade_aquatic:river_beehive` (crafted; verified, recipes in the UA jar)
  (wthit_lines.md 115). item_dict.js has `i.beehive` (2032) and `i.bee_nest` (2029), not the UA hives.
- **Domestication Innovation's model** (decompiled `domesticationinnovation-1.6.1-1.19.2.jar`): `CommonProxy.onLivingDie`
  (`LivingDeathEvent`): a tamed pet with a pet bed saves its full NBT (`saveWithoutId`), type id, dimension, bed pos,
  game time and name into a `RespawnRequest` stored in level SavedData (`DIWorldData.addRespawnRequest`);
  `onLivingDrops` cancels its drops; `PetBedBlockEntity`'s tick looks up requests for its pos and re-adds the entity
  (`addAndRemoveEntity`). DI is in the pack, but only for tamed pets; bees aren't tameable there.
- Vanilla hive behaviour (from memory): a bee remembers `HivePos` (NBT) and goes back to it; `BeehiveBlockEntity`
  holds up to 3 occupants as saved NBT (`addOccupantWithPresetTicks(Entity, boolean hasNectar, int ticksInHive)`,
  `releaseOccupant`); a bee whose hive is gone or full forgets it and looks for another.
- WTHIT shows "Bees: n / 3" on hives (patcherMod `compat/WthitHive`, `[wthit] hiveBees`).
- Nothing in patcherMod touches bees yet (`BeehiveDecoratorBeeNestsMixin` is worldgen only).

### Build
New patcherMod package `com.SaintAlfonzo.creativeengineering.bees`:
1. **Config** (CEConfig, new section `[bees]`): `anchoredHives` (bool, default true), `respawnDelayTicks` (default 24000,
   one day), `anchoredHiveBlocks` (list, default `minecraft:beehive`, `upgrade_aquatic:driftwood_beehive`,
   `upgrade_aquatic:river_beehive`), `nameBees` (bool). Or a block tag `creativeengineering:anchored_hives` filled in a
   KubeJS tag script via `global.tags` (recommended: tags are the pack's habit and editable without Java).
2. **Binding + name** (mixin `BeehiveBlockEntityAnchorMixin` on `BeehiveBlockEntity#addOccupantWithPresetTicks`,
   HEAD): if the entity is a `Bee`, the hive block is in the tag, and the bee has no `ce_hive` tag yet: put
   `ce_hive = {pos, dim}` into `getPersistentData()` and, when it has no custom name, `setCustomName(BeeNames.random())`.
   The persistent data travels with the occupant NBT, so it survives the stay in the hive.
3. **Death** (`LivingDeathEvent`, server, `Bee` with `ce_hive`): unless the source is `/kill` / out of world
   (`DamageSource.OUT_OF_WORLD`) or the bee is a baby, save `bee.saveWithoutId(new CompoundTag())` with health reset
   (`Health` = max), fire / effects / anger cleared (`AngerTime` 0, `HasStung` false, `ActiveEffects` removed) into
   `AnchoredBeeData` (`SavedData` on the hive's level, list of `{pos, nbt, dueTick}`), `dueTick = gameTime +
   respawnDelayTicks`. Cancel `LivingDropsEvent` for it (a bee drops nothing anyway; XP is fine).
4. **Respawn** (`TickEvent.LevelTickEvent`, every 100 ticks, server): for each due request whose hive chunk is loaded:
   if the block is still an anchored hive and has a free slot, create the bee (`EntityType.BEE.create(level)`,
   `load(nbt)`, new UUID to be safe), and `hive.addOccupantWithPresetTicks(bee, false, 0)`; it leaves the hive normally
   later. If the hive is gone: drop the request (the bee is lost, as with a broken pet bed). If full: wait.
5. **Names:** `BeeNames.random()` reads a list (see Decisions) from `data/creativeengineering/mob_names/bee.json` (a
   reload listener) or a list in CEConfig. Shared with `mobs/tweaks #6`.
6. **Feedback:** chat or action-bar line to players within 16 blocks when a bee returns ("Buzzby is back in the
   hive"), optional; a WTHIT line on anchored hives "Returning: 1" (extend `compat/WthitHive`'s server data with the
   pending count) is a cheap win.
7. Writeup `feature_writeup/bees.md` (shared with #1), tests, mod_sides note (server-side logic; the name shows on
   clients without the jar).

### Ids and data
- Blocks: `minecraft:beehive`, `minecraft:bee_nest`, `upgrade_aquatic:driftwood_beehive`, `upgrade_aquatic:river_beehive`
  (verified). Entity `minecraft:bee`.
- New: block tag `creativeengineering:anchored_hives`; persistent-data key `ce_hive`; SavedData id
  `creativeengineering_anchored_bees`; names file `data/creativeengineering/mob_names/bee.json`.
- Vanilla members (`addOccupantWithPresetTicks`, `releaseOccupant`, `HivePos`): from memory; check the SRG names in the
  ForgeGradle-decompiled sources before writing the mixin.

### Decisions
- **New block or the existing crafted hives** (TODO Decisions "design"). Options: (a) every crafted hive anchors
  (tag above); (b) a new "Anchored Beehive" block (crafted from a beehive + e.g. a lodestone or honeycomb block; needs
  a block, block entity type, textures, NCH recipe). Recommendation: (a): no new assets, and "crafted hive" is already
  the TODO's line for naming. Natural nests stay wild.
- **Respawn delay.** Recommendation: one in-game day, respawn only while the chunk is loaded (no chunk loading).
- **Which deaths count.** Recommendation: all but `/kill` and the void, so a stinging bee (which dies after stinging)
  comes back too: that is the point of an anchored hive; players who want to get rid of one can break the hive.
- **Name list.** Recommendation: one shared file for bees and `mobs/tweaks #6`; bee-themed names first (Buzzby,
  Bumble, Honey ...), 100+ entries; no player names.
- **patcherMod or a new jar** (TODO Work). Recommendation: patcherMod (it already has the config, WTHIT plugin and
  event plumbing; a new jar would be a second mod for ~300 lines).

### Gotchas
- A hive broken with Silk Touch keeps its occupants in item NBT; the requests key on the old pos, so they are dropped.
  Acceptable; document it.
- Breeding in an anchored hive's range makes new bees that only bind when they first enter it (the mixin covers
  that). Babies are excluded from respawn so baby farms don't respawn as adults.
- Bees killed while their hive's chunk is unloaded (they wander up to ~22 blocks; rare) still respawn when it loads.
- Persistent data in an occupant: `addOccupantWithPresetTicks` saves the bee with `saveWithoutId`, which includes
  `ForgeData`; verify the key survives a hive round trip in the first test.
- More Mooblooms / Buzzier Bees: BB's `canEnterHive` mixin (weather, Sunny effect) is untouched.

### Tests (prefix `BE-`, continuing #1)
- **BE-04 naming:** craft a beehive, place it near a nest, let a bee move in -> on leaving it has a name; a bee in the
  natural nest has none.
- **BE-05 respawn:** kill a named hive bee with a sword -> next morning (or `/time add 24000`) it is back in that hive
  (WTHIT "Bees" count goes up), same name.
- **BE-06 stinging:** let a named bee sting you and die -> it returns after the delay.
- **BE-07 hive gone:** kill a named bee, break its hive -> nothing respawns, no errors in latest.log.
- **BE-08 switch off:** `[bees] anchoredHives = false` -> no names given, no respawn.

---

## bees #3: Honeycomb shearing yield
Status **Planned** / Work data / KubeJS (a patcherMod mixin is the likely route) / TODO `mobs/bees #3`

### Goal
Shearing a beehive or bee nest with a full honey level gives 2.5 honeycombs on average; Looting adds more.

### Now
- Vanilla 1.19.2 gives a fixed 3 honeycombs: `BeehiveBlock.use` (shears) and the dispenser shears behaviour both call
  `BeehiveBlock.dropHoneycomb` -> `popResource(level, pos, new ItemStack(Items.HONEYCOMB, 3))`. Unverified in this jar
  (read it with `javap`); it is code, not a loot table, so a KubeJS loot edit cannot change it.
- Nothing in the pack touches the amount: patcherMod's bee code is `worldgen/BeeNests`, `BeehiveDecoratorBeeNestsMixin`,
  `TreeFeatureBeeNestsMixin`, `compat/WthitHive` (see [bee_nests.md](../bee_nests.md)); `i.honeycomb` is
  `minecraft:honeycomb` (item_dict line 557).
- Honeycomb is also a recipe ingredient and sugar source (`dishes/ingredients #22`: 2 sugar per honeycomb), so the yield
  feeds that economy.

### Build
1. patcherMod mixin on `BeehiveBlock.dropHoneycomb` (`@ModifyArg` on the count, or `@Redirect` the `ItemStack`
   constructor): count = 2 + 50% chance of +1 (mean 2.5), plus Looting.
2. Looting: shears cannot carry Looting from the enchanting table or an anvil (Looting is a weapon enchantment), so the
   level has to be read from the held item's NBT (`EnchantmentHelper.getItemEnchantmentLevel(LOOTING, shears)`); it only
   exists on shears if another mod or command puts it there. Add `[bees] honeycombBase`, `honeycombBonusChance`,
   `honeycombLootingBonus` to `CEConfig` (and `configs_edited/` as usual).
3. Dispenser shears: the same static is used, so it needs no extra code; the held-tool level is 0 there.

### Decisions
- Exact numbers ("about 2.5"). Recommend base 2, +1 at 50%, Looting adds +1 chance per level (mean 2.5, 3 and more).
- What "Looting" means on shears. Recommend a config switch `honeycombLootingFromShears`, default on, and tell the user
  it only matters when something puts Looting on shears (else make Fortune count: unverified whether the pack allows it).

### Gotchas
Does nothing for the nest items bees leave in structures (hive loot is separate). WTHIT/JEI text for honeycomb sources
(`jei_world` tab Knife and Machete Drops does not list hive shearing).

### Tests (BN-)
- BN-11 (proposed): shear 40 full hives: mean yield 2.4 to 2.6; with a Looting III shear the mean is higher.

---

## Built, needs play

None (no Testing / Complete rows in this subfamily).

## Writeup corrections made

None.
