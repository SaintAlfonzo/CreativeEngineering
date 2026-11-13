/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port state: phases P2 to P10, complete (Flywheel 0.6 PartialModel).
package org.patryk3211.powergrid.collections;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.jozufozu.flywheel.core.PartialModel;
import org.patryk3211.powergrid.PowerGrid;

public class ModdedPartialModels {

    public static final PartialModel CONDUCTIVE_VOLTAGE_HEAD = block("gauge/conductive/voltage_head");
    public static final PartialModel CONDUCTIVE_CURRENT_HEAD = block("gauge/conductive/current_head");
    public static final PartialModel CONDUCTIVE_POWER_HEAD = block("gauge/conductive/power_head");

    public static final PartialModel THERMOMETER_NEEDLE = block("thermometer/needle");
    public static final PartialModel THERMOMETER_NEEDLE_RED = block("thermometer/needle_red");

    public static final PartialModel ENERGY_METER_NEEDLE = block("energy_meter/dial");

    public static final PartialModel MULTIMETER_NEEDLE = model("item/multimeter/indicator");

    public static final PartialModel PLUG = block("plug");

    // Phase P3
    public static final PartialModel HV_SWITCH_ROD = block("switches/hv_switch_rod");
    public static final PartialModel SPARK_GAP_ARM = block("spark_gap/arm");
    public static final PartialModel HV_BREAKER_SIGNAL1 = block("switches/hv_breaker_signal1");
    public static final PartialModel HV_BREAKER_SIGNAL2 = block("switches/hv_breaker_signal2");

    // Phase P4
    public static final PartialModel SHAFT_BIT = block("shaft_bit");
    public static final PartialModel COMMUTATOR_SHAFT = block("generator/commutator_shaft");
    public static final PartialModel COMMUTATOR_BRUSH = block("generator/commutator_brush");
    public static final PartialModel VERTICAL_COMMUTATOR_BRUSH = block("generator/vertical_commutator_brush");
    public static final PartialModel CLUTCH_SHAFT = block("generator/clutch_shaft");
    public static final PartialModel VARIAC_ARMATURE = block("variac/armature");
    public static final PartialModel INDUCTION_ROTOR = block("generator/induction_rotor");

    // Phase P6
    public static final PartialModel FAN_PROPELLER = block("electric_fan/propeller");
    public static final PartialModel FL_RAYS_CENTER = block("factory_light/godrayscenter");
    public static final PartialModel FL_RAYS_BACK = block("factory_light/godraysedgeback");
    public static final PartialModel FL_RAYS_FRONT = block("factory_light/godraysedgefront");
    public static final PartialModel FL_RAYS_SINGLE = block("factory_light/godrayssingular");
    public static final PartialModel SOLAR_PANEL_BEARING_ROTOR = block("solar_panel/solar_panel_bearing_rotor");

    // Phase P7 (upstream keeps these two in DrillItemRenderer and ElectroZapperItemRenderer; here they register with the
    // others in initClient, before Flywheel 0.6 bakes partial models)
    public static final PartialModel DRILL_HEAD = model("item/drill/head");
    public static final PartialModel ELECTROZAPPER_COG = model("item/electrozapper/cog");

    // Phase P8
    public static final PartialModel PLOTTER_POINTER = block("plotter/pointer");
    public static final PartialModel PLOTTER_PAPER = block("plotter/paper");
    public static final PartialModel PLOTTER_VIEWER = block("plotter/viewer");
    public static final PartialModel CRT_BACKGROUND = block("crt_background");
    public static final PartialModel PUNCH_CARD = block("punch_card_reader/card");

    // Phase P8 (catnip SpriteShifter -> Create 0.5.1 foundation.block.render.SpriteShifter)
    public static final com.simibubi.create.foundation.block.render.SpriteShiftEntry PAPER_SHIFT = com.simibubi.create.foundation.block.render.SpriteShifter.get(
            PowerGrid.asResource("block/plotter_paper"),
            PowerGrid.asResource("block/plotter_paper_shift"));

    public static final CTSpriteShiftEntry CONDUCTIVE_CASING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            PowerGrid.asResource("block/conductive_casing"),
            PowerGrid.asResource("block/conductive_casing_connected"));

    // Phase P9a: circuit board component parts (rendered by IRenderedComponent through CircuitBoardRenderer)
    public static final PartialModel ELECTRON_TUBE_GLOW = model("component/triode_glow");
    public static final PartialModel PENTODE_GLOW = model("component/pentode_glow");
    public static final PartialModel THYRATRON_GLOW = model("component/thyratron_glow");
    public static final PartialModel REGULATOR_TUBE_GLOW = model("component/regulator_tube_glow");
    public static final PartialModel POTENTIOMETER_KNOB = model("component/potentiometer_knob");

    public static final PartialModel NEON_TUBE_BULB = model("component/neon_bulb_bulb");
    public static final PartialModel NEON_TUBE_GLOW = model("component/neon_bulb_glow");
    public static final PartialModel NEON_TUBE_VERTICAL_GLOW = model("component/neon_bulb_vertical_glow");

    public static final PartialModel LABEL = model("component/label");

    // Phase P9b
    public static final PartialModel CIRCUIT_TABLE_GLOW = block("circuit_design_table_glow");

    // Phase P10
    public static final CTSpriteShiftEntry COPPER_PLATING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            PowerGrid.asResource("block/copper_plating"),
            PowerGrid.asResource("block/copper_plating_connected"));
    public static final PartialModel CEILING_LIGHT = block("ceiling_tile/ceiling_tile_light_light");

    public static final PartialModel LIGHT_BULB_BULB = model("component/light_bulb_bulb");
    public static final PartialModel LIGHT_BULB_GLOW = model("component/light_bulb_glow");
    public static final PartialModel LIGHT_BULB_BULB_DYED = model("component/light_bulb_bulb_dyed");
    public static final PartialModel LIGHT_BULB_GLOW_DYED = model("component/light_bulb_glow_dyed");

    public static final PartialModel BARRETTER_GLOW = model("component/barretter_tube_glow");

    public static final PartialModel COMPONENT_GAUGE_NEEDLE = model("component/gauge_needle");

    private static PartialModel block(String path) {
        return new PartialModel(PowerGrid.asResource("block/" + path));
    }

    private static PartialModel model(String path) {
        return new PartialModel(PowerGrid.asResource(path));
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }
}
