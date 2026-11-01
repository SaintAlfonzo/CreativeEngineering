package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.common.loot.function.BnCCopyMealFunction;
import umpaz.brewinandchewin.common.loot.function.CopyDrinkFunction;

// Port: no BnCLootConditions. Upstream's two loot conditions only serve the "place a temperature block near a keg"
// advancement, whose 1.21 placed_block trigger takes loot conditions; on 1.19.2 that advancement is awarded from code
// (forge/event/BnCCommonEvents#onBlockPlaced).
public class BnCLootFunctions {
   public static void registerAll(RegisterHelper<LootItemFunctionType> helper) {
      helper.register(CopyDrinkFunction.ID, CopyDrinkFunction.TYPE);
      helper.register(BnCCopyMealFunction.ID, BnCCopyMealFunction.TYPE);
   }
}
