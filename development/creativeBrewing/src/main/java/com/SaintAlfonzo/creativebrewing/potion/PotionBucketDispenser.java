package com.SaintAlfonzo.creativebrewing.potion;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * kubejs/startup_scripts/items/potion_buckets.js registers the 3 potion-bucket items and
 * kubejs/startup_scripts/func/potion_area.js (global.potionArea.spawn) owns the actual
 * dome-of-AreaEffectClouds logic that a right-click bursts (see
 * kubejs/server_scripts/engineering/potion_buckets.js). KubeJS has no API to register a
 * {@code DispenserBlock} behavior, so without this class a dispenser loaded with a potion
 * bucket falls through to vanilla's default behavior - eject the bucket as a dropped item,
 * same as any other unrecognised item - instead of bursting the cloud.
 *
 * This class registers a real dispense behavior for each bucket id, but does NOT duplicate
 * potionArea's dome logic in Java: it forwards to whatever {@link SpawnHandler} KubeJS installs
 * via {@link #setHandler(SpawnHandler)}, so global.potionArea.spawn stays the single source of
 * truth for ring counts / radii / colours / effects. potion_area.js calls setHandler(...) once
 * at startup, passing a JS lambda that just calls back into global.potionArea.spawn.
 *
 * Owner (potion_buckets.md F6, {@code potions/potion-buckets #3}, decided 2026-11-02): a dispenser placed by a player
 * keeps that player's UUID in its block entity's Forge persistent data ({@link #onPlace}); a bucket it fires spawns its
 * clouds owned by that player while they are online in the same level, so kills credit them as for a thrown bucket.
 * Dispensers placed before this, or by no player, and owners who are offline fire owner-less clouds (vanilla).
 */
public final class PotionBucketDispenser {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MODID = CreativeBrewing.NS;

    private static final String[] BUCKET_IDS = {
            "potion_bucket",
            "splash_potion_bucket",
            "lingering_potion_bucket",
    };

    /** Installed by potion_area.js at startup - forwards into global.potionArea.spawn. */
    public interface SpawnHandler {
        void spawn(ServerLevel level, double x, double y, double z, String itemId, ItemStack stack, @Nullable LivingEntity owner);
    }

    /** Forge persistent-data key on the dispenser block entity: the UUID of the player who placed it. */
    static final String OWNER_KEY = CreativeBrewing.NS + ":DispenserOwner";

    /** Forge bus: remember who placed a dispenser (droppers too, harmless: they never use dispense behaviours). */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getLevel().isClientSide()) return;
        if (!(event.getPlacedBlock().getBlock() instanceof DispenserBlock)) return;
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (!(be instanceof DispenserBlockEntity)) return;
        be.getPersistentData().putUUID(OWNER_KEY, player.getUUID());
        be.setChanged();
    }

    /** The placing player if they are in this level now, else null. */
    @Nullable
    static Player owner(BlockSource source) {
        BlockEntity be = source.getEntity();
        if (be == null || !be.getPersistentData().hasUUID(OWNER_KEY)) return null;
        UUID id = be.getPersistentData().getUUID(OWNER_KEY);
        return source.getLevel().getPlayerByUUID(id);
    }

    private static volatile SpawnHandler HANDLER;

    private PotionBucketDispenser() {}

    /** Called from KubeJS (potion_area.js) once at startup. */
    public static void setHandler(SpawnHandler handler) {
        HANDLER = handler;
        LOGGER.info("[CE potions] potion bucket dispense handler installed from KubeJS");
    }

    /** Called from CreativeEngineering#commonSetup via event.enqueueWork - registries are full by then. */
    public static void register() {
        for (String path : BUCKET_IDS) {
            String fullId = MODID + ":" + path;
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(fullId));
            if (item == null) {
                LOGGER.warn("[CE potions] {} not found - skipping dispenser behavior (KubeJS item missing?)", fullId);
                continue;
            }
            DispenserBlock.registerBehavior(item, new BucketBehavior(fullId));
        }
        LOGGER.info("[CE potions] potion bucket dispenser behaviors registered");
    }

    private static final class BucketBehavior extends DefaultDispenseItemBehavior {
        private final String itemId;

        BucketBehavior(String itemId) {
            this.itemId = itemId;
        }

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            if (HANDLER == null) {
                LOGGER.warn("[CE potions] {} dispensed with no KubeJS handler installed - falling back to default eject behavior", itemId);
                return super.execute(source, stack);
            }

            ServerLevel level = source.getLevel();
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos target = source.getPos().relative(facing);
            double x = target.getX() + 0.5;
            double y = target.getY() + 0.1;
            double z = target.getZ() + 0.5;

            // buckets are .unstackable() in KubeJS (max stack size 1), so `stack` here is
            // always exactly the one bucket being dispensed - pass a copy for NBT (potion
            // contents), then hand back a plain empty bucket to replace it in the dispenser.
            HANDLER.spawn(level, x, y, z, itemId, stack.copy(), owner(source));
            return new ItemStack(Items.BUCKET);
        }
    }
}
