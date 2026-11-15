package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.quark.api.event.SimpleHarvestEvent;
import vazkii.quark.content.tools.item.SeedPouchItem;
import vazkii.quark.content.tools.module.SeedPouchModule;

/**
 * Everything that links to Quark; loaded only when Quark is present. The Farmer's Hat XP on simple harvest (D8), and the
 * two seed pouch additions of N23-13: seeds picked up top up a pouch on the hotbar that already holds them, and a
 * dispenser plants a 5x5 from a pouch (SeedContainerDispense). Quark's capacity and its Shift Range planting area are
 * unchanged. The pickup top-up does not double-count with Quark's own SeedPouchModule.onItemPickup: both shrink the same
 * ground stack, Quark's only fills a held pouch, and a full pouch takes nothing more.
 */
public final class QuarkCompat {
    private QuarkCompat() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(QuarkCompat::onSimpleHarvest);
        MinecraftForge.EVENT_BUS.addListener(QuarkCompat::onPickup);
    }

    /** The pouch plants a 5x5 (up to 25) through {@link SeedContainerDispense}; it stays in the dispenser. */
    public static void registerDispenser() {
        SeedContainerDispense.register(SeedPouchModule.seed_pouch, QuarkCompat::plantFromPouch);
    }

    private static void onSimpleHarvest(SimpleHarvestEvent event) {
        if (event.player != null && event.player.level instanceof ServerLevel level && FarmersHatEvents.wearing(event.player)
                && FarmersHatEvents.isRipe(event.blockState)) {
            FarmersHatEvents.reward(level, event.pos);
        }
    }

    private static void onPickup(EntityItemPickupEvent event) {
        ItemEntity entity = event.getItem();
        ItemStack ground = entity.getItem();
        if (ground.isEmpty()) {
            return;
        }
        Inventory inventory = event.getEntity().getInventory();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack pouch = inventory.getItem(slot);
            if (!pouch.is(SeedPouchModule.seed_pouch)) {
                continue;
            }
            Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(pouch);
            if (contents == null || !ItemStack.isSameItemSameTags(contents.getLeft(), ground)) {
                continue;
            }
            int moved = Math.min(ground.getCount(), SeedPouchModule.maxItems - contents.getRight());
            if (moved <= 0) {
                continue;
            }
            SeedPouchItem.setCount(pouch, contents.getRight() + moved);
            ground.shrink(moved);
            if (ground.isEmpty()) {
                entity.discard();
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean plantFromPouch(ServerLevel level, BlockPos centre, ItemStack pouch) {
        Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(pouch);
        if (contents == null || contents.getRight() <= 0 || !contents.getLeft().is(SeedPouchModule.seedPouchHoldableTag)) {
            return false;
        }
        int planted = SeedContainerDispense.plantPattern(level, centre, contents.getLeft(), contents.getRight());
        if (planted > 0) {
            // setCount at 0 clears the contents and keeps the empty pouch.
            SeedPouchItem.setCount(pouch, contents.getRight() - planted);
        }
        return planted > 0;
    }

    static boolean isPouch(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem()) != null && stack.is(SeedPouchModule.seed_pouch);
    }
}
