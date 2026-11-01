package umpaz.brewinandchewin.client.renderer.texture.modifier;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;

public class GlintTextureModifier implements TextureModifier {
   public static final ResourceLocation ID = BrewinAndChewin.asResource("glint");

   @Override
   public RenderType renderType(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, RenderType previous) {
      return RenderType.glint();
   }

   @Override
   public ResourceLocation getId() {
      return ID;
   }
}
