package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;

public class BnCMenuTypes {
   public static final MenuType<KegMenu> KEG = BrewinAndChewin.getHelper().createMenuType(KegMenu::new);

   public static void registerAll(RegisterHelper<MenuType<?>> helper) {
      helper.register(BrewinAndChewin.asResource("keg"), KEG);
   }
}
