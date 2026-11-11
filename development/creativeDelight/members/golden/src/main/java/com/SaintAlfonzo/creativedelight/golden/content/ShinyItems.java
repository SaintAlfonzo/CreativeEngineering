package com.SaintAlfonzo.creativedelight.golden.content;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** The enchanted forms that keep a vanilla item class's use (cake block, milk, honey) and always show the glint. */
public final class ShinyItems {
    private ShinyItems() {
    }

    public static class Block extends BlockItem {
        public Block(net.minecraft.world.level.block.Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }

    /**
     * The golden milk bucket. MilkBucketItem cures with its own stack, and Forge cures only effects whose curative items
     * hold that stack (a vanilla milk bucket by default), so a golden milk cleared nothing (SQ-GLD-03, r1103 sAR: Speed
     * stayed). It cures as a vanilla milk bucket first; as upstream, no food values.
     */
    public static class GoldenMilk extends MilkBucketItem {
        public GoldenMilk(Properties properties) {
            super(properties);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide) entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
            return super.finishUsingItem(stack, level, entity);
        }
    }

    public static class Milk extends GoldenMilk {
        public Milk(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }

    public static class Honey extends HoneyBottleItem {
        public Honey(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }
}
