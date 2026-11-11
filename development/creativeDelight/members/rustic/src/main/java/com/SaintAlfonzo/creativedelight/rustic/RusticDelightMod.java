package com.SaintAlfonzo.creativedelight.rustic;

import com.SaintAlfonzo.creativedelight.rustic.client.RDClient;
import com.SaintAlfonzo.creativedelight.rustic.logic.EnabledCondition;
import com.SaintAlfonzo.creativedelight.rustic.logic.RDEvents;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDBlocks;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDItems;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDLoot;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDPotions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rustic Delight 1.7.0 ported to Forge 1.19.2, a Creative Delight member (feature_writeup/rustic_delight.md 5). All code
 * here is our own, written from the mod's behaviour (GPL-3.0-only); upstream's art and data are baked into this jar's
 * resources by tools/bake_upstream.py (MIT, PhantomWing; NOTICE-upstream-MIT.txt).
 */
@Mod(RusticDelightMod.MODID)
public class RusticDelightMod {
    public static final String MODID = "rusticdelight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public RusticDelightMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        RDBlocks.BLOCKS.register(bus);
        RDItems.ITEMS.register(bus);
        RDPotions.POTIONS.register(bus);
        RDLoot.MODIFIERS.register(bus);
        RDLoot.PLACEMENTS.register(bus);
        CraftingHelper.register(EnabledCondition.Serializer.INSTANCE);
        bus.addListener(RDEvents::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(RDEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(RDEvents::onVillagerTrades);
        MinecraftForge.EVENT_BUS.addListener(RDEvents::onWandererTrades);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RDClient.register(bus));
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
