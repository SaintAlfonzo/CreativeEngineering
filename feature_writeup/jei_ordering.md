# JEI ordering

Goals for a pack-defined order of JEI's ingredient list: items/blocks, Immersive Engineering
multiblocks, fluids and effects. This file states what the system should do. How to build it on
JEI 11.8.1.1034 / KubeJS 1902 is the next step and is not covered here (see 9).

## 0. Status

| Part | Status |
|---|---|
| Goals (this file) | written 2026-10-18 |
| Implementation investigation | not started |
| Build | not started |

## 1. Why

JEI currently sorts by `MOD_NAME, INGREDIENT_TYPE, CREATIVE_MENU`
([jei-client.ini](../config/jei/jei-client.ini) `[sorting]`). That groups by mod, so all copper
tools end up spread across vanilla, Create, IE and the rest, and a player browsing the list has to
know which mod adds a thing before they can find it. The goal is a list that reads like a catalog:
everything of one kind together, in an order the pack chooses, whatever mod adds it.

## 2. Ingredient types in scope

| Type | Source in this pack | Notes |
|---|---|---|
| Items and blocks | every mod | the main job; blocks are sorted as their items |
| Multiblocks | Just Enough Immersive Multiblocks (`jeimultiblocks`) | IE multiblocks as JEI ingredients |
| Fluids | every fluid registered with JEI, plus the fluids [fluid_jei.js](../kubejs/client_scripts/fluid_jei.js) adds (all registry fluids, one `create:potion` stack per potion) | |
| Effects | JEED / MEED (`jeed`, `meed`) | mob effects as JEI ingredients |

Types are listed in this order in JEI: items/blocks, multiblocks, fluids, effects. Any other
ingredient type a mod adds goes after these, in its current order, until the pack gives it a place.

## 3. Tags drive the order

- Placement is by tag. An ingredient's tag says where it sits, e.g.
  `ce:jei/equipment/tools/copper` puts it in Equipment > Tools > Copper.
- The tag path is the hierarchy: `ce:jei/<category>/<subcategory>/<subsubcategory>`. Depth is two or
  three levels below `jei/`; a subcategory with no sub-subcategories holds items directly.
- Tags are made through `global.tags` ([tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js)),
  and item ids come from `global.item` / the item, block and fluid dicts, per [CLAUDE.md](../CLAUDE.md).
  The helper should accept a mod-wide or pattern match (e.g. every `*_copper_*` tool) as well as
  single ids, so whole families are tagged in one line.
- One placement per ingredient. An ingredient in two `ce:jei/...` leaf tags is an error the system
  reports (see 6); it is placed by the first match in the defined order.
- The same scheme covers fluids, effects and multiblocks where they can carry tags. Where a type
  can't be tagged (multiblocks are not a registry), an equivalent id list in the same structure
  stands in. The investigation decides which applies to each type.

## 4. Hierarchy and order

Every level has a predefined order, set in one place (a single ordering definition, not scattered
across scripts):

1. **Category order**: the order of top-level categories.
2. **Subcategory order** within each category.
3. **Sub-subcategory order** within each subcategory.
4. **Item order** within each leaf (5).

A category, subcategory or sub-subcategory left out of the ordering definition still shows, after
the ordered ones at its level, alphabetically, and is reported (6).

### 4.1 Starting categories (items/blocks)

Listed in the intended category order. Subcategory lists are a start, to be filled out while
tagging.

| # | Category | Subcategories (in order) |
|---|---|---|
| 1 | Equipment | tools (by tier: wood, stone, copper, iron, ...), weapons, armor, shields, misc tools (compass, clock, spyglass, ...), transportation (boats, minecarts, ...), enchantment items, ... |
| 2 | Utility blocks | vanilla workstations, enchanting, Quark, Supplementaries, storage, ... |
| 3 | Redstone | vanilla, Create, gates, Project Red |
| 4 | Engineering | Create, Immersive Engineering, PneumaticCraft, misc engineering |
| 5 | Natural | dirt/grass blocks, stone blocks, nether blocks, end blocks, plants, ... |
| 6 | Building | wood blocks, stone blocks, metal blocks, nether blocks, end blocks, ... |
| 7 | Magic items | Botania, ... |
| ... | further categories (food, materials, mob drops, decoration, ...) | added as tagging reaches them |

Fluids and effects get their own small category trees (e.g. fluids: water/lava, molten metals,
potions, fuels/oils, food fluids, ...; effects: beneficial, harmful, neutral), defined the same way.
Multiblocks can be one flat list or grouped by mod/machine family.

## 5. Sorting inside a leaf

Each leaf (the lowest level that holds ingredients) picks a sort mode in the ordering definition.
Modes:

| Mode | What it does | Example |
|---|---|---|
| **Custom list** | an explicit list of ids in order; anything in the leaf but not in the list follows, in the leaf's fallback order | copper tools: sword, pickaxe, axe, shovel, hoe, then anything else |
| **Mod order** | groups by mod in a pack-defined mod order (vanilla first, then Create, then IE, ...), each mod's group in that mod's creative-menu order | redstone/vanilla components then Create's |
| **Creative menu** | JEI's existing creative-menu order | a default for big untouched leaves |
| **Alphabetical** | by display name | effects |

- Modes combine as a primary order with a fallback: e.g. custom list first, then mod order for the
  rest. The default for a leaf with no mode is mod order, then creative menu.
- The mod order is one shared list (vanilla, Create, IE, ...) that any leaf can use, with a per-leaf
  override when a leaf needs a different one.
- A sub-subcategory can itself be ordered by mod (e.g. Redstone > vanilla / Create / gates /
  Project Red is a mod order applied at the subcategory level, not only inside leaves).

## 6. Visibility and the report

The system keeps track of every ingredient's visibility and writes a report (a dump command plus a
generated table in this folder, like the other catalogs). Each ingredient is in exactly one class:

| Class | Meaning | Source |
|---|---|---|
| **Shown, placed** | in JEI and in a `ce:jei/...` leaf | |
| **Shown, unplaced** | in JEI but in no leaf; sorted after every category, in JEI's current order | the to-do list for tagging |
| **Not shown** | registered, but never in JEI's list (no mod registers it as an ingredient; e.g. technical blocks, bucket-less fluids before fluid_jei.js) | registry minus JEI's list |
| **Hidden by default** | registered as an ingredient but hidden by the mod itself or JEI's defaults (mod blacklists, `hidden` NBT variants, ...) | |
| **Hidden by the pack** | hidden on purpose by this pack: `global.removedItems` ([jeichanges.js](../kubejs/client_scripts/jeichanges.js)), the dead fluids in fluid_jei.js, and any later pack hide | pack scripts |

The report also lists:

- ingredients placed in more than one leaf (3),
- tags or leaves that are in the ordering definition but empty,
- leaves present in tags but missing from the ordering definition (4),
- hidden or not-shown ingredients that are still tagged into a leaf (probably a mistake).

Pack hides stay in one place (`global.removedItems` and the like) so the report can tell
"hidden by the pack" apart from "hidden by default".

## 7. Not goals

- Changing what a recipe page shows, or the recipe-category order (the craftable-first sort in
  [jei_craftable_first_port.md](jei_craftable_first_port.md) stays as it is).
- Changing creative tabs. The JEI order may match them later, but that is a separate feature.
- Search behaviour. Search filters the ordered list; it doesn't reorder it.

## 8. Tests (drafted, `JO-`)

- **JO-01** With the ordering on, the first JEI page starts with Equipment > Tools in the defined
  tier order.
- **JO-02** Copper tools appear together in the custom-list order, whichever mod adds each.
- **JO-03** A leaf on mod order shows vanilla, then Create, then IE entries.
- **JO-04** An untagged item shows after every category.
- **JO-05** Item types come before multiblocks, then fluids, then effects.
- **JO-06** Fluids and effects follow their own category order.
- **JO-07** The report lists every ingredient in one visibility class, and `global.removedItems`
  entries show as hidden by the pack.
- **JO-08** An ingredient tagged into two leaves is reported and placed by the first.
- **JO-09** Adding a new leaf tag without an ordering entry still shows the items and reports the
  missing entry.
- **JO-10** Reloading (`/reload` for tags, client resource reload for JEI) applies a changed order
  without a restart, if JEI allows it; otherwise note that a restart is needed.

## 9. Next: implementation investigation

Questions for the next step:

1. How JEI 11.8.1 sorts the ingredient list (`IngredientSortStages`, `IIngredientSorter`, the
   ingredient filter) and where a custom comparator can hook in: a new sort stage via patcherMod
   mixin, a plugin API, or replacing the sorter.
2. Whether client-side code can read item tags when JEI builds its list (tags come from the server
   on join; JEI rebuilds on tag sync), and so whether the order can be data-driven at all on the
   client, or needs a client-side copy.
3. Whether tags work for fluids (yes in vanilla), effects (`mob_effect` registry tags) and
   multiblocks (no registry, so an id list).
4. How to gather the "not shown" and "hidden by default" classes: registry vs JEI's full ingredient
   list vs the visible list.
5. Where the ordering definition lives (a JSON config the Java side reads, or a KubeJS global) and
   how the tag helper should generate `ce:jei/...` tags; namespace check: `ce:` vs the pack's usual
   `creativeengineering:`.
6. Cost: JEI's list is large; the comparator must be cheap and cached.
