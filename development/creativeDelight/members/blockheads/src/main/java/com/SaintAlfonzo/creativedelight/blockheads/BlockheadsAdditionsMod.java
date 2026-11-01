package com.SaintAlfonzo.creativedelight.blockheads;

import com.SaintAlfonzo.creativedelight.blockheads.client.FridgeTopFill;
import com.SaintAlfonzo.creativedelight.blockheads.kitchen.CornerStorage;
import com.SaintAlfonzo.creativedelight.blockheads.kitchen.CounterToppers;
import com.SaintAlfonzo.creativedelight.blockheads.rack.BARacks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * The pack's Cooking for Blockheads additions and changes, a Creative Delight member (feature_writeup/kitchen.md 9).
 * Cooking for Blockheads (All Rights Reserved) stays its own jar; this is our own code and art only (GNU GPL3). New
 * blocks register under CfB's namespace ({@link #NAMESPACE}).
 * <ul>
 * <li>{@code rack}: spice racks and tool racks in 20 woods.</li>
 * <li>{@code kitchen}: corner storage, counter toppers (cutting board, plates, coaster on a counter); the oven fuel and
 * smoking mixins.</li>
 * <li>{@code client}: the 2-tall fridge filled to a full block.</li>
 * </ul>
 * The kitchen code lived in patcherMod until 2026-10-31; its saved data and tag ids keep their creativeengineering names.
 */
@Mod(BlockheadsAdditionsMod.MODID)
public class BlockheadsAdditionsMod {
    public static final String MODID = "blockheads_additions";
    /** Registry namespace of the blocks and items here: Cooking for Blockheads' mod id. */
    public static final String NAMESPACE = "cookingforblockheads";

    public BlockheadsAdditionsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BAConfig.SPEC, MODID + "-common.toml");
        BARacks.BLOCKS.register(bus);
        BARacks.ITEMS.register(bus);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(BARacks::allowOnBlockEntityTypes));
        MinecraftForge.EVENT_BUS.register(CornerStorage.class);
        // The per-state topper cache resets on tag reload.
        CounterToppers.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FridgeTopFill.register(bus));
    }
}
