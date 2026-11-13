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
// Port: upstream reads wire types from a 1.20 datapack registry (powergrid:wire_types, synced to clients). Forge 1.19.2
// has no datapack registry event, so the same files (data/<namespace>/powergrid/wire_types/<item path>.json, same codec)
// are read by a JSON reload listener (WireTypeReloadListener) and sent to clients by WireTypesSyncS2CPacket on join and
// after /reload. Server and client keep separate tables (an integrated server shares the class with its client).
package org.patryk3211.powergrid.electricity.wire.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class WireRegistry {
    /** Data folder of the wire type files, as upstream's registry key {@code powergrid:wire_types}. */
    public static final String DIRECTORY = "powergrid/wire_types";

    private static volatile Map<ResourceLocation, WireItemEntry> serverEntries = Collections.emptyMap();
    private static volatile Map<ResourceLocation, WireItemEntry> clientEntries = Collections.emptyMap();

    @OnlyIn(Dist.CLIENT)
    @Contract("null -> null")
    public static WireItemEntry forItem(Item item) {
        if(item == null)
            return null;
        var level = Minecraft.getInstance().level;
        if(level == null)
            return null;
        return forItem(level, item);
    }

    @Contract("_, null -> null")
    public static WireItemEntry forItem(@NotNull Level level, Item item) {
        if(item == null)
            return null;
        return (level.isClientSide ? clientEntries : serverEntries).get(getId(item));
    }

    public static ResourceLocation getId(@NotNull Item item) {
        return Registry.ITEM.getKey(item);
    }

    public static Map<ResourceLocation, WireItemEntry> serverEntries() {
        return serverEntries;
    }

    /** Set by the reload listener after a data load. */
    public static void setServerEntries(Map<ResourceLocation, WireItemEntry> entries) {
        serverEntries = Map.copyOf(entries);
    }

    /** Set by the sync packet on the client. */
    public static void setClientEntries(Map<ResourceLocation, WireItemEntry> entries) {
        clientEntries = Map.copyOf(entries);
    }
}
