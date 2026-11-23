# Corpse decay and ancient corpses (2026-11-18)

<!-- generated:status_index begin open_work sha1=93559adcdc3c -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 9 open TODO rows (Pinned 1, Testing 8), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `corpses #3` | Pinned | P | Work out ancient corpse loot tables (cave and ocean dead-player kits, tier check against loot_tiers.md) |
| `corpses #1` | Testing | S C | Corpse decay on world time: skeleton after 7 days, empty corpse skeleton after 30 s and despawn after 5 min, shown armor follows contents (Creative Compat) |
| `corpses #2` | Testing | S C | Ancient corpses: skeletons on 3x3 flat cave floors (below y48, about 80% below y16) and ocean floors (cold and deep more common), about 1 per 32 chunks, loot rolled on first open (Creative Loot, only with Corpse loaded) |
| `corpses #4` | Testing | CQ-CMP-15 SQ-CMP-17 CQ-CMP-18 CQ-CMP-19 CQ-CMP-20 CQ-CMP-21 CQ-CMP-22 CQ-CMP-26 | Cosmetic armor and curios go into the corpse; corpse screen in sections by inventory location; cosmetic armor and visible curios shown on the corpse model |
| `corpses #5` | Testing | S C | Ancient corpse armor rolls: equip chains, 56/35/15/5 chances, material weights, oxidized or waxed copper, dyed leather colors (all configurable) |
| `corpses #6` | Testing | S C | Nether corpses wither (skeleton then wither skeleton corpse), End corpses keep the player model (Creative Compat) |
| `corpses #7` | Testing | S C | Visceral Heap Ancient Remains: empty breakable skeletons dropping bones, bone chunks, cracked bones and a skeleton skull 25% of the time |
| `corpses #8` | Testing | S C | Ancient corpse held items: wood, stone, copper or iron sword or pickaxe in the main hand (45%) and offhand (20%, half torches or bread), worn, rarely enchanted, copper oxidized or waxed; a corpse that rolls nothing gets a main hand tool (Creative Loot) |
| `corpses #9` | Testing | S | Skeleton corpses sink in water instead of floating; ocean corpses keep gravity (Creative Compat, Creative Loot) |
<!-- generated:status_index end open_work -->

Status: **planned, rulings in** (section 4); loot contents still to work out (section 5). Status lives in the TODO rows (section 6).

Source: owner notes in `todo/scratchpad.md` (2026-11-18) and chat 2026-11-18:

> - corpses decay to skeleton model after configurable time, decays faster if corpse is empty
>     - keeps armor on skeleton?
> - randomly spawning corpses underground in world, skeletons

Chat additions (owner, 2026-11-18): the corpse decay goes in Creative Compat; the random corpses go in Creative Loot
and only when the Corpse mod is loaded; they spawn in caves or on the ocean floor with loot suited to a "dead player";
they are named "Ancient Corpse", not "Corpse of <player>". Add a note to work out the loot (section 5).

## 1. What the Corpse mod does today (premise check)

Jar: `corpse-forge-1.19.2-1.0.17.jar` (henkelmax, both sides; mod_sides.md). Read from the bytecode
(`de/maxhenkel/corpse/entities/CorpseEntity`) and `serverconfig/corpse-server.toml`:

- **Skeleton stage exists already.** `CorpseEntity.tick()` increments a private `age` each server tick and sets the
  synced `SKELETON` flag when `age >= skeleton_time` (config `[corpse] skeleton_time`, default and pack value
  `72000` ticks). The renderer then draws `DummySkeleton` instead of the player model.
- **The clock counts loaded ticks only.** `age` advances only while the chunk ticks, so a corpse in an unloaded area
  never decays. At 72000 ticks it is 1 hour of loaded time, not world time.
- **Empty corpses vanish.** When the corpse is empty, `emptyAge` is stamped and the entity is discarded
  `[corpse.despawn] time = 600` ticks (30 s) later. With today's values an emptied corpse never reaches the
  skeleton stage, so "decays faster if empty" needs the empty despawn changed too (item 3).
- `force_time = -1`: full corpses never despawn.
- **The skeleton already wears armor.** `show_equipment = true`; `DummySkeleton` gets the corpse's equipment list and
  sets each slot. That list is a **snapshot taken at death** (`setEquipment` is only called on creation and NBT load),
  so armor taken out of the corpse still shows on it (item 4).
- Name: `getDisplayName()` returns `entity.corpse.corpse_of` ("Corpse of %s") when the stored name is set, else
  "Corpse". WTHIT shows the same name (wthit_lines.md).
- Access: `only_owner = false`, so anyone can open any corpse, ancient ones included.

## 2. Items (intake)

| # | Item (owner's words) | Class | Notes |
|---|---|---|---|
| 1 | "corpses decay to skeleton model after configurable time" | premise | Done upstream (section 1). Open: which clock and what default time (Q1, Q2). |
| 2 | "decays faster if corpse is empty" | open | Empty corpses despawn after 30 s today. Open: what happens to an empty corpse (Q3). |
| 3 | "keeps armor on skeleton?" | premise | Done upstream. Open: should the shown armor follow what is still in the corpse (Q4). |
| 4 | corpse decay lives in Creative Compat | clear | Mixin on `CorpseEntity`, gated by `CompatMixinPlugin` on mod id `corpse`. |
| 5 | "randomly spawning corpses underground in world, skeletons" | open | Spawn as skeletons. Rate, depth and placement (Q6). |
| 6 | random corpses live in Creative Loot, only if Corpse is loaded | clear | Creative Loot registers nothing today (creative_loot.md section 0); this adds its first registration (a worldgen feature and biome modifier). |
| 7 | caves or the bottom of the ocean | clear | Two placements: cave floor (air above, solid below, no sky), ocean floor in ocean biomes. |
| 8 | "appropriate loot for a 'dead player'" | open | Contents to be worked out (section 5). Open now: how the loot is rolled and shared (Q5), whether they wear armor (Q7). |
| 9 | "Ancient Corpse" instead of "Corpse of <player>" | clear | Marker tag on the entity; Compat's mixin returns "Ancient Corpse" from `getDisplayName()`; lang key in Creative Loot. |
| 10 | what happens to a looted ancient corpse | open | Not in the notes; follows Q3 unless the owner wants a different rule (Q8). |

## 3. Design (pending rulings)

### 3.1 Corpse decay (Creative Compat)

- Mixin `CorpseDecayMixin` into `CorpseEntity.tick()` (server side), plus save/load of the extra fields. Only applied when
  `corpse` is loaded (`CompatMixinPlugin`). The Corpse jar goes into `development/creativeCommon/libs/` (compileOnly,
  git-ignored, SRG-named, as the other targets).
- Skeleton time on world time (stored death game time, compared each tick and on load), 7 days; empty rule 30 s to skeleton, 5 min to despawn. New keys in `creativecompat-common.toml`, section `[corpse]`:
  skeleton time, empty multiplier or empty skeleton time, empty despawn time. Upstream `skeleton_time` and
  `despawn.time` are left as they are; creative_compat.md lists which upstream keys Compat overrides.
- Shown armor follows contents (ruling 4): the mixin re-syncs `EQUIPMENT` from the corpse's armor and hand slots
  when its inventory changes.

### 3.2 Ancient corpses (Creative Loot)

- A worldgen feature `creativeloot:ancient_corpse` with two placed features (`cave`, `ocean_floor`), added by a Forge
  biome modifier wrapped in a `forge:mod_loaded corpse` condition. Feature registration runs only when
  `ModList.isLoaded("corpse")`; the class touching `CorpseEntity` is loaded only then.
- The feature spawns a `CorpseEntity` with the skeleton flag set, no owner UUID, the ancient marker tag and an unrolled loot table id (rolled on first open, ruling 5) and its armor
  (ruling 7) already in the armor slots, so it shows on the model. Only new chunks get them (worldgen); existing explored areas get none.
- Loot tables `creativeloot:corpse/cave` and `creativeloot:corpse/ocean` (data in Creative Loot, so the LootJS and
  `loot_config.json` tuning layer can reach them). Rate about 1 per 32 chunks (ruling 6), spread by height (ruling 12) and ocean type (ruling 13), flat check ruling 11.
- Name per item 9. Death history (`/corpse`) never lists ancient corpses (no Death record).

### 3.3 Note: cosmetic armor and curios in the corpse (owner, chat 2026-11-18)

> ensure all cosmetic armor and curios items transfer to the corpse appropriately, and automatically show the
> contents of each of these inventories

- **Curios:** Corpse Curios Compat 2.2.2 (`CorpseContainerMixin`) already collects curios into the corpse and puts
  them back into curio slots on Transfer Items (`blacklisted_items = []`). To check: every curio slot type in the pack
  (Botania, Charm of Undying, Elytra Slot, the trinket case), cosmetic curio slots, and that nothing lands in
  "Additional Items" only because a slot type is missing.
- **Cosmetic armor:** conflicts with FX-28 (owner, 2026-10-22, fixes_compat.md): `CosArmorKeepThroughDeath = true`
  keeps cosmetic armor on the player, so it never reaches the corpse. Cosmetic Armor x Corpse Compat 1.0.0 puts it back
  in the cosmetic slots when the keep option is off. Ruling 9: into the corpse; set `CosArmorKeepThroughDeath = false`
  (copy to `configs_edited/`), replacing FX-28.
- **Show the contents (ruling 10):** the corpse screen is laid out by where each item was on the player: hotbar, main
  inventory, armor, offhand, curios (by slot type), cosmetic armor, all visible at once with labels; no Additional
  Items button for these. The corpse and skeleton model also wear the cosmetic armor over the real armor and render
  visible curios (back, belt, elytra slot), as the player looked. Shown items follow contents (ruling 4 applies).
  Needs per-item origin slots stored at death (the Corpse mod keeps a main list and an additional list only), and a
  client screen replacement for `CorpseInventoryScreen`; the model part reuses Curios' and Cosmetic Armor's render
  layers on the dummy entities if they accept them. Look-check with a screenshot before polishing (intake section 5).
- Checks with the decay build: an item in each inventory survives skeleton stage, decay and the 5 min empty despawn
  (a corpse holding only curios or cosmetic items is not "empty"); Transfer Items puts each item back in its own slot.

### 3.4 Placement, armor rolls and dimensions (owner, chat 2026-11-18)

Owner notes, split:

| # | Item (owner's words) | Class |
|---|---|---|
| 11 | "only spawn in flat areas with no overhangs" | open (Q12: footprint and headroom) |
| 12 | "only spawn below y48 (very rare at that height), slowly become more common as they approach y0, and more common below y0 approaching y-64 where most corpses are below y32" | open (Q9: y32 or y-32; Q10: curve) |
| 13 | ocean floor corpses vs the y48 cap | open (Q11; most ocean floor is above y48) |
| 14 | "copper armor on corpses should always be fully oxidized (or waxed? can they be waxed?)" | open (Q13). Premise: yes, Everything Is Copper armor stores `state` (unaffected, exposed, weathered, oxidized) and a `waxed` flag in item NBT, with its own armor textures per stage |
| 15 | "subsequent roll mechanic for armor; equip order chains ... (boots-leg-chest-helmet, helmet-chest-boots-leg, chest-boots-legs-helmet), more common first roll but less common on each subsequent roll" | open (Q14: chances). Mechanic clear: pick one chain at random, roll slot 1; on success roll slot 2 at a lower chance, and so on; the first failure stops |
| 16 | "the first 2 material types are random but any after are 1 of those 2" | clear: pieces 1 and 2 roll materials by weight; pieces 3 and 4 pick one of those two (50/50) |
| 17 | "dyed leather armor is 50% the same color and 50% 2 very different colors" | open (Q16: color source; Q17: how the two colors split) |
| 18 | "leather dyed-leather and copper armor common while iron and chain are rarer" | open (Q15: wood, ruled in ruling 7, is not in this list) |
| 19 | "all of these features and values are configurable" | clear: every chance, weight, chain list, y curve point, footprint and timer in `loot_config.json` (Loot) or `creativecompat-common.toml` (Compat) |
| 20 | "similar tool system, and a chance to hold a tool or inventory item in hands" | note only (section 5b), not designed now |
| 21 | Nether: none spawn; player corpses "turn to skeletons on empty in 15 seconds", "wither to a wither skeleton corpse after 30 seconds, and decays after 5 minutes" | open (Q18: full Nether corpses; Q19: what the 5 min counts from) |
| 22 | wither skeleton look "(shortened? palette swapped skeleton texture?)" | open (Q20). Texture rule: vanilla `wither_skeleton.png` is human art and may be copied; a palette swap needs the owner's source and palette |
| 23 | "the flesh heap nether biome should uncommonly spawn empty skeletons that dont wither or despawn, but can be broken" | open (Q21: rate, name, drops). Biome: Biomes O' Plenty `biomesoplenty:visceral_heap`. Premise: corpses cannot be broken by players today (`hurt()` only reacts to lava, off in the pack), so breaking is new |
| 24 | "player corpses in the end never turn to skeletons and remain the player model until they despawn" | open (Q22: when a full End corpse despawns; `force_time = -1` means never) |

Design holes the questions close are listed with each item; rulings go in section 4.

### 3.5 Second pass notes (owner, chat 2026-11-18)

| # | Item (owner's words) | Class | Notes |
|---|---|---|---|
| 25 | "cosmetic and curios should show on the corpse" | clear | Restates ruling 10 (model part of `corpses #4`) |
| 26 | "spawned corpses should roll their armor and later held items first so they arent empty for deletion and show in world before open" | premise | Armor is rolled at spawn and shown before open already (creative_loot.md 2c); 44% roll none and are kept by the sealed mark (6b). Open: what a corpse that rolls nothing gets (ruling 24) |
| 27 | "mirror current widget locations for curios and cosmetic armor when working on corpse screen, and always shown" | clear | Corpse screen (`corpses #4`): curio slots where the Curios inventory widget puts them, cosmetic armor slots where Cosmetic Armor Reworked puts them on the player inventory; visible without a toggle |
| 28 | "for held items for now just roll a wood/stone/copper/iron sword/pickaxe with random durability enchants and waxing (on copper) to mirror armor rolls, without dye" | open | Premise: Everything Is Copper has `copper_sword` and `copper_pickaxe` with the same `state` / `waxed` NBT as its armor. Hands and weights: rulings 25, 26 |
| 29 | "leave gravity on for the ocean corpses" | clear | Reverses the build choice in 6b (`NoGravity` on ocean corpses) |
| 30 | "disable floating for all skeleton corpses, they sink instead" | clear | Compat: a skeleton-stage corpse (player, ancient or remains) sinks in water and lava; player-model corpses keep the Corpse mod's floating |
| 31 | "keep current inventory rolls" | clear | The first-open placeholder tables stay (cave torches and bread, ocean bread) |
| 32 | "defer the rest of the loot tables until the loot overhaul" | clear | `corpses #3` pinned on `loot/tables #1` |
| 33 | "mark this as a cousin to the cave and underwater ruin/shipwreck loot tables" | clear | The overhaul designs the corpse kits next to the cave ruin, underwater ruin and shipwreck tables |
| 34 | "fix the broken checks" | clear | check_all failures of 2026-11-18 (comment_dates, iso_dates_docs, status_index), fixed in this session |

### 3.6 Round 1 design for the rest of `corpses #4` (rulings 31 to 34)

Facts (read from the jars 2026-11-18; Corpse 1.0.17, Curios 5.1.6.4, Cosmetic Armor Reworked v1a, the two compat jars):
- `CorpseInventoryContainer` (176x245, `corpse:textures/gui/inventory_corpse.png`): armor 4 `LockedSlot`s at
  x 8 + i x 18, y 18 (helmet leftmost), offhand (98, 18; corrected in the round 1 build notes), main rows y 40 / 58 / 76, hotbar y 98, player inventory
  offset 79; `getInventorySize()` 41. The top row x 80, 116, 134 and 152 is free: 4 slots, contiguous once the offhand moves.
- The client corpse holds an **empty** Death: slot contents arrive only by menu sync; synced entity data is ID, NAME,
  SKELETON, MODEL, EQUIPMENT. So the client cannot build slots from the Death; it must know the slot count up front.
- Curios reach the corpse as unmatched drops (`CuriosEventHandler.playerDrops` adds them before the Corpse mod's
  `LivingDropsEvent` at LOWEST), so they land in `additionalItems` with no origin. Cosmetic armor arrives the same way,
  tagged `CosmeticArmorItem` / `CosmeticSlotIndex` / `CosmeticSkinArmor` by Cosmetic Armor x Corpse Compat.
- Corpse Curios Compat and the cosmetic compat both hook `transferItems` at HEAD (remap = false).
- Curios' own screen puts curio slots at x -18, y 12 + n x 18 (8 visible, then a scroll), panel from
  `curios:textures/gui/inventory.png` (u0 v0, 27 wide; slot frame u138 v0 18x18).
- Live bug: Cosmetic Armor Reworked swaps armor on `RenderPlayerEvent.Pre` by UUID only, so the corpse's DummyPlayer
  already shows the dead player's **current** cosmetic armor.

Design:
1. **Curio origin at death** (ruling 32): a `LivingDropsEvent` listener at HIGHEST snapshots the player's curio
   stacks (slot id, index, stack) before Curios clears them; a second at LOW (after Curios adds the drops, before the
   Corpse mod at LOWEST) tags each matching drop `creativecompat:curio` {Slot, Index}. Curios API compileOnly
   (jar already in `creativeCommon/libs`).
2. **Section slots**: a mixin on `CorpseInventoryContainer.<init>` TAIL adds a fixed block of section slots on both
   sides (4 cosmetic, `curioCap` curio, default 24, so client and server agree without a buffer change), backed by a
   view of `additionalItems` indexed by tag (cosmetic by `CosmeticSlotIndex`, curio by our tag, in slot-id then index
   order). Take-only; a slot is active while it has seen a stack this open (client lays out from the synced tags).
   Taking strips our tag and the three cosmetic tags so the item stacks normally.
3. **Additional Items** filtered: tagged stacks are left out of the Additional container's pages and of the
   auto-open and button checks (mixins on `CorpseAdditionalContainer.setSlots` and the open trigger), so they appear
   only in their sections. Open point: `setSlots` indexes the list directly, so it needs a filtered index map.
4. **Transfer Items**: our HEAD hook runs first: each tagged curio goes back to its exact slot when empty (else it is
   left to Corpse Curios Compat), cosmetic stacks as the cosmetic compat already does.
5. **Screen** (client mixin on `CorpseInventoryScreen` `m_7286_`): draws the 4 cosmetic slot frames on the top row
   (frames blitted from the same corpse texture's slot region, no new texture file) and the Curios panel left of the
   window, one column of up to 12, a second column beside it past 12; tooltips name the curio slot type.
6. **Cosmetic bug**: a `RenderPlayerEvent.Pre` listener at HIGHEST for the Corpse mod's `DummyPlayer` swaps in the
   corpse's own cosmetic stacks (synced as a new entity data list, also needed by round 2) and cancels Cosmetic Armor
   Reworked's swap for it.
7. Checks: CQ rows for the screen (screenshot look-check before polish), each inventory surviving decay and the 5 min
   rule (a corpse with only curios or cosmetic items is not empty), Transfer to the exact slot.

Round 2 (later): curios on the DummyPlayer by filling its client curios handler from the synced list; a layer on the
skeleton calling `ICurioRenderer.render`; cosmetic armor on the skeleton. Elytra Slot uses the `back` curio, covered by
the same fill.

Build notes, round 1 (2026-11-19; [creative_compat.md](creative_compat.md) Corpse screen sections; SQ-CMP-17, CQ-CMP-18
to -20). Choices the design left open (confirmed, owner, 2026-11-22, ruling 38):
- **Offhand moved.** The jar has the offhand slot at (98, 18), not (152, 18): the free top-row spots were 80, 116, 134
  and 152. To keep ruling 31 ("between the real armor and the offhand"), the offhand slot moves to x 152 and the
  cosmetic slots take 80 to 134; the 4 cosmetic frames and the offhand frame are always drawn.
- **Cap is synced, not read on each side.** `curioCap` reaches the client in a second synced corpse value (`Cap`, with
  the 4 cosmetic stacks of item 6), so a client config cannot disagree with the server's slot count. History corpses
  (never ticked) use the default 24 on both sides. `sectionedScreen = false` turns the sections and the Additional Items
  filter off. Curios past the cap stay on the Additional Items pages.
- **Cosmetic slots always active, curio slots once filled.** Empty cosmetic slots show the armor silhouettes; the
  curio panel grows with the curios the menu sync delivers and keeps a slot after its item is taken.
- **Item 6 listener order.** A HIGHEST listener cannot cancel Cosmetic Armor Reworked's HIGH swap (canceling the
  event would hide the corpse), so a NORMAL listener writes the corpse's own cosmetic stacks over it and a Post
  listener puts the real armor back before Cosmetic Armor's own LOW restore. A skin-hidden cosmetic slot (`Skin`)
  shows no armor, as Cosmetic Armor does on the player.
- **Curio return order.** The return to the exact slot runs in the Corpse mod's transfer packet, before
  `transferItems`, so it precedes the two compat jars' HEAD hooks whatever their mixin order. Tags are stripped from the
  player's inventory and curio slots after every Transfer Items, so a stack placed by the compat jars or the Corpse mod
  never keeps them.
- **Labels are tooltips.** A section stack's tooltip says "Curio: <slot type>" (with "(cosmetic)" for Curios' cosmetic
  slots) or "Cosmetic armor"; the screen has no room for text labels above the top row. To revisit at the look-check.
- **Curio cosmetic slots** (Curios' own cosmetic column) are recorded and returned too (`Cosmetic` in the tag).

Build notes, round 2 (2026-11-19, ruling 35; [creative_compat.md](creative_compat.md) Corpse models; CQ-CMP-21, -22,
look-checked together with CQ-CMP-18). Choices the design left open (confirmed, owner, 2026-11-22, ruling 38):
- **Synced curio list.** The section tag gains `Curios`: every curio-tagged stack still in the corpse (slot, index,
  cosmetic, hidden, item), not capped by `curioCap` (the cap is the screen's). It is rebuilt once a second from the
  contents like the cosmetic stacks, so a taken curio stops showing within a second (ruling 4).
- **Player model by Curios' own layers.** The dummy player's client curios handler gets one slot type per recorded slot
  id, sized to the highest recorded index (not the player's real slot count), refilled only when the synced tag
  changes. Curios' layer and Elytra Slot's layer then draw as on the player, so the dummy needs no layer of ours.
- **Render toggles kept.** A curio whose render the player had toggled off (Curios' eye button) is recorded `Hidden`
  at death and stays hidden on both models.
- **Skeleton by a layer of ours.** `CorpseSkeletonLayer` on the vanilla skeleton renderer and the wither look draws each
  shown curio (cosmetic over real, one per slot index) through its `ICurioRenderer`. A renderer written for players only
  that throws is skipped (one warning in the log) instead of breaking the frame.
- **Skeleton elytra** (Elytra Slot): an elytra in any curio slot with no curio renderer is drawn by a vanilla elytra
  layer with the elytra in the chest slot for that draw, over a chestplate as on players. A Customizable Elytra elytra
  goes through that mod's own layer (`CorpseCustomElytra`), so its colors and banner patterns show on the skeleton as
  on the player model (ruling 37, built 2026-11-22); any other elytra keeps the vanilla wings texture.
- **Skeleton cosmetic armor**: same rule as the player model (cosmetic over real, nothing for a skin-hidden slot),
  applied to the skeleton's dressing each frame.

## 4. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | skeleton time clock (Q1) | open | World time since death; also decays while the chunk is unloaded, caught up when it loads | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 2 | default skeleton time (Q2) | open | 7 in-game days (168000 ticks), configurable | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 3 | empty corpse rule (Q3) | open | Skeleton 30 s after it becomes empty, despawn 5 min after it became empty (both configurable). "5 min from emptied" confirmed in Q8 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 4 | armor shown after looting (Q4) | open | Follow contents: the model shows only armor and held items still in the corpse | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 5 | how ancient loot is rolled and shared (Q5) | open | Rolled on first open, shared (first opener takes it) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 6 | spawn rate and placement (Q6) | open | About 1 per 32 chunks, configurable in `loot_config.json`; cave floors and ocean floors | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 7 | armor on ancient corpses (Q7) | open | Worn (damaged) wood (Immersive Armors `wooden_*`), leather (also dyed), chainmail, copper (Everything Is Copper `copper_*`), iron; rarely enchanted; lootable, stops showing when taken out | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 8 | looted ancient corpse (Q8) | open | Same as player corpses: despawns 5 min after it became empty | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 9 | cosmetic armor into the corpse (conflict with FX-28, fixes_compat.md 2026-10-22) | conflict | Into the corpse: keep option off; replaces FX-28 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 10 | "automatically show the contents of each of these inventories" | open | Corpse screen in sections by inventory location; cosmetic armor and visible curios also shown on the corpse model | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 11 | "only spawn in flat areas with no overhangs" (Q12) | open | 3x3 floor of full solid blocks at one height, at least 3 blocks of air (water on the ocean floor) above all 9 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 12 | height spread (Q9, Q10) | open | Cave corpses only below y48 (very rare there); target about 80% below y16 (flexible); tune the curve for the largest possible rate gap between y-64 and y32. Build tunes the curve points and reports the resulting shares | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 13 | ocean floor corpses (Q11) | open | On the ocean floor in clear space (same 3x3 check, water above); no y cap. Rate ratios warm 1 : cold 3, shallow 1 : deep 6 | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 14 | copper oxidation and wax (Q13, Q13b) | open | Unwaxed copper is always fully oxidized. 10% of copper pieces are waxed; a waxed piece takes any state but fully oxidized (unaffected, exposed, weathered), with a flat roll penalty on unaffected. Waxed states follow the dye rule: 50% all the same state, 50% two different states | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 15 | armor roll chances (Q14) | open | 56 / 35 / 15 / 5 % along the chosen chain; breakdown below | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 16 | material weights (Q15) | open | Wood, leather, dyed leather, copper 25 each; chain 8, iron 8 (ruling 7 kept: rarely enchanted, worn) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 17 | dyed leather colors (Q16, Q17) | open | Any RGB. 50% all pieces one color, 50% two colors far apart in RGB distance (not opposite hues) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 18 | full Nether corpse (Q18) | open | Skeleton 1 in-game day after death, wither skeleton 1 day after that (world time), never despawns while it holds items | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 19 | empty Nether corpse (Q19) | open | Skeleton 15 s after emptied, wither skeleton 30 s after that, gone 5 min after emptied | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 20 | wither skeleton corpse look (Q20) | open | Vanilla `wither_skeleton.png` (copied human art; sources.csv row) on the normal skeleton model and size | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 21 | Visceral Heap skeletons (Q21) | open | About 1 per 8 chunks in `biomesoplenty:visceral_heap`, same 3x3 check, any height; plain skeleton, empty, never wither or despawn; named "Ancient Remains" | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 22 | breaking (Q21b) | open | Only Visceral Heap skeletons can be broken; drops bones, bone chunk and cracked bones (Philip's Ruins), and a skeleton skull 25% of the time | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 23 | End corpses (Q22) | open | Never turn skeleton, keep the player model; never despawn while full, gone 5 min after emptied | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 24 | spawned corpse that rolls nothing (Q23, item 26) | open | Guarantee a held item: when the armor and hand rolls all miss, the main hand gets a tool, so no spawned corpse is empty; the sealed mark goes | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 25 | hand rolls (Q24, item 28) | open | Main hand 45%, offhand 20%; the offhand item is 50% torches underground or bread underwater, else a tool. Offhand rolled on its own, not only after a main hand hit (Q26) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 26 | tool materials and copper (Q25, item 28) | open | Mirror armor: wood, stone, copper 25 each, iron 8; sword or pickaxe 50/50; unwaxed copper fully oxidized, 10% waxed with the armor's state rules; worn 30 to 90%; 5% enchant; a second tool copies the first one's material 50% of the time | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 27 | ocean corpse gravity, skeleton floating (items 29, 30) | clear | Ocean corpses keep gravity; every skeleton-stage corpse sinks instead of floating | 2026-11-18 | chat 2026-11-18 |
| 28 | offhand stack sizes (Q27) | open | Torches 1 to 6, bread 1 to 3, configurable | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 29 | ancient corpses from the first build (sealed mark, NoGravity) (Q28) | open | No migration: new spawns only; test-world leftovers may despawn or float (nothing deployed) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 30 | second phase scope (Q29) | open | `corpses #8`, `#9` and the rest of `#4` (sectioned screen, curio slot check, cosmetic and curio rendering) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 31 | corpse screen layout (item 27 cannot be literal: the corpse screen has no crafting grid) | conflict | Cosmetic armor as 4 slots on the corpse's top row between the real armor and the offhand; curios in a Curios-style column left of the window (Curios' own panel art, drawn at runtime) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 32 | curio slot origin | open | Recorded at death: each curio stack tagged with its slot type and index, tag removed when it leaves the corpse; Transfer Items returns it to that exact slot | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 33 | order of the rest of `#4` | open | Round 1: curio origin tags, sectioned screen, live cosmetic armor bug fix, screenshot look-check. Round 2: curio and cosmetic rendering on both models | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 34 | Compat clash with a parallel session (uncommitted surface-layer work) | open | Write the `#4` code now, commit only own files; build and install Compat only after the other work is committed | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 36 | height curve share (build note 6b: the curve left 92% below y16) | open | Retune to about 80% below y16 (built 2026-11-22, see 6b) | 2026-11-22 | owner rulings 2026-11-22 (M2) |
| 37 | skeleton elytra look (3.6 round 2: vanilla wings only) | open | Skeleton corpses also show Customizable Elytra colors and banners | 2026-11-22 | owner rulings 2026-11-22 (M2) |
| 38 | build choices the rulings did not fix (6b, 3.6 build notes) | clear | Every other corpse build choice confirmed (owner, 2026-11-22) | 2026-11-22 | owner rulings 2026-11-22 (M2) |
| 35 | round 1 commit 3588e492 did not compile (config fields and a mixin entry pasted out of place); order of the next steps | open | Fix, install, then round 2; the round 1 and round 2 screenshot look-checks are done together later | 2026-11-19 | AskUserQuestion 2026-11-19 |

Armor rolls (ruling 15), share of ancient corpses by pieces worn:

| Pieces | Chance |
|---|---|
| 0 | 44.0% |
| 1 | 36.4% (0.56 x 0.65) |
| 2 | 16.7% (0.56 x 0.35 x 0.85) |
| 3 | 2.79% (0.56 x 0.35 x 0.15 x 0.95) |
| 4 (full set) | 0.0147%, about 1 in 6800 (0.56 x 0.35 x 0.15 x 0.05) |

A full set of one material needs pieces 1 and 2 to roll the same material (pieces 3 and 4 then copy it): at the
weights in ruling 16 that is 19.5% of full sets, about 1 in 35000 corpses for any one-material set and about 1 in 1.4
million for full iron or full chain.

Agent defaults (confirmed with every other build choice, owner, 2026-11-22, ruling 38): the ancient corpse feature is skipped in biomes with no cave floor
(none expected); enchant chance about 5 percent per piece; RGB "far apart" = distance of at least 200 (of 441); unaffected-copper penalty halves its weight; lukewarm and warm oceans count as warm, cold and frozen as cold; Visceral Heap drops 1 to 3 bones, 0 to 1 bone chunk, 0 to 2 cracked bones; `philipsruins:cracked_bone` gets a dict key (only `bone_chunk` has one), values in `loot_config.json`;
ocean corpses only in `#minecraft:is_ocean` biomes (not rivers); cave corpses below the sea level only.

## 5. Note: ancient corpse loot (to work out)

Deferred to the loot overhaul (`loot/tables #1`, items 32 and 33); the placeholder tables stay until then.

Determine the loot for ancient corpses, a "dead player" kit: what an explorer would carry when they died there.
To work out with the owner before the loot tables are filled:
- Cave kit candidates: torches, a worn pick or shovel, food (rations, a can), rope or ladders, a little ore or ingots,
  a map, coins (Lightman's Currency), a damaged weapon.
- Ocean kit candidates: a worn sword (no trident), a compass or map, fish or rations, a bucket, maybe a
  water-breathing item; no tool that makes the ocean trivial.
- Tier by depth? (deeper caves, better kit), and check against loot_tiers.md and the existing chest tiers so ancient
  corpses do not out-loot dungeons.
- Durability: worn items (random damage), no enchantments or light ones.
- Use `loot_census.py` afterwards to check the new tables against the rest.

### 5b. Note: corpse tools and held items (to design later)

Ancient corpses get a tool system like the armor rolls (worn tools by material, configurable), and a chance to hold
a tool or an inventory item in the main hand or offhand (shown on the model, lootable, follows contents).

## 6. TODO rows

- `corpses #1`: corpse decay (Creative Compat, items 1 to 4).
- `corpses #2`: ancient corpses (Creative Loot, items 5 to 10).
- `corpses #4`: cosmetic armor and curios into the corpse, sectioned corpse screen, cosmetic and curio rendering (section 3.3).
- `corpses #5`: ancient corpse armor rolls (chains, chances, materials, copper state and wax, dyed leather), rulings 14 to 17.
- `corpses #6`: Nether and End corpse rules, wither skeleton corpse, rulings 18 to 20 and 23 (Creative Compat).
- `corpses #7`: Visceral Heap "Ancient Remains", breakable with bone drops, rulings 21 and 22.
- `corpses #3`: ancient corpse loot tables (section 5); `corpses #2` ships with placeholder tables until this is done.
  Pinned on the loot overhaul (`loot/tables #1`), a cousin of the cave ruin, underwater ruin and shipwreck tables (items 32, 33).
- `corpses #8`: held items and the no-empty guarantee (rulings 24 to 26). `corpses #9`: skeletons sink, ocean corpses keep gravity (ruling 27).

## 6b. Build notes (2026-11-18)

Built: `corpses #1`, `#6` (Creative Compat, [creative_compat.md](creative_compat.md) Corpse decay), `#2`, `#5`, `#7`
(Creative Loot, [creative_loot.md](creative_loot.md) 2c); of `corpses #4` only ruling 9 (`CosArmorKeepThroughDeath = false`,
in `config/` and `configs_edited/`; CQ-CMP-15). Still open in `#4`: the curio slot check, the sectioned corpse screen
and the cosmetic and curio rendering (3.3). Choices the rulings did not fix (confirmed, owner, 2026-11-22, ruling 38):
- **Sealed corpses** (replaced by ruling 24 in the second pass). An ancient corpse with no armor and its loot not yet rolled holds nothing, so the empty rule
  (ruling 8) would remove it 5 min after its chunk first ticks, before anyone finds it. It carries Compat's
  `creativecompat:corpse_sealed` mark until first opened and counts as full while sealed.
- **Height curve** (ruling 12): the 25x density gap between y-64 and y32 leaves 92% of target heights below y16 (not
  80%); 78% are below y0. Taken as "gap first, 80% flexible". Real spread waits on the survey row SQ-LOOT-08.
  Retuned 2026-11-22 (ruling 36): `y_curve` in `loot_config.json` is now -64 1.0, -32 0.8, 0 0.65, 16 0.6, 24 0.6,
  32 0.55, 48 0.15: 80.3% of target heights below y16, 67% below y0, 7.5% above y32. A monotone curve with 80% below
  y16 cannot keep a wide gap (the band above y16 needs a fifth of the weight), so the y-64 : y32 rate gap is now 1.8x
  (was 25x). The fallback default in Creative Loot's `AncientCorpseConfig` still holds the old points (Creative Loot
  is owned by another agent this session).
- **Plain oceans** (`ocean`, `deep_ocean`, modded oceans) count as neutral, temperature factor 2, between warm 1 and cold 3.
- **Wither look** (ruling 20): the renderer reads vanilla `textures/entity/skeleton/wither_skeleton.png` from its own
  path instead of a copy, so no new texture file and no sources.csv row; resource packs apply.
- **Ocean corpses** have no gravity: the Corpse mod floats corpses up through water. Reversed by ruling 27 (second pass).
- **Placeholder loot** until `corpses #3`: cave = 1 to 6 torches and sometimes bread; ocean = sometimes bread.
- **Remains drops** are rows in `loot_config.json` (`remains.drops`), rolled in Java, not a loot table: a 1.19.2 loot
  table cannot be gated on Philip's Ruins. `philipsruins:cracked_bone` got no dict key (no script names it).

Second phase (2026-11-18, rulings 24 to 30): built `corpses #8` (Creative Loot `AncientHands`, block `hands` in
`loot_config.json`) and `#9` (Compat `[corpses] skeletonsSink`, `sinkSpeed`; ocean corpses lose `NoGravity`). Choices
the rulings did not fix (confirmed, owner, 2026-11-22, ruling 38):
- **Waxed copper states are shared** between a corpse's armor and its copper tools (one state, or two, for the whole
  kit), since the tool rolls "mirror armor" (ruling 26).
- **Offhand tool material copy** (ruling 26, "a second tool copies the first") applies only when the main hand holds a
  tool; an offhand tool on an empty main hand rolls its material freely.
- **Sinking speed**: skeletons accelerate down 0.02 a tick to `sinkSpeed` 0.1 blocks a tick (2 blocks a second), in
  water and lava alike.
- **Hands tuning** has its own `damage`, `enchant_chance`, `enchant_levels` (same values as the armor) so they can
  drift apart later.

## 7. Tests

Prefixes: `CMP-` (creative_compat.md) for decay, `LOOT-` (creative_loot.md) for ancient corpses. Queue: SQ-CMP-10 to
-13, CQ-CMP-14 (decay); SQ-LOOT-07 to -09, CQ-LOOT-10 (ancient corpses, remains); second phase SQ-LOOT-11, CQ-LOOT-12
(held items), SQ-CMP-16 (sinking); `corpses #4` round 1 SQ-CMP-17, CQ-CMP-18 (screenshot look-check), CQ-CMP-19,
CQ-CMP-20; round 2 CQ-CMP-21, CQ-CMP-22 (look-checks done with CQ-CMP-18).

## History

- 2026-11-18: plan written from the scratchpad block and chat; premise checks in section 1; rulings Q1 to Q8 recorded; rows `corpses #1` to `#3`.
- 2026-11-18: section 3.4 added (placement, armor rolls, copper, dye, Nether, End, Visceral Heap); rulings 11 to 23; rows `corpses #5` to `#7`; `corpses #2` narrowed to placement.
- 2026-11-18: section 3.3 added (cosmetic armor and curios); rulings 9 and 10; row `corpses #4`.
- 2026-11-18: built `corpses #1`, `#2`, `#5`, `#6`, `#7`; build notes in 6b.
- 2026-11-18: second pass notes (3.5, items 25 to 34); rulings 24 to 27; rows `corpses #8`, `#9`; `#3` pinned on `loot/tables #1`.
- 2026-11-18: second phase questions: ruling 25 offhand made independent; rulings 28 to 30.
- 2026-11-19: built `corpses #4` round 1 (3.6 items 1 to 6); build notes under 3.6.
- 2026-11-19: ruling 35: round 1 repaired (52a568a9) and installed; built `corpses #4` round 2 (curios, Elytra Slot elytra and cosmetic armor on both corpse models); build notes under 3.6.
- 2026-11-22: rulings 36 to 38 (owner rulings M2): height curve retuned to 80% below y16; Customizable Elytra on
  skeleton corpses built (Compat); every other build choice confirmed.
- 2026-11-18: built `corpses #8`, `#9`; `#4` round 1 designed (3.6), rulings 31 to 34.
