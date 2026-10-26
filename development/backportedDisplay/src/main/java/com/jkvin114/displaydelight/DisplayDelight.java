package com.jkvin114.displaydelight;

import com.jkvin114.displaydelight.init.DisplayBlocks;
import com.jkvin114.displaydelight.init.DisplayConfig;
import com.jkvin114.displaydelight.init.DisplayItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Display Delight 1.7.0 on Forge 1.19.2. Left out of the port: the wandering trader sales of display items for missing
 * mods, the Expanded Delight compat class, the display blocks for mods the pack does not have, and the startup
 * "HELLO" log lines.
 */
@Mod("displaydelight")
public class DisplayDelight {
   public static final String MODID = "displaydelight";
   public static final Logger LOGGER = LogUtils.getLogger();

   public DisplayDelight() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      DisplayItems.registerFoods();
      DisplayItems.REGISTRY.register(modEventBus);
      DisplayBlocks.REGISTRY.register(modEventBus);
      ModLoadingContext.get().registerConfig(Type.COMMON, DisplayConfig.CONFIG, "displaydelight-common.toml");
   }
}
