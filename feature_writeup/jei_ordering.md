# JEI ordering

Goals for a pack-defined order of JEI's ingredient list: items/blocks, Immersive Engineering
multiblocks, fluids and effects. Sections 1-8 state what the system should do; 9 is what the
investigation found in JEI 11.8.1.1034 / KubeJS 1902.6.2 / JEED 2.1.4 / JEI Multiblocks 0.0.3, and
10-11 are the proposed design and build plan.

## 0. Status

| Part | Status |
|---|---|
| Goals (this file) | written 2026-10-18 |
| Implementation investigation | done 2026-10-18 (9), design and build plan proposed (10, 11) |
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
  `creativeengineering:jei/equipment/tools/copper` puts it in Equipment > Tools > Copper.
- The tag path is the hierarchy: `creativeengineering:jei/<category>/<subcategory>/<subsubcategory>`. Depth is two or
  three levels below `jei/`; a subcategory with no sub-subcategories holds items directly.
- Tags are made through `global.tags` ([tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js)),
  and item ids come from `global.item` / the item, block and fluid dicts, per [CLAUDE.md](../CLAUDE.md).
  The helper should accept a mod-wide or pattern match (e.g. every `*_copper_*` tool) as well as
  single ids, so whole families are tagged in one line.
- One placement per ingredient. An ingredient in two `creativeengineering:jei/...` leaf tags on
  different branches is an error the system reports (see 6); it is placed by the first match in the
  defined order. Parent tags along the same path are fine: the deepest tag wins (10.3).
- The same scheme covers fluids (`fluid` tags) and effects (`mob_effect` tags). Multiblocks are
  not a registry and can't be tagged, so their leaves list multiblock ids in the ordering
  definition instead (9.4).

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
| **Shown, placed** | in JEI and in a `creativeengineering:jei/...` leaf | |
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

## 9. Investigation (2026-10-18)

Read from the JEI 11.8.1.1034 sources (maven.blamejared.com `-sources` jars), and by `javap` from
the KubeJS 1902.6.2-build.73, JEED 1.19.2-2.1.4 and JEI Multiblocks 1.19.2-0.0.3 jars.

### 9.1 How JEI 11.8.1 orders the ingredient list

- `ForgeGuiPlugin.registerRuntime` calls `JeiGuiStarter.start`, which builds the base list
  (`IngredientListElementFactory.createBaseList`: every registered ingredient type in turn, every
  ingredient of that type) and then calls `IngredientSorter.sortIngredients(...)`.
- `sortIngredients` sorts the list once with the comparator built from the `IngredientSortStages`
  config (`IngredientSorterComparators`: `MOD_NAME`, `INGREDIENT_TYPE`, `ALPHABETICAL`,
  `CREATIVE_MENU`, `TAG`, `ARMOR`, `MAX_DURABILITY`), writes each element's position into
  `ListElement.setSortedIndex(i)`, and returns `COMPARE_SORT_INDEX`, a comparator on that index.
- `IngredientFilter` keeps that comparator and uses it every time it rebuilds the visible list
  (`getIngredientListUncached`: `parallelStream().filter(isVisible).sorted(comparator)`). Search
  results go through the same sort, so they follow the pack order too.
- **The stages can't express a pack order.** `TAG` sorts by each item's single most popular tag
  name alphabetically; there is no stage for a pack-supplied order and no API to add one.
  `IIngredientSorter` (with `doPreSort` / `invalidateCache`) exists in the sources but nothing
  uses it in 11.8.1.
- **Hook point:** the sorted index. Replace the sort in `IngredientSorter.sortIngredients` (one
  `@Inject` at HEAD, cancellable) and assign `sortedIndex` from the pack order. JEI's own
  `COMPARE_SORT_INDEX` then carries the pack order everywhere, with no other change to JEI.
- The created index (`ListElementInfo.create`: a running counter) is the base list's registration
  order: by ingredient type, then creative tab order for items. That is what `CREATIVE_MENU` sorts
  by and is the natural last fallback.

### 9.2 Ingredients added after the sort are not sorted today

- `JeiStarter.start` runs `registerRuntime` on every plugin (the GUI start and the sort happen
  here) and only then `onRuntimeAvailable`. KubeJS's JEI plugin (`kubejs:jei`) fires
  `JEIEvents.hideItems` / `addFluids` / ... from `onRuntimeAvailable`, so after the sort.
- An ingredient added at runtime (`IIngredientManager.addIngredientsAtRuntime`) gets a fresh
  `ListElement` whose `sortIndex` is its created index, which is larger than every base element's.
  **So every fluid [fluid_jei.js](../kubejs/client_scripts/fluid_jei.js) adds sits unsorted at the
  end of the list today**, in registry order.
- The pack order therefore has to re-sort after the runtime plugins: at the end of
  `JeiStarter.start`, and again on `IngredientFilter.onIngredientsAdded` for any later addition.
  A re-sort is a sort of precomputed keys (9.6), so it is cheap.

### 9.3 Tags on the client

- JEI starts only after it has seen both `TagsUpdatedEvent` and `RecipesUpdatedEvent`
  (`forge/startup/StartEventObserver`), and restarts when they fire again (`/reload`, rejoin). The
  server's tags are therefore synced and readable on the client when the sort runs, and a changed
  tag set re-sorts on `/reload`.
- Tags are server data, so they work the same in single player and on the dedicated server; the
  server needs no JEI code, only the tag scripts.
- JEI's own tag lookup per type (`IIngredientHelper.getTagStream`):
  - items: item tags **plus the block tags of a BlockItem's block**. The pack order should read
    item tags only (`Registry.ITEM` holders), so a block tag can't place an item by accident;
  - fluids: `Registry.FLUID` tags;
  - effects (JEED `EffectInstanceHelper`): `Registry.MOB_EFFECT` tags;
  - multiblocks (JEI Multiblocks `MultiblockIngredientHelper`): no tags (default empty stream).
- KubeJS 1902 has `RegistryInfo.MOB_EFFECT`, so `ServerEvents.tags('mob_effect', ...)` works, and
  [tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js) is already registry-agnostic
  (the pack uses it on `worldgen/biome` and `worldgen/structure`).
- **Not verified:** that 1.19.2 syncs `mob_effect` tags to the client (it syncs the static
  registries' tags as far as I know, but check in game: JO-11). Fallback if not: effect leaves
  list ids in the definition, like multiblocks.
- **Not relied on:** tag entry order. KubeJS keeps a tag's entries as an ordered list and vanilla
  probably preserves it through the sync, but a member reached through a nested `#tag` or a
  regex lands wherever that expansion puts it. Custom-list order lives in the definition (10.2).

### 9.4 The four ingredient types

| Type | JEI ingredient class | Universe (for the report) | Placement by | Id |
|---|---|---|---|---|
| Items/blocks | `ItemStack` (JEI's vanilla plugin) | `Registry.ITEM` | item tags | item id; NBT variants (enchanted books, potions, tipped arrows) share their item's placement and keep creative order among themselves |
| Fluids | `FluidStack` (Forge) | `Registry.FLUID` (still forms) | fluid tags | fluid id; `create:potion` stacks share one placement |
| Effects | `MobEffectInstance` (JEED, `jeed`) | `Registry.MOB_EFFECT` | `mob_effect` tags | effect id |
| Multiblocks | IE `MultiblockHandler.IMultiblock` (`jeimultiblocks`) | `MultiblockHandler.getMultiblocks()` | id lists in the definition | `IMultiblock.getUniqueName()`, e.g. `immersiveengineering:multiblocks/crusher`; Immersive Petroleum's multiblocks are IE multiblocks too |

- JEED builds its effect list from the registry minus `hidden_effects` in
  [jeed-client.toml](../configs_edited/root_config/jeed-client.toml) (currently
  `buzzier_bees:sunny`): those are never registered with JEI. That config is pack-shipped, so they
  count as "hidden by the pack".
- MEED is only description data for JEED; it adds no ingredient type.
- Other ingredient types may exist (the report lists every registered type); they sort after the
  four above, in JEI's order, until the definition names them.

### 9.5 Visibility: where each class comes from

- **Items in no creative tab are never in JEI.** JEI's item list is the creative tabs' contents
  (`ItemStackListFactory`, deduplicated by subtype uid) plus anything a plugin registers. Registry
  minus base list = "not shown".
- **Runtime hides remove the ingredient.** `removeIngredientsAtRuntime` drops it from the
  ingredient manager and the filter marks its element invisible. KubeJS's `JEIEvents.hide*`
  ([jeichanges.js](../kubejs/client_scripts/jeichanges.js) with `global.removedItems`) goes this
  way, and so would any mod hiding at runtime. To tell the pack's hides from a mod's, record which
  plugin is running: `PluginCaller.callOnPlugins` calls each plugin in turn, so a mixin around its
  `func.accept(plugin)` can set a thread-local plugin uid, and a mixin on
  `IngredientManager.addIngredientsAtRuntime` / `removeIngredientsAtRuntime` records the uid with
  each change. `kubejs:jei` = the pack.
- **Edit-mode hides** (`config/jei/blacklist.cfg`, `IngredientVisibility` via `EditModeConfig`)
  are the player's own. The pack ships none (only `jei-client.ini` in
  `configs_edited/root_config/jei/`); report them as a separate "hidden by the player" line.
- **Never-added on purpose:** the dead fluids in fluid_jei.js are skipped, not hidden, so they'd
  read as "not shown". They need to be in the pack-hidden list the report reads (10.5).
- So the classes in 6 come from: registry (universe), base list after plugin registration
  (registered), runtime adds/removes with plugin uid (pack vs mod), JEED config and the pack's
  never-added list (pack), edit-mode config (player), and the final visible flag.

### 9.6 Cost

- The base list is tens of thousands of elements. Placement lookup per element is one map lookup
  (item/fluid/effect -> leaf, built once per JEI start by walking the `creativeengineering:jei/*`
  tags), and the sort key is a few ints, computed once per element and kept. The sort is then
  `n log n` integer compares, same order of cost as JEI's own `MOD_NAME` stage (which compares
  strings).
- `IngredientFilter` sorts with a `parallelStream`, so the comparator must be thread-safe: reading
  the precomputed `sortedIndex` (what JEI already does) is.

## 10. Proposed design

### 10.1 Where the pieces live

| Piece | Where | Runs on |
|---|---|---|
| Ordering definition: category tree, per-level order, leaf sort modes, custom lists, mod order, multiblock leaves, pack-hidden lists | one KubeJS startup file, e.g. `kubejs/startup_scripts/jei/order.js`, as `global.jeiOrder`, built through a new helper in `startup_scripts/func/` (like `global.tags`); item ids from `global.item` and the dicts | both sides (startup scripts load on client and server) |
| `creativeengineering:jei/...` tags | one server script, e.g. `server_scripts/tags/jei_order.js`, that walks `global.jeiOrder` and makes the tags through `global.tags.add` / `addMatching` (`@mod`, `#tag`, globs) for `item`, `fluid`, `mob_effect` | server |
| Hand-off to Java | a client script calls `Java.loadClass('com.SaintAlfonzo.creativeengineering.compat.jei.PackIngredientOrder').define(JSON.stringify(global.jeiOrder))`, the same pattern as the worldgen dumps' `WorldgenFiles` | client |
| Sort, re-sort, attribution, report | patcherMod `compat/jei/PackIngredientOrder` + client mixins on JEI | client |
| Switches | `CEConfig` `[jei]`: `packOrder` (on: pack order; off: JEI's `IngredientSortStages` as now), `orderReport` (write the report at each JEI start) | client |

One definition feeds both sides: the server script makes the tags from it, and the client sorts
with it, so the category order and the tag paths can't drift apart.

### 10.2 Definition shape (sketch)

```js
global.jeiOrder = {
  types: ['item', 'multiblock', 'fluid', 'effect'],
  modOrder: ['minecraft', 'create', 'immersiveengineering', 'pneumaticcraft', /* ... */],
  item: [
    { id: 'equipment', children: [
      { id: 'tools', children: [
        { id: 'copper', sort: ['list', 'mod', 'creative'],
          from: ['minecraft:*copper*', '@create'],           // tag members, via global.tags.addMatching
          list: [global.item.copperSword, /* ... */] },       // custom order inside the leaf
      ]},
    ]},
  ],
  fluid: [ /* same shape */ ],
  effect: [ /* same shape */ ],
  multiblock: [ { id: 'ie', list: ['immersiveengineering:multiblocks/coke_oven', /* ... */] } ],
  packHidden: { fluid: [/* the dead fluids */] },
}
```

- Order of `children` = the predefined order at that level. `sort` is the leaf's mode chain (5);
  default `['mod', 'creative']`.
- `from` is optional: members can also come from any other script tagging
  `creativeengineering:jei/<path>` directly through `global.tags`.
- A node with `sort` but also `children` sorts its own direct members before its children.

### 10.3 Placement rules

- An ingredient's placement is its **deepest** `creativeengineering:jei/...` tag, so a script that
  adds the whole chain (`global.tags.tagChain`) still places by the leaf.
- Two tags on different branches = conflict: placed by the first in definition order, reported.
- A tag path missing from the definition still places its members: after its defined siblings,
  alphabetically by path segment, and reported.
- Untagged ingredients: after every category of their type, in JEI's creative order (created
  index).

### 10.4 Sort key

Per element, computed once: `(type rank, rank at each level of the path, leaf key, created index)`
where the leaf key comes from the leaf's mode chain: list position (unlisted after listed), mod
rank from `modOrder` (unlisted mods after, by mod name), lower-case display name, or created index.
Sort the whole list by key, write positions into `sortedIndex`, done; re-run after
`onRuntimeAvailable` and on later runtime additions (9.2).

### 10.5 Report

- At each JEI start (when `orderReport` is on) write `kubejs/exported/jei_order.json`: every
  ingredient of the four types (registry universe plus everything JEI saw), with type, id, mod,
  placement path, final position, visibility class (6) and, for runtime changes, the plugin uid.
  Also the conflicts, undefined paths, empty leaves and hidden-but-tagged entries.
- `tools/jei_order_report.py` turns that into `feature_writeup/jei_order_catalog.md` (by
  category, then a "not placed" list by mod, then the hidden classes), like the other generated
  catalogs.
- The pack-hidden list the report trusts: `global.removedItems` (seen as `kubejs:jei` runtime
  removals), `global.jeiOrder.packHidden` (never-added on purpose; fluid_jei.js reads its
  `DEAD_FLUIDS` from here instead of its own constant), and JEED's `hidden_effects`.

### 10.6 Mixins (all client, `remap = false`, JEI 11.8.1)

| Target | Kind | Why |
|---|---|---|
| `mezz.jei.gui.ingredients.IngredientSorter.sortIngredients` | `@Inject` HEAD, cancellable | pack sort of the base list when `packOrder` is on |
| `mezz.jei.library.startup.JeiStarter.start` | `@Inject` TAIL | re-sort after `onRuntimeAvailable` (KubeJS adds/hides); write the report |
| `mezz.jei.gui.ingredients.IngredientFilter.onIngredientsAdded` | `@Inject` TAIL | re-sort later runtime additions |
| `IngredientFilter.elementSearch` | `@Accessor` | the full element list for the re-sort |
| `mezz.jei.library.load.PluginCaller.callOnPlugins` | `@WrapOperation` / `@Redirect` on `Consumer.accept` | current plugin uid for attribution |
| `mezz.jei.library.ingredients.IngredientManager.add/removeIngredientsAtRuntime` | `@Inject` HEAD | record runtime adds/removes with the plugin uid |

The JEI sorter keeps working unchanged when `packOrder` is off, so the feature can ship disabled.

## 11. Build plan

1. **patcherMod core**: `PackIngredientOrder` (parse the definition JSON, build placement maps
   from tags, keys, sort), the mixins in 10.6, `CEConfig` `[jei] packOrder` / `orderReport`.
   Check the mixins boot clean in single player with `packOrder` off, then on with an empty
   definition (should look like creative order by type).
2. **KubeJS plumbing**: the `global.jeiOrder` helper in `startup_scripts/func/`, the definition
   file with a first small tree (Equipment > Tools > copper/iron, one fluid leaf, one effect leaf,
   the IE multiblocks), the server tag script, the client hand-off script; move fluid_jei.js's
   dead fluids into `packHidden`.
3. **Report**: the JSON dump and `tools/jei_order_report.py`; run once to get the full "not placed"
   list per mod, which becomes the tagging to-do list.
4. **Fill the tree** category by category (4.1), checking the catalog after each.
5. **Tests** JO-01 to JO-11 (8, plus JO-11 below), then stage for a release.

Additional test from the investigation:

- **JO-11** An effect tagged `creativeengineering:jei/...` through `ServerEvents.tags('mob_effect')`
  is placed on the client (proves `mob_effect` tags sync; if not, effect leaves move to id lists).
- **JO-12** The fluids fluid_jei.js adds are sorted with the other fluids, not appended at the end.

Open points for the build:

- Whether the definition should also accept item ids with NBT (one enchanted book level placed
  separately). Not needed for the goals; NBT variants follow their item.
- Reload: a tag change applies on `/reload` (JEI restarts). A definition change needs the startup
  script reloaded (`/kubejs reload startup_scripts`, then the client hand-off re-run with
  `/kubejs reload client_scripts`) and a JEI restart (`/reload`); check this in game, else it is a
  game restart.
