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
// Port state: phases P2 to P10 (P5 is ponder only; complete: every upstream block is here) (each phase's blocks after the previous one's). PortPhase.require guards each id. Registrate data-generation calls (blockstate,
// item model, tag, lang, recipe) are left out: the jar ships upstream's generated output.
package org.patryk3211.powergrid.collections;

import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import org.patryk3211.powergrid.PortPhase;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import net.minecraft.world.level.material.MaterialColor;
import org.patryk3211.powergrid.electricity.bell.AlarmBellBlock;
import org.patryk3211.powergrid.electricity.crt.CRTBlock;
import org.patryk3211.powergrid.electricity.crt.EncasedCRTBlock;
import org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlock;
import org.patryk3211.powergrid.kinetics.plotter.PlotterBlock;
import org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlock;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import org.patryk3211.powergrid.config.CStress;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import org.patryk3211.powergrid.electricity.basinheater.BasinHeaterBlock;
import org.patryk3211.powergrid.electricity.basinheater.BasinHeaterBlockEntity;
import org.patryk3211.powergrid.electricity.fan.ElectricFanBlock;
import org.patryk3211.powergrid.electricity.heater.HeaterBlock;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightBlock;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightLightBlock;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlock;
import org.patryk3211.powergrid.electricity.pump.ElectricPumpBlock;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingBlock;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBlock;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelCTBehaviour;
import org.patryk3211.powergrid.electricity.battery.*;
import org.patryk3211.powergrid.electricity.carbonpile.*;
import org.patryk3211.powergrid.electricity.contactor.ContactorBlock;
import org.patryk3211.powergrid.electricity.deviceconnector.DeviceConnectorBlock;
import org.patryk3211.powergrid.electricity.electricswitch.*;
import org.patryk3211.powergrid.electricity.electromagnet.ElectromagnetBlock;
import org.patryk3211.powergrid.electricity.febridge.FEInverterBlock;
import org.patryk3211.powergrid.electricity.fuse.FuseHolderBlock;
import org.patryk3211.powergrid.electricity.grounding.GroundingRodBlock;
import org.patryk3211.powergrid.electricity.redstoneconverter.RedstoneConverterBlock;
import org.patryk3211.powergrid.electricity.resistor.ResistorBlock;
import org.patryk3211.powergrid.electricity.sparkgap.SparkGapBlock;
import org.patryk3211.powergrid.electricity.transformer.*;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingBlock;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlock;
import org.patryk3211.powergrid.kinetics.generator.housing.GeneratorHousing;
import org.patryk3211.powergrid.kinetics.generator.housing.VerticalGeneratorHousing;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.CommutatorBlock;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.InductionRotorBlock;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.LargeInductionRotorBlock;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.VerticalCommutatorBlock;
import org.patryk3211.powergrid.kinetics.motor.ConstantSpeedMotorBlock;
import org.patryk3211.powergrid.kinetics.motor.ElectricMotorBlock;
import org.patryk3211.powergrid.kinetics.rheostat.RheostatBlock;
import org.patryk3211.powergrid.kinetics.servo.ServoBlock;
import org.patryk3211.powergrid.kinetics.variac.VariacBlock;
import org.patryk3211.powergrid.config.CResistance;
import org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryBlock;
import org.patryk3211.powergrid.config.CThermal;
import org.patryk3211.powergrid.electricity.creative.CreativeResistorBlock;
import org.patryk3211.powergrid.electricity.creative.CreativeSourceBlock;
import org.patryk3211.powergrid.electricity.gauge.CurrentGaugeBlock;
import org.patryk3211.powergrid.electricity.gauge.EnergyMeterBlock;
import org.patryk3211.powergrid.electricity.gauge.PowerGaugeBlock;
import org.patryk3211.powergrid.electricity.gauge.VoltageGaugeBlock;
import org.patryk3211.powergrid.electricity.overheated.OverheatedComponentBlock;
import org.patryk3211.powergrid.electricity.socket.SocketBlock;
import org.patryk3211.powergrid.electricity.wireconnector.ConnectorBlock;
import org.patryk3211.powergrid.electricity.wireconnector.CordJunctionBlock;
import org.patryk3211.powergrid.electricity.wireconnector.HeavyConnectorBlock;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerBlock;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerItem;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerItemRenderer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.patryk3211.powergrid.electricity.light.string.StringLightBlock;
import org.patryk3211.powergrid.general.ceilingtile.CeilingTileBlock;
import org.patryk3211.powergrid.general.ceilingtile.junction.CeilingTileJunctionBlock;
import org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlock;
import org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlock;
import org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlockCTBehaviour;
import org.patryk3211.powergrid.general.ceilingtile.wire.CeilingTileConnectorBlock;

import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedBlocks {
    private static String id(String name) {
        return PortPhase.require("block", name);
    }

    public static final BlockEntry<ConnectorBlock> WIRE_CONNECTOR = REGISTRATE.block(id("wire_connector"), ConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<HeavyConnectorBlock> HEAVY_WIRE_CONNECTOR = REGISTRATE.block(id("heavy_wire_connector"), HeavyConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<CordJunctionBlock> CORD_JUNCTION = REGISTRATE.block(id("cord_junction"), CordJunctionBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<VoltageGaugeBlock> VOLTAGE_METER = REGISTRATE.block(id("voltage_gauge"), VoltageGaugeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistances("range_2kv", 2e7, "range_200v", 2e6, "range_20v", 2e5, "range_2v", 2e4))
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.ELECTRIC_GAUGE, block))
            .simpleItem()
            .register();

    public static final BlockEntry<CurrentGaugeBlock> CURRENT_METER = REGISTRATE.block(id("current_gauge"), CurrentGaugeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(CResistance.setResistance(0.05f))
            .transform(CThermal.maxPower(35, 2.0f))
            .transform(pickaxeOnly())
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.ELECTRIC_GAUGE, block))
            .simpleItem()
            .register();

    public static final BlockEntry<PowerGaugeBlock> POWER_METER = REGISTRATE.block(id("power_gauge"), PowerGaugeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(CResistance.setResistances("series", 0.05f, "shunt_range_20kw", 2e7, "shunt_range_2kw", 2e7, "shunt_range_200w", 2e6, "shunt_range_20w", 2e5))
            .transform(CThermal.maxPower(35, 2.0f))
            .transform(pickaxeOnly())
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.ELECTRIC_GAUGE, block))
            .simpleItem()
            .register();

    public static final BlockEntry<EnergyMeterBlock> ENERGY_METER = REGISTRATE.block(id("energy_meter"), EnergyMeterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CResistance.setResistances("series", 0.025f, "shunt", 2e6))
            .transform(CThermal.maxPower(100, 2.0f))
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<CreativeSourceBlock> CREATIVE_VOLTAGE_SOURCE = REGISTRATE.block(id("creative_voltage_source"), CreativeSourceBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<CreativeSourceBlock> CREATIVE_CURRENT_SOURCE = REGISTRATE.block(id("creative_current_source"), CreativeSourceBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<CreativeResistorBlock> CREATIVE_RESISTOR = REGISTRATE.block(id("creative_resistor"), CreativeResistorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<SocketBlock> SOCKET = REGISTRATE.block(id("socket"), SocketBlock::new)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<ThermometerBlock> THERMOMETER = REGISTRATE.block(id("thermometer"), ThermometerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.THERMOMETER, block))
            .item(ThermometerItem::new)
                .transform(ModdedItems.customRenderer(() -> ThermometerItemRenderer::new))
                .build()
            .register();

    // Port (D4, GPLv3): what an overheated component turns into. No item; it is placed only by ThermalBehaviour.
    public static final BlockEntry<OverheatedComponentBlock> OVERHEATED_COMPONENT = REGISTRATE.block(id("overheated_component"), OverheatedComponentBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.noOcclusion().strength(1.5f))
            .transform(pickaxeOnly())
            .register();

    // Phase P3
    public static final BlockEntry<BatteryBlock> BATTERY = REGISTRATE.block(id("battery"), BatteryBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(BatteryBlock.setSpec(SimpleBatterySpec.ACID_BATTERY))
            .transform(CThermal.maxPower(100, 1.5f))
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.BATTERY, block))
            .onRegister(CreateRegistrate.connectedTextures(BatteryCTBehaviour::new))
            .item(BatteryItem::new)
                .build()
            .register();

    public static final BlockEntry<PotatoBatteryBlock> POTATO_BATTERY = REGISTRATE.block(id("potato_battery"), PotatoBatteryBlock::new)
            .initialProperties(() -> Blocks.NETHER_WART)
            .simpleItem()
            .register();

    public static final BlockEntry<LvSwitchBlock> LV_SWITCH = REGISTRATE.block(id("lv_switch"), LvSwitchBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(0.15))
            .transform(CThermal.maxPower(38.4, 0.5f))
            .simpleItem()
            .register();

    public static final BlockEntry<LvButtonBlock> LV_BUTTON = REGISTRATE.block(id("lv_button"), LvButtonBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(0.15))
            .transform(CThermal.maxPower(38.4, 0.5f))
            .simpleItem()
            .register();

    public static final BlockEntry<MvSwitchBlock> MV_SWITCH = REGISTRATE.block(id("mv_switch"), MvSwitchBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(0.05))
            .transform(CThermal.maxPower(51.2, 1.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<HvSwitchBlock> HV_SWITCH = REGISTRATE.block(id("hv_switch"), HvSwitchBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .transform(CStress.setImpact(2))
            .transform(CResistance.setResistance(0.1))
            .transform(CThermal.maxPower(1000, 5.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<HvBreakerBlock> HV_BREAKER = REGISTRATE.block(id("hv_breaker"), HvBreakerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CStress.setImpact(2))
            .transform(CResistance.setResistance(0.1))
            .transform(CThermal.maxPower(102.4, 2.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<SparkGapBlock> SPARK_GAP = REGISTRATE.block(id("spark_gap"), SparkGapBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .simpleItem()
            .register();

    public static final BlockEntry<ContactorBlock> CONTACTOR = REGISTRATE.block(id("contactor"), ContactorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistances("coil", 12, "switch", 0.05))
            .transform(CThermal.maxPower(252.8, 2.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<ResistorBlock> RESISTOR = REGISTRATE.block(id("power_resistor"), ResistorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(CThermal.maxPower(1000, 2.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<TransformerCoreBlock> TRANSFORMER_CORE = REGISTRATE.block(id("transformer_core"), TransformerCoreBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(properties -> properties.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<TransformerSmallBlock> TRANSFORMER_SMALL = REGISTRATE.block(id("transformer_small"), TransformerSmallBlock::new)
            .initialProperties(TRANSFORMER_CORE)
            .properties(properties -> properties.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .transform(CThermal.maxPower(1000, 4.0f))
            .register();

    public static final BlockEntry<TransformerMediumBlock> TRANSFORMER_MEDIUM = REGISTRATE.block(id("transformer_medium"), TransformerMediumBlock::new)
            .initialProperties(TRANSFORMER_CORE)
            .properties(properties -> properties.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .transform(CThermal.maxPower(4000, 16.0f))
            .register();

    public static final BlockEntry<NetherTransformerBlock> NETHER_TRANSFORMER = REGISTRATE.block(id("nether_transformer"), NetherTransformerBlock::new)
            .initialProperties(TRANSFORMER_CORE)
            .transform(pickaxeOnly())
            .transform(CThermal.maxPower(4000, 16.0f))
            .register();

    public static final BlockEntry<ElectromagnetBlock> ELECTROMAGNET = REGISTRATE.block(id("electromagnet"), ElectromagnetBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(15))
            .transform(CThermal.maxPower(1500, 4.0f))
            .item(AssemblyOperatorBlockItem::new)
                .build()
            .register();

    public static final BlockEntry<WindingBlock> WINDING = REGISTRATE.block(id("winding"), WindingBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(7.5))
            .transform(CThermal.maxPower(200, 1.5f))
            .addLayer(() -> RenderType::cutoutMipped)
            .register();

    public static final BlockEntry<DeviceConnectorBlock> DEVICE_CONNECTOR = REGISTRATE.block(id("device_connector"), DeviceConnectorBlock::new)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<RedstoneConverterBlock> REDSTONE_CONVERTER = REGISTRATE.block(id("redstone_converter"), RedstoneConverterBlock::new)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistances("min", 5, "max", 1000))
            .transform(CThermal.maxPower(50, 0.75f))
            .simpleItem()
            .register();

    public static final BlockEntry<FEInverterBlock> FE_INVERTER = REGISTRATE.block(id("fe_inverter"), FEInverterBlock::new)
            .transform(axeOrPickaxe())
            .simpleItem()
            .register();

    public static final BlockEntry<FuseHolderBlock> FUSE_HOLDER = REGISTRATE.block(id("fuse_holder"), FuseHolderBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(0.2))
            .simpleItem()
            .register();

    public static final BlockEntry<GroundingRodBlock> GROUNDING_ROD = REGISTRATE.block(id("grounding_rod"), GroundingRodBlock::new)
            .initialProperties(() -> Blocks.LIGHTNING_ROD)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<CarbonPileCoilBlock> CARBON_PILE_COIL = REGISTRATE.block(id("carbon_pile_coil"), CarbonPileCoilBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(50))
            .transform(CThermal.maxPower(100, 2.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<CarbonPileBlock> CARBON_PILE = REGISTRATE.block(id("carbon_pile"), CarbonPileBlock::new)
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(20))
            .transform(CThermal.maxPower(1000, 2.0f))
            .register();

    // Phase P4
    public static final BlockEntry<CasingBlock> CONDUCTIVE_CASING = REGISTRATE.block(id("conductive_casing"), CasingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .onRegister(connectedTextures(() -> new EncasedCTBehaviour(ModdedPartialModels.CONDUCTIVE_CASING)))
            .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ModdedPartialModels.CONDUCTIVE_CASING)))
            .simpleItem()
            .register();

    public static final BlockEntry<InductionRotorBlock> GENERATOR_INDUCTION_ROTOR = REGISTRATE.block(id("generator_induction_rotor"), InductionRotorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(0.5))
            .transform(CStress.setImpact(32))
            .simpleItem()
            .register();

    public static final BlockEntry<LargeInductionRotorBlock> GENERATOR_LARGE_INDUCTION_ROTOR = REGISTRATE.block(id("generator_large_induction_rotor"), LargeInductionRotorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(1.0))
            .transform(CStress.setImpact(128))
            .simpleItem()
            .register();

    public static final BlockEntry<CommutatorBlock> GENERATOR_COMMUTATOR = REGISTRATE.block(id("generator_commutator"), CommutatorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CStress.setImpact(8))
            .simpleItem()
            .register();

    public static final BlockEntry<VerticalCommutatorBlock> GENERATOR_VERTICAL_COMMUTATOR = REGISTRATE.block(id("generator_vertical_commutator"), VerticalCommutatorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CStress.setImpact(8))
            .simpleItem()
            .register();

    // Tag ignore_in_rotor_assembly_size comes from the shipped tag file (upstream: Registrate .tag).
    public static final BlockEntry<GeneratorClutchBlock> GENERATOR_CLUTCH = REGISTRATE.block(id("generator_clutch"), GeneratorClutchBlock::new)
            .initialProperties(SharedProperties::wooden)
            .transform(axeOrPickaxe())
            .transform(CStress.setImpact(32))
            .onRegister(block -> AllDisplayBehaviours.assignBlock(ModdedDisplaySources.CLUTCH, block))
            .simpleItem()
            .register();

    public static final BlockEntry<GeneratorHousing> GENERATOR_HOUSING = REGISTRATE.block(id("generator_housing"), GeneratorHousing::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .simpleItem()
            .register();

    public static final BlockEntry<VerticalGeneratorHousing> VERTICAL_GENERATOR_HOUSING = REGISTRATE.block(id("vertical_generator_housing"), VerticalGeneratorHousing::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .addLayer(() -> RenderType::cutoutMipped)
            .simpleItem()
            .register();

    public static final BlockEntry<VariacBlock> VARIAC = REGISTRATE.block(id("variac"), VariacBlock::new)
            .initialProperties(TRANSFORMER_CORE)
            .transform(pickaxeOnly())
            .transform(CStress.setNoImpact())
            .transform(CThermal.maxPower(1000, 4.0f))
            .simpleItem()
            .register();

    // Port: upstream onRegister(BlockStressValues.setGeneratorSpeed(256, true)) (Create 6) -> CStress.setGeneratorSpeed,
    // read by the Create 0.5.1 stress provider's getGeneratedRPM (ModdedConfigs).
    public static final BlockEntry<ElectricMotorBlock> ELECTRIC_MOTOR = REGISTRATE.block(id("electric_motor"), ElectricMotorBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(CStress.setCapacity(64))
            .transform(CResistance.setResistance(25.6))
            .transform(pickaxeOnly())
            .transform(CStress.setGeneratorSpeed(256, true))
            .simpleItem()
            .register();

    public static final BlockEntry<ConstantSpeedMotorBlock> CONSTANT_SPEED_MOTOR = REGISTRATE.block(id("constant_speed_motor"), ConstantSpeedMotorBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(CStress.setCapacity(64))
            .transform(CResistance.setResistance(25.6))
            .transform(pickaxeOnly())
            .transform(CStress.setGeneratorSpeed(256, true))
            .simpleItem()
            .register();

    public static final BlockEntry<ServoBlock> SERVO = REGISTRATE.block(id("servo"), ServoBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(CStress.setCapacity(32))
            .transform(CResistance.setResistances("on", 12.8, "idle", 64))
            .transform(pickaxeOnly())
            .transform(CStress.setGeneratorSpeed(32, true))
            .simpleItem()
            .register();

    public static final BlockEntry<RheostatBlock> RHEOSTAT = REGISTRATE.block(id("rheostat"), RheostatBlock::new)
            .initialProperties(CONDUCTIVE_CASING)
            .transform(pickaxeOnly())
            .transform(CStress.setNoImpact())
            .transform(CThermal.maxPower(1000, 4.0f))
            .simpleItem()
            .register();

    // Phase P6: heat, light, fluid, solar
    public static final BlockEntry<HeaterBlock> HEATING_COIL = REGISTRATE.block(id("heating_coil"), HeaterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(25))
            .transform(CThermal.maxPower(60, 1.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<BasinHeaterBlock> BASIN_HEATER = REGISTRATE.block(id("basin_heater"), BasinHeaterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.lightLevel(state -> {
                BlazeBurnerBlock.HeatLevel heat = state.getValue(BasinHeaterBlock.HEAT_LEVEL);

                if (heat == BlazeBurnerBlock.HeatLevel.NONE)
                    return 0;
                if (heat == BlazeBurnerBlock.HeatLevel.KINDLED)
                    return 12;
                if (heat == BlazeBurnerBlock.HeatLevel.SEETHING)
                    return 15;

                return 0;
            }))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(10))
            // Port: Create 6 BoilerHeater.REGISTRY -> Create 0.5.1 BoilerHeaters.registerHeater (float heat, -1 none)
            .onRegister(block -> BoilerHeaters.registerHeater(block, BasinHeaterBlockEntity::boilerHeater))
            .simpleItem()
            .register();

    public static final BlockEntry<LightFixtureBlock> LIGHT_FIXTURE = REGISTRATE.block(id("light_fixture"), LightFixtureBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .transform(LightFixtureBlock.setBulbModelOffset(0, 3 / 16f, 0))
            .simpleItem()
            .register();

    public static final BlockEntry<FactoryLightBlock> FACTORY_LIGHT = REGISTRATE.block(id("factory_light"), FactoryLightBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<FactoryLightLightBlock> FACTORY_LIGHT_LIGHT = REGISTRATE.block(id("factory_light_light"), FactoryLightLightBlock::new)
            .initialProperties(FactoryLightLightBlock.MATERIAL)
            .register();

    public static final BlockEntry<ElectricFanBlock> ELECTRIC_FAN = REGISTRATE.block(id("electric_fan"), ElectricFanBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(20))
            .addLayer(() -> RenderType::cutoutMipped)
            .simpleItem()
            .register();

    public static final BlockEntry<ElectricPumpBlock> ELECTRIC_PUMP = REGISTRATE.block(id("electric_pump"), ElectricPumpBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(480))
            .simpleItem()
            .register();

    public static final BlockEntry<SolarPanelBlock> SOLAR_PANEL = REGISTRATE.block(id("solar_panel"), SolarPanelBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .onRegister(CreateRegistrate.connectedTextures(SolarPanelCTBehaviour::new))
            .simpleItem()
            .register();

    public static final BlockEntry<SolarPanelBearingBlock> SOLAR_PANEL_BEARING = REGISTRATE.block(id("solar_panel_bearing"), SolarPanelBearingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .simpleItem()
            .register();

    // Phase P7 (equipment). The block has no item of its own: powergrid:portable_battery (the worn item) and
    // portable_battery_placeable are in ModdedItems; the loot table ships from upstream's generated data.
    public static final BlockEntry<PortableBatteryBlock> PORTABLE_BATTERY = REGISTRATE.block(id("portable_battery"), PortableBatteryBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(25))
            .transform(CThermal.maxPower(100, 1.0f))
            .register();

    // Phase P8 (display and control). The plotter and punch card reader item models, the encased CRT variants'
    // blockstates and the lang ship from upstream's generated data. 1.20 MapColor.PODZOL -> MaterialColor.PODZOL.
    public static final BlockEntry<PlotterBlock> PLOTTER = REGISTRATE.block(id("plotter"), PlotterBlock::new)
            .initialProperties(SharedProperties::wooden)
            .addLayer(() -> RenderType::translucent)
            .transform(CStress.setImpact(2.0))
            .transform(axeOrPickaxe())
            .simpleItem()
            .register();

    public static final BlockEntry<AlarmBellBlock> ALARM_BELL = REGISTRATE.block(id("alarm_bell"), AlarmBellBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistance(20))
            .transform(CThermal.maxPower(50, 1.5f))
            .simpleItem()
            .register();

    public static final BlockEntry<CRTBlock> CRT = REGISTRATE.block(id("crt"), CRTBlock::new)
            .initialProperties(() -> Blocks.GLASS)
            .addLayer(() -> RenderType::translucent)
            .transform(pickaxeOnly())
            .transform(CResistance.setResistances("heater", 12, "coils", 40))
            .transform(CThermal.maxPower(100, 3.0f))
            .simpleItem()
            .register();

    public static final BlockEntry<EncasedCRTBlock> ANDESITE_CRT = REGISTRATE.block(id("andesite_encased_crt"), p -> new EncasedCRTBlock(p, AllBlocks.ANDESITE_CASING::get))
            .initialProperties(SharedProperties::wooden)
            .addLayer(() -> RenderType::translucent)
            .transform(axeOrPickaxe())
            .properties(p -> p.color(MaterialColor.PODZOL))
            .transform(EncasingRegistry.addVariantTo(CRT))
            .register();

    public static final BlockEntry<EncasedCRTBlock> BRASS_CRT = REGISTRATE.block(id("brass_encased_crt"), p -> new EncasedCRTBlock(p, AllBlocks.BRASS_CASING::get))
            .initialProperties(SharedProperties::wooden)
            .addLayer(() -> RenderType::translucent)
            .transform(axeOrPickaxe())
            .properties(p -> p.color(MaterialColor.PODZOL))
            .transform(EncasingRegistry.addVariantTo(CRT))
            .register();

    public static final BlockEntry<PunchCardReaderBlock> PUNCH_CARD_READER = REGISTRATE.block(id("punch_card_reader"), PunchCardReaderBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .transform(CStress.setImpact(2))
            .transform(CResistance.setResistance(1f))
            .transform(CThermal.maxPower(10, 0.2f))
            .simpleItem()
            .register();

    public static final BlockEntry<ModularDisplayBlock> MODULAR_DISPLAY = REGISTRATE.block(id("modular_display"), ModularDisplayBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    // Phase P9a (components on boards). Blockstate, the circuit_board model (custom loader, CircuitBoardModelLoader),
    // the item model, the loot table (copies Schematic and Thermal) and the circuit_schematic_holder item tag ship from
    // upstream's generated data.
    public static final BlockEntry<org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock> CIRCUIT_BOARD = REGISTRATE.block(id("circuit_board"), org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    // Phase P9b (design table and editor). Blockstate, models (and the glow partial), loot and the mineable tags ship
    // from upstream's generated data; its crafting recipe needs copper_plating (craftable since P10, phases.json override).
    public static final BlockEntry<org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlock> CIRCUIT_DESIGN_TABLE = REGISTRATE.block(id("circuit_design_table"), org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlock::new)
            .initialProperties(() -> Blocks.CRAFTING_TABLE)
            .transform(axeOrPickaxe())
            .transform(CResistance.setResistance(480))
            .transform(CThermal.maxPower(50, 0.5f))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    // Phase P10 (decor). Blockstates, models, item models, loot tables and the mineable tags ship from upstream's
    // generated data (B33: the slab and stairs added to mineable/pickaxe, the double slab drops two). Forge's
    // StairBlock(Supplier, Properties) for the 1.20 StairBlock(BlockState, Properties). The four ceiling tile
    // attachments have no item (pick-block gives the attachment; their loot drops the tile and the attachment).
    public static final BlockEntry<CasingBlock> COPPER_PLATING = REGISTRATE.block(id("copper_plating"), CasingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .onRegister(connectedTextures(() -> new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING)))
            .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ModdedPartialModels.COPPER_PLATING)))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> COPPER_PLATING_STAIRS = REGISTRATE.block(id("copper_plating_stairs"), p -> new StairBlock(() -> ModdedBlocks.COPPER_PLATING.getDefaultState(), p))
            .initialProperties(SharedProperties::softMetal)
            .onRegister(connectedTextures(() -> new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING)))
            .onRegister(casingConnectivity((block, cc) -> cc.make(block, ModdedPartialModels.COPPER_PLATING, (s, f) -> {
                var shape = s.getValue(StairBlock.SHAPE);
                var half = s.getValue(StairBlock.HALF);
                var facing = s.getValue(StairBlock.FACING);
                if(half == Half.TOP && f == Direction.UP)
                    return true;
                if(half == Half.BOTTOM && f == Direction.DOWN)
                    return true;
                if(facing == f)
                    return true;
                if(shape == StairsShape.INNER_LEFT && f == facing.getCounterClockWise())
                    return true;
                if(shape == StairsShape.INNER_RIGHT && f == facing.getClockWise())
                    return true;
                return false;
            })))
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> COPPER_PLATING_SLAB = REGISTRATE.block(id("copper_plating_slab"), SlabBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .onRegister(connectedTextures(() -> new EncasedCTBehaviour(ModdedPartialModels.COPPER_PLATING)))
            .onRegister(casingConnectivity((block, cc) -> cc.make(block, ModdedPartialModels.COPPER_PLATING, (s, f) -> {
                var type = s.getValue(SlabBlock.TYPE);
                if(type == SlabType.TOP && f == Direction.UP)
                    return true;
                if(type == SlabType.BOTTOM && f == Direction.DOWN)
                    return true;
                return false;
            })))
            .simpleItem()
            .register();

    public static final BlockEntry<CeilingTileBlock> CEILING_TILE = REGISTRATE.block(id("ceiling_tile"), CeilingTileBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .simpleItem()
            .register();

    public static final BlockEntry<CeilingTileLampBlock> CEILING_TILE_LAMP = REGISTRATE.block(id("ceiling_tile_lamp"), CeilingTileLampBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .addLayer(() -> RenderType::cutout)
            .register();

    public static final BlockEntry<CeilingTileConnectorBlock> CEILING_TILE_CONNECTOR = REGISTRATE.block(id("ceiling_tile_connector"), CeilingTileConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<CeilingTileJunctionBlock> CEILING_TILE_JUNCTION = REGISTRATE.block(id("ceiling_tile_junction"), CeilingTileJunctionBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<CeilingTileSolarBlock> CEILING_TILE_SOLAR = REGISTRATE.block(id("ceiling_tile_solar"), CeilingTileSolarBlock::new)
            .initialProperties(SharedProperties::stone)
            .transform(axeOrPickaxe())
            .onRegister(CreateRegistrate.connectedTextures(CeilingTileSolarBlockCTBehaviour::new))
            .register();

    // Light blocks the lit string light cord places in the air along its length (as the factory light's light block).
    public static final BlockEntry<StringLightBlock> STRING_LIGHT_BLOCK = REGISTRATE.block(id("string_light_block"), StringLightBlock::new)
            .initialProperties(FactoryLightLightBlock.MATERIAL)
            .register();

    public static void register() { /* Initialize static fields. */ }
}
