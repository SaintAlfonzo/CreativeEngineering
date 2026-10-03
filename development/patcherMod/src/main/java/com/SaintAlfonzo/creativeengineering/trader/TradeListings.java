package com.SaintAlfonzo.creativeengineering.trader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Java side of {@code global.trades} (kubejs/startup_scripts/func/trade_helper.js): builds trade
 * listings and finds existing ones by what they sell. KubeJS decides the content; this class only
 * does the parts Rhino can't do safely (catching every Throwable while sampling a foreign listing).
 */
public final class TradeListings {

    private static final Logger LOG = LogUtils.getLogger();

    private TradeListings() {}

    /** Emeralds for items, Forge's {@code BasicItemListing} at vanilla's 0.05 price multiplier. Null + a warning for an unknown id. */
    public static VillagerTrades.ItemListing item(String id, int count, int emeralds, int maxUses) {
        Item item = itemOrNull(id);
        if (item == null) {
            LOG.warn("[trades] unknown item '{}', listing skipped", id);
            return null;
        }
        return new BasicItemListing(emeralds, new ItemStack(item, count), maxUses, 1, 0.05F);
    }

    /** A random member of an item tag each time an offer is rolled ('#ns:path' or 'ns:path'). */
    public static VillagerTrades.ItemListing fromTag(String tag, int count, int emeralds, int maxUses) {
        return new TagItemListing(tag, count, emeralds, maxUses);
    }

    /**
     * Removes and returns every listing in {@code pool} whose sampled result matches one of
     * {@code matchers}: {@code 'ns:id'} for one item, {@code '@ns'} for a whole mod. {@code matchers}
     * may be a list (a JS array), an array or one comma-separated string.
     */
    public static List<VillagerTrades.ItemListing> take(List<VillagerTrades.ItemListing> pool, Object matchers) {
        List<String> ms = matcherList(matchers);
        List<VillagerTrades.ItemListing> out = new ArrayList<>();
        for (Iterator<VillagerTrades.ItemListing> it = pool.iterator(); it.hasNext(); ) {
            VillagerTrades.ItemListing listing = it.next();
            String id = resultId(listing);
            if (id != null && matches(id, ms)) {
                it.remove();
                out.add(listing);
            }
        }
        return out;
    }

    /**
     * The item id a listing sells, from one sample roll with no entity, or null when the listing
     * gives nothing or needs an entity. Every Throwable is caught: some mod listings throw on a
     * null entity, and Rhino can't catch {@code java.lang.Error}.
     */
    public static String resultId(VillagerTrades.ItemListing listing) {
        try {
            MerchantOffer offer = listing.getOffer(null, RandomSource.create(0));
            if (offer == null || offer.getResult().isEmpty()) return null;
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(offer.getResult().getItem());
            return key == null ? null : key.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    static Item itemOrNull(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) return null;
        return ForgeRegistries.ITEMS.getValue(rl);
    }

    private static boolean matches(String id, List<String> matchers) {
        String ns = id.substring(0, id.indexOf(':') + 1);
        for (String m : matchers) {
            if (m.startsWith("@") ? ns.equals(m.substring(1) + ":") : id.equals(m)) return true;
        }
        return false;
    }

    private static List<String> matcherList(Object matchers) {
        List<String> out = new ArrayList<>();
        if (matchers instanceof Collection<?> c) {
            for (Object o : c) out.add(String.valueOf(o).trim());
        } else if (matchers instanceof Object[] arr) {
            for (Object o : arr) out.add(String.valueOf(o).trim());
        } else if (matchers != null) {
            for (String s : String.valueOf(matchers).split(",")) out.add(s.trim());
        }
        out.removeIf(String::isEmpty);
        return out;
    }
}
