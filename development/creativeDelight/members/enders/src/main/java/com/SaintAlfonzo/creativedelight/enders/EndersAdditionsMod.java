package com.SaintAlfonzo.creativedelight.enders;

import com.SaintAlfonzo.creativedelight.enders.block.EABlocks;
import com.SaintAlfonzo.creativedelight.enders.client.EAClient;
import com.SaintAlfonzo.creativedelight.enders.item.EAItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Ender's Delight 1.1.4's new End plants, items and dishes rebuilt for Forge 1.19.2, a Creative Delight member. The code
 * is our own (GNU GPL3), written from the 1.1.4 behaviour; the art and data are the MIT 1.1.4 files
 * (NOTICE-upstream-MIT.txt). Everything registers under Ender's Delight's namespace ({@link #NAMESPACE}), so the ids match
 * upstream 1.1.4; Ender's Delight itself stays its own jar.
 */
@Mod(EndersAdditionsMod.MODID)
public class EndersAdditionsMod {
    public static final String MODID = "enders_additions";
    /** Registry namespace of every block and item here: Ender's Delight's mod id. */
    public static final String NAMESPACE = "endersdelight";

    public EndersAdditionsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EAConfig.SPEC, MODID + "-common.toml");
        EABlocks.BLOCKS.register(bus);
        EAItems.ITEMS.register(bus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EAClient.register(bus));
    }
}
