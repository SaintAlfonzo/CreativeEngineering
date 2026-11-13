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
// Port state: phases P2 to P10, complete (each phase's entries after the previous one's; Flywheel 1.0 visuals not ported,
// the renderers draw everything; upstream's commented-out generator_rotor stays out).
package org.patryk3211.powergrid.collections;

import org.patryk3211.powergrid.electricity.basinheater.BasinHeaterBlockEntity;
import org.patryk3211.powergrid.electricity.fan.ElectricFanBlockEntity;
import org.patryk3211.powergrid.electricity.fan.ElectricFanRenderer;
import org.patryk3211.powergrid.electricity.heater.HeaterBlockEntity;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightBlockEntity;
import org.patryk3211.powergrid.electricity.light.factorylight.FactoryLightRenderer;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureBlockEntity;
import org.patryk3211.powergrid.electricity.light.fixture.LightFixtureRenderer;
import org.patryk3211.powergrid.electricity.pump.ElectricPumpBlockEntity;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingBlockEntity;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBearingRenderer;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import org.patryk3211.powergrid.electricity.battery.*;
import org.patryk3211.powergrid.electricity.carbonpile.*;
import org.patryk3211.powergrid.electricity.contactor.*;
import org.patryk3211.powergrid.electricity.deviceconnector.*;
import org.patryk3211.powergrid.electricity.electricswitch.*;
import org.patryk3211.powergrid.electricity.electromagnet.*;
import org.patryk3211.powergrid.electricity.febridge.*;
import org.patryk3211.powergrid.electricity.fuse.*;
import org.patryk3211.powergrid.electricity.grounding.*;
import org.patryk3211.powergrid.electricity.redstoneconverter.*;
import org.patryk3211.powergrid.electricity.resistor.ResistorBlockEntity;
import org.patryk3211.powergrid.electricity.sparkgap.*;
import org.patryk3211.powergrid.electricity.transformer.*;
import org.patryk3211.powergrid.kinetics.generator.winding.WindingBlockEntity;
import org.patryk3211.powergrid.utility.proxy.SubstituteBlockEntityProvider;
import org.patryk3211.powergrid.electricity.creative.CreativeResistorBlockEntity;
import org.patryk3211.powergrid.electricity.creative.CreativeSourceBlockEntity;
import org.patryk3211.powergrid.electricity.gauge.*;
import org.patryk3211.powergrid.electricity.overheated.OverheatedComponentBlockEntity;
import org.patryk3211.powergrid.electricity.overheated.OverheatedComponentRenderer;
import org.patryk3211.powergrid.electricity.socket.SocketBlockEntity;
import org.patryk3211.powergrid.electricity.wireconnector.ConnectorBlockEntity;
import org.patryk3211.powergrid.electricity.wireconnector.CordJunctionBlockEntity;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerBlockEntity;
import org.patryk3211.powergrid.equipment.thermometer.ThermometerRenderer;
import org.patryk3211.powergrid.kinetics.base.TunedBlockRenderer;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.clutch.GeneratorClutchRenderer;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.CommutatorBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.CommutatorRenderer;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.InductionRotorBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.inductionrotor.LargeInductionRotorBlockEntity;
import org.patryk3211.powergrid.kinetics.generator.rotor.RotorRenderer;
import org.patryk3211.powergrid.kinetics.motor.ConstantSpeedMotorBlockEntity;
import org.patryk3211.powergrid.kinetics.motor.ElectricMotorBlockEntity;
import org.patryk3211.powergrid.kinetics.motor.ElectricMotorRenderer;
import org.patryk3211.powergrid.kinetics.rheostat.RheostatBlockEntity;
import org.patryk3211.powergrid.kinetics.servo.ServoBlockEntity;
import org.patryk3211.powergrid.kinetics.servo.ServoRenderer;
import org.patryk3211.powergrid.kinetics.variac.VariacBlockEntity;

import static org.patryk3211.powergrid.PowerGrid.REGISTRATE;

public class ModdedBlockEntities {
    public static final BlockEntityEntry<ConnectorBlockEntity> WIRE_CONNECTOR =
            REGISTRATE.blockEntity("wire_connector", ConnectorBlockEntity::new)
                    .validBlocks(ModdedBlocks.WIRE_CONNECTOR, ModdedBlocks.HEAVY_WIRE_CONNECTOR)
                    .register();

    public static final BlockEntityEntry<CordJunctionBlockEntity> CORD_JUNCTION =
            REGISTRATE.blockEntity("cord_junction", CordJunctionBlockEntity::new)
                    .validBlock(ModdedBlocks.CORD_JUNCTION)
                    .register();

    public static final BlockEntityEntry<VoltageGaugeBlockEntity> VOLTAGE_METER =
            REGISTRATE.blockEntity("voltage_meter", VoltageGaugeBlockEntity::new)
                    .validBlocks(ModdedBlocks.VOLTAGE_METER)
                    .renderer(() -> GaugeRenderer::new)
                    .register();

    public static final BlockEntityEntry<CurrentGaugeBlockEntity> CURRENT_METER =
            REGISTRATE.blockEntity("current_meter", CurrentGaugeBlockEntity::new)
                    .validBlocks(ModdedBlocks.CURRENT_METER)
                    .renderer(() -> GaugeRenderer::new)
                    .register();

    public static final BlockEntityEntry<PowerGaugeBlockEntity> POWER_METER =
            REGISTRATE.blockEntity("power_meter", PowerGaugeBlockEntity::new)
                    .validBlocks(ModdedBlocks.POWER_METER)
                    .renderer(() -> GaugeRenderer::new)
                    .register();

    public static final BlockEntityEntry<EnergyMeterBlockEntity> ENERGY_METER =
            REGISTRATE.blockEntity("energy_meter", EnergyMeterBlockEntity::new)
                    .validBlock(ModdedBlocks.ENERGY_METER)
                    .renderer(() -> EnergyMeterRenderer::new)
                    .register();

    public static final BlockEntityEntry<CreativeSourceBlockEntity> CREATIVE_SOURCE =
            REGISTRATE.blockEntity("creative_source", CreativeSourceBlockEntity::new)
                    .validBlocks(ModdedBlocks.CREATIVE_VOLTAGE_SOURCE, ModdedBlocks.CREATIVE_CURRENT_SOURCE)
                    .register();

    public static final BlockEntityEntry<CreativeResistorBlockEntity> CREATIVE_RESISTOR =
            REGISTRATE.blockEntity("creative_resistor", CreativeResistorBlockEntity::new)
                    .validBlock(ModdedBlocks.CREATIVE_RESISTOR)
                    .register();

    public static final BlockEntityEntry<ThermometerBlockEntity> THERMOMETER =
            REGISTRATE.blockEntity("thermometer", ThermometerBlockEntity::new)
                    .validBlock(ModdedBlocks.THERMOMETER)
                    .renderer(() -> ThermometerRenderer::new)
                    .register();

    public static final BlockEntityEntry<SocketBlockEntity> SOCKET =
            REGISTRATE.blockEntity("socket", SocketBlockEntity::new)
                    .validBlock(ModdedBlocks.SOCKET)
                    .register();

    // Port (D4, GPLv3)
    public static final BlockEntityEntry<OverheatedComponentBlockEntity> OVERHEATED_COMPONENT =
            REGISTRATE.blockEntity("overheated_component", OverheatedComponentBlockEntity::new)
                    .validBlock(ModdedBlocks.OVERHEATED_COMPONENT)
                    .renderer(() -> OverheatedComponentRenderer::new)
                    .register();

    // Phase P3
    public static final BlockEntityEntry<MultiBlockBatteryEntity> MULTIBLOCK_BATTERY =
            REGISTRATE.blockEntity("battery", MultiBlockBatteryEntity::new)
                    .validBlock(ModdedBlocks.BATTERY)
                    .register();

    public static final BlockEntityEntry<PotatoBatteryBlockEntity> POTATO_BATTERY =
            REGISTRATE.blockEntity("potato_battery", PotatoBatteryBlockEntity::new)
                    .validBlock(ModdedBlocks.POTATO_BATTERY)
                    .register();

    public static final BlockEntityEntry<SwitchBlockEntity> SWITCH =
            REGISTRATE.blockEntity("switch", SwitchBlockEntity::new)
                    .validBlocks(ModdedBlocks.LV_SWITCH, ModdedBlocks.MV_SWITCH, ModdedBlocks.LV_BUTTON)
                    .register();

    public static final BlockEntityEntry<HvSwitchBlockEntity> HV_SWITCH =
            REGISTRATE.blockEntity("hv_switch", HvSwitchBlockEntity::new)
                    .validBlock(ModdedBlocks.HV_SWITCH)
                    .renderer(() -> HvSwitchRenderer::new)
                    .register();

    public static final BlockEntityEntry<SparkGapBlockEntity> SPARK_GAP =
            REGISTRATE.blockEntity("spark_gap", SparkGapBlockEntity::new)
                    .validBlock(ModdedBlocks.SPARK_GAP)
                    .renderer(() -> SparkGapRenderer::new)
                    .register();

    public static final BlockEntityEntry<HvBreakerBlockEntity> HV_BREAKER =
            REGISTRATE.blockEntity("hv_breaker", HvBreakerBlockEntity::new)
                    .validBlock(ModdedBlocks.HV_BREAKER)
                    .renderer(() -> HvBreakerRenderer::new)
                    .register();

    public static final BlockEntityEntry<ContactorBlockEntity> CONTACTOR =
            REGISTRATE.blockEntity("contactor", ContactorBlockEntity::new)
                    .validBlock(ModdedBlocks.CONTACTOR)
                    .register();

    public static final BlockEntityEntry<ResistorBlockEntity> RESISTOR =
            REGISTRATE.blockEntity("resistor", ResistorBlockEntity::new)
                    .validBlock(ModdedBlocks.RESISTOR)
                    .register();

    public static final BlockEntityEntry<TransformerSmallBlockEntity> TRANSFORMER_SMALL =
            REGISTRATE.blockEntity("transformer_small", TransformerSmallBlockEntity::new)
                    .validBlock(ModdedBlocks.TRANSFORMER_SMALL)
                    .register();

    public static final BlockEntityEntry<TransformerMediumBlockEntity> TRANSFORMER_MEDIUM =
            REGISTRATE.blockEntity("transformer_medium", TransformerMediumBlockEntity::new)
                    .validBlock(ModdedBlocks.TRANSFORMER_MEDIUM)
                    .register();

    public static final BlockEntityEntry<NetherTransformerBlockEntity> NETHER_TRANSFORMER =
            REGISTRATE.blockEntity("nether_transformer", NetherTransformerBlockEntity::new)
                    .validBlock(ModdedBlocks.NETHER_TRANSFORMER)
                    .register();

    public static final BlockEntityEntry<ElectromagnetBlockEntity> ELECTROMAGNET =
            REGISTRATE.blockEntity("electromagnet", ElectromagnetBlockEntity::new)
                    .validBlock(ModdedBlocks.ELECTROMAGNET)
                    .register();

    public static final BlockEntityEntry<WindingBlockEntity> WINDING =
            REGISTRATE.blockEntity("winding", WindingBlockEntity::new)
                    .validBlock(ModdedBlocks.WINDING)
                    .register();

    public static final BlockEntityEntry<DeviceConnectorBlockEntity> DEVICE_CONNECTOR =
            REGISTRATE.blockEntity("device_connector", SubstituteBlockEntityProvider.INSTANCE.get(DeviceConnectorBlockEntity.class))
                    .validBlock(ModdedBlocks.DEVICE_CONNECTOR)
                    .register();

    public static final BlockEntityEntry<FuseHolderBlockEntity> FUSE_HOLDER =
            REGISTRATE.blockEntity("fuse_holder", FuseHolderBlockEntity::new)
                    .validBlock(ModdedBlocks.FUSE_HOLDER)
                    .register();

    public static final BlockEntityEntry<GroundingRodBlockEntity> GROUNDING_ROD =
            REGISTRATE.blockEntity("grounding_rod", GroundingRodBlockEntity::new)
                    .validBlock(ModdedBlocks.GROUNDING_ROD)
                    .register();

    public static final BlockEntityEntry<RedstoneConverterBlockEntity> REDSTONE_CONVERTER =
            REGISTRATE.blockEntity("redstone_converter", RedstoneConverterBlockEntity::new)
                    .validBlock(ModdedBlocks.REDSTONE_CONVERTER)
                    .register();

    public static final BlockEntityEntry<FEInverterBlockEntity> FE_INVERTER =
            REGISTRATE.blockEntity("fe_inverter", SubstituteBlockEntityProvider.INSTANCE.get(FEInverterBlockEntity.class))
                    .validBlock(ModdedBlocks.FE_INVERTER)
                    .register();

    public static final BlockEntityEntry<CarbonPileCoilBlockEntity> CARBON_PILE_COIL =
            REGISTRATE.blockEntity("carbon_pile_coil", CarbonPileCoilBlockEntity::new)
                    .validBlock(ModdedBlocks.CARBON_PILE_COIL)
                    .register();

    public static final BlockEntityEntry<CarbonPileBlockEntity> CARBON_PILE =
            REGISTRATE.blockEntity("carbon_pile", CarbonPileBlockEntity::new)
                    .validBlock(ModdedBlocks.CARBON_PILE)
                    .register();

    // Phase P4. Flywheel 1.0 visuals (RotorVisual, CommutatorVisual, GeneratorClutchVisual, TunedBlockVisual,
    // HalfShaftVisual) are not ported; every renderer draws its shaft itself (PORT_NOTES.md section 14).
    public static final BlockEntityEntry<InductionRotorBlockEntity> GENERATOR_INDUCTION_ROTOR =
            REGISTRATE.blockEntity("generator_induction_rotor", InductionRotorBlockEntity::new)
                    .validBlock(ModdedBlocks.GENERATOR_INDUCTION_ROTOR)
                    .renderer(() -> RotorRenderer::new)
                    .register();

    public static final BlockEntityEntry<LargeInductionRotorBlockEntity> GENERATOR_LARGE_INDUCTION_ROTOR =
            REGISTRATE.blockEntity("generator_large_induction_rotor", LargeInductionRotorBlockEntity::new)
                    .validBlock(ModdedBlocks.GENERATOR_LARGE_INDUCTION_ROTOR)
                    .renderer(() -> RotorRenderer::new)
                    .register();

    public static final BlockEntityEntry<CommutatorBlockEntity> GENERATOR_COMMUTATOR =
            REGISTRATE.blockEntity("generator_commutator", CommutatorBlockEntity::new)
                    .validBlocks(ModdedBlocks.GENERATOR_COMMUTATOR, ModdedBlocks.GENERATOR_VERTICAL_COMMUTATOR)
                    .renderer(() -> CommutatorRenderer::new)
                    .register();

    public static final BlockEntityEntry<GeneratorClutchBlockEntity> GENERATOR_CLUTCH =
            REGISTRATE.blockEntity("generator_clutch", GeneratorClutchBlockEntity::new)
                    .validBlock(ModdedBlocks.GENERATOR_CLUTCH)
                    .renderer(() -> GeneratorClutchRenderer::new)
                    .register();

    public static final BlockEntityEntry<VariacBlockEntity> VARIAC =
            REGISTRATE.blockEntity("variac", VariacBlockEntity::new)
                    .validBlock(ModdedBlocks.VARIAC)
                    .renderer(() -> TunedBlockRenderer::new)
                    .register();

    public static final BlockEntityEntry<ElectricMotorBlockEntity> ELECTRIC_MOTOR =
            REGISTRATE.blockEntity("electric_motor", ElectricMotorBlockEntity::new)
                    .validBlock(ModdedBlocks.ELECTRIC_MOTOR)
                    .renderer(() -> ElectricMotorRenderer::new)
                    .register();

    public static final BlockEntityEntry<ConstantSpeedMotorBlockEntity> CONSTANT_SPEED_MOTOR =
            REGISTRATE.blockEntity("constant_speed_motor", ConstantSpeedMotorBlockEntity::new)
                    .validBlock(ModdedBlocks.CONSTANT_SPEED_MOTOR)
                    .renderer(() -> ElectricMotorRenderer::new)
                    .register();

    public static final BlockEntityEntry<ServoBlockEntity> SERVO =
            REGISTRATE.blockEntity("servo", ServoBlockEntity::new)
                    .validBlock(ModdedBlocks.SERVO)
                    .renderer(() -> ServoRenderer::new)
                    .register();

    public static final BlockEntityEntry<RheostatBlockEntity> RHEOSTAT =
            REGISTRATE.blockEntity("rheostat", RheostatBlockEntity::new)
                    .validBlock(ModdedBlocks.RHEOSTAT)
                    .renderer(() -> TunedBlockRenderer::new)
                    .register();

    // Phase P6
    public static final BlockEntityEntry<HeaterBlockEntity> HEATING_COIL =
            REGISTRATE.blockEntity("heating_coil", HeaterBlockEntity::new)
                    .validBlock(ModdedBlocks.HEATING_COIL)
                    .register();

    public static final BlockEntityEntry<BasinHeaterBlockEntity> BASIN_HEATER =
            REGISTRATE.blockEntity("basin_heater", BasinHeaterBlockEntity::new)
                    .validBlock(ModdedBlocks.BASIN_HEATER)
                    .register();

    public static final BlockEntityEntry<LightFixtureBlockEntity> LIGHT_FIXTURE =
            REGISTRATE.blockEntity("light_fixture", LightFixtureBlockEntity::new)
                    .validBlock(ModdedBlocks.LIGHT_FIXTURE)
                    .renderer(() -> LightFixtureRenderer::new)
                    .register();

    public static final BlockEntityEntry<FactoryLightBlockEntity> FACTORY_LIGHT =
            REGISTRATE.blockEntity("factory_light", FactoryLightBlockEntity::new)
                    .validBlock(ModdedBlocks.FACTORY_LIGHT)
                    .renderer(() -> FactoryLightRenderer::new)
                    .register();

    public static final BlockEntityEntry<ElectricFanBlockEntity> ELECTRIC_FAN =
            REGISTRATE.blockEntity("electric_fan", ElectricFanBlockEntity::new)
                    .validBlock(ModdedBlocks.ELECTRIC_FAN)
                    .renderer(() -> ElectricFanRenderer::new)
                    .register();

    public static final BlockEntityEntry<ElectricPumpBlockEntity> ELECTRIC_PUMP =
            REGISTRATE.blockEntity("electric_pump", ElectricPumpBlockEntity::new)
                    .validBlock(ModdedBlocks.ELECTRIC_PUMP)
                    .register();

    public static final BlockEntityEntry<SolarPanelBlockEntity> SOLAR_PANEL =
            REGISTRATE.blockEntity("solar_panel", SolarPanelBlockEntity::new)
                    .validBlock(ModdedBlocks.SOLAR_PANEL)
                    .register();

    // Port: upstream's Flywheel 1.0 SolarPanelBearingVisual is not ported (E3); the renderer draws the rotor
    public static final BlockEntityEntry<SolarPanelBearingBlockEntity> SOLAR_PANEL_BEARING =
            REGISTRATE.blockEntity("solar_panel_bearing", SolarPanelBearingBlockEntity::new)
                    .validBlock(ModdedBlocks.SOLAR_PANEL_BEARING)
                    .renderer(() -> SolarPanelBearingRenderer::new)
                    .register();

    // Phase P7
    public static final BlockEntityEntry<org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryBlockEntity> PORTABLE_BATTERY =
            REGISTRATE.blockEntity("portable_battery", org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryBlockEntity::new)
                    .validBlock(ModdedBlocks.PORTABLE_BATTERY)
                    .register();

    // Phase P8. Upstream's Flywheel 1.0 ShaftVisual on the plotter and punch card reader is not ported (E3): their
    // renderers draw the shaft themselves.
    public static final BlockEntityEntry<org.patryk3211.powergrid.kinetics.plotter.PlotterBlockEntity> PLOTTER =
            REGISTRATE.blockEntity("plotter", org.patryk3211.powergrid.kinetics.plotter.PlotterBlockEntity::new)
                    .validBlock(ModdedBlocks.PLOTTER)
                    .renderer(() -> org.patryk3211.powergrid.kinetics.plotter.PlotterRenderer::new)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.electricity.bell.AlarmBellBlockEntity> ALARM_BELL =
            REGISTRATE.blockEntity("alarm_bell", org.patryk3211.powergrid.electricity.bell.AlarmBellBlockEntity::new)
                    .validBlock(ModdedBlocks.ALARM_BELL)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.electricity.crt.CRTBlockEntity> CRT =
            REGISTRATE.blockEntity("crt", org.patryk3211.powergrid.electricity.crt.CRTBlockEntity::new)
                    .validBlocks(ModdedBlocks.CRT, ModdedBlocks.ANDESITE_CRT, ModdedBlocks.BRASS_CRT)
                    .renderer(() -> org.patryk3211.powergrid.electricity.crt.CRTRenderer::new)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity> PUNCH_CARD_READER =
            REGISTRATE.blockEntity("punch_card_reader", SubstituteBlockEntityProvider.INSTANCE.get(org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity.class))
                    .validBlock(ModdedBlocks.PUNCH_CARD_READER)
                    .renderer(() -> org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderRenderer::new)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity> MODULAR_DISPLAY =
            REGISTRATE.<org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity>blockEntity("modular_display", org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntity::new)
                    .validBlock(ModdedBlocks.MODULAR_DISPLAY)
                    .renderer(() -> org.patryk3211.powergrid.electricity.modulardisplay.ModularDisplayBlockEntityRenderer::new)
                    .register();

    // Phase P9a
    public static final BlockEntityEntry<org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity> CIRCUIT_BOARD =
            REGISTRATE.blockEntity("circuit_board", org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity::new)
                    .validBlock(ModdedBlocks.CIRCUIT_BOARD)
                    .renderer(() -> org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardRenderer::new)
                    .register();

    // Phase P9b
    public static final BlockEntityEntry<org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlockEntity> CIRCUIT_DESIGN_TABLE =
            REGISTRATE.blockEntity("circuit_design_table", org.patryk3211.powergrid.circuits.editor.CircuitDesignTableBlockEntity::new)
                    .validBlock(ModdedBlocks.CIRCUIT_DESIGN_TABLE)
                    .renderer(() -> org.patryk3211.powergrid.circuits.editor.CircuitDesignTableRenderer::new)
                    .register();

    // Phase P10 (ceiling tile attachments)
    public static final BlockEntityEntry<org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlockEntity> CEILING_TILE_LAMP =
            REGISTRATE.blockEntity("ceiling_tile_lamp", org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampBlockEntity::new)
                    .validBlock(ModdedBlocks.CEILING_TILE_LAMP)
                    .renderer(() -> org.patryk3211.powergrid.general.ceilingtile.lamp.CeilingTileLampRenderer::new)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.general.ceilingtile.wire.CeilingTileConnectorBlockEntity> CEILING_TILE_CONNECTOR =
            REGISTRATE.blockEntity("ceiling_tile_connector", org.patryk3211.powergrid.general.ceilingtile.wire.CeilingTileConnectorBlockEntity::new)
                    .validBlock(ModdedBlocks.CEILING_TILE_CONNECTOR)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.general.ceilingtile.junction.CeilingTileJunctionBlockEntity> CEILING_TILE_JUNCTION =
            REGISTRATE.blockEntity("ceiling_tile_junction", org.patryk3211.powergrid.general.ceilingtile.junction.CeilingTileJunctionBlockEntity::new)
                    .validBlock(ModdedBlocks.CEILING_TILE_JUNCTION)
                    .register();

    public static final BlockEntityEntry<org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlockEntity> CEILING_TILE_SOLAR =
            REGISTRATE.blockEntity("ceiling_tile_solar", org.patryk3211.powergrid.general.ceilingtile.solar.CeilingTileSolarBlockEntity::new)
                    .validBlock(ModdedBlocks.CEILING_TILE_SOLAR)
                    .register();

    public static void register() { /* Initialize static fields. */ }
}
