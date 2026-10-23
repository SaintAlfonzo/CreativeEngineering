# Item physics: fire vs lava burning, floating, and what burned items leave behind

## 0. At a glance

| Part | Status |
|---|---|
| Fire/lava split on top of ItemPhysic: per item, survive fire, survive lava, both, or neither (`ItemPhysicSplit` + `ItemPhysicServerMixin`) | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3); its recurring `$1` crash (F1) was fixed in 1.0.9 |
| Float/sink control in water, additive to ItemPhysic's own list | DEPLOYED |
| Four item tags `#creativeengineering:physics/{floats,sinks,fireproof,lavaproof}`, filled in KubeJS (reloadable) with a small jar baseline | DEPLOYED |
| Burn transforms: glass → dirty glass, wood → 25% ash, TNT / TNT minecart / gunpowder explode (TNT with shrapnel) | DEPLOYED |

- **Pending:** nothing built and unshipped. IP-01 to IP-14 have never been run. Findings F1 to F6 (section 4); F1 (a crash)
  was fixed in 1.0.9, IP-13 checks it; none needs a decision except F4.
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
- **F4. Modded items mostly fall to the default (sink and burn). Your call.** The tags cover vanilla families, the
  pack's metals and end-game gear; most other mods' items (Create parts, IE materials, food) sink and burn. The file's
  "Future" list is the starting point.
- **F5. Vanilla `fireResistant` isn't consulted by the split.** An item made fireproof with `tuneTools` /
  `tuneArmor` (`fireproof: true`) survives fire only if ItemPhysic itself honours that flag; the pack also puts all
  end-game gear in `lavaproof`, so today nothing depends on it. Check with IP-10.
- **F6. Raw `event.add` inside `physicsFromTag`** rather than `global.tags.add`. Small; same rule as the other
  writeups.

### Known limits

- **ItemPhysic only:** items in a hopper, on a belt or in a container are not affected; only dropped item entities.
- **Burn transforms only fire on the kill:** an item that survives (lavaproof) never transforms.
- **Explosions** from burning TNT hurt entities but never break blocks.

### Rejected options

- **Editing ItemPhysic's two lists:** one list can't separate fire from lava, and the config can't be reloaded; tags
  can.

## 5. File map

- patcherMod: [itemphysic/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/itemphysic/) (`ItemPhysicSplit`, `ItemBurnTransform`, `BurnBlastDamage`), [mixin/ItemPhysicServerMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/ItemPhysicServerMixin.java), `data/creativeengineering/tags/items/physics/*.json` (seed).
- KubeJS: [func/item_physics.js](../kubejs/startup_scripts/func/item_physics.js) (`global.itemPhysics`), [tags/item_physics_tags.js](../kubejs/server_scripts/tags/item_physics_tags.js).
- Config: `config/itemphysic.json`.
- Related: shrapnel ([tnt_shrapnel.md](tnt_shrapnel.md)).

## 6. Offline checks

None. `/dumptags` lists the four physics tags' final members.

## 7. In-game test suite

Drop items with Q into the hazard.

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

## 8. History

- 2026-09-08 to 09-09: fire/lava split, float control, burn transforms; two crashes (a wrong `@Redirect` signature,
  fixed; the `$1` switch class, not fixed).
- By 2026-09-27 (initial git commit): tags filled; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F6; tests IP-01 to IP-14. No code changed.
- 2026-10-19: F1 fixed in patcherMod 1.0.9 ([plan_1.0.9.md](plan_1.0.9.md)). IP-13 still to run.
- 2026-10-19: F2 fixed (item_physics.js and item_physics_tags.js headers).
