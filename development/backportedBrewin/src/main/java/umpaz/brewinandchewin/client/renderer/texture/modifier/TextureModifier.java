package umpaz.brewinandchewin.client.renderer.texture.modifier;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

// Port: modifiers are looked up by "type" id (BnCTextureModifiers) instead of a dispatch codec; none take parameters.
public interface TextureModifier {
   default int color(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, int previous) {
      return previous;
   }

   default RenderType renderType(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, RenderType previous) {
      return previous;
   }

   ResourceLocation getId();
}
