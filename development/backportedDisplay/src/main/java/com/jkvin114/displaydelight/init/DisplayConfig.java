package com.jkvin114.displaydelight.init;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

/**
 * displaydelight-common.toml. 1.7.0's keys, all under [Features] as in 1.2.0, without WanderingTraderSellFoodBlock
 * (the port has no trades) and the unused Tooltips.
 */
public class DisplayConfig {
   public static final Builder BUILDER = new Builder();
   public static final BooleanValue MESSAGE;
   public static final BooleanValue DISABLE_VANILLA_FOODS;
   public static final BooleanValue DISABLE_EATING_ANIMATION_FIX;
   public static final BooleanValue SILKTOUCH_DROPS_FOOD_BLOCK_ITEM;
   public static final BooleanValue DISABLE_CREATIVE_TAB_ITEMS;
   public static final BooleanValue DISABLE_PLACE_ALL_INTERACTION;
   public static final BooleanValue DISABLE_TAKE_ALL_INTERACTION;
   public static final BooleanValue DISABLE_HIDE_PLATE_INTERACTION;
   public static final BooleanValue DISABLE_PUT_PLATE_INTERACTION;
   public static final ForgeConfigSpec CONFIG;

   static {
      BUILDER.push("Features");
      MESSAGE = BUILDER.comment("Should a message be enabled when attempting to place a food item?").define("Display-Message", true);
      DISABLE_VANILLA_FOODS = BUILDER.comment(
            "Disable Vanila Food(mushroom stew,rabbit stew,beetroot soup) Placement. Set to true if vanila food placement conflicts with other mods"
         )
         .define("Disable-Vanila-Food-Placement", false);
      DISABLE_EATING_ANIMATION_FIX = BUILDER.comment(
            "This mod uses hardcoded check to remove annoying eating animation while plating some foods.\n Try setting this to true if there are any related issues(can't eat foods, can't plate foods, etc.). (only applicable for Forge version)"
         )
         .define("Disable-Eating-Animation-Fix", false);
      SILKTOUCH_DROPS_FOOD_BLOCK_ITEM = BUILDER.comment("Whether to drop the food block item when food block is broken by silktouch")
         .define("SilkTouchDropFoodBlock", true);
      DISABLE_CREATIVE_TAB_ITEMS = BUILDER.comment(
            "Should creative tab items disabled? Try turning this on if this mod's items take too much space in JEI"
         )
         .define("DisableCreativeTabItems", false);
      DISABLE_PLACE_ALL_INTERACTION = BUILDER.comment("Should shift right-click interation to place all items be disabled?")
         .define("DisablePlaceAllInteratction", false);
      DISABLE_TAKE_ALL_INTERACTION = BUILDER.comment("Should shift right-click interation to take all items be disabled?")
         .define("DisableTakeAllInteratction", false);
      DISABLE_HIDE_PLATE_INTERACTION = BUILDER.comment("Should axe right-click interation to hide plates disabled?")
         .define("DisableHidePlateInteratction", false);
      DISABLE_PUT_PLATE_INTERACTION = BUILDER.comment("Should palte right-click interation to put back the plates disabled?")
         .define("DisablePutPlateInteratction", false);
      BUILDER.pop();
      CONFIG = BUILDER.build();
   }
}
