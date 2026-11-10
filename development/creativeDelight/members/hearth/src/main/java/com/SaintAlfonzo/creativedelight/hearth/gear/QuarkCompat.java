package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
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
 * dispenser plants from a pouch. Quark's capacity and its Shift Range planting area are unchanged.
 */
public final class QuarkCompat {
    /** 3x3 in front of the dispenser, as a square centred two blocks out on a horizontal facing. */
    private static final int DISPENSE_RADIUS = 1;

    private QuarkCompat() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(QuarkCompat::onSimpleHarvest);
        MinecraftForge.EVENT_BUS.addListener(QuarkCompat::onPickup);
    }

    public static void registerDispenser() {
        DispenserBlock.registerBehavior(SeedPouchModule.seed_pouch, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack pouch) {
                dispense(source, pouch);
                return pouch;
            }
        });
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

    private static void dispense(BlockSource source, ItemStack pouch) {
        Pair<ItemStack, Integer> contents = SeedPouchItem.getContents(pouch);
        if (contents == null || !(contents.getLeft().getItem() instanceof BlockItem item) || !(item.getBlock() instanceof IPlantable)) {
            return;
        }
        ServerLevel level = source.getLevel();
        Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos center = source.getPos().relative(facing, facing.getAxis().isHorizontal() ? DISPENSE_RADIUS + 1 : 1);
        int left = contents.getRight();
        for (int dx = -DISPENSE_RADIUS; dx <= DISPENSE_RADIUS && left > 0; dx++) {
            for (int dz = -DISPENSE_RADIUS; dz <= DISPENSE_RADIUS && left > 0; dz++) {
                BlockPos target = plantSpot(level, center.offset(dx, 0, dz), item);
                if (target != null) {
                    level.setBlock(target, item.getBlock().defaultBlockState(), 3);
                    left--;
                }
            }
        }
        if (left != contents.getRight()) {
            SeedPouchItem.setCount(pouch, left);
        }
    }

    /** The air block at this column that the plant survives on, looking one block up and down for a dispenser above farmland. */
    private static BlockPos plantSpot(ServerLevel level, BlockPos pos, BlockItem item) {
        for (int y = 1; y >= -1; y--) {
            BlockPos spot = pos.above(y);
            BlockState plant = item.getBlock().defaultBlockState();
            if (level.isEmptyBlock(spot) && plant.canSurvive(level, spot)) {
                return spot;
            }
        }
        return null;
    }

    static boolean isPouch(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem()) != null && stack.is(SeedPouchModule.seed_pouch);
    }
}
