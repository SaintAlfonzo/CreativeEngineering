package net.satisfy.foundation.rarity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Item to {@link FoundationRarity} lookup. Any mod using Foundation can register its items here,
 * the tooltip title is then recolored on the client ({@code FoundationClientForge}).
 */
public final class FoundationRarities {
    private static final Map<Item, FoundationRarity> RARITIES = new HashMap<>();

    private FoundationRarities() {
    }

    public static void register(ItemLike item, FoundationRarity rarity) {
        RARITIES.put(item.asItem(), rarity);
    }

    @Nullable
    public static FoundationRarity get(ItemStack stack) {
        return RARITIES.get(stack.getItem());
    }
}
