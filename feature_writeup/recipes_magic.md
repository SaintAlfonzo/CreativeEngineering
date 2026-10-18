# Magic recipes: XP machines, sculk growth, ender storage, compasses

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/magic/`
`xp.js`, `sculk.js`, `storage.js` and `progression.js`. The rest of the folder has writeups: `armor_effects.js` and
`tool_effects.js` in [armor_effects.md](armor_effects.md); `dragon_breathing.js` in [engineering.md](engineering.md)
2.2; `lava_vision.js` in [potions.md](potions.md); `enchants.js` (Nock Enough Arrows enchantments kept off tables and
trades) in [archery.md](archery.md); the Flint and Terrasteel recipe in [nether_portal.md](nether_portal.md).

## 0. At a glance

| Part | Status |
|---|---|
| Create Enchantment Industry: disenchanter, experience rotor and printer rebuilt around Create's experience block; Compressed Creativity memory essence mixing removed | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Sculk doubled by mixing with liquid XP (50 mB per 2) or hyper XP (5 mB per 4) | DEPLOYED |
| Ender chests: vanilla's needs a Botania third eye; Cloud Storage's on an ender chest; Ender Storage's three accept any leather, ender pearl, obsidian, chest tag and hemp fabric | DEPLOYED |
| Compasses: Nature's Compass needs a terrasteel ingot; Explorer's Compass recipe removed | DEPLOYED; Explorer's Compass uncraftable (F1) |

- **Pending:** nothing built and unshipped. RA-01 to RA-08 have never been run. Findings F1 to F4 (section 4); F1 needs
  your call.
- **Files to open:** [xp.js](../kubejs/server_scripts/magic/xp.js), [storage.js](../kubejs/server_scripts/magic/storage.js),
  [progression.js](../kubejs/server_scripts/magic/progression.js).
- **Knobs:** none. Server scripts (`/reload`).

## 1. Overview

The "magic" side of the pack is mostly Botania, Create Enchantment Industry and the End. Its recipes put the strongest
utility items (ender storage, compasses, XP machines) behind magic materials: a Botania eye for ender chests, terrasteel
for the biome compass, Create's experience block for the XP machines.

## 2. Reference

### 2.1 XP (`xp.js`, `sculk.js`)

| Item | Pack recipe |
|---|---|
| Disenchanter | sandpaper, item drain, 3 experience blocks |
| Experience rotor | propeller in 4 experience blocks |
| Printer | fluid tank, dried kelp, 2 iron plates, experience block |
| Memory essence (Compressed Creativity) | its mixing recipe removed |
| Sculk ×4 | unheated mixing: 2 sculk + 50 mB liquid XP |
| Sculk ×8 | unheated mixing: 4 sculk + 5 mB hyper experience |

### 2.2 Storage (`storage.js`)

- **Vanilla ender chest:** a Botania third eye instead of the eye of ender; the third eye takes an ender pearl instead of
  an eye of ender.
- **Cloud Storage:** cloud chest on an ender chest instead of a wooden chest; static cloud chest on a cloud chest.
- **Ender Storage** (its own `enderstorage:create_recipe` type, which sets the frequency): pouch (4 blaze powder, 3
  leather, ender pearl, wool or hemp fabric), chest (4 blaze rods, 2 obsidian, wooden chest, ender pearl, wool or hemp
  fabric), tank (the same with a cauldron). The pack's versions take any `#forge:leather`, `#forge:ender_pearls`, `#forge:obsidian` and
  `#forge:chests/wooden`, and hemp fabric as well as white wool.

### 2.3 Compasses (`progression.js`)

- Nature's Compass: a terrasteel ingot instead of the logs.
- Explorer's Compass: recipe removed, nothing replaces it (F1).

## 3. What the pack changes

`R.replaceInput`, `R.remove`, `R.shaped`, `R.createMixing` through NCH; the three Ender Storage recipes through raw
`e.remove` / `e.custom` (F2).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Explorer's Compass can't be crafted. Your call.** `progression.js` removes its recipe and adds none; the old
  `global.CE` block in `misc_mods.js` did the same. If it's meant as loot only, say so in its tooltip; otherwise give it a
  late recipe (next to Nature's Compass). Check with RA-07.
- **F2. Raw recipe code:** `storage.js` writes the Ender Storage recipes with `e.remove` and `e.custom`, and reads tag
  names through `global.tags._clean`. An NCH helper for `enderstorage:create_recipe` would fit the rule.
- **F3. Dict duplicates:** `item_dict.js` defines `i.xp_block` and `i.any_sandpaper` three times each, `i.disenchanter`
  twice, `i.explorers_compass` and `i.explore_compass` for one item (same values).
- **F4. Sculk loop:** 2 sculk + 50 mB XP → 4 sculk, and sculk is what echo shards are made of
  ([recipes_engineering.md](recipes_engineering.md) 2.7), so echo shards cost XP rather than Deep Dark trips. Intended.

### Known limits

- **Ender Storage recolouring** stays the mod's own recipes (JEI shows a stand-in, [jei_coverage.md](jei_coverage.md)).

### Rejected options

None recorded.

## 5. File map

- [server_scripts/magic/](../kubejs/server_scripts/magic/): `xp.js`, `sculk.js`, `storage.js`, `progression.js` (this
  writeup); `armor_effects.js`, `tool_effects.js`, `dragon_breathing.js`, `lava_vision.js`, `enchants.js` (other
  writeups, above).

## 6. Offline checks

None.

## 7. In-game test suite

- [ ] **RA-01 Disenchanter:** JEI → sandpaper, item drain, 3 experience blocks; craft it.
- [ ] **RA-02 Printer and rotor:** JEI each → the recipes in 2.1.
- [ ] **RA-03 Sculk:** unheated mixer, 2 sculk + 50 mB liquid XP → 4 sculk.
- [ ] **RA-04 Ender chest:** JEI ender chest → Botania third eye; third eye → ender pearl.
- [ ] **RA-05 Ender Storage:** craft an ender pouch with hemp fabric and modded leather → works, frequency set.
- [ ] **RA-06 Cloud chest:** JEI cloud chest → ender chest in the centre.
- [ ] **RA-07 Explorer's Compass (F1):** JEI → note any recipe.
- [ ] **RA-08 Nature's Compass:** JEI → terrasteel ingot.

## 8. History

- By 2026-09-27 (initial git commit): the folder; shipped with 1.0.3.
- 2026-10-18: this writeup; findings F1 to F4; tests RA-01 to RA-08. No code changed.
