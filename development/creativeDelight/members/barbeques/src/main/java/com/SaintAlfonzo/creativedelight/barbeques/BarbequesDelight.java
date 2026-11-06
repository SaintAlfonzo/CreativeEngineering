package com.SaintAlfonzo.creativedelight.barbeques;

import com.SaintAlfonzo.creativedelight.barbeques.block.BasinBlock;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlock;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQItems;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import com.SaintAlfonzo.creativedelight.barbeques.registry.PackRawSkewers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Barbeque's Delight 1.3.0 (Mao, lcy0x1; NeoForge 1.21.1, LGPL-2.1) ported to plain Forge 1.19.2, a Creative Delight
 * member (feature_writeup/barbeques_delight.md 4). The Registrate / L2 library code of 1.3.0 is replaced by ordinary
 * blocks, block entities, items and recipe serializers; the art, models and lang are 1.3.0's (NOTICE-upstream-LGPL.txt).
 * Ids match upstream.
 *
 * Port changes (owner decisions in barbeques_delight.md 0.1): the six butcher trades are not registered (O6); the grill
 * campfire follows vanilla campfire rules and the pack rain rule (O11, 3.4.1); a burnt skewer stops re-syncing its grill
 * (B1); sauces are refused on raw skewers (O28); the FD cutting board takes seasonings through an event, not a mixin;
 * the Jade grill tooltip is a WTHIT provider (O9). Recipes are emitted by the pack (global.NCH), not shipped as data.
 */
@Mod(BarbequesDelight.MODID)
public class BarbequesDelight {
    public static final String MODID = "barbequesdelight";

    public BarbequesDelight() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BBQBlocks.BLOCKS.register(bus);
        BBQBlocks.BLOCK_ENTITIES.register(bus);
        BBQItems.ITEMS.register(bus);
        PackRawSkewers.register(bus);
        BBQRecipes.SERIALIZERS.register(bus);
        BBQRecipes.TYPES.register(bus);
        // 1.3.0's cutting board seasoning use (a mixin, then a RightClickBlock listener) is retired at the seasoning fold
        // (food/seasoning #11): seasonings are crafted on
        MinecraftForge.EVENT_BUS.addListener(GrillBlock::onSplashWater);
        MinecraftForge.EVENT_BUS.addListener(BasinBlock::onSneakGrab);
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }
}
