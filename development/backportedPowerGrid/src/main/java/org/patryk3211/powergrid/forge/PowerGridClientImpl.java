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
// Port state: complete since P10 (the upstream copy is deleted). Since P3: particle factories; since P9a: circuit
// board models. Since P7: the battery armor layer and the electro-zapper render handler.
package org.patryk3211.powergrid.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.patryk3211.powergrid.PowerGridClient;
import org.patryk3211.powergrid.collections.forge.ModdedKeysImpl;
import org.patryk3211.powergrid.collections.forge.ModdedParticlesImpl;
import org.patryk3211.powergrid.electricity.wire.forge.WirePreviewImpl;
import org.patryk3211.powergrid.ponder.PowerGridPonderPlugin;

@OnlyIn(Dist.CLIENT)
public class PowerGridClientImpl {
    public static void init() {
        PowerGridClient.initClient();

        // Mod specific bus
        PowerGridImpl.bus.register(PowerGridClientImpl.class);

        // Main forge event bus
        MinecraftForge.EVENT_BUS.register(ForgeClientEvents.class);
        MinecraftForge.EVENT_BUS.register(WirePreviewImpl.class);
        // Phase P7
        PowerGridClient.ELECTRO_ZAPPER_RENDER_HANDLER.registerListeners(MinecraftForge.EVENT_BUS);
    }

    // P5: ponder scenes and tags into Create 0.5.1's PonderRegistry (upstream: PonderIndex.addPlugin in initClient).
    // On the main thread: Create's own PonderIndex.register fills the same maps from its client setup.
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(PowerGridPonderPlugin::register);
    }

    @SubscribeEvent
    public static void particleManagerRegistration(RegisterParticleProvidersEvent event) {
        ModdedParticlesImpl.registerFactories(event);
    }

    // Phase P7: the portable battery drawn on the back of every humanoid renderer (as Create's backtank layer)
    @SubscribeEvent
    public static void addEntityLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
        var dispatcher = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher();
        org.patryk3211.powergrid.electricity.portablebattery.forge.BatteryArmorLayerImpl.registerOnAll(dispatcher);
    }

    @SubscribeEvent
    public static void keyRegistration(RegisterKeyMappingsEvent event) {
        ModdedKeysImpl.register(event);
    }

    // Phase P9a: the component models drawn on circuit boards, the board's base model and its custom loader
    // ("loader": "powergrid:circuit_board" in models/circuit_board.json).
    @SubscribeEvent
    public static void modelRequestLoad(net.minecraftforge.client.event.ModelEvent.RegisterAdditional event) {
        if(org.patryk3211.powergrid.PortPhase.CURRENT < 9.1)
            return;
        org.patryk3211.powergrid.circuits.components.ComponentModels.collectRawIds().forEach(event::register);
        event.register(org.patryk3211.powergrid.circuits.components.forge.CircuitBoardModel.BASE_MODEL);
    }

    @SubscribeEvent
    public static void modelLoaders(net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders event) {
        event.register("circuit_board", new org.patryk3211.powergrid.circuits.components.forge.CircuitBoardModelLoader());
    }
}
