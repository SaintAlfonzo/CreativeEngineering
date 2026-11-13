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
// Port state: phase P2 part. The portable drill left-click hook (P7) is not here: Forge 1.19.2 LeftClickBlock has no
// break action, so mixin/forge/DrillBreakActionMixin replaces it.
package org.patryk3211.powergrid.forge;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.wire.BaseWireEntity;

public class ForgeEvents {
    @SubscribeEvent
    public static void entityUnloadEvent(EntityLeaveLevelEvent event) {
        if(event.getLevel() instanceof ServerLevel world) {
            BaseWireEntity.entityUnload(event.getEntity(), world);
        }
    }

    @SubscribeEvent
    public static void chunkTicketUpdate(ChunkTicketLevelUpdatedEvent event) {
        if(event.getChunkHolder() == null)
            return;
        ElectricBehaviour.handleTicketChange(event.getNewTicketLevel(), event.getChunkHolder(), event.getOldTicketLevel());
    }

    @SubscribeEvent
    public static void chunkLoad(ChunkEvent.Load event) {
        var level = event.getLevel();
        if(level.isClientSide())
            return;
        var global = GlobalElectricNetworks.getWorldNetworks(level);
        if(global == null)
            return;
        global.chunkLoaded(event.getChunk().getPos());
    }

    // Phase P8 (port, GPLv3): records the clicking player's main hand item for the modular display's value box, before
    // Create 0.5.1's ValueSettingsInputHandler (NORMAL priority) asks it for a hit (see CustomValueBoxTransformer).
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void recordInteractingItem(PlayerInteractEvent.RightClickBlock event) {
        if(!event.getLevel().isClientSide())
            org.patryk3211.powergrid.electricity.modulardisplay.CustomValueBoxTransformer.SERVER_INTERACTING_ITEM.set(event.getEntity().getMainHandItem());
    }

}
