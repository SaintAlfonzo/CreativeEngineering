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
// Create 6 PonderSceneRegistrationHelper -> Create 0.5.1 PonderRegistrationHelper (same storyboard calls; tags are
// PonderTag objects instead of ids). Port state: complete, every upstream scene (81): P2 to P4 content (36 scenes, P5),
// P6 content plus the P5 list's scenes that waited for P6 blocks (22), P8 (plotter, modular display, bell, crt, punch
// cards), P9a (every circuit/* scene and encased_fan), P10 (wire/string_lights, ceiling_tile). A scene is registered
// only when every block in its structure and every block or item its code places is ported; its structure file ships
// from the same phase (phases.json `resources`).
package org.patryk3211.powergrid.ponder;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry.PonderStoryBoard;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.ponder.scenes.*;

public class PowerGridPonderScenes {
    /** Every Power Grid storyboard starts with the upstream level-restore hook (PowerGridPonderPlugin). */
    static PonderStoryBoard wrap(PonderStoryBoard storyBoard) {
        return (scene, util) -> {
            scene.addInstruction(PowerGridPonderPlugin::onPonderLevelRestore);
            storyBoard.program(scene, util);
        };
    }

    public static void register() {
        PonderRegistrationHelper HELPER = new PonderRegistrationHelper(PowerGrid.MOD_ID);

        HELPER.addStoryBoard(ModdedBlocks.VOLTAGE_METER, "gauges", wrap(GaugeScenes::voltage));
        HELPER.addStoryBoard(ModdedBlocks.CURRENT_METER, "gauges", wrap(GaugeScenes::current));
        HELPER.addStoryBoard(ModdedBlocks.POWER_METER, "power_gauge", wrap(GaugeScenes::power));
        HELPER.addStoryBoard(ModdedBlocks.ENERGY_METER, "energy_meter", wrap(GaugeScenes::energyMeter));

        // Phase P6
        HELPER.forComponents(ModdedBlocks.HEATING_COIL)
                .addStoryBoard("heating_coil/basic", wrap(DeviceScenes::heatingCoilBasic), PowerGridPonderTags.ELECTRIC_DEVICES)
                .addStoryBoard("heating_coil/speed", wrap(DeviceScenes::heatingCoilSpeed), PowerGridPonderTags.ELECTRIC_DEVICES);

        HELPER.forComponents(ModdedBlocks.WINDING, ModdedItems.COPPER_COIL)
                .addStoryBoard("generator/winding", wrap(GeneratorScenes::winding), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/parallel_windings", wrap(GeneratorScenes::parallelWinding), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_1", wrap(GeneratorScenes::generator1), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_2", wrap(GeneratorScenes::generator2), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_3", wrap(GeneratorScenes::generator3), PowerGridPonderTags.GENERATOR_ASSEMBLY);

        HELPER.forComponents(ModdedBlocks.GENERATOR_CLUTCH)
                .addStoryBoard("generator/clutch", wrap(GeneratorScenes::clutch), PowerGridPonderTags.GENERATOR_ASSEMBLY, AllPonderTags.KINETIC_APPLIANCES)
                .addStoryBoard("generator/generator_1", wrap(GeneratorScenes::generator1), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_2", wrap(GeneratorScenes::generator2), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_3", wrap(GeneratorScenes::generator3), PowerGridPonderTags.GENERATOR_ASSEMBLY);

        HELPER.forComponents(ModdedBlocks.GENERATOR_HOUSING, ModdedBlocks.VERTICAL_GENERATOR_HOUSING)
                .addStoryBoard("generator/housing", wrap(GeneratorScenes::housing), PowerGridPonderTags.GENERATOR_ASSEMBLY);

        HELPER.forComponents(ModdedBlocks.GENERATOR_INDUCTION_ROTOR, ModdedBlocks.GENERATOR_COMMUTATOR, ModdedBlocks.GENERATOR_VERTICAL_COMMUTATOR)
                .addStoryBoard("generator/rotor", wrap(GeneratorScenes::rotor), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_1", wrap(GeneratorScenes::generator1), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_2", wrap(GeneratorScenes::generator2), PowerGridPonderTags.GENERATOR_ASSEMBLY)
                .addStoryBoard("generator/generator_3", wrap(GeneratorScenes::generator3), PowerGridPonderTags.GENERATOR_ASSEMBLY);

        // Phase P10
        HELPER.addStoryBoard(ModdedItems.STRING_LIGHT_CORD, "wire/string_lights", wrap(WireScenes::stringLights));
        HELPER.addStoryBoard(ModdedItems.INSULATED_COPPER_WIRE, "wire/insulated_wire", wrap(WireScenes::insulatedWire));
        HELPER.forComponents(ModdedItems.WIRE, ModdedItems.IRON_WIRE, ModdedItems.GOLDEN_WIRE, ModdedItems.INSULATED_COPPER_WIRE)
                .addStoryBoard("wire/simple", wrap(WireScenes::simple))
                .addStoryBoard("wire/wire_loop", wrap(WireScenes::wireLoop))
                .addStoryBoard("wire/voltage_drop", wrap(WireScenes::voltageDrop));
        HELPER.forComponents(ModdedItems.CORD, ModdedItems.STRING_LIGHT_CORD)
                .addStoryBoard("wire/cord", wrap(WireScenes::cord));

        HELPER.forComponents(ModdedBlocks.GROUNDING_ROD)
                .addStoryBoard("ground1", wrap(WireScenes::grounding), PowerGridPonderTags.ELECTRIC_RELAYS)
                .addStoryBoard("ground2", wrap(WireScenes::improvedGround), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.forComponents(ModdedBlocks.TRANSFORMER_CORE)
                .addStoryBoard("transformer/sizes", wrap(RelayScenes::transformerSizes), PowerGridPonderTags.ELECTRIC_RELAYS)
                .addStoryBoard("transformer/winding", wrap(RelayScenes::transformerWinding), PowerGridPonderTags.ELECTRIC_RELAYS)
                .addStoryBoard("transformer/nether", wrap(RelayScenes::transformerNether), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.VARIAC, "variac", wrap(RelayScenes::variac), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.addStoryBoard(ModdedBlocks.RHEOSTAT, "rheostat", wrap(RelayScenes::rheostat), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.RESISTOR, "power_resistor", wrap(RelayScenes::powerResistor), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.forComponents(ModdedBlocks.CARBON_PILE_COIL)
                .addStoryBoard("carbon_pile", wrap(RelayScenes::carbonPile), PowerGridPonderTags.ELECTRIC_RELAYS)
                .addStoryBoard("generator/generator_3", wrap(GeneratorScenes::generator3), PowerGridPonderTags.GENERATOR_ASSEMBLY);

        HELPER.forComponents(ModdedBlocks.LIGHT_FIXTURE, ModdedItems.LIGHT_BULB, ModdedItems.LV_LIGHT_BULB)
                .addStoryBoard("lightbulb", wrap(DeviceScenes::light), PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.addStoryBoard(ModdedItems.GROWTH_LAMP, "growth_lamp", wrap(DeviceScenes::growthLamp), PowerGridPonderTags.ELECTRIC_DEVICES);

        HELPER.forComponents(ModdedBlocks.WIRE_CONNECTOR, ModdedBlocks.HEAVY_WIRE_CONNECTOR)
                .addStoryBoard("wire/connector", wrap(WireScenes::connector), PowerGridPonderTags.ELECTRIC_RELAYS, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS);
        HELPER.addStoryBoard(ModdedBlocks.DEVICE_CONNECTOR, "device_connector", wrap(RelayScenes::deviceConnector), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.addStoryBoard(ModdedBlocks.ELECTRIC_MOTOR, "motor", wrap(DeviceScenes::motor), AllPonderTags.KINETIC_SOURCES, PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.addStoryBoard(ModdedBlocks.CONSTANT_SPEED_MOTOR, "constant_speed_motor", wrap(DeviceScenes::constantSpeedMotor), AllPonderTags.KINETIC_SOURCES, PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.addStoryBoard(ModdedBlocks.SERVO, "servo", wrap(DeviceScenes::servo), AllPonderTags.KINETIC_SOURCES, PowerGridPonderTags.ELECTRIC_DEVICES);

        // Phase P6 (CEILING_TILE_ATTACHMENTS on the solar panel and factory light scenes since P10, as upstream)
        HELPER.addStoryBoard(ModdedBlocks.ELECTRIC_FAN, "electric_fan", wrap(DeviceScenes::electricFan), PowerGridPonderTags.ELECTRIC_DEVICES);
        // encased_fan (on Create's encased fan) since P9a: its structure and text show a circuit board
        HELPER.addStoryBoard(com.simibubi.create.AllBlocks.ENCASED_FAN, "encased_fan", wrap(DeviceScenes::encasedFan));
        HELPER.forComponents(ModdedBlocks.SOLAR_PANEL)
                .addStoryBoard("solar_panel/solar_panel_conditions", wrap(DeviceScenes::solarPanel), PowerGridPonderTags.ELECTRIC_DEVICES, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS)
                .addStoryBoard("solar_panel/solar_panel_power_creation", wrap(DeviceScenes::solarPanel2), PowerGridPonderTags.ELECTRIC_DEVICES, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS);
        HELPER.addStoryBoard(ModdedBlocks.SOLAR_PANEL_BEARING, "solar_panel_bearing", wrap(DeviceScenes::solarPanelBearing), PowerGridPonderTags.ELECTRIC_DEVICES, AllPonderTags.KINETIC_SOURCES);
        HELPER.addStoryBoard(ModdedBlocks.BASIN_HEATER, "basin_heater", wrap(DeviceScenes::basinHeater), PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.forComponents(ModdedBlocks.FACTORY_LIGHT)
                .addStoryBoard("factory_light/factory_light", wrap(DeviceScenes::factoryLight), PowerGridPonderTags.ELECTRIC_DEVICES, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS)
                .addStoryBoard("factory_light/factory_light_tall", wrap(DeviceScenes::factoryLightTall), PowerGridPonderTags.ELECTRIC_DEVICES, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS)
                .addStoryBoard("factory_light/factory_light_connect", wrap(DeviceScenes::factoryLightConnect), PowerGridPonderTags.ELECTRIC_DEVICES, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS);

        // Phase P10 (upstream registers it after the modular display, between the P6 scenes)
        HELPER.addStoryBoard(ModdedBlocks.CEILING_TILE, "ceiling_tile", wrap(DeviceScenes::ceilingTile), PowerGridPonderTags.CEILING_TILE_ATTACHMENTS);

        HELPER.forComponents(ModdedItems.MAGNET)
                .addStoryBoard("magnet", wrap(MagnetScenes::magnet))
                .addStoryBoard("lightning_attractor", wrap(MagnetScenes::lightningAttractor));
        HELPER.addStoryBoard(new ResourceLocation("lightning_rod"), "lightning_attractor", wrap(MagnetScenes::lightningAttractor));

        HELPER.addStoryBoard(ModdedBlocks.LV_SWITCH, "switch", wrap(RelayScenes.switchSceneFor(ModdedBlocks.LV_SWITCH, "lv_switch")), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.LV_BUTTON, "switch", wrap(RelayScenes.switchSceneFor(ModdedBlocks.LV_BUTTON, "lv_button")), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.MV_SWITCH, "switch", wrap(RelayScenes.switchSceneFor(ModdedBlocks.MV_SWITCH, "mv_switch")), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.HV_SWITCH, "hv_switch", wrap(RelayScenes::hvSwitch), PowerGridPonderTags.ELECTRIC_RELAYS);
        HELPER.addStoryBoard(ModdedBlocks.HV_BREAKER, "hv_breaker", wrap(RelayScenes::hvBreaker), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.addStoryBoard(ModdedBlocks.REDSTONE_CONVERTER, "redstone_converter", wrap(RelayScenes::redstoneConverter));

        HELPER.forComponents(ModdedBlocks.CONTACTOR)
                .addStoryBoard("contactor", wrap(RelayScenes::contactor), PowerGridPonderTags.ELECTRIC_RELAYS)
                .addStoryBoard("contactor_stack", wrap(RelayScenes::contactorStack), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.addStoryBoard(ModdedBlocks.FUSE_HOLDER, "fuse", wrap(RelayScenes::fuse), PowerGridPonderTags.ELECTRIC_RELAYS);

        HELPER.addStoryBoard(ModdedBlocks.ELECTROMAGNET, "electromagnet", wrap(DeviceScenes::electromagnet), PowerGridPonderTags.ELECTRIC_DEVICES);

        HELPER.addStoryBoard(ModdedBlocks.BATTERY, "battery", wrap(DeviceScenes::battery));

        HELPER.addStoryBoard(ModdedBlocks.CORD_JUNCTION, "wire/cord_junction", wrap(WireScenes::cordJunction), PowerGridPonderTags.ELECTRIC_RELAYS, PowerGridPonderTags.CEILING_TILE_ATTACHMENTS);
        HELPER.addStoryBoard(ModdedBlocks.SOCKET, "wire/cord_socket", wrap(WireScenes::cordSocket), PowerGridPonderTags.ELECTRIC_RELAYS);

        // Phase P8 (upstream registers the plotter first and the others between the P2 to P6 scenes; the order within
        // a component is what counts, and each of these components has one scene)
        HELPER.addStoryBoard(ModdedBlocks.PLOTTER, "plotter", wrap(DeviceScenes::plotter));
        HELPER.addStoryBoard(ModdedBlocks.MODULAR_DISPLAY, "modular_display", wrap(DeviceScenes::modularDisplay), PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.addStoryBoard(ModdedBlocks.ALARM_BELL, "bell", wrap(DeviceScenes::bell), PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.addStoryBoard(ModdedBlocks.CRT, "crt", wrap(DeviceScenes::crt), PowerGridPonderTags.ELECTRIC_DEVICES);
        HELPER.forComponents(ModdedBlocks.PUNCH_CARD_READER, ModdedItems.PUNCH_CARD)
                .addStoryBoard("punch_cards", wrap(DeviceScenes::punchCardReader), PowerGridPonderTags.ELECTRIC_DEVICES, AllPonderTags.KINETIC_APPLIANCES);

        // Phase P9a: circuit board scenes (upstream order)
        HELPER.addStoryBoard(ModdedItems.RESISTOR, "circuit/resistor", wrap(CircuitScenes::resistor), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedBlocks.VOLTAGE_METER, "circuit/voltage", wrap(CircuitScenes::voltageGauge), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedBlocks.CURRENT_METER, "circuit/current", wrap(CircuitScenes::currentGauge), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.CAPACITOR, "circuit/capacitor", wrap(CircuitScenes::capacitor), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.forComponents(ModdedItems.RELAY, ModdedItems.RELAY_DPDT)
                .addStoryBoard("circuit/relay", wrap(CircuitScenes::relay), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.TRIODE, "circuit/triode", wrap(CircuitScenes::triode), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.PENTODE, "circuit/pentode", wrap(CircuitScenes::pentode), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.THYRATRON, "circuit/thyratron", wrap(CircuitScenes::thyratron), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.REDSTONE_RELAY, "circuit/redstone_relay", wrap(CircuitScenes::redstoneRelay), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(com.simibubi.create.AllItems.COPPER_NUGGET, "circuit/via", wrap(CircuitScenes::via), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.POTENTIOMETER, "circuit/potentiometer", wrap(CircuitScenes::potentiometer), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.REGULATOR_TUBE, "circuit/regulator_tube", wrap(CircuitScenes::regulatorTube), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.NEON_BULB, "circuit/neon", wrap(CircuitScenes::neonBulb), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(new ResourceLocation("paper"), "circuit/label", wrap(CircuitScenes::label), PowerGridPonderTags.CIRCUIT_COMPONENTS);
        HELPER.addStoryBoard(ModdedItems.DISPLAY_MODULE, "circuit/display_module", wrap(CircuitScenes::displayModule), PowerGridPonderTags.CIRCUIT_COMPONENTS);
    }
}
