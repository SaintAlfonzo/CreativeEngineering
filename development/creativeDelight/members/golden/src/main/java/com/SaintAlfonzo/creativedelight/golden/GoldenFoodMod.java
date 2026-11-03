package com.SaintAlfonzo.creativedelight.golden;

import com.SaintAlfonzo.creativedelight.golden.registry.GoldenBlocks;
import com.SaintAlfonzo.creativedelight.golden.registry.GoldenItems;
import com.SaintAlfonzo.creativedelight.golden.shared.BuiltInPacks;
import com.SaintAlfonzo.creativedelight.golden.upstream.GoldenTranslator;
import com.SaintAlfonzo.creativedelight.golden.upstream.UpstreamJars;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Golden Food 2.0.0 rebuilt as a Creative Delight member (feature_writeup/golden_food.md). All code here is our own,
 * written from the mod's behaviour; art and data come from the unmodified upstream jar in {@code goldenfood_upstream/} at
 * runtime (GoldenTranslator). Same mod id, item and block ids as upstream (including its three replacements of vanilla
 * items), so old worlds load as they are. Upstream's Golden Food Enchantment and its anvil recipe are removed (owner,
 * 2026-11-02): enchanted golden foods are made by the Creative Delight sequenced assembly (kubejs goldenfood/golden.js),
 * and the old enchantment id is in the swap list "removed" (data/creativedelight/swaps/removed.json).
 */
@Mod(GoldenFoodMod.MODID)
public class GoldenFoodMod {
    public static final String MODID = "goldenfood";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public GoldenFoodMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        GoldenBlocks.BLOCKS.register(bus);
        GoldenItems.ITEMS.register(bus);
        GoldenItems.VANILLA_ITEMS.register(bus);
        // Upstream art and data as built-in packs, always on and at the bottom (datapacks and KubeJS still override).
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "goldenfood_upstream",
                "Golden Food: art and data read from the upstream jar", GoldenTranslator::assets, GoldenTranslator::data));
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
