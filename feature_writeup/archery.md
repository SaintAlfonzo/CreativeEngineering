# Archery and bullets: fletching-table and blueprint arrow recipes, shaft tags, NEA enchantments, IE bullets

## 0. At a glance

| Part | Status |
|---|---|
| Nock Enough Arrows' own recipes removed; every arrow re-made at the fletching table (head + shaft + fin) and on an IE `archery` blueprint | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3, live in 1.0.6); no test ids before this writeup |
| Shaft tags `#forge:arrow_shaft[/wooden|light|quick|metal|heavy|blazing]` and `#forge:fletching` | DEPLOYED |
| Fletching from feathers (knife), emu feather → feathers | DEPLOYED |
| Torch arrow on Quark's item (5 recipes), NEA's torch arrow and quiver hidden; Supplementaries quiver recipe; slingshot takes leather / hemp fabric / canvas | DEPLOYED |
| `archery` and `bullet` blueprints | DEPLOYED; the archery blueprint needs the trader-only Ethereal Arrow, kept on purpose (F3) |
| NEA's Stockpile, Recovery and Cycle enchantments taken out (mixins, MoreJS filters, red tooltip on books) | DEPLOYED; the `isDiscoverable` override (F6) fixed in 1.0.9 (SRG `m_6592_`), not yet checked in game (`pack/release #4`); trade leak (F9) FIXED 2026-11-01 (`isTradeable` override, patcherMod built, not installed; `loot/tables #10`) |
| IE armor-piercing bullets from a dragon scale (64 per scale) | DEPLOYED unreachable; moved to the `bullet` blueprint (F1), BUILT 2026-10-22, not run |
| Dedicated-server join kick from NEA's fletching recipe book (`NockRecipeBookSync`) | DEPLOYED; belongs to fixes and compat (backlog row 10), noted here because it's NEA |

- **Pending:** F1, F5 and F7 built 2026-10-22, unshipped (`/reload`); built, scratch server PASSED 2026-10-22 (SQ-GM-04); in game AR-07, AR-13 (JEI, workbench screen). AR-01 to AR-18 have never been run. F6 fixed
  in 1.0.9, F3 kept, F8 moot; F2 needs your call, F4 is open.
- **Files to open:** [archery.js](../kubejs/server_scripts/equipment/archery.js) (every arrow recipe),
  [bullets.js](../kubejs/server_scripts/equipment/bullets.js),
  [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) lines 44-105 (fletching and shaft tags),
  [blueprints.js](../kubejs/server_scripts/engineering/blueprints.js) (the blueprints).
- **Knobs:** none; all recipes (`/reload`).

## 1. Overview

- **Nock Enough Arrows 1.2.0** adds about 25 special arrows, a fletching table with a head / shaft / fin recipe type
  (`nock_enough_arrows:fletching`), a quiver, a fletching recipe-book tab, and three quiver enchantments (Stockpile,
  Recovery, Cycle; category `QUIVER`, NEA's quiver only, so in this pack they can only appear on books; jar 1.2.0
  checked 2026-10-21). Four of its arrows (Cupid, Ethereal, Growing, Echoing) are sold by the wandering trader
  ([wandering_trader_catalog.md](wandering_trader_catalog.md)).
- **Others:** Quark's torch arrow, Alex's Mobs' shark tooth arrow, Cloud Storage's balloon arrow, Supplementaries'
  quiver and slingshot, vanilla's arrow and spectral arrow.
- **IE:** revolver cartridges made on the Engineer's Workbench from blueprints (`bullet` category).

The pack moves every arrow onto the fletching table with its own inputs and better yields than the crafting grid,
mirrors each one on an IE `archery` blueprint so the workbench can make them in bulk, and turns shafts into tagged
material classes (wooden, light, quick, metal, heavy) that gate the better arrows.

## 2. Reference

### 2.1 Arrows

Fletching table: head + shaft + fin → count. The IE `archery` blueprint takes the same three items for the same
count. "Any" shaft = `#forge:arrow_shaft`; fin is `#forge:fletching` unless stated.

| Arrow | Mod | Head | Shaft | Fin | Count | Other routes |
|---|---|---|---|---|---|---|
| Arrow | vanilla | flint | any | | 6 | vanilla grid recipe (4) kept |
| Spectral Arrow | vanilla | glowstone dust | an arrow | glowstone dust | 2 | vanilla grid recipe kept |
| Shark Tooth Arrow | Alex's Mobs | shark tooth | any | kelp | 10 | AM's own recipe kept |
| Torch Arrow | Quark | coal | wooden | | 4 | smelt or campfire an arrow (1); arrow + flint and steel (flint and steel kept, damaged) shapeless or by Deployer; 3 arrows + torch → 3 |
| Redstone Torch Arrow | NEA | redstone torch | any | | 2 | |
| Soul Torch Arrow | NEA | soul torch | any | | 2 | |
| Blossom Arrow | NEA | bone meal | wooden | | 3 | |
| Message Arrow | NEA | paper | any | | 2 | |
| Ink Arrow | NEA | ink sac | any | | 3 | |
| Luminous Arrow | NEA | glow ink sac | any | | 3 | |
| Slime Arrow | NEA | slimeball | any | | 3 | |
| Echoing Arrow | NEA | echo shard | light | | 2 | wandering trader |
| Whistling Arrow | NEA | goat horn | any | | 8 | |
| Repulsive Arrow | NEA | poisonous potato | any | | 2 | |
| Teleportation Arrow | NEA | ender pearl | quick | | 2 | |
| Seeker Arrow | NEA | eye of ender | light | | 3 | |
| Explosive Arrow | NEA | TNT | `#forge:arrow_shaft/blazing` (blaze rod) | | 4 | |
| Pufferfish Arrow | NEA | pufferfish | any | | 1 | |
| Prismarine Arrow | NEA | prismarine shard | metal | | 6 | |
| Party Arrow | NEA | firework rocket | any | | 3 | |
| Reinforced Arrow | NEA | obsidian / dragon scale | steel rod | | 6 / 32 | |
| Hookshot Arrow | NEA | lead | any | | 3 | |
| Paint Arrow | NEA | white wool | any | | 6 | NEA's dye recipe |
| Dousing Arrow | NEA | water bottle | metal | | 3 | |
| Drill Arrow | NEA | iron / steel drill head (IE) | heavy | | 32 / 64 | |
| Split Arrow | NEA | | | | | grid: 6 arrows + electrum or brass wire → 2; blueprint: **4** arrows + wire → 2 (F2) |
| Balloon Arrow | Cloud Storage | | | | | balloon + arrow, shapeless |
| Cupid, Ethereal, Growing | NEA | | | | | wandering trader only |

### 2.2 Shafts and fins

| Tag | Members |
|---|---|
| `#forge:arrow_shaft/wooden` | stick, treated stick |
| `#forge:arrow_shaft/light` | aluminum rod |
| `#forge:arrow_shaft/quick` | silver, electrum, gold, rose gold rods |
| `#forge:arrow_shaft/heavy` | lead, uranium rods |
| `#forge:arrow_shaft/metal` | every metal rod above, plus copper, iron, zinc, nickel, brass, constantan, steel |
| `#forge:arrow_shaft/blazing` | blaze rod (the explosive arrow's shaft) |
| `#forge:arrow_shaft` | all of the above |
| `#forge:fletching` | feather, `creativeengineering:arrow_fletching`, roadrunner feather (AM) |

The shaft class only decides which recipe accepts it; it doesn't change the arrow.

Fletching: feather on a cutting board with any knife → 2 arrow fletchings. Emu feather on a cutting board → 2
feathers. So one feather makes 12 arrows at the fletching table (4 in the crafting grid).

### 2.3 Blueprints (IE Engineer's Workbench)

| Blueprint | Made from | Category used by |
|---|---|---|
| `archery` | echoing + ethereal + reinforced arrow over 3 blue dye + 3 paper, or over an empty schematic | every arrow in 2.1 |
| `bullet` | gunpowder + empty casing + gunpowder over an empty schematic | IE's own cartridges, the dragon scale armor-piercing recipe |

Any blueprint + an empty schematic → 2 copies.

### 2.4 Bullets

[bullets.js](../kubejs/server_scripts/equipment/bullets.js) adds one recipe: 64 empty casings + 32 gunpowder + a dragon
scale → 64 armor-piercing cartridges, in blueprint category `bullet` (was `projectiles`, which nothing made; F1).
IE's own cartridge recipes are untouched.

### 2.5 Removed or hidden

- Recipes: Quark's torch arrow and all 25 NEA arrow, fletching-arrow and quiver recipes (`archery.js` lines 12-38),
  each replaced in 2.1.
- Items: `nock_enough_arrows:quiver` and `nock_enough_arrows:torch_arrow` (in `removed_items.js`; Supplementaries'
  quiver and Quark's torch arrow are used instead). Supplementaries quiver recipe: `TCT / TCT / _T_`, T = leather,
  hemp fabric or canvas, C = copper ingot.
- Enchantments: Stockpile, Recovery, Cycle.
  - patcherMod mixins make each one not discoverable, not appliable at the enchanting table and not allowed on books.
  - MoreJS filters drop them from the enchanting table's offers and from librarian book trades
    ([enchants.js](../kubejs/server_scripts/magic/enchants.js)). The trade filter only reaches vanilla's
    `EnchantBookForEmeralds`; Dynamic Villager Trades builds librarian book offers itself from every enchantment
    whose `isTradeable` is true, and the mixins don't override that, so DVT librarians can probably still sell the
    three (DVT 1.2.3 `LibrarianGenerator`, read 2026-10-21; [brief](briefs/potions_enchanting.md) #1).
  - A book that still has one shows a red bold "REMOVED ITEM: DO NOT USE" line ([jeichanges.js](../kubejs/client_scripts/jeichanges.js)).
    The JEI hide for those books was a commented-out block, deleted 2026-10-22.

## 3. What the pack changes

- **Recipes:** all through NCH helpers: `R.fletching` (a `nock_enough_arrows:fletching` JSON with arrowhead / shaft /
  fin / result), `R.ieBlueprint`, `R.fdCutting`, `R.shaped`, `R.bulk('shapeless' | 'smelting' | 'remove')`,
  `R.campfireCooking`, `R.createDeploying`, `R.replaceInput` (slingshot).
- **Tags:** plain `e.add` calls in `equipmenttags.js` (F4).
- **Recipe-book sync:** NEA creates its "fletching" `RecipeBookType` in a client-only class, so a dedicated server had
  one type fewer than the client and kicked the client on join (`DecoderException: readerIndex(18) + length(1)`).
  `NockRecipeBookSync` creates the same enum value on a dedicated server at common setup.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The dragon scale armor-piercing recipe can't be made.** FIXED 2026-10-22 (TODO `gear/archery #2`): category
  `bullet`. The finding before the fix: It uses blueprint category `projectiles`;
  no recipe in the pack makes a `{blueprint:"projectiles"}` and IE's own categories are `bullet` and `specialBullet`
  (checked 2026-10-21 in the IE 9.2.4-170 jar: IE's own armor-piercing recipe is in `bullet`). Fix: change the category to `bullet`, or add a `projectiles` blueprint
  to [blueprints.js](../kubejs/server_scripts/engineering/blueprints.js) and its duplication list.
- **F2. Split arrow costs differ.** DECIDED 2026-10-29 (owner): keep both costs (TODO `gear/archery #3`). The grid recipe takes 6 arrows + 1 wire for 2 split arrows; the
  blueprint takes 4 arrows + 1 wire for the same 2. Pick one.
- **F3. The archery blueprint needs an Ethereal Arrow.** KEPT 2026-10-22 (TODO `gear/archery #4`): intended, the
  blueprint's arrows are all trader arrows. The finding: Ethereal arrows only come from the wandering
  trader (6 emeralds), so every arrow blueprint recipe waits on a trader roll. Fine if intended;
  otherwise swap it for a craftable arrow (e.g. a seeker or teleportation arrow).
- **F4.** FIXED 2026-10-27 ([pack_hygiene.md](pack_hygiene.md) 3.3 finding 15: `equipmenttags.js` uses `global.tags`). **Hand-written tags.** The fletching and shaft tags are 44 `e.add` lines, against the `global.tags` rule
  (also [gear_tuning.md](gear_tuning.md) F3 in the same file). One `global.tags.add` call per tag would do.
- **F5. `#forge:arrow_shaft/blazing` is unused;** the explosive arrow named the blaze rod directly. FIXED 2026-10-22
  (TODO `gear/archery #5`): both explosive arrow recipes take the tag.
- **F6.** CONFIRMED and FIXED 2026-10-19: the 1.0.8 jar named the method `isDiscoverable`; 1.0.9 uses `m_6592_` ([plan_1.0.9.md](plan_1.0.9.md)). The finding before the fix: **probably only two of the three enchantment overrides applied.** The mixins add `isDiscoverable`,
  `canApplyAtEnchantingTable` and `isAllowedOnBooks` without `@Overwrite` and without a refmap. The two Forge
  methods have the same name at runtime, so they work. `isDiscoverable` is a vanilla method (SRG `m_6592_`) and the
  mixin class doesn't extend `Enchantment`, so its Mojmap name is probably not remapped and the method is never
  called. Then `enchant_randomly` loot can still roll the three enchantments, which would explain the red book
  tooltip. Checking that needs `javap` on the built jar; the fix is an SRG-named method or a refmap. The unused
  `Overwrite` imports are in [TODO](todo/TODO.md) `tooling/patchermod-cleanup #1`.
- **F7. `bullets.js` is a stub:** an empty "Removals" section, "Hello!" log lines, one recipe (F1). FIXED 2026-10-22:
  the empty section, the log lines and an unused `global.fluid` are gone; one recipe is left.
- **F8. Supplementaries' own quiver recipe isn't removed.** If it's enabled in Supplementaries' config, JEI shows two
  quiver recipes. Check in JEI (AR-15). Moot (checked 2026-10-21): `supplementaries-1.19.2-2.4.20.jar` has no crafting
  recipe for the quiver (it is loot-only there); AR-15 only confirms that JEI shows one recipe.
- **F9. The books came from trades, not loot.** FIXED 2026-11-01 (`loot/tables #10`).
  No loot table in the dumps or `kubejs/data` names Stockpile, Recovery or Cycle; the random-enchant functions
  (`enchant_randomly`, `enchant_with_levels`) honour the F6 override (present in the installed patcherMod jar). The
  `/dumptrades` dump (2026-10-30) had NEA books in two trade paths the MoreJS filter in `enchants.js` never sees:
  Dynamic Villager Trades librarians (one offer per enchantment whose `isTradeable()` is true, built from vanilla's
  `EnchantBookForEmeralds` listing) and Lightman's Currency cashiers (`enchanted_book_for_coins`, levels 1 to 4, which
  filters the registry on `Enchantment::isTradeable`, javap). Fix at the source: the three NEA mixins also override
  `isTradeable` (SRG `m_6594_`) to false. Every other book trade stays. Villagers that already rolled an NEA book keep
  that offer (saved in their NBT). Built in patcherMod (`creativeengineering-1.1.04.jar` in `build/libs`), not installed.

### Known limits

- **Vanilla grid recipes remain** for arrows and spectral arrows, deliberately worse than the fletching table.
- **Trader-only arrows** (Cupid, Ethereal, Growing) have no recipe.
- **NEA's paint-arrow dyeing** is a special recipe JEI doesn't show ([jei_coverage.md](jei_coverage.md)). Same for the
  balloon arrow, whose recipe has no ingredients for JEI to read.

### Rejected options

- **NEA's own recipes with tweaks:** replaced whole so every arrow has the same head / shaft / fin shape and a matching
  blueprint.

## 5. File map

- [server_scripts/equipment/archery.js](../kubejs/server_scripts/equipment/archery.js): removals, fletching, arrows, quiver, slingshot.
- [server_scripts/equipment/bullets.js](../kubejs/server_scripts/equipment/bullets.js): armor-piercing bullets.
- [server_scripts/engineering/blueprints.js](../kubejs/server_scripts/engineering/blueprints.js): `archery` and `bullet` blueprints, blueprint duplication.
- [server_scripts/tags/equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js): `#forge:fletching`, `#forge:arrow_shaft/*`.
- [server_scripts/magic/enchants.js](../kubejs/server_scripts/magic/enchants.js), [client_scripts/jeichanges.js](../kubejs/client_scripts/jeichanges.js): NEA enchantments.
- [startup_scripts/items/misc_items.js](../kubejs/startup_scripts/items/misc_items.js) (`creativeengineering:arrow_fletching`), [startup_scripts/_lib/removed_items.js](../kubejs/startup_scripts/_lib/removed_items.js) (NEA quiver and torch arrow).
- [client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) lines 592-618 and 673: one line per arrow.
- patcherMod: [CycleEnchantmentMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/CycleEnchantmentMixin.java), [StockpileEnchantmentMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/StockpileEnchantmentMixin.java), [RecoveryEnchantmentMixin](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/RecoveryEnchantmentMixin.java), [compat/NockRecipeBookSync.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/NockRecipeBookSync.java) (`CreativeEngineering.java` line 291).
- `NCH.fletching` (~line 888) and `NCH.ieBlueprint` (~line 1787) in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js).
- Not here: tipped arrows ([potion_buckets.md](potion_buckets.md) 2.5); the dragon bow and crossbow ([gear_tuning.md](gear_tuning.md), [armor_effects.md](armor_effects.md)).

## 6. Offline checks

None scripted. Done by hand 2026-10-18: every blueprint category used by `R.ieBlueprint` in `archery.js` and
`bullets.js` against the blueprints `blueprints.js` makes (F1 is the only miss); every NEA recipe removed has a
replacement or a hidden item.

## 7. In-game test suite

- [ ] **AR-01 Fletching:** cutting board, feather + knife → 2 arrow fletchings; emu feather → 2 feathers.
- [ ] **AR-02 Plain arrows:** fletching table, flint + stick + fletching → 6 arrows; flint + iron rod + feather → 6.
- [ ] **AR-03 Shaft gates:** echoing arrow needs an aluminum rod (a stick or iron rod is refused); teleportation needs a
  silver / electrum / gold / rose gold rod; drill arrow a lead or uranium rod; dousing a metal rod.
- [ ] **AR-04 Torch arrows:** smelt an arrow → Quark torch arrow; campfire → same; arrow + flint and steel in the grid →
  torch arrow, flint and steel stays with 1 durability less; Deployer with flint and steel on an arrow → same; 3 arrows +
  torch → 3.
- [ ] **AR-05 NEA recipes gone:** JEI on a slime arrow → only the pack's fletching and blueprint recipes; the NEA quiver
  and NEA torch arrow don't show in JEI.
- [ ] **AR-06 Big yields:** reinforced arrow from a dragon scale + steel rod + fletching → 32; drill arrow from a steel
  drill head → 64.
- [ ] **AR-07 Explosive arrow (F5):** TNT + blaze rod (any `#forge:arrow_shaft/blazing`, only the blaze rod today) +
  fletching → 4, at the fletching table and the archery blueprint; JEI shows the shaft slot as the tag. Recipes:
  scratch server PASSED 2026-10-22 (SQ-GM-04, blueprint and fletching take a blaze rod); the JEI view: client.
- [ ] **AR-08 Split arrow:** grid, 6 arrows around electrum wire → 2; brass wire works too. Workbench with the archery
  blueprint: 4 arrows + wire → 2 (F2).
- [ ] **AR-09 Balloon arrow:** grid, balloon + arrow → balloon arrow.
- [ ] **AR-10 Archery blueprint:** echoing + ethereal + reinforced arrow over an empty schematic → archery blueprint;
  blueprint + empty schematic → 2 blueprints.
- [ ] **AR-11 Workbench arrows:** Engineer's Workbench with the archery blueprint lists every arrow in 2.1; flint +
  stick + feather → 6 arrows.
- [ ] **AR-12 Bullet blueprint:** gunpowder + empty casing + gunpowder over an empty schematic → bullet blueprint; the
  workbench shows IE's cartridges.
- [ ] **AR-13 Dragon scale bullets (F1)** (recipe category and input match: scratch server PASSED 2026-10-22, SQ-GM-04, `bullet` only, 64 out; the workbench
  screen: in game): Engineer's Workbench with the bullet blueprint → the dragon scale recipe is
  listed; 64 casings + 32 gunpowder + a dragon scale → 64 armor-piercing cartridges.
- [ ] **AR-14 Enchantments gone:** enchanting table with a book at level 30, many rolls → never Stockpile, Recovery or
  Cycle; new librarians and cashiers still sell enchanted books, never one of these three (F9); an anvil refuses a book of one. `/give` an enchanted book with Cycle → red "REMOVED
  ITEM: DO NOT USE".
- [ ] **AR-15 Quiver:** JEI on the Supplementaries quiver → the pack's copper recipe; note whether Supplementaries' own
  recipe is also listed (F8).
- [ ] **AR-16 Slingshot:** the Supplementaries slingshot recipe accepts leather, hemp fabric or canvas where it had
  rabbit hide.
- [ ] **AR-17 Dedicated server join:** join the dedicated server with NEA installed → no
  `DecoderException ... readerIndex` kick; the log has `[nock-recipe-book] added server-side RecipeBookType`.
- [ ] **AR-18 Arrow tooltips:** hover a seeker, split and drill arrow → one line each ("Homes in on entities", ...).

## 8. History

- By 2026-09-27 (initial git commit): archery recipes, shaft tags, blueprints, enchantment removal, dragon scale
  bullets; shipped with 1.0.3. `NockRecipeBookSync` was first committed 2026-10-02; when it was first built isn't
  recorded.
- 2026-10-18: this writeup; findings F1 to F8; tests AR-01 to AR-18. No code changed.
- 2026-10-19: F6 confirmed with `javap` and fixed in patcherMod 1.0.9 (SRG name `m_6592_` in the three NEA mixins, [plan_1.0.9.md](plan_1.0.9.md)). Not run in game.
- 2026-10-22: F1 (`bullet` category), F5 (blazing shaft tag on the explosive arrow) and F7 (`bullets.js` cleanup)
  fixed; F3 kept (Ethereal Arrow stays in the archery blueprint); AR-07, AR-13 updated.
- 2026-10-22 (second bulk run): SQ-GM-04 PASS on the scratch server; JEI and the workbench screen stay in game.
- 2026-11-01: F9 found and fixed (`loot/tables #10`): loot already clean; the three NEA mixins now also override
  `isTradeable` (`m_6594_`), so no librarian (vanilla or DVT) or LC cashier offers them. AR-14 updated. patcherMod built,
  not installed; needs install + restart. Not run in game.
