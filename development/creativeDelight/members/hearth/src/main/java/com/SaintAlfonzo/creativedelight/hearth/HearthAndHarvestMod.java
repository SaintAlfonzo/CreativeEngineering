package com.SaintAlfonzo.creativedelight.hearth;

import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import com.SaintAlfonzo.creativedelight.hearth.entity.FoxBerryGoals;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Hearth and Harvest 1.4.1 ported to Forge 1.19.2 piece by piece, a Creative Delight member (feature_writeup/
 * hearth_and_harvest.md 4). The code is our own (GNU GPL3), written from the 1.4.1 behaviour; the art and data are the
 * MIT 1.4.1 files (NOTICE-upstream-MIT.txt). Ids match upstream. Ported so far: the raspberry bush, Raspberry and
 * Raspberry Pips (4.1); the watering can and its tiers (4.0, moved from patcherMod).
 */
@Mod(HearthAndHarvestMod.MODID)
public class HearthAndHarvestMod {
    public static final String MODID = "hearthandharvest";

    public HearthAndHarvestMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        HHBlocks.BLOCKS.register(bus);
        HHItems.ITEMS.register(bus);
        MinecraftForge.EVENT_BUS.addListener(FoxBerryGoals::onEntityJoin);
    }
}
