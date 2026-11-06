package com.SaintAlfonzo.creativedelight.largemeals;

import com.SaintAlfonzo.creativedelight.largemeals.registry.MealBlocks;
import com.SaintAlfonzo.creativedelight.largemeals.registry.MealItems;
import com.SaintAlfonzo.creativedelight.largemeals.shared.BuiltInPacks;
import com.SaintAlfonzo.creativedelight.largemeals.upstream.LargeMealsTranslator;
import com.SaintAlfonzo.creativedelight.largemeals.upstream.UpstreamJars;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.AddPackFindersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Large Meals 1.3.0 ported to Forge 1.19.2, a Creative Delight member (feature_writeup/large_meals.md). All code here
 * is our own, written from the mod's behaviour; art, block loot tables and the two sheep drop modifiers come from the
 * unmodified upstream jar in {@code largemeals_upstream/} at runtime. Recipes, tags, food types and compost are pack
 * scripts (CD {@code largemeals} script folders); upstream's recipes are not loaded.
 */
@Mod(LargeMealsMod.MODID)
public class LargeMealsMod {
    public static final String MODID = "largemeals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public LargeMealsMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MealBlocks.BLOCKS.register(bus);
        MealItems.ITEMS.register(bus);
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "largemeals_upstream",
                "Large Meals: art and loot read from the upstream jar", LargeMealsTranslator::assets, LargeMealsTranslator::data));
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
