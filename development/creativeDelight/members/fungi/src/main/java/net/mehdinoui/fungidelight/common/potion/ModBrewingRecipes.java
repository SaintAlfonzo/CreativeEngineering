package net.mehdinoui.fungidelight.common.potion;

import net.mehdinoui.fungidelight.common.registry.ModItems;
import net.mehdinoui.fungidelight.common.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Port change (fungi_delight.md 5.1.4, D3): the three Burrowing potions are vanilla PotionBrewing mixes, not Forge
 * BrewingRecipes, so every system that reads the vanilla mix list (brewing stand, Create's mixer, IE, splash and
 * lingering variants, potion buckets) treats them like any other potion. addMix is opened by the access transformer.
 * Called from common setup inside enqueueWork (the mix list is not thread safe).
 */
public class ModBrewingRecipes {
   public static void register() {
      PotionBrewing.addMix(Potions.AWKWARD, ModItems.TRUFFLE.get(), ModPotions.BURROWING_POTION.get());
      PotionBrewing.addMix(ModPotions.BURROWING_POTION.get(), Items.REDSTONE, ModPotions.LONG_BURROWING_POTION.get());
      PotionBrewing.addMix(ModPotions.BURROWING_POTION.get(), Items.GLOWSTONE_DUST, ModPotions.STRONG_BURROWING_POTION.get());
   }
}
