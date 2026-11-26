// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/YuushyaRegistries.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.registries;

import net.minecraftforge.eventbus.api.IEventBus;

public class YuushyaRegistries {
   public static void register(IEventBus bus) {
      GroupRegistry.register();
      BlockRegistry.register(bus);
      ItemRegistry.register(bus);
      BlockEntityRegistry.register(bus);
      MenuRegistry.register(bus);
   }
}
