package com.jkvin114.displaydelight.init;

import com.jkvin114.displaydelight.shared.CdSettings.Flag;

/**
 * Display Delight's settings. Since the port became a Creative Delight member (2026-11-02, plan_creative_delight.md A6)
 * they are keys of Creative Delight's config, section {@code [display]} (CD's CDConfig defines them, comments and
 * defaults); the port registers no config file of its own. The old displaydelight-common.toml ([Features], 1.7.0's keys)
 * is read once by Creative Delight and renamed. Field names and {@code get()} are 1.7.0's, so the call sites are unchanged;
 * the CD keys are positive switches, so the "Disable..." fields read them inverted.
 */
public class DisplayConfig {
   public static final String SECTION = "display.";
   public static final Flag MESSAGE = new Flag(SECTION + "placeMessage", true, false);
   public static final Flag DISABLE_VANILLA_FOODS = new Flag(SECTION + "vanillaFoodPlacement", true, true);
   public static final Flag DISABLE_EATING_ANIMATION_FIX = new Flag(SECTION + "eatingAnimationFix", true, true);
   public static final Flag SILKTOUCH_DROPS_FOOD_BLOCK_ITEM = new Flag(SECTION + "silkTouchDropsFoodBlock", true, false);
   public static final Flag DISABLE_CREATIVE_TAB_ITEMS = new Flag(SECTION + "creativeTabItems", true, true);
   public static final Flag DISABLE_PLACE_ALL_INTERACTION = new Flag(SECTION + "placeAll", true, true);
   public static final Flag DISABLE_TAKE_ALL_INTERACTION = new Flag(SECTION + "takeAll", true, true);
   public static final Flag DISABLE_HIDE_PLATE_INTERACTION = new Flag(SECTION + "axeHidesPlates", true, true);
   public static final Flag DISABLE_PUT_PLATE_INTERACTION = new Flag(SECTION + "platesPutBack", true, true);

   private DisplayConfig() {
   }
}
