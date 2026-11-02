package com.SaintAlfonzo.creativedelight.oceanic;

import com.SaintAlfonzo.creativedelight.oceanic.logic.OceanicSetup;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicBlocks;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicEffects;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicItems;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicLootModifiers;
import com.SaintAlfonzo.creativedelight.oceanic.shared.BuiltInPacks;
import com.SaintAlfonzo.creativedelight.oceanic.upstream.OceanicTranslator;
import com.SaintAlfonzo.creativedelight.oceanic.upstream.UpstreamJars;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oceanic Delight 1.0.1 rebuilt as a Creative Delight member (feature_writeup/ocean_delight.md 5.6). All code here is our
 * own, written from the mod's behaviour; art and data come from the unmodified upstream jar in {@code oceanic_upstream/}
 * at runtime (OceanicTranslator). Same mod id, item, block, effect and potion ids as upstream, so old worlds load as
 * they are. The pack's fishing net rules (tags, tables) are Creative Delight data.
 */
@Mod(OceanicDelightMod.MODID)
public class OceanicDelightMod {
    public static final String MODID = "oceanic_delight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public OceanicDelightMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        OceanicBlocks.BLOCKS.register(bus);
        OceanicItems.ITEMS.register(bus);
        OceanicEffects.EFFECTS.register(bus);
        OceanicEffects.POTIONS.register(bus);
        OceanicLootModifiers.SERIALIZERS.register(bus);
        bus.addListener(OceanicSetup::commonSetup);
        // Upstream art and data as built-in packs, always on and at the bottom (datapacks and KubeJS still override).
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "oceanic_upstream",
                "Oceanic Delight: art and data read from the upstream jar", OceanicTranslator::assets, OceanicTranslator::data));
        MinecraftForge.EVENT_BUS.addListener(OceanicSetup::wanderingTrades);
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
