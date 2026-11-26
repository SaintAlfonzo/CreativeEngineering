// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/MenuRegistry.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.registries;

import com.yuushya.modelling.gui.engrave.EngraveMenu;
import com.yuushya.modelling.gui.history.HistoryMenu;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class MenuRegistry {
   public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "yuushya");
   public static final Supplier<MenuType<EngraveMenu>> EngraveMenu = MENU_TYPE.register("engrave", () -> IForgeMenuType.create(EngraveMenu::new));
   public static final Supplier<MenuType<HistoryMenu>> HistoryMenu = MENU_TYPE.register("history", () -> IForgeMenuType.create(HistoryMenu::new));

   public static void register(IEventBus bus) {
      MENU_TYPE.register(bus);
   }
}
