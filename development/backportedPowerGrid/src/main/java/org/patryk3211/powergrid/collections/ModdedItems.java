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
// Port state: phases P2 (wires, cord, cutters, multimeter, gizmo, debug) and P3 (coils, magnet, zinc sheet, incomplete
// battery and transformer core), P6 (light bulbs, neon bulb, incomplete solar panel), P7 (equipment), P9a / P9b (components, schematic), P9c
// (etching chain items), P9d (Integrated Circuit, incomplete electrical gizmo) and P10 (string light cord): complete.
// Data-generation calls (tag, lang, model, wire_types provider) are left
// out: the jar ships upstream's generated output, and wire_types are read by WireRegistry's reload listener.
package org.patryk3211.powergrid.collections;

import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.light.bulb.GrowthLamp;
import org.patryk3211.powergrid.electricity.light.bulb.LightBulb;
import org.patryk3211.powergrid.electricity.light.bulb.LvLightBulb;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.Item;
import org.patryk3211.powergrid.PortPhase;
import org.patryk3211.powergrid.electricity.sim.DebugItem;
import org.patryk3211.powergrid.electricity.wire.WireItem;
import org.patryk3211.powergrid.electricity.wire.powercord.CordItem;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItem;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItemRenderer;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingItem;

import com.simibubi.create.content.equipment.armor.BacktankItem;
import org.patryk3211.powergrid.equipment.BoostingChipItem;
import org.patryk3211.powergrid.equipment.BoostingChipRenderer;
import org.patryk3211.powergrid.equipment.ZincArmorMaterial;
import org.patryk3211.powergrid.equipment.baton.ElectroBatonItem;
import org.patryk3211.powergrid.equipment.drill.DrillItem;
import org.patryk3211.powergrid.equipment.drill.DrillItemRenderer;
import org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryItem;
import org.patryk3211.powergrid.equipment.saw.SawItem;
import org.patryk3211.powergrid.equipment.saw.SawItemRenderer;
import org.patryk3211.powergrid.equipment.zapper.ElectroZapperItem;
import org.patryk3211.powergrid.equipment.zapper.ElectroZapperItemRenderer;

import java.util.function.Supplier;

import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedItems {
    private static String id(String name) {
        return PortPhase.require("item", name);
    }

    public static final ItemEntry<WireItem> WIRE = REGISTRATE.item(id("wire"), WireItem::new).register();
    public static final ItemEntry<WireItem> IRON_WIRE = REGISTRATE.item(id("iron_wire"), WireItem::new).register();
    public static final ItemEntry<WireItem> GOLDEN_WIRE = REGISTRATE.item(id("golden_wire"), WireItem::new).register();
    public static final ItemEntry<WireItem> INSULATED_COPPER_WIRE = REGISTRATE.item(id("insulated_copper_wire"), WireItem::new).register();
    public static final ItemEntry<CordItem> CORD = REGISTRATE.item(id("copper_cord"), CordItem::new).register();
    // Phase P10, kept in upstream's place (after the copper cord) for the creative tab order. Its wire_types file ships.
    public static final ItemEntry<org.patryk3211.powergrid.electricity.light.string.StringLightCordItem> STRING_LIGHT_CORD = REGISTRATE.item(id("string_light_cord"), org.patryk3211.powergrid.electricity.light.string.StringLightCordItem::new).register();

    public static final ItemEntry<Item> WIRE_CUTTER = REGISTRATE.item(id("wire_cutter"), Item::new).register();

    public static final ItemEntry<DebugItem> DEBUG_ITEM = REGISTRATE.item(id("debug"), DebugItem::new).register();

    public static final ItemEntry<Item> ELECTRICAL_GIZMO = REGISTRATE.item(id("electrical_gizmo"), Item::new).register();

    public static final ItemEntry<MultimeterItem> MULTIMETER = REGISTRATE.item(id("multimeter"), MultimeterItem::new)
            .transform(customRenderer(() -> MultimeterItemRenderer::new))
            .register();

    // Phase P3 (tags are in the shipped item tag files, as for P2)
    public static final ItemEntry<Item> RESISTIVE_COIL = REGISTRATE.item(id("resistive_coil"), Item::new).register();
    public static final ItemEntry<WindingItem> COPPER_COIL = REGISTRATE.item(id("copper_coil"), WindingItem::new).register();
    public static final ItemEntry<Item> MAGNET = REGISTRATE.item(id("magnet"), Item::new).register();
    public static final ItemEntry<Item> ZINC_SHEET = REGISTRATE.item(id("zinc_sheet"), Item::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_TRANSFORMER_CORE = REGISTRATE.item(id("incomplete_transformer_core"), SequencedAssemblyItem::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_BATTERY = REGISTRATE.item(id("incomplete_battery"), SequencedAssemblyItem::new).register();

    // Phase P6 (light bulbs; models and the item model files ship from upstream's generated data)
    public static final ItemEntry<LvLightBulb> LV_LIGHT_BULB = REGISTRATE.item(id("lv_light_bulb"), LvLightBulb::new)
            .transform(LightBulb.setModelNameProvider(() -> state -> PowerGrid.asResource(switch(state) {
                case OFF -> "block/lamps/light_bulb";
                case LOW_POWER, ON -> "block/lamps/light_bulb_on";
                case BROKEN -> "block/lamps/light_bulb_broken";
                case LIGHT -> "block/lamps/light_bulb_light";
            })))
            .transform(LightBulb.setDyedModelNameProvider(() -> state -> PowerGrid.asResource(switch(state) {
                case OFF -> "block/lamps/dyed_light_bulb";
                case LOW_POWER, ON -> "block/lamps/dyed_light_bulb_on";
                case BROKEN -> "block/lamps/dyed_light_bulb_broken";
                case LIGHT -> "block/lamps/dyed_light_bulb_light";
                case BULB -> "block/lamps/dyed_light_bulb_bulb";
            })))
            .transform(LightBulb.setProperties(3, 12, 20, 0.001f))
            .register();

    public static final ItemEntry<LightBulb> LIGHT_BULB = REGISTRATE.item(id("light_bulb"), LightBulb::new)
            .transform(LightBulb.setModelNameProvider(() -> state -> PowerGrid.asResource(switch(state) {
                case OFF -> "block/lamps/light_bulb";
                case LOW_POWER, ON -> "block/lamps/light_bulb_on";
                case BROKEN -> "block/lamps/light_bulb_broken";
                case LIGHT -> "block/lamps/light_bulb_light";
            })))
            .transform(LightBulb.setDyedModelNameProvider(() -> state -> PowerGrid.asResource(switch(state) {
                case OFF -> "block/lamps/dyed_light_bulb";
                case LOW_POWER, ON -> "block/lamps/dyed_light_bulb_on";
                case BROKEN -> "block/lamps/dyed_light_bulb_broken";
                case LIGHT -> "block/lamps/dyed_light_bulb_light";
                case BULB -> "block/lamps/dyed_light_bulb_bulb";
            })))
            .transform(LightBulb.setProperties(30, 120, 120, 0.005f))
            .register();

    public static final ItemEntry<GrowthLamp> GROWTH_LAMP = REGISTRATE.item(id("growth_lamp"), GrowthLamp::new)
            .transform(LightBulb.setModelNameProvider(() -> state -> PowerGrid.asResource(switch(state) {
                case OFF -> "block/lamps/growth_lamp";
                case LOW_POWER, ON -> "block/lamps/growth_lamp_on";
                case BROKEN -> "block/lamps/growth_lamp_broken";
                case LIGHT -> "block/lamps/growth_lamp_light";
            })))
            .transform(LightBulb.setProperties(120, 240, 120, 0.01f))
            .register();

    public static final ItemEntry<Item> NEON_BULB = REGISTRATE.item(id("neon_bulb"), Item::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_SOLAR_PANEL = REGISTRATE.item(id("incomplete_solar_panel"), SequencedAssemblyItem::new).register();

    // Phase P7 (equipment; models, the chestplate tag and lang ship from upstream's generated data)
    public static final ItemEntry<ElectroZapperItem> ELECTROZAPPER = REGISTRATE.item(id("electrozapper"), ElectroZapperItem::new)
            .transform(customRenderer(() -> ElectroZapperItemRenderer::new))
            .register();

    public static final ItemEntry<ElectroBatonItem> ELECTROBATON = REGISTRATE.item(id("electrobaton"), ElectroBatonItem::new)
            .register();

    public static final ItemEntry<DrillItem> PORTABLE_DRILL = REGISTRATE.item(id("portable_drill"), DrillItem::new)
            .transform(customRenderer(() -> DrillItemRenderer::new))
            .register();

    public static final ItemEntry<SawItem> PORTABLE_SAW = REGISTRATE.item(id("portable_saw"), SawItem::new)
            .transform(customRenderer(() -> SawItemRenderer::new))
            .register();

    public static final ItemEntry<BacktankItem.BacktankBlockItem> PORTABLE_BATTERY_PLACEABLE = REGISTRATE.item(id("portable_battery_placeable"),
                    p -> new BacktankItem.BacktankBlockItem(ModdedBlocks.PORTABLE_BATTERY.get(), ModdedItems.PORTABLE_BATTERY::get, p))
            .register();

    public static final ItemEntry<PortableBatteryItem> PORTABLE_BATTERY = REGISTRATE.item(id("portable_battery"), p -> new PortableBatteryItem(ZincArmorMaterial.INSTANCE, p, PowerGrid.asResource("zinc"), PORTABLE_BATTERY_PLACEABLE))
            .properties(p -> p.durability(-1))
            .register();

    // Phase P8 (display and control)
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_PUNCH_CARD = REGISTRATE.item(id("incomplete_punch_card"), SequencedAssemblyItem::new).register();

    public static final ItemEntry<org.patryk3211.powergrid.kinetics.punchcard.PunchCardItem> PUNCH_CARD = REGISTRATE.item(id("punch_card"), org.patryk3211.powergrid.kinetics.punchcard.PunchCardItem::new)
            .register();

    public static final ItemEntry<org.patryk3211.powergrid.electricity.modulardisplay.DisplayModuleItem> DISPLAY_MODULE = REGISTRATE.item(id("display_module"), org.patryk3211.powergrid.electricity.modulardisplay.DisplayModuleItem::new)
            .register();

    // Phase P9a (components on boards). Plain items as upstream; names (Double Pole Relay, Static Induction Transistor,
    // NPN BJT, ...) and the generated item models ship from upstream's data. The BJTs' transitional items come with
    // their sequenced assembly recipes.
    public static final ItemEntry<Item> PINS = REGISTRATE.item(id("pins"), Item::new).register();
    public static final ItemEntry<Item> RELAY = REGISTRATE.item(id("relay"), Item::new).register();
    public static final ItemEntry<Item> RELAY_DPDT = REGISTRATE.item(id("relay_dpdt"), Item::new).register();
    public static final ItemEntry<Item> RESISTOR = REGISTRATE.item(id("resistor"), Item::new).register();
    public static final ItemEntry<Item> REDSTONE_RELAY = REGISTRATE.item(id("redstone_relay"), Item::new).register();
    public static final ItemEntry<Item> DIODE = REGISTRATE.item(id("diode"), Item::new).register();
    public static final ItemEntry<Item> VFET = REGISTRATE.item(id("vfet"), Item::new).register();
    public static final ItemEntry<Item> BJT_NPN = REGISTRATE.item(id("bjt_npn"), Item::new).register();
    public static final ItemEntry<Item> BJT_PNP = REGISTRATE.item(id("bjt_pnp"), Item::new).register();
    public static final ItemEntry<Item> CAPACITOR = REGISTRATE.item(id("capacitor"), Item::new).register();
    public static final ItemEntry<Item> POTENTIOMETER = REGISTRATE.item(id("potentiometer"), Item::new).register();
    public static final ItemEntry<Item> TRIODE = REGISTRATE.item(id("triode"), Item::new).register();
    public static final ItemEntry<Item> PENTODE = REGISTRATE.item(id("pentode"), Item::new).register();
    public static final ItemEntry<Item> THYRATRON = REGISTRATE.item(id("thyratron"), Item::new).register();
    public static final ItemEntry<Item> REGULATOR_TUBE = REGISTRATE.item(id("regulator_tube"), Item::new).register();
    public static final ItemEntry<Item> BARRETTER_TUBE = REGISTRATE.item(id("barretter_tube"), Item::new).register();
    public static final ItemEntry<Item> VARISTOR = REGISTRATE.item(id("varistor"), Item::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_BJT_NPN = REGISTRATE.item(id("incomplete_bjt_npn"), SequencedAssemblyItem::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_BJT_PNP = REGISTRATE.item(id("incomplete_bjt_pnp"), SequencedAssemblyItem::new).register();

    // Phase P9b. The circuit_schematic_holder tag entry ships from upstream's data.
    public static final ItemEntry<org.patryk3211.powergrid.circuits.schematic.CircuitSchematicItem> CIRCUIT_SCHEMATIC = REGISTRATE.item(id("circuit_schematic"), org.patryk3211.powergrid.circuits.schematic.CircuitSchematicItem::new).register();

    // Phase P9c: the etching chain (circuit_schematic_holder tag entries ship from upstream's data). The acid bucket
    // registers with the fluid (ModdedFluids).
    public static final ItemEntry<Item> EMPTY_CIRCUIT = REGISTRATE.item(id("empty_circuit"), Item::new).register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_UNETCHED_CIRCUIT = REGISTRATE.item(id("incomplete_unetched_circuit"), SequencedAssemblyItem::new).register();
    public static final ItemEntry<Item> UNETCHED_CIRCUIT = REGISTRATE.item(id("unetched_circuit"), Item::new).register();
    public static final ItemEntry<org.patryk3211.powergrid.circuits.circuitboard.IncompleteCircuitItem> INCOMPLETE_CIRCUIT = REGISTRATE.item(id("incomplete_circuit"), org.patryk3211.powergrid.circuits.circuitboard.IncompleteCircuitItem::new).register();

    // Phase P9d: the Integrated Circuit (boosting chip; deployers and the hand apply it to the equipment through the
    // boost recipes) and the electrical gizmo's transitional item.
    public static final ItemEntry<BoostingChipItem> INTEGRATED_CIRCUIT = REGISTRATE.item(id("integrated_circuit"), BoostingChipItem::new)
            .transform(customRenderer(() -> BoostingChipRenderer::new))
            .register();
    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_ELECTRICAL_GIZMO = REGISTRATE.item(id("incomplete_electrical_gizmo"), SequencedAssemblyItem::new).register();

    @SuppressWarnings("EmptyMethod")
    public static void register() { /* Initialize static fields. */ }

    // @ExpectPlatform inlined (D2): forge implementation in ModdedItemsImpl
    public static <T extends Item, P> NonNullUnaryOperator<ItemBuilder<T, P>> customRenderer(Supplier<Supplier<CustomRenderedItemModelRenderer>> renderer) {
        return org.patryk3211.powergrid.collections.forge.ModdedItemsImpl.customRenderer(renderer);
    }
}
