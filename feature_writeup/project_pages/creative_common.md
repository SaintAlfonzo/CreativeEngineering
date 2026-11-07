# Creative Common

**The shared library behind the Creative mods.** Creative Common is the core that every Creative mod is built on. On
its own it adds no blocks or items. It provides the shared code, scripting helpers and data the other Creative mods
need, plus a set of interface improvements, bug fixes and performance fixes that help any large Forge 1.19.2 pack.

---

## Mods that require Creative Common

- **Creative Engineering**: machines and engineering
- **Creative Delight**: food, cooking and Farmer's Delight addons
- **Creative Brewing**: brewing, alcohol and potions
- **Creative Equipment**: armor, tools and gear
- **Creative Magic**: magic mod integration
- **Creative Worldgen**: world generation and wildlife
- **Creative Calendar**: seasons and time
- **Creative Compat**: compatibility between third-party mods
- **Creative Devtools**: pack development tools

---

## What it does on its own

Every feature below works without any other Creative mod. Features that touch another mod only switch on when that mod
is installed.

### For mod and pack authors (KubeJS)

- **KubeJS scripts loaded from mod jars.** Creative mods ship their KubeJS scripts inside their own jars. Creative
  Common loads them before the pack's own `startup_scripts`, so a mod can be dropped into any pack without copying files.
- **Shared helpers** for recipes, tags, composting, villager and wandering trader trades, and JEI ordering.
- **Item, block, fluid, food and effect dictionaries** that skip any mod that is not installed, so scripts can name
  optional content safely.
- **Swap lists**: when a mod renames or removes an item, block, effect or potion, existing worlds keep working. Old ids
  are remapped on load, and items that were merged into another are converted in inventories, containers and on the
  ground. Operators can run `/ccswap items` to convert everything loaded right away.
- **Data-driven damage tags** that group damage sources (fire, magic, and so on) for other mods to use.
- **Loot tables that name optional mods** load without errors when those mods are missing.

### Interface

- **JEI**: recipes you can craft right now are listed first; extra recipe categories and pages (cycling crafting pages,
  smithing templates, Armor Trims recipes, world interactions); fluid amounts in tanks drawn on a readable scale; an
  optional pack-defined ingredient order.
- **WTHIT plugins**: contents of holders, pots, hives and tanks; campfire burn and rain state; silk touch and shears
  hints; block model icons; and more, each with its own config switch.
- **Tooltips**: a tooltip taller than the screen keeps its name and first lines visible. Item tooltips in AE2, FTB
  Quests, Patchouli, Lightman's Currency, Pretty Pipes and Wares screens now show tooltip images such as AppleSkin's food
  bars.
- **Armor bar overflow** (with Detail Armor Bar): armor above 20 points is drawn as extra rows of icons.
- **Container title plaques** from GUI resource packs stretch to fit long titles.
- **Effect HUD icons and descriptions**: food and drink effects show the icon of the item that gave them; works with
  Just Enough Effect Descriptions.
- **Toast filter**: tutorial and advancement toasts can be turned off.
- **In-game changelog viewer** on the title screen and pause menu, and a **loading screen tip bar** (tip packs use the
  Loading Screen Tips format).
- **Mod display names**: rename how any mod is shown in JEI, WTHIT and the Mods screen.

### Effects and potions

- **Magic Resistance**: a new effect that reduces magic damage and shortens poison and wither.
- Potions (normal, long and strong) for Magic Resistance, and for Reach and Phantom Bane when Better Beacons is installed.
- Effect categories, HUD icons and effect id remaps can be set in the config.

### Bug fixes

- **Ghost blocks on dedicated servers**: vanilla 1.19.2 corrupts multi-block updates once a pack has more than about
  524,000 block states. Fixed, along with the client-side crash from a missing block state in those packets.
- **Creative pick block deleting the held item** on servers (MC-197793).
- **Client never exiting after quit**: on some NVIDIA drivers the game window closes but the process keeps running.
- **"Accessing LegacyRandomSource from multiple threads" crashes**: each world's shared random is made thread safe.
- **Unbound holder crashes** from mods that check tags on unregistered entries.

---

## Optional pack settings (off by default)

Out of the box, Creative Common leaves vanilla behaviour alone. The *Creative Engineering* pack turns these on in its
own config, and any pack can do the same in `config/creativecommon-common.toml`:

| Setting | What it does |
|---|---|
| `[advancements] enabled = false` | Loads no advancements at all (vanilla, mods and datapacks) |
| `[toasts] tutorial / advancement = false` | Hides tutorial and advancement toasts |
| `[jei] packOrder = true` | Orders JEI's ingredient list by the pack's definition |
| `[wandering_trader] enabled = true` | Sets the Wandering Trader's spawn interval and number of offers |
| `[creative_menu] disabled = true` | Creative players open the survival inventory, which avoids the long freeze of building the creative search in large packs; JEI's cheat mode covers item browsing |

Most other features have a switch in the same file. With Create installed, the file can also be edited in game through
Create's config screen.

---

## Optional mods: what changes with each

None of these are required. Each change only switches on when that mod is installed. Changes that alter another mod's
code are tied to the versions given; with any other version they are turned off and one warning is logged, so an
update to that mod never crashes the game. "Any version" means Creative Common only uses the mod's public features.

### Recipe viewers and tooltips

- **JEI (11.8.x)**
  - Recipes you can craft right now are listed first.
  - Fluid amounts in tanks are drawn on a readable scale.
  - Extra pages: cycling crafting grids, repairs, world interactions, potion rows.
  - Optional pack-defined ingredient order.
- **Just Enough Effect Descriptions (2.1.x)**
  - Compound effects list their parts in the effect page and hover text.
- **WTHIT (any version)**
  - Look-at lines for holder, pot, hive and tank contents, campfires, silk touch and shears hints, and block model icons, plus the integrations listed below.
- **Legendary Tooltips with Iceberg (1.4.x)**
  - Fixes the name underline drawn through the title and 3D model on armor and tool tooltips.
- **AE2 (12.9.x)**
  - Item tooltips in terminals, pattern encoding and the guide show tooltip images (AppleSkin food bars and similar).
- **FTB Library (1902.4.x) and FTB Quests (1902.5.x)**
  - Same tooltip image fix in the quest book and FTB screens.
- **Patchouli (1.19.2-77 and later)**
  - Same tooltip image fix in books.
- **Lightman's Currency (2.2.x)**
  - Same tooltip image fix in its screens.
- **Pretty Pipes (1.13.x)**
  - Same tooltip image fix in the item terminal.
- **Wares (1.2.x)**
  - Same tooltip image fix on delivery agreements.

### Create and Immersive Engineering

- **Create (0.5.1)**
  - Sequenced assembly keeps the item's NBT (enchantments, names) through each step.
  - The Configs screen can edit text settings (Create leaves them as "Impl missing"), which also makes Creative Common's config editable in game.
  - Spout and Item Drain recipes appear on JEI row pages.
  - Haste from a pipe spilling Builder's Tea shows its HUD icon.
- **Create: Gravity (1.2.x)**
  - Its per-entity tick logic only runs in the dimensions that can have gravity (performance).
- **Immersive Engineering (9.2.x)**
  - Fluid inputs (Bottling Machine, Mixer, Refinery) accept a single fluid by id.
  - Bottling Machine recipes appear on JEI row pages.
  - WTHIT shows a sheetmetal tank's contents on every part.

### Performance and stability

- **Immersive Weathering (1.2.x)**
  - Faster random ticks.
- **WorldEdit (7.2.x)**
  - Skips building its full block state table at server start (a long stall in large packs).
- **Quark (3.4)**
  - Teleports into unexplored terrain no longer force chunk generation.
- **Nock Enough Arrows (1.2.x)**
  - The whistle goal skips its search when no whistling arrow exists.
- **Armor Trims (1.3.x)**
  - Item lists are cached.
  - JEI's smithing pages show which template each recipe needs.
- **Immersive Portals (2.3.x) and Heartstone (1.1.x)**
  - Crash at world creation with Serene Seasons fixed.
  - Client crash at launch with Heartstone fixed.

### Armor bar

- **Detail Armor Bar (2.6.x)**
  - Armor above 20 points draws as extra rows of icons.
  - Armor sets without their own icons get them.
- **Create: Deep Dark (any version)**
  - Its armor gets Detail Armor Bar icons.

### Food, effects and blocks

- **Better Beacons (1.0.x)**
  - Potions of Reach and Phantom Bane (normal, long, strong).
  - One player with Phantom Bane no longer stops phantom spawns for every player after them.
- **Farmer's Respite (2.0)**
  - Caffeinated keeps phantoms away through a hidden Phantom Bane part instead of resetting the rest timer.
  - WTHIT shows the kettle's leftover slots.
- **Fruits Delight (0.5.x)**
  - Effect remaps from the config also apply to jam, jelly and jello effects and their tooltips.
- **Farmer's Delight (any version)**
  - WTHIT shows the cooking pot's leftover slots.
  - Its stoves count as fire in the damage tags.
- **Supplementaries with Moonlight (any version)**
  - WTHIT shows what is in a jar, goblet or cage, and plants in hanging pots and flower boxes.
  - Potion jars appear in JEI's potion rows.
- **Framed Blocks (any version)**
  - WTHIT shows the plant in a framed flower pot.
- **Unlit Campfire (any version)**
  - WTHIT shows a campfire's remaining burn time.
- **Applied Cooking (any version)**
  - WTHIT shows whether the ME Kitchen Station is online and which security terminal it is connected to.

---

## Requirements

| | Version |
|---|---|
| Minecraft | 1.19.2 |
| Forge | 43 or newer |
| KubeJS | 1902.6.2-build.73 or newer (1902.6.x) |
| Rhino and Architectury | required by KubeJS |

Everything else is optional; see "Optional mods: what changes with each" above.

Both client and server need it.

---

## Source, issues and licence

- Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
- Licence: GNU GPL v3. Includes code adapted from Detail Armor Bar by RedLime (MIT) and Immersive Weathering by Ordana,
  MehVahdJukaar and Keybounce (LGPL v3), and a shaded copy of commonmark-java (BSD 2-Clause). The full notices are in the jar.
- Free to include in modpacks.
