package com.SaintAlfonzo.creativeengineering.trader;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Sells a random member of an item tag, picked again every time an offer is rolled, so one pool
 * entry stands for a whole group (golden foods, canned golden foods). The tag is read when the
 * offer is rolled, so /reload changes to it apply to traders spawned afterwards. An empty or
 * unknown tag gives no offer (vanilla skips a null offer).
 */
public class TagItemListing implements VillagerTrades.ItemListing {

    private final TagKey<Item> tag;
    private final int count;
    private final int emeralds;
    private final int maxUses;

    public TagItemListing(String tag, int count, int emeralds, int maxUses) {
        String id = tag.startsWith("#") ? tag.substring(1) : tag;
        this.tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(id));
        this.count = count;
        this.emeralds = emeralds;
        this.maxUses = maxUses;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        Optional<HolderSet.Named<Item>> members = Registry.ITEM.getTag(tag);
        if (members.isEmpty() || members.get().size() == 0) return null;
        Optional<Holder<Item>> pick = members.get().getRandomElement(random);
        if (pick.isEmpty()) return null;
        return new MerchantOffer(new ItemStack(Items.EMERALD, emeralds), new ItemStack(pick.get().value(), count),
                maxUses, 1, 0.05F);
    }

    @Override
    public String toString() {
        return "TagItemListing[#" + tag.location() + "]";
    }
}
