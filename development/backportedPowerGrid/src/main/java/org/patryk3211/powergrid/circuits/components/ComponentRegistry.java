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
// Port: upstream reads component_items from a 1.20 datapack registry (powergrid:component_items, synced to clients).
// Forge 1.19.2 has no datapack registry event, so the same files (data/<namespace>/powergrid/component_items/<component
// path>.json, same codec) are read by ReloadListener and sent to clients by ComponentItemsSyncS2CPacket on join and
// after /reload (as WireRegistry). Server and client keep separate tables (an integrated server shares the class).
package org.patryk3211.powergrid.circuits.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentRegistry {
    public static final ResourceKey<Registry<Component>> REGISTRY_KEY = ResourceKey.createRegistryKey(PowerGrid.asResource("components"));

    /** Data folder of the component item files, as upstream's registry key {@code powergrid:component_items}. */
    public static final String ITEM_DIRECTORY = "powergrid/component_items";
    public static final Codec<ComponentItemEntry> ITEM_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Registry.ITEM.byNameCodec().fieldOf("item").forGetter(ComponentItemEntry::item),
                    TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(ComponentItemEntry::tag)
            ).apply(instance, ComponentItemEntry::new));

    private static volatile Map<ResourceLocation, ComponentItemEntry> serverItems = Collections.emptyMap();
    private static volatile Map<ResourceLocation, ComponentItemEntry> clientItems = Collections.emptyMap();

    // @ExpectPlatform inlined (D2): forge implementation in ComponentRegistryImpl
    public static Iterable<Component> entries() {
        return org.patryk3211.powergrid.circuits.components.forge.ComponentRegistryImpl.entries();
    }

    private static Map<ResourceLocation, ComponentItemEntry> items(Level level) {
        return level.isClientSide ? clientItems : serverItems;
    }

    @Contract("_, null -> null")
    public static Item getItem(@NotNull Level level, Component component) {
        if(component == null)
            return null;
        var entry = items(level).get(getId(component));
        return entry == null ? null : entry.item();
    }

    @Contract("_, null -> null")
    public static Optional<TagKey<Item>> getItemTag(@NotNull Level level, Component component) {
        if(component == null)
            return null;
        var entry = items(level).get(getId(component));
        return entry == null ? Optional.empty() : entry.tag();
    }

    public static ResourceLocation getComponentId(@NotNull Level level, ItemStack stack) {
        for(var entry : items(level).entrySet()) {
            if(stack.is(entry.getValue().item()))
                return remapId(entry.getKey());
            var tag = entry.getValue().tag();
            if(tag.isPresent() && stack.is(tag.get()))
                return remapId(entry.getKey());
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static Item getItem(Component component) {
        if(component == null)
            return null;
        var entry = clientItems.get(getId(component));
        return entry == null ? null : entry.item();
    }

    @OnlyIn(Dist.CLIENT)
    public static Component getComponent(Item item) {
        for(var entry : clientItems.entrySet()) {
            if(entry.getValue().item() == item)
                return get(remapId(entry.getKey()));
            var tag = entry.getValue().tag();
            if(tag.isPresent() && item.builtInRegistryHolder().is(tag.get()))
                return get(remapId(entry.getKey()));
        }
        return null;
    }

    // Leftover component_items/electron_tube.json still maps create:electron_tube
    // after the component was renamed to triode.
    private static ResourceLocation remapId(ResourceLocation id) {
        if ("powergrid:electron_tube".equals(id.toString()))
            return PowerGrid.asResource("triode");
        return id;
    }

    @NotNull
    // @ExpectPlatform inlined (D2): forge implementation in ComponentRegistryImpl
    public static ResourceLocation getId(@NotNull Component component) {
        return org.patryk3211.powergrid.circuits.components.forge.ComponentRegistryImpl.getId(component);
    }

    @NotNull
    // @ExpectPlatform inlined (D2): forge implementation in ComponentRegistryImpl
    public static Component get(@NotNull ResourceLocation id) {
        return org.patryk3211.powergrid.circuits.components.forge.ComponentRegistryImpl.get(id);
    }

    // ---- Port (GPLv3): component_items reload listener and sync tables ----

    public static Map<ResourceLocation, ComponentItemEntry> serverItems() {
        return serverItems;
    }

    /** Set by the sync packet on the client. */
    public static void setClientItems(Map<ResourceLocation, ComponentItemEntry> entries) {
        clientItems = Map.copyOf(entries);
    }

    /** Reads data/<namespace>/powergrid/component_items/*.json on every data load (server side). */
    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public ReloadListener() {
            super(GSON, ITEM_DIRECTORY);
        }

        @Override
        protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, ComponentItemEntry> entries = new HashMap<>();
            files.forEach((id, json) -> ITEM_CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> PowerGrid.LOGGER.error("Failed to parse component item {}: {}", id, error))
                    .ifPresent(entry -> entries.put(id, entry)));
            serverItems = Map.copyOf(entries);
            PowerGrid.LOGGER.info("Loaded {} component items", entries.size());
        }
    }
}
