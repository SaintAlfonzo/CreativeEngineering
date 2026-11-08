# JEI ordering

Goals for a pack-defined order of JEI's ingredient list: items/blocks, Immersive Engineering
multiblocks, fluids and effects. Sections 1-8 state what the system should do; 9 is what the
investigation found in JEI 11.8.1.1034 / KubeJS 1902.6.2 / JEED 2.1.4 / JEI Multiblocks 0.0.3,
10-11 are the design and build plan, and 12 is what was built.

## 0. Status

| Part | Status |
|---|---|
| Goals (this file) | written 2026-10-18 |
| Implementation investigation | done 2026-10-18 (9), design and build plan proposed (10, 11) |
| Build | steps 1-3 BUILT 2026-10-18 and DEPLOYED with 1.0.7 (patcherMod 1.0.7, KubeJS, first small tree, report tool; 12); not run in game, JO-01 to JO-12 pending; step 4 (fill the tree) not started |
| Moved to Creative Compat | 2026-11-07: helper, client handoff and the tags (`creativecompat:jei/...`) are Compat's; JEI-guarded (creative_engineering.md 7.3, SQ-CCB-01, CQ-CCB-02) |
| Equipment family | BUILT 2026-10-26 (KubeJS only; 12.5): replaces the first small tree's Equipment > Tools; JO-13 to JO-16 pending |
| Bug: order never reached patcherMod | FIXED 2026-10-20: `client_scripts/jei_order.js` threw "redeclaration of var $PackIngredientOrder" on every client load (top-level `const` inside `try`, a known quirk of this Rhino, reproduced in the pack's Rhino jar), so `define()` never ran and JEI kept its own order. Now `Java.loadClass(...).define(...)` with no binding; a scratch client logs `[jei order] definition: 4 types, 10 nodes` |

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
  `creativecompat:jei/equipment/tools/copper` puts it in Equipment > Tools > Copper.
- The tag path is the hierarchy: `creativecompat:jei/<category>/<subcategory>/<subsubcategory>`. Depth is two or
  three levels below `jei/`; a subcategory with no sub-subcategories holds items directly.
- Tags are made through `global.tags` ([tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js)),
  and item ids come from `global.item` / the item, block and fluid dicts, per [CLAUDE.md](../CLAUDE.md).
  The helper should accept a mod-wide or pattern match (e.g. every `*_copper_*` tool) as well as
  single ids, so whole families are tagged in one line.
- One placement per ingredient. An ingredient in two `creativecompat:jei/...` leaf tags on
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
| **Shown, placed** | in JEI and in a `creativecompat:jei/...` leaf | |
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
  (item/fluid/effect -> leaf, built once per JEI start by walking the `creativecompat:jei/*`
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
| `creativecompat:jei/...` tags | one server script, e.g. `server_scripts/tags/jei_order.js`, that walks `global.jeiOrder` and makes the tags through `global.tags.add` / `addMatching` (`@mod`, `#tag`, globs) for `item`, `fluid`, `mob_effect` | server |
| Hand-off to Java | a client script calls `Java.loadClass('com.SaintAlfonzo.creativeengineering.compat.jei.PackIngredientOrder').define(JSON.stringify(global.jeiOrder))`, the same pattern as the worldgen dumps' `WorldgenFiles` | client |
| Sort, re-sort, attribution, report | Creative Compat `compat/jei/PackIngredientOrder` + client mixins on JEI | client |
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
  `creativecompat:jei/<path>` directly through `global.tags`.
- A node with `sort` but also `children` sorts its own direct members before its children.

### 10.3 Placement rules

- An ingredient's placement is its **deepest** `creativecompat:jei/...` tag, so a script that
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
| `mezz.jei.gui.ingredients.IngredientFilter.<init>` | `@Inject` RETURN | remember the list and JEI's own order of it (the fallback) |
| `mezz.jei.library.startup.JeiStarter.start` | `@Inject` HEAD / RETURN | reset per-start state; pack sort after `onRuntimeAvailable` (KubeJS adds/hides); write the report |
| `JeiStarter.stop` | `@Inject` HEAD | drop the old list |
| `mezz.jei.gui.ingredients.IngredientFilter.onIngredientsAdded` | `@Inject` TAIL | re-sort later runtime additions |
| `IngredientFilter.elementSearch`, `ingredientManager`, `notifyListenersOfChange` | `@Accessor` / `@Invoker` | the full element list for the re-sort, and refreshing an open list |
| `mezz.jei.library.load.PluginCaller.callOnPlugins` | `@Redirect` on `Consumer.accept` (no MixinExtras in patcherMod) | current plugin uid for attribution |
| `mezz.jei.library.ingredients.IngredientManager.add/removeIngredientsAtRuntime` | `@Inject` HEAD | record runtime adds/removes with the plugin uid |

As built, JEI's own sort is left alone (no `IngredientSorter` hook): it runs as before, its result is kept as
the fallback order, and the pack sort rewrites the indices once JEI has finished starting. With `packOrder`
off nothing is re-sorted, so the feature can ship disabled.

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

- **JO-11** An effect tagged `creativecompat:jei/...` through `ServerEvents.tags('mob_effect')`
  is placed on the client (proves `mob_effect` tags sync; if not, effect leaves move to id lists).
- **JO-12** The fluids fluid_jei.js adds are sorted with the other fluids, not appended at the end.

Equipment family (12.5):

- **JO-13** The item list opens with Equipment: wood, leather, stone, copper, ... in the 12.5 family order; inside a
  family the tools come first (sword, pickaxe, axe, shovel, hoe, knife, machete), then armor (helmet to boots, then
  goggle helmet, diving helmet, backtank, diving boots), then the family's shield, shears, horse armor, fishing rod
  and backpack.
- **JO-14** After the families: misc vanilla (bow, crossbow, trident, ...), misc modded, knives (the leftover
  Delightful knives by name, none of the six placed in a family), arrows (Nock Enough Arrows included), guns (Old
  Guns first, then IE, then PneumaticCraft), then tech, magic and hats (every Simple Hats item) last.
- **JO-15** `jei_order_catalog.md` section 2 lists no conflicts and no unknown list ids for `equipment/...`.
- **JO-16** None of the mechanisms or repair parts of Old Guns sit under `equipment/misc/guns` (`except`).

Open points for the build:

- Whether the definition should also accept item ids with NBT (one enchanted book level placed
  separately). Not needed for the goals; NBT variants follow their item.
- Reload: a tag change applies on `/reload` (JEI restarts). A definition change needs the startup
  script reloaded (`/kubejs reload startup_scripts`, then the client hand-off re-run with
  `/kubejs reload client_scripts`) and a JEI restart (`/reload`); check this in game, else it is a
  game restart.

## 12. Build (2026-10-18)

Steps 1-3 of 11. Compiled in isolation against Minecraft 1.19.2 / Forge 43.3.9 (official mappings) and the JEI
11.8.1.1034 jar; the full patcherMod jar was not built here (its third-party `libs/` jars are not in this
checkout). The definition was run through Node with the real dicts and helper, and its JSON through the Java
parser and sort key in a harness (order below came out as designed). **Nothing has run in game yet.**

### 12.1 Files

| File | What |
|---|---|
| [PackIngredientOrder.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/PackIngredientOrder.java) | definition parse, placements from tags + lists, sort key, re-sort |
| [PackOrderReport.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/PackOrderReport.java) | `kubejs/exported/jei_order.json` |
| [JeiIngredientChanges.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/compat/jei/JeiIngredientChanges.java) | runtime adds/removes with the plugin that made them |
| `mixin/JeiIngredientFilterAccessor`, `JeiIngredientFilterMixin`, `JeiStarterMixin`, `JeiPluginCallerMixin`, `JeiIngredientManagerMixin` | client mixins (10.6) |
| [CompatConfig.java](../development/creativeCompat/src/main/java/com/SaintAlfonzo/creativecompat/CompatConfig.java) `[jei]` | `packOrder` (default on), `orderReport` (default off) |
| [func/jei_order.js](../development/creativeCompat/src/main/resources/kubejs_startup/creativecompat/func/jei_order.js) (Creative Compat jar) | `global.jeiOrder`: `define`, `tag`, `walk`, `toJson`, `packHidden`; checks node ids, sort modes, list entries |
| [jei/order.js](../kubejs/startup_scripts/jei/order.js) | the definition (first small tree, 12.2) |
| [_lib/effect_dict.js](../kubejs/startup_scripts/_lib/effect_dict.js), [_lib/multiblock_dict.js](../kubejs/startup_scripts/_lib/multiblock_dict.js) | new dicts: `global.effect` (vanilla effects), `global.multiblock` (all 21 IE multiblocks, ids read from the IE 9.2.4 jar; the arc furnace is `multiblocks/arcfurnace`) |
| [tags/jei_order_tags.js](../kubejs/server_scripts/tags/jei_order_tags.js) | `creativecompat:jei/...` item, fluid and mob_effect tags from the definition |
| [jei_order.js](../kubejs/assets/creativecompat/kubejs/jei_order.js) (Compat client script, in the jar) | hands the definition to Java; skipped without JEI |
| [_lib/fluid_jei.js](../kubejs/assets/creativecommon/kubejs/_lib/fluid_jei.js) (Common client script) | dead fluids from `packHidden` (none without Compat); skipped without JEI |
| [tools/jei_order_report.py](../tools/jei_order_report.py) | report -> `feature_writeup/jei_order_catalog.md` (`--check` exits 1 on definition problems) |

### 12.2 First tree

- **item**: Equipment > Tools > copper (Everything Copper sword/pickaxe/axe/shovel/hoe, Create Things and Misc and
  Delightful copper knives), iron (vanilla iron tools, Farmer's Delight iron knife). Sort `list, mod, creative`.
- **multiblock**: immersiveengineering > stone (coke oven, alloy smelter, blast furnace, improved), machines,
  power_storage. Placed by `list` (no tags).
- **fluid**: vanilla (water, lava, milk).
- **effect**: beneficial (19 vanilla effects).
- Type order item, multiblock, fluid, effect; everything else unplaced, in JEI's own order after the placed
  entries of its type.

### 12.3 Behaviour as built

- An ingredient's place: its deepest `creativecompat:jei/...` tag (item tags only, not a block's tags);
  else the first node whose `list` names it. Tag on two branches, or tag vs a list elsewhere: kept by definition
  order, reported as a conflict.
- Re-sorts: when JEI finishes starting (join, `/reload`), when a mod adds ingredients later, and when
  `client_scripts/jei_order.js` runs again (`/kubejs reload client_scripts`). Tags still need `/reload`.
- Runtime-added ingredients with no place (e.g. most of fluid_jei.js's fluids for now) sit after the other
  unplaced ones of their type: JEI gives them no position of their own. Once placed they sort normally (JO-12).
- A sort or report failure is logged (`[jei order]` in latest.log) and JEI keeps its own order.

### 12.4 First in-game run

1. Build and install patcherMod (`./gradlew :creativeCommon:installToMods`), set `orderReport = true` under `[jei]`
   in the patcherMod common config.
2. Join a world. latest.log: `[jeiOrder] defined 10 nodes over 4 types` (startup), `[jeiOrder] item: 2 jei
   tags` / `fluid: 1` / `effect: 1` (server), `[jei order] definition: 4 types, 10 nodes`, `[jei order] sorted
   N ingredients in M ms`, `[jei order] report: ...`.
3. JEI's first page: copper tools, then iron tools (JO-01, JO-02); after the items: the IE multiblocks, then
   water/lava/milk, then the effects (JO-05, JO-06).
4. `python3 tools/jei_order_report.py`, read `jei_order_catalog.md` section 2 (should list no problems except
   any list id the game doesn't know) and 4 (`global.removedItems` as hidden by the pack: JO-07).
5. JO-11: speed/haste/... placed under `beneficial` proves mob_effect tags reach the client (they are listed
   too, so check `placedBy: tag` in the JSON, not just the position).

### 12.5 Equipment family (2026-10-26)

From the 2026-10-26 scratchpad layout. KubeJS only (no patcherMod change): `/kubejs reload startup_scripts`,
`/kubejs reload client_scripts`, `/reload`, or a restart.

- `item` tree: `equipment` is the first (and so far only) item category. Children, in order: `wood`, `leather`,
  `stone`, `copper`, `chainmail`, `iron`, `gold`, `diamond`, `netherite`, `echo`, `felsic`, `dragon`, then (order to
  tune) `turtle`, `shulker`, `immersive_armors` (bone, slime, robe, prismarine, warrior, heavy, steampunk, divine,
  wither; the wooden set is in `wood`), `steel` (IE tools and armor, Delightful steel knife), `faraday`,
  `compressed_iron` (PneumaticCraft compressed iron and pneumatic armor), `quartz` (AE2 certus and nether quartz),
  `fluix`, `botania` (glass pick, manasteel, manaweave, elementium, terrasteel, alfsteel knife), `wardrobe`,
  `cardboard`, `vinery`, `floral` (Floral Enchantment crowns), `alexsmobs`, then `misc`.
- Each family is one `list` leaf: tools, then armor, then the family's copies of misc gear (shields, shears, horse
  armor, fishing rods and upgrades, backpacks, Dragon Loot's bow/crossbow/trident).
- `misc` children: `vanilla`, `modded`, `knives`, `arrows`, `guns`, then the catch-alls from the scratchpad's
  "equipment not yet in the families" list, folded in at the end: `tech` (Create, CTM, Create DD, IE tools and
  upgrades, PneumaticCraft, AE2, Construction Wands, physics gun, portal guns), `magic` (Botania wands, bows, rods,
  cloaks and charms, MCA staff, Witch Mobility hats, suit and brooms), `hats` (cosmetic heads, conductor caps,
  Botania cosmetics, all of Simple Hats via `@simplehats`).
- Pattern members: `knives` takes `delightful:*_knife` except the six placed in a family; `arrows` takes
  `@nock_enough_arrows`; `guns` takes Old Guns guns, shot, cartridges, cannonballs, shells, rockets and cannon barrels
  by wildcard, except `*_mechanism` / `*_repair_parts`, sorted `mod, name` with node `modOrder` Old Guns, IE,
  PneumaticCraft. `hats` takes `@simplehats`.
- Helper: nodes take `except` (passed to `global.tags.addMatching`), so a `from` pattern can leave out ids another node
  lists instead of making a conflict.
- Removed items are left out (`cb_microblock` saws, Everything Copper's shield; CE's copper shield is listed).
  172 new `global.item` keys for gear that had none (end of `item_dict.js`, "JEI equipment tree").
- Checked off-game: definition run in Node with the real dicts and helper (no warnings, 36 item nodes, 34 tags);
  every listed id exists in `exported/items_by_mod`, no id in two nodes, and the patterns expanded against the dumps
  give 1136 placed items with no conflicts.
- Not placed yet (on purpose): Sophisticated Backpacks upgrades, Old Guns parts and materials, Create toolboxes,
  IE `toolbox` and `turret_gun`, `physics_gun_mod` mode variants.

## 13. History

- 2026-10-18: goals, investigation, design, build plan; steps 1 to 3 built (12).
- 2026-10-19: steps 1 to 3 went out in pack 1.0.7 (DEPLOYED 2026-10-18 19:43, [plan_1.0.7.md](plan_1.0.7.md) JO). No
  change in 1.0.8. JO-01 to JO-12 pending.
- 2026-10-20: found that the client script never handed the definition over (Rhino "redeclaration of var" on a
  top-level `const` in `try`, in the user's client.log since 1.0.7), so nothing in this file had ever applied in game.
  Fixed in `jei_order.js`; proven on a scratch client (latest.log `[jei order] definition: 4 types, 10 nodes`). JO-01 to
  JO-12 still need a world.
- 2026-10-26: Equipment family built (12.5), replacing Equipment > Tools > copper / iron. JO-13 to JO-16 added.

### 12.6 Equipment not yet in the family (generated 2026-10-26, moved from the scratchpad 2026-10-27)

Generated from `items_by_mod`, excluding everything in `exported/gear` `tools.csv` and `armor.csv`; a name-pattern scan,
so check for misses. TODO `jei/jei-ordering #6`.

Family order wanted (owner, equipment at the front of the list for evaluation): by material family, then misc. Materials:
wood (including immersive armor), leather, stone, copper, chainmail, iron, gold, diamond, netherite, echo, felsic,
dragon (all equipment, even copies of misc); later, in logical order: turtle shell, shulker shell, immersive armor
tiers, steel (both), faraday, PC armor, AE2 quartz and fluix, Botania (progression order), wardrobe, cardboard, vinery,
floral enchantment, Alex's Mobs items. Types: sword, pick, axe, shovel, hoe, knife, machete, helm, chest, legs, boots,
goggle helm, diving helm, backtank, diving boots. Misc: vanilla (bow, crossbow, trident, compass, clock, bundle,
spyglass...), modded (quiver, slingshot, trowel, abacus...), remaining knives, all arrows, all guns (old guns first,
then IE), and everything listed below.

- vanilla: bow, crossbow, trident, shield, shears, fishing_rod, flint_and_steel, elytra, saddle, totem_of_undying, arrow,
  spectral_arrow, tipped_arrow, leather / iron / golden / diamond horse armor, netherite_horse_armor (the mod)
- quark: hammer, backpack (in the armor dump), forgotten_hat (in the armor dump), torch_arrow, iron_rod
- supplementaries: slingshot, quiver, bomb, rope_arrow, spring_launcher, wrench
- vanilla plus: everythingcopper copper_shield, copper_shears, copper_horse_armor; creativeengineering copper / gold /
  diamond / netherite / echo / felsic / dragon shield and arrow_fletching; dragonloot dragon_bow, dragon_crossbow,
  dragon_trident, dragon_horse_armor, dragon_shield (ce); iron_fishing_rods iron / gold / diamond / netherite rods and
  iron / gold / diamond upgrades; nock_enough_arrows quiver + 26 arrows (blossom, cupid, dousing, drill, echoing,
  ethereal, explosive, growing, ink, luminous, message, paint, party, prismarine, pufferfish, redstone_torch,
  reinforced, repulsive, seeker, slime, soul_torch, split, teleportation, torch, whistling); customizableelytra
  customizable_elytra, elytra_wing; domesticationinnovation deflection_shield; alexsmobs shield_of_the_deep,
  ancient_dart, shark_tooth_arrow, squid_grapple, vine_lasso, straddle_helmet, straddle_saddle; sophisticatedbackpacks
  backpack + copper / iron / gold / diamond / netherite backpack (armor-slot style); wardrobe backpack, large_backpack,
  travel_backpack, book_hat
- tech: create wrench, goggles, potato_cannon, wand_of_symmetry (mechanical_drill / saw are blocks, probably skip);
  create_things_and_misc sticky_launcher, brass_sticky_launcher, spout_gun (+ chocolate, honey, lava, slime, water);
  create_dd modular_drill, jetpack_placeable (block); ae2 certus / nether_quartz wrench and cutting_knife, charged_staff,
  matter_cannon; immersiveengineering hammer, drill (+ drillheads iron / steel, toolupgrade_drill_*), excavator, shield
  (+ toolupgrade_shield_*), glider, turret_gun; pneumaticcraft pneumatic_wrench, jackhammer, air_cannon, vortex_cannon,
  drill_bit_* (iron / compressed_iron / diamond / netherite), elytra_upgrade, jet_boots_upgrade_1-5; createaddition
  electrum_amulet; constructionwand stone / iron / diamond / infinity wand; cb_microblock saws (in the tools dump);
  physics_gun_mod physics_gun, supercharged_gravity_gun, displacer_cannon, tool_gun (+ about 14 mode variants, probably
  hide all but the base); ricksportalgun portal_gun, golden_portal_gun, prime_portal_gun
- oldguns (guns and ammo, would be its own family): matchlock, wheellock, flintlock, caplock x musket / long_musket /
  musketoon / pistol / blunderbuss / blunderbuss_pistol / doublebarrel / pepperbox / nock_gun; bombard,
  medium_naval_cannon, cannon barrels (small / medium / large), cannonballs, ram_rod; musket balls and paper cartridges
  in small / medium / large x stone / lead / iron
- magic: botania livingwood_bow, crystal_bow, slingshot, dreamwood_wand, twig_wand, mana_gun, light_launcher, mana_bomb,
  manasteel_shears, elementium_shears, balance_cloak, holy_cloak, unholy_cloak, invisibility_cloak, diva_charm,
  goddess_charm, divining_rod and the rods (cobble, dirt, skydirt, exchange, fire, gravity, missile, smelt,
  terraform, tornado, water), cosmetic_ancient_mask, eerie_mask, engineer_goggles, lusitanic_shield,
  tiny_potato_mask; mca staff_of_life; witchmobility silver_arrow_broom_item (brooms, check others); cloudstorage
  balloon_arrow
- hats and cosmetics: simplehats, the whole mod (about 20+ hats: angrymask, azumanga_hat, bigcrown, cat_hat, crown,
  cucumbereyemask, fishing_hat, flowercrown, ghostmask, goggles, hockeymask, horsemask, lilbow, orange_hat,
  right_hand_hat, sleepeyemask, strawberry_hat, udder_hat, worm_hat, plus more the scan missed); fruitsdelight
  durian_helmet; botania cosmetic_* (above)
