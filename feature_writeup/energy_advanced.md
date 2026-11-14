# Energy (advanced mode): everything on Power Grid energy

Written 2026-11-12. **Speculative; nothing built.** Reference for the **advanced** electricity mode, used only if Create:
Power Grid ([power_grid.md](power_grid.md)) is ported and integrated. Basic mode is [energy_basic.md](energy_basic.md).
Mode is chosen **per world**: advanced changes apply only when an advanced world is created or loaded; worlds can be
upgraded or downgraded with a destructive clean-up (section 6).

## 1. Principle (owner decisions, 2026-11-12)

- **Order:** port Power Grid fully, in phases, **before** any integration with the pack or other mods starts.
- **Everything works on PG energy.** At the edge of every FE device sits a **PG-FE converter**; the device keeps using FE
  internally. WTHIT and GUI values are rewritten to show PG values (V, A, W), so **the player never sees FE**.
- Advanced uses **PG wires** (basic uses IE's). PG overrides name clashes. CC&A Electric Motor and Alternator are dropped;
  equipment, display and decor are merged with their PG equivalents, decor last and only non-overlapping pieces.
- If PG connectors cannot serve some IE machines, merge IE's models and functionality into PG connectors and wires.
- Overheating is on, with **no explosions and no block break**: an overheated component stops working and becomes an
  **Overheated Component** block (cracked overlay on the original texture, keeps all block data, drops the component only
  on a chance). **Burnt wires disappear.**

## 2. Phases

| Stage | Content | Notes |
|---|---|---|
| 1 | Phase 1 port: solver, wires, connectors, motors, generators, transformers, switches, gauges, FE Inverter, Device Connector, creative sources | [plan_power_grid.md](plan_power_grid.md) P0 to P4; no ponder |
| 2 | Ponder scenes | owner D12; plan P5, later phases bring their own scenes |
| 3 | Lights, heaters, fan, pump, solar, then equipment and display/control | plan P6 to P8 |
| 4 | Components and circuits (editor, boards), then decor | plan P9, P10; integration with other mods' circuits postponed until after basic integration |
| 4b | Evaluation of the bare port, keep/drop/merge table | plan P11 |
| 5 | Mode switch, per-world flag, converter layer, display rewrites | section 4 and 5 |
| 6 | Integration and balance | sections 3 and 7 |

## 3. Tier mapping: PG to IE (investigation, H7)

Numbers: IE from the server config (energy_basic.md 2); PG from the decompile (`WireItem` properties, `CElectricity`).
PG's FE inverter computes `fe = ceil(forgeEnergyPerWatt * power)`, read as FE per tick (not yet verified in game).

| PG fact | Value |
|---|---|
| Copper wire | 0.0015 ohm per item, max 80 A, 24 blocks |
| Iron wire | 0.005 ohm per item, max 160 A, 64 blocks |
| Golden wire | 0.003 ohm per item, max 160 A, 12 blocks |
| Electric Motor at 256 RPM | about 258 V, 10 A, 2.6 kW |
| Upstream `forgeEnergyPerWatt` | 10, so 2.6 kW is about 26 000 FE/t |
| IE LV connector | 256 FE/t; LV wire 2048 FE/t |

**Finding:** at the upstream rate, one PG motor draws about 100 times the energy of an IE LV connector and 13 times an IE LV
wire; one PG copper wire at 80 A and 258 V carries about 20 kW (about 200 000 FE/t). IE's tiers are two orders of magnitude
smaller. Mapping by power would make PG machines absurdly expensive to feed from IE sources.

**Decided (owner 2026-11-13):**
1. **`forgeEnergyPerWatt` = 0.1 for the mapping for now**, so a motor at rated speed costs about 260 FE/t, the same as one IE LV
   connector. Check against IE generator outputs (Dynamo, diesel, thermoelectric) and CC&A rates after the first
   running build.
2. **LV / MV / HV bands are not fixed yet:** they are mapped to behaviour observed in a **bare PG port** (PWG tests with no
   integration). The bands below are a starting guess only: LV about 12 to 48 V, MV about 120 to 260 V, HV above 500 V.
   PG has no tiers itself; wire current limits and device ratings are the real limits.
3. Machine sinks translate rated FE per tick into rated watts: `W = FE per tick / forgeEnergyPerWatt`. The converter
   gives the machine voltage in its band and draws the current that delivers that power.
4. Check overheat: a converter on an IE machine gets a thermal rating from the machine's rated power (power_grid.md N5).

## 4. The converter layer (H1)

- One converter per FE device: a capability adapter on the device's block entity (Creative Common), with terminals on the
  device's PG-visible face, using the allowlist of mods whose machines are converted.
- The converter owns the thermal model for the device (overheat rule of section 1) unless the device is exempt (N5).
- **Display:** WTHIT plugin, device GUIs and JEI energy bars and tooltips of every converted mod show V / A / W, never FE.
  This needs a per-mod list of FE displays; mods whose GUIs draw their own FE bar (IE, CC&A, PneumaticCraft) need a mixin
  or a resource override for the text.
- Internal FE buffers are fine: the converter wraps the FE capability and the display shows the equivalent PG value (owner 2026-11-13). Only machines with no FE capability at all (AE2's AE units, PneumaticCraft air pressure) are outside the converter layer (power_grid.md N7).

## 5. Mode and world handling

- A per-world flag selects the mode (power_grid.md N1); the global config only sets the default for new worlds.
- A client joining a dedicated server checks the mode against the server (N2).
- **Upgrade** (basic to advanced) and **downgrade** (advanced to basic) are destructive clean-ups that remove inconsistencies:
  IE wires and connectors, CC&A Electric Motors and Alternators, PG blocks and items, converters, overheated components.
  A backup copy of the world is made first (N3). Item stacks and block entities that no longer exist are deleted, not
  converted; the exact removal list is built from the registry diff when the mode set is final.

## 6. Steps for the integration stage (advanced only)

| # | Step |
|---|---|
| A1 | Converter layer and its allowlist (section 4) |
| A2 | Display rewrites for every converted mod |
| A3 | Item merges and removals: PG overrides name clashes; IE copper wire, CE iron and gold wire, CC&A motor and alternator, PG zinc sheet and wires as chosen (power_grid.md D6 to D8) |
| A4 | Tags: `forge:cardboard` (Dustrial Decor `folded_cardboard`, N4 answered), trims of `forge:wires` and the coil tags |
| A5 | Test PG connectors against IE machine terminals; if they fall short, merge IE models and functionality into PG connectors (H2) |
| A6 | Equipment, display and control merges (D9, D10); decor last, non-overlapping only (D11) |
| A7 | Circuit integration with PneumaticCraft, IE, ProjectRed and Control Engineering (after basic integration; D5) |
| A8 | Balance pass (power_grid.md H10) with the tier mapping of section 3 |
| A9 | World upgrade / downgrade tool and mode flag (section 5) |

## 7. Tests

Performance measuring for server and client is defined in the scratch queue: `SQ-PWG-01` to `SQ-PWG-08`, `CQ-PWG-01` to `CQ-PWG-04`
([scratch_queue.md](testing/scratch_queue.md)). Functional tests are PWG-01 to PWG-30 in power_grid.md 8.

## 8. P11 evaluation of the bare port (stage 4b)

Prepared 2026-11-13 for plan P11 ([plan_power_grid.md](plan_power_grid.md)). The owner plays the port (power_grid.md 8.1
hand list) and fills the **Owner** columns. Our facts come from power_grid.md 2.1 (overlap), 3.5 (bugs B5 to B34),
PORT_NOTES.md and the r1113 verdicts (`perf/runs/r1113/verdicts/pwg*.md`). Bug numbers are fixed in the port unless
marked "recorded".

### 8.1 Keep / drop / merge per content group

| Group | What it does | Overlap in the pack | Bugs found | Measured / perf notes | Owner: keep / drop / merge | Owner: notes |
|---|---|---|---|---|---|---|
| Wires and cords (copper, iron, golden, insulated wire; copper cord; cutters; connectors, socket, cord junction) | wire entities with resistance; the solver gives every node a voltage each tick | IE copper wire, CE iron and gold wire (D6, A3); IE wire cutter; IE connectors run a separate FE network | B5 (wire to ground ignored after the first solve) | copper drop matches 0.0015 ohm per item (SQ-PWG-16); burnt wire vanishes, no drop (-17); solver 0.11 ms per changed solve on a 1000-node ladder (offline harness) | | |
| Sources, gauges, meters (creative sources and resistor, voltage / current / power gauge, energy meter, thermometer, multimeter, debug item, `/powergrid`) | creative test parts and readouts | multimeter name with IE, ProjectRed, D&D (D7) | B8 (`performance get` NPE) | gauges read V / R within rounding (-14) | | |
| Overheated Component (port-only, D4) | an overheated device becomes a cracked block that keeps its data | none | none | drop chance 0 / 0.25 / 1 honoured (-18) | | |
| Passive and switching (power resistor, fuse holder, LV / MV / HV switch, HV breaker, LV button, contactor, redstone converter, spark gap, carbon pile, grounding rod) | protection and control on the grid | IE breaker and redstone breaker (different network) | B6 (grounding rod damage grew with distance) | HV switch and breaker add 2 SU each; fuse cut (-30); carbon pile holds a generator within about 5 V under load (-44) | | |
| Transformers (core, small, medium, nether; winding; variac) | step voltage by turns ratio; nether pair across a portal | IE transformers (FE tiers, a different system) | B19 (winding recursion crashed the server) | 1:10 small transformer: secondary 91.6 V for 100 V (-8.4 %), primary current 10.01 x secondary (-26) | | |
| Storage (battery, potato battery) | simulated cell with EMF and internal resistance | CC&A accumulators, IE capacitors | none | battery 388 800 J (108 Wh) at 90 %, about 0.1 ohm internal, 11.52 V at 10.55 A (-31) | | |
| FE bridge (FE inverter, device connector) | FE to PG and PG to FE at `forgeEnergyPerWatt` | the only link to IE / CC&A FE (section 3, converter layer 4) | none | inverter: 15.78 W gave 158 FE/t = 10.03 FE/t per W at upstream 10 (N6, -27); device connector into an IE LV capacitor: 239 FE/t at 120 V (-32) | | |
| Magnet (electromagnet, magnet item, lightning) | magnetizes andesite alloy | Quark magnet (name, D7) | B7 (config comment said iron ingot) | lightning about 5 % per strike; electromagnet only when powered (-33) | | |
| Motors (Electric Motor, Constant Speed Motor, servo, rheostat) | electric power to Create rotation | CC&A Electric Motor (dropped in advanced, D8), D&D kinetic motor | B9 (servo target lost without power) | 258 V: 255 RPM, 0.52 A idle, 5.05 A with 4 presses, 10.07 A at 9 presses (overstressed), 2.6 kW full load (-38); AC 0.5 Hz follows within about one read (-37) | | |
| Generator set (clutch, induction rotors, commutators, housings, windings) | Create rotation to electric power with back-EMF | CC&A Alternator (dropped, D8), IE Kinetic Dynamo | B19 | 256 RPM: 175 V open circuit (10 ohm), 24.6 A into 1 ohm at 50.7 V EMF, rotor slows 183 RPM under the clutch (-39, -44); self-excited 252.8 V at 256 RPM (-42) | | |
| Heat (heating coil, basin heater) | fan blasting and smoking; basin, boiler, FD pot and skillet heat | Create blaze burner, Compressed Creativity heater, IE external heater; Dragon Breathing fan type coexists (-48) | none | heater 50 V: blasting 484 C, raw iron in about 100 ticks; basin heater 140 / 200 / 250 V = fading / kindled / seething (-48, -49) | | |
| Fan and pump (electric fan, electric pump) | electric Create fan and pump | Create's own (kinetic) | none | fan 20 / 40 / 80 V = 64 / 128 / 256 RPM; pump 120 V 64 RPM, 240 V 256 RPM (-50, -51) | | |
| Lights (bulbs, LV bulb, neon bulb, light fixture, factory light, growth lamp) | light level follows power; growth lamp ticks crops | IE light bulb and floodlight, Beautify bulb (name, D7) | B11 (growth lamp config comment) | fixture 120 V emission 15; LV bulb at 120 V burns out within 10 ticks (-52); growth lamp has no season gate but its ticks fire `CropGrowEvent.Pre` (-53, PWG-22) | | |
| Solar (solar panel, bearing, ceiling solar) | daylight power; panels merge; bearing turns with the sun | none electric in the pack | B10 (only the last biome override applied) | panel at noon 60 W into 7 ohm; 4-panel string 233.5 W (3.89 x); desert override lower (-54) | | |
| Equipment (portable battery, drill, saw, Electro-Baton, Electro-Zapper) | chest-slot battery powers tools; tools charge from it | IE Accumulator Backpack, drill, buzzsaw; SecurityCraft taser (D9, A6) | B13, B14, B20 (battery charge lost into an FE item) | battery 20 000 FE base + 20 000 per Capacity level; drill -25 charge per block, dig speed 6 to 16 (-58); zapper 500 per shot, chain 3 x 2.7 (-60) | | |
| Display and control (CRT and encased CRTs, modular display, plotter, punch card and reader, alarm bell, CC: Tweaked peripherals) | analog displays, recording, card sequencing, computer access | Control Engineering CRT and punched tape (D10, A6); Create display board | B15, B16, B17 | plotter 80 samples at 64 RPM (-65); peripheral `voltage` 12.0 = gauge (-66); modular display slot by hand open (owner item 12) | | |
| Circuits (28 board components, circuit board, design table, schematic, editor, acid, etching, assembly) | simulated components on boards designed in an editor | PneumaticCraft PCB and etching acid, IE logic circuit, ProjectRed, Control Engineering (D5, A7); capacitor name with CC&A (D7) | B18 (recorded), B21 to B27 (B27 recorded) | diode 0.62 V, BJT and tube curves match the harness (SQ-PWG-06); P9b to P9d gate rows wait for the fixed probes (r1113 pwg16) | | |
| Integrated Circuit and boost | boosts drill, saw, baton, zapper (faster, double draw, counted uses) | D&D integrated circuit (name, D7) | B28, B29 | boosted drill -50 per block (SQ-PWG-87, pwg16 LOOK, to judge) | | |
| Decor (copper plating with slab and stairs, ceiling tiles and attachments, string light cord) | decoration with working lamps, solar and wiring | Create Deco and IE sheet metal; Fairy Lights (D11) | B30 to B34 | string light 3.125 W per item at 120 V, burns out above 127 V (SQ-PWG-95, to run) | | |
| Ponder (81 scenes, 5 tags) | in-game guides | none | B12 (missing `zap.json` crashed clients) | 36 P2 to P4 scenes play (CQ-PWG-14); later scenes in boot C1 | | |

### 8.2 H7: tier mapping measured on the bare port

Method (no integration, `forgeEnergyPerWatt` kept at upstream 10 so the FE side reads as measured, then scaled to the
decided 0.1):
1. **FE per watt in game:** SQ-PWG-27 measured 10.03 FE/t per W at the upstream 10 (the rate is FE per tick per watt,
   N6 confirmed). At 0.1, one watt is 0.1 FE/t.
2. **Device operating points** from the gate rows (table 8.1): Electric Motor 258 V, up to 10 A (2.6 kW at full load,
   0.13 kW idle); generator set 175 V open, about 0.6 kW into 1 ohm (24.6 A at 24.6 V); heater, basin heater, fan, pump, lights and solar at
   the voltages above. For each, record the voltage band it works in and its rated power (the run log row numbers).
3. **IE side:** the FE/t of IE's LV, MV and HV connectors (256, 1024, 4096), of IE generators (from IE's server config,
   energy_basic.md 2) and of CC&A's motor and alternator, from the pack's config, not measured.
4. **Bands:** group the operating voltages: devices that work at 12 to 48 V (LV bulb, fan at 20 V, servo), 120 to 260 V
   (motors, pump, fixtures, basin heater), above 500 V (spark gap, CRT anode, long lines). Each band's typical power x
   0.1 gives FE/t to set beside IE's connector rates; a band whose FE/t lands far from the IE tier it maps to moves.
5. **Check row:** SQ-PWG-32 (device connector into an IE LV capacitor: 239 FE/t at 120 V) with the rate at 0.1 must
   stay under the LV connector's 256 FE/t, so a single connector fits LV; record it again with the rate changed.

Current reading (from the numbers above, not yet a decision): at 0.1 a motor at full load is about 260 FE/t (one IE LV
connector), a generator into a 1 ohm load about 60 FE/t, a solar panel 6 FE/t, a fixture about 15 FE/t. The MV and HV
bands have no measured device yet besides the motor and transformer rows.

### 8.3 D14 decision inputs (client mirror)

| Input | Row | Numbers | Status |
|---|---|---|---|
| Server cost of the solver, no players | SQ-PWG-01 to -05 (P11 boot P1) | mean / p99 tick delta for 50, 200, 1000 nodes and 20 x 50 nodes; NetSolve, JavaMNA counters | to run (perf session) |
| Generator and motor cost | SQ-PWG-07 | tick delta, rotor steadiness | to run |
| Server tick with 40 players near a grid | SQ-PWG-08 | tick with perfBot bots (no network use: bots have no client) | to run |
| Mirror on vs off, client side | CQ-PWG-01, -02, -04 | FPS next to and away from 200- and 1000-node grids, both settings | owner (8.1 item 1) |
| Gameplay cost of server-only values | CQ-PWG-03, -06, -16 | needle lag felt; light glow with mirror off | owner (8.1 items 1, 3, 10) |
| Offline baseline | solver harness | 9 ms first solve, 0.11 ms per changed solve, 0.015 ms unchanged (1000 nodes) | done |

Decision rule suggested for the owner: keep the mirror unless CQ-PWG-04 shows a visible FPS drop next to a 1000-node
grid or SQ-PWG-08 shows server tick above 50 ms; otherwise the server-only setting (`clientMirror = false`, already in
the port, H5) becomes the default.

## 9. History

- 2026-11-13: section 8 (P11 evaluation scaffolding): keep / drop / merge table with our facts, H7 method on the bare
  port, D14 inputs.

- 2026-11-12: written from the owner's answers to power_grid.md H1 to H7; tier mapping investigated (section 3).
