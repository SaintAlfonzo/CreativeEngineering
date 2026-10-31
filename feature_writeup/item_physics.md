# Item physics: fire vs lava burning, floating, and what burned items leave behind

## 0. At a glance

| Part | Status |
|---|---|
| Fire/lava split on top of ItemPhysic: per item, survive fire, survive lava, both, or neither (`ItemPhysicSplit` + `ItemPhysicServerMixin`) | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3); its recurring `$1` crash (F1) was fixed in 1.0.9 |
| Float/sink control in water, additive to ItemPhysic's own list | DEPLOYED |
| Four item tags `#creativeengineering:physics/{floats,sinks,fireproof,lavaproof}`, filled in KubeJS (reloadable) with a small jar baseline | DEPLOYED |
| Burn transforms: glass → dirty glass, wood → 25% ash, TNT / TNT minecart / gunpowder explode (TNT with shrapnel) | DEPLOYED |
| Family classification audit: `/dumpphysics` + `tools/item_physics_audit.py` → [item_physics_audit.md](item_physics_audit.md) (section 9) | BUILT 2026-10-28, report only; `/dumpphysics` not run yet (SQ-IP-16) |
| Surface floating for light items and lava floating for lavaproof items (section 10) | DESIGNED 2026-10-28, not built |
| Allurement Curse of Ascension / Fleeting on dropped items (`AllurementItemCurses`, history 2026-10-27) | DEPLOYED to the client with 1.1.04 (changelog Fixed bullet); same cause as TODO `items/item-physics #3` (forced `isNoGravity` skips the gravity branch), so that row is fixed, not open; IP-15 / SQ-IP not run |

- **Pending:** the families (section 9) are a proposal: nothing writes them into the physics tags yet. 89% of the
  pack's items (17,650 of 19,743) are in no physics tag and sink and burn by default. IP-01 to IP-14 have never been
  run. Findings F1 to F9 (section 4); decisions in section 9.4 and 10.4.
- **Files to open:** [item_physics_tags.js](../kubejs/server_scripts/tags/item_physics_tags.js) (what floats and
  burns), [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js) (burn transforms),
  [itemphysic/ItemPhysicSplit.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/itemphysic/ItemPhysicSplit.java),
  [mixin/ItemPhysicServerMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemPhysicServerMixin.java).
- **Knobs:** the four tags (`/reload`); the burn-transform rules and programmatic rules (startup, restart);
  ItemPhysic's own `config/itemphysic.json` (lists `swimmingItems`, `burningItems`, `undestroyableItems`); TNT shrapnel
  in `[shrapnel]` ([tnt_shrapnel.md](tnt_shrapnel.md)).

## 1. Overview

ItemPhysic Full 1.6.7 (with CreativeCore 2.11.28) makes dropped items behave physically: they don't bob, they sink or
float in water by a list, and anything on its `burningItems` list is destroyed by fire **and** lava alike. There is
one list for both hazards, so an item can't survive a campfire and still melt in lava. The pack's
`config/itemphysic.json` makes every item sink and burn by default.

The pack splits fire from lava, makes floating additive and tag-driven, and lets some items leave something behind
when they burn.

## 2. Reference

### 2.1 Water and fire axes

| Tag | Water | Fire | Lava |
|---|---|---|---|
| no tag | sinks | destroyed | destroyed |
| `physics/floats` | floats | | |
| `physics/sinks` | sinks, beats floats and ItemPhysic's list | | |
| `physics/fireproof` | | survives | destroyed |
| `physics/lavaproof` | | survives | survives (beats fireproof) |

An item can be in one water tag and one fire tag (a crimson boat floats and is fireproof). Programmatic rules (2.3)
add two finer states, burns in fire only and burns in lava only, that the tags can't express.

"In lava" means the item entity's eyes or body are in lava, or the block it's in or the one below is lava (so items
resting in a shallow pool count); anything else burning it is fire.

### 2.2 What's in the tags (KubeJS + jar baseline)

[item_physics_tags.js](../kubejs/server_scripts/tags/item_physics_tags.js), on top of the jar's seed files
(`floats.json` 16 entries, `lavaproof.json` 17, `fireproof.json` and `sinks.json` empty):

- **Floats:** wood and everything made of wood (planks, logs, slabs, stairs, fences, doors, trapdoors, buttons, plates,
  signs, boats, banners, wooden chests and rods), light organics and crafting powders (bone meal, feathers, paper...),
  hay, shields, wooden tools, armor stands.
- **Sinks:** heavy powders (gunpowder, redstone, glowstone, blaze powder, sugar), ender pearls and eyes, blaze rods.
- **Lavaproof:** the stone family (every `#forge:stone`, cobblestone and Create stone type), obsidian, end stone,
  netherrack and nether blocks, quartz, diamond, netherite, prismarine building blocks, shulker boxes and shells,
  fire-immune mob drops (magma cream, blaze rod and powder, ghast tear), and all echo, felsic and dragon gear.
- **Fireproof:** metal storage blocks, ice / packed ice / snow, the sculk family, sea lanterns, bottled honey.
- **Left burning on purpose:** TNT, torches, campfires, coal, redstone torches and lamps, targets, nether wart.

The file's header lists each grey-area call ("flip any of these freely").

### 2.3 Burn transforms (`global.itemPhysics`)

When fire or lava destroys a dropped item (ItemPhysic's item health reaches 0 from a fire source):

| Items | Becomes | Chance |
|---|---|---|
| `#forge:glass` (incl. tinted) | Quark dirty glass | 100%, per item in the stack |
| `#forge:glass_panes` | Quark dirty glass pane | 100%, per item |
| `#quark:shards` | Quark dirty shard | 100%, per item |
| `#creativeengineering:burn/to_ash` (planks, logs, wooden building blocks, boats, banners, beds, chests, scrap wood) | Supplementaries ash | 25%, per item |
| `minecraft:tnt`, `tnt_minecart` | explosion power 2, plus a TNT shrapnel burst | 100% |
| `minecraft:gunpowder`, `#creativeengineering:burn/explosive` | explosion power 1 | 100% |

- Explosions don't break blocks and hurt on every difficulty (`BurnBlastDamage`: an explosion source without
  difficulty scaling, so Peaceful players take damage too; Blast Protection applies).
- Products are tagged so they don't transform again in the same fire; dirty glass, panes, shards and ash are also
  excluded outright.
- Note the order with the fire axis: wood floats but is not fireproof, so it ashes in fire and lava; glass isn't
  fireproof either.

## 3. What the pack changes

- **The mixin:** ItemPhysic asks one question for every hazard, `SortingList.canPass(stack)`, against the list in
  play. `ItemPhysicServerMixin` redirects that call in `fireImmune`, `hurt`, `updatePre` and
  `updateFluidHeightAndDoFluidPushing`, and dispatches on which list it is:
  - `burningItems`: `ItemPhysicSplit.burnDecision(stack, touchingLava)`; no rule → ItemPhysic's own list.
  - `swimmingItems`: `physics/sinks` → sink; else `isFloat` (tag or rule) or ItemPhysic's list.
  - anything else (`undestroyableItems`) untouched.
- **Burn death:** a second redirect on `discard()` in `hurt` runs `ItemBurnTransform.apply` first when the killing
  source is fire, server side, and the item isn't already a product.
- **Tags vs rules:** tag membership is read live (`/reload` applies); the programmatic API (`float`, `sink`,
  `unburnable`, `fireOnly`, `lavaOnly`, `burnReplace`, `burnExplode`, `burnExclude`) is startup-only and is reset and
  re-applied each launch. Today all float/burn choices live in tags; only the burn transforms use the API.
- **Tag helper:** `physicsFromTag(e, behaviour, sourceTags, { except })` adds a nested tag reference (live) or, with
  `except`, the expanded members.
- **Force-load:** `ItemPhysicSplit` is force-loaded at startup (`CreativeEngineering.java` line 273) against the
  late-class-load crashes this area has had.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 in patcherMod 1.0.9 (DEPLOYED with pack 1.0.9): if-chain, no `ItemPhysicSplit$1` in the jar ([plan_1.0.9.md](plan_1.0.9.md)). Was (before the fix): `burnDecision` switched on the `Burn` enum, so
  javac makes a synthetic `ItemPhysicSplit$1` class that is loaded lazily and isn't covered by the force-load; it
  crashed the server twice in September (`NoClassDefFoundError`). Fix: an if/else chain instead of the switch, or
  force-load `$1` too. Was in the old TODO.md section 3; recurrence since not checked.
- **F2.** FIXED 2026-10-19 (jar name generic, fireproof listed, "chat" gone, "Future" note points at TODO `items/item-physics #2`). **Stale comments:** `item_physics.js` says "creativeengineering-1.0.2.jar" and lists three tags (fireproof is
  missing); `item_physics_tags.js` says "see the breakdown in chat" (that chat is gone; the header itself is the
  breakdown) and has a "Future" note (Create components sink and fireproof, belts float, flour floats, treated wood
  sinks, "add items from all other mods") that was never done.
- **F3. The jar seeds two empty tag files** (`fireproof.json`, `sinks.json`). Harmless; KubeJS fills both.
- **F4. Modded items mostly fall to the default (sink and burn).** Measured 2026-10-28: 17,650 of 19,743 items (89%)
  are in none of the four tags; Minecraft is 84% covered, Create 35%, most other mods under 10%. Design for closing
  it: section 9.
- **F5. RESOLVED 2026-10-28 (read from ItemPhysic 1.6.7): vanilla fire resistance is honoured.** `ItemPhysicServer.hurt`
  returns early when `!item.canBeHurtBy(source)`, and vanilla `Item#canBeHurtBy` is false for a fire-resistant item
  against any fire source (`IN_FIRE`, `ON_FIRE`, `LAVA`). So every `Item.Properties#fireResistant()` item survives
  fire and lava whatever its tags (netherite, `tuneTools` / `tuneArmor` `fireproof: true`, modded fire-resistant gear).
  The split can't make such an item burn. It still sinks in lava and smokes, but takes no damage.
- **F7. Lava survivors today (2026-10-28, from the tag and gear dumps):** 593 items. 538 by the `lavaproof` tag
  alone, 37 by the tag and their own fire resistance, 15 by fire resistance alone (Botania terrasteel and terra
  tools, Create netherite backtank and diving gear, the netherite knives, `creategoggles` netherite helmets,
  `abnormals_delight:necromium_knife`, `creategbd:beam_reactor_helmet`), 3 also on ItemPhysic's `undestroyableItems`
  (nether star, bedrock, obsidian). By mod: Minecraft 222, Create 208 (stone types), Design Decor 84, Dragon Loot 15,
  Felsic 11, IE 10, Quark 10, the pack 7, Botania 7, others 1 to 5. Fire-resistant non-gear items (netherite scrap
  is also tagged; modded ones are unknown) only show once `/dumpphysics` has run. The list and the 38 survivors whose
  family isn't `refractory` are in [item_physics_audit.md](item_physics_audit.md) section 4.
- **F8. Why floating items dive and bob (read from `ItemPhysicServer.updatePre` / `update`).** In water a floating
  item gets +0.04 per tick upward (until it rises at 0.1 per tick), and ItemPhysic applies **no vertical drag in a
  fluid**. Simulated with those constants: dropped from 3 blocks it dives 2.8 blocks before turning, then bobs
  about 0.35 blocks forever. The "in fluid" test is the item's block position, so it rests at the top of the water
  block, slightly above the drawn surface. Section 10 has the fix.
- **F9. Nothing floats in lava.** The float branch is skipped for lava (`!fluid.is(LAVA)`); lava viscosity is 6000
  (6x water), so every item, lavaproof or not, sinks slowly (0.0033 per tick) and stops sideways almost at once
  (horizontal speed x0.14 per tick).
- **F6. Raw `event.add` inside `physicsFromTag`** rather than `global.tags.add`. Small; same rule as the other
  writeups.

### Known limits

- **ItemPhysic only:** items in a hopper, on a belt or in a container are not affected; only dropped item entities.
- **Burn transforms only fire on the kill:** an item that survives (lavaproof) never transforms.
- **Explosions** from burning TNT hurt entities but never break blocks.

### Rejected options

- **Editing ItemPhysic's two lists:** one list can't separate fire from lava, and the config can't be reloaded; tags
  can.

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/items_item-physics.md](briefs/items_item-physics.md). Nothing here is built.

- [TODO](todo/TODO.md) `items/item-physics #5`: the goose's golden egg (`vintagedelight:golden_egg`) falls and floats like an ordinary item. The drop code
  (`GoldenGooseEgg.drop`) removes no gravity, and the egg is in none of the four physics tags (eggs float, untagged items sink and burn):
  reproduce first, then add it to the `floats` group in `item_physics_tags.js`; decide fireproof. Open: what exactly is wrong in game.

## 5. File map

- patcherMod: [itemphysic/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/itemphysic/) (`ItemPhysicSplit`, `ItemBurnTransform`, `BurnBlastDamage`, `AllurementItemCurses`), [mixin/ItemPhysicServerMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemPhysicServerMixin.java), `data/creativeengineering/tags/items/physics/*.json` (seed).
- KubeJS: [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js) (`global.itemPhysics`), [tags/item_physics_tags.js](../kubejs/server_scripts/tags/item_physics_tags.js).
- Audit: [_lib/dump_physics.js](../kubejs/server_scripts/_lib/dump_physics.js) (`/dumpphysics`, in `/dumpall`),
  [tools/item_physics_audit.py](../tools/item_physics_audit.py) (`/cegen run item_physics_audit`), output
  [item_physics_audit.md](item_physics_audit.md) and `kubejs/generation_scripts/physics/item_physics_families.json`.
- Config: `config/itemphysic.json`.
- Related: shrapnel ([tnt_shrapnel.md](tnt_shrapnel.md)).

## 6. Offline checks

- `python3 tools/item_physics_audit.py` (add `--check` to exit 1 while any item is unclassified). Reads the dumps;
  without `kubejs/exported/physics/item_physics.json` it classifies by tag, gear and name only.
- `/dumptags` lists the four physics tags' final members.

## 7. In-game test suite

Drop items with Q into the hazard.

- IP-15: drop an item with Curse of Ascension (anvil or `/give` with the enchantment) and one with Curse of Fleeting. Ascension rises; Fleeting pushes nearby mobs and players away. Queued as SQ-IP-15.

- [ ] **IP-01 Default:** drop a Create cogwheel in water → sinks; in fire → burns; in lava → burns.
- [ ] **IP-02 Floats:** oak planks, a boat and bone meal in water → float. Gunpowder → sinks.
- [ ] **IP-03 Sinks wins:** an item in both floats and sinks (add one via a datapack tag) → sinks.
- [ ] **IP-04 Fireproof:** an iron block into a campfire → survives; into lava → destroyed.
- [ ] **IP-05 Lavaproof:** cobblestone, a diamond and a netherite ingot into lava → all survive.
- [ ] **IP-06 Shallow lava:** a gold block resting in a 1-deep lava pool → destroyed (counts as lava).
- [ ] **IP-07 Dirty glass:** 8 glass into fire → 8 Quark dirty glass; those 8 back into fire → just burn.
- [ ] **IP-08 Ash:** 16 oak planks into lava → about 4 Supplementaries ash.
- [ ] **IP-09 TNT:** a TNT item into fire → explosion (no block damage) with shrapnel; gunpowder → a small pop.
- [ ] **IP-10 Fire-resistant gear (F5):** a felsic sword into lava → survives (lavaproof tag); a netherite sword → survives.
- [ ] **IP-11 Peaceful blast:** on Peaceful, stand next to burning TNT items → you take damage.
- [ ] **IP-12 Tag reload:** remove `minecraft:oak_planks` from floats in a datapack override, `/reload`, drop planks in
  water → sink. Remove the override.
- [ ] **IP-13 No crash (F1):** throw a stack of mixed items into lava and fire repeatedly over a few minutes on the
  dedicated server → no `NoClassDefFoundError ... ItemPhysicSplit$1`.
- [ ] **IP-14 Echo gear:** an echo helmet into lava → survives.
- [ ] **IP-21 Physics dump (SQ-IP-16):** `/dumpphysics` → chat "Dumped N items (M survive lava)", no `[dumpphysics]
  skipped` flood in latest.log; `kubejs/exported/physics/item_physics.json` rows carry `material` and `sound` for
  block items. Then `python3 tools/item_physics_audit.py`: the header line says "dump yes", unclassified drops below
  the 650 of the tag-only run, and section 4 lists `minecraft:netherite_scrap` under `fire_resistant_item`.

## 8. History

- 2026-09-08 to 09-09: fire/lava split, float control, burn transforms; two crashes (a wrong `@Redirect` signature,
  fixed; the `$1` switch class, not fixed).
- By 2026-09-27 (initial git commit): tags filled; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F6; tests IP-01 to IP-14. No code changed.
- 2026-10-19: F1 fixed in patcherMod 1.0.9 ([plan_1.0.9.md](plan_1.0.9.md)). IP-13 still to run.
- 2026-10-19: F2 fixed (item_physics.js and item_physics_tags.js headers).
- 2026-10-24: planned from the scratchpad: golden egg physics (`items/item-physics #5`). No code changed.
- 2026-10-27: Allurement's Ascension/Fleeting hook sits in the gravity branch that ItemPhysic's `isNoGravity -> true` redirect skips; `ItemPhysicServerMixin` now calls them at the tail of `updatePre` (`AllurementItemCurses`, reflection, optional). Built and installed in patcherMod 1.1.03; IP-15 not run.
- 2026-10-28: analysis of the integration; F4 measured, F5 resolved from ItemPhysic's source, F7 to F9 added.
  Built the `/dumpphysics` dump and `tools/item_physics_audit.py` (report only). Designed the material families
  (section 9) and surface / lava floating (section 10). No game code changed.

## 9. Families: classifying every item (design, 2026-10-28)

### 9.1 Goal and principle

Every item gets exactly one **family**, chosen from what the item is made of. The family fixes its water and fire
behaviour. Tags stay the runtime mechanism (the four physics tags, read live by `ItemPhysicSplit`); families decide
what goes into them. No item can be missed: an item that matches no rule lands in `unclassified`, and the audit
lists it by mod. An item that only matched its mod's default is also listed.

### 9.2 The families

| Family | Water | Fire | Covers | Today |
|---|---|---|---|---|
| explosive | sink | burn, explodes | TNT, gunpowder, bombs, charges | burn transform exists |
| refractory | sink (floats on lava, 10) | lavaproof | natural stone, deepslate, nether and end stone, obsidian, diamond, netherite, fire-resistant items | = `lavaproof` |
| metal | sink | fireproof | ingots, nuggets, plates, sheets, metal blocks, rails, parts, metal tools and armor | partly `fireproof` |
| machine | sink | fireproof | circuits, storage, computers, electronics, AE2 / RS / PNC devices | none |
| masonry | sink | fireproof | bricks, terracotta, concrete, tiles, plaster, pottery | partly |
| earth | sink | fireproof | sand, gravel, dirt, mud, clay, salt, silt | partly |
| crystal | sink | fireproof | gems other than diamond, amethyst, prismarine, certus, fluix | partly |
| glass | sink | burn → dirty glass | glass, panes, windows, shards | burn transform exists |
| container | sink | fireproof | potions, bottles, buckets, jars, cans, drinks | partly |
| powder | sink | fireproof | redstone, glowstone, metal dusts, crushed ores | `sinks`, burns |
| ice | **float** | fireproof | ice, packed and blue ice, snow | sinks |
| wood | float | burn → ash | logs, planks, wooden blocks, furniture, wooden tools | = `floats` + `burn/to_ash` |
| nether_wood | float | **fireproof** | crimson and warped wood (`#minecraft:non_flammable_wood`) | floats, burns |
| produce | float | burn | crops, fruit, vegetables, mushrooms, kelp, gourds | partly |
| food | float | burn | prepared food not in a container | partly |
| organic | float | burn | bone, flesh, hide, leather, slime, shell, horn, eggs, coral | partly |
| foliage | **skim** (10) | burn | leaves, flowers, petals, seeds, grass, saplings, vines, tobacco | floats |
| fibre | **skim** | burn | wool, carpet, cloth, string, rope, paper, books, feathers, hats, beds, candles | floats |
| light_powder | **skim** | burn | flour, sugar, bone meal, dyes, sawdust, pollen, cocoa | mixed |
| technical | sink | burn | spawn eggs, debug and creative-only items | not audited |
| unclassified | sink | burn | no rule matched | listed by the audit |

Three water behaviours (skim / float / sink) and three fire behaviours (burn / fireproof / lavaproof) cover
everything the pack does today, plus the transforms each family already has. 21 families is small enough to read
in one table, and each maps to a material a player would recognise.

### 9.3 How an item finds its family (rungs, first match wins)

1. **Manual override:** an explicit id list kept in `item_physics_tags.js` (today's hand decisions that differ
   from their family, e.g. prismarine and nether brick lavaproof). Beats everything.
2. **Tag:** a vanilla / Forge / pack tag (`#minecraft:planks`, `#forge:ingots`, `#forge:glass`, the pack's
   `creativeengineering:food_type/*`, ...). The most reliable signal, and modded items usually carry Forge tags.
3. **Fire resistance:** `Item#isFireResistant` → refractory (it survives lava anyway, F5).
4. **Block material and sound** (block items, from `/dumpphysics`): `Material.WOOD` → wood, `STONE` → refractory unless
   the id says brick / machine / metal, `METAL` → metal, `WOOL` → fibre, `PLANT` / `LEAVES` → foliage, and so on. A
   metal-sounding block on stone material is metal.
5. **Gear:** tool tier / armor material name (iron → metal, diamond / netherite → refractory, leather / wood → wood).
6. **Edible:** food, unless the name says container (bottled drinks), produce or organic.
7. **Name token:** whole tokens of the id path (`oak`, `brick`, `copper`, `flour`, ...), with a few compound parts
   (`stone`, `glass`, `flower`).
8. **Mod default:** one family per mod for what is left (AE2 → machine, Create → metal, Simple Hats → fibre, ...).
9. **None:** `unclassified`. The audit lists every one by mod and `--check` exits 1.

### 9.4 Audit results (tag-only run, 2026-10-28; rerun after `/dumpphysics`)

- 19,743 items; rungs: name 12,811, tag 3,422, mod default 2,640, gear 168, fire resistance 52, **unclassified
  650** (Botania 287 non-block magic items, Minecraft 119 odds and ends, Quark 111, Supplementaries 85, ...). The
  material rung isn't used yet: no `/dumpphysics` output exists, so blocks fall to their names.
- The families agree with **91% of today's hand decisions** (1,911 of 2,093 tagged items). The 182 that differ are
  mostly proposals to decide, not classifier errors:
  - nether wood: floats and burns today, the family says fireproof (97 items); vanilla crimson and warped wood
    don't burn.
  - ice and snow: sink today, the family says float (5).
  - the pack's grey-area calls: nether brick and prismarine are lavaproof today (masonry / crystal say fireproof);
    smooth stone and sandstone are fireproof today, but their names hit the refractory rung (29).
- Name-rung misses still to expect: colour words, furniture named after wood in metal mods, Botania flower names.
  The material rung fixes most block items. Each run's list is in [item_physics_audit.md](item_physics_audit.md)
  sections 5 and 6.

### 9.5 Applying it (not built)

1. Run `/dumpphysics` and the audit, and settle the decisions (9.6).
2. Move the classifier's tables into a generator mode of the same script that writes one item tag per family,
   `creativeengineering:physics/family/<family>`, as JSON under `kubejs/data/creativeengineering/tags/items/physics/family/`.
   It reads the manual-override list too, so every item lands in exactly one family tag.
3. `item_physics_tags.js` maps families to behaviour with nested tag refs through `global.tags` (e.g. floats ⊇
   `#…/family/wood`, `#…/family/produce`). The hand lists shrink to the override list.
4. New mods: `/dumpall`, `/cegen run item_physics_audit`; unclassified items or a mod with no default show in the
   report; add a tag rule, a name token, a mod default or an override, then rerun.

### 9.6 Decisions

- **Nether wood fireproof?** Recommended yes (matches vanilla, where crimson and warped don't burn).
- **Ice floats?** Recommended yes.
- **Machines fireproof?** Electronics are plausible either way. Recommended fireproof, so a dropped AE2 drive
  isn't lost to a campfire.
- **Light items skim** (needs section 10) or just float. Recommended skim for foliage, fibre and light powder.
- **Stone family:** keep all natural stone lavaproof (the pack's existing choice) or reserve lavaproof for the
  volcanic / deep stones. Recommended keep.

## 10. Surface floating and lava floating (investigation, 2026-10-28)

### 10.1 What ItemPhysic does (F8, F9)

Per tick in a fluid, with the constants from `ItemPhysicServer.updatePre` and `update`:

| Case | Vertical change per tick | Drag | Result |
|---|---|---|---|
| floating item in water | +min(0.04, 0.1 − vy) | none vertical | dives 2.8 blocks from a 3-block drop; bobs about 0.35 for good |
| sinking item | −0.02 / viscosity; ×0.8 when falling faster than 0.1 | | sinks about 0.1 blocks per tick |
| any item in lava | −0.0033 (viscosity 6) | sideways ×0.14 | sinks slowly; floating is never applied |

### 10.2 Proposed motion (simulated, not built)

Add a **skim** water behaviour, plus lava floating for lavaproof items. Both use one damped surface spring, applied
at the tail of `ItemPhysicServer.updatePre` (the mixin already injects there for Allurement) so it replaces
ItemPhysic's vertical force for those items only:

- **Entry look-ahead (in air):** when the block below is the fluid (`CommonPhysic.getFluid(item, true)`) and this
  tick's fall would carry the item past the surface, clamp the fall so it lands on the surface. Without this, a
  fast item overshoots by one tick's travel (0.35 blocks or more) whatever the drag.
- **In the fluid:** `vy = vy × drag + clamp((restY − y) × k, ±0.06)`, with `restY` just below the surface (the top
  fluid block's height, found by walking up at most a few blocks).

Simulated (scratchpad `float_sim.py`, the same tick order as ItemPhysic):

| Behaviour | drag / k / rest depth | Deepest after a 3-block drop | Bob when settled |
|---|---|---|---|
| ItemPhysic float (today) | none | 2.81 | 0.355 |
| float, damped | 0.75 / 0.06 / 0.15 | 1.07 | 0 |
| skim | 0.45 / 0.12 / 0.05 | 0.55 | 0 |
| skim + entry look-ahead | same | about 0.1 (estimated: the overshoot is what remains) | 0 |
| lava float (lavaproof in lava) | 0.35 / 0.10 / 0.08 | 0.23 from a 2-block drop | 0 |

Water: skim for foliage, fibre and light powder; damped float (an optional upgrade) for the other float families,
which keeps the dip-and-rise of heavier wood and food but stops the endless bob. Lava: everything in the
`lavaproof` tag (refractory family) floats on top, since lava is dense; everything else keeps sinking and burning.

### 10.3 Build sketch

- `ItemPhysicSplit`: a `Buoyancy` query (SKIM / FLOAT / SINK in water; FLOAT / SINK in lava) from a new
  `creativeengineering:physics/skims` tag and the existing `lavaproof` tag. Reload-able like the other four.
- `ItemPhysicServerMixin`: extend the existing `updatePre` TAIL inject; read `ItemPhysicServer.fluid` (a public
  ThreadLocal set at the start of `updatePre`). It runs on both sides (item ticks are simulated client-side too,
  and tags are synced), so the motion is smooth without extra packets.
- The `updateFluidHeightAndDoFluidPushing` 0.3 inflate for floaters stays (currents still push skimmers).
- Tuning knobs in `CEConfig [itemphysic]`: drag, k and rest depth per behaviour.

### 10.4 Decisions and the player-in-lava note

- **Build it?** Small (one query, one inject, three config values) once 9.6 is settled.
- **Player speed in lava: to investigate (TODO `items/item-physics #7`).** Vanilla already slows a player in lava more
  than in water (unverified this session: `LivingEntity#travel` applies ×0.5 drag in lava against ×0.8 in water).
  Lava Vision (`alexsmobs:lava_vision`, also on the full netherite set) only changes fog: its effect tick is empty
  (`EffectLavaVision`, read 2026-10-28). The idea: lower player speed in lava further, and give Lava Vision a
  water-like speed in lava. Open questions: Forge's `SWIM_SPEED` attribute or a `travel` mixin; how it interacts
  with Fire Resistance; mobs too or players only.
