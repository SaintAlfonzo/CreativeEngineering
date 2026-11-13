# Energy (basic mode): electricity as the pack has it today

Written 2026-11-12. This is the reference for the **basic** electricity mode: the pack's current FE-based
electricity, and the home for any later changes to that same core that do not need Create: Power Grid. The other mode is
[energy_advanced.md](energy_advanced.md) (speculative; Power Grid, [power_grid.md](power_grid.md)). If Power Grid is
ever added, a per-world switch picks basic or advanced (power_grid.md 5.1); in basic, **no Power Grid block or item
is active**, so nothing in this file changes when it is added.

## 1. What basic is today

| Part | State |
|---|---|
| Energy unit | Forge Energy (FE), moved by capabilities and by IE wire networks (IF per tick) |
| Backbone | Immersive Engineering 9.2.4: LV / MV / HV wires, connectors, relays, transformers, capacitors, generators, Kinetic Dynamo, Mining Drill, Buzzsaw, Accumulator Backpack, charging station, Tesla Coil. More Immersive Wires adds AE2 and CC:T networks |
| Create Crafts & Additions 1.2.3 | alternator, electric motor, accumulator, modular accumulator, tesla coil, rolling mill, portable energy interface, capacitor. Its wire network (connectors, spools, relay, wires, sheets) is removed (`removed_items.js`, `recipes_engineering.md` `power.js`) |
| Other FE sources and sinks | PneumaticCraft (Pneumatic Dynamo, Flux Compressor), Immersive Petroleum Portable Generator, Create Applied Kinetics Energy Provider, Create: Dreams & Desires kinetic motor |
| Pack parts | Creative Engineering: IE iron and steel components, wire coils and coil blocks for 12 metals ([engineering.md](engineering.md)). Own mods register no FE storage |
| Not installed | TFMG, Mekanism, Thermal |

## 2. IE tier numbers (server config, 9.2.4 defaults)

| Tier | Wire transfer (IF/t) | Loss per 16 blocks | Connector in/out (IF/t) | Max length | Capacitor storage |
|---|---|---|---|---|---|
| LV copper | 2048 | 5 % | 256 | 16 | 100 000 (in/out 256) |
| MV electrum | 8192 | 2.5 % | 1024 (capacitor) | | 1 000 000 (in/out 1024) |
| HV steel | 32768 | 2.5 % | | | (HV capacitor) |

Source: `serverconfig/immersiveengineering-server.toml` of a pack world (`perf/runs/r1110/fixture/world_pristine/`). Values are
per world; copy any edit to `configs_edited/`.

## 3. Rules for basic

- Basic gets **no Power Grid content**. Pack changes to electricity that are wanted in both modes live here and in the
  scripts/mods that exist today; changes that need PG are advanced-only.
- Recipes and tags go through the pack helpers (`global.NCH`, `global.tags`), as for the rest of the pack.
- CC&A's Electric Motor and Alternator stay in basic (they are dropped only in advanced, power_grid.md D8).

## 4. Changes to basic

None planned (owner 2026-11-13: the planned-changes list is dumped). Add a change here only when one is decided, with its owner decision, status and test IDs.

## 5. Open work

| Item | Note |
|---|---|
| Create: Dreams & Desires kinetic motor, multimeter and integrated circuit are not in the item dict and have no pack rule | check against the dict when touching electricity |
| FE adapter (advanced only) needs a list of every FE device in the pack | build the list from the dumps (energy capability holders), then share it with the advanced doc |

## 6. History

- 2026-11-12: written from the overlap scan in power_grid.md 2.
