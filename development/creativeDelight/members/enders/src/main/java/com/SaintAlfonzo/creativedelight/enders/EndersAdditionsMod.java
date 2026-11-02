package com.SaintAlfonzo.creativedelight.enders;

import com.SaintAlfonzo.creativedelight.enders.block.EABlocks;
import com.SaintAlfonzo.creativedelight.enders.client.EAClient;
import com.SaintAlfonzo.creativedelight.enders.item.EAItems;
import com.SaintAlfonzo.creativedelight.enders.shared.BuiltInPacks;
import com.SaintAlfonzo.creativedelight.enders.upstream.EndersTranslator;
import com.SaintAlfonzo.creativedelight.enders.upstream.UpstreamJars;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ender's Delight 1.1.4's new End plants, items and dishes rebuilt for Forge 1.19.2, a Creative Delight member. The code
 * is our own (GNU GPL3), written from the 1.1.4 behaviour. The art and data are read at runtime from the unmodified
 * MIT 1.1.4 jar in {@code enders_upstream/} (donor route, owner 2026-11-01; {@link UpstreamJars}, {@link EndersTranslator}),
 * so no upstream file ships in this jar. Everything registers under Ender's Delight's namespace ({@link #NAMESPACE}), so the ids match
 * upstream 1.1.4; Ender's Delight itself stays its own jar.
 */
@Mod(EndersAdditionsMod.MODID)
public class EndersAdditionsMod {
    public static final String MODID = "enders_additions";
    /** Registry namespace of every block and item here: Ender's Delight's mod id. */
    public static final String NAMESPACE = "endersdelight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public EndersAdditionsMod() {
        UpstreamJars.verifyOrFail();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // 1.1.4 art and data as built-in packs, always on and at the bottom: resource packs, KubeJS and datapacks still
        // override them. Only this member's own paths (EndersTranslator), none of which the installed 1.2.2 jar has.
        bus.addListener((AddPackFindersEvent event) -> BuiltInPacks.addUpstream(event, "enders_upstream",
                "Ender's Delight 1.1.4: art and data read from the upstream jar", EndersTranslator::assets, EndersTranslator::data));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EAConfig.SPEC, MODID + "-common.toml");
        EABlocks.BLOCKS.register(bus);
        EAItems.ITEMS.register(bus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EAClient.register(bus));
    }
}
