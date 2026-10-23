package net.mehdinoui.veggiesdelight;

import net.mehdinoui.veggiesdelight.common.registry.ModBlocks;
import net.mehdinoui.veggiesdelight.common.registry.ModItems;
import net.mehdinoui.veggiesdelight.common.world.VillageStructures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// Veggies Delight 1.9.2 ported to Forge 1.19.2 (feature_writeup/veggies_delight.md 4.1). Upstream's config, trades,
// compost hook and rarity placement modifier are not ported: the pack sets those through its own tools.
@Mod(VeggiesDelight.MOD_ID)
public class VeggiesDelight {
    public static final String MOD_ID = "veggiesdelight";

    public VeggiesDelight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(VillageStructures::addNewVillageBuilding);
    }
}
