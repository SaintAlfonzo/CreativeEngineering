// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 YuushyaNeoForge.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling;

import com.yuushya.modelling.network.YuushyaModellingNetwork;
import com.yuushya.modelling.registries.YuushyaRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("yuushya_modelling")
public class YuushyaNeoForge {
   public YuushyaNeoForge() {
      IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
      YuushyaRegistries.register(eventBus);
      YuushyaModellingNetwork.register();
   }
}
