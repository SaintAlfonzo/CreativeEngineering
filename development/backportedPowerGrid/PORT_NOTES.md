# Create: Power Grid 0.6.2 port: working notes (project `:backportedPowerGrid`)

Decisions and overlap: [power_grid.md](../../feature_writeup/power_grid.md). Execution plan per phase:
[plan_power_grid.md](../../feature_writeup/plan_power_grid.md). Mod id `powergrid` (upstream ids kept).

Status 2026-11-13: **phase P10 (decor) done: the port is complete** (section 24; every upstream block, item, entity,
block entity, recipe, mixin and ponder scene is in; `upstream/java` holds only what never ports, section 24.4), jar
`backportedPowerGrid-0.6.2-ce-19.2-17.jar` (`port_phase=10`; gate copy
`jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-17.jar`, sha1 cbc2ddee37ca4e8f8d844958a9744bc02c148d4a); P9d's
`-16` is sha1 618ed8016949c7be095f2f7b80156c6316a54000; P9c's
`-15` is sha1 f95311cc26f8294fe100347ca2d841af193de4dd; P9b's `-14` is sha1 d80a8babfdc03300f60b446813f08da9eb31bb7f (`-13` the same
without fix B20), P9a's `-12` sha1 75aa8fc5b1f41df529101f4e92d69db7f84e8d85 (`-11` without fix B19);
`./gradlew :backportedPowerGrid:build --offline` clean (no `sim` change since P9a, so the solver harness was not rerun;
84 passed, 6 known at P9a). Registers 78 blocks, 119 items, 2 fluids (1 fluid type), 5 entity types, 60 block entity
types, 7 menus, recipe type `powergrid:magnetization`, 3 particle types, recipe serializers `powergrid:boost_recipe` and
`powergrid:crafting_special_string_light_cord`, 116 recipes (all of them), 6 wire types, 29 mixins, 2 fan
processing types, 1 contraption type, the lightning rod movement behaviour, the forge registry `powergrid:components` (28
components), the `solar_biome_override` and `component_items` reload listeners, the CC: Tweaked peripheral provider
(when CC: Tweaked is loaded), the JEI categories magnetizing, circuit design and circuit assembly, and on the client 81
ponder scenes, 5 tags and 79 structures (sections 20, 24). P10 gate rows SQ-PWG-90 to -96, CQ-PWG-28, -29 queued (probe
`tools/scratch_query/pwg_p10_probe.js`); P9d rows SQ-PWG-84 to -89, CQ-PWG-27 (`pwg_p9d_probe.js`). P9 runs as four sub-steps with decimal phases (P9a = 9.1 to P9d =
9.4, section 5); P9c is section 22, P9d (Integrated Circuit and boost) starts from section 22.4. P9c gate rows
SQ-PWG-76 to -83, CQ-PWG-26 queued (probe `tools/scratch_query/pwg_p9c_probe.js`); P9b rows SQ-PWG-72 to -75, CQ-PWG-24,
-25 (`pwg_p9b_probe.js`); P9a rows SQ-PWG-67 to -71, SQ-PWG-06, CQ-PWG-22, -23 (`pwg_p9a_probe.js`). The jar is not
installed into `mods/` (plan E1). P11 (evaluation) prepared 2026-11-13: the regression run on `-17` and the owner session are pending (plan P11); no code change since `-17`, these notes are final unless a run finds a port fault.

## 2. Layout

| Path | What |
|---|---|
| `src/main/java` | ported code. Package layout and class names as upstream (`org.patryk3211.powergrid...`) |
| `upstream/java` | upstream code **not ported yet**, not compiled: `common` (`src/main/java`) and `forge` (`forge/src/main/java`) merged into one tree (no file clashed), `@ExpectPlatform` inlined (section 3). A phase moves each class it ports into `src/main/java`. A class ported only in part (`PowerGrid`, `forge/PowerGridImpl`) has its skeleton in `src/main/java` and keeps the full upstream copy here until the last part moves. **Since P10 it holds only code that never ports** (31 files: datagen, Flywheel 1.0 visuals, REI, TFMG, the 1.20 tab generator; section 24.4); every partial copy is deleted |
| `upstream/test` | upstream's solver tests (`org.patryk3211.electricity`, 21 files): input for the P1 harness |
| `upstream/mixins` | upstream's three mixin configs (`powergrid.mixins.json`, `powergrid.client.mixins.json`, `powergrid-forge.mixins.json`), for reference: each phase moves its mixin names into our single `src/main/resources/powergrid.mixins.json` |
| `upstream/mods.toml.upstream` | upstream's forge `mods.toml`, for reference |
| `src/main/resources` | **all** upstream resources (assets and data from the three trees above, merged; no clash), minus section 4's deletions, plus our `META-INF/mods.toml`, `pack.mcmeta` (pack format 9) and `powergrid.mixins.json`. The build ships only the current phase's part of it (section 5) |
| `phases.json` | phase manifest (section 5) |
| `libs/` | hard links to the pack's jars for `compileOnly` (git-ignored): Create 0.5.1.i, Flywheel 0.6.11-22, Registrate MC1.19-1.1.5, Architectury 6.6.92, JEI 11.8.1.1034, CC: Tweaked 1.101.4, Farmer's Delight 1.2.4 |

Not taken from the repository: `fabric/` (Fabric platform), `native/` (native solver sources), Gradle/Architectury build
files, `crowdin.yml`, `TRANSLATING.md`.

## 3. Platform inlining (D2)

`@ExpectPlatform` needs the Architectury Gradle plugin, which `build-logic` does not use. All 58 annotated methods (18
classes) were rewritten by script: the annotation and its import are gone and the body calls the forge implementation
(`<package>.forge.<Class>Impl.<method>(args)`), marked `// @ExpectPlatform inlined (D2)`. The forge `*Impl` classes stay
in their `forge` sub-packages. When a phase ports such a class it may fold the Impl body in (done for
`ModdedConfigs.registerPlatform`, whose `ModdedConfigsImpl` is deleted). Architectury 6 API calls (events,
`NetworkManager`, menus, `EventBuses`) stay; the pack has 6.6.92 and `mods.toml` requires `[6.5,7)`.

## 4. Deleted (plan P0 step 7, section 0 "Not ported")

| What | Files |
|---|---|
| Native solver (D4, B2) | `electricity/sim/solver/NativeMNA.java`; `native/` not copied |
| REI compat | `compat/rei/` (8 files) |
| Cold Sweat compat | `compat/cold_sweat/` (2), `config/CColdSweat.java` |
| TFMG compat | `compat/tfmg/` (4), `utility/proxy/TFMGProxy.java` |
| Advancements | `advancements/` (2), `collections/ModdedAdvancements.java`, `data/powergrid/advancements/` (96 files), `textures/gui/advancements.png` |
| Data-driven damage types (1.19.4+) | `data/powergrid/damage_type/` (6 files); P2/P4/P7/P9 add `DamageSource` constants with the same message keys |

Upstream classes in `upstream/java` that still reference deleted classes (fix when the class is ported: drop the
advancement call or the compat branch): `PowerGrid`, `forge/PowerGridImpl`, `electricity/base/ElectricBlockEntity`,
`ElectricBlock`, `ThermalBehaviour`, `electricity/battery/PotatoBatteryBlockEntity`,
`electricity/deviceconnector/DeviceConnectorBlock`, `electricity/gauge/EnergyMeterBlockEntity`,
`electricity/transformer/TransformerBlockEntity`, `TransformerCoreBlock`, `electricity/wire/BaseWireEntity`,
`EntityWireInteraction`, `equipment/BoostingChipItem`, `equipment/thunder/LightningRodMovementBehaviour`,
`kinetics/base/ElectricKineticBlockEntity`, `ElectricKineticBlock`, `kinetics/generator/rotor/AbstractRotorBlock`,
`kinetics/motor/ConstantSpeedMotorBlockEntity`, `ElectricMotorBlockEntity`, `mixin/BeltDeployerMixin`, `mixin/SheepMixin`.
The `data/` datagen providers stay in `upstream/java` but are not to be ported: the port ships upstream's generated
output (`forge/src/main/generated`) instead.

## 5. Phase manifest and resource filter

- `gradle.properties` `port_phase` (now **10**, the last phase: P10 decor; P9 was 9.1 to 9.4) is the build's phase. Phases are numbers (since P9a): a phase split into
  sub-steps uses decimals, so P9 is 9.1 (P9a) to 9.4 (P9d); `PortPhase.CURRENT` and the table values are doubles and
  `pg_phase_check.py --phase` takes a decimal. `build.gradle` generates
  `org.patryk3211.powergrid.PortPhase` (task `portPhaseSource`) with `CURRENT`, `FIRST_CONTENT_PHASE` (2) and the phase
  of every id in `phases.json`: `PortPhase.phaseOf(kind, id)`, `isPorted(kind, id)`, `require(kind, id)` (kinds
  `block`, `item`, `entity`, `fluid`, `menu`; an item id falls back to its block). Phases that port `Modded*` entries
  wrap each registration in `require` (fails at load if a later-phase id is registered) or skip it with `isPorted`.
- `phases.json`: generated once (2026-11-13) from the lang file and the plan's per-phase lists (all 77 blocks, 57
  items, 5 entities and the acid fluid matched the plan's lists exactly; `flowing_acid` and the menus added by hand,
  `portable_battery` kept as a block only, its item follows the block). Sections: `blocks`, `items` (standalone items),
  `entities`, `fluids`, `menus`, `recipe_types`, `recipes` (per-recipe overrides), `resources` (path globs; ponder files
  by scene, `component_items` P9, `solar_biome_override` P6, `atlases/` and `lang/default/` never (99)).
- `tools/pg_phase_check.py` (dev-only): report mode lists the recipes each phase enables; build mode (task
  `phaseResources`, run by every build) checks the manifest and the recipe gate, then copies the resources that ship at
  `port_phase` into `build/generated/phaseResources`, which is the only resource source of the jar. A file's phase:
  blockstates, `models/block/<id>.json`, `models/item/<id>[.json|/]`, `loot_tables/blocks/<id>.json` and
  `wire_types/<id>.json` follow their id; recipes take the highest phase of their output, powergrid ingredients and
  powergrid recipe type; everything else ships (lang whole, textures, sounds, particles, block models in sub-folders,
  component models). Tag files (`data/*/tags/{blocks,items,entity_types,fluids}`) lose their entries for unported ids
  (20 tags at phase 0). An unknown powergrid id in a recipe or tag fails the build.
- Phase 0 result: 540 files ship (assets only, plus 20 filtered tags), 535 left out (all blockstates, loot tables,
  recipes, item models, ponder, wire types, component items).
- Phase 2 result: 611 files ship (13 tags filtered), 466 left out. P2 enables 11 recipes (copper cord, current gauge
  from conversion, heavy wire connector, insulated copper wire, power gauge, voltage gauge from conversion, wire
  connector, wire cutter, three wire cuttings), the 12 P2 loot tables and 5 wire types (`string_light_cord` is P10).
  Manifest edits in P2: block `overheated_component` added (P2, new, section 11); menu `custom_display` moved from P8 to
  P2 (the gauges open it with a name tag, `CustomDisplayBehaviour.use`). The 31 cross-phase recipe overrides are
  unchanged (owner decision pending).
- Phase 3 result: 698 files ship (8 tags filtered), 379 left out. P3 enables 11 recipes (copper coil, resistive coil,
  device connector, grounding rod, HV switch, potato battery, the thermometer (P2 output, waited for the resistive coil),
  the two magnetization recipes, zinc sheet pressing, transformer core sequenced assembly), the 23 P3 loot tables and
  item tags `forge:copper_coils`, `forge:iron_coils`, `forge:coils`, `forge:plates/zinc` (with `powergrid:zinc_sheet`,
  next to the pack's zinc sheet: integration A3). The battery's sequenced assembly needs `acid` (P9) and stays out. No
  manifest edit in P3. The transformer core assembly (`create:sequenced_assembly`, deploying iron plate + pressing,
  transitional `powergrid:incomplete_transformer_core`, a `SequencedAssemblyItem`; weighted results 100 / 5 / 3 / 1 / 1
  including `create:iron_sheet`) uses only formats Create 0.5.1.i reads (`ingredient`, `transitionalItem`, `sequence`,
  `results`, `loops`).
- Phase 5 result: 36 of the 81 ponder structures ship (plus the P4 set). Manifest edits in P5 (`resources`, the longest
  glob wins): eight structures of scenes that place later-phase blocks moved out of the P5 `ponder/**` glob:
  `power_gauge.nbt`, `switch.nbt`, `hv_breaker.nbt`, `generator/generator_3.nbt`, `wire/simple.nbt`, `wire/wire_loop.nbt`,
  `wire/cord.nbt` to P6 (light fixture, basin heater) and `wire/string_lights.nbt` to P10. No recipe or tag change.
- Phase 6 result: 862 files ship (6 tags filtered), 216 left out. P6 enables 9 recipes (electric fan, electric pump,
  factory light, growth lamp, light bulb, LV light bulb, neon bulb, basin heater mechanical crafting, solar panel
  sequenced assembly); the heating coil and light fixture stay uncraftable (`pins`, P9) and the solar panel bearing
  (`copper_plating`, P10): 31 overrides unchanged. The P6 loot tables, blockstates, item models, the
  `create:fan_transparent` entry for the heating coil and the 19 P6 ponder structures ship. Manifest edit in P6:
  `ponder/encased_fan.nbt` moved from 6 to 9 (its structure holds a `circuit_board`). Since 2026-11-13 the build also
  checks client assets (`check_assets`): every block, item and particle type the ported collections register needs its
  blockstate, item model, lang key, `particles/<name>.json` and particle sprites in the output.
- Phase 7 result: 878 files ship (5 tags filtered), 200 left out. P7 enables 5 recipes (portable battery, and the
  mechanical crafting of the Electro-Baton, Electro-Zapper, portable drill and portable saw); the item tag
  `forge:chestplates` now carries `powergrid:portable_battery`. The four `deploying/*_boosting` recipes stay P9 (boost
  recipe type). No manifest edit in P7; 31 overrides unchanged.
- Phase 8 result: 912 files ship (5 tags filtered), 166 left out. P8 enables 5 recipes (alarm bell, display module,
  plotter, CRT mechanical crafting, punch card sequenced assembly from `#forge:cardboard`, uncraftable until the pack
  fills that tag, D13); the modular display (`pins`) and the punch card reader (override) stay P9. The P8 loot tables,
  blockstates, item models and the 5 P8 ponder structures ship. No manifest edit in P8; 31 overrides unchanged.
- Phase 9.1 (P9a) result: 1008 files ship (4 tags filtered), 70 left out. Manifest edits in P9a: the P9 entries split
  into 9.1 to 9.4 (`phases` has the four sub-step names): 9.1 `circuit_board`, the 17 component items and
  `incomplete_bjt_npn` / `incomplete_bjt_pnp` (the BJTs' own assembly transitional items, moved from P9c so the BJT
  recipes ship with the BJTs), resources `component_items/**`, `ponder/circuit/**` and `encased_fan.nbt`; 9.2
  `circuit_design_table`, `circuit_schematic`, the four editor menus; 9.3 `acid` (block and fluids), `acid_bucket`,
  `empty_circuit`, `unetched_circuit`, `incomplete_circuit`, `incomplete_unetched_circuit`; 9.4 `integrated_circuit`,
  `incomplete_electrical_gizmo`, recipe type `boost_recipe`. P9a enables 28 recipes (94 in all): the 17 component
  crafting recipes, `pins`, the two BJT sequenced assemblies, and the 11 recipes that waited for `pins` (energy meter,
  FE inverter, heating coil, light fixture, LV button, LV switch, modular display, redstone converter, spark gap,
  generator commutator, punch card reader). The 17 overrides that said 9 now name their sub-step (11 x 9.1, battery
  9.3, the four boosts and the electrical gizmo 9.4); with 9.1 the 11 `pins` overrides are met (their outputs are
  craftable now) but stay in the manifest: the checker still needs them, since the output's phase is below `pins`.
  31 overrides in all; still unmet at 9.1: `copper_plating` (P10: design table, solar panel bearing), acid (battery
  assembly, 9.3), the Integrated Circuit (electrical gizmo assembly, 9.4) and the four boosts (9.4); the
  `conductive_casing` ones were met in P4, the `pins` ones now. Tag `powergrid:circuit_component` ships whole (28
  entries).
- Phase 9.2 (P9b) result: 1013 files ship (4 tags filtered), 65 left out: the design table's blockstate, models, item
  model and loot table, the `circuit_schematic` item model; the mineable tags gain `circuit_design_table`. Manifest edit:
  recipe override `deploying/schematic_copy` 9.3 (computed 9.2 from `circuit_schematic`; the copy of the schematic tag
  onto the deployed item is `BeltDeployerMixin`'s schematic branch, which P9c needs for the unetched circuit chain
  anyway, section 21.3; without it the recipe turned an empty schematic into a blank one). Recipes stay 94: the design
  table's crafting recipe waits for `copper_plating` (P10). 32 overrides.
- Phase 9.3 (P9c) result: 1026 files ship (2 tags filtered), 52 left out: the acid blockstate and block model, the
  `acid_bucket` and the four circuit items' models, the acid fluid tag (both entries) and the `circuit_schematic_holder`
  entries `incomplete_circuit`, `incomplete_unetched_circuit`, `unetched_circuit`. P9c enables 6 recipes (100 in all):
  `mixing/acid`, `mixing/etched_circuit_board`, `sequenced_assembly/unetched_circuit_board`,
  `cutting/empty_circuit_slabs`, `deploying/schematic_copy` and `sequenced_assembly/battery` (P3 output, waited for
  acid). Two overrides are met at 9.3 and drop out of the unmet list: `sequenced_assembly/battery` (redundant now: the
  recipe's acid fluid ingredient already gives 9.3; kept for the record) and `deploying/schematic_copy` (needed: its own
  ids give 9.2). Still unmet: `copper_plating` (P10: design table, solar panel bearing) and the P9d ones (electrical
  gizmo assembly, four boosts). No manifest edit in P9c. The client-asset check now covers fluids (section 22): each
  `REGISTRATE.fluid(...)` in `ModdedFluids` needs its still and flowing textures, `fluid.powergrid.<id>`, the liquid
  block's blockstate and lang key, and with `.bucket()` the bucket's item model and lang key.
- Phase 9.4 (P9d) result: 1034 files ship (2 tags filtered), 44 left out (P10 and the never-shipped 99 set): the
  `integrated_circuit` and `incomplete_electrical_gizmo` item models and the six P9d recipes. P9d enables 6 recipes
  (106 in all): `mechanical_crafting/integrated_circuit`, `sequenced_assembly/electrical_gizmo` and the four
  `deploying/*_boosting` (`powergrid:boost_recipe` serializer, type `create:deploying`). Their five overrides are met.
  Unmet after P9: only the two `copper_plating` ones (`crafting/circuit_design_table`, `crafting/solar_panel_bearing`,
  P10). The electrical gizmo (P2 item) had no recipe that could run until now, so the 9 recipes that use it
  (shipped since P2 to P8) become survival-craftable with P9d. No manifest edit in P9d.
- Phase 10 (P10) result: 1072 files ship (0 tags filtered), 6 left out (only the never-shipped 99 set: `atlases/`,
  `lang/default/`): the P10 blockstates, models, item models, loot tables, the `string_light_cord` wire type, the
  `ceiling_tile.nbt` and `wire/string_lights.nbt` ponder structures and 10 recipes (116 in all, every recipe file):
  the copper plating slab and stairs (stonecutting and crafting), `crafting/string_light_cord_pattern` (special type),
  `deploying/copper_plating`, `deploying/string_light_cord`, `deploying/ceiling_tile` (needs `#forge:cardboard`, empty
  in the pack until integration, D13) and the two that waited for `copper_plating`, `crafting/circuit_design_table`
  and `crafting/solar_panel_bearing`. **All 32 overrides are met** (the highest is 10 = `port_phase`; none is above
  it), so no recipe waits on a later phase any more. Resource edits (B33): `mineable/pickaxe` gains the plating slab and
  stairs; the slab's loot table drops two from a double slab. No manifest edit in P10.
- Every non-powergrid id in the shipped tags exists in the pack (checked against `kubejs/exported/items_by_mod`).

**Recipe gate result (32 overrides in `phases.json` `recipes`; the 32nd, `deploying/schematic_copy`, since P9b).** Many recipes need an ingredient from a later phase than
their output, so the output is uncraftable until then: `conductive_casing` (P4) is in 16 P2/P3 recipes (cord junction,
socket, voltage gauge, multimeter, energy meter, contactor, electromagnet, fuse holder, HV breaker, MV switch, power
resistor, carbon pile coil, LV button, LV switch, redstone converter, battery); `pins` (P9) in 11 (energy meter, FE inverter, LV
button, LV switch, redstone converter, spark gap, heating coil, light fixture, modular display, generator commutator,
punch card reader; met since P9a, 9.1); `resistive_coil` (P3) in the thermometer; `copper_plating` (P10) in the circuit design table and solar
panel bearing; the battery assembly needs `acid` (P9c, 9.3; met since P9c); the electrical gizmo assembly needs `integrated_circuit`,
`zinc_sheet` and `incomplete_electrical_gizmo` (P9d, 9.4; met since P9d); the four boost recipes are P9d by design (met since P9d). Scratch rows use `/give`, so
gates are not affected; if the owner wants survival crafting earlier, move the plain crafting materials
(`conductive_casing`, `pins`, `copper_plating`, ...) to an earlier phase in `phases.json` (plan_power_grid.md history,
open question to the owner).

## 6. Recipe JSON fixes (all phases, done once in P0)

`python3 tools/pg_phase_check.py --fix-recipes` (idempotent) rewrote 76 of the 116 recipe files: `category` and
`show_notification` dropped (74 files); ingredient `create:copper_sheet` -> tag `forge:plates/copper`; ingredient
`create:cardboard` -> tag `forge:cardboard` (fe_inverter, copper_coil, ceiling_tile deploying, punch_card sequenced
assembly: uncraftable until the pack fills `#forge:cardboard` at integration, D13, H6). Byproducts: the punch card
assembly's 5 % `create:cardboard` byproduct is **dropped** (no such item on Create 0.5.1; an output cannot be a tag); the
battery assembly's 2 % `create:copper_sheet` byproduct stays (the item exists in 0.5.1).

## 7. Classes ported

| Phase | Class | Change |
|---|---|---|
| P0 | `PowerGrid` | skeleton: `MOD_ID`, `LOGGER`, `asResource`, `texture`, `maxRPM` (`AllConfigs.server()`), `init` -> `ModdedConfigs.register()`. `getPlatform` / `forPlatform` dropped (Forge only) |
| P0 | `forge/PowerGridImpl` | skeleton `@Mod` entry: mod bus, `EventBuses.registerModEventBus`, config load/reload events, creative tab from phase 2 |
| P0 | `forge/PowerGridCreativeTab` (new, GPLv3) | 1.19.2 `CreativeModeTab` subclass, label `powergrid.main` (lang key `itemGroup.powergrid.main`); copper ingot icon until the Electric Motor (P4) |
| P0 | `collections/ModdedConfigs` | `registerPlatform` inlined (forge body); stress provider hook moved to P4 (Create 0.5.1 `BlockStressValues` API) |
| P0 | `config/*` (15 classes) | `ConfigBase` from `com.simibubi.create.foundation.config` (same API in 0.5.1); `BuiltInRegistries` / `CatnipServices` -> `ForgeRegistries.BLOCKS`; `ResourceLocation.withSuffix` (1.20) -> local helper in `CResistance` |
| P0 | `config/CServer` | Cold Sweat section removed; `GlobalElectricNetworks.configsReloaded()` on reload returns in P2 |
| P0 | `config/CSolver` | `solverBackend`: `JAVA` only and default (upstream default `NATIVE`); `SolverBackend.create(network)` returns in P1; B3: key `splittingsTransformers` kept, commented (E4) |
| P0 | `config/CGenerator` | B4: comments added to the five inertia keys (`generatorRotorInertia` is read by no block upstream; its comment says so) |
| P0 | `config/CElectricity` | `TFMGConnectorPower` removed (TFMG compat dropped) |
| P1 | `electricity/sim` (63 upstream files: `ElectricalNetwork`, `GraphedElectricalNetwork`, `NetworkGraph`, wires, `node/*`, `solver/*`, `calculation/*`, `special/*`) | moved unchanged except the rows below. **Not moved (game-side, P2):** `DebugItem` (item, block entities), `OwnedElectricWire` (`WireEntity`), `node/OwnedFloatingNode` (`IWireEndpoint`), `special/SamplingWire` (`ElectricBehaviour.SyncAppender`, `FriendlyByteBuf`), `special/TransmissionLine` and `TransmissionLinePart` (`WorldNetworks`, wire entities, `ChunkPos`, NBT) |
| P1 | `sim/ElectricalNetwork`, `GraphedElectricalNetwork` | Catnip `Pair` -> `com.simibubi.create.foundation.utility.Pair` (same `of` / `getFirst` / `getSecond`) |
| P1 | `sim/ElectricalNetwork.updateConductance` | **fix B5** (power_grid.md 3.5): a null node is ground. Upstream returned early for every wire with a ground end (`hasNode(null)` is false), so adding, removing or changing the resistance of a wire to ground after the network's first solve was ignored until a full matrix rebuild. Shown by upstream's own `StructureChangeTests` (3 tests) and `TransformerTests.transformer1P2S`, which fail without the fix and pass with it, and by gate 8 of the harness on the game's network type |
| P1 | `sim/GraphedElectricalNetwork.convergenceProblems` | `DebugCommand.pushProblems` (a command class, P2) replaced by a static hook `PROBLEM_SINK` (`IProblemSink`, same arguments), null until the debug command sets it in P2 |
| P1 | `sim/NetworkGraph`, `sim/special/TransmissionLinePort` | the concrete `TransmissionLine` replaced by the new interface `special/ITransmissionLine` (GPLv3; `getNode1()` / `getNode2()`): line sets, `getConnectedLines`, `IGraphModifyHooks.lineConnected` / `lineDisconnected`, the port's `line` field and `getLine()`. P2 makes `TransmissionLine` implement it; `WorldNetworks` / `ClientWorldNetworks` hook overrides take `ITransmissionLine` and cast where they need the class |
| P1 | `config/CSolver.SolverBackend` | `JAVA(JavaMNA::new)` constructor and `create(ElectricalNetwork)` back, as upstream (minus `NATIVE`) |
| P1 | `circuits/thermal` (`ThermalUnit`, `ThermalBuilder`) | **deferred to P2**: both read `electricity/base/ThermalBehaviour` (a Create `BlockEntityBehaviour`: `OVERHEAT_TICKS`, `ABSOLUTE_ZERO`, `dissipationFactor`), and `ThermalUnit` uses `CompoundTag` / `Vec3`. They move with `ThermalBehaviour` in P2 (only circuit components, P9, use them) |
| P0 | `config/CCommon` | **new key `clientMirror`** (default true; H5, D14): server-values-only switch. Changes nothing until P2 wires it into gauges, light and wire glow |

| P2 | `electricity/base` (24 files), `wire` (33, with `powercord`, `registry`, `forge/WirePreviewImpl`), `wireconnector`, `creative`, `gauge`, `info` (with `customdisplay`), `socket`, `GlobalElectricNetworks`, `WorldNetworks`, `ClientWorldNetworks`, `ClientElectricNetwork`, the six game-side `sim` classes, `electricity/resistor` base classes (`AbstractResistorBlock`, `ResistorBlockEntity`, `ResistorBoxTransform`, `ResistorValueBehaviour`; the creative resistor extends them, the `power_resistor` block is P3), `commands` (4), `equipment/multimeter`, `equipment/thermometer`, `circuits/thermal`, `network/SimplePacket` and 11 packets, `base/CustomProperties`, `IConnectableBlock`, `utility` (Lang, NumberFormats, Unit, BlockTrace, PlacementOverlay, PlayerUtilities, PlayerLookup, ShaperUtils, ClientSideAccess, EditableScrollBox, IComplexRaycast, `sound/*`), 11 mixins | moved; the API mapping of power_grid.md 3.6 applied (Catnip to `foundation.utility` / `foundation.render` / `CreateClient.OUTLINER`, Flywheel 1.0 `PartialModel` to 0.6, `CreateLang` to Create's `Lang`, `IHaveGoggleInformation` / `DisplaySource` to their 0.5.1 packages, `@Environment` to `@OnlyIn`, `BuiltInRegistries` / `Registries` to `Registry`, `level()` to `level`, `BlockPos.containing` to `new BlockPos`, `copyWithCount` to `ItemHandlerHelper.copyStackWithSize`, `ItemDisplayContext` to `TransformType`, joml to `com.mojang.math`, `PoseStack.rotateAround` to translate / `mulPose` / translate, catnip `rotate*CenteredDegrees` to `centre().rotate*().unCentre()`, `ChunkLevel.isBlockTicking` to `ticketLevel <= 32`, `ForgeMod.BLOCK_REACH` to `REACH_DISTANCE`, `sendSuccess(Supplier)` to `sendSuccess(Component)`). Every changed file carries the "Modified for the Forge 1.19.2" line |
| P2 | `PowerGrid`, `forge/PowerGridImpl`, `PowerGridClient`, `forge/PowerGridClientImpl`, `forge/ForgeEvents`, `forge/ForgeClientEvents`, `AbstractPowerGridRegistrate`, `forge/ForgePowerGridRegistrate`, `collections/*` (blocks, items, block entities, entities, menus, packets, tags, keys, partial models, sound events, damage types, render layers, icons, commands, display sources) | P2 parts only; the full upstream classes stay in `upstream/java` until their last part ports. Registrate data-generation calls (blockstate, item model, tag, lang, recipe, the wire_types provider `WireItem.properties`) are left out: the jar ships upstream's generated output. `ItemDisplay` (1.20 tab display generator) not ported: items join `PowerGridCreativeTab` through Registrate's default tab |
| P2 | partial, upstream copy kept in `upstream/java` | `DebugItem` (generator readouts P4, circuit board P9), `CircuitBoardEndpoint` (kept for `WireEndpointType` ordinals; board methods stubbed until P9), `MultimeterItem` (circuit board probing P9), `IElectricEntity` (`setTo(BakedCircuit)` P9), `WireItem` (string light cord tooltip P10), `PlacementOverlay` (transformer, switch, modular display texts P3/P8), `ForgeEvents` (drill hook P7), `ForgeClientEvents` (drill and saw hand render P7). Moved back to `upstream/java`: `mixin/EntityMixin` (it magnetizes andesite into `magnet`, a P3 item) and `network/packets/ChangeScreenC2SPacket` (circuit editor screens, P9) |
| P2 | advancements | every call removed for good (`ElectricBlockEntity.registerAwardables` / `award` / `awardIfNear`, `ElectricBlock.setPlacedBy`, `ThermalBehaviour` BLOW_UP, `EntityWireInteraction` ELECTRIC_DAMAGE, `BaseWireEntity` WIRE_CUT, `EnergyMeterBlockEntity` rollover). Later phases that call `award(...)` drop the call |
| P2 | new (GPLv3) | `electricity/overheated/*` (D4), `electricity/wire/registry/WireTypeReloadListener`, `network/packets/WireTypesSyncS2CPacket`, `utility/GuiGraphics` (1.20 GuiGraphics stand-in over PoseStack / GuiComponent), `utility/PonderLevels` (ponder level check kept off servers) |
| P3 | `electricity/resistor/ResistorBlock`, `fuse` (3), `electricswitch` (13; `HvSwitchVisual` not ported), `contactor` (2), `redstoneconverter` (9), `transformer` (11), `battery` (13), `grounding` (2), `sparkgap` (4), `carbonpile` (5), `electromagnet` (9, with `recipe/` and `forge/ElectromagnetBlockEntityImpl`), `febridge` (4, with `forge/FEInverterBlockEntityImpl`), `deviceconnector` (7, with the forge impls), `particles` (6), `kinetics/base/ElectricKineticBlockEntity` (moved forward from P4: the HV switch and HV breaker are kinetic), `kinetics/generator/winding` (5; the `winding` block is P3), `collections/ModdedParticles` and `forge/ModdedParticlesImpl`, `network/packets/TransformerWindingC2SPacket`, `utility/proxy/SubstituteProvider` and `SubstituteBlockEntityProvider`, `compat/jei/MagnetizingCategory`, `AnimatedMagnet`, `PowerGridJEI` (part), mixins `EntityMixin`, `LightningAccessor`, `AirCurrentMixin` (moved forward from P6), client `ValueSettingsScreenMixin` | moved; API mapping as P2 plus section 13. Every changed file carries the "Modified for the Forge 1.19.2" line (55 of the 106 moved files; the other 51 are byte-identical to the tag) |
| P3 | `PowerGrid`, `forge/PowerGridImpl`, `PowerGridClient`, `forge/PowerGridClientImpl`, `collections/ModdedBlocks`, `ModdedItems`, `ModdedBlockEntities`, `ModdedPackets` (`TRANSFORMER_WINDING` appended), `ModdedPartialModels` (HV switch rod, spark gap arm, HV breaker signals), `ModdedDisplaySources` (battery), `ModdedConfigs` (stress provider), `utility/PlacementOverlay` (transformer winding and switch texts back), `utility/GuiGraphics` (9-argument `blit`), `electricity/sim/DebugItem` (winding dump back) | P3 parts added; upstream copies of the partial classes stay in `upstream/java` (`PlacementOverlay` keeps the modular display text, P8) |
| P3 | not ported, stay in `upstream/java` | `utility/proxy/ProxyProvider` (only the TFMG proxy used it), `electricity/electricswitch/HvSwitchVisual` (Flywheel 1.0), `compat/HiddenItems` (REI only) |
| P3 | advancements | removed for good: `TransformerBlockEntity` (TRANSFORMER), `PotatoBatteryBlockEntity` (POTATO_BATTERY), `TransformerCoreBlock` (NETHER_TRANSFORMER and `PGAdvancementBehaviour.setPlacedBy`), `ElectricKineticBlockEntity.registerAwardables` / `award` / `awardIfNear` |
| P4 | `kinetics/base` (`ElectricKineticBlock`, `TunedBlock`, `TunedBlockEntity`, `TunedBlockRenderer`, `GeneratorBlockEntity`; `ElectricKineticBlockEntity` already moved in P3), `kinetics/motor` (6), `rheostat` (2), `variac` (2), `servo` (3), `generator/IRotorAssemblyPart`, `generator/housing` (2), `generator/inductionrotor` (9), `generator/clutch` (4), `generator/rotor` (5), `base/SegmentedBehaviour`, `utility/Directions`, `mixin/KineticBlockEntityAccessor` | moved; 1.19.2 / Create 0.5.1 changes in section 14. `GeneratorBlockEntity` is dead code upstream (no subclass) and moved unchanged |
| P4 | `collections/ModdedBlocks` (13), `ModdedBlockEntities` (9), `ModdedPartialModels` (shaft bit, commutator shaft and brushes, clutch shaft, variac armature, induction rotor, conductive casing CT shift), `ModdedDisplaySources` (`CLUTCH`), `ModdedConfigs` (generator speeds in the stress provider), `config/CStress` (`setGeneratorSpeed`, `getGeneratedRPM`), `forge/PowerGridImpl` (Carry On: clutch, induction rotor, commutator), `forge/PowerGridCreativeTab` (Electric Motor icon), `RedstoneConverterRegistry` (clutch), `DebugItem` (commutator, rotor and induction rotor readouts), `WindingBlock.canConnect` (housing branches back), `powergrid.mixins.json` (`KineticBlockEntityAccessor`) | P3 hand-off list (section 9) done |
| P4 | not ported, stay in `upstream/java` | Flywheel 1.0 visuals `kinetics/base/HalfShaftVisual`, `TunedBlockVisual`, `generator/rotor/RotorVisual`, `inductionrotor/CommutatorVisual`, `clutch/GeneratorClutchVisual` (E3; the renderers draw everything); `kinetics/plotter`, `punchcard` (P8) |
| P4 | advancements | removed for good: `ElectricMotorBlockEntity` and `ConstantSpeedMotorBlockEntity` (ELECTRIC_MOTOR, BLOW_UP), `ElectricKineticBlock.setPlacedBy` and `AbstractRotorBlock.setPlacedBy` (`PGAdvancementBehaviour.setPlacedBy`) |

| P5 | `ponder/base` (`CreateWireInstruction`, `RemoveWireInstruction`, `ElectricityTickInstruction`, `SetSourceInstruction`, `UnloadElectricityWorldInstruction`, `VoltageSource`, `WireElement`, `InvisibleWireElement`), `ponder/PowerGridPonderPlugin`, `ponder/scenes/MagnetScenes`, `electricity/PonderElectricNetwork` | moved whole, on Create 0.5.1 `foundation.ponder` (section 15) |
| P5 | `ponder/base/PowerGridSceneBuilder` | rewritten as a wrapper (upstream extends Create 6 `CreateSceneBuilder`; section 15) |
| P5 | partial, upstream copy kept in `upstream/java` | `ponder/base/ElectricInstructions` (`connectLightCord` P10, solar panel methods P6), `PowerGridPonderScenes` and `PowerGridPonderTags` (later-phase scenes and members), `scenes/GaugeScenes` (voltage, energy meter; current P6, power P6), `GeneratorScenes` (all but `generator3`, P6), `RelayScenes` (all but `switchSceneFor` / `switchScene` and `hvBreaker`, P6), `WireScenes` (voltage drop, connector, grounding, improved grounding, insulated wire, cord junction, cord socket; `simple`, `wireLoop`, `cord` P6, `stringLights` P10), `DeviceScenes` (motor, constant speed motor, servo, electromagnet, battery; the rest P6, P8, P10) |
| P5 | not ported, stay in `upstream/java` | `ponder/scenes/CircuitScenes` (P9); `forge/PowerGridImpl.providePonderLang` (datagen, never ported) |
| P5 | `forge/PowerGridClientImpl` | `clientSetup` (`FMLClientSetupEvent`, `enqueueWork`) calls `PowerGridPonderPlugin.register` (upstream: `PonderIndex.addPlugin` in `PowerGridClient.initClient`) |
| P6 | `electricity/heater`, `basinheater`, `fan`, `pump`, `light/fixture`, `light/factorylight`, `light/bulb`, `solarpanel` (with `registry/`), `collections/ModdedContraptions`, `utility/RecipeNbtTransfer`, `utility/forge/IterateItemHandler`, mixins `ChuteBlockEntityMixin`, `FanProcessingMixin`, `FluidPropagatorMixin`, `forge/BasinRecipeMixin`, `forge/CookingPotBlockEntityMixin`, `forge/SkilletBlockEntityMixin` | moved; changes in section 16. `SolarBiomeRegistry` rewritten as a reload listener (B10 fixed) |
| P6 | `PowerGrid` (block movement check), `forge/PowerGridImpl` (`commonSetup`: fan types, contraption type, movement check; solar reload listener), `ModdedBlocks`, `ModdedItems`, `ModdedBlockEntities`, `ModdedPartialModels`, ponder classes (section 17) | P6 parts added |
| P6 | not ported, stay in `upstream/java` | `electricity/solarpanel/SolarPanelBearingVisual` (Flywheel 1.0, E3), `light/string` (P10) |
| P6 | new (GPLv3) | `assets/powergrid/particles/zap.json` (`{}`; missing upstream, fatal on 1.19.2 clients), the client-asset check in `tools/pg_phase_check.py` |
| P7 | `equipment/portablebattery` (6, with `forge/PortableBatteryItemImpl`), `electricity/portablebattery/forge/BatteryArmorLayerImpl`, `equipment/drill` (3), `saw` (2), `baton`, `zapper` (7), `thunder/LightningRodMovementBehaviour`, `equipment/ItemBoostUtils` and `forge/ItemBoostUtilsImpl`, `ZincArmorMaterial`, `ZincToolMaterial`, `utility/EntityProperties` and `forge/EntityPropertiesImpl`, packets `ZapProjectileS2CPacket`, `DrillSpeedS2CPacket`, `LightningSyncS2CPacket`, mixins `SheepMixin`, `forge/PlayerDrillMixin` | moved; changes in section 18 (24 of the 31 files changed; `DrillItem`, `PlayerDrillExtensions`, `DrillSpeedS2CPacket`, `ItemBoostUtilsImpl`, `PortableBatteryItemImpl`, `BatteryArmorLayerImpl`, `EntityPropertiesImpl` are byte-identical to the tag) |
| P7 | `PowerGrid` (worn battery tick, `registerMovementBehaviours`), `PowerGridClient` (zapper render handler), `forge/PowerGridImpl` (`commonSetup` P7 `enqueueWork`, Carry On: zap projectile), `forge/PowerGridClientImpl` (battery armor layer, render handler listeners), `forge/ForgeClientEvents` (drill and saw hand render), `ModdedBlocks`, `ModdedItems`, `ModdedBlockEntities`, `ModdedEntities`, `ModdedPackets` (4 appended), `ModdedPartialModels` (drill head, zapper cog), `ModdedDamageTypes.indirect`, `config/CElectricity` (B11 comment) | P7 parts added. `forge/ForgeEvents` and `forge/ForgeClientEvents` are now whole (upstream copies removed from `upstream/java`) |
| P7 | new (GPLv3) | `mixin/forge/DrillBreakActionMixin` (replaces upstream's 1.20 Forge `LeftClickBlock` action hook, section 18) |
| P7 | not ported, stay in `upstream/java` | `equipment/BoostingChipItem`, `BoostingChipRenderer`, `BoostRecipe` (the Integrated Circuit and boost recipes, P9) |
| P8 | `electricity/crt` (4), `electricity/modulardisplay` (16, with `modules/`), `electricity/bell` (3), `kinetics/plotter` (3), `kinetics/punchcard` (10, with `forge/PunchCardMenuImpl`, `forge/PunchCardReaderBlockEntityImpl`), `compat/cc` (9: `CCBridge` and the 8 peripherals), packets `SaveCardC2SPacket`, `DisplayBurnoutS2CPacket` | moved; changes in section 19 |
| P8 | `PowerGrid` (punch card reader substitute default), `forge/PowerGridImpl` (CC bridge when `computercraft` is loaded; forge punch card reader and menu constructors), `forge/ForgeEvents` (`recordInteractingItem`, new, GPLv3), `ModdedBlocks` (7), `ModdedItems` (3), `ModdedBlockEntities` (5), `ModdedMenus` (`punch_card`), `ModdedPackets` (2 appended), `ModdedPartialModels` (plotter pointer, paper, viewer, CRT background, punch card; `PAPER_SHIFT`), `utility/PlacementOverlay` (modular display text back; the upstream copy is deleted), ponder classes (section 19) | P8 parts added |
| P8 | not ported, stay in `upstream/java` | the Flywheel 1.0 `ShaftVisual` of the plotter and reader (E3; the renderers draw the shaft); `compat/HiddenItems` (REI) |
| P9a | `circuits/components` (all 60: 45, `properties/` 10 and `forge/` 5: `CircuitBoardModel`, `CircuitBoardModelLoader`, `UnbakedCircuitBoardModel`, `ComponentModelsImpl`, `ComponentRegistryImpl`), `circuits/schematic` (10 of 12), `circuits/circuitboard` (6 of 7), `electricity/wire/CircuitBoardEndpoint` (the P2 stub replaced by the full class), `network/packets/UpdateComponentBiPacket`, `utility/CustomValueSettingsScreen` | section 20 |
| P9a | `IElectricEntity.CircuitBuilder.setTo(BakedCircuit)`, `AbstractPowerGridRegistrate.component(...)`, `DebugItem` (board readout), `MultimeterItem` (board traces), `mixin/AirCurrentMixin` (board cooling branch), `ModdedBlocks`, `ModdedItems`, `ModdedBlockEntities`, `ModdedPackets`, `ModdedPartialModels`, `PowerGrid` (`Components.register`), `PowerGridClient` (potentiometer screen tick), `forge/PowerGridImpl` (registry, reload listener, sync), `forge/PowerGridClientImpl` (model loader, component models), `utility/GuiGraphics` (`renderOutline`), ponder (section 20.2) | section 20 |
| P9a | new (GPLv3) | `network/packets/ComponentItemsSyncS2CPacket`; `ComponentRegistry` server / client tables and `ReloadListener` (inside the upstream class, marked); `forge/PowerGridCreativeTab.fillItemList` (upstream's tab exclusions); `tools/pg_solver_test/.../BoardComponentTests` |
| P9a | not ported, stay in `upstream/java` | `circuits/editor` (12), `circuits/gui` (9), `circuits/schematic/CircuitSchematicItem`, `CircuitSchematicRender` (P9b); `circuits/circuitboard/IncompleteCircuitItem`, the arm mixins (P9c); `equipment/BoostingChipItem`, `BoostingChipRenderer`, `BoostRecipe`, `BeltDeployerMixin`, `RecipeApplierMixin` (P9d) |
| P9b | `circuits/editor` (12: the design table block, block entity and renderer; `AbstractCircuitDesignTableMenu`, `CircuitEditMenu`, the four menus `CircuitDesignTableMenu`, `CircuitDesignTableEditMenu`, `CircuitDesignTableLoadMenu`, `CircuitBoardEditMenu`; the three screens), `circuits/gui` (9), `circuits/schematic/CircuitSchematicItem`, `CircuitSchematicRender`, `base/IMultiScreenHandlerFactory`, `network/packets/ChangeScreenC2SPacket`, `SaveSchematicC2SPacket` |
| P9b | `CircuitSchematic.toItemStack`, `CircuitBoardBlock.use` (creative empty-hand edit menu), `ComponentFootprint` (colours from `CircuitSchematicRender` again), `compat/jei/PowerGridJEI.registerGuiHandlers` (ghost ingredients), `utility/GuiGraphics` (extends `GuiComponent`; `hLine`, `vLine`, `fillGradient`, `blitRepeating`, `renderTooltip`), `ModdedBlocks`, `ModdedItems`, `ModdedBlockEntities`, `ModdedMenus` (4), `ModdedPackets` (2, appended), `ModdedPartialModels` (`CIRCUIT_TABLE_GLOW`) |
| P9b | not ported, stay in `upstream/java` | `circuits/circuitboard/IncompleteCircuitItem`, `compat/jei/CircuitDesignCategory`, `CircuitAssemblyCategory`, `AnimatedMechanicalArm` (P9c), the boost classes (P9d) |
| P9c | `collections/ModdedFluids` (the forge `ModdedFluidsImpl` folded in, D2; both upstream copies deleted), `circuits/circuitboard/IncompleteCircuitItem`, `compat/jei/CircuitDesignCategory`, `CircuitAssemblyCategory`, `AnimatedMechanicalArm`, mixins `ArmBlockEntityMixin`, `forge/ArmInteractionPointMixin`, `BeltDeployerMixin` (schematic branch only; upstream copy deleted, P9d adds nothing) | section 22 |
| P9c | `PowerGrid` (`ModdedFluids.register` from 9.3), `ModdedItems` (4), `compat/jei/PowerGridJEI` (the two categories, catalysts, dummy recipes), `tools/pg_phase_check.py` (fluid asset check) | section 22 |
| P9c | partial, upstream copy kept in `upstream/java` | `mixin/RecipeApplierMixin` (schematic branch ported; the `BoostRecipe` branch is P9d) |
| P9d | `equipment/BoostingChipItem`, `BoostingChipRenderer`, `BoostRecipe`; `mixin/RecipeApplierMixin` (the `BoostRecipe` branch; the upstream copy and `upstream/java/.../mixin/` deleted) | section 23 |
| P9d | `PowerGrid.registerRecipes` (`boost_recipe` serializer), `ModdedItems` (2), `ElectroZapperItem` (the chip check; B29 tooltip), `ElectroBatonItem` (B29 tooltip), `mixin/forge/PlayerDrillMixin` (comment only: the boost branch was already in) | section 23 |
| P10 | `general/ceilingtile` (13: `CeilingBlock`, `CeilingTileBlock`, the lamp, connector, junction and solar blocks and block entities, `CeilingTileLampRenderer`, `CeilingTileSolarBlockCTBehaviour`), `electricity/light/string` (5: `StringLightBlock`, `StringLightCordEntity`, `StringLightCordItem`, `StringLightCordRecipe`, `StringLightCordRenderer`) | section 24 |
| P10 | `ModdedBlocks` (9), `ModdedItems` (`string_light_cord`), `ModdedEntities` (`string_light_cord`), `ModdedBlockEntities` (4), `ModdedPartialModels` (`COPPER_PLATING` CT shift, `CEILING_LIGHT`), `PowerGrid.registerRecipes` (`crafting_special_string_light_cord` serializer), `forge/PowerGridImpl` (Carry On entity blacklist), `WireItem` (the cord's rated lines), ponder (`ElectricInstructions.connectLightCord`, `WireScenes.stringLights`, `DeviceScenes.ceilingTile`, `CEILING_TILE_ATTACHMENTS` tag and its upstream uses); every remaining partial upstream copy deleted (37 files) | section 24 |
| P7 | advancements | removed for good: `SheepMixin` (SHEEP_CUT), `LightningRodMovementBehaviour.fire` (LIGHTNING for players within 16 blocks) |
The per-block config sections (`stressValues`, `thermal`, `resistance`) are filled by Registrate builder transforms
when blocks register, so they are empty at P0 and grow per phase; their keys exist only for registered blocks.

## 8. Build

- `./gradlew :backportedPowerGrid:build --offline`; jar `build/libs/backportedPowerGrid-<mod_version>.jar`.
- EJML 0.44.0 (`ejml-core`, `-ddense`, `-dsparse`, `-simple`, the four upstream nests) comes from Maven Central
  (cached since the first online build) and is nested in `META-INF/jarjar/` with `metadata.json` (Forge 43 loads plain
  library jars from jar-in-jar; the pack already does so for caffeine and jankson).
- MixinExtras is `compileOnly` (0.4.1 common, as Brewin'): the pack loads MixinExtras 0.5.4 through other mods, so it is
  not nested (plan said nest 0.3.5; Creative Common does not ship it either).
- Mixins: one config, `powergrid.mixins.json` (`defaultRequire: 1`), empty at P0 and P1. Since P2 the project uses
  `ce.forge-mod-refmap` (`refmap_name=powergrid.refmap.json` in `gradle.properties`) and the config names the refmap.
- `jar_archive/` keeps the first jar of each `mod_version`, so bump the port build number (`-ce-19.2-NN`) when a phase
  ships a new jar for testing.
- `tools/build_deploy.py` `IN_TREE_MODS` has the project as not required (the jar is not in `mods/`). No `mod_sides.md`
  row yet: the jar is not in the pack (add one at integration, power_grid.md section 6).

## 9. Notes for later phases

- P2 items from P0 and P1 are done: `ModdedBlocks` registers before `ModdedConfigs.register()`; `clientMirror` wired
  (section 11); `ElectricalNetwork.LOGGER`, `TransmissionLine implements ITransmissionLine`, `PROBLEM_SINK`,
  `circuits/thermal` and the six game-side `sim` classes moved.
- P3 and later: add each phase's entries to the P2 `collections/*` classes from their upstream copies (each id through
  `PortPhase.require`); add packets at the end of `ModdedPackets` (ids are ordinals; client and server use the same jar);
  re-add mixins to `powergrid.mixins.json` (done in P3: `EntityMixin`, `LightningAccessor`, `ValueSettingsScreenMixin`,
  plus `AirCurrentMixin`). Overheat callbacks that called `level.destroyBlock` upstream call
  `ThermalBehaviour.overheatBlock(level, pos)` instead (D4; done in P3 for the medium transformer and the winding, which
  is a P3 block); `ThermalBehaviour.explode` already routes there (battery). Display sources register in
  `ModdedDisplaySources` and are assigned with `AllDisplayBehaviours.assignBlock` in `onRegister` (Create 0.5.1 has no
  display source registry; battery done in P3, clutch P4). Screens use `utility/GuiGraphics` (extend it when a screen
  needs more of the 1.20 API). `SubstituteBlockEntityProvider` (device connector, FE inverter) and
  `RedstoneConverterRegistry.init` (`LifecycleEvent.SETUP`) are in `PowerGrid` since P3; P8 added the punch card reader
  to the provider before its `lock()`.
- P4 (from P3): done (section 14): generator speeds in the stress provider, housing branches of `WindingBlock.canConnect`,
  clutch in `RedstoneConverterRegistry.init` and `ModdedDisplaySources`, the generator block entities in the Carry On
  blacklist, `DebugItem` generator readouts; kinetic renderers draw their own shaft.
- P5 (ponder, from P4): done (section 15).
- P6 and later (from P5): each phase ports its scenes with its blocks. Per scene: move the method from the upstream
  scene class in `upstream/java` into the `src/main/java` copy (the extraction keeps upstream text; call sites need only
  `util.grid` / `util.select` / `util.vector` as fields and `new PowerGridSceneBuilder(builder)` at the top, which gives
  the Create 6 style `scene.world()` / `overlay()` / `effects()` accessors), register it in `PowerGridPonderScenes`
  through `wrap(...)` with its upstream components and tags, add the later-phase members to `PowerGridPonderTags`
  (the comments list them per phase), and lower the structure's entry in `phases.json` `resources` (or drop it to fall
  back to the `ponder/**` glob). Check the scene's structure palette and its code for blocks of a later phase first
  (section 15 has the method: `nbtscan` over the palette, the methods' `Modded*` references). P6 brings back: current
  gauge (and the `else` branch of `GaugeScenes.gauge`), power gauge, `wire/simple`, `wire/wire_loop`, `wire/cord`
  (the copper cord's only scene), the three switch scenes and `hv_breaker`, `generator/generator_3` (also for the
  carbon pile coil), plus the P6 device scenes; the `ElectricInstructions` solar panel methods; tag members (comments).
  P8: plotter, crt, punch cards, modular display, bell. P9: `CircuitScenes` (every `circuit/*`) and the
  `CIRCUIT_COMPONENTS` tag. P10: `wire/string_lights`, `ceiling_tile`, `connectLightCord`, the
  `CEILING_TILE_ATTACHMENTS` tag (also on the connector and cord junction scenes upstream).
- P6: done (sections 16, 17): the P6 mixins, lights checked against `clientMirror` (glow sync at power level changes).
  `AirCurrentMixin`'s circuit board branch still comes with P9.
- P7 (from P6): done (section 18): B11 comment fixed; the lightning rod movement behaviour registers in `commonSetup`
  `enqueueWork`; the drill and saw hand hooks are in `ForgeClientEvents`; the drill's left-click hook became
  `DrillBreakActionMixin` (Forge 1.19.2 has no break action on `LeftClickBlock`). No P7 ponder scene upstream.
- P8 (from P7): no P7 class is needed by P8. Packets append after `DRILL_SPEED` (ids are ordinals). `PlacementOverlay`
  still keeps the modular display text for P8 (upstream copy in `upstream/java`). The CC: Tweaked peripherals and the
  Create display sources go through `ModdedDisplaySources` (assign with `AllDisplayBehaviours.assignBlock` in
  `onRegister`, as the battery and clutch). The boost branch of `PlayerDrillMixin`, the boost tooltip and the
  `ItemBoostUtils.useBoost` calls are live code but inert until P9 brings the Integrated Circuit (`BoostingChipItem`) and
  the four boost recipes; P9 also restores the Electro-Zapper's "pass when the other hand holds a Boosting Chip" check
  (`ElectroZapperItem.use`, removed with a comment).
- P8: done (section 19). The CC: Tweaked bridge and the punch card reader substitute are in; display sources: none new
  in P8 (upstream's four, electric gauge, thermometer, battery, clutch, were ported in P2 to P4).
- P9 (from P8): the display module's own scene is `circuit/display_module` (`CircuitScenes::displayModule`, a circuit
  board scene with `ModularDisplayComponent`); `DisplayModuleType.getTranslationKey` returns the lang key now (section
  19), so `ModularDisplayComponent`'s module option must translate it the same way. The modular display
  (mechanical crafting) and punch card reader recipes need `pins`. Create 0.5.1's option formatter prefixes `create.` to
  `INamedIconOptions.getTranslationKey` (`ValueSettingsFormatter.ScrollOptionSettingsFormatter`): P8 overrides the
  modular display's board; the P4 clutch mode and the P6 solar bearing options use the plain board, so their labels may
  show `create.powergrid...` (look in CQ-PWG-19; same fix if so).
- P9: `assets/powergrid/atlases/blocks.json` (1.19.3+ atlas source list for `block/circuit_board_trace` and
  `block/circuit_board_pad`) does not ship; those sprites need a `TextureStitchEvent.Pre` on 1.19.2. Done in P9a
  differently: `UnbakedCircuitBoardModel.getMaterials` lists both (1.19.2 stitches a geometry's materials).
- P9a: done (section 20). The P8 hand-off items: the display module scene and `CIRCUIT_COMPONENTS` are in (20.2);
  `ModularDisplayComponent`'s module option is not translated anywhere in P9a (upstream's editor widget
  `EnumPropertyWidget` shows the enum constant names: `Component.literal(property.toString(value))`), so nothing to
  fix until P9b, which may translate `DisplayModuleType.getTranslationKey()` there if it wants lang text; the clutch
  mode and solar bearing labels wait for CQ-PWG-19's result (no change in P9a). `pins` recipes ship.
- P9b to P9d (from P9a): section 20.3.
- Ponder `.nbt` files are phased per scene in `phases.json` (`resources`); P5 checked the 36 it ships (section 15).

## 10. Solver harness (P1 gate)

`tools/test_power_grid_solver.sh` (no game; run `./gradlew :backportedPowerGrid:build --offline` first; `-v` prints the
full output). It compiles against `build/classes/java/main` with the mapped Forge jar and libraries from
`tools/java_test_cp.sh`, the four EJML jars from the Gradle cache, Create 0.5.1.i (`Pair`, `ConfigBase`) and the
`FMLEnvironment` stub, then runs:

- upstream's solver tests from `upstream/test` (20 classes; `PerformanceTest` left out: it runs `NativeMNA`) and,
  since P8, `other/EquationParserTests` (3 tests of the custom display expression parser, `info/customdisplay/Expression`),
  through a small JUnit stand-in (`tools/pg_solver_test/org/junit`; JUnit is not in the offline Gradle cache);
- `tools/pg_solver_test/.../PortGateTests` (GPLv3): the plan's five circuits, each on upstream's plain test network and
  on the network the game builds (`GraphedElectricalNetwork`, G_MIN on, backend from `CSolver.SolverBackend.JAVA::create`),
  structure and value changes after the first solve (gate 8), the 1000-node ladder and upstream's 32 x 32 grid timings.
  The runner builds the port's server and common configs from their specs (defaults), as `GraphedElectricalNetwork` reads
  `seriesWireOptimization`.
- since P9a, `tools/pg_solver_test/.../BoardComponentTests` (GPLv3): the non-linear models as the circuit board builds
  them (section 10.1).

Known upstream test failures are listed with their reason in `tools/pg_solver_test/known_failures.txt` and reported as
`KNOWN` without failing the run (a listed test that starts passing prints `NOW PASSES`). Six at tag v0.6.2: three stale
or ill-posed tests (`SolverTests.testGenerator` never sets the generator's field provider; `StructureChangeTests.testMerge`
compares a float literal with a double without a delta; `CapacitorTest.testMultiTick` expects 0.632 +- 0.01 after 1.05 s
of backward Euler, which is 0.6421), two tube current tolerances (`ElectronTubeTest`, 0.15 % off against a 1e-6 A
delta) and the split transmission-line test (experimental `splittingTransmissionLines`, default off). None changes with
the port's edits (values identical before and after; the ported `sim` differs from the tag only in the rows of section 7).

**Result 2026-11-13: 75 passed (8 gate tests, 67 of upstream's 73), 0 failed, 6 known.** Gate values (plain and game network identical):

| Gate | Circuit | Result |
|---|---|---|
| 1 | 10 V, 1 k / 1 k divider | 5.000000000 V, 5.000000000 mA |
| 2 | RC: 1 V, 1 ohm, 1 F, 20 ticks of 0.05 s (one RC) | 0.62306 V against 0.63212: 1.43 % (backward Euler; within the 2 % gate) |
| 3 | ideal transformer 1:10, 10 V primary, 100 ohm load | 100.000000 V, 1 A load, 10 A primary |
| 4 | diode (circuit-board diode parameters), 5 V through 1 k | forward drop 0.6165 V at 4.38 mA, converged in 3 ticks |
| 5 | floating: two-terminal 5 V source over 1 k + 1 k, plus a source-less island | finite, exact (5 V, 2.5 V, -2.5 mA, island 0 V), converged. Upstream's plain network without G_MIN also stays finite on this circuit (info only) |
| 8 | after the first solve: add, remove and re-value a wire to ground; add and remove a wire between nodes | all equal a fresh solve (fails on both networks without fix B5) |

**Timings** (this machine, one JVM after 3 warm-up rounds; microseconds per `calculate()`; 200 solves each). Ladder: 1000
nodes, 1 ohm source, 0.1 ohm series, 1000 ohm shunt per node; max node error against a tridiagonal (Thomas) reference
2.3e-8 V.

| Network | Build | First solve | Unchanged | Source voltage step | Refactorize (one wire's resistance alternated) |
|---|---|---|---|---|---|
| ladder 1000, plain | 0.5 to 0.8 ms | 8.1 to 9.1 ms | 14.9 us (max 29) | 53.8 us (max 86) | 108.8 us (max 128) |
| ladder 1000, game | 1.1 ms | 8.7 to 9.6 ms | 15.9 us (max 25) | 55.7 us (max 74) | 108.1 us (max 134) |
| grid 32 x 32 (1024 nodes, mesh; upstream `PerformanceTest`, Java), game type without G_MIN | | | | | 2.7 to 3.3 ms mean, max 13 to 14 ms (source flip and refactorize every solve) |

Reading for SQ-PWG-04: a 1000-node linear grid costs about 0.1 ms per tick when something in it changes and 0.015 ms when
nothing does; the first solve after a structure change (full factorization) about 9 ms. A meshed grid of the same node
count costs about 30 times more per change (LU fill-in), so the in-game row should record both shapes if it can.
Upstream's own 32 x 32 grid has 1.0 density, so it is the dense end.

Limits found while writing the gates (not changed, not game paths): `PNJunctionWire` (diode) does not accept a null
(ground) node (NPE in `addResidual`); every diode in the game sits between two nodes, so gate 4 puts the cathode on a 0 V
source node.

### 10.1 Board component models (P9a)

`BoardComponentTests` makes the wires each board component's `bake()` makes, with the component's default properties
(copied: the component classes do not load outside the game, their static init reaches `PowerGrid`'s Architectury
`DeferredRegister`s and Create's SRG-named lang helpers), on the game's network type with the game's tube step limits
(`triodeLim*` 0.5, as `WorldNetworks.createNetwork` sets from the solver config; the class default is 0.8). A hot tube
gets the saturation current or emission its thermal callback sets at 1400 C, a cold one zero. "Settled" means the
solver reports convergence and the watched nodes then stay within 1 uV for 60 ticks; the tests require settling within
40 ticks (2 s). **Result 2026-11-13: 6 passed** (84 in the run: 8 gate, 6 board, 70 of upstream's 76 with 6 known):

| Test | Circuit | Result |
|---|---|---|
| diode | `DiodeComponent` (PNJunctionWire 5.47e-9, 0.075 ohm, 22 C, n 1.783, breakdown 1000 V): 5 V / 1 k forward, -5 V reverse | 0.6165 V at 4.38 mA (1 tick); reverse 0.99 uA |
| NPN / PNP | `BJTWire` (Is 5.47e-12, gain 20, Rs 0.1), common emitter 5 V, Rc 1 k; Rb 100 k / 4.7 k | Vbe 0.474 V, Ic 0.909 mA, Ib 45.3 uA, Ic/Ib 20.09, Vce 4.09 V; saturated Vce 0.034 V (both types, mirrored) |
| triode | `ElectronTubeComponent` defaults (mu 5, kg 13600, kp 600, kvb 300, ex 1.5, Is 0.1 A); 250 V / 10 k, grid 0 and -4 V; cold | 14.36 mA (Va 106.4 V, 3 ticks); 13.05 mA (Va 119.5 V, **33 ticks**: B18); cold 2.5 uA |
| pentode | `PentodeComponent` defaults (mu 8, kg1 6000, kg2 4500, kp 48, kvb 12, ex 1.35), screen 150 V | 21.46 mA at grid 0 (11 ticks), 17.67 mA at -4 V (8 ticks) |
| thyratron | `ThyratronComponent` defaults (mu 20, Vb 60 V, Vh 12 V, Ih 8.3 mA, g 50, grid leak 1 Mohm), 200 V / 2 k | grid -30 V: off (Va 199.6 V); grid 0 V: fires, Va 12.00 V |
| regulator tube | `RegulatorTubeComponent` defaults (Vh 60 V, Vb 84 V, Ih 3.3 mA, g 0.025), 5 k from 150 V and 200 V | 60.71 V and 61.11 V (2 ticks) |

B18 (power_grid.md 3.5, not changed): the tube wires limit how far their evaluation point moves per Newton iteration
(`min(0.5, |step|) * triodeLim*`, 0.25 V with the default config) and the residual is computed at that lagged point, so a
triode that starts in cut-off with a large anode step reports `isConverged()` at once while its operating point stalls
and then walks for about 35 ticks (1.7 s); the thermal units skip heat while a source is not converged, so heating is
counted only as the walk allows. The P1 gate 4 (diode) and upstream's tube tests are unchanged.

## 11. Phase P2: behaviour changes and new code

| Area | Change |
|---|---|
| Overheated Component (D4, new block `powergrid:overheated_component`, no item) | `ThermalBehaviour.explode` (every overheat path: the default `OVERHEAT_EXPLOSION` flag) now calls `ThermalBehaviour.overheatBlock` -> `OverheatedComponentBlock.overheat(level, pos)`: saves the device's block state and full block entity NBT, replaces the block (the device's normal removal runs: it leaves its network, attached wires drop as on a break), plays a fire-extinguish sound and smoke. No explosion, no block break, whatever `explosiveDeconstruction` says (key kept, comment says unused). The block entity (`SmartBlockEntity`, synced) renders the original model with destroy stage 7 (`OverheatedComponentRenderer`), takes the original's shape, shows the block name, state and stored NBT keys on goggles (3 lines, 8 when sneaking), gives nothing on pick-block, and on break drops the original block's item with chance `electricity.overheatedDropChance` (new server key, default 0.25). Blockstate and particle-only model added; 5 lang keys added to `en_us.json` (`block.powergrid.overheated_component`, `powergrid.gui.overheated_component.*`). Not repairable (H14 default) |
| Burnt wires (H4) | upstream already removes a burnt wire without a drop (`BaseWireEntity.tick`: `discard()` 5 ticks after it overheats, sound `wire_burned`); kept as is |
| Server-values-only switch (H5, D14) | `clientMirror` (common config, read on the server) gates the state stream in `WorldNetworks.postTick` (every 5 ticks, thinned by distance per player). Off: clients get values only from the full state sync (`fullStateSynchronizationInterval`, default 100 ticks: every network block entity's `sendData`, which carries the node values). Gauges, energy meter, terminal readouts and the multimeter follow; wire temperature and glow are server values in both modes (synced entity data); lights are P6. Nothing is sent to the client about the switch |
| Wire types (`powergrid:wire_types`) | 1.20 datapack registry replaced by `WireTypeReloadListener` (same folder `data/<ns>/powergrid/wire_types`, same codec) and `WireTypesSyncS2CPacket` (sent from `OnDatapackSyncEvent`: on join and after `/reload`). `WireRegistry` keeps a server and a client table (one JVM in singleplayer); `forItem(level, item)` picks by `level.isClientSide` |
| Damage sources | `ModdedDamageTypes` constants are 1.19.2 `DamageSource`s with upstream's message ids (`electrocution`, `live_wire_cutting`, `overloaded_machine`, `zap`, `spinning_rotor`, `acid`), exhaustion 0 as upstream's types, armor applies (upstream has no damage type tags). `MachineOverloadDamageSource` keeps the machine name in the death message |
| Chunk tickets | Forge 43.3.9 has `ChunkTicketLevelUpdatedEvent`, so `ForgeEvents.chunkTicketUpdate` stays; `ChunkLevel.isBlockTicking` is `level <= 32` |
| Ponder level checks | `instanceof PonderLevel` (common code upstream) -> `PonderLevels.isPonder` (client-side only check of Create 0.5.1's client class `PonderWorld`) |
| Render types | `ModdedRenderLayers` extends `RenderType` to reach the protected 1.19.2 shards (upstream: access wideners) |
| Display sources | `electric_gauge` and `thermometer` registered with `AllDisplayBehaviours.register`, assigned to the gauges and the thermometer |
| Creative tab | Registrate default tab `PowerGridCreativeTab` (copper ingot icon until P4); item order is registration order (upstream `ItemDisplay` sorted 3D models first) |
| Mixins (11) | common: `BlockStateBaseMixin`, `LevelEntitiesAccessor`, `PlayerMixin`, `SoundEntryBuilderMixin`, `forge.EntityTrackerAccessor`, `forge.ItemAccessor`, `forge.ThreadedAnvilChunkStorageAccessor`; client: `BlueprintOverlayMixin`, `BlueprintOverlayRendererAccessor`, `ComplexEntityRaycastMixin`, `RenderBuffersAccessor`. Targets checked against 1.19.2 and Create 0.5.1.i (`BlueprintOverlayRenderer` fields `ingredients`, `active`, `empty`, `noOutput` and `tick`; `SoundEntryBuilder.build` puts into `AllSoundEvents.ALL`). `EntityMixin` moved to P3 |
| Other 1.19.2 changes | `BaseWireEntity.getAddEntityPacket` returns `new ClientboundAddEntityPacket(this)` (abstract on 1.19.2); `TooltipWidget` is a Create `AbstractSimiWidget` (1.19.2 widgets have no `Tooltip`); `MultimeterDataC2SPacket` sends the point as three floats; `CustomDisplayScreen` / `EnergyMeterScreen` draw through `GuiGraphics` over the `PoseStack`; items in screens are drawn through the model-view stack |
| Not ported in P2 (later phases) | particles (`ModdedParticles`, sparks: P3 and later), keys are all registered (the circuit editor keys are unused until P9) |
| Wires on an overheated device (scratch run r1113) | when a device turns into an Overheated Component, the wires attached to it drop as items (its normal removal runs). Kept: upstream's `explode` also broke the block, which dropped its wires the same way |
| Fixes after the P2 scratch run r1113 (shipped in `-04`) | fix B8: `PerformanceCounter.getTimestamp` returns "no measurement yet" before the first measurement (upstream: NullPointerException from `/powergrid performance get <counter>`, SQ-PWG-13). Probe and row fixes, no code: the current gauge's own 0.05 ohm series resistance (config `resistance.current_gauge`) is now in the loop reads (`Rgauge`, SQ-PWG-14, -16, -19); `cut` picks one wire by its endpoints (SQ-PWG-15); `overheat` builds its loop without a current gauge (the 35 W gauge overheated before the wire) and reads the current from the resistor (`ohm`), reads report `overheated <part>` instead of failing (SQ-PWG-17); `chunk` reports a chunk's load and ticking state, reads never load a chunk, and SQ-PWG-21 puts the resistor three chunks from the only forced chunk; SQ-PWG-18 sets `overheatedDropChance` explicitly (default 0.25, owner decision pending) |

## 12. Chunk handling (H9, read before porting `WorldNetworks`)

How upstream 0.6.2 handles a network across unloaded chunks (unchanged in the port):

- **Block entities leave block ticking** (ticket level above 32, `ChunkTicketLevelUpdatedEvent` ->
  `ElectricBehaviour.handleTicketChange`): every electric behaviour in the chunk is **paused**: its internal wires and
  nodes leave the network (the device stops acting), its external terminal nodes stay. Back to ticking: `unpause` (also
  from `lazyTick` if still paused).
- **Chunk unload** (`SmartBlockEntity.onChunkUnloaded` then `invalidate` -> `ElectricBehaviour.unload`): pause, then
  `WorldNetworks.nodeHolderUnloaded`: a terminal node with exactly one transmission line and one connection is removed,
  and the walk continues along that line (dead-end lines are unresolved); a node joining several lines is kept, so a
  junction in an unloaded chunk still carries power between loaded parts.
- **Wires** are entities, but their electrical part is a `TransmissionLinePart` saved in the level's saved data
  (`powergrid_electric_network_data`, `WorldNetworks` NBT), so a line through unloaded chunks keeps conducting as one
  lumped resistance between its loaded ends. When a wire entity unloads, its part records the entity's chunk
  (`bounty`); when that chunk loads (`ChunkEvent.Load` -> `chunkLoaded`) the entity must reappear within 10 ticks, or the
  whole line is removed (a wire lost while unloaded does not keep conducting).
- Effect for players: the loaded part of a grid keeps running; devices in unloaded or non-ticking chunks drop out (open
  terminals); wires between loaded parts keep working. Forced chunks (Create Power Loader, `forceload`) tick, so their
  devices keep working. Test: SQ-PWG-21 (PWG-26).

## 13. Phase P3: behaviour changes and new code

| Area | Change |
|---|---|
| Overheat (D4) | medium transformer: its overheat callback turns all four blocks into Overheated Components (upstream: `destroyBlock` without drops); the parts have no item, so breaking them drops nothing (upstream dropped nothing either). Winding: every block of the coil turns into an Overheated Component (same). Multiblock battery: `ThermalBehaviour.explode` on a random block of the battery (unchanged code; it routes to `overheatBlock`) |
| Kinetics in P3 | `ElectricKineticBlockEntity` moved forward from P4 (the HV switch and HV breaker are shaft driven). Stress: the Create 0.5.1 provider for the `powergrid` namespace is registered in `ModdedConfigs.register` (P0 had left it for P4); the HV switch and HV breaker have impact 2 SU (`stressValues` keys `hv_switch`, `hv_breaker`). `getGeneratedRPM` returns null until P4 |
| Flywheel visuals | `HvSwitchVisual` and the HV breaker's `ShaftVisual` (Flywheel 1.0) are not ported; `HvSwitchRenderer` and `HvBreakerRenderer` draw the shaft themselves with `renderRotatingBuffer` (Create 0.5.1's `KineticBlockEntityRenderer.renderSafe` skips the shaft under Flywheel instancing) and always draw the rod / signal flags |
| Device cooling | `AirCurrentMixin` moved forward from P6 (gate PWG-14 needs encased fans to cool devices); the circuit board branch (P9) is left out. Targets `AirCurrent.rebuild` HEAD and TAIL, fields `source`, `direction`, method `getLimit` (same on Create 0.5.1.i; no pack mixin touches `AirCurrent`) |
| Winding | the `winding` block is P3 (placed by the copper coil on transformers); `WindingBlock.canConnect` has no generator housing branches until P4 (section 9) |
| Magnetization | recipe type and serializer `powergrid:magnetization` registered by `PowerGrid.registerRecipes` (Architectury `DeferredRegister` on `Registry.RECIPE_SERIALIZER_REGISTRY` / `RECIPE_TYPE_REGISTRY`). `MagnetizingRecipe.getJEISubCategory` has Create 0.5.1's signature (supplier of a sub-category), null as upstream (no assembly uses a magnetizing step). `ElectromagnetBlockEntity` uses 0.5.1 `RecipeApplier.applyRecipeOn(stack, recipe)` (Create 6 adds a level and a remainder flag; magnetizing recipes have no remainders). Lightning: `EntityMixin` (`Entity.thunderHit` HEAD) turns one andesite alloy of a struck item entity into a magnet with chance `lightningMagnetizationChance` (0.05) and protects alloy and magnets from the strike |
| JEI | `PowerGridJEI` registers the magnetizing category (electromagnet catalyst) and the mystery conversion (andesite alloy to magnet); the circuit design / assembly categories and the design table ghost handler return with P9. `AnimatedMagnet` and `MagnetizingCategory` draw on a `PoseStack` (JEI 11), tooltip through `addTooltipCallback` |
| Transformer winding screen | `ValueSettingsScreenMixin` retargeted: `renderWindow(PoseStack,IIF)` (remap off), the bar cap before the third `Font.draw` (the row label, as Create 6's third `drawString`), the milestone overlay after the second `AllGuiTextures.render` (the plain milestone); locals `x`, `y`, `milestoneX`, `milestone` by name (in the 0.5.1.i local variable table). `TransformerWindingScreen` uses the 0.5.1 constructor (no net id; `pos` null is read only by the overridden `saveAndClose`) |
| FE bridge | upstream rates kept: `forgeEnergyPerWatt` 10, `forgeEnergyPerVolt` 2, buffer 20000. Code reading: the inverter takes `ceil(10 x P)` FE every tick (N6: FE per tick per watt; SQ-PWG-27 measures it). The forge `FEInverterBlockEntityImpl` replaces the common block entity through `SubstituteBlockEntityProvider` (registered in `PowerGridImpl` before `PowerGrid.init`; defaults for the device connector and inverter in `PowerGrid.register`, then locked). The device connector's TFMG proxy branch is removed (TFMG compat not ported) |
| Redstone converter | `RedstoneConverterRegistry` uses a `ConcurrentHashMap` (Create 6's `SimpleRegistry` is not in 0.5.1); `init` runs on `LifecycleEvent.SETUP`; the generator clutch entry returns with P4 |
| Battery | `BatteryBlockEntity` implements Create 0.5.1's `ThresholdSwitchObservable.getPercent` (0 to 100; Create 6 reads max / min / current value, kept as plain methods). Battery display source registered (`ModdedDisplaySources.BATTERY`) and assigned in `onRegister`; connected textures through `CreateRegistrate.connectedTextures(BatteryCTBehaviour::new)`. The multiplace check for a symmetry wand scans the hotbar (Create 6's `SymmetryWandItem.presentInHotbar` is not in 0.5.1) |
| Nether transformer | Create 6 `PortalTrackProvider.getOtherSide` -> Create 0.5.1 `AllPortalTracks.getOtherSide` (`Pair<ServerLevel, BlockFace>`). The pack has Immersive Portals, which replaces vanilla portals; CQ-PWG-11 checks the pairing |
| Grounding rod | fix B6: the touch damage near a grounding rod now falls with distance (upstream hurt with the squared distance, so far entities took more). Damage source: `GroundingRodDamageSource` extends the port's `PowerGridDamageSource` with the `zap` message id (upstream: a damage type holder) |
| Config | fix B7: `lightningMagnetizationChance` comment says andesite alloy (upstream: iron ingot; the mixin checks andesite alloy) |
| Particles | `ModdedParticles` ported whole (`magnetization`, `spark`, `zap`; Architectury `DeferredRegister`), factories registered on `RegisterParticleProvidersEvent` (`PowerGridClientImpl`) |
| Other 1.19.2 changes | `BlockPos.getCenter` -> `Vec3.atCenterOf`; `Entity.onGround()` -> `isOnGround()`; `BlockState.canBeReplaced()` -> `getMaterial().isReplaceable()`; `copyWithCount` -> `ItemHandlerHelper.copyStackWithSize`; `Direction.fromDelta` -> `fromNormal`; `Level.playLocalSound(BlockPos, ...)` -> the x, y, z overload at the block centre; `Vec3.offsetRandom` and `Mth.lerpInt` -> local helpers with the same formula; joml vectors -> `Vec3` math (spark particles) and plain doubles (device connector check shape); static `IWrenchable.playRemoveSound` / `playRotateSound` -> the `AllSoundEvents` calls their 0.5.1 defaults make; `LootParams.Builder` -> `LootContext.Builder`; `BuiltInRegistries` -> `Registry`; SuperByteBuffer `center` / `uncenter` / `rotateYDegrees` -> `centre` / `unCentre` / `rotateY`, `rotateCentered(angle, axis)` -> `rotateCentered(axis, angle)`. Item tags come from the shipped tag files (no Registrate `.tag` calls), as in P2. Carry On blacklist: winding, medium transformer, HV switch, device connector, battery, carbon pile and its coil |
| Restored in partial classes | `PlacementOverlay` transformer winding text (first provider, as upstream) and switch text; `DebugItem` winding dump; client ticks `WindingPreview.tick` and `TransformerWindingScreen.clientTick` |

## 14. Phase P4: behaviour changes and new code

| Area | Change |
|---|---|
| Stress values | Create 6 `BlockStressValues.setGeneratorSpeed(value, mayGenerateLess)` (motors 256, servo 32) -> `CStress.setGeneratorSpeed` (GPLv3 addition): kept per block id and returned by the stress provider's `getGeneratedRPM` as a Create 0.5.1 `Couple` (`0..value` when it may generate less, as Create's creative motor). Create 0.5.1 reads it only for the item tooltip (capacity x RPM, "up to"). Impacts and capacities as upstream: motors 64 SU per RPM capacity, servo 32, clutch 32 impact, induction rotor 32, large induction rotor 128, commutators 8, variac and rheostat 0. `BlockStressValues.getCapacity` / `getImpact` calls -> the 0.5.1 class (`content.kinetics.BlockStressValues`), same per-block signature |
| Generator coupling (checked against Create 0.5.1 speed propagation) | no change needed. The clutch couples only through Create's stress model: generator mode is a stress consumer whose impact is the sum of the assembly's impacts x (15 - redstone) / 15; the rotor runs its own PD loop (`rotorKp` 0.85, `rotorKd` 0.002) toward the clutch's theoretical speed with the force capped at `torqueForStress` x applied stress x pi / 30, and gives no torque back to Create (the applied stress stays the same under electrical load; only the clutch `load`, display source and comparator output rise). Motor mode is a `GeneratingKineticBlockEntity` source with speed = the rotor's RPM (lazy tick) and capacity = the same impact sum x coupling. Create 0.5.1's `KineticNetwork` (capacity and stress x abs(speed)), `RotationPropagator`, `GeneratingKineticBlockEntity.updateGeneratedRotation` / `applyNewSpeed` (source conflicts pop the weaker block) and `KineticBlockEntity.calculateStressApplied` / `calculateAddedStressCapacity` (fields `lastStressApplied`, `lastCapacityProvided`) behave as Create 6's on these paths. The motors' flicker workaround writes Create's private `flickerTally` through `KineticBlockEntityAccessor` (`remap = false`; field name the same in 0.5.1.i). No Create 6 rotation API is used anywhere in P4 |
| Damage source `spinning_rotor` | `ModdedDamageTypes.SPINNING_ROTOR` (P2 enum, message id `spinning_rotor`, death message `death.attack.spinning_rotor`: "was killed in a generator accident"); `RotorBehaviour.damageCalc` unchanged (radius 0.55 induction rotor, 1.3 large induction rotor, 0 commutator and clutch; config `dangerousGenerators`). 1.20 `Entity.addDeltaMovement(v)` -> `setDeltaMovement(getDeltaMovement().add(v))` (same body) |
| Renderers (Flywheel) | the five Flywheel 1.0 visuals are not ported (E3). `RotorRenderer` and `CommutatorRenderer` always draw the rotor (upstream skipped all but the large rotor when Flywheel 1.0 was on; rotors are `ENTITYBLOCK_ANIMATED`, not kinetic block entities). `GeneratorClutchRenderer`, `TunedBlockRenderer` (variac, rheostat), `ElectricMotorRenderer` (both motors) and `ServoRenderer` draw their shaft with `renderRotatingBuffer` (Create 0.5.1's `KineticBlockEntityRenderer.renderSafe` returns early under Flywheel instancing, and these blocks have no instance), as the P3 HV switch. `AllPartialModels.SHAFT` (Create 6) -> `CachedBufferer.block(KINETIC_BLOCK, shaft(Y))` (the same shaft model). SuperByteBuffer: `rotateCentered(angle, dir)` -> `rotateCentered(dir, angle)`, `center` / `uncenter` -> `centre` / `unCentre`, `rotateZ(radians)` -> `rotateZRadians` (Flywheel 0.6 `rotateZ` takes degrees) |
| Conductive casing | `CasingBlock` with `EncasedCTBehaviour` and casing connectivity on the 0.5.1 classes (same API); CT shift `ModdedPartialModels.CONDUCTIVE_CASING` (0.5.1 `CTSpriteShifter.getCT`) |
| Display source and redstone | clutch display source registered in `AllDisplayBehaviours` and assigned in `onRegister` (as the battery in P3); `ClutchDisplaySource` uses the port's `Lang` and Create 0.5.1 `Lang.translate("generic.unit.rpm")` (Create 6 `CreateLang`). `RedstoneConverterRegistry.registerUnifiedBlock(GENERATOR_CLUTCH)` |
| Winding and housings | `WindingBlock.canConnect` housing branches restored (qualified `GeneratorHousing.HORIZONTAL_FACING` / `UP` instead of static imports). Housings: unused `SolarPanelBlock` import dropped; `BlockState.canBeReplaced()` -> `getMaterial().isReplaceable()` in both placement helpers |
| Commutator sparks | 1.20 `BlockPos.getCenter` -> `Vec3.atCenterOf`; `Vec3.offsetRandom` -> local helper with the same formula (as P3) |
| Servo | **fix B9** (power_grid.md 3.5): upstream committed a new target angle even when the coil power gave a speed of 0 RPM (angle marked reached without turning; `rotation / convertToAngular(0)` overflowed the tick count), and kept the coil at the moving resistance (`on`, 12.8 ohm) after a move until the block entity reloaded. The port waits for power before committing and returns to `idle` (64 ohm) when the move ends |
| Rotor overspeed | unchanged: above `rotorRPMMax` (272) for 5 ticks the assembly's controller block is destroyed without drops. Not an overheat (D4 does not cover it); Create caps shafts at 256 RPM, so only a motor-mode or unbalanced electrical drive reaches it |
| Recipes | 25 recipes now load (47 in the jar): the motors, generator parts, rheostat, variac, servo, conductive casing (Create 0.5.1 `item_application`, same JSON) and the P2 / P3 recipes that waited for `conductive_casing` (the 31 cross-phase overrides are unchanged). Still uncraftable: the generator commutator (mechanical crafting needs `pins`, override to P9; the vertical / horizontal conversions load) and, in practice, the servo and the multimeter (they need `electrical_gizmo`, whose only recipe, a sequenced assembly, is P9) |
| Creative tab | icon is the Electric Motor, as upstream |

## 15. Phase P5: ponder

| Area | Change |
|---|---|
| Ponder base | Create 6 ponder library (`net.createmod.ponder`, Catnip) -> Create 0.5.1 `com.simibubi.create.foundation.ponder`. `SceneBuilder` / `SceneBuildingUtil` are classes with public fields there (`scene.world`, `util.grid`), so `PowerGridSceneBuilder` is a wrapper (upstream extends `CreateSceneBuilder`) with the Create 6 accessors `world()`, `overlay()`, `effects()`, `special()`, `debug()` and the plain calls (`title`, `idle`, `markAsFinished`, ...); scenes that took a plain `SceneBuilder` upstream are wrapped at their first line, so their calls are unchanged; `util.grid()` / `select()` / `vector()` -> fields. `ElementLinkImpl` -> 0.5.1 `ElementLink`; `PonderElementBase` / the `PonderElement` interface -> the 0.5.1 `PonderElement` class; `AnimatedSceneElementBase.renderLast(..., GuiGraphics, ...)` -> `AnimatedSceneElement.renderLast(..., PoseStack, ...)`; `PonderLevel` -> `PonderWorld` |
| Create 6 calls without a 0.5.1 twin | `showControls(Vec3, Pointing, int)` returning a chainable element: the wrapper builds a 0.5.1 `InputWindowElement`, schedules a `ShowInputInstruction` with it and returns it (same `rightClick` / `withItem` chain); `showOutlineWithText` -> `showSelectionWithText`; `effects().simpleParticleEmitter` -> `EmitParticlesInstruction.Emitter.simple`; `setNextUpEnabled` (Create 6 "next up" list) is a no-op; `new ItemStack(ItemEntry)` -> `entry.get()` / `asStack()` (Registrate 1.19 entries are not `ItemLike`) |
| Registration | no `PonderPlugin` on 0.5.1: `PowerGridPonderPlugin.register` runs from `FMLClientSetupEvent.enqueueWork` (main thread, after Create's own `PonderIndex`), only on the client and only at `port_phase` >= 5. Scenes through `PonderRegistrationHelper("powergrid")` with upstream's components, structure paths, scene ids and tags; text keys are upstream's (`powergrid.ponder.<scene>.header` / `text_N`, same numbering on 0.5.1, checked against the shipped lang for all 36 scenes), shared text `powergrid.ponder.shared.gauge_*`, tags `powergrid.ponder.tag.*` (lang ships whole; `defaultLang` / `registerShared` keep upstream's English for Create's ponder editing mode) |
| Level restore hook | upstream's `onPonderLevelRestore` (unpause every electric behaviour after the ponder level restores, so hidden parts such as the creative source under the base plate join the circuit) has no 0.5.1 hook: `PowerGridPonderScenes.wrap` puts it as the first instruction of every Power Grid storyboard (0.5.1 `PonderScene.begin` restores the level, then runs the instructions from the start, also on rewind) |
| Network ticking | `ElectricityTickInstruction.tick` does not call `TickingInstruction.tick`, as upstream: a `tickFor(n)` never completes and keeps ticking the ponder level's networks to the end of the scene, one more ticker per `tickFor`. The Create 6 ponder `TickingInstruction` counts down in `tick()` like 0.5.1's, so this is what upstream plays; kept, not fixed (CQ-PWG-14 looks at the scenes) |
| Tags | `generator_assembly`, `electric_relays`, `electric_devices` registered (0.5.1 `PonderTag` objects, `PonderRegistry.TAGS.forTag`); P2 to P4 members as upstream, plus Create's `KINETIC_APPLIANCES` (clutch), `KINETIC_SOURCES` (motors, servo), `DISPLAY_SOURCES` (voltage, current and power gauge, clutch, battery). `circuit_components` (P9) and `ceiling_tile_attachments` (P10) and later members wait for their phase |
| Scenes (36) | gauges: `voltage_gauge`, `energy_meter`; generator: `generator_winding`, `parallel_generator_winding`, `generator_1`, `generator_2`, `generator_clutch`, `generator_housing`, `generator_rotor`; wire: `insulated_wire`, `wire_voltage_drop`, `wire_connector`, `grounding`, `improved_grounding`, `cord_junction`, `cord_socket`; relays: `transformer_sizes`, `transformer_winding`, `transformer_nether`, `variac`, `rheostat`, `power_resistor`, `carbon_pile`, `device_connector`, `hv_switch`, `redstone_converter`, `contactor`, `contactor_stack`, `fuse`; devices: `electric_motor`, `constant_speed_motor`, `servo`, `electromagnet`, `battery`; magnet: `thunder_magnet`, `lightning_attractor` (also on `minecraft:lightning_rod`) |
| Scenes left for later phases | a scene waits when its structure or its code places a later-phase block (rule: no missing block in a scene, gate PWG-32). Of the plan's P5 list: `current_gauge` (heating coils, P6), `power_gauge`, `switch` (LV switch, LV button, MV switch), `hv_breaker`, `wire/simple`, `wire/wire_loop` (light fixture, P6), `wire/cord` (basin heater, P6; the copper cord has no other scene), `generator/generator_3` (basin heater, P6; it is also the carbon pile coil's second scene), `wire/string_lights` (P10). Every P6+ scene waits with its blocks (section 9 lists them) |
| Ponder structures | upstream's 81 files are 1.20.1 saves (`DataVersion` 3465; 1.19.2 is 3120). Create 0.5.1 loads them with `StructureTemplate.load` straight from the jar (`PonderRegistry.loadSchematic`), no data fixer: a block id unknown to the game would become air, an unknown property or value would be dropped silently, nothing throws. Offline check of the 36 that ship (palette names against the pack's item dump, Create 0.5.1 and the port's registries; properties against the 1.19.2 / Create 0.5.1 / port blockstate definitions and block classes): every block exists on 1.19.2 / Create 0.5.1 / the port, every property exists. Upstream-same quirks (the files carry them on 1.20.1 too, nothing to port): `powergrid:creative_voltage_source` saved with `axis` in 6 files (the block has `facing` since; it loads with its default facing, as upstream), `power_resistor.axis_along_first` stored as a number in `wire/voltage_drop` (ignored, default, as upstream). Entities in the files: hanging wires (`energy_meter`, 8), Create super glue (`lightning_attractor`, 1; `From` / `To` keys the same on Create 0.5.1). The in-game check is SQ-PWG-45 (each file through the server's structure manager, placed and compared block by block) and CQ-PWG-14 (every scene opened) |

Per-file results (offline, 2026-11-13; in-game results go to the run doc):

| Structure | Scene | Blocks / palette / BEs / entities | Offline result |
|---|---|---|---|
| `battery` | `battery` | 29 / 5 / 4 / 0 | names and properties ok |
| `carbon_pile` | `carbon_pile` | 35 / 9 / 5 / 0 | names and properties ok |
| `constant_speed_motor` | `constant_speed_motor` | 30 / 7 / 5 / 0 | names and properties ok |
| `contactor` | `contactor` | 34 / 10 / 8 / 0 | names and properties ok; quirk (upstream-same): creative source saved with `axis` |
| `contactor_stack` | `contactor_stack` | 29 / 4 / 4 / 0 | names and properties ok |
| `device_connector` | `device_connector` | 27 / 4 / 2 / 0 | names ok; quirk (upstream-same, found by r1113): `device_connector` saved with `axis_along_first`, which the block never had (dropped on load) |
| `electromagnet` | `electromagnet` | 38 / 13 / 13 / 0 | names ok; quirk (upstream-same, found by r1113): `electromagnet` saved with `facing`, the block has no state properties (dropped); item-less blocks `create:andesite_belt_funnel`, `create:belt` exist on 1.19.2 / Create 0.5.1 |
| `energy_meter` | `energy_meter` | 38 / 9 / 9 / 8 | names and properties ok |
| `fuse` | `fuse` | 28 / 4 / 3 / 0 | names and properties ok |
| `gauges` | `voltage_gauge` | 31 / 6 / 4 / 0 | names and properties ok; quirk (upstream-same): creative source saved with `axis` |
| `generator/clutch` | `generator_clutch` | 32 / 8 / 6 / 0 | names and properties ok |
| `generator/generator_1` | `generator_1` | 69 / 18 / 15 / 0 | names and properties ok; item-less blocks `create:belt` exist on 1.19.2 / Create 0.5.1 |
| `generator/generator_2` | `generator_2` | 77 / 20 / 18 / 0 | names and properties ok |
| `generator/housing` | `generator_housing` | 31 / 8 / 6 / 0 | names and properties ok |
| `generator/parallel_windings` | `parallel_generator_winding` | 34 / 5 / 9 / 0 | names and properties ok; quirk (upstream-same, found by r1113): `winding.left=false` reads back true (`WindingBlock.updateCase` sets it once the neighbour is placed) |
| `generator/rotor` | `generator_rotor` | 32 / 8 / 6 / 0 | names and properties ok |
| `generator/winding` | `generator_winding` | 27 / 3 / 2 / 0 | names and properties ok |
| `ground1` | `grounding` | 26 / 3 / 1 / 0 | names and properties ok |
| `ground2` | `improved_grounding` | 44 / 10 / 1 / 0 | names and properties ok |
| `hv_switch` | `hv_switch` | 32 / 9 / 5 / 0 | names and properties ok |
| `lightning_attractor` | `lightning_attractor` | 57 / 6 / 1 / 1 | names and properties ok |
| `magnet` | `thunder_magnet` | 26 / 3 / 0 / 0 | names and properties ok |
| `motor` | `electric_motor` | 36 / 11 / 10 / 0 | names and properties ok; quirk (upstream-same): creative source saved with `axis` |
| `power_resistor` | `power_resistor` | 32 / 7 / 4 / 0 | names and properties ok |
| `redstone_converter` | `redstone_converter` | 33 / 8 / 5 / 0 | names and properties ok; item-less blocks `minecraft:redstone_wire` exist on 1.19.2 / Create 0.5.1 |
| `rheostat` | `rheostat` | 31 / 7 / 5 / 0 | names and properties ok; quirk (upstream-same): creative source saved with `axis` |
| `servo` | `servo` | 31 / 7 / 6 / 0 | names and properties ok |
| `transformer/nether` | `transformer_nether` | 155 / 10 / 0 / 0 | names and properties ok; item-less blocks `minecraft:lava`, `minecraft:nether_portal` exist on 1.19.2 / Create 0.5.1 |
| `transformer/sizes` | `transformer_sizes` | 30 / 3 / 0 / 0 | names and properties ok |
| `transformer/winding` | `transformer_winding` | 26 / 3 / 1 / 0 | names and properties ok |
| `variac` | `variac` | 31 / 7 / 5 / 0 | names and properties ok; quirk (upstream-same): creative source saved with `axis` |
| `wire/connector` | `wire_connector` | 57 / 3 / 8 / 0 | names and properties ok |
| `wire/cord_junction` | `cord_junction` | 30 / 6 / 3 / 0 | names and properties ok |
| `wire/cord_socket` | `cord_socket` | 29 / 5 / 2 / 0 | names and properties ok |
| `wire/insulated_wire` | `insulated_wire` | 29 / 3 / 4 / 0 | names and properties ok |
| `wire/voltage_drop` | `wire_voltage_drop` | 57 / 6 / 8 / 0 | names and properties ok; quirk (upstream-same): `power_resistor.axis_along_first` stored as a number, creative source saved with `axis` |

## 16. Phase P6: heat, light, fluid, solar

Classes moved from `upstream/java` (every changed file carries the "Modified for the Forge 1.19.2" line):
`electricity/heater` (6), `basinheater` (2), `fan` (3), `pump` (3, with `forge/ElectricPumpBlockEntityImpl`),
`light/fixture` (4), `light/factorylight` (6), `light/bulb` (6), `solarpanel` (11 with `registry/`; the Flywheel 1.0
`SolarPanelBearingVisual` stays in `upstream/java`, E3), `collections/ModdedContraptions`, `utility/RecipeNbtTransfer`,
`utility/forge/IterateItemHandler`, mixins `ChuteBlockEntityMixin`, `FanProcessingMixin`, `FluidPropagatorMixin`,
`forge/BasinRecipeMixin`, `forge/CookingPotBlockEntityMixin`, `forge/SkilletBlockEntityMixin`. `light/string` (string
lights) stays for P10. P6 parts added to `ModdedBlocks` (9), `ModdedItems` (5), `ModdedBlockEntities` (8),
`ModdedPartialModels` (fan propeller, factory light god rays x4, solar bearing rotor), `PowerGrid`, `forge/PowerGridImpl`.
Ponder: section 17.

| Area | Change |
|---|---|
| Fan processing types | Create 6 `CreateRegistries.FAN_PROCESSING_TYPE` -> Create 0.5.1 `FanProcessingTypeRegistry.register` (`HeaterFanProcessingTypes`), ids `powergrid:heater_blasting` (priority 150) and `powergrid:heater_smoking` (250), valid only at a heating coil in that state. Registered from `PowerGridImpl.commonSetup` `enqueueWork` (main thread, after Create's types; the registry is a plain sorted list), as the pack's `creativeengineering:dragon_breathing` (500). They do not compete with Dragon Breathing: each type is valid only at its own catalyst block; SQ-PWG-48 checks both together |
| `FanProcessingMixin` | targets checked on 0.5.1.i: `decrementProcessingTime` (first `CompoundTag.putInt`, local `CompoundTag` ordinal 2 = `processing`) and the transported `applyProcessing` (before `canProcess`, after `processingTime` is set). The second injector has `remap = false` (Create method and target; the refmap processor failed on it) |
| `ChuteBlockEntityMixin` | `@Inject(order = 1100)` dropped: Mixin 0.8.5 (Forge 43) has no injector order attribute. compressedcreativity injects the same two methods later in the body; ours cancels at HEAD only when an electric fan faces the chute |
| `FluidPropagatorMixin` | 0.5.1 `propagateChangedPipe`: the second `getBlockState`, the only `Direction` local and `BlockPos` ordinal 2 (`target`) match upstream; D&D's `MixinFluidPropagator` is empty |
| `BasinRecipeMixin` | 0.5.1 `apply(BasinBlockEntity, Recipe, boolean)`: `acceptOutputs` call, the single `IItemHandler` local, `List` ordinal 0 = `recipeOutputItems`. Serves circuit schematics (P9 items; `CIRCUIT_SCHEMATIC_HOLDER` has no member yet), so a no-op until P9; ported now as the plan's table puts it in P6 |
| Farmer's Delight mixins | targets checked on the pack's FD (the `repackedFarmers-1.19.2-1.2.4-ce-19.2-04` member nested in Creative Delight; same bytecode as `libs/`): `isHeated(Level, BlockPos)` calls in `cookingTick`, `animationTick` (pot and skillet) and `addItemToCook`, `isHeated()` in `isCooking`, `isHeated()Z` HEAD. No pack mixin redirects these calls (Create Central Kitchen injects `processCooking`; Creative Delight's three pot mixins touch contents and remainders). FD is a hard need of these two mixins: the scratch server always has it |
| Basin heater | Create 6 `BoilerHeater.REGISTRY.register` -> 0.5.1 `BoilerHeaters.registerHeater(block, heater)` with a float heat (`NO_HEAT` -> -1). The heat level is Create's `BlazeBurnerBlock.HEAT_LEVEL` (`blaze`), which 0.5.1's `BasinBlockEntity.getHeatLevelOf` reads |
| Solar biome overrides | upstream reads `solar_biome_override` as a 1.20 datapack registry (`DataPackRegistryEvent`). Port: `SolarBiomeRegistry.ReloadListener` (a `SimpleJsonResourceReloadListener` on the same path, `data/<ns>/powergrid/solar_biome_override/*.json`, same codec), added in `PowerGridImpl.addReloadListeners`; server only (every reader is server side; nothing synced). Difference: `/reload` changes the overrides, but each panel reads its entry once at its first tick (upstream-same; on 1.20.1 the registry never changes mid-world). **Fix B10** (power_grid.md 3.5): upstream's lookup loop overwrote its result on every entry, so only the last entry's biome could ever match |
| Solar contraption | Create 6 `CONTRAPTION_TYPE` registry -> 0.5.1 `ContraptionType.register(String, Supplier)` with the id string `powergrid:solar_panel` (what Create 6 writes as the contraption's `Type`), from `commonSetup` `enqueueWork` (plain `HashMap`). The solar panel attached check (`BlockMovementChecks.registerAttachedCheck`) moved there too. `BearingContraption.addBlock` has no level argument on 0.5.1; `ITransformableBlockEntity.transform(StructureTransform)`; `StructureBlockInfo.state` is a field on 1.19.2. The bearing never reads a biome override (its field is never set; upstream-same) |
| Solar math | joml `Vector3d` -> `Vec3` (same x, y, z, dot, equals); `BlockPos.containing` -> `new BlockPos(Vec3)` (floors); `getCenter` -> `Vec3.atCenterOf` |
| Lights and the server-values-only switch (H5) | the light level and the bulb's on / low power model come from the block state (`power`), set by the server: server values in both modes. The glow's fade reads the filament temperature, a sync appender on the state stream that `clientMirror` off stops. Port: with `clientMirror` off, `LightBulbState.updatePowerLevel` sends the block entity data at each power level change, so the glow starts from the server's temperature; between changes it follows the full state sync (100 ticks). The electric fan and pump compute their speed from the coil current on both sides, so with the mirror off their client speed (fan rotation, client-side entity push, particles) follows the full sync too; the fan's own `sendData` on each speed change keeps that close. CQ-PWG-16 records how it looks |
| Factory light light block | 1.20 `Properties.replaceable()` / `pushReaction(DESTROY)` -> a 1.19.2 `Material` (`MATERIAL`: replaceable, destroyed on push, not solid) as the initial properties; `noParticlesOnBreak()` dropped (no 1.19.2 twin; the block has no shape) |
| Growth lamp | upstream behaviour kept (plan P6): each power-level tick random-ticks every `#powergrid:affected_by_lamp` block in a radius-2 box on the lit side with chance 1 in `growthLampChance` (50), no season gate of its own. On the pack, crops' `randomTick` fires Forge's `CropGrowEvent.Pre`, so Creative Calendar's off-season refusal (about a quarter of checks) applies to lamp ticks as to natural ones; the lamp's rate (about 27 times vanilla) still ripens crops out of season. SQ-PWG-53 measures it; the gate is an integration step (power_grid.md 14). Upstream config comment bug B11 found (not fixed yet: the next build) |
| Particles (fix for `-04` to `-07`) | upstream ships no `particles/zap.json`; 1.20.1 skips a missing description, 1.19.2's `ParticleEngine` fails the client resource load. Port: `assets/powergrid/particles/zap.json` = `{}` (`ZapParticle` draws itself, no sprite set, so no `textures` key) since `-08` |
| Other 1.19.2 / Create 0.5.1 changes | catnip / Flywheel 1.0 imports to 0.5.1 / Flywheel 0.6 (`VoxelShaper`, `CachedBufferer`, `SuperByteBuffer`, `PartialModel`, `Couple`, `Iterate`, `Pair`, `BlockFace`, `AngleHelper`, `Pointing`, `VecHelper`, `AnimationTickHolder`, `WrappedWorld`, placement helpers, `TransformStack.cast`); `CreateLang` -> Create's `Lang` (fully qualified next to the port's `Lang`); `@Environment` -> `@OnlyIn`; `BuiltInRegistries` -> `Registry`; `PartialModel.of` -> `new PartialModel`; `rotateCentered(angle, dir)` -> `(dir, angle)`; `rotateYCenteredDegrees` -> `centre().rotateY().unCentre()`; `LootParams.Builder` -> `LootContext.Builder`; `canBeReplaced()` -> `getMaterial().isReplaceable()`; `ValueBoxTransform.Sided` `getLocalOffset` / `rotate` take the block state only. Create's own `FAN_PROCESSING` / `CONTRAPTION_ACTORS` awards stay (Create triggers, harmless; the pack drops advancements) |
| Datagen | `.blockstate`, `.item().model`, `.defaultLoot`, `.tag`, `.lang` calls left out as before; the shipped upstream data covers them (the asset check confirms blockstates, item models and lang) |

## 17. Phase P6: ponder

22 scenes added (58 in all; 3 tags unchanged), each through `wrap(...)` with upstream's components, structure, scene id
and tags: `current_gauge` (`gauges`, the current-gauge branch of `GaugeScenes.gauge` back), `power_gauge`,
`heating_coil_basic`, `heating_coil_speed`, `generator_3` (winding and copper coil, clutch, induction rotor and both
commutators, carbon pile coil), `wire_simple`, `wire_loop` (the four wires), `cord` (copper cord; the string light cord
gets it in P10), `light` (light fixture, light bulb, LV light bulb), `growth_lamp`, `electric_fan`,
`solar_panel_conditions`, `solar_panel_power_creation`, `solar_panel_bearing` (also Create's `KINETIC_SOURCES`),
`basin_heater`, `factory_light`, `factory_light_tall`, `factory_light_connect`, `switch_lv_switch`, `switch_lv_button`,
`switch_mv_switch` (`RelayScenes.switchSceneFor`), `hv_breaker`. `ElectricInstructions` gained `connectPanels`,
`mergePanels`, `splitPanels`. `ELECTRIC_DEVICES` gained heating coil, electric fan, basin heater, LV light bulb, light
bulb, growth lamp, solar panel, solar panel bearing. Waits: `encased_fan` (P9: `powergrid:circuit_board` in its structure
and its second text; manifest moved to 9); `CEILING_TILE_ATTACHMENTS` on the solar and factory light scenes (P10). Mapped by
hand: `ElectricInstructions.of(scene.base())` on the wrapper, `ModdedItems.LIGHT_BULB.asStack()`. Text keys and counts
match the shipped lang. The scene classes in `upstream/java` stay whole (as P5). On a ponder level the solar panel block
entity does nothing (client side; upstream-same) and the bulbs never heat, so the scenes set `power` directly.

Per-file results (offline, 2026-11-13):

| Structure | Scene | Blocks / palette / BEs / entities | Offline result |
|---|---|---|---|
| `power_gauge` | `power_gauge` | 30 / 6 / 3 / 0 | names and properties ok |
| `switch` | three switch scenes | 35 / 8 / 4 / 0 | names ok; quirks (upstream-same): creative source `axis`; `lv_switch` saved with `axis_along_first` (the block has `rotation` since; default rotation) |
| `hv_breaker` | `hv_breaker` | 33 / 9 / 4 / 0 | names and properties ok; item-less `minecraft:redstone_wire` |
| `generator/generator_3` | `generator_3` | 115 / 30 / 11 / 0 | names and properties ok (windings may read back `left` / `right` changed, as `parallel_windings`) |
| `wire/simple` | `wire_simple` | 55 / 5 / 3 / 0 | names ok; quirk (upstream-same): creative source `axis` |
| `wire/wire_loop` | `wire_loop` | 53 / 5 / 3 / 0 | names and properties ok |
| `wire/cord` | `cord` | 31 / 7 / 4 / 0 | names and properties ok |
| `basin_heater` | `basin_heater` | 27 / 4 / 2 / 0 | names and properties ok |
| `heating_coil/basic` | `heating_coil_basic` | 37 / 11 / 7 / 0 | names and properties ok |
| `heating_coil/speed` | `heating_coil_speed` | 40 / 12 / 7 / 0 | names and properties ok |
| `electric_fan` | `electric_fan` | 28 / 4 / 2 / 4 | names and properties ok; 4 hanging wires |
| `encased_fan` | (not registered) | 7 palette / 5 BEs / 0 | waits for P9: `powergrid:circuit_board`; does not ship |
| `growth_lamp` | `growth_lamp` | 35 / 8 / 1 / 0 | names and properties ok |
| `lightbulb` | `light` | 34 / 7 / 3 / 0 | names ok; quirk (upstream-same): creative source `axis` |
| `factory_light/factory_light` | `factory_light` | 45 / 11 / 3 / 2 | names and properties ok; 2 hanging wires |
| `factory_light/factory_light_tall` | `factory_light_tall` | 37 / 8 / 3 / 2 | names and properties ok; 2 hanging wires |
| `factory_light/factory_light_connect` | `factory_light_connect` | 36 / 9 / 1 / 0 | names and properties ok |
| `solar_panel/solar_panel_conditions` | `solar_panel_conditions` | 68 / 7 / 2 / 0 | names ok; quirk (upstream-same): `solar_panel` saved with `rotation` (no such property; dropped) |
| `solar_panel/solar_panel_power_creation` | `solar_panel_power_creation` | 29 / 6 / 4 / 4 | names ok; same `rotation` quirk; 1 block wire, 3 hanging wires |
| `solar_panel_bearing` | `solar_panel_bearing` | 62 / 8 / 3 / 0 | names ok; same `rotation` quirk |

## 18. Phase P7: equipment

Classes moved from `upstream/java` (section 7 lists them; every changed file carries the "Modified for the Forge
1.19.2" line). P7 parts added to `ModdedBlocks` (portable battery, no item of its own), `ModdedItems` (Electro-Zapper,
Electro-Baton, portable drill, portable saw, the placeable battery `BacktankBlockItem`, the worn portable battery),
`ModdedBlockEntities` (portable battery), `ModdedEntities` (`zap_projectile`, with upstream's `EntityProperties`:
0.25 x 0.25, tracking 4 chunks, update every 20 ticks, forced velocity updates), `ModdedPackets` (`ELECTRO_ZAPPER_SHOOT`,
`ZAP_PROJECTILE`, `LIGHTNING_SYNC`, `DRILL_SPEED`, appended). No ponder scene: upstream has none for the equipment; the
`lightning_attractor` scene (P5) now has its behaviour behind it.

| Area | Change |
|---|---|
| Portable battery item | 1.20 `BaseArmorItem(material, Type.CHESTPLATE, ...)` -> 0.5.1 `BaseArmorItem(material, EquipmentSlot.CHEST, ...)`: the vanilla chest armor slot, no Curios (plan). `ZincArmorMaterial`: `getDurabilityForType(Type)` / `getDefenseForType(Type)` -> `getDurabilityForSlot` / `getDefenseForSlot` (helmet, chestplate, leggings, boots map to the same array entries). Capacity is Create 0.5.1's `AllEnchantments.CAPACITY` / `CapacityEnchantment.ICapacityEnchantable` (same API). The worn tick is architectury `TickEvent.PLAYER_PRE` -> `PortableBatteryItem.onWornTick` (server only): it charges a held Forge Energy item. Zinc `Ingredient.of(AllItems.ZINC_INGOT.get())` (Registrate 1.1.5's `ItemEntry` is not an `ItemLike`) |
| Portable battery block | `SpecialBlockItemRequirement` (Create 6 API) -> 0.5.1 `ISpecialBlockItemRequirement`; `getDrops(BlockState, LootParams.Builder)` -> `LootContext.Builder`; catnip `VecHelper` -> Create's; `BlockPos.getCenter` -> `Vec3.atCenterOf`; 1.20 `Vec3.offsetRandom` written out (same formula). Charging: 2 FE per volt per tick across the 25 ohm switch until full (upstream-same) |
| Battery armor layer | catnip `CachedBuffers` / `SuperByteBuffer` -> Create 0.5.1 `CachedBufferer` / `SuperByteBuffer`; registered on every living renderer with a humanoid model in `EntityRenderersEvent.AddLayers` (upstream's forge impl) |
| Drill | `DrillItem` unchanged. `PlayerDrillMixin`: `level()` -> `level`; `getDigSpeed` is Forge's (`remap = false`, as upstream); its boost branch (x2 when the drill carries a `Boosted` tag) is inert until P9 (no way to boost before the Integrated Circuit). **`DrillBreakActionMixin` (new, GPLv3)**: upstream sets the drill's mining flag from Forge 1.20's `PlayerInteractEvent.LeftClickBlock` action (START sets it, STOP and ABORT clear it). Forge 1.19.2 fires that event for the start only and has no action, so the port injects at the head of `ServerPlayerGameMode.handleBlockBreakAction` (main-hand drill: mining = action is `START_DESTROY_BLOCK`). The flag only stops the speed decay; the speed-up per block, the cap by battery output and the client sync (`DrillSpeedS2CPacket` from `Player.tick`) are upstream's |
| Saw | Create 6 `content.kinetics.saw.TreeCutter` -> 0.5.1 `foundation.utility.TreeCutter` (same `findDynamicTree` / `findTree` / `destroyBlocks(Level, LivingEntity, BiConsumer)`) |
| Hand renderers | `ItemDisplayContext` -> `ItemTransforms.TransformType`; joml `Axis` -> `Vector3f` rotations; catnip `AnimationTickHolder` -> Create's; the drill head and zapper cog `PartialModel`s moved to `ModdedPartialModels` (`DRILL_HEAD`, `ELECTROZAPPER_COG`) so Flywheel 0.6 registers them with the rest at client init. `RenderHandEvent` hooks in `ForgeClientEvents` (Forge 1.19.2 has the same getters) |
| Electro-Zapper | `ElectroZapperS2CPacket` extends 0.5.1 `PotatoCannonPacket` (a Create `SimplePacketBase`) but travels on the Power Grid channel: `encode` = `ShootGadgetPacket.write`, the channel handler is upstream's; `ModdedPackets` uses the generic constructor (upstream had a dedicated one). `ElectroZapperRenderHandler` has no `@OnlyIn` (upstream `@Environment(CLIENT)`): `getHandler()` returns it, so the common packet class verifies against it on a dedicated server, as Create's own `PotatoCannonPacket` does with its handler. The "pass when the other hand holds a Boosting Chip" check is removed until P9 (`BoostingChipItem` is a P9 item). `ShootableGadgetItemMethods`, `CustomArmPoseItem` are Create 0.5.1's |
| Zap projectile | 1.20 `ProjectileUtil.getHitResultOnMoveVector` -> 1.19.2 `getHitResult` (same ray along the motion); damage `new DamageSource(ZAP holder, this, owner)` -> `ModdedDamageTypes.ZAP.indirect(this, owner)`: an `IndirectEntityDamageSource` with message id `zap` (death messages `death.attack.zap[.item]`), no difficulty scaling and no exhaustion (upstream type `scaling: never`, `exhaustion: 0`, in no damage type tag); `ZapProjectileS2CPacket` writes the hit position as three floats (no `Vector3f` buffer methods on 1.19.2). Renderer: catnip `TransformStack` -> `PoseStack` calls, joml matrices -> `com.mojang.math` |
| Lightning attractor | Create 6 `MovementBehaviour.REGISTRY.register(LIGHTNING_ROD, ...)` -> 0.5.1 `AllMovementBehaviours.registerBehaviour(Blocks.LIGHTNING_ROD, ...)` from `PowerGridImpl.commonSetup` `enqueueWork` (`PowerGrid.registerMovementBehaviours`); interface `com.simibubi.create.content.contraptions.behaviour.MovementBehaviour`; `BlockPos.containing` -> `new BlockPos(Vec3)`; `Vec3.offsetRandom` written out; the LIGHTNING advancement award removed |
| Sheep | `SheepMixin` (wire cutters shear for string and hurt the sheep by 1): `level()` -> `level`, `damageSources().playerAttack` -> `DamageSource.playerAttack`; the SHEEP_CUT award removed. Only `#powergrid:wire_cutters` (the wire cutter) shears; the saw does not (upstream-same) |
| Fixes | **B13**: `ZapProjectileS2CPacket` placed the chain sparks with joml `Math.lerp(fraction, start, end)`, whose order is `(a, b, t)`, so the sparks landed off the arc; the port uses `Mth.lerp(fraction, start, end)`. **B14**: `ElectroBatonItem.hurtEnemy` returned before any wear for a player, so a player's baton never lost durability (its tooltip says durability is used only without battery energy, as the drill, saw and zapper do); the port wears 1 per hit when the battery gives no power. **B11**: `growthLampChance` comment (P6 finding) |
| Fix (in P9b's jar `-14`) | **B20** (SQ-PWG-57 on `-10`): the worn battery's FE transfer (`forge/PortableBatteryItemImpl.tryTransferPower`) asked for the held item's whole free space and `BatteryUtils.drawEnergy` zeroed the charge and returned 0 when the charge was below the request, so a big buffer (AE2 wireless terminal) emptied the battery for nothing; the request is now capped at `BatteryUtils.availableEnergy(player)` (the charge; unlimited in creative, as `drawEnergy` draws nothing there) and `drawEnergy` leaves the charge untouched when it cannot meet a request (drill, saw, baton and zapper: unpowered below their per-use amount as before, the remaining charge kept) |
| Datagen | the item models, blockstate, loot table, `forge:chestplates` tag and lang ship from upstream's generated data; the asset check passes for the 59 blocks |

## 19. Phase P8: display and control

Classes moved from `upstream/java` (section 7; every changed file carries the "Modified for the Forge 1.19.2" line):
`electricity/crt`, `modulardisplay`, `bell`, `kinetics/plotter`, `punchcard`, `compat/cc`, packets `SaveCardC2SPacket`,
`DisplayBurnoutS2CPacket` (appended after `DRILL_SPEED` as `SAVE_CARD`, `DISPLAY_BURNOUT`). P8 parts added to
`ModdedBlocks` (plotter, alarm bell, CRT, andesite and brass encased CRT, punch card reader, modular display),
`ModdedItems` (incomplete punch card, punch card, display module), `ModdedBlockEntities`, `ModdedMenus`,
`ModdedPartialModels`, `PowerGrid`, `forge/PowerGridImpl`, `forge/ForgeEvents`, `utility/PlacementOverlay` (complete
now; upstream copy deleted). No mixin, no new config key (the CRT client keys and `plotterRecordNonconvergence` were
ported with the configs in P0). Display sources: none new (upstream has four, all ported in P2 to P4); no display target.

| Area | Change |
|---|---|
| Registration | `MapColor.PODZOL` -> `MaterialColor.PODZOL` (encased CRTs); `EncasingRegistry.addVariantTo(CRT)` is the same on 0.5.1; the encased CRTs have no item (upstream-same); `.lang`, `.blockstate`, `.item().model`, `.defaultLoot` left out (shipped data); `ItemEntry.asItem()` -> `get()` |
| Renderers | joml `Quaternionf` / `Matrix4f` / `Axis` -> `com.mojang.math.Quaternion`, `Matrix4f`, `Vector3f.XP/YP/ZP`; 1.19.2 `PoseStack` has no `rotateAround` (translate, `mulPose`, translate back); `PlotterRenderer.graphPoint` writes out the rotation about X (the goggle outline lines use it); Flywheel 1.0 `center` / `uncenter` -> 0.6 `centre` / `unCentre` (`rotateToFace` has the same angles); `PAPER_SHIFT`: catnip `SpriteShifter` -> Create 0.5.1 `foundation.block.render.SpriteShifter` (stitches `plotter_paper_shift`). The plotter and reader draw their shaft with `renderRotatingBuffer` (Create 0.5.1's kinetic renderer skips it under Flywheel instancing; upstream's `ShaftVisual` is Flywheel 1.0, E3). The modular display draws its modules with `RenderType.text` on the plain textures (no atlas; 1.19.2-safe) |
| Modular display value box | Create 0.5.1 `ValueBoxTransform`: `getLocalOffset(state)`, `rotate(state, ms)`, `testHit(state, localHit)` (no level and position: the slot code reads the block entity); `TransformStack.of(ms).rotateYDegrees` -> `cast(ms).rotateY` (degrees). Create 6's `ScrollValueBehaviour.bypassesInput` (dye and wrench clicks go to the block) does not exist on 0.5.1, whose `ValueSettingsInputHandler` also cancels the click on the server when `testHit` is true. Port: `testHit` returns false for a dye or a wrench on both sides: the client player's item, and on the server the item recorded by `ForgeEvents.recordInteractingItem` (new, GPLv3; `PlayerInteractEvent.RightClickBlock` at HIGH priority, before Create's NORMAL handler, stored in `CustomValueBoxTransformer.SERVER_INTERACTING_ITEM`). SQ-PWG-63, CQ-PWG-21 |
| Module type board | upstream's `DisplayModuleType` names resolve the lang text when the enum loads and pass that text as the option key; Create 0.5.1's option formatter shows `Lang.translateDirect(key)`, which prefixes `create.`. Port: the enum holds the lang keys (`powergrid.gui.modular_display.*`) and `ModularDisplayBlockEntity` overrides `createBoard` with the same board and a formatter that translates the key (icons kept); 1.20 `ByIdMap.continuous` written out |
| Punch card screen | `GuiGraphics` -> `utility/GuiGraphics` over the `PoseStack` (`renderBg`, `renderForeground`); catnip `AbstractSimiWidget.doRender` -> Create 0.5.1 `renderButton(PoseStack, ...)`, `getX()` / `getY()` -> `x` / `y`; 1.19.2 has no `StringWidget` (a locked card's author and name are drawn in `renderForeground` at the same places, with shadow as `StringWidget` draws them) and no `Tooltip` (the sign button uses the `AbstractSimiWidget` tooltip list); the name field is added with `addRenderableWidget`; particle rotation `mulPose(Vector3f.ZP.rotation(...))` |
| Punch card reader | `ItemStack.copyWithCount` -> `ItemHandlerHelper.copyStackWithSize`; the forge `PunchCardReaderBlockEntityImpl` (item handler capability: top inserts at angle 0, the facing side at 16; extraction only at the end it was read to) replaces the common one through `SubstituteBlockEntityProvider` (registered in the `PowerGridImpl` constructor, default in `PowerGrid.register`, as the FE inverter); `PunchCardMenu.CONSTRUCTORS` set before `PowerGrid.init` (upstream order; `ModdedMenus` binds it at class init) |
| CC: Tweaked | `CCBridge.register` from the `PowerGridImpl` constructor when `ModList` has `computercraft` (upstream: Architectury `Platform.isModLoaded`); CC: Tweaked 1.101.4 has the same `ForgeComputerCraftAPI.registerPeripheralProvider`, `IPeripheral` and `@LuaFunction`; `org.jspecify` annotations -> `org.jetbrains`. Peripheral types: `powergrid_voltage_gauge`, `_current_gauge`, `_power_gauge`, `_energy_meter`, `_battery`, `_generator_clutch`, `_redstone_converter` |
| Other 1.19.2 changes | `BlockPos.getCenter` -> `Vec3.atCenterOf` (bell); `Outliner.getInstance()` -> `CreateClient.OUTLINER` (plotter goggles); `LootParams.Builder` -> `LootContext.Builder` |
| Fixes | **B15**: `PunchCardReaderBlock.onRemove` dropped the card after `super.onRemove`, which has already removed the block entity (`IBE.onRemove`), so breaking a reader with a card in it lost the card; the port drops it first (SQ-PWG-65). **B16**: `DisplayBurnoutS2CPacket.handle` ran on the network thread (no `queue`): particles and sound off the client thread, and `getValue(HORIZONTAL_FACING)` threw when the block at the position was no longer a modular display; queued and checked (CQ-PWG-21). **B17**: `RedstoneConverterPeripheral.setValue` / `clearValue` ran on the computer thread and wrote the block entity from there; `@LuaFunction(mainThread = true)` (SQ-PWG-66) |
| Kept as upstream | a modular display slot that burns out destroys its module (no drop); the CRT's trace is client-only (not saved, not synced); `DisplayModuleRegistry` reads `symbol:` for the symbol module (its `serialize` writes `symbol:`, consistent); the dye on the CRT, plotter and modular display is not consumed; `SaveCardC2SPacket` edits the card in the main hand (the screen opens only for a main-hand card) |

### 19.1 Ponder (P8)

5 scenes added (63 in all; tags unchanged), each through `wrap(...)` with upstream's components, structure, scene id
and tags: `plotter`, `modular_display` (Electric Devices), `bell` (Electric Devices), `crt` (Electric Devices),
`punch_cards` (punch card reader and punch card; Electric Devices, Kinetic Appliances). `bell` took a plain Create 6
`SceneBuilder`; it now opens with `new PowerGridSceneBuilder(builder)` like the others. Tag members added: Electric
Devices plotter, CRT, punch card reader, modular display (upstream order); Kinetic Appliances plotter, punch card reader.
The display module's scene `circuit/display_module` is a circuit board scene (P9). Text keys and counts match the
shipped lang.

| Structure | Scene | Blocks / palette / BEs / entities | Offline result |
|---|---|---|---|
| `bell` | `bell` | 28 / 4 / 1 / 0 | names and properties ok |
| `crt` | `crt` | 10 / 3 / 1 / 0 | names and properties ok |
| `modular_display` | `modular_display` | 29 / 4 / 2 / 0 | names and properties ok |
| `plotter` | `plotter` | 42 / 10 / 4 / 0 | names and properties ok (light fixture P6, Create girder, cogwheels, shaft) |
| `punch_cards` | `punch_cards` | 36 / 10 / 4 / 0 | names and properties ok |

## 20. Phase P9a: components on boards, the solver's non-linear models

P9 is split into four sub-steps, each with its own decimal phase (section 5): **P9a (9.1)** components on boards,
**P9b (9.2)** design table and editor, **P9c (9.3)** etching, acid and assembly, **P9d (9.4)** Integrated Circuit and
boost. Each builds and is playable alone. P9a's boundary (what a board needs to exist and run, nothing that makes one):

- **In P9a:** the `circuit_board` block, block entity, renderer and baked model (`circuits/circuitboard` minus
  `IncompleteCircuitItem`; `circuits/components/forge` model loader); every component (`circuits/components`, the
  forge registry `powergrid:components`, 28 entries) and their properties; the schematic data model
  (`circuits/schematic` minus `CircuitSchematicItem` and `CircuitSchematicRender`); `component_items` (reload listener
  plus sync); the 17 component items, `pins` and the BJTs' transitional items; wires to board terminals and traces
  (`CircuitBoardEndpoint`); the potentiometer's value screen (`utility/CustomValueSettingsScreen`); board cooling by
  encased fans; the debug item and multimeter board readouts; the 15 circuit ponder scenes, `encased_fan` and the
  Circuit Components tag; the non-linear models in the solver harness (the `sim` classes themselves came in P1).
- **Out (later sub-steps):** P9b `circuits/editor`, `circuits/gui`, `CircuitSchematicItem`, `CircuitSchematicRender`,
  the design table block, the `circuit_schematic` item, the four menus, packets `ChangeScreenC2SPacket` /
  `SaveSchematicC2SPacket`, the circuit board edit menu (creative empty-hand use), `CircuitSchematic.toItemStack`, the
  design table ghost-ingredient JEI handler, the editor keys; P9c `IncompleteCircuitItem`, acid (fluid, block, bucket,
  damage source in use), `empty_circuit`, `unetched_circuit`, the incomplete circuit items, the sequenced assembly and
  mixing chains, `ArmBlockEntityMixin`, `ArmInteractionPointMixin`, the JEI circuit design / assembly categories; P9d
  `BoostingChipItem` (Integrated Circuit), its renderer, `BoostRecipe` (`boost_recipe`), `ItemBoostUtils` use sites,
  `BeltDeployerMixin`, `RecipeApplierMixin`, the JEI boost category, the Electro-Zapper's boosting-chip check.
- **Playable in P9a:** a board placed from an item that carries a `Schematic` tag (pick-block of a board, a dropped
  board, `/give` with NBT, or `zpwg9 board` on a scratch server) runs its circuit on the grid: its connectors are wire
  terminals, parts heat up, burn out and are repaired, switches, buttons, potentiometers and redstone relays work,
  gauges drive redstone, boards join through vias. Boards cannot be made in survival until P9b / P9c.

Classes moved from `upstream/java` (section 7; every changed file carries the "Modified for the Forge 1.19.2" line).
Packets `UPDATE_COMPONENT` and the port's `COMPONENT_ITEMS` appended after `DISPLAY_BURNOUT` (ids are ordinals).
No new mixin; `AirCurrentMixin` gets back its board branch. No new config key.

| Area | Change |
|---|---|
| Component registry | upstream `NewRegistryEvent` + `RegistryBuilder.of` -> 1.19.2 `event.create(new RegistryBuilder<Component>().setName(...), callback)` in `PowerGridImpl.newRegistry`; Registrate's `component(...)` builder entries register into it (`AbstractPowerGridRegistrate.component`, the upstream methods). `ComponentBuilder.item(...)` (datagen feed for `component_items`) is a no-op taking `Object` (Registrate 1.19 entries are not `ItemLike`); the jar ships upstream's generated `component_items` and `circuit_component` tag |
| component_items | 1.20 datapack registry `powergrid:component_items` -> `ComponentRegistry.ReloadListener` (same folder `data/<ns>/powergrid/component_items`, same codec: `item`, optional `tag`; `optionalFieldOf` for 1.20's `Codec.optionalField`) and `ComponentItemsSyncS2CPacket` from `OnDatapackSyncEvent` (join and `/reload`), with server and client tables as `WireRegistry`; the client logs `Received N component items from the server` (port line). `getItem` / `getComponent` / `getComponentId` read the table for the level's side; a missing entry gives null / empty instead of an exception |
| Board model | `IUnbakedGeometry.bake` takes 1.19.2's `ModelBakery` (`bake(BASE_MODEL, state)`); `getMaterials` returns the trace and pad sprites (replaces `atlases/blocks.json`, which never ships); joml `Quaternionf` / `Vector3f` -> `com.mojang.math`, the board rotation as `Vector3f.YP.rotation(..)` times `XP.rotation(..)` (joml's post-multiplied order); Forge 1.19.2 has no `QuadTransformers.applyingColor`: a local transformer writes the vertex colour (ABGR `0xFF404040` for a burnt part, as upstream's `applyingColor(64, 64, 64)`). Loader `powergrid:circuit_board` registered in `ModelEvent.RegisterGeometryLoaders`, component models and the base model in `ModelEvent.RegisterAdditional` (both events exist in Forge 43) |
| Rendering | catnip `CachedBuffers` / `SuperByteBuffer` -> Create 0.5.1 `CachedBufferer` / `SuperByteBuffer` (`rotateYDegrees` -> `rotateY`, `center` / `uncenter` -> `centre` / `unCentre`); Create 6 `RenderTypes.additive()` -> 0.5.1 `RenderTypes.getAdditive()`; Flywheel `TransformStack.of` -> `TransformStack.cast`; `Font.drawInBatch(..., DisplayMode.NORMAL, ...)` -> the 1.19.2 `seeThrough=false` overload; joml `Matrix4f` -> `com.mojang.math.Matrix4f`; `ResourceLocation.withSuffix` (1.20) -> `Component.withSuffix` helper; the component partial models in `ModdedPartialModels` (P9a block) |
| Editor-only code kept compiling | `ComponentFootprint`'s GUI render methods use `utility/GuiGraphics` (new `renderOutline`, the 1.20 frame) and carry `CircuitSchematicRender`'s two colours locally until P9b |
| Board block | the creative empty-hand edit menu (`CircuitBoardEditMenu`) is left out until P9b (comment at the spot); `Direction.fromDelta` -> `fromNormal`; everything else as upstream (orientation, redstone, interactable parts, creative sneak repair, pick-block with the schematic, loot table copies `Schematic` and `Thermal`) |
| Potentiometer screen | `CustomValueSettingsScreen` on Create 0.5.1's `ValueSettingsScreen(pos, board, settings, onHover)` (no network id; `saveAndClose` is overridden as upstream, so no `ValueSettingsPacket`); `ScreenOpener` from `foundation.gui`; ticked from `PowerGridClient.clientTick` |
| Packets | `UpdateComponentBiPacket`: `player.level()` -> `player.level`; Architectury 6's `PacketContext.getEnv()` is a Forge `Dist`, so the side switch uses `getEnvironment()` (`Env.CLIENT` / `SERVER`) |
| Thyratron hum | `BlockPos.getCenter` -> `Vec3.atCenterOf` |
| Creative tab | upstream's `ItemDisplay` exclusions applied in `PowerGridCreativeTab.fillItemList` (transitional items, the placeable portable battery, `circuit_schematic`, `unetched_circuit`, `circuit_board`): before P9a every registered item was in the tab. On 1.19.2 the search tab and JEI still list them (they fill from every item's category) |
| Kept as upstream | a board item without a `Schematic` places and removes itself; on the server a burnt part's `destroyed` flag stays false (only reads set it; the burnt part's heat-source wires are removed, which is what opens the circuit); the electron tube `remapId` for leftover `electron_tube` ids |

### 20.1 Fixes and findings (P9a)

**B19** (fixed, P4 code, found by the `-10` scratch boot): `WindingBlockEntity.addParallel` guarded its re-entry only
when logging was enabled (`if(adding && logsEnabled())`), and asked its direct owner to add, so two windings naming each
other as owner (windings placed over windings that already had owners) recursed until a StackOverflowError crashed
the server; the port returns on any re-entry and follows the owner chain to its root (`rootOwner`, visited set): a
loop or a missing owner makes the winding the owner, a longer chain is flattened onto the root (SQ-PWG-71). Jar `-12`.
**B18** (power_grid.md 3.5, behaviour, not changed): tube wires report convergence while
their damped evaluation point still walks (section 10.1). Port slip fixed while porting: the P2 stub of
`CircuitBoardEndpoint` is replaced by the full upstream class (the stub returned the block centre and no node).

### 20.2 Ponder (P9a)

16 scenes added (79 in all; 4 tags), each through `wrap(...)` with upstream's components, structure, scene id and
tags: the 15 `circuit/*` scenes (`resistor`, `voltage`, `current`, `capacitor`, `relay` (relay and DPDT relay),
`triode`, `pentode`, `thyratron`, `redstone_relay`, `via` (on Create's copper nugget), `potentiometer`,
`regulator_tube`, `neon`, `label` (on paper), `display_module`), all tagged Circuit Components, plus `encased_fan` (on
Create's encased fan, untagged as upstream; it waited for the circuit board since P6). New tag `circuit_components`
(icon circuit board) with upstream's 26 members: wire connector, copper nugget, resistor, diode, capacitor, copper coil,
both relays, redstone relay, the four tubes, neon bulb, LV switch, LV button, LV light bulb, potentiometer, both gauges,
varistor, VFET, both BJTs, display module. `CircuitScenes` needed only the 0.5.1 imports and the `util` fields;
upstream's copy stays in `upstream/java`. Text keys and counts match the shipped lang. Ponder structures shipping: 77 of
79 (the 16 `circuit/*` files and `encased_fan` added; only `ceiling_tile` and `wire/string_lights`, both P10, are left
out). `circuit/electron_tube.nbt` ships but no scene uses it (upstream leftover; its board names
`powergrid:electron_tube`, which `PlacedComponent` remaps to `triode`). Offline check: every structure's palette and
board component ids are registered at 9.1 (the board state properties `facing`, `rotation` match the block); script
in the session scratchpad (`p9a/ponder_check.py`).

| Structure | Scene | Blocks / palette / BEs / entities | Offline result |
|---|---|---|---|
| `circuit/capacitor` | `circuit_capacitor` | 12 / 4 / 3 / 0 | names and properties ok |
| `circuit/current` | `circuit_cgauge` | 11 / 3 / 2 / 0 | ok |
| `circuit/display_module` | `circuit_display_module` | 29 / 4 / 4 / 0 | ok |
| `circuit/electron_tube` | (none) | 10 / 3 / 1 / 0 | ok; component id `electron_tube` (remapped to triode) |
| `circuit/label` | `circuit_label` | 18 / 4 / 1 / 0 | ok |
| `circuit/neon` | `circuit_neon` | 10 / 3 / 1 / 0 | ok |
| `circuit/pentode` | `circuit_pentode` | 10 / 3 / 1 / 0 | ok |
| `circuit/potentiometer` | `circuit_potentiometer` | 10 / 3 / 1 / 0 | ok |
| `circuit/redstone_relay` | `circuit_redstone_relay` | 11 / 4 / 1 / 0 | ok (lever) |
| `circuit/regulator_tube` | `circuit_regulator_tube` | 10 / 3 / 1 / 0 | ok |
| `circuit/relay` | `circuit_relay` | 32 / 5 / 7 / 0 | ok (light fixture P6) |
| `circuit/resistor` | `circuit_resistor` | 10 / 3 / 1 / 0 | ok |
| `circuit/thyratron` | `circuit_thyratron` | 10 / 3 / 1 / 0 | ok |
| `circuit/triode` | `circuit_triode` | 10 / 3 / 1 / 0 | ok |
| `circuit/via` | `circuit_via` | 34 / 8 / 9 / 2 | ok (four board orientations, creative voltage source; 2 wires) |
| `circuit/voltage` | `circuit_vgauge` | 11 / 3 / 2 / 0 | ok |
| `encased_fan` | `encased_fan` | 33 / 7 / 8 / 0 | ok (Create encased fan and cogwheel, thermometer, rheostat) |

### 20.3 Notes for P9b to P9d

- **P9b (9.2, design table and editor):** move `circuits/editor`, `circuits/gui`, `circuits/schematic/CircuitSchematicItem`
  and `CircuitSchematicRender` (then import its colours back into `ComponentFootprint`, which holds two copies now),
  packets `ChangeScreenC2SPacket` and `SaveSchematicC2SPacket` (append after `COMPONENT_ITEMS`), restore
  `CircuitSchematic.toItemStack` (comment marks it) and the creative empty-hand `CircuitBoardEditMenu` branch of
  `CircuitBoardBlock.use` (comment marks it; `CircuitBoardBlockEntity.sendToMenu` is already there), `ModdedMenus`
  entries (`circuit_design_table`, `_edit`, `_load`, `circuit_board_edit`, manifest 9.2), the design table block and
  block entity, the JEI ghost-ingredient handler (`PowerGridJEI` comment). The whole editor draws with 1.20
  `GuiGraphics`: extend `utility/GuiGraphics` (it has `fill`, `blit`, `renderOutline`, `drawString`, `renderItem` and
  tooltips). The component property widgets show enum names (`EnumPropertyWidget`, `Component.literal`), including the
  display module's module option; translate `DisplayModuleType.getTranslationKey()` there if lang text is wanted.
  Server-side item lookups for the editor (`ComponentRegistry.getComponentId(level, stack)`, `getItem(level, ..)`)
  read the reload listener's table; the client ones (`getItem(component)`, `getComponent(item)`) the synced table.
  The design table's own recipe stays out until `copper_plating` (P10). PWG-41 is P9b's gate.
- P9b done: section 21. **Read 21.3 before P9c.**
- **P9c (9.3):** `IncompleteCircuitItem` (uses `ComponentRegistry.getComponentId` / `getItem`), acid fluid and block
  (`ModdedFluids`, P9c manifest), the unetched / etched circuit chains, the battery assembly (needs acid), the arm
  mixins; PWG-42's survival part.
- **P9d (9.4):** `BoostingChipItem`, `BoostingChipRenderer`, `BoostRecipe`, `BeltDeployerMixin` and `RecipeApplierMixin`
  (advancement calls removed), the four boost recipes, the electrical gizmo assembly, the JEI boost category, the
  `PlayerDrillMixin` boost branch (inert since P7) and the Electro-Zapper's boosting-chip check (section 18). PWG-43.
- Probes: `zpwg9 board` builds schematics from parts and traces (`CircuitSchematic.placeComponent`,
  `front().addHorizontalLine` / `addVerticalLine`, `rebuildPads`, then `CircuitBoardBlockEntity.setSchematic`); P9b
  can compare an editor-made board against those designs.

## 21. Phase P9b: circuit design table and editor

Boundary (section 20): the `circuit_design_table` block, block entity and glow renderer; the `circuit_schematic` item;
the editor (`circuits/editor`, `circuits/gui`) and `CircuitSchematicRender`; the four menus `circuit_design_table`,
`circuit_design_table_edit`, `circuit_design_table_load`, `circuit_board_edit`; packets `CHANGE_SCREEN` and
`SAVE_SCHEMATIC` (appended after `COMPONENT_ITEMS`: ids are ordinals; upstream has them before `SAVE_CARD`);
`CircuitSchematic.toItemStack`; the creative empty-hand board edit menu in `CircuitBoardBlock.use`; the JEI
ghost-ingredient handler (drag a circuit component from JEI onto the editor grid). The editor keys (rotate, trace,
delete, pick, layer) and the client config colours (`CCircuit`) were already registered since P2.

**Playable in P9b:** a powered table (30 W: 120 V across its 480 ohm socket; creative ignores power) opens the table
screen; the edit screen places components from the inventory (click a placeable item, hotbar keys, or JEI drag), draws
traces on both layers, deletes areas, edits part properties, names and saves the design; the load screen saves and
loads designs as files in `<game dir>/circuits/` (client side). The confirm button writes the design onto an Empty
Schematic or a Circuit Schematic in the save slot (consumed; creative needs none) into the output slot; the arrow
button reads a schematic or a board in the load slot. A creative player sneak-uses a Circuit Schematic to get a Circuit
Board with that design (upstream; a survival board needs the etching chain, P9c) and edits a placed board with an empty
hand. The table's own crafting recipe waits for `copper_plating` (P10): survival players get the table from `/give`
until then (scratch rows do).

| Area | Change |
|---|---|
| GUI | 1.20 `GuiGraphics` -> `utility/GuiGraphics` around 1.19.2's `PoseStack` (it now extends `GuiComponent` for the protected `hLine` / `vLine` / `fillGradient`); new `blitRepeating` (1.20's tiling with `Divisor`-style slices) and `renderTooltip` for pre-split lines and one component. Screens: `renderBg` / `renderForeground` / `render` / `renderTooltip` take the `PoseStack` and wrap it; catnip `doRender` -> `renderButton(PoseStack, ...)` (inside Create 0.5.1's `AbstractSimiWidget` push / pop); `getX()` / `getY()` / `setX` -> the 1.19.2 `x` / `y` fields; `AllGuiTextures.getWidth()` etc -> the 0.5.1 fields; `AllIcons` / `ModIcons.render(PoseStack, ..)`; 1.20 `Tooltip.create` -> Create's widget tooltip list (`getToolTip().add`), as the punch card (P8) |
| Edit screen | 1.20 `getTooltipFromContainerItem` -> 1.19.2 `Screen.getTooltipFromItem` (the "Can be placed on a circuit" line on placeable inventory items); the saving overlay's `fillGradient` and save icon on the screen's `PoseStack` |
| Focus | 1.20 focus navigation (`nextFocusPath`, `ComponentPath`) has no 1.19.2 counterpart and is dropped; `PropertyWidget.setFocused` widened to public (protected on 1.19.2) so the properties panel can drop focus (that is what applies a typed value when another part is picked); the inner `EditBox` uses `setFocus`; the enum widget's scroll input has no focus on 1.19.2 (it scrolls under the mouse), so its `setFocused` is empty. Text fields read their focus before forwarding a click: 1.19.2's `EditBox.mouseClicked` drops focus itself on a click outside, which would skip applying the typed value |
| Properties panel | `Component.translatableWithFallback` (1.19.4) -> `I18n.exists` check for the `.summary` hover text; the title falls back to the component id when the synced `component_items` table has no item for it (upstream: NullPointerException) |
| Schematic item | 1.20 `TooltipFlag.isCreative` does not exist: the "sneak right-click to convert" line shows when the client player is in creative (the other half of upstream's condition); `new ItemStack(BlockEntry, 1)` -> `asStack()` (Registrate 1.19 entries are not `ItemLike`) |
| Block and renderer | catnip `VoxelShaper` -> `foundation.utility.VoxelShaper`; `CachedBuffers.partial` -> `CachedBufferer.partial`, `rotateYCenteredDegrees` -> `rotateCentered(Direction.UP, radians)`; the block's data (blockstate, models, glow partial, loot, mineable tags) ships from upstream's generated files; `CThermal.maxPower(50, 0.5)` and `CResistance.setResistance(480)` add the table's `thermal` / `resistance` config keys (per-block keys, section 7) |
| Packets | `ChangeScreenC2SPacket` / `SaveSchematicC2SPacket`: the handler body moved into `apply(Player)` (called from `handle`'s queued task; the scratch probe runs it with a fake player); `player.level()` -> `player.level` |
| JEI | JEI 11's `IGhostIngredientHandler.getTargets(gui, ingredient, doStart)` (raw ingredient) instead of 1.20 JEI's `getTargetsTyped` |
| Menus | Create 0.5.1's `MenuBase` has the same constructor / `createOnClient` / `addSlots` API; the menus port unchanged. `ModdedMenus` places them before the punch card as upstream (menu ids are a synced registry) |
| Load screen | the file list's directory stream is closed (upstream leaked one per refresh, every second while the screen is open) |
| Kept as upstream | the table's inventory is not saved (its menu returns slot items to the player on close, also when switching to the edit or load screen); a client-side file name in the load screen is not sanitised on load (the save path is); the editor keeps a copy of the schematic while open |

### 21.1 Fixes (P9b; power_grid.md 3.5)

The `-14` jar also carries **B20** (P7 code, portable battery FE transfer; section 18 table, SQ-PWG-57).

- **B21** `ChangeScreenC2SPacket` and `SaveSchematicC2SPacket` acted on any position the client named: open a table's
  screens without power or reach, overwrite any table's design, load an unloaded chunk (`getBlockEntity`). Now both
  need a loaded block within container reach (8 blocks from its centre).
- **B22** The board editor's name field was dropped on save (the handler applied the name to tables only); now the
  board takes it.
- **B23** Writing a schematic replaced the output slot, deleting a schematic still in it; now the write waits until
  the output slot is empty (button does nothing, no item consumed).
- **B24** Two editor changes were not recorded as changes, so Esc closed the editor without the unsaved-changes
  warning and dropped them: removing a part with a right click, and changing an option property (enum widget, e.g.
  the display module's module). Both now mark the design changed.

### 21.2 Rows

PWG-41 (P9b part): SQ-PWG-72 (boot, registries, menus, packets, recipes), SQ-PWG-73 (table power, the two packets
with their reach check, schematic item round trip, write / load slots), SQ-PWG-74 (a design saved on the table becomes
a board that solves: the P9a resistor design through the table, the schematic item and the creative conversion),
SQ-PWG-75 (board edit menu and board editor save, creative only), CQ-PWG-24 (client: table and editor by hand),
CQ-PWG-25 (client: board editor in creative, load screen files, JEI drag). Probe `tools/scratch_query/pwg_p9b_probe.js`
(`zpwg9b`, fake player `[zpwg9b]`).

### 21.3 Notes for P9c and P9d

- `deploying/schematic_copy` is held at 9.3 (`phases.json` override): its NBT copy is `BeltDeployerMixin`'s schematic
  branch (upstream: one mixin with the boost advancement and the schematic copy). P9c needs that branch for the
  unetched circuit chain (sequenced assembly deploying a schematic) anyway: port `BeltDeployerMixin` in P9c with the
  schematic branch only (no advancement), and P9d adds nothing to it (the boost branch was only the advancement). If
  P9c does not bring it, raise the override to 9.4.
- P9c's JEI circuit design category takes the design table as catalyst (`PowerGridJEI` upstream line
  `addRecipeCatalyst(ModdedBlocks.CIRCUIT_DESIGN_TABLE, CircuitDesignCategory.TYPE)`); the ghost handler is already in
  `registerGuiHandlers`.
- `IncompleteCircuitItem` and the etching chain read the `Schematic` tag that `CircuitSchematic.toItemStack` writes
  (safe NBT: no part UUIDs, no unsafe (runtime state) properties); `RecipeNbtTransfer` (P6) copies it in basins.
- P9c done: section 22 (`BeltDeployerMixin` came with its schematic branch, so `deploying/schematic_copy` ships at 9.3;
  the circuit design and assembly categories are in).

## 22. Phase P9c: etching, acid, assembly

Boundary (sections 20, 21.3): the acid fluid (`powergrid:acid` source, `powergrid:flowing_acid`, liquid block `acid`,
bucket `acid_bucket`, fluid type with upstream's acid damage), the items `empty_circuit`, `unetched_circuit`,
`incomplete_unetched_circuit` (Create `SequencedAssemblyItem`) and `incomplete_circuit` (`IncompleteCircuitItem`), the
six recipes that need them (section 5), the mixins `BeltDeployerMixin` (schematic branch), `RecipeApplierMixin`
(schematic branch), `ArmInteractionPointMixin`, `ArmBlockEntityMixin`, and the JEI categories Circuit Designing (design
table catalyst) and Circuit Assembly (mechanical arm catalyst; animated arm). No new packet, menu, config key or
ponder scene (upstream has no acid or assembly scene).

**Playable in P9c (survival chain):** a deployer holding a Circuit Schematic copies its design onto an Empty Schematic
on a belt or depot (`deploying/schematic_copy`, the held schematic is kept); the unetched circuit is a sequenced
assembly on the Circuit Schematic (two loops of deploy Empty Circuit, deploy Dough, deploy copper sheet, press),
each step keeping the design; Blazing Acid is mixed in a heated basin (redstone, blaze powder, 250 mB water -> 250 mB);
a heated basin etches an Unetched Circuit with 250 mB acid into an Incomplete Circuit; mechanical arms put the
design's parts into an Incomplete Circuit lying on a depot or on a belt they deposit to (one part per trip; the arm
only takes parts the circuit still needs), and the last part turns it into a Circuit Board with the design. The
battery's sequenced assembly (P3 output: copper sheet, zinc sheet, 250 mB acid by spout, three loops) ships now.
Empty Circuits come from cutting wooden slabs (two per slab).

| Area | Change |
|---|---|
| Fluid | Registrate MC1.19-1.1.5 has 1.20's `fluid(name, still, flowing, FluidTypeFactory)` builder and the default liquid block; `ForgeFlowingFluid.Source` source, Registrate's bucket, translucent render type on the client only (`EnvExecutor`, as upstream); the forge `ModdedFluidsImpl` is folded into `ModdedFluids` (D2), whose `ACID` entry registers through `PortPhase.require("fluid", "acid")`; `acid()` / `acidFlowing()` return the source / flowing fluids. `AcidFluidType` extends Create 0.5.1's `AllFluids.TintedFluidType` (same constructor and tint methods; it supplies the still / flowing textures, which 1.19.2 stitches from the fluid type's client extension, no atlas file). `move`: `entity.level()` -> the 1.19.2 field; 2 HP of `ModdedDamageTypes.ACID` (message id `acid`, P2 port of the damage type) when `random.nextInt(15) >= 10`, on each tick the entity's feet block holds acid, then the vanilla fluid movement (upstream) |
| Data | lang, blockstate, block and item models, the fluid tag `powergrid:acid` and the item tag entries ship from upstream's generated files (Registrate's `.lang` / `.tag` are data generation and are left out, as for every other entry) |
| Incomplete circuit | catnip `Color.mixColors` -> Create 0.5.1 `foundation.utility.Color.mixColors(int, int, float)`; `new ItemStack(BlockEntry/ItemEntry)` -> `asStack()` (Registrate 1.19 entries are not `ItemLike`); fix B26 (below) |
| BeltDeployerMixin | only the schematic copy branch (section 21.3): upstream's other branch awarded the boosting chip advancement (removed for good; the pack drops advancements), so P9d adds nothing to this mixin. Same injection point on Create 0.5.1 (`BeltDeployerCallbacks.activate`, the `DeployerBlockEntity.award` call after the results are collected; MixinExtras `@Local` for `collect`); remap off |
| RecipeApplierMixin | the schematic branch only (P9d adds the `BoostRecipe` branch; upstream copy kept in `upstream/java`): Create 0.5.1's method is `applyRecipeOn(ItemStack, Recipe)` (Create 6 adds a level and a remainder flag); deployers on belts and depots and the mechanical press apply every sequenced assembly step through it, so this is what carries the design through the unetched circuit's assembly (upstream relied on it too). `@Inject(order = 1500)` dropped: `Inject.order` is newer than the Mixin 0.8.5 of Forge 1.19.2, and no pack mod injects into `RecipeApplier` (checked: pack jars' mixin classes, in-tree mods) |
| Arm mixins | `ArmInteractionPointMixin` unchanged (Create 0.5.1 `ArmInteractionPoint.insert` has the same `ItemHandlerHelper.insertItem` call; `BeltPoint` extends `DepotPoint`, checked first as upstream); `ArmBlockEntityMixin` remap off, fix B25 (below). Create 0.5.1's arm takes from an input only what its outputs accept in a simulated insert (`getDistributableAmount`), so the mixin's "one less" remainder makes the arm carry one part per trip and skip parts the circuit does not need |
| JEI | JEI 11: `getBackground` is required (a blank drawable of upstream's size from the gui helper, so the categories take an `IGuiHelper`), `addSlot(role, x, y)` for `addInputSlot` / `addOutputSlot`, item stacks for `addItemLike`, Create's `getRenderedSlot()` for `setStandardSlotBackground`; drawing on a `PoseStack` (`utility/GuiGraphics.blit` for the overlay textures, `IDrawable.draw(PoseStack, x, y)`). `AnimatedMechanicalArm`: Flywheel 0.6 `TransformStack.cast` (`centre`, `rotateX` in degrees), joml `Axis` -> `Vector3f`, `ItemDisplayContext.FIXED` -> `TransformType.FIXED`, the held item into the client buffer source (flushed); Create 0.5.1 has the same `ArmRenderer` transforms and arm partial models |
| Asset check | `tools/pg_phase_check.py` reads `ModdedFluids` and checks each fluid's textures (`block/acid_still`, `block/acid_flow`), `fluid.powergrid.acid`, the `acid` blockstate and lang key, and the bucket's item model and lang key (the plan's "fluid textures must pass") |
| Creative tab | gains Blazing Acid Bucket and Empty Circuit; Unetched Circuit and the two incomplete circuits stay out (upstream's exclusions, section 20) |
| Kept as upstream | a deployer copy of a held schematic without a design writes nothing; the etching keeps only the `Schematic` tag (a schematic's custom name is not carried to the board); an Incomplete Circuit stacks to 1 |

### 22.1 Fixes and findings (P9c; power_grid.md 3.5)

- **B25** (fixed) `ArmBlockEntityMixin` returned from its whole scan at the first output that is not a belt, so an arm
  whose outputs list a depot (or any non-belt point) before a belt never stopped incomplete circuits on that belt and
  they rode past half done; the port skips such outputs (`continue`). SQ-PWG-82 puts a full depot first.
- **B26** (fixed) The incomplete circuit's tooltip threw a NullPointerException when the synced `component_items`
  table had no item for a missing part (a datapack that drops an entry), and building the assembly tag threw for a
  part whose component id is not registered; the port shows the component id and skips unknown parts. The progress bar
  divided by a zero part count (NaN colour); it shows 0 now.
- **B27** (recorded, not changed) An Incomplete Circuit whose design has no parts, or one etched from an Unetched
  Circuit made on a blank Circuit Schematic (no design), can never be completed: no part is accepted and nothing turns
  it into a board. Upstream behaviour; the owner may want the etching to require a design (P11 evaluation).

### 22.2 Rows

PWG-42 (survival part): SQ-PWG-76 (boot, registries, mixins, recipes; `zpwg9c check` runs the `RecipeApplierMixin`
unit check), SQ-PWG-77 (acid: flow, bucket, damage in a pen), SQ-PWG-78 to -81 (the survival chain on one boot:
deployer schematic copy, unetched circuit line with deployers and a press, acid and etching in heated basins, arm
assembly on a depot into a board with the same design), SQ-PWG-82 (arm on a belt, B25), SQ-PWG-83 (battery assembly),
CQ-PWG-26 (client: fluid render, JEI categories, tooltip). Probe `tools/scratch_query/pwg_p9c_probe.js` (`zpwg9c`,
fake player `[zpwg9c]`).

### 22.3 Overrides at 9.3

`sequenced_assembly/battery` and `deploying/schematic_copy` are met (section 5). Unmet: `crafting/circuit_design_table`,
`crafting/solar_panel_bearing` (P10), `sequenced_assembly/electrical_gizmo` and the four `deploying/*_boosting` (9.4).

### 22.4 Notes for P9d

- `RecipeApplierMixin` is in `src/main/java` with the schematic branch; add upstream's `BoostRecipe` branch (copy the
  tool's tag onto the boosted tool except `Boosted`; upstream copy in `upstream/java/.../mixin/RecipeApplierMixin.java`,
  then delete it). Keep the schematic branch after it, as upstream.
- `BeltDeployerMixin` needs nothing in P9d (its boost branch only awarded an advancement).
- P9d: `BoostingChipItem` (`integrated_circuit`), `BoostingChipRenderer`, `BoostRecipe` (`boost_recipe` type and
  serializer, register in `PowerGrid.registerRecipes`), `incomplete_electrical_gizmo`, the electrical gizmo assembly
  and four boost recipes (their overrides say 9.4), the JEI boost category, the `PlayerDrillMixin` boost branch and the
  Electro-Zapper's boosting-chip check (sections 9, 18). Its recipe is `mechanical_crafting/integrated_circuit`
  (check its ingredients and the overrides with `pg_phase_check.py --phase 9.4`).
- JEI: `PowerGridJEI.registerCategories` takes the gui helper from `registry.getJeiHelpers()`; a boost category built
  with the `CategoryBuilder` (as magnetizing) needs no helper.

## 23. Phase P9d: Integrated Circuit and boost (P9 complete)

Boundary (sections 18, 20.3, 22.4): the items `integrated_circuit` (`BoostingChipItem`, custom renderer
`BoostingChipRenderer`) and `incomplete_electrical_gizmo` (Create `SequencedAssemblyItem`), the `powergrid:boost_recipe`
serializer (`BoostRecipe`), the `BoostRecipe` branch of `RecipeApplierMixin`, the Electro-Zapper's chip check, and the
six recipes that need them (section 5). `port_phase=9.4`, jar `0.6.2-ce-19.2-16`. No new block, block entity, menu,
packet, config key, mixin class or ponder scene (upstream has none for the boost). `BeltDeployerMixin` gains nothing
(its upstream boost branch only awarded an advancement, section 21.3). `PlayerDrillMixin` already had the boost
branch from P7 (only its comment changed). With P9d every P9 id is registered; `upstream/java` keeps no P9 class.

**Playable in P9d:** the Integrated Circuit is mechanical-crafted (lapis, redstone, polished rose quartz, gold
nuggets). It boosts the portable drill, portable saw, Electro-Baton and Electro-Zapper, either by a deployer holding
it over the tool on a belt or depot (Create's deploying, the tool keeps its tag: damage, name, enchantments) or by
hand (hold right-click with the chip in one hand and the tool in the other: an eating animation and the boosting sound,
32 ticks, one chip used). A boost lasts 30 % of the tool's max durability in uses (drill and saw 75, baton 6, zapper 15;
each use takes one, the last plays the item-break event): the drill digs at twice the speed for twice the energy, the
saw cuts at twice the speed for twice the energy, the baton stuns as (60 - target max health) x power (cap 10, upstream
30) for 120 ticks (60), and the zapper shoots at twice the power (twice the damage; above 0.6 it chains, so a boosted
zapper at 0.6 output chains too). A boosted tool is not boosted again; the bar turns light blue while powered. The
electrical gizmo is made by sequenced assembly on a zinc sheet (copper coil, electron tube, Integrated Circuit, gold
nugget; one loop), which unlocks the 9 crafting recipes that use it.

| Area | Change |
|---|---|
| BoostRecipe | Create 0.5.1 `ItemApplicationRecipe` matches on a `RecipeWrapper` (Create 6: `Container`), so `matches(RecipeWrapper, Level)`; `rollResults(List<ProcessingOutput>)` exists on 0.5.1 and `RecipeApplier.applyRecipeOn(ItemStack, Recipe)` calls it, so the override applies on belts and depots as upstream. The type is `create:deploying` (from `DeployerApplicationRecipe`), only the serializer is Power Grid's: registered as `boost_recipe` in `PowerGrid.registerRecipes`, no recipe type of its own (the manifest's `recipe_types` entry only places the recipes at 9.4) |
| BoostingChipItem | Create 0.5.1 `CustomUseEffectsItem.shouldTriggerUseEffects` returns a nullable `Boolean` (Create 6: catnip `TriState`), hooked by Create 0.5.1's `CustomItemUseEffectsMixin`; the BOOSTING_CHIP advancement award removed (the pack drops advancements); fix B28 (below) |
| BoostingChipRenderer | `ItemDisplayContext` -> `ItemTransforms.TransformType`, joml `Axis` -> `Vector3f` rotations; a null-player guard on the first-person branch. Registered through the P7 `customRenderer` transform (Create 0.5.1 `SimpleCustomRenderer`) |
| RecipeApplierMixin | the `BoostRecipe` branch before the schematic branch (upstream order); `getOrCreateTag` on the output (upstream `getTag`; the recipe always sets `Boosted`, so only a guard). The copied tag includes `Damage`, which replaces the recipe result's `"Damage": 0`: a boost does not repair (upstream-same) |
| Electro-Zapper | the chip check is back: an unboosted zapper passes when the other hand holds a `BoostingChipItem`, so the chip's use can start. `useBoost` still runs first (a boosted zapper shoots and spends a boost even with a chip in the other hand; upstream-same) |
| JEI | upstream has no boost category: the four boost recipes are of type `create:deploying`, so Create 0.5.1's Deploying category lists them (tool in, chip held, tool out). Nothing added to `PowerGridJEI`; the plan's "JEI boost category" is Create's Deploying page (CQ-PWG-27) |
| Creative tab | gains the Integrated Circuit; `incomplete_electrical_gizmo` stays out (upstream's exclusions, section 20) |
| Kept as upstream | the chip is used up in creative too; the hand path boosts any tool a `deploying` recipe pairs with the chip (the four tools); a boosted tool's boost shows as a light blue bar only while the battery powers it |

### 23.1 Fixes and findings (P9d; power_grid.md 3.5)

- **B28** (fixed) `BoostingChipItem.finishUsingItem` boosted whatever the other hand held when the 32 ticks ended and
  used the chip either way. With the chip in the off hand, a hotbar scroll during the use puts any item in the main
  hand (vanilla only stops the use when the used hand's item changes): a sword got a `Boosted` tag it never uses, a
  block nothing, and the chip was gone. The port checks the recipe again at the end (`findRecipe` and not boosted)
  and keeps the chip without a match. SQ-PWG-86 `handswap`.
- **B29** (fixed) Only the drill and saw showed the "Boosted" tooltip line; the baton and zapper (boostable by the same
  recipes) showed none, so a boosted baton or zapper looked the same as a plain one (the light blue bar shows only
  while powered). The port adds `ItemBoostUtils.addTooltip` to both.

### 23.2 Rows

PWG-43: SQ-PWG-84 (boot, registries, recipes, `zpwg9d check` with the `RecipeApplierMixin` unit check), SQ-PWG-85
(boost by deployers on a depot and a belt, the tag kept, no second boost), SQ-PWG-86 (hand boost, B28, the zapper's
chip check), SQ-PWG-87 (boosted drill and saw against SQ-PWG-58), SQ-PWG-88 (boosted baton and zapper against
SQ-PWG-59 / -60), SQ-PWG-89 (electrical gizmo assembly), CQ-PWG-27 (client: chip render and hand use, tooltips, JEI
Deploying page). Probe `tools/scratch_query/pwg_p9d_probe.js` (`zpwg9d`, fake player `[zpwg9d]`; it changes
`[zpwg7]`'s main hand for the equipment rows and uses `zpwg9c`'s machines).

### 23.3 Overrides after P9

Met at 9.4: `sequenced_assembly/electrical_gizmo` and the four `deploying/*_boosting`. Left (both `copper_plating`,
P10): `crafting/circuit_design_table`, `crafting/solar_panel_bearing`. All other overrides are met (`conductive_casing`
since P4, `pins` since 9.1, acid since 9.3).

### 23.4 Notes for P10

- `copper_plating` (block, slab, stairs) unlocks the last two overrides (design table, solar panel bearing); check
  with `pg_phase_check.py --phase 10` that both drop out and no recipe stays unmet.
- After P9 the P10 content is the only unported part of the upstream `collections` copies (`ModdedItems`,
  `ModdedBlocks`, `ModdedBlockEntities`, `ModdedEntities`) and of the `WireItem` partial (string light cord tooltip,
  section 7); `general/ceilingtile` and `electricity/light/string` wait in `upstream/java`. Check each copy for other
  leftovers before deleting it after P10 (`data/` generators and the `*Visual` Flywheel 1 classes never port).
- `ponder/wire/string_lights.nbt` is held at 10 (section 5, P5 manifest edit); the `crafting_special_string_light_cord`
  recipe type and the string light entity are P10's.
- Nothing in P10 touches the boost or circuit code; the P9 mixins (`RecipeApplierMixin`, `BeltDeployerMixin`, arm
  mixins) are complete.

## 24. Phase P10: decor (the port is complete)

Boundary (plan P10, section 23.4): the blocks `copper_plating`, `copper_plating_slab`, `copper_plating_stairs`,
`ceiling_tile`, `ceiling_tile_lamp`, `ceiling_tile_connector`, `ceiling_tile_junction`, `ceiling_tile_solar`,
`string_light_block`; the item and entity `string_light_cord`; the recipe serializer
`powergrid:crafting_special_string_light_cord`; four block entities (lamp, connector, junction, solar); the ponder
scenes `ceiling_tile` and `wire/string_lights` and the tag `ceiling_tile_attachments`. `port_phase=10`, jar
`0.6.2-ce-19.2-17`. No new menu, packet, config key or mixin. With P10 every upstream id is registered and every
recipe file ships.

**Playable in P10:** copper plating (deployer: copper block + copper plate) is a casing-like block with connected
textures; its slab and stairs come from the stonecutter or the crafting table and join its texture on full faces. No
oxidation or waxing (upstream has none: a `CasingBlock`). Ceiling tiles (deployer: copycat panel + cardboard) are 2 px
plates; right-clicking one with a factory light, wire connector, cord junction or solar panel turns it into the
matching attachment (the lamp takes light bulbs like the light fixture; the connector has one wire terminal, the
junction takes cords, the solar tile is a solar panel facing up, multiblock by wrench on its top face); a wrench on a
side face gives the attachment back and leaves the tile; holding ceiling tiles places more next to a ceiling block
(placement helper). The string light cord (deployer: copper cord + light bulb) is a cord with small bulbs: a filament
load of 2 x 4608 / N ohm across each end (N items), so V^2 / 4608 W per item (3.125 W at 120 V, its rating); it glows
from about 73 V, fills the air along it with light blocks (light 5) above about 108 V and burns out above about 127 V
(filament 1800 K). Dyes in a crafting grid with one cord give it a bulb colour pattern (any number of dyes, in grid
order).

| Area | Change |
|---|---|
| Registration | `ModdedBlocks` P10 section (each id through `PortPhase.require`), `StairBlock(Supplier, Properties)` (Forge) for 1.20's `StairBlock(BlockState, Properties)`; the four attachments have no item (upstream-same: pick-block gives the attachment, the loot drops tile and attachment); `string_light_block` uses `FactoryLightLightBlock.MATERIAL` (1.20 `replaceable()` / `pushReaction(DESTROY)` are Material properties on 1.19.2). `STRING_LIGHT_CORD` item sits after the copper cord (upstream's tab order); the entity is in the Carry On blacklist as upstream |
| Ceiling blocks | catnip placement helpers -> Create 0.5.1 `foundation.placement`; `SpecialBlockItemRequirement` -> 0.5.1 `ISpecialBlockItemRequirement`; `IWrenchable.playRemoveSound` is an instance method on 0.5.1; `BlockState.canBeReplaced()` -> `getMaterial().isReplaceable()`; renderer `CachedBuffers` -> `CachedBufferer` |
| Ceiling solar | the same 1.19.2 changes as the P6 solar panel (joml `Vector3d` -> `Vec3`, `BlockPos.containing` -> `new BlockPos`, `getCenter` -> `Vec3.atCenterOf`, `StructureBlockInfo.state` field); its NBT keeps upstream's absolute positions with `LastKnownPos` (the older format the solar panel replaced); it shares the B10-fixed biome override lookup |
| String light cord | `level()` -> the `level` field; catnip `PonderLevel` -> `PonderLevels.isPonder`; `BlockPos.containing` -> `new BlockPos`; the recipe on 1.19.2's `CustomRecipe(ResourceLocation)` / `SimpleRecipeSerializer` (no `CraftingBookCategory`, `RegistryAccess` or `CraftingContainer.getItems()`); a null guard in `setLightBlocks`; fixes B30, B31, B34 |
| WireItem | the cord's rated lines (120 V, 3 W) are back in the tooltip (`StringLightCordItem.appendProperties`) |
| Ponder | `connectLightCord` in `ElectricInstructions`, `WireScenes.stringLights`, `DeviceScenes.ceilingTile` (call sites `util.grid` / `select` / `vector` as fields); `CEILING_TILE_ATTACHMENTS` (icon ceiling tile, upstream's five members) on the ceiling tile scene and, as upstream, on the wire connector, cord junction, solar panel (both) and factory light (all three) scenes; `wire/cord` also on the string light cord. 81 scenes, 5 tags, 79 structures; both new structures checked offline (palette names and properties registered at 10, no later phase) |
| JEI | nothing added: the dye pattern recipe is a special crafting recipe (upstream shows none either); the deploying recipes show on Create's Deploying page |

### 24.1 Fixes (P10; power_grid.md 3.5)

- **B30** (fixed) `StringLightCordEntity.makeWire` did not drop the existing wires first (`CordEntity.makeWire` does).
  `ElectricBehaviour` calls `makeWire` again when it rebuilds an endpoint's connections, so each rewire left the two
  cord wires and both filament loads of the previous call in the network, with no owner: the far end kept the source
  voltage and the source kept feeding the load after the cord was cut or broken. The port calls `dropWire()` first.
  SQ-PWG-95 `rewire` then `cut`.
- **B31** (fixed) the "lit" flag that makes a cord clear its light blocks is not saved, so a cord that came back unlit
  after a chunk reload or a restart (or cooled while unloaded) never removed the light blocks it had placed. The port
  starts the flag true, so the first lazy tick of an unlit cord clears them (air stays air). SQ-PWG-95 (light blocks
  set before the cord).
- **B32** (fixed) the ceiling tile lamp lost its light bulb when broken (no `getDrops` override, unlike the light
  fixture) or wrenched (only the factory light came back). The port drops it with the block and gives it back with the
  wrench. SQ-PWG-91.
- **B33** (fixed) the copper plating slab and stairs copy the soft metal properties (correct tool required) but were in
  no `mineable` tag, so no tool could harvest them: breaking them dropped nothing. Their loot table also dropped one
  slab from a double slab. The port adds both to `mineable/pickaxe` (as the plating) and drops two from a double slab.
  SQ-PWG-90 `check`.
- **B34** (fixed) crafting a string light cord with no dye (to clear a pattern) gave a cord with an empty `Pattern`
  tag, which shows no pattern but no longer stacks with plain cords. The port returns a plain cord. SQ-PWG-90 `check`.
- **B35** (open, owner note 2026-11-13) a wire block does not drop when the block under it breaks, and cannot be broken by hand. Check upstream behaviour, then fix; TODO `largecontent/mod-additions #78`.

### 24.2 Rows

PWG-44: SQ-PWG-90 (boot, registries, recipes, `zpwg10 check`: tool tags and loot B33, pattern grids B34), SQ-PWG-91
(attachments, placement helper, wrench and drops, B32), SQ-PWG-92 (ceiling lamp on the grid), SQ-PWG-93 (ceiling
solar against a solar panel, multiblock), SQ-PWG-94 (ceiling connector and junction in a circuit), SQ-PWG-95 (string
lights with numbers, B30, B31, burn-out), SQ-PWG-96 (deploying recipes by machines; design table and solar bearing),
CQ-PWG-28 (client rendering, placement by hand, tooltips, JEI, creative tab), CQ-PWG-29 (ponder). Probe
`tools/scratch_query/pwg_p10_probe.js` (`zpwg10`, fake player `[zpwg10]`).

### 24.3 Overrides after P10

All 32 `phases.json` recipe overrides are met (the highest is 10). `crafting/circuit_design_table` and
`crafting/solar_panel_bearing` ship with `copper_plating`. Recipes still uncraftable in the pack for a pack reason, not
a phase: the four that need `#forge:cardboard` (D13: `deploying/ceiling_tile`, the FE inverter, copper coil and punch
card ones; the tag is filled at integration).

### 24.4 What is left in `upstream/java` (never ports)

31 files, none meant to be ported: the datagen providers `data/` (16: tag, recipe and fluid constant providers; the jar
ships upstream's generated output), `utility/DataProviderUtility` and `utility/forge/DataProviderUtilityImpl`
(datagen), `forge/ComponentItemEntryProviderImpl` and `forge/WireItemEntryProviderImpl` (datagen providers for
`component_items` and `wire_types`); the Flywheel 1.0 visuals `HvSwitchVisual`, `SolarPanelBearingVisual`,
`HalfShaftVisual`, `TunedBlockVisual`, `GeneratorClutchVisual`, `CommutatorVisual`, `RotorVisual` and
`utility/SimpleBlockEntityVisualFactory` (E3: the renderers draw everything); `compat/HiddenItems` (REI only, REI
dropped); `utility/proxy/ProxyProvider` (only the TFMG proxy used it, TFMG dropped); `collections/ItemDisplay` (1.20
`CreativeModeTab.DisplayItemsGenerator`; `forge/PowerGridCreativeTab` does the tab and its exclusions). Deleted in
P10: the 37 partial upstream copies (collections, `PowerGrid`, `PowerGridClient`, the forge entry points, ponder
classes, `WireItem`, `DebugItem`, `MultimeterItem`, `IElectricEntity`, JEI), each checked first: what they held beyond
the port is datagen (Registrate `blockstate` / `loot` / `recipe` / `tag` / `lang` calls, `provideDefaultLang`,
`providePonderLang`, `SoundEntryProvider`), the Fabric / platform switches (`forPlatform`, `c:` tags), the 1.20
creative tab and datapack registries (replaced by `PowerGridCreativeTab` and reload listeners), Flywheel 1.0
`.visual(...)` calls, TFMG / Cold Sweat branches and upstream's commented-out `generator_rotor`. Also never ported:
the native solver (D4), advancements, data-driven damage types (section 4). `upstream/mixins` (every mixin is in
`powergrid.mixins.json`, 29), `upstream/test` (the P1 harness input) and `upstream/mods.toml.upstream` stay as
references.

### 24.5 Notes for P11

- P11 is evaluation only (plan P11): full scratch run of PWG-01 to -44 and every SQ / CQ-PWG row on `-17`, D14 from
  CQ-PWG-01 to -04, the owner's keep / drop / merge table, H7 tier measurement. No port code is planned; fixes from
  that run go in as `-18` onward.
- Integration points found during the port and waiting for stage 5: `#forge:cardboard` (D13, four recipes), the via
  component's `create:copper_nugget` (removed by the pack; SQ-PWG-67), the PG zinc sheet next to the pack's (A3), the
  copper plate the copper plating deployer takes (`#forge:plates/copper` = the IE plate in the pack).
