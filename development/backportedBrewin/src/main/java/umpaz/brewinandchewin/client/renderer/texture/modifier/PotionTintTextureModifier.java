package umpaz.brewinandchewin.client.renderer.texture.modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;

public class PotionTintTextureModifier implements TextureModifier {
   public static final ResourceLocation ID = BrewinAndChewin.asResource("potion_tint");

   @Override
   public int color(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, int previous) {
      return 0xFF000000 | (PotionUtils.getColor(stack) & 0xFFFFFF);
   }

   @Override
   public ResourceLocation getId() {
      return ID;
   }
}
