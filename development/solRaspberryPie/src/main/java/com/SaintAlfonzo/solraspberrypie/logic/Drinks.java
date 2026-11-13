package com.SaintAlfonzo.solraspberrypie.logic;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.RpData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Drinks are out of the food diet (7): 0 diversity, no ageing of other foods, no foods-eaten count, not in the Diet
 * Log. They are remembered in the Liquid Diet Log (unique drinks, last 256), which gives nothing.
 * Source: the item tag solraspberrypie:drinks (the pack's drink tags plus potions and milk).
 */
public final class Drinks {
    public static final TagKey<Item> TAG = ItemTags.create(new ResourceLocation(SolRaspberryPie.MOD_ID, "drinks"));

    public static boolean isDrink(Item item) {
        return item != null && new ItemStack(item).is(TAG);
    }

    public static void onDrink(ServerPlayer player, ItemStack stack) {
        if (!stack.is(TAG)) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return;
        RpData.get(player).addDrink(id);
        DietEngine.sync(player);
    }

    private Drinks() {}
}
