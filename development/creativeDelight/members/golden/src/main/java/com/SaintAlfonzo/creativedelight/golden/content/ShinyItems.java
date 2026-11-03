package com.SaintAlfonzo.creativedelight.golden.content;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;

/** The enchanted forms that keep a vanilla item class's use (cake block, stew, milk, honey) and always show the glint. */
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

    public static class Bowl extends BowlFoodItem {
        public Bowl(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }

    public static class Milk extends MilkBucketItem {
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
