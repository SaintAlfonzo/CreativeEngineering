package com.SaintAlfonzo.creativeengineering;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Test stand-in for patcherMod's main class: only what the remainder buffer and the oven need. */
@Mod(CreativeEngineering.MODID)
public class CreativeEngineering {
    public static final String MODID = "creativeengineering";

    public CreativeEngineering() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CEConfig.COMMON_SPEC, "rbtest-common.toml");
        com.SaintAlfonzo.creativedelight.oven.OvenRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativedelight.cooking.RemainderBufferStorage.class);
    }
}
