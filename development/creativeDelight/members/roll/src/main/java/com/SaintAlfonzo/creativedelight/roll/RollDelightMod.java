package com.SaintAlfonzo.creativedelight.roll;

import com.SaintAlfonzo.creativedelight.roll.client.RollClient;
import com.SaintAlfonzo.creativedelight.roll.logic.Chopsticks;
import com.SaintAlfonzo.creativedelight.roll.logic.FrogspawnShears;
import com.SaintAlfonzo.creativedelight.roll.registry.RollBlocks;
import com.SaintAlfonzo.creativedelight.roll.registry.RollItems;
import com.SaintAlfonzo.creativedelight.roll.shared.BuiltInPacks;
import com.SaintAlfonzo.creativedelight.roll.upstream.RollTranslator;
import com.SaintAlfonzo.creativedelight.roll.upstream.UpstreamJars;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Roll Delight 1.3.1 ported to Forge 1.19.2, a Creative Delight member (feature_writeup/roll_delight.md). All code here
 * is our own, written from the mod's behaviour; art comes from the unmodified upstream jar in {@code roll_upstream/} at
 * runtime. Recipes, tags, food types and compost are pack scripts (CD {@code roll_delight} script folders); upstream's
 * recipes are not loaded.
 */
@Mod(RollDelightMod.MODID)
public class RollDelightMod {
    public static final String MODID = "roll_delight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public RollDelightMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        RollBlocks.BLOCKS.register(bus);
        RollItems.ITEMS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RollConfig.SPEC, MODID + "-common.toml");
        // Upstream art as a built-in pack, always on and at the bottom; the data pack holds only our own loot tables.
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "roll_upstream",
                "Roll Delight: art read from the upstream jar", RollTranslator::assets, RollTranslator::data));
        MinecraftForge.EVENT_BUS.addListener(Chopsticks::onUseStart);
        MinecraftForge.EVENT_BUS.addListener(Chopsticks::onUseFinish);
        MinecraftForge.EVENT_BUS.addListener(Chopsticks::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(FrogspawnShears::onBreak);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> RollClient::register);
    }

    public static ResourceLocation path(String path) {
        return new ResourceLocation(MODID, path);
    }
}
