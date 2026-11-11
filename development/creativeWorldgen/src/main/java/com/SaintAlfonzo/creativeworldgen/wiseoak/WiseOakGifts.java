package com.SaintAlfonzo.creativeworldgen.wiseoak;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The gift a trusting Wise Oak can drop after a conversation (feature_writeup/wise_oak.md 4.7). One of the configured
 * categories is picked evenly, then one item in it. An unknown id is skipped with one warning, and a category left with
 * no known item is out of the draw. Lists are read on each gift, so a config reload applies at once.
 */
public final class WiseOakGifts {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> WARNED = new HashSet<>();

    private WiseOakGifts() {}

    private record Category(List<Item> items, int count) {}

    /** Called server side, after a conversation with an awake tree; {@code trust} already includes that conversation. */
    public static void roll(Level level, BlockPos pos, Direction face, int trust) {
        CWGConfig.WiseOak cfg = CWGConfig.WISE_OAK;
        if (trust < cfg.giftTrustThreshold.get()) return;
        if (level.random.nextDouble() * 100.0D >= cfg.giftChancePercent.get()) return;

        List<Category> categories = new ArrayList<>();
        add(categories, cfg.giftSaplings.get(), cfg.giftSaplingCount.get());
        add(categories, cfg.giftFruitSaplings.get(), cfg.giftFruitSaplingCount.get());
        add(categories, cfg.giftFruit.get(), cfg.giftFruitCount.get());
        add(categories, cfg.giftLeaves.get(), cfg.giftLeavesCount.get());
        if (categories.isEmpty()) return;

        Category category = categories.get(level.random.nextInt(categories.size()));
        Item item = category.items().get(level.random.nextInt(category.items().size()));
        int left = category.count();
        while (left > 0) {
            int stack = Math.min(left, item.getMaxStackSize());
            Block.popResourceFromFace(level, pos, face, new ItemStack(item, stack));
            left -= stack;
        }
    }

    private static void add(List<Category> categories, List<? extends String> ids, int count) {
        List<Item> items = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null && ForgeRegistries.ITEMS.containsKey(key)) {
                items.add(ForgeRegistries.ITEMS.getValue(key));
            } else if (WARNED.add(id)) {
                LOGGER.warn("Wise Oak gift list: unknown item {}", id);
            }
        }
        if (!items.isEmpty()) categories.add(new Category(items, count));
    }
}
