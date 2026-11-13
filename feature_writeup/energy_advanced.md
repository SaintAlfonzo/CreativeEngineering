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

## 8. History

- 2026-11-12: written from the owner's answers to power_grid.md H1 to H7; tier mapping investigated (section 3).
