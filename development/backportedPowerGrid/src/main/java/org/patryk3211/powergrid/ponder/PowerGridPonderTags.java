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
// Create 6 PonderTagRegistrationHelper -> Create 0.5.1 PonderTag objects and PonderRegistry.TAGS.forTag(...).add(...).
// Titles and descriptions come from the lang file (powergrid.ponder.tag.*); defaultLang keeps upstream's English for
// Create's ponder editing mode. Port state: complete (P5 tags, members of P6 and P8 added, CIRCUIT_COMPONENTS since P9a
// (icon circuit board), CEILING_TILE_ATTACHMENTS since P10 (icon ceiling tile)).
package org.patryk3211.powergrid.ponder;

import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;

public class PowerGridPonderTags {
    public static final PonderTag
            GENERATOR_ASSEMBLY = new PonderTag(PowerGrid.asResource("generator_assembly")),
            ELECTRIC_RELAYS = new PonderTag(PowerGrid.asResource("electric_relays")),
            ELECTRIC_DEVICES = new PonderTag(PowerGrid.asResource("electric_devices")),
            CIRCUIT_COMPONENTS = new PonderTag(PowerGrid.asResource("circuit_components")), // P9a
            CEILING_TILE_ATTACHMENTS = new PonderTag(PowerGrid.asResource("ceiling_tile_attachments")); // P10

    public static void register() {
        GENERATOR_ASSEMBLY
                .defaultLang("Generator Parts", "Components which can be used to build a generator")
                .item(ModdedBlocks.GENERATOR_INDUCTION_ROTOR.get(), true, false)
                .addToIndex();

        ELECTRIC_RELAYS
                .defaultLang("Electricity Relays", "Components which help guide electricity in the right direction")
                .item(ModdedBlocks.MV_SWITCH.get(), true, false)
                .addToIndex();

        ELECTRIC_DEVICES
                .defaultLang("Electric Devices", "Components which use electricity to do something")
                .item(ModdedBlocks.ELECTRIC_MOTOR.get(), true, false)
                .addToIndex();

        // Phase P9a
        CIRCUIT_COMPONENTS
                .defaultLang("Circuit Components", "Components which can be placed on a circuit")
                .item(ModdedBlocks.CIRCUIT_BOARD.get(), true, true)
                .addToIndex();

        // Phase P10
        CEILING_TILE_ATTACHMENTS
                .defaultLang("Ceiling tile attachments", "Blocks which can be placed on the ceiling tile")
                .item(ModdedBlocks.CEILING_TILE.get(), true, true)
                .addToIndex();

        PonderRegistry.TAGS.forTag(GENERATOR_ASSEMBLY)
                .add(ModdedBlocks.GENERATOR_CLUTCH)
                .add(ModdedBlocks.GENERATOR_INDUCTION_ROTOR)
                .add(ModdedBlocks.GENERATOR_COMMUTATOR)
                .add(ModdedBlocks.GENERATOR_VERTICAL_COMMUTATOR)
                .add(ModdedItems.COPPER_COIL)
                .add(ModdedBlocks.GENERATOR_HOUSING)
                .add(ModdedBlocks.VERTICAL_GENERATOR_HOUSING);

        PonderRegistry.TAGS.forTag(ELECTRIC_RELAYS)
                .add(ModdedBlocks.LV_SWITCH)
                .add(ModdedBlocks.LV_BUTTON)
                .add(ModdedBlocks.MV_SWITCH)
                .add(ModdedBlocks.HV_SWITCH)
                .add(ModdedBlocks.HV_BREAKER)
                .add(ModdedBlocks.CONTACTOR)
                .add(ModdedBlocks.SPARK_GAP)
                .add(ModdedBlocks.FUSE_HOLDER)
                .add(ModdedBlocks.TRANSFORMER_CORE)
                .add(ModdedBlocks.VARIAC)
                .add(ModdedBlocks.RHEOSTAT)
                .add(ModdedBlocks.RESISTOR)
                .add(ModdedBlocks.CARBON_PILE_COIL)
                .add(ModdedBlocks.WIRE_CONNECTOR)
                .add(ModdedBlocks.HEAVY_WIRE_CONNECTOR)
                .add(ModdedBlocks.DEVICE_CONNECTOR)
                .add(ModdedBlocks.CORD_JUNCTION)
                .add(ModdedBlocks.SOCKET)
                .add(ModdedBlocks.GROUNDING_ROD);

        PonderRegistry.TAGS.forTag(ELECTRIC_DEVICES)
                .add(ModdedBlocks.ELECTRIC_MOTOR)
                .add(ModdedBlocks.CONSTANT_SPEED_MOTOR)
                .add(ModdedBlocks.SERVO)
                .add(ModdedBlocks.HEATING_COIL)
                .add(ModdedBlocks.ELECTRIC_FAN)
                .add(ModdedBlocks.BASIN_HEATER)
                .add(ModdedItems.LV_LIGHT_BULB)
                .add(ModdedItems.LIGHT_BULB)
                .add(ModdedItems.GROWTH_LAMP)
                .add(ModdedBlocks.PLOTTER)
                .add(ModdedBlocks.CRT)
                .add(ModdedBlocks.PUNCH_CARD_READER)
                .add(ModdedBlocks.SOLAR_PANEL)
                .add(ModdedBlocks.SOLAR_PANEL_BEARING)
                .add(ModdedBlocks.MODULAR_DISPLAY);

        PonderRegistry.TAGS.forTag(AllPonderTags.KINETIC_APPLIANCES)
                .add(ModdedBlocks.GENERATOR_CLUTCH)
                .add(ModdedBlocks.PLOTTER)
                .add(ModdedBlocks.PUNCH_CARD_READER);

        PonderRegistry.TAGS.forTag(AllPonderTags.KINETIC_SOURCES)
                .add(ModdedBlocks.ELECTRIC_MOTOR)
                .add(ModdedBlocks.CONSTANT_SPEED_MOTOR)
                .add(ModdedBlocks.SERVO);

        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES)
                .add(ModdedBlocks.VOLTAGE_METER)
                .add(ModdedBlocks.CURRENT_METER)
                .add(ModdedBlocks.POWER_METER)
                .add(ModdedBlocks.GENERATOR_CLUTCH)
                .add(ModdedBlocks.BATTERY);

        // Phase P9a: upstream's members and order
        PonderRegistry.TAGS.forTag(CIRCUIT_COMPONENTS)
                .add(ModdedBlocks.WIRE_CONNECTOR)
                .add(com.simibubi.create.AllItems.COPPER_NUGGET)
                .add(ModdedItems.RESISTOR)
                .add(ModdedItems.DIODE)
                .add(ModdedItems.CAPACITOR)
                .add(ModdedItems.COPPER_COIL)
                .add(ModdedItems.RELAY)
                .add(ModdedItems.RELAY_DPDT)
                .add(ModdedItems.REDSTONE_RELAY)
                .add(ModdedItems.TRIODE)
                .add(ModdedItems.PENTODE)
                .add(ModdedItems.THYRATRON)
                .add(ModdedItems.REGULATOR_TUBE)
                .add(ModdedItems.BARRETTER_TUBE)
                .add(ModdedItems.NEON_BULB)
                .add(ModdedBlocks.LV_SWITCH)
                .add(ModdedBlocks.LV_BUTTON)
                .add(ModdedItems.LV_LIGHT_BULB)
                .add(ModdedItems.POTENTIOMETER)
                .add(ModdedBlocks.VOLTAGE_METER)
                .add(ModdedBlocks.CURRENT_METER)
                .add(ModdedItems.VARISTOR)
                .add(ModdedItems.VFET)
                .add(ModdedItems.BJT_NPN)
                .add(ModdedItems.BJT_PNP)
                .add(ModdedItems.DISPLAY_MODULE);

        // Phase P10: upstream's members and order
        PonderRegistry.TAGS.forTag(CEILING_TILE_ATTACHMENTS)
                .add(ModdedBlocks.CEILING_TILE)
                .add(ModdedBlocks.FACTORY_LIGHT)
                .add(ModdedBlocks.WIRE_CONNECTOR)
                .add(ModdedBlocks.CORD_JUNCTION)
                .add(ModdedBlocks.SOLAR_PANEL);
    }
}
