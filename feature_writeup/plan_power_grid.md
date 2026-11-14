# Create: Power Grid backport: execution plan per phase

Written 2026-11-13. **P0 to P5 done 2026-11-13 (P2 scratch-run r1113, its fixes in the P3 jar; P3 / P4 gate runs in progress; P5 gate rows queued). P2 to P4 complete D1's "phase 1" (first evaluation point); P5 added ponder for that content; P6 (heat, light, fluid, solar) done 2026-11-13 (jar `-08`); P7 (equipment) done 2026-11-13 (jar `-09`); P8 (display and control) done 2026-11-13 (jar `-10`); P9 (components and circuits) runs as four sub-steps P9a to P9d (phases 9.1 to 9.4): P9a (components on boards, non-linear models) done 2026-11-13 (jar `-12`, with fix B19 for the P4 winding crash), P9b (design table and editor) done 2026-11-13 (jar `-14`, with fix B20 for the P7 portable battery), P9c (etching, acid, assembly) done 2026-11-13 (jar `-15`), P9d (Integrated Circuit and boost) done 2026-11-13 (jar `-16`): **P9 complete**; P10 (decor) done 2026-11-13 (jar `-17`): **the port is complete**; P11 (evaluation) prepared 2026-11-13: regression plan, owner hand list and evaluation scaffolding written, runs pending. The port runs through P11 (E5).** Source report: [power_grid.md](power_grid.md) (inventory, overlap,
decisions D1 to D14, holes H1 to H15, tests PWG-01 to PWG-44). Integration after the port: [energy_advanced.md](energy_advanced.md).
Test prefix `PWG-`. PG is Power Grid.

**Goal of this plan:** port **as much of PG 0.6.2 as feasible, for evaluation**: the owner plays every part of the mod
on 1.19.2 inside the pack before deciding what advanced mode keeps (energy_advanced.md stages 5 and 6). Port rules
apply throughout: upstream ids kept, no conversions, no advancements, no integration with pack systems until the port
is complete (D1, 5.1 delivery model).

## 0. Summary

| Phase | Content | Upstream size (classes) | Exit gate |
|---|---|---|---|
| P0 | Fork, Gradle project, licence files, platform inlining, phase manifest | n/a | empty mod loads on a scratch server |
| P1 | Solver core (`electricity/sim`) and thermal model (`circuits/thermal`) | about 90 | javac harness passes 5 circuits |
| P2 | Grid base: wires, connectors, sync, creative sources, gauges, wire tools, `/powergrid`, Overheated Component | about 130 | PWG-01 to -05, -25, -26, -28; SQ-PWG-01 to -05, -08; CQ-PWG-01 to -04 |
| P3 | Passive and switching devices, transformers, storage, FE bridge, magnet | about 150 | PWG-12 to -19, -24, -31 |
| P4 | Kinetics: motors, generator set, rheostat, variac, servo | about 80 | PWG-06 to -11; SQ-PWG-07 |
| P5 | Ponder library and scenes for P2 to P4 content (D12) | about 15 + scenes | PWG-32 |
| P6 | Heat, light, fluid and solar | about 70 | PWG-20 to -23, -33 |
| P7 | Equipment | about 35 | PWG-34 to -37 |
| P8 | Display and control, CC: Tweaked peripherals | about 60 | PWG-38 to -40 |
| P9 | Components and circuits (editor, boards, acid, Integrated Circuit boost) | about 160 | PWG-41 to -43; SQ-PWG-06 |
| P10 | Decor | about 20 | PWG-44 |
| P11 | Evaluation pass and handover to integration | n/a | owner keep/drop list written |

Each phase from P5 on ports its own ponder scenes along with its blocks (P5 sets up the library). The order goes from
what the voltage system needs to what is optional. P2 to P4 together are the "phase 1" of D1. Phases after P4 can be
reordered or stopped at any point: the manifest (P0) keeps unported content out of the registries and data.

**Not ported (infeasible or decided):**

| Upstream part | Why | Replacement |
|---|---|---|
| Native solver (`NativeMNA`, `.pg-native/`) | D4, B2 | Java solver only |
| 96 advancements and every trigger call (`PowerGridAdvancement`, also in `BeltDeployerMixin`, `SheepMixin`) | pack drops advancements | calls removed |
| REI, Cold Sweat, TFMG compat | mods not in the pack | dropped |
| Flywheel 1.0 visuals (33 files) | Create 0.5.1 has Flywheel 0.6 | the block entity renderers already in PG; Flywheel 0.6 instances only if a measured FPS problem appears |
| Data-driven damage types (`data/powergrid/damage_type`) | 1.19.4+ format | `DamageSource` constants with the same message keys |
| Explosions and block break on overheat | D4 | Overheated Component (P2), burnt wires removed (H4) |

## 1. Shared rules for every phase

- **Home:** `development/backportedPowerGrid` (mod id `powergrid`), `ce.forge-mod` plugin, own jar. Model:
  [backportedTempad](../development/backportedTempad/build.gradle) (`PORT_NOTES.md`, licence files, `-Xmaxerrs`).
- **Phase manifest:** `development/backportedPowerGrid/phases.json` maps every upstream block, item, entity, fluid and
  menu id to a phase. The `ModdedBlocks` / `ModdedItems` / `ModdedEntities` entries check a build-time constant
  (`PortPhase.CURRENT`), and `processResources` excludes the data and assets of unported ids (blockstates, models, loot,
  tags entries, recipes, ponder). Unported content is **not registered at all**, never half-registered (route B risk). The
  lang file ships whole.
- **Recipe gate:** a recipe ships only once its output **and every ingredient** are registered. A small checker
  (`tools/pg_phase_check.py`, dev-only) reads the manifest and the upstream recipe JSONs, lists the recipes each phase
  enables, and fails the build on a recipe whose ingredient is in a later phase. Recipe JSON fixes at port time:
  drop `category` and `show_notification`; `create:copper_sheet` to `#forge:plates/copper`; `create:cardboard` to
  `#forge:cardboard` (D13, H6: the tag is filled by the pack in the integration stage, so those 4 recipes load
  uncraftable until then).
- **Mixins** go in with the phase whose content they serve (table in each phase). `defaultRequire: 1` stays, so a
  missing target fails at load and does not pass silently.
- **API mapping** follows power_grid.md 3.6: Catnip to `foundation.utility`, `create.api` to the 0.5.1 classes,
  `GuiGraphics` to `PoseStack` + `GuiComponent`, registries to `Registry.*_REGISTRY` / `ForgeRegistries`, `Material` on
  block properties, `ItemDisplayContext` to `TransformType`, `CreativeModeTab` subclass.
- **Evaluation install:** the PG jar does **not** go into the pack's `mods/` during the port (no basic/advanced switch
  exists yet, and PG clashes with IE, CC&A and CE items, power_grid.md section 2). Builds run on scratch servers and
  clients (skill `scratch-test`) with the pack's mods plus the PG jar (E1, answered).
- **Per phase, done means:** build clean with `--offline`; the phase's harness or scratch rows written in
  [scratch_queue.md](testing/scratch_queue.md) (run only when the owner says); `PORT_NOTES.md` lists the classes ported
  and every behaviour change; power_grid.md history line; TODO `mod-additions #74` status. **No changelog bullet** during
  the port (the jar is not in the pack); bullets come with stage 5.
- **Upstream bugs** found while porting are fixed and listed in power_grid.md 3.5 (B5 onward).

## 2. Phases

### P0. Fork and skeleton

**Done 2026-11-13** (history; PORT_NOTES.md). Gate row SQ-PWG-09 queued.


1. Clone `github.com/patryk3211/PowerGrid` at the commit tagged for 0.6.2 on `architectury-1.20.1` into a scratch dir;
   record the commit hash in `PORT_NOTES.md`. Diff the tag's resources against the decompile
   (`DecompiledMods/powergrid-mc1.20.1-0.6.2/`) to confirm it is the same release.
2. Merge `common/` and `forge/` into one Forge source tree; inline every `@ExpectPlatform` with its forge impl (D2).
   Keep Architectury 6 calls (`NetworkManager`, events, menus); pack has 6.6.92.
3. LICENSE (Apache-2.0 text), NOTICE (upstream authors, "modified by" statement, our GPLv3 for new files) (B1, H12).
4. Gradle: `settings.gradle` include, `IN_TREE_MODS` in `tools/build_deploy.py`, jar-in-jar EJML 0.44 (core, dense,
   sparse, simple) and MixinExtras 0.3.5 (check first whether Creative Common already ships MixinExtras), Registrate as
   Create 0.5.1.i depends on it, `compileOnly` on the pack's Create, Architectury, JEI and CC: Tweaked jars.
5. Configs: port `CServer`, `CSolver`, `CGenerator` and the rest whole (keys for unported blocks are harmless). Fix B3 in
   a comment only (the key name stays upstream's, E4); add the missing comments (B4). Add the server-values-only switch
   needed by H5 (`clientMirror`, default on) here so P2 can wire it.
6. `PortPhase`, `phases.json` (generated once from the lang and `Modded*` lists, then edited by hand), resource exclude,
   `tools/pg_phase_check.py`. Creative tab subclass.
7. Delete the native solver, REI, Cold Sweat, TFMG, advancement classes and data.

Gate: `./gradlew :backportedPowerGrid:build --offline`; scratch server loads `powergrid` with nothing registered and no
mixin errors (all mixins disabled until their phase).

### P1. Solver core

**Done 2026-11-13** (history; PORT_NOTES.md sections 7 and 10). Harness `tools/test_power_grid_solver.sh` passes; jar
0.6.2-ce-19.2-02; scratch row SQ-PWG-11 queued. `circuits/thermal` moved to P2 (it needs `ThermalBehaviour`).

| | |
|---|---|
| Code | `electricity/sim` (72 files), `circuits/thermal` |
| Changes | Catnip `Pair` to Create 0.5.1 `Pair`; no Minecraft types expected; any found are moved behind an interface |
| Harness | javac/java harness (as for Creative Common, memory `patchermod-headless-smoke-test`) with EJML on the classpath: voltage divider, RC charge to 63 % within 2 %, ideal transformer 1:10 under load, diode forward drop near 0.7 V, floating grid stays finite (power_grid.md 7). Add: a 1000-node ladder (time per solve, input for SQ-PWG-04) |

Gate: harness passes; timings written to `PORT_NOTES.md`.

### P2. Grid base

**Done 2026-11-13** (history; PORT_NOTES.md sections 7, 11, 12). Jar 0.6.2-ce-19.2-03 (`port_phase=2`), gate copy in
`jar_archive/pwg_gate/`. Gate rows SQ-PWG-12 to -23 and CQ-PWG-05 to -09 queued, perf rows refreshed. Deviations from the
table below: `EntityMixin` moved to P3 (it makes the P3 `magnet`); menu `custom_display` moved from P8 to P2 (the gauges
open it); `PonderElectricNetwork` and particles not needed yet.

| | |
|---|---|
| Blocks | `wire_connector`, `heavy_wire_connector`, `cord_junction`, `socket`, `creative_voltage_source`, `creative_current_source`, `creative_resistor`, `voltage_gauge`, `current_gauge`, `power_gauge`, `energy_meter`, `thermometer` |
| Items | `wire`, `iron_wire`, `golden_wire`, `insulated_copper_wire`, `copper_cord`, `wire_cutter`, `multimeter`, `electrical_gizmo`, `debug` |
| Entities | `hanging_wire`, `block_wire`, `cord` |
| Code | `electricity/base`, `wire`, `wireconnector`, `creative`, `gauge`, `info`, `socket`, `WorldNetworks` and the client mirror, `network/packets`, `commands`, `equipment/multimeter`, `equipment/thermometer` |
| Data | `wire_types` as a JSON reload listener plus a sync packet (1.19.2 has no dynamic registry for it); damage sources `electrocution`, `live_wire_cutting`, `overloaded_machine` |
| Mixins | `EntityMixin` (verify its lightning or item use; may move to P3), `PlayerMixin`, `BlockStateBaseMixin`, `LevelEntitiesAccessor`, `EntityTrackerAccessor`, `ThreadedAnvilChunkStorageAccessor`, `ItemAccessor`, `SoundEntryBuilderMixin`; client `ComplexEntityRaycastMixin`, `RenderBuffersAccessor`, `BlueprintOverlayMixin`, `BlueprintOverlayRendererAccessor` |
| New code | **Overheated Component** block and block entity (D4): stores the original state and NBT, renders the original model with the destroy-stage crack overlay, shows the stored data on goggles, has no item, drops the component on a chance (config `overheatedDropChance`). Every `ThermalBehaviour` overheat path routes to it. **Burnt wires** are removed with no drop (H4). **Server-values-only** switch from P0 wired into gauges, light and wire glow (H5) |
| Chunk handling | read how upstream handles a network across unloaded chunks before porting `WorldNetworks` (H9); record it in `PORT_NOTES.md` |

Gate: PWG-01 to -05, -25, -26, -28. Then the performance rows SQ-PWG-01 to -05 and -08 and CQ-PWG-01 to -04: they are
the D14 decision input and need only P2 content (creative sources and resistors).

### P3. Passive, switching, transformers, storage, FE bridge

**Done 2026-11-13** (history; PORT_NOTES.md sections 7 and 13). Jar 0.6.2-ce-19.2-04 (`port_phase=3`), gate copy in
`jar_archive/pwg_gate/`. Gate rows SQ-PWG-24 to -34 and CQ-PWG-10 to -12 queued (probe
`tools/scratch_query/pwg_p3_probe.js`). Deviations from the table below: `ElectricKineticBlockEntity` (P4) and the Create
0.5.1 stress provider moved here (the HV switch and HV breaker are kinetic); `AirCurrentMixin` moved here from P6 (PWG-14
needs fan cooling; circuit board branch P9); the winding's generator housing branches wait for P4; the battery's sequenced
assembly needs `acid` and loads with P9 (only the transformer core assembly ships); PWG-31 is lightning on andesite
alloy, not iron (B7).

| | |
|---|---|
| Blocks | `power_resistor`, `fuse_holder`, `lv_switch`, `mv_switch`, `hv_switch`, `hv_breaker`, `lv_button`, `contactor`, `redstone_converter`, `transformer_core`, `transformer_small`, `transformer_medium`, `winding`, `nether_transformer`, `battery`, `potato_battery`, `grounding_rod`, `spark_gap`, `carbon_pile`, `carbon_pile_coil`, `electromagnet`, `fe_inverter`, `device_connector` |
| Items | `copper_coil`, `resistive_coil`, `magnet`, `zinc_sheet` (crafting material; clashes with CE's zinc sheet, resolved at integration A3), `incomplete_battery`, `incomplete_transformer_core` |
| Code | `electricity/resistor`, `fuse`, `electricswitch`, `contactor`, `redstoneconverter`, `transformer`, `battery`, `grounding`, `sparkgap`, `carbonpile`, `electromagnet`, `febridge`, `deviceconnector` |
| Recipes | own type `magnetization` (lightning magnetizes iron, 5 %), sequenced assembly for battery and transformer core (check transitional items on 0.5.1) |
| Mixins | `LightningAccessor`; client `ValueSettingsScreenMixin` (transformer winding screen) |
| Compat | JEI magnetization category |
| FE bridge | upstream rates (10 FE per W, 2 FE per V) stay in the port; the 0.1 rate of H7 is an integration value. Measure N6 (FE per tick per watt) in PWG-18 |

Gate: PWG-12 to -19, -24, -31.

### P4. Kinetics

**Done 2026-11-13** (history; PORT_NOTES.md section 14). Jar 0.6.2-ce-19.2-05 (`port_phase=4`), gate copy in
`jar_archive/pwg_gate/`. Gate rows SQ-PWG-35 to -44, SQ-PWG-07 (made runnable) and CQ-PWG-13 queued (probe
`tools/scratch_query/pwg_p4_probe.js`). Deviations from the table below: none in content (all 13 blocks and every
`kinetics` package listed); the stress side is the Create 0.5.1 provider registered in P3 (`BlockStressValues` has no
per-block setter on 0.5.1; generator speeds through `CStress.setGeneratorSpeed`); the five Flywheel 1.0 visuals are not
ported (E3), the renderers draw shafts and rotors. Generator coupling needs no change on Create 0.5.1 (stress-only
coupling). Upstream bug B9 (servo) fixed. The commutator stays uncraftable until P9 (31 overrides unchanged).

| | |
|---|---|
| Blocks | `electric_motor`, `constant_speed_motor`, `generator_housing`, `vertical_generator_housing`, `generator_clutch`, `generator_commutator`, `generator_vertical_commutator`, `generator_induction_rotor`, `generator_large_induction_rotor`, `conductive_casing`, `rheostat`, `variac`, `servo` |
| Code | `kinetics/base`, `generator`, `motor`, `rheostat`, `variac`, `servo` |
| Create | stress and capacity through 0.5.1 `BlockStressValues`; `KineticBlockEntityAccessor`; damage source `spinning_rotor`; check generator coupling against Create 0.5.1 speed propagation (no Create 6 rotation API) |

Gate: PWG-06 to -11; SQ-PWG-07.

### P5. Ponder

**Done 2026-11-13** (history; PORT_NOTES.md section 15). Jar 0.6.2-ce-19.2-06 (`port_phase=5`), gate copy in `jar_archive/pwg_gate/`. Gate rows SQ-PWG-45 (structures load on 1.19.2, probe `tools/scratch_query/pwg_p5_probe.js`) and CQ-PWG-14 (PWG-32, every scene) queued. Deviations from the list below: 36 scenes ported; nine scenes of the list place later-phase blocks and wait for that phase: `power_gauge`, the current gauge (the voltage gauge scene is in), `switch` (LV switch, LV button, MV switch), `hv_breaker`, `wire/simple`, `wire/wire_loop`, `wire/cord` (P6: light fixture, heating coil, basin heater), `generator/generator_3` (P6: basin heater), `wire/string_lights` (P10). The structure check found every block and property on 1.19.2 (two upstream-same file quirks). Upstream's never-ending `tickFor` kept (power_grid.md 3.5).

- Port PG's ponder base to 0.5.1 `foundation.ponder` (`SceneBuilder`, `PonderRegistrationHelper`, tags). Scene
  structures are the 43 `.nbt` files in `assets/powergrid/ponder/`: check that 1.20.1 structure NBT loads on 1.19.2
  (data version newer than the game; the blocks are the same, so expect it to load; verify per file).
- Scenes for P2 to P4 content: wire, gauges, switch, fuse, power_resistor, contactor(_stack), redstone_converter,
  transformer, battery, ground1/2, carbon_pile, electromagnet, magnet, lightning_attractor, device_connector, hv_switch,
  hv_breaker, generator, motor, constant_speed_motor, rheostat, variac, servo, energy_meter, power_gauge.
- Each later phase carries its own scenes.

Gate: PWG-32 (every ported scene opens and plays, no missing block in any scene).

### P6. Heat, light, fluid, solar

**Done 2026-11-13** (history; PORT_NOTES.md sections 16, 17). Jar 0.6.2-ce-19.2-08 (`port_phase=6`), gate copy in
`jar_archive/pwg_gate/` (sha1 08533f728ab433b0258ac3bf5144d0f193aab73f; `-07` has the same content without
`particles/zap.json` and is superseded). Gate rows SQ-PWG-46 to -54, CQ-PWG-15, -16 queued (probe
`tools/scratch_query/pwg_p6_probe.js`). Deviations from the table below: none in content (9 blocks, 5 items, every listed
package and mixin; `BasinRecipeMixin` serves circuit schematics and is a no-op until P9; the bearing's Flywheel 1.0
visual is not ported, E3). The fan processing types, the contraption type and the solar attached check register in the
common setup's `enqueueWork` (Create 0.5.1 keeps them in plain collections); `solar_biome_override` is a reload listener
(upstream bug B10 fixed). Growth Lamp as upstream: its random ticks fire `CropGrowEvent.Pre`, so the pack's season
refusal applies to them, but the lamp still ripens off-season crops (PWG-22, SQ-PWG-53). Ponder: 22 scenes (`encased_fan`
waits for P9). Upstream bug B11 (growth lamp config comment) found, fixed with P7; B12 (no `zap.json`, client crash since
P3) fixed in `-08`. 31 recipe overrides unchanged.

| | |
|---|---|
| Blocks | `heating_coil`, `basin_heater`, `electric_fan`, `electric_pump`, `light_fixture`, `factory_light`, `factory_light_light`, `solar_panel`, `solar_panel_bearing` |
| Items | `light_bulb`, `lv_light_bulb`, `neon_bulb`, `growth_lamp`, `incomplete_solar_panel` |
| Code | `electricity/heater`, `basinheater`, `fan`, `pump`, `light`, `solarpanel` |
| Data | `solar_biome_override` as a reload listener |
| Mixins | `AirCurrentMixin` (done in P3, without its circuit board branch; P9 adds it), `FanProcessingMixin`, `ChuteBlockEntityMixin`, `FluidPropagatorMixin`, `BasinRecipeMixin`; Farmer's Delight `CookingPotBlockEntityMixin`, `SkilletBlockEntityMixin` (FD installed) |
| Checks | fan processing type against the pack's Dragon Breathing hook (memory `create-fan-processing-type`); Growth Lamp random ticks run on the port as upstream; the season gate is an integration step (PWG-22 records the upstream behaviour) |

Gate: PWG-20 to -23, -33.

### P7. Equipment

**Done 2026-11-13** (history; PORT_NOTES.md section 18). Jar 0.6.2-ce-19.2-09 (`port_phase=7`), gate copy in
`jar_archive/pwg_gate/` (sha1 5008eaaf00388d0982cfefe3330a36995e2fc730). Gate rows SQ-PWG-55 to -61, CQ-PWG-17, -18
queued (probe `tools/scratch_query/pwg_p7_probe.js`). Deviations from the table below: one more mixin,
`DrillBreakActionMixin` (new, GPLv3; Forge 1.19.2's `LeftClickBlock` has no break action, so upstream's left-click hook
moved to `ServerPlayerGameMode.handleBlockBreakAction`); `ItemBoostUtils` ported now (the boost tag helpers the tools
call; boosting stays inert until P9 brings the Integrated Circuit); the zapper's "Boosting Chip in the other hand" check
returns with P9. Upstream has no P7 ponder scene. B11 fixed (comment), B13 (zap chain sparks) and B14 (a player's baton
never wore) found and fixed. PWG-37 wording corrected (the saw does not shear). 31 recipe overrides unchanged.

| | |
|---|---|
| Blocks / items | `portable_battery` (block), `portable_battery` and `portable_battery_placeable` (items), `portable_drill`, `portable_saw`, `electrobaton`, `electrozapper` |
| Entities | `zap_projectile`; damage source `zap` |
| Code | `equipment/portablebattery`, `drill`, `saw`, `baton`, `zapper`, `thunder` |
| Mixins | `PlayerDrillMixin` (its boost branch stays inert until P9), `SheepMixin` (advancement call removed) |
| Notes | Capacity enchantment is Create's (present in 0.5.1); chest-slot battery uses the vanilla armor slot, no Curios |

Gate: PWG-34 to -37.

### P8. Display and control

**Done 2026-11-13** (history; PORT_NOTES.md section 19). Jar 0.6.2-ce-19.2-10 (`port_phase=8`), gate copy in
`jar_archive/pwg_gate/` (sha1 c4624ce39cffd84966af81de8c5b4a28fd487c7f). Gate rows SQ-PWG-62 to -66, CQ-PWG-19 to -21
queued (probe `tools/scratch_query/pwg_p8_probe.js`, CC program `tools/scratch_query/pwg_p8_cc_startup.lua`). Ponder
scenes plotter, modular_display, bell, crt, punch_cards ported (the display module's scene is a circuit scene, P9).
Deviations from the table below: no display source to map (upstream's four were ported in P2 to P4; P8 adds none, and no
display target); a high-priority Forge right-click listener (`ForgeEvents.recordInteractingItem`, GPLv3) and a
`createBoard` override stand in for Create 6 API on the modular display (value box `bypassesInput`, option labels).
B15 (card lost when a reader breaks), B16 (burnout packet off the client thread), B17 (CC redstone converter writes
from the computer thread) found and fixed. PWG-40 wording corrected (no display peripheral upstream). Upstream's
`EquationParserTests` joined the solver harness. 31 recipe overrides unchanged.

| | |
|---|---|
| Blocks | `crt`, `andesite_encased_crt`, `brass_encased_crt`, `modular_display`, `plotter`, `punch_card_reader`, `alarm_bell` |
| Items | `display_module`, `punch_card`, `incomplete_punch_card` |
| Code | `electricity/crt`, `modulardisplay`, `bell`, `kinetics/plotter`, `punchcard` |
| Compat | CC: Tweaked peripherals (`compat/cc`); Create display sources mapped to 0.5.1 `DisplayBehaviour` |

Gate: PWG-38 to -40.

### P9. Components and circuits

The largest phase (circuits package 135 classes, editor 10.7k decompiled lines). Run as four sub-steps with their own
decimal phase in `phases.json` and `port_phase` (9.1 to 9.4; PORT_NOTES.md section 5), each with its own jar and gate.

**P9a done 2026-11-13** (history; PORT_NOTES.md section 20). Jar 0.6.2-ce-19.2-12 (`port_phase=9.1`), gate copy in
`jar_archive/pwg_gate/` (sha1 75aa8fc5b1f41df529101f4e92d69db7f84e8d85; `-11` is the same without fix B19). Boundary: the circuit board block, block
entity, renderer and baked model; every component (forge registry `powergrid:components`, 28) with its properties; the
schematic data model without the schematic item and its GUI render; `component_items` as a reload listener plus sync;
the 17 component items, `pins` and the two incomplete BJTs (their own assembly, moved from P9c); wires to board
terminals and traces; the potentiometer value screen; board fan cooling; debug item and multimeter readouts; the 15
circuit ponder scenes, `encased_fan` and the Circuit Components tag; the non-linear models (diode, NPN, PNP, triode,
pentode, thyratron, regulator tube) in the solver harness. Boards come from items carrying a schematic (pick-block,
drops, NBT) until P9b / P9c make them. 28 recipes ship (the components, the BJT assemblies, and the 11 that waited for
`pins`); 31 overrides kept, the 11 `pins` ones now met. Gate rows SQ-PWG-67 to -71 (-71: B19 regression), SQ-PWG-06 (now runnable),
CQ-PWG-22, -23 (probe `tools/scratch_query/pwg_p9a_probe.js`). Deviations: the circuit board edit menu (creative,
empty hands) waits for P9b; the creative tab applies upstream's exclusion list from now on; P8's display module option
needed no translation in P9a (the editor widget shows enum names). B18 (tube convergence reported early) recorded, not
changed; B19 (winding owner recursion, the `-10` server crash, P4 code) fixed in `-12`.

**P9b done 2026-11-13** (history; PORT_NOTES.md section 21). Jar 0.6.2-ce-19.2-14 (`port_phase=9.2`), gate copy in
`jar_archive/pwg_gate/` (sha1 d80a8babfdc03300f60b446813f08da9eb31bb7f; `-13` is the same without fix B20). Boundary: the circuit design table (block,
block entity, glow renderer), the circuit schematic item, the editor (`circuits/editor`, `circuits/gui`,
`CircuitSchematicRender`), the four menus, packets `CHANGE_SCREEN` and `SAVE_SCHEMATIC`, the creative board edit menu,
`CircuitSchematic.toItemStack`, the JEI ghost-ingredient handler. Designs are drawn on a powered table (30 W) and
written onto Empty Schematics; creative players turn a schematic into a board and edit placed boards; survival boards
wait for the etching chain (P9c). The table's crafting recipe waits for `copper_plating` (P10); `deploying/schematic_copy`
is held to 9.3 with `BeltDeployerMixin`'s schematic branch (P9c). Gate rows SQ-PWG-72 to -75, CQ-PWG-24, -25 (probe
`tools/scratch_query/pwg_p9b_probe.js`). Upstream bugs B21 (packets act at any distance), B22 (board editor name
dropped), B23 (write deletes the output slot's schematic), B24 (editor removals and option changes not recorded)
fixed; B20 (P7 code: the worn portable battery lost its charge on an FE item with a big buffer, SQ-PWG-57) fixed in the
same jar. Deviations: the schematic item's creative tooltip line shows by the client player's game mode only (1.19.2's
tooltip flag has no creative bit); 1.20 focus navigation dropped.

**P9d done 2026-11-13; P9 complete** (history; PORT_NOTES.md section 23). Jar 0.6.2-ce-19.2-16 (`port_phase=9.4`, the
last P9 sub-step; P10 is 10), gate copy in `jar_archive/pwg_gate/` (sha1 618ed8016949c7be095f2f7b80156c6316a54000).
Boundary: `integrated_circuit` (`BoostingChipItem`, `BoostingChipRenderer`), `incomplete_electrical_gizmo`, the
`boost_recipe` serializer (`BoostRecipe`, type `create:deploying`), the `BoostRecipe` branch of `RecipeApplierMixin`
(upstream copy deleted), the Electro-Zapper's chip check; `PlayerDrillMixin`'s boost branch (in since P7) now reachable.
6 recipes ship (106 in all): the Integrated Circuit, the electrical gizmo assembly (it unlocks the 9 recipes that use
the gizmo, a P2 item with no runnable recipe until now) and the four boosts; their 5 overrides are met, only the two
`copper_plating` ones (P10) remain. Gate rows SQ-PWG-84 to -89, CQ-PWG-27 (PWG-43; probe
`tools/scratch_query/pwg_p9d_probe.js`). Upstream bugs B28 (the chip boosted whatever the other hand held when its use
ended) and B29 (no Boosted tooltip on the baton and zapper) fixed. Deviations: no JEI boost category of our own
(upstream has none either: the boosts are `create:deploying` recipes and show on Create's Deploying page);
`BeltDeployerMixin` gains nothing (its upstream boost branch only awarded an advancement); the BOOSTING_CHIP
advancement removed.

| | |
|---|---|
**P9c done 2026-11-13** (history; PORT_NOTES.md section 22). Jar 0.6.2-ce-19.2-15 (`port_phase=9.3`), gate copy in
`jar_archive/pwg_gate/` (sha1 f95311cc26f8294fe100347ca2d841af193de4dd). Boundary: the acid fluid (source, flowing,
liquid block, bucket; `acid` damage while in it), `empty_circuit`, `unetched_circuit`, `incomplete_unetched_circuit`,
`incomplete_circuit` (`IncompleteCircuitItem`), the mixins `BeltDeployerMixin` (schematic copy only, no advancement),
`RecipeApplierMixin` (schematic branch; the boost branch is P9d), `ArmInteractionPointMixin`, `ArmBlockEntityMixin`,
and the JEI Circuit Designing (design table catalyst) and Circuit Assembly (mechanical arm) categories. 6 recipes ship
(100 in all): acid mixing, etching, the unetched circuit assembly, empty circuits from slabs,
`deploying/schematic_copy` and the battery assembly P3 held back for acid; those two overrides are met. Survival
boards now work end to end: schematic (table or deployer copy) -> unetched circuit (deployers and a press) -> etched
in a heated basin with acid -> parts put in by mechanical arms -> Circuit Board with the design. Gate rows SQ-PWG-76 to
-83, CQ-PWG-26 (probe `tools/scratch_query/pwg_p9c_probe.js`, which builds the machines). Upstream bugs B25 (arm belt
lock skipped after a non-belt output) and B26 (incomplete circuit tooltip / bar errors) fixed, B27 (a circuit with no
design or no parts can never be completed) recorded. Deviation: `RecipeApplierMixin` came forward from P9d with its
schematic branch (the unetched circuit chain needs it); `Inject.order` dropped (Mixin 0.8.5). The asset check now
covers fluids.

| Blocks | `circuit_board`, `circuit_design_table`, `acid` (fluid block) |
| Items | components `resistor`, `potentiometer`, `capacitor`, `diode`, `bjt_npn`, `bjt_pnp`, `vfet`, `triode`, `pentode`, `thyratron`, `barretter_tube`, `regulator_tube`, `varistor`, `relay`, `relay_dpdt`, `redstone_relay`, `pins`; `circuit_schematic`, `empty_circuit`, `unetched_circuit`, `integrated_circuit`, `acid_bucket`, `incomplete_bjt_npn`, `incomplete_bjt_pnp`, `incomplete_circuit`, `incomplete_unetched_circuit`, `incomplete_electrical_gizmo` |
| Code | `circuits/circuitboard`, `components`, `editor`, `gui`, `schematic`; `equipment/BoostRecipe`, `ItemBoostUtils` |
| Data | `component_items` (28 entries) as a reload listener plus sync. It also maps items registered in earlier phases onto boards (bulbs, neon bulb, gauges, switch, button, fuse holder, display module, connector); those items keep their phase, and only their board use arrives here. Board-only entries `inductor`, `label`, `via` have no item; recipe types `boost_recipe` and the sequenced assembly chains; damage source `acid` |
| Mixins | `ArmBlockEntityMixin`, `ArmInteractionPointMixin`, `BeltDeployerMixin` and `RecipeApplierMixin` (boost; advancement call removed) |
| GUI | `GuiGraphics` to `PoseStack` + `GuiComponent` for the whole editor (32 files); client config colours |
| Compat | JEI boost category |
| Sub-steps | P9a (9.1) components on boards and the solver's non-linear models (diode, BJT, tubes): board, components, schematic model, `component_items`, component items and `pins`, circuit ponder; P9b (9.2) design table and editor (`circuits/editor`, `gui`, schematic item and render, menus, edit menu); P9c (9.3) etching, acid, assembly (`IncompleteCircuitItem`, acid, unetched / etched chains, arm mixins); P9d (9.4) Integrated Circuit and boost (`BoostingChipItem`, `BoostRecipe`, deployer and applier mixins, JEI boost). Each sub-step builds and is playable on its own; the manifest split is in `phases.json` (PORT_NOTES.md section 20) |

Gate: PWG-41 to -43; SQ-PWG-06 (non-linear convergence).

### P10. Decor

**P10 done 2026-11-13; the port is complete** (history; PORT_NOTES.md section 24). Jar 0.6.2-ce-19.2-17
(`port_phase=10`), gate copy in `jar_archive/pwg_gate/` (sha1 cbc2ddee37ca4e8f8d844958a9744bc02c148d4a). Everything in
the table below is in, with the ponder scenes `ceiling_tile` and `wire/string_lights` and the Ceiling Tile Attachments
tag; every upstream id is registered (78 blocks, 119 items, 5 entities) and every recipe file ships (116). The last two
overrides (`crafting/circuit_design_table`, `crafting/solar_panel_bearing`, both `copper_plating`) are met: all 32 are.
Copper plating has no oxidation or waxing upstream. `upstream/java` now holds only code that never ports (31 files:
datagen, Flywheel 1.0 visuals, REI, TFMG, the 1.20 tab generator); the 37 partial copies are deleted. Gate rows
SQ-PWG-90 to -96, CQ-PWG-28, -29 (probe `tools/scratch_query/pwg_p10_probe.js`). Upstream bugs B30 (string light cord
rewire left old wires in the network), B31 (stale light blocks after a reload), B32 (ceiling lamp lost its bulb), B33
(plating slab and stairs harvestable by no tool; double slab dropped one), B34 (empty pattern tag stopped stacking)
fixed. Deviations: none beyond the 1.19.2 API changes (Forge's `StairBlock(Supplier, ...)`, the string light block on
the factory light's `Material`, the special recipe on `SimpleRecipeSerializer`).

| | |
|---|---|
| Blocks | `copper_plating`, `copper_plating_slab`, `copper_plating_stairs`, `ceiling_tile`, `ceiling_tile_connector`, `ceiling_tile_junction`, `ceiling_tile_lamp`, `ceiling_tile_solar`, `string_light_block` |
| Items / entities | `string_light_cord` (item and entity), recipe type `crafting_special_string_light_cord` |
| Code | `general/ceilingtile`, string lights |

Gate: PWG-44. Last priority (D11), but ported in full for evaluation; the non-overlap filter is an integration step.

### P11. Evaluation and handover

1. Full scratch run of PWG-01 to -44 and the SQ/CQ-PWG rows on the last build; results in the queue run log.
2. D14 answered from CQ-PWG-01 to -04.
3. The owner plays the port in a scratch instance and fills a **keep / drop / merge** table per content group in
   energy_advanced.md (feeds A3, A6, D9 to D11).
4. power_grid.md 3.5 gets every upstream bug found; `PORT_NOTES.md` is final; the tier mapping of energy_advanced.md 3
   is measured on the bare port (H7).
5. Stage 5 (mode switch, converter layer) starts from energy_advanced.md.

**P11 preparation done 2026-11-13** (no code change; target jar `-17`, sha1 cbc2ddee37ca):
- Step 1: regression plan in [scratch_run_2026-11-13.md](testing/scratch_run_2026-11-13.md) section 9: 5 boots (world
  `pwgr` R1 to R3, world `pwgperf` with a perf session) plus one client pass, 80 server rows plus the 6 P10 rows, about 3
  hours with judging; batches `tools/scratch_query/batches/r1113pwg/` (`pwgr1.qb`, `pwgr2.qb`, `pwgr3.qb`,
  `pwgperf.qb`); superseded boot and check rows listed there.
- Step 2: D14 inputs listed in energy_advanced.md 8.3 (SQ-PWG-01 to -08, owner CQ-PWG-01 to -04, -03, -06, -16).
- Step 3: owner hand list power_grid.md 8.1 (17 items, 25 CQ-PWG rows); keep / drop / merge table with our facts in
  energy_advanced.md 8.1 (owner columns empty).
- Step 4: bugs B5 to B34 in power_grid.md 3.5 are current; H7 method in energy_advanced.md 8.2.
- Open: the runs themselves (runner), the owner session, and the pwg16 probe fault (P9b to P9d rows not yet judged on a
  working probe). SQ-PWG-63 closed: Create 0.5.1's value box handler replays every fake-player click as a block-centre
  hit (not a port fault; probe calls the block's `use` directly now; the hand check is owner item 12).

## 3. Open points

| # | Point | Suggested answer |
|---|---|---|
| E1 | **(answered 2026-11-13: as suggested)** Where the port runs during evaluation | scratch servers and clients only (pack mods plus the PG jar); the pack's `mods/` gets the jar only with the stage 5 mode switch |
| E2 | **(answered 2026-11-13: as suggested)** Ponder: one phase (P5) for core content then per phase, or all scenes at the end | as planned (D12 puts ponder right after the core) |
| E3 | **(answered 2026-11-13: as suggested)** Flywheel 0.6 instances for heavy blocks (generators, motors) | not planned; only if CQ-PWG rows show an FPS problem from renderers |
| E4 | **(answered 2026-11-13: as suggested)** Config key typo `splittingsTransformers` (B3) | keep the key, comment it |
| E5 | **(answered 2026-11-13: no early stop)** Stop point | the owner decided the port continues through P11 (every phase P7 to P10, then the evaluation pass); earlier suggestion: any phase after P4 could be the last one |

## 4. History

- 2026-11-13: **P11 prepared** (regression plan, batches `r1113pwg/`, owner hand list, energy_advanced.md section 8
  scaffolding; SQ-PWG-63 explained, probe change only). No code change; jar stays `-17`.
- 2026-11-13: **P10 done; the port is complete.** Moved `general/ceilingtile` (13) and `electricity/light/string` (5);
  `ModdedBlocks` (9), `ModdedItems`, `ModdedEntities`, `ModdedBlockEntities` (4), `ModdedPartialModels` (copper plating
  CT, ceiling light), the `crafting_special_string_light_cord` serializer, the Carry On entity entry, the cord's
  tooltip lines in `WireItem`, ponder (`connectLightCord`, two scenes, the Ceiling Tile Attachments tag on its six
  upstream storyboards). Deleted the 37 partial upstream copies after checking each for leftovers (only datagen,
  platform switches, Flywheel 1.0 visuals and dropped compat); 31 never-port files stay in `upstream/java`. Fixed B30
  to B34 (power_grid.md 3.5). Jar `-17`, sha1 cbc2ddee37ca4e8f8d844958a9744bc02c148d4a; 116 recipes, all 32 overrides
  met; rows SQ-PWG-90 to -96, CQ-PWG-28, -29. Same session: SQ-PWG-67 answered (the pack removes `create:copper_nugget`,
  so the circuit component tag loads 27; row and power_grid.md section 4 updated). Next: P11.
- 2026-11-13: **P9d done; P9 complete.** Moved `BoostingChipItem`, `BoostingChipRenderer`, `BoostRecipe` and the
  `BoostRecipe` branch of `RecipeApplierMixin` (upstream copies deleted); `ModdedItems` (2), `PowerGrid.registerRecipes`
  (`boost_recipe` serializer), the Electro-Zapper's chip check. `port_phase=9.4`, jar 0.6.2-ce-19.2-16 (69 blocks, 114
  items, 106 recipes), build clean, asset check passes (1034 files ship, 44 left out); solver harness not rerun (no
  `sim` change). No manifest edit. B28, B29 fixed. power_grid.md sections 3.6 to 9 restored (lost in a P9a edit). P10
  notes: PORT_NOTES.md section 23.4.
- 2026-11-13: **P9c done.** Moved `ModdedFluids` (forge Impl folded in), `IncompleteCircuitItem`, the JEI
  `CircuitDesignCategory`, `CircuitAssemblyCategory`, `AnimatedMechanicalArm`, mixins `ArmBlockEntityMixin`,
  `ArmInteractionPointMixin`, `BeltDeployerMixin` (schematic branch), `RecipeApplierMixin` (schematic branch; boost
  branch P9d); `ModdedItems` (4), `PowerGridJEI` (categories, catalysts). `port_phase=9.3`, jar 0.6.2-ce-19.2-15 (69
  blocks, 112 items, 2 fluids, 29 mixins, 100 recipes), build clean, the client-asset check covers fluids; solver
  harness not rerun (no `sim` change). No manifest edit; overrides `sequenced_assembly/battery` and
  `deploying/schematic_copy` met. B25, B26 fixed, B27 recorded. P9d notes: PORT_NOTES.md section 22.4.

- 2026-11-13: **P9b done.** 25 classes moved (`circuits/editor` 12, `circuits/gui` 9, `CircuitSchematicItem`,
  `CircuitSchematicRender`, `IMultiScreenHandlerFactory`, the two packets), restored P9b parts in `CircuitSchematic`
  (`toItemStack`), `CircuitBoardBlock` (creative edit menu), `ComponentFootprint` (colours), `PowerGridJEI` (ghost
  handler); `utility/GuiGraphics` extended (now a `GuiComponent`: lines, gradient, `blitRepeating`, tooltips).
  `port_phase=9.2`, jar 0.6.2-ce-19.2-14 (68 blocks, 107 items, 56 block entities, 7 menus, 94 recipes), build clean;
  solver harness not rerun (no `sim` change). Manifest: override `deploying/schematic_copy` 9.3. B21 to B24 found and
  fixed; B20 (portable battery FE transfer, P7 code, scratch finding SQ-PWG-57) fixed. P9c notes: PORT_NOTES.md section 21.3.
- 2026-11-13: **P9a done.** P9 split into P9a to P9d with decimal phases 9.1 to 9.4 (`build.gradle`, `PortPhase` and
  `pg_phase_check.py` take decimals; `phases.json` sub-step entries and overrides). 77 classes moved (the components and
  their properties and model classes, the schematic model, the circuit board block, block entity, renderer and baked
  model, `CircuitBoardEndpoint`, `UpdateComponentBiPacket`, `CustomValueSettingsScreen`, `CircuitScenes`), new
  `ComponentItemsSyncS2CPacket` and the reload listener in `ComponentRegistry`; restored P9a parts in
  `IElectricEntity`, `DebugItem`, `MultimeterItem`, `AirCurrentMixin`; collections and glue. `port_phase=9.1`, jar
  0.6.2-ce-19.2-12 (67 blocks, 105 items, 55 block entities, 94 recipes, 28 components, 79 ponder scenes), build clean;
  solver harness 84 passed with `BoardComponentTests` (6 circuits). Create 0.5.1 / Forge 43 counterparts used:
  `RegistryBuilder.setName`, `IUnbakedGeometry` with `ModelBakery` and `getMaterials` (replaces the 1.19.3 atlas file),
  `ModelEvent.RegisterGeometryLoaders` / `RegisterAdditional`, `ValueSettingsScreen` without a network id. B18 found
  (not changed); B19 (winding owner recursion, scratch boot of `-10`) fixed. Creative tab now hides upstream's exclusion list. P9b notes: PORT_NOTES.md section 20.3.
- 2026-11-13: **P8 done.** 48 classes moved (CRT and encased CRT, renderer; modular display with 6 module types,
  registry, slot thermals, value box, renderer; alarm bell and its sound; plotter and renderer; punch card item, menu,
  screen, buttons, reader block, block entity with the forge item handler impl, renderer; CC: Tweaked bridge and 8
  peripherals) plus 2 packets and the P8 parts of the collections and glue (`PlacementOverlay` now complete).
  `port_phase=8`, jar 0.6.2-ce-19.2-10 (66 blocks, 85 items, 4 entity types, 54 block entities, 3 menus, 66 recipes, 25
  mixins), build clean; solver harness 78 passed with `EquationParserTests` added. Create 0.5.1 counterparts used as they
  are: `EncasingRegistry`, `GhostItemMenu`, `ScrollOptionBehaviour` (board overridden), `SpriteShifter`, the display link
  API (no new source); CC: Tweaked 1.101.4 API unchanged. B15 to B17 found and fixed. P9 notes: PORT_NOTES.md section 9.
- 2026-11-13: **P7 done.** 31 classes moved (portable battery block, block entity, item, utils and armor layer; drill,
  saw, baton, zapper with its projectile, renderers, render handler and packet; lightning rod movement behaviour; zinc
  materials; `ItemBoostUtils`; three packets; `SheepMixin`, `PlayerDrillMixin`) plus the P7 parts of the collections and
  glue; new `DrillBreakActionMixin`. `port_phase=7`, jar 0.6.2-ce-19.2-09 (59 blocks, 77 items, 4 entity types, 49 block
  entities, 61 recipes, 25 mixins), build clean; solver harness not rerun (no `sim` change). Create 0.5.1 counterparts used
  as they are: `BaseArmorItem`, `BacktankBlockItem`, Capacity, `PotatoCannonPacket` / `ShootableGadgetItemMethods` /
  `ShootableGadgetRenderHandler`, `TreeCutter`, `AllMovementBehaviours` (registered in `commonSetup` `enqueueWork`). B11
  fixed; B13, B14 found and fixed. Recipe gate unchanged (31 overrides). P8 notes: PORT_NOTES.md section 9.

- 2026-11-13: **P6 done.** About 45 classes moved (heater, basin heater, fan, pump, light fixture, factory light, bulbs,
  solar panel and bearing, contraption, six mixins) plus the P6 parts of the collections and glue; `port_phase=6`, jar
  0.6.2-ce-19.2-08 (58 blocks, 71 items, 48 block entities, 56 recipes, 22 mixins, 2 fan processing types, 1 contraption
  type), build clean; solver harness not rerun (no `sim` change). Fan types checked against the pack's Dragon Breathing
  hook (own row SQ-PWG-48); Farmer's Delight mixin targets checked on the pack's nested FD and against Create Central
  Kitchen / Creative Delight mixins. H5 wired into lights. 22 ponder scenes (58), structure check offline (19 files) and
  `pwg_p5_probe.js` extended to 55. B10 fixed, B11 found, B12 (missing `zap.json`: every client since `-04` crashed) fixed
  with a client-asset check in `pg_phase_check.py`. r1113 findings: SQ-PWG-32 and -37 are row faults (IE capacitor input
  side; AC sampling aliased with the period), rows -28, -30, -31, -32, -36, -37, -43, -45 fixed. E5 answered (no early
  stop). Recipe gate unchanged (31 overrides). P7 notes: PORT_NOTES.md section 9.

- 2026-11-13: **P5 done.** Ponder base on Create 0.5.1 `foundation.ponder`: `PowerGridSceneBuilder` rewritten as a wrapper with the Create 6 accessors, the wire elements, voltage source and network instructions ported, the level-restore hook as each scene's first instruction (0.5.1 has no plugin hook), registration from the client setup. 36 scenes (gauges 2, generator 7, wire 7, relays 13, devices 5, magnet 2) and 3 tags; scenes whose structure or code places a P6+ block wait for that phase (manifest: 8 structures moved to P6 / P10). Text keys and numbering match the shipped lang for all 36. The 36 shipped structures (1.20.1, DataVersion 3465, loaded without data fixing) checked offline: every block and property exists on 1.19.2 / Create 0.5.1; quirks the same on 1.20.1 (creative source `axis` in 6 files, numeric `axis_along_first`). `port_phase=5`, jar 0.6.2-ce-19.2-06 (same registry content as P4), build clean; gate copy sha1 81c35a983a386f4889fae202807e65a3d2a46ba9. Gate rows SQ-PWG-45, CQ-PWG-14; P3 run follow-ups: SQ-PWG-16 / -17 probe and rows (rerun). Recipe gate unchanged (31 overrides). P6 notes: PORT_NOTES.md section 9.

- 2026-11-13: **P4 done.** About 45 classes moved (`kinetics/base`, `motor`, `rheostat`, `variac`, `servo`, generator
  `housing`, `inductionrotor`, `clutch`, `rotor`, plus `SegmentedBehaviour`, `Directions` and the mixin
  `KineticBlockEntityAccessor`) and the P4 parts of the collections and glue; `port_phase=4`, jar 0.6.2-ce-19.2-05 (49
  blocks, 58 items, 40 block entities, 47 recipes, 16 mixins). P3 hand-off done: generator speeds in the stress
  provider, winding to housing connections, clutch redstone converter entry and display source, Carry On blacklist,
  `DebugItem` readouts, renderers drawing their own shafts. Generator coupling checked against Create 0.5.1 speed
  propagation (no change; PORT_NOTES.md 14); damage source `spinning_rotor` from the P2 enum. Upstream bug B9 (servo
  target lost without power, coil stuck at the moving resistance) fixed. PWG-06 and PWG-10 wording corrected
  (power_grid.md 3.5). Build clean; solver harness not rerun (no `sim` change). Recipe gate unchanged (31 overrides,
  owner decision pending). P2 to P4 complete D1's "phase 1". P5 notes: PORT_NOTES.md section 9.

- 2026-11-13: **P3 done.** About 105 classes moved (resistor, fuse, switches, contactor, redstone converter,
  transformers, battery, grounding, spark gap, carbon pile, electromagnet with the magnetization recipe, FE inverter,
  device connector, particles, winding, JEI magnetizing category) plus the P3 parts of the collections and glue;
  `port_phase=3`, jar 0.6.2-ce-19.2-04 (36 blocks, 45 items, 31 block entities, 22 recipes, 3 particle types, 15 mixins,
  `defaultRequire: 1`). FE bridge at upstream rates (code reads as FE per tick per watt: N6, measured by SQ-PWG-27). D4
  for the medium transformer and the winding. Moved forward: `ElectricKineticBlockEntity` and the stress provider (P4),
  `AirCurrentMixin` (P6). Upstream bugs B6 (grounding rod damage grew with distance) and B7 (magnetization config
  comment) fixed. Same jar fixes the P2 scratch run r1113 findings: B8 (NPE in `/powergrid performance get`) and four
  probe / row problems (SQ-PWG-15, -16, -17, -21; SQ-PWG-18 sets the drop chance explicitly); rerun SQ-PWG-13, -15, -16,
  -17, -21. Build clean; solver harness 75 passed, 6 known. Recipe gate unchanged (31 overrides, owner decision pending).
  P4 notes: PORT_NOTES.md section 9.

- 2026-11-13: **P2 done.** Grid base ported (about 200 classes moved from `upstream/java`, P2 parts of the collections,
  `PowerGrid` and the forge glue; datagen calls left out, the jar ships upstream's generated data); `port_phase=2`, jar
  0.6.2-ce-19.2-03 (13 blocks, 21 items, 3 entities, 11 block entities, 2 menus, 11 recipes, 5 wire types, 11 mixins with
  refmap, `defaultRequire: 1`). New code: Overheated Component (D4; every overheat path through `ThermalBehaviour.explode`
  / `overheatBlock`; config `overheatedDropChance` 0.25), wire types reload listener plus sync packet, 1.19.2 damage
  sources with upstream message ids, a `GuiGraphics` stand-in for the screens. H4: burnt wires already vanish with no
  drop upstream. H5: `clientMirror` off stops the per-tick state stream (clients see the 100-tick full sync). H9 recorded
  in PORT_NOTES.md 12. No new upstream bug. Build clean; solver harness still 75 passed, 6 known. Gate rows SQ-PWG-12 to
  -23, CQ-PWG-05 to -09; perf rows SQ-PWG-01 to -05, -08, CQ-PWG-01 to -04 refreshed; probe
  `tools/scratch_query/pwg_grid_probe.js`. Recipe gate unchanged (31 overrides, owner decision pending).
- 2026-11-13: **P1 done.** `electricity/sim` ported (63 files) and builds clean; `port_phase=1`, jar 0.6.2-ce-19.2-02
  (registers nothing, like P0). Harness `tools/test_power_grid_solver.sh` (javac/java, no game): the five circuits pass
  on upstream's test network and on the game's network type (divider exact, RC 1.43 % off at one time constant,
  transformer 1:10 exact, diode drop 0.62 V, floating grid finite), plus upstream's solver tests through a JUnit
  stand-in (67 of 73 pass, 6 known test-side failures listed in `tools/pg_solver_test/known_failures.txt`). Timings:
  1000-node ladder 9 ms first solve, 0.11 ms per solve after a change, 0.015 ms unchanged; upstream's 32 x 32 mesh 3 ms.
  Upstream bug **B5** fixed (wires to ground ignored after the first solve; power_grid.md 3.5). Deviations: the
  `TransmissionLine` class and the debug command are behind an interface / a hook so the core has no game types;
  `circuits/thermal` and six game-side `sim` classes (`DebugItem`, `OwnedElectricWire`, `OwnedFloatingNode`,
  `SamplingWire`, `TransmissionLine`, `TransmissionLinePart`) move with P2. Live check SQ-PWG-11 queued (runs the solver
  in the game's class loader, EJML from jar-in-jar). P2 notes: PORT_NOTES.md section 9.
- 2026-11-13: **P0 done.** Build clean (`./gradlew :backportedPowerGrid:build --offline`); gate row SQ-PWG-09 queued.
  Deviations: MixinExtras not nested (the pack loads 0.5.4; compileOnly as Brewin'); upstream code not yet ported lives in
  `upstream/java` (not compiled) and moves to `src/main/java` per phase; one mixin config instead of upstream's three;
  recipe JSON fixes applied to all recipes at once (`pg_phase_check.py --fix-recipes`), and the punch card's cardboard
  byproduct dropped. The recipe gate found 31 recipes whose ingredients come later than their output (mostly
  `conductive_casing` P4 and `pins` P9 in P2/P3 recipes); they are accepted as overrides in `phases.json`, so those
  outputs are uncraftable until the later phase. Open question for the owner: move plain crafting materials earlier
  instead. Details: `development/backportedPowerGrid/PORT_NOTES.md`.
- 2026-11-13: E1 to E4 answered by the owner as suggested; E5 (stop point) stays open.

- 2026-11-13: plan written (phases P0 to P11, phase manifest and recipe gate, per-phase content from the decompile's
  lang, packages and mixins). Nothing built.
