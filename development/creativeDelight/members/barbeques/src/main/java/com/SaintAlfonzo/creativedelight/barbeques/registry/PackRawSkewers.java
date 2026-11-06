package com.SaintAlfonzo.creativedelight.barbeques.registry;

import com.SaintAlfonzo.creativedelight.barbeques.item.BBQSkewerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Raw versions of the eight pack skewers (plan_meals_rolls_bbq.md C3, barbeques_delight.md 4.3 #2): one raw item per
 * cooked skewer, under the cooked skewer's own namespace, registered only while that mod is loaded. Not edible, stack 64,
 * the stick (blaze rod for the Nether Skewer) as craft remainder, a {@link BBQSkewerItem} so powders season them and
 * the Grill carries the seasoning onto the cooked skewer as for the ten Barbeque's Delight lines. Recipes: CD
 * barbequesdelight/skewers.js (F.skewer). Models reuse the raw cod skewer texture until recolors (food/placeable #5).
 */
public final class PackRawSkewers {
    /** namespace -> raw item paths (each the cooked id with {@code raw_} in front) */
    private static final Map<String, String[]> RAWS = new LinkedHashMap<>();

    static {
        RAWS.put("farmersdelight", new String[]{"raw_barbecue_stick"});
        RAWS.put("ends_delight", new String[]{"raw_end_barbecue_stick"});
        RAWS.put("miners_delight", new String[]{"raw_improvised_barbecue_stick"});
        RAWS.put("nethersdelight", new String[]{"raw_nether_skewer"});
        RAWS.put("oceanic_delight", new String[]{"raw_seafood_skewer"});
        RAWS.put("veggiesdelight", new String[]{"raw_turnip_mutton_skewer"});
        RAWS.put("endersdelight", new String[]{"raw_crispy_skewer"});
        RAWS.put("hostiledelight", new String[]{"raw_caveskewer"});
    }

    private PackRawSkewers() {
    }

    public static void register(IEventBus bus) {
        RAWS.forEach((ns, paths) -> {
            if (!ModList.get().isLoaded(ns)) {
                return;
            }
            DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, ns);
            for (String path : paths) {
                Supplier<Item> remainder = "nethersdelight".equals(ns) ? () -> Items.BLAZE_ROD : () -> Items.STICK;
                items.register(path, () -> new BBQSkewerItem(ModItems.basicItem().craftRemainder(remainder.get())));
            }
            items.register(bus);
        });
    }
}
