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
// The forge entry point, as upstream (the @ExpectPlatform methods of PowerGrid are inlined into it, D2). Port state:
// complete since P10 (the upstream copy is deleted). Not ported: datagen
// (gatherData; the jar ships upstream's generated output), the 1.20 datapack registries (wire_types: a reload listener,
// see WireRegistry; components P9; solar biomes P6), TFMG and Cold Sweat bridges (mods not in the pack).
package org.patryk3211.powergrid.forge;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.patryk3211.powergrid.electricity.heater.HeaterFanProcessingTypes;
import org.patryk3211.powergrid.collections.ModdedContraptions;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.item.TooltipHelper;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import org.patryk3211.powergrid.AbstractPowerGridRegistrate;
import org.patryk3211.powergrid.PortPhase;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.collections.ModdedEntities;
import org.patryk3211.powergrid.collections.ModdedPackets;
import org.patryk3211.powergrid.collections.forge.ModdedSoundEventsImpl;
import org.patryk3211.powergrid.commands.PerformanceCommand;
import org.patryk3211.powergrid.electricity.wire.registry.WireRegistry;
import org.patryk3211.powergrid.electricity.febridge.FEInverterBlockEntity;
import org.patryk3211.powergrid.electricity.febridge.forge.FEInverterBlockEntityImpl;
import org.patryk3211.powergrid.electricity.wire.registry.WireTypeReloadListener;
import org.patryk3211.powergrid.electricity.solarpanel.registry.SolarBiomeRegistry;
import org.patryk3211.powergrid.utility.proxy.SubstituteBlockEntityProvider;
import org.patryk3211.powergrid.network.packets.WireTypesSyncS2CPacket;

import java.util.List;

@Mod(PowerGrid.MOD_ID)
public class PowerGridImpl {
    public static ModLoadingContext context;
    public static IEventBus bus;

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, PowerGrid.MOD_ID);

    public PowerGridImpl() {
        context = ModLoadingContext.get();
        bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(PowerGridImpl.class);
        EventBuses.registerModEventBus(PowerGrid.MOD_ID, bus);

        // Port: upstream's Cold Sweat and TFMG bridges are not ported (mods not in the pack). Phase P8: the CC: Tweaked
        // bridge (peripherals of the gauges, energy meter, battery, clutch and redstone converter) when CC: Tweaked is
        // loaded (upstream: Architectury Platform.isModLoaded).
        if(PortPhase.CURRENT >= 8 && net.minecraftforge.fml.ModList.get().isLoaded("computercraft")) {
            org.patryk3211.powergrid.compat.cc.CCBridge.register();
        }
        // Phase P3: the forge FE inverter (Forge Energy capability) replaces the common block entity.
        // Phase P8: the forge punch card reader and the forge punch card menu constructors.
        SubstituteBlockEntityProvider.INSTANCE.register(org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity.class, org.patryk3211.powergrid.kinetics.punchcard.forge.PunchCardReaderBlockEntityImpl::new);
        SubstituteBlockEntityProvider.INSTANCE.register(FEInverterBlockEntity.class, FEInverterBlockEntityImpl::new);
        org.patryk3211.powergrid.kinetics.punchcard.PunchCardMenu.CONSTRUCTORS = org.patryk3211.powergrid.kinetics.punchcard.forge.PunchCardMenuImpl.constructors();
        PowerGrid.init();

        COMMAND_ARGUMENT_TYPES.register("performance_counter", () -> ArgumentTypeInfos
                .registerByClass(PerformanceCommand.PerformanceCounterArgument.class,
                        SingletonArgumentInfo.contextFree(PerformanceCommand.PerformanceCounterArgument::new)));
        COMMAND_ARGUMENT_TYPES.register(bus);

        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
        MinecraftForge.EVENT_BUS.addListener(PowerGridImpl::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(PowerGridImpl::datapackSync);

        // Client init
        EnvExecutor.runInEnv(Env.CLIENT, () -> PowerGridClientImpl::init);
    }

    // Phase P6: heating coil fan processing types and the solar panel contraption type, on the main thread after
    // Create's own (FanProcessingTypeRegistry and ContraptionType.ENTRIES are plain collections; upstream registers
    // both through Create 6 registries).
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if(PortPhase.CURRENT >= 6) {
            event.enqueueWork(() -> {
                HeaterFanProcessingTypes.register();
                ModdedContraptions.register();
                PowerGrid.registerBlockMovementChecks();
            });
        }
        // Phase P7: the lightning rod movement behaviour (lightning attractor), same reason
        if(PortPhase.CURRENT >= 7)
            event.enqueueWork(PowerGrid::registerMovementBehaviours);
    }

    @SubscribeEvent
    public static void configLoad(ModConfigEvent.Loading event) {
        ModdedConfigs.onLoad(event.getConfig());
    }

    @SubscribeEvent
    public static void configReload(ModConfigEvent.Reloading event) {
        ModdedConfigs.onReload(event.getConfig());
    }

    @SubscribeEvent
    public static void soundEventRegister(RegisterEvent event) {
        event.register(Registry.SOUND_EVENT_REGISTRY, ModdedSoundEventsImpl::register);
    }

    @SubscribeEvent
    public static void imcEnqueue(InterModEnqueueEvent event) {
        // Upstream's list; the generator block entities (clutch, induction rotor, commutator) since phase P4.
        var forbiddenBlockEntities = java.util.List.of(
                ModdedBlockEntities.GENERATOR_CLUTCH,
                ModdedBlockEntities.GENERATOR_INDUCTION_ROTOR,
                ModdedBlockEntities.GENERATOR_COMMUTATOR,
                ModdedBlockEntities.WINDING,
                ModdedBlockEntities.TRANSFORMER_MEDIUM,
                ModdedBlockEntities.HV_SWITCH,
                ModdedBlockEntities.DEVICE_CONNECTOR,
                ModdedBlockEntities.MULTIBLOCK_BATTERY,
                ModdedBlockEntities.CARBON_PILE,
                ModdedBlockEntities.CARBON_PILE_COIL
        );
        forbiddenBlockEntities.stream()
                .map(entry -> entry.getId().toString())
                .forEach(id -> InterModComms.sendTo("carryon", "blacklistBlock", () -> id));
        var forbiddenEntities = List.of(
                ModdedEntities.BLOCK_WIRE,
                ModdedEntities.HANGING_WIRE,
                ModdedEntities.CORD_ENTITY,
                ModdedEntities.STRING_LIGHT_CORD, // P10
                ModdedEntities.ZAP_PROJECTILE // P7
        );
        forbiddenEntities.stream()
                .map(entry -> entry.getId().toString())
                .forEach(id -> InterModComms.sendTo("carryon", "blacklistEntity", () -> id));
    }

    // Port: wire_types reload listener and its sync (WireRegistry).
    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WireTypeReloadListener());
        // Phase P9a: component_items (upstream: a 1.20 datapack registry; ComponentRegistry)
        if(PortPhase.CURRENT >= 9.1)
            event.addListener(new org.patryk3211.powergrid.circuits.components.ComponentRegistry.ReloadListener());
        // Phase P6: solar_biome_override (upstream: a 1.20 datapack registry; SolarBiomeRegistry)
        if(PortPhase.CURRENT >= 6)
            event.addListener(new SolarBiomeRegistry.ReloadListener());
    }

    private static void datapackSync(OnDatapackSyncEvent event) {
        var packet = new WireTypesSyncS2CPacket(WireRegistry.serverEntries());
        ServerPlayer player = event.getPlayer();
        if(player != null)
            ModdedPackets.sendToClient(packet, player);
        else
            ModdedPackets.sendToClients(packet, event.getPlayerList().getPlayers());
        // Phase P9a
        if(PortPhase.CURRENT >= 9.1) {
            var components = new org.patryk3211.powergrid.network.packets.ComponentItemsSyncS2CPacket(
                    org.patryk3211.powergrid.circuits.components.ComponentRegistry.serverItems());
            if(player != null)
                ModdedPackets.sendToClient(components, player);
            else
                ModdedPackets.sendToClients(components, event.getPlayerList().getPlayers());
        }
    }

    // Phase P9a: the circuit component registry (Forge registry; upstream: NewRegistryEvent with RegistryBuilder.of).
    // Created in every phase so the key is stable; empty before P9a.
    @SubscribeEvent
    public static void newRegistry(net.minecraftforge.registries.NewRegistryEvent event) {
        event.create(new net.minecraftforge.registries.RegistryBuilder<org.patryk3211.powergrid.circuits.components.Component>()
                        .setName(org.patryk3211.powergrid.circuits.components.ComponentRegistry.REGISTRY_KEY.location()),
                registry -> org.patryk3211.powergrid.circuits.components.forge.ComponentRegistryImpl.REGISTRY = registry);
    }

    public static AbstractPowerGridRegistrate createRegistrate() {
        var registrate = ForgePowerGridRegistrate.create(PowerGrid.MOD_ID)
                .setTooltipModifierFactory(item ->
                        new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                                .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                                .andThen(TooltipModifier.mapNull(ElectricProperties.create(item)))
                );
        // Port: 1.19.2 items join a tab through Registrate's default tab (upstream: a 1.20 tab with displayItems).
        if(PortPhase.CURRENT >= PortPhase.FIRST_CONTENT_PHASE)
            registrate.creativeModeTab(PowerGridCreativeTab::create);
        return registrate;
    }

    public static void finalizeRegistrate() {
        ((ForgePowerGridRegistrate) PowerGrid.REGISTRATE).registerEventListeners(bus);
    }
}
