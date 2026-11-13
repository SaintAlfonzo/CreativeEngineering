# Create: Power Grid: overlap, port and integration report

Investigation written 2026-11-12. **Nothing built**, and adding the mod at all is speculative (owner, 2026-11-12). The jar is
`powergrid-mc1.20.1-0.6.2.jar` (Create: Power Grid 0.6.2, mod id `powergrid`, by patryk3211, Da-Negy, rvndm and casvara),
unpacked to `DecompiledMods/powergrid-mc1.20.1-0.6.2/` (data and classes; the jar itself was deleted after unpacking).
It is built for **MC 1.20.1, Forge 47, Create 6.0.7 and Architectury 9**, so a **port to 1.19.2 / Create 0.5.1.i is required**.
Licence **Apache-2.0** (mods.toml; the jar has no LICENSE or NOTICE file, but the GitHub repo has one). Apache-2.0
code can go into a GPLv3 project, so a **fork is allowed**. The source is public at
github.com/patryk3211/PowerGrid; branches exist for 1.20.1 and 1.21.1 only, with no 1.19.2 or Create 0.5 line, so a port
starts from the `architectury-1.20.1` source rather than the decompile. "PG" in this file is Power Grid. Test
prefix `PWG-`.

**Owner focus:** the voltage system (section 3.1) and how it could be built here (section 3.2). The rest of the
report covers the other parts of the mod as background for that.

**Scope rule:** integration adds no recipe or mechanic that is not in PG or set by a pack rule, unless the owner
asks for it by name. Upstream bugs are fixed.

**Overlap marker:** rows that duplicate something in the pack carry `*overlaps w/ <what>*` (section 2).

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 77 blocks, 57 items (incl. 8 `incomplete_*`), 1 fluid (Blazing Acid), 5 entities (4 wire kinds + zap projectile), 6 damage types, 116 recipe files (3 own types), 74 block loot tables, 28 tag files, 96 advancements, 2 datapack registries (`wire_types`, `component_items`), solar biome overrides, 20 mixins | 1, 3 | reference, from the decompile |
| Voltage system: a modified nodal analysis (MNA) circuit solver with Newton-Raphson per tick, thermal model per device | 3.1 | analysed |
| Implementation routes for a voltage system here | 3.2 | three routes, recommendation |
| Port 1.20.1 / Create 6 to 1.19.2 / Create 0.5.1.i | 3.6 | change list; the solver core ports almost as-is, the device and client layers are the bulk |
| Overlap | 2 | 45 rows; wires, motors, capacitor, multimeter, integrated circuit, CRT, zinc sheet are direct clashes |
| Upstream issues | 3.5 | 4 noted (no log evidence: never loaded in this pack) |
| Fold-in | 4 | 22 steps |
| Decisions | 5 | D1 to D14 answered 2026-11-12 except D14 (measure); 15 holes H1 to H15 |
| Tests | 8 | PWG-01 to PWG-44 drafted |
| Execution plan | [plan_power_grid.md](plan_power_grid.md) | phases P0 to P11, 2026-11-13 |

- **Dependencies:** Create 6.0.7 (pack: 0.5.1.i), Architectury 9.2.14+ (pack: 6.6.92), jar-in-jar EJML 0.44 (dense,
  sparse, core, simple) and MixinExtras 0.3.5. Optional compat: CC: Tweaked (installed), Cold Sweat, TFMG (not installed),
  JEI (installed), REI, Farmer's Delight (installed: Cooking Pot and Skillet mixins let PG heaters heat them).
- **Pack references:** none. No script, doc or tool mentions `powergrid`; no in-tree mod registers FE storage.
- **Native solver:** `NativeMNA` loads `powergridNative7` (`.so`/`.dll`) into `.pg-native/` in the game folder.
  This jar **ships no binary**; the source has a `native/` C++ project. Without the binary PG falls back to the Java
  solver. A port should drop the native path (D4).

## 1. Overview

PG adds electricity to Create as a **circuit simulation**, not as an energy-per-tick transfer. Each block exposes
terminals; the player strings wires (entities with real resistance) between them; every tick the mod solves the
circuit for node voltages and branch currents. Generators are Create-driven rotors in a magnetic field; motors turn
voltage into Create speed; transformers change voltage by turn ratio; every device heats from the power it burns
and breaks or explodes when it overheats. On top sits a component layer (resistors, diodes, transistors, vacuum
tubes, relays) that can be placed on circuit boards, designed in a schematic editor and baked into Integrated
Circuits. FE is reached through an FE Inverter (FE to grid) and a Device Connector (grid to FE machines).

### Facts

| | |
|---|---|
| Mod id / version | `powergrid` 0.6.2, display name "Create: Power Grid" |
| MC / loader | 1.20.1, Forge 47 (Architectury common + forge split) |
| Code size | 778 classes, about 85k decompiled lines: simulator 8.3k, circuits and editor 10.7k, ponder 5.3k, compat 2.2k |
| Blocks | 77 (machines, wires and connectors, gauges, switches, transformers, generator parts, lights, decor) |
| Items | 57 (components, wires, tools, equipment, 8 sequenced-assembly intermediates) |
| Fluids | Blazing Acid (`acid`, bucket "Blazing Acid Bucket") for circuit etching |
| Entities | `hanging_wire`, `block_wire`, `cord`, `string_light_cord`, `zap_projectile` |
| Effects / potions | none |
| Damage types | `acid`, `electrocution`, `live_wire_cutting`, `overloaded_machine`, `spinning_rotor`, `zap` (1.19.4+ data format) |
| Recipes | 116: 46 shaped, 28 shapeless, 13 mechanical crafting, 8 sequenced assembly, 4 deploying, 4 cutting, 2 mixing, 2 stonecutting, 1 pressing, 1 item application; own types `boost_recipe` (4), `magnetization` (2), `crafting_special_string_light_cord` (1) |
| Loot | 74 block self-drop tables; no mob or chest loot, no GLMs |
| Tags | 28 files; creates `forge:coils`, `forge:copper_coils`, `forge:iron_coils`; joins `forge:wires/*`, `forge:plates/zinc`, `forge:chestplates`, `create:fan_transparent` |
| Worldgen | none (solar output reads biome temperature; `solar_biome_override` data) |
| Advancements | 96; the pack drops all advancements |
| Config | server: electricity, solver, generator, kinetics, equipment, resistance and thermal values per block; common: logging, client sync; client: circuit editor colours |
| Commands | `/powergrid` (creative sources, debug) |
| Lang / ponder | full English lang; 81 ponder scenes |

## 2. Overlap (first)

Sources: `kubejs/exported/display_names.json`, `kubejs/exported/tags/item_tags.json`, `item_dict.js`,
`removed_items.js`, [engineering.md](engineering.md), [recipes_engineering.md](recipes_engineering.md).

### 2.1 Items

| PG item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Copper Wire `wire` *overlaps w/ IE copper wire* | `immersiveengineering:wire_copper` (only `forge:wires/copper`); CC&A `copper_wire` removed | second copper wire in `forge:wires/copper` | remove `powergrid:wire`; register IE `wire_copper` as a PG wire type (D6) |
| Iron Wire *overlaps w/ CE iron wire* | `creativeengineering:iron_wire` (`i.wire_iron`) | duplicate | register CE iron wire as a wire type, remove PG's |
| Golden Wire *overlaps w/ CE gold wire* | `creativeengineering:gold_wire` (`i.wire_gold`) | duplicate | same |
| Insulated Copper Wire *overlaps w/ IE insulated coil* | `immersiveengineering:wirecoil_copper_ins` | PG puts it in `forge:wires`; IE's is a coil, not a wire | keep PG's (dyeable, different use); take it out of `forge:wires` |
| Copper Cord, Cord Junction | IE wire coils | different mechanic | keep |
| String Light Cord, String Light Block *overlaps w/ Fairy Lights* | `fairylights:*` | decorative overlap only | keep (D11) |
| Wire Connector, Heavy Wire Connector *overlaps w/ IE connectors* | IE `connector_lv/mv/hv`, relays; CC&A connectors removed | two wire networks side by side | keep; the networks do not interact (D1) |
| Wire Cutters *overlaps w/ IE Wire Cutter* | `immersiveengineering:wirecutter` | two cutters | keep both (each cuts its own wires) unless PG accepts IE's (code change) |
| Capacitor *overlaps w/ CC&A Capacitor* | `createaddition:capacitor` (`i.capacitor`), `pneumaticcraft:capacitor` ("Inductor") | same English name | rename PG's or merge CC&A's into it (D7) |
| Diode, BJTs, Static Induction Transistor *overlaps w/ PneumaticCraft transistor* | `pneumaticcraft:transistor` | similar | keep (simulated parts) |
| Triode, Pentode, Thyratron, Barretter, Regulator Tube | `create:electron_tube`, IE `electron_tube` ("Vacuum Tube") | PG uses `create:electron_tube` as ingredient | keep |
| Relay, Double Pole Relay, Contactor | IE relays (wire relays, a different thing) | name only | keep |
| Redstone Relay *overlaps w/ CC&A (removed)* | `createaddition:redstone_relay` removed | same name, the other is gone | keep |
| Integrated Circuit *overlaps w/ D&D* | `create_dd:integrated_circuit` | exact name clash, different use | rename D&D's display or PG's (D7) |
| Circuit Board, Unetched/Empty Circuit, Design Table, Schematic *overlaps w/ PneumaticCraft PCB, IE, ProjectRed Fabrication, Control Engineering* | `pneumaticcraft:empty_pcb`, `printed_circuit_board`, IE `circuit_board`, `logic_circuit`, ProjectRed IC workbench | fifth circuit-making chain | port late or drop (D5) |
| Acid / Blazing Acid Bucket *overlaps w/ PneumaticCraft etching acid* | `pneumaticcraft:etching_acid_bucket`, IE redstone acid, Vintage sulfuric acid | same role as PneumaticCraft etching | if circuits are ported, use PneumaticCraft's etching acid (D5) |
| Magnet *overlaps w/ Quark magnet (name)* | `quark:magnet` (`i.magnet`, a block) | same name, different thing | rename PG's "Magnetized Iron" or similar (D7) |
| Copper Coil, Resistive Coil, Winding *overlaps w/ CE coils* | `creativeengineering:wirecoil_<m>`, `coil_<m>`; IE `coil_lv/mv/hv` | PG creates `forge:copper_coils` (17 recipes) | tag CE `wirecoil_copper` into `forge:copper_coils`; decide whether PG's coil item stays (D6) |
| Transformer Small/Medium, Core, Variac, Nether Transformer *overlaps w/ IE transformers* | IE `transformer`, `transformer_hv` | IE steps FE tiers, PG steps volts by turns | keep (different systems) |
| Electric Motor *overlaps w/ CC&A Electric Motor* | `createaddition:electric_motor` (`i.electric_motor`), `create_dd:kinetic_motor` | exact name clash | rename or retire one (D8) |
| Generator parts, Constant Speed Motor, Servo *overlaps w/ CC&A Alternator, IE Kinetic Dynamo* | `createaddition:alternator`, IE `dynamo` | both turn rotation into FE | keep; decide whether CC&A's FE motor/alternator stay (D8) |
| Battery, Potato Battery | CC&A accumulators, IE capacitors | different (simulated cell) | keep |
| Portable Battery (chest slot) *overlaps w/ IE Accumulator Backpack* | `immersiveengineering:powerpack` | same role | keep or drop with equipment (D9) |
| Portable Drill, Portable Saw *overlaps w/ IE Drill, Buzzsaw* | IE `drill`, `buzzsaw` | same role | D9 |
| Electro-Baton, Electro-Zapper *overlaps w/ SecurityCraft taser* | `securitycraft:taser` | similar | D9 |
| Light Bulb, LV Light Bulb *overlaps w/ IE Light Bulb, Beautify bulb* | `immersiveengineering:light_bulb` (`i.light_bulb`), `beautify:lamp_light_bulb` | three "Light Bulb" items | rename PG's "Incandescent Bulb" (D7) |
| Neon Bulb, Fixtures, Factory Light, Ceiling Tile Lamp | IE floodlight, ProjectRed lamps | decorative overlap | keep |
| Growth Lamp | none | new | keep; check against seasons and crop rules (step 14) |
| Multimeter *overlaps w/ IE, ProjectRed, D&D multimeters* | IE `voltmeter` ("Engineer's Multimeter"), `projectred_core:multimeter` (`i.multimeter`), `create_dd:multimeter` | four multimeters | rename PG's "Circuit Multimeter" (D7) |
| Voltage/Current/Power Gauge, Energy Meter, Thermometer | Advanced Peripherals energy detector, IE current transformer | different | keep |
| Electric Fan, Electric Pump, Basin Heater, Heating Coil | Create fan/pump, D&D industrial fan, Compressed Creativity heater, IE external heater | electric versions of kinetic blocks | keep |
| Solar Panel, Solar Bearing, Ceiling Tile Solar | PneumaticCraft solar compressor | the pack has no electric solar | keep |
| FE Inverter | none | new; recipe needs `create:cardboard` (Create 6 only) | keep; recipe swap (step 9) |
| Zinc Sheet *overlaps w/ CE zinc plate* | `creativeengineering:zinc_plate` (`i.plate_zinc`); CC&A, Create Deco, Vintage zinc sheets already removed | duplicate | remove PG's like the others; recipes use `#forge:plates/zinc` |
| Copper Plating (+ slab, stairs) *overlaps w/ Create Deco, IE sheet metal* | `createdeco:copper_sheet_metal`, IE `sheetmetal_copper` | decorative duplicate | D11 |
| Switches, HV Breaker, LV Button, Fuse Holder | IE breaker switch, redstone breaker | IE ones switch IE wires | keep |
| Redstone Converter | IE redstone connector | different | keep |
| Punch Card, Reader *overlaps w/ Control Engineering punched tape* | `controlengineering:punched_tape`, `keypunch` | similar idea | keep or drop (D10) |
| Plotter | `projectred_fabrication:plotting_table` (name only) | different | keep |
| CRT, Encased CRTs *overlaps w/ Control Engineering CRT* | `controlengineering:crt_tube` ("Cathode-Ray Tube", `i.crt_tube`) | near-identical name | D10, rename if kept |
| Modular Display, Display Module | Create display board, nixie tube | different | D10 |
| Alarm Bell | `securitycraft:alarm` | similar | keep |
| Spark Gap, Grounding Rod, Carbon Pile, Socket, Conductive Casing, Generator Clutch, Electromagnet, Rheostat, Varistor, Potentiometer, Resistors | none | new | keep |

### 2.2 Systems

| System | Pack today | PG | Overlap |
|---|---|---|---|
| Power transport | IE LV/MV/HV wires carry FE between connectors, losses by wire type and length; CC&A's wire network removed | wire entities with resistance, voltages solved per tick | **two parallel power networks.** They meet only through PG's FE Inverter and Device Connector (section 3.3) |
| Rotation to power | CC&A Alternator (SU to FE), IE Kinetic Dynamo | rotor/winding generator with inertia, field and back-EMF | same role, very different depth |
| Power to rotation | CC&A Electric Motor (FE to SU), D&D kinetic motor | Electric Motor, Constant Speed Motor, Servo | same role; name clash |
| Storage | CC&A accumulators, IE capacitors | Battery (acid cell, 120 Wh), Portable Battery | different |
| Heat | Create blaze burners, Compressed Creativity heater | Basin Heater (up to superheated), Heating Coil (fan blasting at 0.75 time) | the Heating Coil adds a faster bulk-blasting route |
| Fan processing | Create fans plus pack Dragon Breathing ([engineering.md](engineering.md)) | heating coil in a fan stream; fans cool hot devices | PG mixes into `AirCurrent` and fan processing; must coexist with Dragon Breathing |
| Circuits | PneumaticCraft PCB, IE logic circuit, ProjectRed fabrication, Control Engineering | simulated components on boards, schematic editor | fifth chain (D5) |
| Crop growth | seasons, crop rules ([crops.md](crops.md)) | Growth Lamp random-ticks crops in radius 2 | must respect season gates (step 14) |
| Cooking heat | FD heat sources tag | mixins make PG heaters heat Cooking Pot and Skillet | check against the pack's heat-source tag rules |

## 3. New content and the voltage system

### 3.1 How the voltage system works

The simulator lives in `org.patryk3211.powergrid.electricity.sim` (8.3k lines). It imports almost nothing from
Minecraft: `Mth`, one `Pair` from Create's Catnip library, and a debug item. It is a small circuit simulator in the
SPICE family.

**Model.** A grid is a graph of *nodes* (terminals, each with a voltage) and *wires* (two-terminal elements with a
conductance G = 1/R). Ideal sources and transformers add *coupling nodes*, extra unknowns that carry a branch
current. `ElectricalNetwork` stamps every element into one matrix (the MNA Jacobian: node conductances plus coupling
rows), stamps sources into a right-hand side, and solves for the state vector (all node voltages and coupling
currents).

**Solve per tick** (`JavaMNA.singleTick`):
1. Build the residual from static sources and the current state.
2. Newton-Raphson: solve J * x = r with EJML (dense or sparse, picked by `DynamicallyTypedMatrix`), rows and columns
   scaled for conditioning.
3. Line search: if the new state is worse, step back halfway (up to `solverMaxSearchAlpha` 0.99).
4. Repeat until the error is below 1e-7 absolute (or 1e-14 relative change), up to 200 iterations.
5. Non-linear parts (diodes, BJTs, tubes, varistors, neon bulbs, solar cells, the generator's back-EMF) are
   *solver hooks*: each iteration they re-linearise around the current state, with smoothing factors
   (`diodeLimAlpha` 0.025, `bjtLimAlpha` 0.5, triode 0.5) to keep Newton stable.
6. A floating grid (no ground rod, no current source) gets a 1000 S anchor on one node so the matrix is not
   singular; `G_MIN` 1e-8 is added for the same reason.

**Time.** One step is 0.05 s (one tick). `multiTicks` (default 1) splits a tick into sub-steps for reactive parts.
Capacitors and inductors are companion models (`CapacitorWire`, `InductorWire`, `CRSeriesWire`, `LRSeriesWire`:
conductance and history current from dt), so RC/LR transients and oscillators work. There is **no AC**: every source
is DC or slowly varying. Transformers are ideal ratio couplings (`TransformerCoupling`: V_secondary = ratio * V_primary
with a series resistance), so they work on DC; that is a game simplification. A transformer has a configurable winding
count per side (wound with wire items through a GUI); the ratio is the turns ratio.

**Grid topology.** `WorldNetworks` keeps one `ElectricalNetwork` per connected component. Placing or cutting a wire
merges or splits networks (`NetworkGraph`, `GraphedElectricalNetwork`), and the matrix is rebuilt when the node set
changes or when accumulated conductance changes pass a limit (40 updates per node or a sum of 1000 S). Small changes,
such as a switch toggling or a motor changing resistance with load, are applied as matrix deltas. Two experimental
options cut big grids into islands: transmission lines above 0.2 ohm and transformers become delay elements
(about one tick of lag, some capacitance) so each island solves alone (`splittingTransmissionLines`,
`splittingTransformers`, both off by default).

**Client mirror.** The client runs its own copy of each network (`ClientWorldNetworks`, `calculate(1)` per tick) so
gauges, lights and wire glow move smoothly; the server sends full state every 100 ticks
(`fullStateSynchronizationInterval`) and deltas in between, in doubles by default. This doubles the solver cost on a
single-player host and puts it on every client in multiplayer.

**Wires.** Wires are entities (`HangingWireEntity`, `BlockWireEntity`, cord, string light cord) between terminal
points. A wire item's properties come from the `powergrid:wire_types` datapack registry, keyed by item id:

| Wire | Ohm per item | Max length (blocks) | Items per block | Max current (A) | Notes |
|---|---|---|---|---|---|
| Copper Wire | 0.0015 | 24 | 0.5 | 80 | bare |
| Iron Wire | 0.005 | 64 | 0.5 | 160 | long spans, lossy |
| Golden Wire | 0.003 | 12 | 0.5 | 160 | short, high current |
| Insulated Copper Wire | 0.0015 | 16 | 0.5 | 70 | dyeable, safe to touch |
| Copper Cord | 0.0015 | 16 | 0.5 | 60 | two conductors, plugs into devices |

A 24-block copper span costs 12 wires and is 0.018 ohm. Wires heat by I^2 R against a thermal mass and burn above their
limit (`wireOverheating`). Bare wires are live: an entity touching two wires closes a 5000 ohm circuit and takes
damage above 0.1 A; cutting a wire carrying more than 0.2 A hurts.

**Devices.** A device is a block entity with `ElectricBehaviour` (its terminals and internal wires) and usually a
`ThermalBehaviour`. Each device is a fixed or variable resistance, so it draws P = V^2 / R: **the voltage you feed it
decides how hard it runs, and too much voltage cooks it.** Default resistances (config `CResistance`, per block):

| Device | Resistance | Thermal max power (W) | Behaviour |
|---|---|---|---|
| Electric Motor | 25.6 ohm (divided by load, min load 5 %) | from max RPM | speed = (V^2/R) / torque * 94.25; torque = 64 SU * 15 = 960; **256 RPM at about 258 V, about 10 A, 2.6 kW** |
| Constant Speed Motor | 25.6 ohm | | fixed speed, voltage sets stress capacity and direction |
| Servo | 12.8 ohm on, 64 ohm idle | | control pin -5..5 V = -360..360 degrees |
| Heating Coil | 25 ohm | 60 | heats a fan stream; higher voltage reaches bulk blasting |
| Basin Heater | 10 ohm | | heat levels up to superheated by power |
| Electric Fan | 20 ohm | | speed and direction from voltage |
| Electric Pump | 480 ohm | | 24-block range, up to 256 RPM equivalent |
| Electromagnet | 15 ohm | 1500 | magnetizes items below (`magnetization` recipe) |
| Winding (generator coil) | 7.5 ohm | 200 | field or armature coil |
| Contactor | coil 12 ohm, contacts 0.05 ohm | 252.8 | relay for heavy loads; holds at 90 % of pull-in current |
| Switches LV / MV / HV | 0.15 / 0.05 / 0.1 ohm | 38.4 / 51.2 / 1000 | HV switch is kinetic and arcs if opened too slowly |
| Transformers small / medium / nether | turns ratio, Al 1.5 / 10, coupling K 0.9999 / 0.99999 | 1000 / 4000 / 4000 | nether one links across a portal |
| Voltage Gauge | 20 k to 20 M ohm by range | | parallel meter |
| Current Gauge | 0.05 ohm | 35 | series meter |
| CRT | heater 12 ohm, coils 40 ohm | 100 | needs 12 V heater and about 1000 V anode |
| Battery (acid) | 0.1 ohm full | 100 | 120 Wh, starts at 90 %; voltage and resistance drift with charge |

**Heat.** `ThermalBehaviour` integrates temperature from the power a device burns against its thermal mass and a
dissipation factor tuned so the rated power settles 25 degrees under the overheat point (default 175 C). Ambient comes
from biome temperature (13.65 * base temp + 7.1). Create encased fans blowing over a device cool it (0.01 per RPM);
that is the intended way to run things hard. Past the limit a device smokes, then breaks, and explodes if
`explosiveDeconstruction` is on.

**Generators.** A generator is a multiblock: a Generator Clutch (redstone-tuned soft coupling to the kinetic network),
rotors (own angular velocity and inertia, max 272 RPM, up to 8 long), a commutator (DC output) and windings on shafts
around it (the stator). EMF = field * angular velocity, field from the field winding's current, saturating at 2 A
(`fieldSaturationCurrent`). Load current puts counter-torque on the rotor (`GeneratorCoupling.postUpperSolve`), so a
heavier load slows the rotor and draws stress from Create through the clutch (PD control, Kp 0.85, Kd 0.002). Generators
can self-excite (field fed from their own output) and be regulated with a Carbon Pile (current-controlled resistor).

**Other sources.** Solar panels follow a diode IV curve (Voc 26.4 V, Isc 3.3 A per panel, multiblock up to 25 panels,
series and parallel strings set on the bearing, hotter biomes lose output); potato battery; acid battery; FE Inverter;
creative voltage and current sources; a lightning attractor (lightning rods and wool on a spinning bearing) raises
the lightning rate.

**Protection and control.** Fuses (set rating, burn above it), HV breaker (wind up, redstone trip), contactors and
relays (coil-driven), varistors (voltage clamps), grounding rod (0 V reference; ground resistance 5000 ohm down to 1 ohm
with 10 to 64 conductive blocks nearby; a bad ground electrifies the area), redstone converter (signal to resistance),
gauges and energy meter (Wh / kWh, comparator pulse per unit).

**Performance.** Each network costs one sparse or dense solve per tick per Newton iteration; linear grids converge in
1 to 3 iterations, grids with tubes or diodes can use many. Matrix size is the node count, so one big factory grid is
one big solve. The mod has a `PerformanceCounter` and `/powergrid` debug; a pack test needs a large-grid TPS check
(PWG-27).

### 3.2 Ways to bring a voltage system into the pack

| Route | What | Effort | Gain | Risk |
|---|---|---|---|---|
| **A. Full port** | fork the 1.20.1 source to `development/backportedPowerGrid`, port every feature | largest: every renderer, ponder scene, GUI and Create hook | the whole mod, upstream ids kept | long tail of Create 6 API rewrites; circuits editor GUI; huge test surface |
| **B. Phased port (recommended)** | same fork, but phase 1 registers only the electrical core: wires, connectors, terminals, solver, thermal, gauges, switches, fuse, resistor, transformers, battery, motor and generator, FE Inverter and Device Connector, creative sources. Later phases add lights, heaters, fan and pump, solar, then components and circuits | phase 1 is roughly a third of the mod | the voltage system in play quickly, with the FE bridge into IE and CC&A | the phase plan must keep unported blocks unregistered, not half-registered |
| **C. Own simple system** | write a pack voltage system in Creative Engineering (fixed tiers or a plain resistive solver) | medium | fits pack style | reinvents what PG and IE already do; IE already has LV/MV/HV tiers |

**Recommendation: B.** The solver core is the most valuable part and the cheapest to port (pure Java plus EJML);
everything the player sees is the expensive part, and phasing lets the owner judge the gameplay before the circuit
editor and its 10k lines are touched. Keep the mod id `powergrid` (port rule: upstream ids stay).

A lighter variant of B, if the owner wants the voltage idea but not PG's content: lift only the `sim` package
(Apache-2.0, keep the notice) into Creative Engineering as a library and build pack devices on it. That is route C
with a proven solver, and it means writing the wire entities, terminals and sync layer ourselves (about 6k lines in
PG's `electricity/base`, `wire`, `WorldNetworks`, packets). Not recommended unless PG's own blocks are unwanted.

### 3.3 Where PG meets the pack's FE

- **FE Inverter** (FE to grid): takes FE from any FE source and drives a voltage source; a control pin (0 to 20 V,
  `feInverterControlVoltage`) sets the output voltage; FE buffer 20 000 (`feInverterBufferSize`) also caps the maximum
  voltage.
- **Device Connector** (grid to FE): wired like a load, it fills an FE buffer in the device it is attached to, and can
  power PG-terminal-less devices. Buffer size is `forgeEnergyPerVolt` (2 FE per volt), cost is `forgeEnergyPerWatt`
  (10 FE per watt).
- So IE generators, CC&A alternators and PneumaticCraft dynamos can feed a PG grid, and a PG grid can run IE machines,
  CC&A machines and AE2 (through Create Applied Kinetics or AE2 energy acceptors). The exchange rate is the balance
  lever (D3).

### 3.4 Other new content (summary)

- **Components (28 component items, `component_items` registry):** resistors, potentiometer, capacitor, diode, NPN/PNP
  BJT, static induction transistor (vFET), triode, pentode, thyratron, barretter, regulator tube, neon bulb, varistor,
  relay, double pole relay, redstone relay, compact gauges, display module, labels, vias. Placed on circuit boards
  (traces, two layers, vias between boards), cooled by fans.
- **Circuit design:** Circuit Design Table (480 ohm load) writes Circuit Schematics; boards are etched with Blazing
  Acid and filled by sequenced assembly and Mechanical Arms; an Integrated Circuit can **boost** compatible equipment
  (better performance, double power draw, limited uses; `boost_recipe`).
- **Equipment:** Portable Battery (chest slot, 20 000 energy, Capacity enchant +20 000 per level), Portable Drill
  (diamond-level, speeds up with use), Portable Saw, Electro-Baton (knockback and stun), Electro-Zapper (chain lightning),
  Multimeter, Wire Cutters, Electrical Gizmo.
- **Display and control:** CRT (oscilloscope-like dot, deflection coils), Modular Display with flip modules, Plotter
  (voltage over time), Punch Card Reader (kinetic sequencer), Alarm Bell, Redstone Converter, Energy Meter.
- **Lights and decor:** light bulbs (dyeable), LV bulb, neon bulb, Light Fixture, Factory Light (projects light 16
  blocks down), String Lights (dye patterns), Ceiling Tiles (lamp, solar, wire, junction), Copper Plating.
- **Kinetic extras:** Rheostat and Variac (kinetic-tuned resistance and ratio), HV Switch, Generator Clutch, Servo.
- **World hooks:** lightning can magnetize iron ingots (5 %); lightning attractor; solar biome overrides.

### 3.5 Upstream issues found

| # | Issue | Evidence |
|---|---|---|
| B1 | No LICENSE / NOTICE file in the jar (Apache-2.0 needs the licence text shipped) | jar listing; add both in the fork |
| B2 | `NativeMNA` writes `.pg-native/` in the game folder and loads a native library | `NativeMNA.tryLoad`; drop in the port (D4) |
| B3 | Typo config key `splittingsTransformers` | `CSolver.java` |
| B4 | `generatorCommutatorInertia` and the other inertia keys have no comment | `CGenerator.java`; add comments in the port |
| B5 | **Fixed in the port (P1).** `ElectricalNetwork.updateConductance` returns early for any wire with a ground (null) end, so adding, removing or re-valuing a wire to ground on a network that has already solved is ignored until a full matrix rebuild | upstream's own `StructureChangeTests` (3) and `TransformerTests.transformer1P2S` fail at tag v0.6.2 and pass with the fix; harness gate 8 (`tools/test_power_grid_solver.sh`) |
| B6 | **Fixed in the port (P3).** The grounding rod's touch damage grows with distance: `GroundingRodBlockEntity.tick` computes a falloff `sourceDamage / max(distance^2, 1)` and then hurts with `sourceDamage` divided by that falloff, which is the squared distance | code reading (`GroundingRodBlockEntity.tick`, tag v0.6.2); the port hurts with the falloff value; CQ-PWG-12 |
| B7 | **Fixed in the port (P3).** Config comment of `lightningMagnetizationChance` says a lightning strike turns an iron ingot into a magnet; the code (`EntityMixin`) converts andesite alloy (also what the JEI conversion shows). Test PWG-31's "iron ingots" came from the comment | `CRecipes`, `EntityMixin`; SQ-PWG-33 checks that iron ingots give nothing |
| B8 | **Fixed in the port (P2 run).** `/powergrid performance get <counter>` throws a NullPointerException before the counter has measured anything (`PerformanceCounter.getTimestamp` formats a null date) | scratch run r1113, SQ-PWG-13 (P2 jar `-03`); same code at tag v0.6.2; fixed in `-04` |
| B9 | **Fixed in the port (P4).** The servo commits a new target angle even when its coil power gives a speed of 0 RPM: the angle is marked reached without turning (`rotation / convertToAngular(0)` also overflows the move's tick count), so after power returns it never turns to that angle; and after any move the coil keeps the moving resistance (12.8 ohm instead of the idle 64 ohm) until the block entity reloads | code reading (`ServoBlockEntity.tick` / `read`, tag v0.6.2); the port waits for power and goes back to idle when a move ends; SQ-PWG-43 |
| B10 | **Fixed in the port (P6).** `SolarBiomeRegistry.forBiome` sets its result on every registry entry (`result = match ? entry : null`), so only the last entry can ever apply: with two or more `solar_biome_override` files every override but the last one's biome is ignored | code reading (tag v0.6.2); the port's reload listener keys overrides by biome; SQ-PWG-54 uses two files |
| B11 | **Fixed in the port (P7 jar `-09`).** Config comment of `growthLampChance` said the chance value is divided by the lamp's power level; `GrowthLamp.State.specialEffects` uses it as is (1 in N per block per tick, at any lit power level). Upstream-same behaviour kept; the comment now says what the code does (as B7) | code reading (`CElectricity`, `GrowthLamp`); SQ-PWG-53 records the rate |
| B12 | **Fixed in the port (P6 jar `-08`).** No `particles/zap.json` ships (the zap particle draws itself). 1.20.1 skips a missing particle description; 1.19.2 fails the client resource load, so every port jar from P3 (`-04` to `-07`) crashed clients | crash report r1113 (client); `zap.json` added, `pg_phase_check.py` checks client assets in every build |
| B13 | **Fixed in the port (P7).** The Electro-Zapper's chain effect (`ZapProjectileS2CPacket`, client) places its sparks with joml `Math.lerp(fraction, start, end)`; joml's order is `(a, b, t)`, so each spark landed at `fraction + (start - fraction) x end` instead of along the arc between the two mobs. The port uses `Mth.lerp(fraction, start, end)`. Visual only | code reading; CQ-PWG-17 looks at the arcs |
| B14 | **Fixed in the port (P7).** `ElectroBatonItem.hurtEnemy` returns before any wear when the attacker is a player, so a player's baton never loses durability, with or without a battery; its tooltip (and the drill, saw and zapper code) say durability is used only when the worn battery gives no energy. The port wears 1 per hit when the battery gives no power (no battery, empty) | code reading, lang `electrobaton.tooltip`; SQ-PWG-59 |
| B15 | **Fixed in the port (P8).** Breaking a Punch Card Reader with a card in it loses the card: `PunchCardReaderBlock.onRemove` calls `super.onRemove` first, which removes the block entity (`IBE.onRemove`), and only then looks for the block entity to drop its contents | code reading (tag v0.6.2); the port drops the card first; SQ-PWG-65 |
| B16 | **Fixed in the port (P8).** `DisplayBurnoutS2CPacket.handle` (modular display slot burnout) runs on the network thread: it spawns particles and plays a sound off the client thread, and throws when the block at the position is no longer a modular display (`getValue(HORIZONTAL_FACING)`) | code reading (no `context.queue`, unlike the other client packets); queued and checked in the port; CQ-PWG-21 |
| B17 | **Fixed in the port (P8).** The CC: Tweaked redstone converter peripheral's `setValue` / `clearValue` run on the computer thread and write the block entity's override from there (no `mainThread`) | code reading (`RedstoneConverterPeripheral`); the port marks both `mainThread = true`; SQ-PWG-66 |
| B18 | **Recorded, not changed (P9a).** The tube wires (`ElectronTubeWire`, `PentodeWire`) move their evaluation point by at most `0.5 * triodeLim*` V per Newton iteration (0.25 V with the default config) and build the residual at that lagged point, so a tube that starts in cut-off with a large anode step reports `isConverged()` at once while its operating point stalls and then walks for about 35 ticks (1.7 s; triode, 250 V through 10 k, grid -4 V); heat is counted only as the walk allows (thermal units skip a non-converged source) | offline harness `BoardComponentTests` (PORT_NOTES.md 10.1); SQ-PWG-06 `zpwg9 watch` |
| B19 | **Fixed in the port (P9a jar `-12`; P4 code).** `WindingBlockEntity.addParallel` returns on re-entry only when logging is enabled (`if(adding && logsEnabled())`) and hands the add to its direct owner, so two windings that name each other as owner (windings placed over windings that already have owners, or broken and re-placed next to them) recurse until a StackOverflowError crashes the server; the port returns on any re-entry and follows the owner chain to its root (a loop or a missing owner makes the winding the owner) | crash report of the `-10` scratch boot (2026-11-13 10:27); SQ-PWG-71 |
| B20 | **Fixed in the port (P9b jar `-14`; P7 code).** The worn portable battery charges the held FE item by asking for the item's whole free space, and `BatteryUtils.drawEnergy` zeroes the battery's charge and returns 0 when the charge is below the request: an item with a large buffer (AE2 wireless terminal, 3.2M FE) empties a full battery each tick and receives nothing. The port caps the request at the battery's charge (`BatteryUtils.availableEnergy`), and `drawEnergy` leaves the charge as it is when it cannot meet a request (the drill, saw, baton and zapper ask a fixed amount per use: below it they act unpowered as before, without losing the rest of the charge) | SQ-PWG-57 on `-10` (r1113, `fe 0/3200000 -> 0/3200000, charge 20000/20000 -> 0/20000`) |
| B21 | **Fixed in the port (P9b jar `-14`).** `ChangeScreenC2SPacket` and `SaveSchematicC2SPacket` act on whatever block position the client sends: a client can open a design table's screens without power or reach, overwrite any table's design or a board's (creative), and make the server load an unloaded chunk (`getBlockEntity`); the port requires a loaded block within container reach (8 blocks from its centre) | code read while porting; SQ-PWG-73 |
| B22 | **Fixed in the port (P9b).** The board editor (creative, empty hand on a board) has a name field, but the save handler applies the name only to design tables, so a board's new name is dropped | code read; SQ-PWG-75 |
| B23 | **Fixed in the port (P9b).** Writing a design to an item replaces the table's output slot, deleting a schematic still lying in it (and consuming the blank); the port writes only into an empty output slot | code read; SQ-PWG-73 |
| B24 | **Fixed in the port (P9b).** The editor does not record two kinds of change: removing a part with a right click and changing an option property (enum widget); Esc then closes without the unsaved-changes warning and the change is lost; both now mark the design changed | code read; CQ-PWG-24 |
| B25 | **Fixed in the port (P9c jar `-15`).** `ArmBlockEntityMixin` (stops an incomplete circuit on a belt segment an arm deposits to) returns from its whole loop over the arm's outputs at the first output that is not a belt, so an arm that lists a depot or any other non-belt output before the belt never stops the circuit, which rides past unfinished; the port skips such outputs | code read while porting; SQ-PWG-82 (a full depot as first output) |
| B26 | **Fixed in the port (P9c).** `IncompleteCircuitItem`: the tooltip throws a NullPointerException when the synced `component_items` table has no item for a missing part (an edited datapack), building the assembly tag throws for a part whose component is not registered, and the progress bar divides by a zero part count; the port shows the component id, skips unknown parts and shows 0 | code read; CQ-PWG-26 (tooltip) |
| B27 | **Recorded, not changed (P9c).** An Incomplete Circuit whose design has no parts, or one etched from an Unetched Circuit assembled on a blank Circuit Schematic (no design), can never be completed: no part is accepted and nothing else turns it into a board (a dead-end item that used acid and materials) | code read (`IncompleteCircuitItem.insert`); evaluation point for P11 (require a design in the etching or the assembly) |
| B28 | **Fixed in the port (P9d jar `-16`).** `BoostingChipItem.finishUsingItem` boosts whatever the other hand holds when the 32-tick use ends and uses the chip either way: with the chip in the off hand, a hotbar scroll during the use puts any item in the main hand (vanilla stops the use only when the used hand changes), so a sword gets a `Boosted` tag it never reads, a block gets nothing, and the chip is gone; the port checks the boost recipe again at the end and keeps the chip without a match | code read while porting; SQ-PWG-86 `handswap` |
| B29 | **Fixed in the port (P9d).** Only the portable drill and saw show the "Boosted" tooltip line; the Electro-Baton and Electro-Zapper, boosted by the same recipes, show none, so a boosted one looks like a plain one (the light blue bar shows only while a battery powers it); the port adds the line to both | code read; CQ-PWG-27 |
| B30 | **Fixed in the port (P10 jar `-17`).** `StringLightCordEntity.makeWire` does not drop the cord's existing wires first (the copper cord's `makeWire` does); `ElectricBehaviour` calls it again when it rebuilds an endpoint's connections, so each rewire leaves the previous two cord wires and both filament loads in the network with no owner: the far end keeps the source voltage and the source keeps feeding the load after the cord is cut or broken; the port calls `dropWire()` first | code read; SQ-PWG-95 |
| B31 | **Fixed in the port (P10).** A string light cord clears the light blocks it placed only when its unsaved "lit" flag is set, so a cord that comes back unlit after a chunk reload or restart leaves its light blocks (light 5) in the air for good; the port starts the flag set, so the first check of an unlit cord clears them | code read; SQ-PWG-95 |
| B32 | **Fixed in the port (P10).** The ceiling tile lamp loses its light bulb when broken (no drop, unlike the light fixture) or wrenched (only the factory light comes back); the port drops it and returns it with the wrench | code read; SQ-PWG-91 |
| B33 | **Fixed in the port (P10).** Copper plating slab and stairs need the right tool (soft metal properties) but are in no `mineable` tag, so they drop nothing when broken; the double slab's loot drops one slab; the port tags both for the pickaxe (as the plating) and drops two from a double slab | code and data read; SQ-PWG-90 |
| B34 | **Fixed in the port (P10).** Crafting a string light cord with no dye (clearing its pattern) gives a cord with an empty `Pattern` tag that no longer stacks with plain cords; the port returns a plain cord | code read; SQ-PWG-90 |

No upstream bug was found while porting P2 (grid base); B8 came from its scratch run. The P2 to P4 changes are port changes (plan_power_grid.md P2 to P4, PORT_NOTES.md sections 11, 13 and 14). No upstream bug was fixed in P5 (ponder). Seen and kept as upstream plays it: `ElectricityTickInstruction` never completes (its `tick` skips the countdown), so every `tickFor(n)` in a scene keeps ticking the ponder level's networks to the end of the scene; and two structure-file quirks that load the same on 1.20.1 (`creative_voltage_source` saved with an old `axis` property in 6 files, a numeric `axis_along_first` in `wire/voltage_drop`; PORT_NOTES.md 15). Test wording found wrong in P4 (section 8, not upstream bugs): PWG-06's "120 V about half speed" (motor speed goes with the square of the voltage: 120 V is about 55 RPM, half speed is about 182 V) and PWG-10's "raises Create stress" (the clutch's stress is the assembly's impact x coupling and does not change with electrical load; the load shows as the clutch's load value, display source and comparator output). Found in P7: PWG-37's "Wire Cutters or saw on a sheep" (only `#powergrid:wire_cutters`, which holds the wire cutter alone, shears in `SheepMixin`; the portable saw does not, upstream-same; SQ-PWG-61 checks both). No P7 upstream code changed behaviour except B13 and B14. Found in P8: PWG-40's "gauge or display as a peripheral" (upstream has no display peripheral: the peripherals are the voltage, current and power gauges, energy meter, battery, generator clutch and redstone converter; wording corrected). No P8 upstream code changed behaviour except B15 to B17; the modular display's value box and module type board needed port changes for Create 0.5.1 (PORT_NOTES.md 19).

### 3.6 Port changes (1.20.1 / Create 6 to 1.19.2 / Create 0.5.1.i)

Counts are files in the decompile that use each API.

| Area | Files | Change |
|---|---|---|
| Solver core (`electricity/sim`) | 72 | almost none: replace Catnip `Pair`, keep EJML 0.44 as jar-in-jar (Java 17 is fine) |
| Architectury | 65 | pack has Architectury 6.6.92 (1.19.2), so `NetworkManager`, events and menu registry calls mostly stay. `@ExpectPlatform` needs the Architectury Gradle plugin, which `build-logic` does not use: inline the forge impls (forge-only build) (D2) |
| Create Catnip (`net.createmod.catnip`) | 170 | Create 6 split helpers into Catnip; on 0.5.1 they are in `com.simibubi.create.foundation.utility` (Lang, VecHelper, Pair, AnimationTickHolder, Theme, config base, gui elements). Mostly import and name changes |
| Create API (`com.simibubi.create.api`) | 49 | stress values, display sources, arm interaction points, fan processing types, contraption storage moved into `api` in 6. Map each to its 0.5.1 class (`content.kinetics.BlockStressValues`, `kinetics.fan.processing.FanProcessingType`, `mechanicalArm.ArmInteractionPointType`, all present in 0.5.1.i); check the fan processing type against the pack's Dragon Breathing hook (memory `create-fan-processing-type`) |
| Flywheel 1.0 visuals (`dev.engine_room.flywheel`) | 33 | Create 0.5.1 uses Flywheel 0.6 instances. Drop the visuals and rely on the block entity renderers (already present for most blocks), or rewrite as 0.6 instances later |
| Ponder (`net.createmod.ponder`) | 29 + 5.3k lines | Create 6 ponder is a separate library; 0.5.1 has `foundation.ponder.SceneBuilder` with a similar scene API. Port scenes in later phases or ship without ponder (D12) |
| `GuiGraphics` | 32 | 1.20 GUI API; back to `PoseStack` + `GuiComponent` (circuit editor is most of it) |
| Registries | 19 | `core.registries.Registries` / `BuiltInRegistries` to `Registry.*_REGISTRY` / `ForgeRegistries` |
| Datapack registries (`wire_types`) | 3 | 1.20 dynamic registry; on Forge 1.19.2 use `RegistryBuilder.dataPackRegistry` or a JSON reload listener with a sync packet (simpler) |
| Damage types | 8 | data-driven in 1.19.4+; back to `DamageSource` constants, drop `data/powergrid/damage_type` |
| `ItemDisplayContext`, `RenderType`, `Holder` | 8 / 47 / 16 | `TransformType`; render types mostly stay; holder getters |
| Creative tabs | 2 | `CreativeModeTab` subclass |
| Blocks | all | `Material` on block properties; `BlockBehaviour.Properties` changes (`mapColor`, `instrument`) |
| Mixins | 20 | retarget each to 0.5.1 (the Create targets all exist in 0.5.1.i under `content.kinetics.*` / `foundation.*`, checked in the jar): `AirCurrent`, fan processing, `ArmBlockEntity`, `ArmInteractionPoint`, `BeltDeployer`, `Chute`, `FluidPropagator`, `BasinRecipe`, `RecipeApplier`, `BlueprintOverlay`, `ValueSettingsScreen`, FD `CookingPot`, `Skillet`, vanilla `Entity`, `Player`, `Sheep`, `BlockStateBase` |
| Registrate | many | use the Registrate version Create 0.5.1.i depends on (`CreateRegistrate`); expect small builder differences |
| Recipe JSON | 116 | drop `category`, `show_notification`; `create:cardboard` (4 recipes) has no 1.19.2 item; `create:copper_sheet` is removed in the pack |
| Loot | 74 | 1.20 condition names to 1.19.2 (`any_of` to `alternative`) if present |
| Advancements | 96 | drop (pack drops them; port rule) |
| Native solver | 1 | drop |
| Compat | 2.2k lines | keep JEI and CC: Tweaked (both installed), keep Cold Sweat and TFMG only if the mods exist for 1.19.2 in the pack (they are not installed), drop REI |

## 4. Fold-in, in build order

Assumes route B. Files: the fork in `development/backportedPowerGrid` (mod id `powergrid`); pack scripts for PG in
`kubejs/server_scripts/` under the Engineering trees used for IE/CC&A ([recipes_engineering.md](recipes_engineering.md)),
ids in Creative Common's dicts.

0. **Fork and build skeleton.** Fork the `architectury-1.20.1/dev` source at the 0.6.2 tag; add LICENSE and NOTICE
   (B1); root `settings.gradle`, `installToMods`, `IN_TREE_MODS` in `tools/build_deploy.py`; EJML jar-in-jar through
   `build-logic`. Phase list in a plan file (`plan_power_grid.md`).
1. **Solver core.** Port `electricity/sim` and `circuits/thermal`; javac harness tests on known circuits (divider, RC
   charge, transformer ratio, diode bridge) against hand-calculated values (section 7).
2. **Electrical base, wires, sync.** `electricity/base`, `wire`, `wireconnector`, `WorldNetworks`, packets, client mirror.
   `wire_types` as a reload listener.
3. **Phase 1 devices** (section 3.2) with BER renderers, no ponder. Unported blocks are not registered.
4. **Dumps and generators:** `/dumpall` after install (memory `dumpall-refresh-workflow`); generators read the new ids.
5. **Ids in `item_dict.js` / block dict** for every registered PG item, under the `powergrid` namespace (memory
   `ids-follow-owning-mod`).
6. **Item merges and removals** ([item_merges.md](item_merges.md)): PG `wire`, `iron_wire`, `golden_wire`, `zinc_sheet`
   out (`removed_items.js`); IE `wire_copper`, CE `iron_wire`, CE `gold_wire` registered as PG wire types (needs the
   placement code to accept any item with a wire type, D6).
7. **Tags via `global.tags`:** take `insulated_copper_wire` out of `forge:wires`; CE `wirecoil_copper` into
   `forge:copper_coils` (and `wirecoil_iron` into `forge:iron_coils`) if PG's Copper Coil goes (D6); `forge:plates/zinc`
   stays CE's.
8. **Upstream recipe removals and literal inputs to tags** through `global.NCH`: literal `create:copper_sheet` to
   `#forge:plates/copper`; `create:cardboard` to paper or `#forge:plates/...` per owner (D13). The via component
   (`component_items` `via` and the item tag `powergrid:circuit_component`) names `create:copper_nugget`, which
   Creative Common's `removed_items.js` removes, so the tag loads 27 of 28 in the pack (SQ-PWG-67, 2026-11-13): needs a
   pack-side answer (alias the removed nugget, or add the pack's copper nugget to the tag and the via entry).
9. **Recipes by pack rules:** CE components and coils where PG uses its own parts (memory `ce-components-coils`);
   check every PG Create recipe still runs on 0.5.1 (sequenced assembly transitional items).
10. **FE bridge balance** (D3): set `forgeEnergyPerWatt` / `forgeEnergyPerVolt` against IE wire tiers and CC&A rates;
    copy the edited server config to `configs_edited/`.
11. **Thermal and explosions** (D4): pack defaults for `explosiveDeconstruction`, `overheating`, `wireOverheating`.
12. **Fan processing:** Heating Coil fan route and PG's `AirCurrent` mixin tested with the pack's Dragon Breathing
    type ([engineering.md](engineering.md)).
13. **Potions, effects, food, cans, compost:** none (PG has no effects, potions or food).
14. **Crops and seasons:** Growth Lamp random ticks must not bypass season gates ([crops.md](crops.md)); test PWG-22.
15. **Loot:** block self-drops only; none to fold ([loot.md](loot.md)).
16. **Worldgen / structures:** none.
17. **Names and lang** ([names.md](names.md)): renames from D7 through the pack's lang route (memory
    `resource-pack-lang-priority`).
18. **Mob and block audit:** mineable tags, BlockRunner tier, Item Physic, WTHIT (PG has its own goggle tooltips; check
    WTHIT shows voltage/current, memory `wthit-plugin-patchermod`).
19. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): wires and components.
20. **Tooltips and JEI** ([tooltips.md](tooltips.md), [jei_coverage.md](jei_coverage.md)): PG's JEI plugin (magnetization,
    boost); hide unported items.
21. **Release:** `mod_sides.md` row (both sides: blocks, entities and client mirror), changelog bullets (new mod, phase
    content), TODO, plan row; scratch queue rows.

Ordering: 0 to 3 before anything else; 4 after the first install; 5 before 6 to 9; 6 before 7 and 8; 10 to 12 need a
running grid; 17 after D7.

## 5. Decisions

### 5.1 Owner decisions (2026-11-12)

**Delivery model.** If PG is implemented, it is ported **fully, in its phases, before any integration with the pack or
other mods starts**. A config switch then picks the electricity mode:

- **Basic:** electricity as the pack has it today (IE wires and FE, CC&A where it stays), plus any later changes to that
  same electricity core within the current basic context. No PG blocks or items are active, so nothing clashes.
- **Advanced:** includes every PG change and integration. All power sources and consumers in the pack work on PG energy
  (grid voltage, not plain FE).

| # | Decision | Recorded answer |
|---|---|---|
| D1 | Route | Phased port (3.2 route B): phase 1 electrical core, later phases lights, heaters, fan, pump, solar, then components and circuits |
| D2 | Architectury | Recommendation: keep the Architectury 6 API calls, inline `@ExpectPlatform` |
| D3 | FE rate | Recommendation for phase 1: keep upstream (10 FE per W, 2 FE per V); retune after PWG-18. Advanced mode makes the rate less central (see H1) |
| D4 | Overheating | Overheating on. **No explosions, no block break.** An overheated component stops working and turns into an **Overheated Component** block: a cracked/mining-overlay on the overheated block's own texture, keeps all block data for inspection, and on break has only a chance to drop the component item. Native solver dropped |
| D5 | Circuits | Later build phase; integration with other mods' circuits (PneumaticCraft, IE, ProjectRed, Control Engineering) is postponed until after basic integration is done |
| D6 | Wires | Advanced uses PG wires; basic uses IE |
| D7 | Name clashes | PG overrides on advanced; on basic PG does not exist, so nothing clashes |
| D8 | CC&A Electric Motor and Alternator | Dropped in advanced |
| D9 | Equipment vs IE drill, buzzsaw, powerpack | Integrated / merged in advanced |
| D10 | CRT, displays, Plotter, Punch Cards vs Control Engineering | Integrated / merged in advanced |
| D11 | Decor (Copper Plating, string lights, ceiling tiles) | Last priority; advanced adds only non-overlapping pieces |
| D12 | Ponder scenes | Phase 2 |
| D13 | `create:cardboard` | Use a cardboard tag; the pack has cardboard (see H6: no such tag exists yet) |
| D14 | Client mirror | Not decided: measure first. Identify the performance cost of the client mirror against the gameplay cost of server-only values (task PWG-27/28, see H5) |

### 5.2 Effects of these decisions on the plan

- **Section 3.2 route B stands.** Order of work becomes: (1) full port in phases, PG running alone in a test instance;
  (2) basic/advanced switch; (3) integration. Steps 5 to 20 of section 4 move to the integration stage and apply to
  advanced mode only; step 6 (item removals) and step 7 (tags) become mode-dependent.
- **D6/D7 change step 6.** Nothing of PG's is removed in basic; in advanced the IE and CE wires are replaced by PG's wire
  items (not registered as PG wire types, so the `wire_types` registry stays upstream-shaped).
- **D4 needs new code:** an `overheated_component` block and block entity that copies the original block's state and NBT,
  renders the original texture with a crack overlay (the vanilla destroy-stage textures work), is inspectable (WTHIT, goggles),
  cannot be placed from an item, and has a drop chance (a new config key). Every `ThermalBehaviour` overheat path
  (`overheatCallback`, wires, `explosiveDeconstruction`) routes to it. Wires keep burning as wires (open question H4).
- **Advanced = everything on PG energy** is the biggest scope in the plan (H1).

### 5.3 Open decisions and holes found

| # | Hole | Why it matters | Suggested answer |
|---|---|---|---|
| H1 | **What "all power sources and consumers work on PG energy" means.** IE, CC&A, PneumaticCraft, AE2 and others use FE through Forge capabilities. Options: (a) replace each mod's FE storage with a PG voltage node (code in every mod, mixins), (b) bridge: every FE machine gets an implicit Device Connector, every FE generator an implicit Inverter, so they see grid voltage through a capability adapter, (c) block-level swap (replace the machines with PG ones) | This is the largest single piece of work; (a) is invasive and fragile, (c) loses the mods' content | (b), as a capability-adapter layer in Creative Common, with a per-mod allowlist. Needs its own design pass and a decision on what a wire-less FE machine does (H2) |
| H2 | **Wireless and non-wire FE.** Adjacent-block FE transfer, cables from other mods (AE2 energy, Mekanism-style) and IE wire networks are not wires in PG | In advanced the IE wire network must go (D6), but IE machines' FE input is capability-based | Decide per source: IE connectors disabled in advanced, machine inputs become PG terminals |
| H3 | **Save compatibility between modes.** Switching basic to advanced (or back) in an existing world: IE wires and connectors, CC&A motors (D8) and every converted machine | World damage or item loss | Treat the switch as a world-creation choice, not a live toggle; or ship a one-way conversion |
| H4 | **Wires and overheating.** D4 covers components; wires burn and disappear today | Consistent rule: do burnt wires become something inspectable? | Wires snap and drop nothing, or leave a "burnt wire" entity; owner call |
| H5 | **Server-only gameplay cost (D14).** PG ships no server-only mode; the client mirror is built into gauges, light and wire glow | The decision needs a measured comparison: solve cost per client per tick vs how stale 100-tick state sync feels | Add a config to send server values only and test PWG-27/28; no decision until measured |
| H6 | **No cardboard tag in the pack.** Cardboard exists only as Dustrial Decor items (`folded_cardboard`, `cardboard_scrap`, smooth cardboard); no `forge:cardboard` tag appears in the dumps | The 4 PG recipes need a tag | Create `forge:cardboard` in `global.tags` (Dustrial Decor folded and smooth cardboard); the four recipes use it |
| H7 | **Dependence on the pack's Create build.** Create 0.5.1.i is a custom build ("i"); fan, arm and value-box mixins must be re-checked against its patches | Mixins may fail at load | PWG-20 and a first-boot mixin check (section 7) |
| H8 | **Multiplayer and tick budget.** A big grid is one solve on the server tick; PG has `multiTicks` and splitting options off by default | A factory pack may build large grids | Test PWG-27; decide default for `splittingTransmissionLines` (experimental) |
| H9 | **Chunk loading.** A grid that spans unloaded chunks: PG behaviour is not specified (PWG-26) | Create Power Loader is installed | Read upstream chunk handling when porting; test |
| H10 | **Balance of advanced mode.** IE's tiers (LV/MV/HV in FE per tick) vs PG voltages; recipes that gate progression (IE steam and diesel, AE2 power) | Progression could break or become trivial | A balance pass after integration, with an explicit target |
| H11 | **Basic mode's own changes.** "Future changes to the same electricity core within current basic context" | The pack's electricity today is scattered (IE, CC&A removals, CE coils); no single owner doc | Name a home for the basic electricity writeup before the first advanced-only change lands |
| H12 | **Licence upkeep.** Apache-2.0 requires NOTICE, a marked-changes statement and the licence text in the fork; assets (textures, sounds, ponder) come under the same licence per the repo | Must be recorded before publishing | Add in step 0 (already B1) and a licence audit row like Common's |
| H13 | **Port maintenance.** Upstream is at 0.6.x with large changes between versions; the fork diverges immediately (Create 0.5 vs 6) | Later PG features need hand-porting | Treat the port as a snapshot of 0.6.2; list upstream changes to follow in the plan |
| H14 | **Overheated Component interaction.** Repair path? Does JEI/ponder or the explosion toggle exist? Can a fan cool one back? | Players need to know whether it is permanent | Default: permanent, drops the component only on a chance (as decided); a recipe to repair is not planned |
| H15 | **Dedicated-client sides.** `mod_sides` for PG: blocks, entities and the sync layer exist on both sides; client-only config for the circuit editor | Row needed | Add row `both` at step 21 |

### 5.4 Owner answers to the holes (2026-11-12)

| # | Answer | Consequence |
|---|---|---|
| H1 | A **PG-FE converter at the edge of every FE device**; WTHIT and GUI values are rewritten to show PG values (V, A, W), so the player never sees FE | The adapter layer of H1 is option (b), now fixed. FE stays an internal detail of the converter. Display work: WTHIT plugin, JEI/GUI energy bars and tooltips of every FE mod (a per-mod list is part of the advanced doc) |
| H2 | If PG connectors prove insufficient, merge IE's models and functionality into PG connectors and wires | Keep as a fallback; first test whether PG connectors cover IE machines' terminals (advanced doc step A5) |
| H3 | World-creation choice: advanced changes apply only when loading or starting an advanced world. Worlds get an **upgrade / downgrade option that destructively removes inconsistencies** | Needs a per-world mode flag (saved data), a converter that deletes or replaces unusable blocks and items, and a confirmation screen; the world is backed up first (open point N3) |
| H4 | Burnt wires disappear | The wire is removed, no drop, no entity |
| H5 | Scratch tests must be defined for server and client performance measuring | Rows SQ-PWG-01 to -08 and CQ-PWG-01 to -04 added to the queue (held until a port exists) |
| H6 | Add the cardboard item to a tag; recipes use the tag, not the item | Create `forge:cardboard` through `global.tags` with the Dustrial Decor cardboard (which item exactly: open point N4); the four PG recipes use the tag |
| H7 | Needs investigation; PG could be mapped to IE tiers | Done in [energy_advanced.md](energy_advanced.md) section 3; headline: PG power is about 100 times IE's per tier at upstream settings. Owner 2026-11-13: use an FE-per-watt rate of 0.1 for the mapping for now; LV/MV/HV tiers are mapped to behaviour observed in a bare PG port, not fixed in advance |
| H11 | Create `energy_basic.md` and `energy_advanced.md` | Both written 2026-11-12: [energy_basic.md](energy_basic.md), [energy_advanced.md](energy_advanced.md) |

### 5.5 Open points after these answers

| # | Point | Suggested answer |
|---|---|---|
| N1 | **(answered: correct)** **Mode is saved per world, but the config switch is global.** Which wins on an existing world? | Per-world flag wins; the global config only sets the default for new worlds |
| N2 | **(answered: correct)** **Dedicated servers:** the mode comes from the server world; a client joining must run the matching jar content | Client checks the mode on join and refuses a mismatch with a clear message |
| N3 | **(answered: correct)** **Backup before upgrade or downgrade**: a destructive conversion needs a restore path | Copy the world folder to `saves/<name>_backup_<date>` before converting; no conversion without it |
| N4 | **(answered: Dustrial Decor)** Cardboard source for `forge:cardboard` | Dustrial Decor `folded_cardboard` goes in the tag; add `smooth_cardboard` or scrap only if a recipe needs them |
| N5 | **(answered: correct, derive the thermal rating from rated power)** **"Overheated" applies to devices; what about the converter at the edge of an FE device?** An overheat should depend on the device's own thermal values, but FE devices have none | Give converters a thermal rating derived from the machine's rated power (generated table), or exempt them; owner call |
| N6 | **FE rate unit check:** upstream's `forgeEnergyPerWatt` of 10 reads as FE per tick per watt in the code (inverter: `ceil(10 * power)`). Verify against a running build before tuning (energy_advanced.md 3) | PWG-18 measures it |
| N7 | **(answered)** Internal FE buffers are fine: the converter wraps the FE capability and the display shows the equivalent PG value. The only exclusions are machines with **no FE capability at all** (their own energy system, such as AE2's AE units or PneumaticCraft air pressure) | List them per mod when the integration starts |
| N8 | **(answered: dump the list)** Planned basic-mode changes | List removed from energy_basic.md; basic changes are added there only when one is decided |

## 6. File map (planned)

| File | Role |
|---|---|
| `development/backportedPowerGrid/` | the fork (mod id `powergrid`), Apache-2.0 upstream files with NOTICE, our changes GPLv3 |
| [plan_power_grid.md](plan_power_grid.md) | execution plan per port phase, P0 to P11 (written 2026-11-13) |
| `kubejs/server_scripts/` Engineering tree, `power_grid.js` | recipe swaps and removals through `global.NCH` |
| Creative Common `_lib/item_dict.js`, `removed_items.js` | PG ids, removed PG duplicates |
| `configs_edited/server_config/powergrid-server.toml` | balanced FE rates, thermal options |
| `mod_sides.md` | new row |

## 7. Offline checks (once built)

- javac harness on the solver (no Minecraft): voltage divider (10 V, 1 k / 1 k: 5 V), RC charge to 63 % at one time
  constant within 2 %, ideal transformer ratio 1:10 under load, diode forward drop near 0.7 V, floating grid stays
  finite.
- `./gradlew :backportedPowerGrid:build --offline` clean; mixin refmap applies on a scratch server with no `Mixin apply failed`.
- P0 skeleton on a scratch server: SQ-PWG-09 (loads, nothing registered, no mixin or tag errors), SQ-PWG-10 (port configs).
- Data load: no `Failed to parse` for `powergrid` recipes, tags or loot in the scratch server log.
- `python3 tools/changelog_lint.py` after bullets.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| PWG-01 | Creative voltage source 12 V, resistor, current gauge in a loop | gauge reads V / R |
| PWG-02 | Open the loop (cut a wire) | current 0, no errors in log |
| PWG-03 | 24-block copper wire run under load | voltage drop matches 0.018 ohm |
| PWG-04 | Overload a copper wire past 80 A | wire burns, network splits cleanly |
| PWG-05 | Touch a bare live wire; touch an insulated one | damage only from bare |
| PWG-06 | Electric Motor at 120 V, 182 V and 258 V | about 55, 127 and 255 RPM (speed = 0.003835 x V^2, capped at 256; corrected 2026-11-13: 120 V is not half speed) |
| PWG-07 | Motor under heavy vs light stress | current drops at light load |
| PWG-08 | Generator: clutch, rotors, commutator, windings, separate excitation | voltage rises with RPM |
| PWG-09 | Self-excited generator | builds voltage without an outside source |
| PWG-10 | Generator load slows the rotor and loads the clutch | rotor RPM falls under the clutch speed, clutch load and comparator output rise; Create stress stays the assembly impact x coupling (corrected 2026-11-13: the code never raises it with load) |
| PWG-11 | Carbon pile regulator holds generator voltage under load change | within a few volts |
| PWG-12 | Small transformer 1:10 | secondary 10x, current 1/10 |
| PWG-13 | Device over its rating, no cooling | smokes, then breaks (per D4) |
| PWG-14 | Same device with an encased fan blowing on it | stays below limit |
| PWG-15 | Fuse at 5 A on a 10 A load | fuse burns, load stops |
| PWG-16 | Battery charge and discharge | voltage drifts with charge; energy meter counts Wh |
| PWG-17 | Grounding rod with poor and good ground | resistance falls with conductive blocks |
| PWG-18 | FE Inverter fed by an IE generator drives a motor | rate matches D3 |
| PWG-19 | Device Connector on an IE machine | machine runs on grid power |
| PWG-20 | Heating Coil in a fan stream | bulk blasting at the configured speed; Dragon Breathing fans still work |
| PWG-21 | Basin Heater at high power | superheated basin recipe runs |
| PWG-22 | Growth Lamp over an out-of-season crop | crop does not grow out of season (integration target; the port keeps upstream: the lamp has no gate, but its ticks fire `CropGrowEvent.Pre`, so the pack's off-season refusal applies to them; SQ-PWG-53 records the rate) |
| PWG-23 | Solar panel multiblock, plains vs desert | desert lower |
| PWG-24 | Nether transformer across a portal | linked pair carries power |
| PWG-25 | Save and reload a running grid | voltages restore, no wire loss |
| PWG-26 | Chunk unload of half a grid | no crash, the rest keeps running or pauses cleanly |
| PWG-27 | Large grid (200+ nodes) on a dedicated server | TPS and client FPS within budget; `/powergrid` perf |
| PWG-28 | Dedicated server and client: gauges move smoothly | client mirror in sync |
| PWG-29 | Wire placement with IE copper wire, CE iron and gold wire (D6) | each places with its wire type |
| PWG-30 | JEI shows magnetization; unported items hidden | as listed |
| PWG-31 | Magnet: lightning on dropped andesite alloy (corrected 2026-11-13, B7: not iron ingots); electromagnet on a grid over an alloy | some alloy magnetizes (about 5 % per strike); electromagnet magnetizes only when powered |
| PWG-32 | Open every ported ponder scene | each plays to the end; no missing blocks (P5: 36 scenes; CQ-PWG-14, structures SQ-PWG-45; P6: 58 scenes, CQ-PWG-15, structures SQ-PWG-47) |
| PWG-33 | Light bulb, LV bulb, Factory Light, Light Fixture on a grid; Electric Pump and Electric Fan run on grid power | light level follows voltage; Factory Light lights 16 blocks down; pump and fan speed follow power |
| PWG-34 | Portable Battery in the chest slot, charge and drain; Capacity enchantment | 20 000 base, +20 000 per level |
| PWG-35 | Portable Drill and Portable Saw on a charged battery | mine diamond-level blocks; drill speeds up with use; stop when the battery is empty |
| PWG-36 | Electro-Baton and Electro-Zapper on mobs | knockback and stun; zap chains between mobs |
| PWG-37 | Wire Cutters on a sheep (corrected 2026-11-13: not the saw, which does not shear upstream either) | the sheep is sheared for string and hurt by 1, as upstream; no advancement error in the log |
| PWG-38 | CRT and Modular Display driven by the grid | dot and flip modules follow the inputs |
| PWG-39 | Plotter and Punch Card Reader | plotter draws voltage over time; reader sequences kinetic output from a card |
| PWG-40 | CC: Tweaked computer reads a PG gauge (or meter, battery, clutch) as a peripheral and sets a redstone converter (corrected 2026-11-13: upstream has no display peripheral) | values match the gauge |
| PWG-41 | Circuit Design Table: draw a schematic, save it | schematic item holds the design; editor works at GUI scales 2 and 4 |
| PWG-42 | Etch and fill a board (Blazing Acid, sequenced assembly, Mechanical Arm) | the board's circuit behaves as designed in the solver |
| PWG-43 | Boost a Portable Drill with an Integrated Circuit | faster, double power draw, uses count down |
| PWG-44 | Ceiling tiles (lamp, solar, wire, junction), string lights with dye pattern, Copper Plating | place, connect and light as upstream |

## 9. History

- 2026-11-13: port phase P10 (decor) done; **the port is complete** (plan_power_grid.md P10, PORT_NOTES.md 24): copper plating with its slab and stairs (connected textures; no oxidation upstream), the ceiling tile and its four attachments (lamp, connector, junction, solar; placement helper, wrench removal), the string light cord (item, entity, light blocks, the dye pattern recipe `crafting_special_string_light_cord`), the ponder scenes `ceiling_tile` and `wire/string_lights` and the Ceiling Tile Attachments tag (81 scenes, 5 tags). Every upstream id and recipe is in (78 blocks, 119 items, 116 recipes); all 32 recipe overrides are met (the circuit design table and solar panel bearing are craftable); `upstream/java` keeps only code that never ports (datagen, Flywheel 1.0 visuals, REI, TFMG, the 1.20 tab generator). Upstream bugs B30 to B34 fixed. Jar 0.6.2-ce-19.2-17 (`port_phase=10`), gate copy `jar_archive/pwg_gate/` (sha1 cbc2ddee), not in `mods/`; gate rows SQ-PWG-90 to -96, CQ-PWG-28, -29 (PWG-44; probe `tools/scratch_query/pwg_p10_probe.js`). SQ-PWG-67 answered: the circuit component tag loads 27 of 28 in the pack because the pack removes `create:copper_nugget` (fold-in point, section 4 item 8). Next: P11 (evaluation).
- 2026-11-13: port phase P9d (Integrated Circuit and boost) done; **P9 complete** (plan_power_grid.md P9): the Integrated Circuit (boosting chip, custom renderer, hand use with the eating animation), `incomplete_electrical_gizmo`, the `powergrid:boost_recipe` serializer (type `create:deploying`, so Create's JEI Deploying page lists the boosts), the `BoostRecipe` branch of `RecipeApplierMixin` (the boosted tool keeps its tag), the Electro-Zapper's chip check; 6 recipes (106 in all; the electrical gizmo assembly unlocks the 9 recipes that use the gizmo). A boost lasts 30 % of the tool's durability in uses: drill and saw twice as fast for twice the energy, baton stun from (60 - max health) for 120 ticks, zapper twice the power. B28 (chip boosted the wrong item) and B29 (Boosted tooltip missing on baton and zapper) fixed. Jar 0.6.2-ce-19.2-16 (sha1 618ed801), rows SQ-PWG-84 to -89, CQ-PWG-27 (PWG-43). Overrides left: the two `copper_plating` ones (P10). Sections 3.6 to 9 of this file were restored the same day: a P9a edit had cut everything after the 3.5 table (rebuilt from the 00:31 snapshot plus every later edit, replayed).
- 2026-11-13: port phase P9c (etching, acid, assembly) done (plan_power_grid.md P9c): Blazing Acid fluid, bucket and damage, `empty_circuit`, `unetched_circuit`, `incomplete_unetched_circuit`, `incomplete_circuit`, the arm mixins, `BeltDeployerMixin` and `RecipeApplierMixin` (schematic branches), JEI Circuit Designing and Circuit Assembly; survival boards end to end. B25, B26 fixed, B27 recorded. Jar 0.6.2-ce-19.2-15 (sha1 f95311cc), rows SQ-PWG-76 to -83, CQ-PWG-26 (PWG-42 survival part). Line added in P9d (the section was missing then).
- 2026-11-13: port phase P9b (circuit design table and editor) done (plan_power_grid.md P9b): design table, schematic item, editor, the four menus, packets, creative board edit menu, JEI ghost ingredients. B21 to B24 fixed, B20 (P7 portable battery) fixed in the same jar. Jar 0.6.2-ce-19.2-14 (sha1 d80a8bab), rows SQ-PWG-72 to -75, CQ-PWG-24, -25 (PWG-41). Line added in P9d (the section was missing then).
- 2026-11-13: port phase P9a (components on boards, the solver's non-linear models) done (plan_power_grid.md P9; P9 runs as sub-steps P9a to P9d, phases 9.1 to 9.4): circuit board block, block entity and baked model, the 28 board components (forge registry `powergrid:components`), the schematic data model, `component_items` as a reload listener with client sync, 17 component items and pins (the 11 recipes that waited for pins ship), the 15 circuit ponder scenes and `encased_fan`; diode, BJT and tube circuits added to the solver harness (84 passed). B18 recorded, B19 (winding owner recursion, server crash on `-10`) fixed. Jar 0.6.2-ce-19.2-12 (sha1 75aa8fc5), rows SQ-PWG-67 to -71, SQ-PWG-06, CQ-PWG-22, -23. PWG-41 is P9b's gate, PWG-43 P9d's.
- 2026-11-13: port phase P8 (display and control) done (plan_power_grid.md P8): Cathode Ray Tube with andesite and brass encased variants (electron gun model on the grid, client trace), Modular Display with display modules (flip coils per slot, module types 0-9, 9-0, 1-0, hex, symbols, alphabet, blanking page, dye, slot burnout), Plotter (voltage over time on scrolling paper), Punch Card Reader and punch cards (16 x 8 hole editor screen, sign and lock, playback through 8 switched outputs and a comparator signal, kinetic read speed, top and side automation), Alarm Bell; CC: Tweaked peripherals (gauges, energy meter, battery, clutch, redstone converter) on the pack's CC: Tweaked 1.101.4; no new display source (all four were in P2 to P4). Ponder: plotter, modular_display, bell, crt, punch_cards. Jar 0.6.2-ce-19.2-10 (`port_phase=8`: 66 blocks, 85 items, 54 block entities, 3 menus, 66 recipes, 25 mixins). Port changes for Create 0.5.1 on the modular display's value box (dye and wrench reach the block: Create 0.5.1 has no `bypassesInput`) and module type board (Create's option formatter prefixes `create.`). Fixed B15 (card lost when a reader breaks), B16 (burnout packet off the client thread), B17 (CC redstone converter writes from the computer thread). PWG-40 wording corrected. Solver harness now also runs upstream's `EquationParserTests` (78 passed). Gate rows SQ-PWG-62 to -66, CQ-PWG-19 to -21; probe `tools/scratch_query/pwg_p8_probe.js`, CC program `tools/scratch_query/pwg_p8_cc_startup.lua`. 31 recipe overrides unchanged.
- 2026-11-13: port phase P7 (equipment) done (plan_power_grid.md P7): portable battery (worn in the vanilla chest slot, no Curios; placeable block charged from the grid at 2 FE per volt; Create's Capacity enchantment, +20 000 FE per level; worn tick charges a held Forge Energy item), portable drill (speeds up with continuous use, capped by battery output), portable saw (fells a tree when sneaking), Electro-Baton (stun by slowness), Electro-Zapper and its zap projectile (damage shared with up to 2 nearby mobs; damage source `zap`), lightning rod movement behaviour (lightning attractor, its P5 ponder scene now live). Mixins `SheepMixin` (advancement call removed), `PlayerDrillMixin` (boost branch inert until P9) and the new `DrillBreakActionMixin` (Forge 1.19.2's left-click event has no break action). No P7 ponder scene upstream. Jar 0.6.2-ce-19.2-09 (`port_phase=7`: 59 blocks, 77 items, 4 entity types, 49 block entities, 61 recipes, 25 mixins). Fixed B11 (P6 finding, comment), B13 (zap chain sparks), B14 (baton never wore for players). PWG-37 wording corrected (the saw does not shear). Gate rows SQ-PWG-55 to -61, CQ-PWG-17, -18; probe `tools/scratch_query/pwg_p7_probe.js`. 31 recipe overrides unchanged.
- 2026-11-13: port phase P6 (heat, light, fluid, solar) done (plan_power_grid.md P6): heating coil (fan processing types `powergrid:heater_blasting` / `heater_smoking` on Create 0.5.1's registry, next to the pack's Dragon Breathing type), basin heater (basin, boiler and Farmer's Delight pot and skillet heat), electric fan (chute push / pull), electric pump, light fixture, factory light, light bulbs, growth lamp (upstream rate, no season gate of its own), solar panel and bearing (contraption type, `solar_biome_override` as a reload listener, B10 fixed), 6 mixins with `defaultRequire 1`, 22 ponder scenes (58 in all; encased fan waits for P9). H5: lights follow the server's block state in both modes; with the mirror off the glow syncs at each power level change. Jar 0.6.2-ce-19.2-08 (`port_phase=6`: 58 blocks, 71 items, 48 block entities, 56 recipes, 22 mixins); `-08` also fixes B12 (missing `zap.json` crashed every client since P3; new client-asset check). B11 found. Gate rows SQ-PWG-46 to -54, CQ-PWG-15, -16. r1113 P3 / P4 findings: SQ-PWG-32 (connector on an IE capacitor's non-input side) and SQ-PWG-37 (AC reads aliased with the period) are row faults, rows fixed with -28, -30, -31, -36, -43, -45.
- 2026-11-13: port phase P5 (ponder) done (plan_power_grid.md P5): Power Grid's ponder base on Create 0.5.1 `foundation.ponder` (scene builder wrapper with the Create 6 accessors, wire elements, voltage sources, network ticking, the level-restore hook as each scene's first instruction), 36 scenes for P2 to P4 content and the tags Generator Parts, Electricity Relays, Electric Devices (plus Create's kinetic and display source tags). Scenes that place P6+ blocks wait for their phase (current and power gauge, switch, HV breaker, simple wire, wire loop, cord, generator 3, string lights). The 36 shipped structures (1.20.1 saves) checked offline. Jar 0.6.2-ce-19.2-06 (`port_phase=5`), same registry content as P4; gate rows SQ-PWG-45, CQ-PWG-14
- 2026-11-13: port phase P4 (kinetics) done (plan_power_grid.md P4): Electric Motor, Constant Speed Motor, servo, rheostat, variac, conductive casing, generator clutch, induction rotor and large induction rotor, horizontal and vertical commutator, horizontal and vertical generator housing. Jar 0.6.2-ce-19.2-05 (`port_phase=4`) registers 49 blocks, 58 items, 40 block entities, 47 recipes, 16 mixins (`KineticBlockEntityAccessor` added). Generator coupling checked against Create 0.5.1 speed propagation: no change needed (stress-only coupling, own PD loop; PORT_NOTES.md 14). Generator speeds served by the Create 0.5.1 stress provider (`getGeneratedRPM`); damage source `spinning_rotor` from the P2 enum. Flywheel 1.0 visuals not ported; every P4 renderer draws its own shaft and rotor. Upstream bug **B9** (servo) fixed. P2 to P4 complete D1's "phase 1": the owner can play the full voltage system with motors and generators on a scratch server. Commutator still uncraftable until P9 (recipe override, owner decision pending on the 31 overrides). Gate rows SQ-PWG-35 to -43, SQ-PWG-07 (now runnable), CQ-PWG-13; probe `tools/scratch_query/pwg_p4_probe.js`. PWG-06 and PWG-10 wording corrected (3.5 note).
- 2026-11-13: port phase P3 (passive and switching devices, transformers, storage, FE bridge, magnet) done (plan_power_grid.md P3): power resistor, fuse holder, LV / MV / HV switches, HV breaker, LV button, contactor, redstone converter, transformer core with small / medium / nether transformers, winding, battery and potato battery, grounding rod, spark gap, carbon pile and coil, electromagnet, FE inverter, device connector; coils, magnet, zinc sheet, incomplete battery and transformer core; recipe type `powergrid:magnetization` with its JEI category; particles. Jar 0.6.2-ce-19.2-04 (`port_phase=3`) registers 36 blocks, 45 items, 31 block entities, 22 recipes, 15 mixins. FE bridge at upstream rates (10 FE per W, 2 FE per V; code reads as FE per tick per watt, N6 measured by SQ-PWG-27). D4: overheated medium transformers and windings turn into Overheated Components. Moved forward: `ElectricKineticBlockEntity` (P4) and the Create 0.5.1 stress provider (HV switch and breaker are kinetic), `AirCurrentMixin` (P6, fan cooling for PWG-14). Upstream bugs B6 (grounding rod damage grew with distance) and B7 (magnetization comment) fixed, plus B8 from the P2 scratch run r1113, whose probe and row problems were fixed in the same jar (rerun SQ-PWG-13, -15, -16, -17, -21). Gate rows SQ-PWG-24 to -34, CQ-PWG-10 to -12 queued (probe `tools/scratch_query/pwg_p3_probe.js`).
- 2026-11-13: port phase P2 (grid base) done (plan_power_grid.md): wires, connectors, cord, socket, creative sources and resistor, gauges, energy meter, thermometer, multimeter, wire cutters, debug item, `/powergrid`, world networks and client sync ported; jar 0.6.2-ce-19.2-03 (`port_phase=2`) registers 13 blocks, 21 items, 3 entities, 11 block entities, 2 menus, 11 recipes, 5 wire types, 11 mixins. D4: new **Overheated Component** block (every overheat turns the device into it: original state and data kept, crack overlay, goggles, drop chance `overheatedDropChance` 0.25, no item); H4: burnt wires vanish with no drop (upstream already did); H5: `clientMirror` off stops the per-tick state stream so clients see only the 100-tick full sync; `wire_types` is a reload listener plus a sync packet; damage sources are 1.19.2 `DamageSource`s with upstream message ids; H9 chunk handling recorded (PORT_NOTES.md section 12). Manifest: `custom_display` menu moved to P2 (gauges use it). No new upstream bug. Build clean, solver harness still passes. Scratch rows SQ-PWG-12 to -23, CQ-PWG-05 to -09 queued; perf rows SQ-PWG-01 to -05, -08, CQ-PWG-01 to -04 refreshed (probe `tools/scratch_query/pwg_grid_probe.js`).
- 2026-11-13: port phase P1 (solver core) done (plan_power_grid.md): `electricity/sim` ported (63 files; Catnip `Pair` to Create's; `TransmissionLine` behind a new `ITransmissionLine` interface and the debug command behind a hook, so the core has no game types; `CSolver.SolverBackend.create` back); `circuits/thermal` and six game-side `sim` classes moved to P2. Upstream bug **B5** found and fixed (wires to ground ignored after the first solve). Offline harness `tools/test_power_grid_solver.sh`: plan's five circuits pass on the plain and the game network type, upstream's solver tests 75 pass, 6 known test-side failures listed; 1000-node ladder 0.1 ms per changed solve, 9 ms first solve; 32 x 32 mesh 3 ms (PORT_NOTES.md section 10). Jar 0.6.2-ce-19.2-02 (`port_phase=1`), still registers nothing; scratch row SQ-PWG-11 (solver inside the game class loader) queued.
- 2026-11-13: port phase P0 done (plan_power_grid.md): fork at tag v0.6.2 (commit d681ac98, resources identical to the decompile) in `development/backportedPowerGrid`, common and forge trees merged with `@ExpectPlatform` inlined (D2), LICENSE / LICENSE.txt / NOTICE (B1, H12), configs ported (B3 commented, B4 comments added, new `clientMirror` switch for H5), phase manifest `phases.json` with the `PortPhase` constant and `tools/pg_phase_check.py` resource filter and recipe gate, native solver / REI / Cold Sweat / TFMG / advancements / damage-type data deleted. Build clean; the jar registers nothing. Scratch row SQ-PWG-09. Notes: `development/backportedPowerGrid/PORT_NOTES.md`.
- 2026-11-13: execution plan written: [plan_power_grid.md](plan_power_grid.md), phases P0 to P11 covering the whole mod for evaluation (only the native solver, advancements, REI / Cold Sweat / TFMG compat, Flywheel 1.0 visuals, damage-type data and explosions left out); tests PWG-31 to -44 drafted for the content that had none.
- 2026-11-13: owner answers to N1 to N8 recorded (5.5): FE-per-watt 0.1 for now, LV/MV/HV mapped from a bare PG port, internal FE buffers allowed through the converter, Dustrial Decor cardboard, basic-mode list dumped.
- 2026-11-12: owner answers to H1 to H7 and H11 recorded (5.4), open points N1 to N8 (5.5); [energy_basic.md](energy_basic.md) and [energy_advanced.md](energy_advanced.md) created; scratch rows SQ-PWG / CQ-PWG added (held).
- 2026-11-12: owner decisions D1 to D13 recorded (section 5.1), D14 deferred to measurement, 15 holes listed (5.3).
- 2026-11-12: investigation written (jar 0.6.2 for 1.20.1, decompiled to the session scratchpad; source repo checked:
  no 1.19.2 branch). Nothing built.
