package com.SaintAlfonzo.creativeengineering.devtools;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * {@link WorldJeiDump} part: mobs a bucket picks up (Bucketable) and what bucket they become.
 *
 * <p>Every entity type is created in the level (never added to it, so nothing spawns) and dropped again. For each
 * {@link Bucketable} one entry: {@code entity}, {@code bucket_item} (what {@code getBucketItemStack()} returns),
 * {@code fluid} (the fluid a {@link BucketItem} result holds; the bucket the player uses is that fluid's bucket),
 * {@code empty_bucket} (that fluid's bucket item id), {@code class}. Types that can't be created are listed under
 * {@code failed}.
 */
final class WorldJeiMobBuckets {
    private static final Logger LOGGER = LogUtils.getLogger();

    private WorldJeiMobBuckets() {}

    static JsonElement dump(ServerLevel level) {
        JsonArray found = new JsonArray();
        JsonArray failed = new JsonArray();
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            String id = String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(type));
            Entity entity;
            try {
                entity = type.create(level);
            } catch (Throwable t) {
                failed.add(id);
                continue;
            }
            if (entity == null) continue;
            try {
                if (entity instanceof Bucketable bucketable) {
                    JsonObject o = new JsonObject();
                    o.addProperty("entity", id);
                    ItemStack stack = bucketable.getBucketItemStack();
                    o.addProperty("bucket_item", String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem())));
                    Fluid fluid = Fluids.EMPTY;
                    if (stack.getItem() instanceof BucketItem bucket) fluid = bucket.getFluid();
                    o.addProperty("fluid", String.valueOf(ForgeRegistries.FLUIDS.getKey(fluid)));
                    var used = fluid == Fluids.EMPTY ? Items.AIR : fluid.getBucket();
                    o.addProperty("empty_bucket", String.valueOf(ForgeRegistries.ITEMS.getKey(used)));
                    o.addProperty("class", entity.getClass().getName());
                    found.add(o);
                }
            } catch (Throwable t) {
                LOGGER.warn("[dumpworldjei] mob_buckets: {} failed: {}", id, t.toString());
                failed.add(id);
            } finally {
                entity.discard();
            }
        }
        JsonObject out = new JsonObject();
        out.add("bucketable", found);
        out.add("failed", failed);
        return out;
    }
}
