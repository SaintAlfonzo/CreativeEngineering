package com.sammy.minersdelight;

import com.sammy.minersdelight.client.MDClient;
import com.sammy.minersdelight.logic.CupConversions;
import com.sammy.minersdelight.logic.MDSetup;
import com.sammy.minersdelight.registry.MDBlockEntities;
import com.sammy.minersdelight.registry.MDBlocks;
import com.sammy.minersdelight.registry.MDItems;
import com.sammy.minersdelight.registry.MDLoot;
import com.sammy.minersdelight.registry.MDMenuTypes;
import com.sammy.minersdelight.registry.MDPotions;
import com.sammy.minersdelight.upstream.UpstreamJars;
import com.sammy.minersdelight.upstream.UpstreamPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miner's Delight 1.4.5 and 1.1.2 merged and ported to Forge 1.19.2 (feature_writeup/miners_delight.md). All code here
 * is our own, written from the mod's behaviour; art and data come from the unmodified upstream jar in
 * {@code miners_upstream/} at runtime.
 */
@Mod(MinersDelightMod.MODID)
public class MinersDelightMod {
    public static final String MODID = "miners_delight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public MinersDelightMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MDBlocks.BLOCKS.register(bus);
        MDItems.ITEMS.register(bus);
        MDBlockEntities.BLOCK_ENTITIES.register(bus);
        MDMenuTypes.MENU_TYPES.register(bus);
        MDPotions.POTIONS.register(bus);
        MDLoot.MODIFIERS.register(bus);
        MDLoot.CONDITIONS.register(bus);
        MDLoot.FUNCTIONS.register(bus);
        bus.addListener(UpstreamPacks::onAddPackFinders);
        bus.addListener(MDSetup::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(CupConversions::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(CupConversions::onDatapackSync);
        CupConversions.registerNetwork();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MDClient.register(bus));
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
