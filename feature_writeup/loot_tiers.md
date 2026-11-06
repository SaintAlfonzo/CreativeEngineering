# Loot tuning system: categories, sets and family tables

Design notes for TODO `loot/tables #1` (comprehensive loot table system). Written 2026-11-01 from the owner's notes.
Design only: nothing is built. Nether and End loot are out of scope for now (section 6).

Related: [loot.md](loot.md) (framework, 3.8 rules, 7 draft table contents), [loot_islands.md](loot_islands.md)
(172 tables used by one or two structures), [loot_catalog/](loot_catalog/README.md) (every container table as shipped),
[briefs/loot_tables.md](briefs/loot_tables.md). Related TODO rows: `loot/tables #4` (crates and sacks), `#5` (cans),
`#6` (MCA items), `#11` (enchanted book tiers), `#12` (azalea plants in lush loot), `#13` (Rustic seeds), `#14`
(seed, crate and raw food loot by biome).

## 1. Idea

Every loot table behind an in-world chest becomes a member of a three-level tree:

1. **Category** (large): village, underground, nether, end, dungeon, pillager, underwater, misc.
2. **Set** (specific): the kind of place inside the category, e.g. blacksmith, shepherd, other professions, mineshaft,
   lush, bastion, nether fortress, end ship, jungle temple, pyramid.
3. **Table** (the contents, usually a grade): e.g. `underground/mineshaft/common`.

A table does not list items itself. It **pulls from several predefined family tables** (section 3), each family
being one material type. Tuning a family (adding an item, banning one, changing a weight) changes every table that
draws from it; tuning a table changes its own mix of families (how many rolls, which families, at what weight).

This replaces the per-table item lists of the draft in [loot.md](loot.md) 7 ("Basic loot table": mob loot, crafting
material, minable, tools, food ...). Those draft groups map onto families below.

## 2. Categories and sets

The set list is open: the owner's examples are given, the rest comes from the census. Existing tables are mapped by
structure with [loot_catalog/by_structure.md](loot_catalog/by_structure.md); the island list
([loot_islands.md](loot_islands.md)) shows which tables fold into a set first.

| Category | Sets (examples) | Notes |
|---|---|---|
| village | blacksmith, shepherd, other professions | one set per profession house, plus generic houses |
| underground | mineshaft, lush, ... | cave and mine content; mineshaft `common` is the worked example |
| dungeon | | monster rooms and similar enclosed chests |
| pillager | | outposts, camps, mansion, ships |
| underwater | | shipwrecks, ruins, monuments |
| misc | | anything that fits no other category |
| nether | bastion, nether fortress | **not tuned yet** |
| end | end ship | **not tuned yet** |

Also named by the owner, category not stated (to place): jungle temple, pyramid (desert temple). Likely dungeon or
misc; decide when the sets are listed.

Naming: tables are addressed `category/set/grade`, e.g. `underground/mineshaft/common`. Grades beyond `common` and
`rare` are not fixed. Implementation of the tree (tag tree vs. a table in `loot_config.json`) is open, see section 5.

## 3. Family tables

A family is a reusable pool of one material type. Tables compose families.

### 3.1 Underground and general materials

| Family | Contents (owner notes) |
|---|---|
| rails | rails |
| arrows | arrows |
| overworld mining, common | common mining drops |
| overworld mining, rare | rare mining drops |
| underground mob loot | cave mob drops |
| lush loot | lush cave items (ties to `loot/tables #12` azalea plants) |
| common equipment | basic tools and armor |
| rare equipment | better tools and armor |
| underground food | food found in caves and mines |
| common book loot | books and paper, plain |
| rare book loot | rarer books |
| enchanted books | **separate family, not part of book loot**; tiers per `loot/tables #11` |
| mushrooms, common | |
| mushrooms, rare | |
| spider loot | string, spider eyes and similar |
| surface mob loot | surface mob drops |

The draft in [loot.md](loot.md) 7 already lists contents for several of these (mob loot, minable, tools, building,
rare random); fold them in rather than restate them.

### 3.2 Surface general food

One family, with these members:

- bread
- potato, potato crate, baked potato
- carrot, carrot crate
- onion, onion crate
- cabbage, cabbage crate, sliced cabbage
- wheat, wheat bale
- beetroot, mushroom soup
- suspicious stew from dandelion and poppy
- cod, cooked cod
- simple salads

### 3.3 Biome foods

Family whose contents depend on the biome the chest generates in (needs the biome loot condition from the fishing
net work, `creativeengineering:biome_tag`, or LootJS biome scoping in `30_redistribute.js`):

- suspicious stew by the flowers that grow in the biome
- fruit and vegetable items and their crates, by biome
- dandelion food in biomes with flowers

Covers `loot/tables #14` (seed, crate and raw food loot by biome).

### 3.4 Fishing

| Family | Contents |
|---|---|
| fishing, common | |
| fishing, rare | |

## 4. Example composition

`underground/mineshaft/common` draws from: rails, arrows, overworld mining common, underground mob loot, common
equipment, underground food, common book loot, mushrooms common. `underground/mineshaft/rare` swaps in or adds
overworld mining rare, rare equipment, rare book loot, enchanted books, mushrooms rare, spider loot. This is an
illustration of the mechanism, not a decided list: the real mixes are set per table once the families exist.

## 5. Open questions

- How the tree is stored: item/loot tag tree (`container/village/leatherworker/...` as in TODO `loot/tables #1`), or
  `category/set/grade` data in `loot_config.json` consumed by `05_tuning.js` (`global.lootTuning`). Families need a
  place to live and be validated either way.
- How a table "pulls from" a family at runtime: nested loot table reference, or the pre-roll pass in `05_tuning.js`
  replacing a table's pools with family draws. Chests that other mods inject into must keep working (loot.md 2.5).
- Biome-dependent families (3.3) versus the static pre-roll: which mechanism picks per biome.
- Grade names and count: only `common` and `rare` are implied so far.
- Where jungle temple and pyramid sit.
- Chests that stay hand-tuned (hand-placed containers, [loot_catalog/handplaced.md](loot_catalog/handplaced.md)).

## 6. Out of scope for now

Nether (bastion, nether fortress) and End (end ship) loot tuning is left out until the Overworld tree works. The two
categories stay in the tree so their tables are not mapped elsewhere by mistake.

## 7. Next steps

1. Regenerate the loot index and catalog (step 0 in [briefs/loot_tables.md](briefs/loot_tables.md)).
2. List sets per category from `by_structure.md`, folding island tables.
3. Write the family contents (3.1 to 3.4), using the loot.md 7 draft as a start.
4. Decide the storage and runtime questions in section 5.
5. Build, then add `LT-` tests to the loot.md suite.

## 8. History

- 2026-11-01: written from the owner's notes. Design only.
