package umpaz.brewinandchewin.forge.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import umpaz.brewinandchewin.client.utility.BnCGuiUtils;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

public class BnCClientPlatformHelperForge implements BnCClientPlatformHelper {
   /** The keg gauge: a 24x28 window, the lower 16 px drawn first, then the top 12 px for the last 43% of the tank. */
   @Override
   public void renderFluidInKeg(AbstractedFluidStack stack, PoseStack pose, int x, int y, float alphaModifier) {
      if (!(stack.loaderSpecific() instanceof FluidStack fluidStack)) {
         return;
      }
      IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(stack.fluid());
      ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
      TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
      int tintColor = fluidTypeExtensions.getTintColor(fluidStack);
      float alpha = (float)(tintColor >> 24 & 0xFF) / 255.0F * alphaModifier;
      float red = (float)(tintColor >> 16 & 0xFF) / 255.0F;
      float green = (float)(tintColor >> 8 & 0xFF) / 255.0F;
      float blue = (float)(tintColor & 0xFF) / 255.0F;
      long kegCapacity = BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity();
      float capacity = (float)Math.min(kegCapacity, stack.unit().convertToLoader(stack.amount())) / (float)kegCapacity;
      ResourceLocation atlas = sprite.atlas().location();
      float halfU = sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0());
      if ((double)capacity > 0.57) {
         int y1 = y + (int)(12.0F * (1.0F - (capacity - 0.57F) / 0.43F));
         int y2 = y + 12;
         float topCapacity = (capacity - 0.57F) / 0.43F;
         float vDistance = sprite.getV1() - sprite.getV0();
         float v0 = sprite.getV0() + 0.25F * vDistance + 0.75F * vDistance * (1.0F - topCapacity);
         BnCGuiUtils.innerBlit(pose, atlas, x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
         BnCGuiUtils.innerBlit(pose, atlas, x + 16, x + 24, y1, y2, 0, sprite.getU0(), halfU, v0, sprite.getV1(), red, green, blue, alpha);
      }

      int y1 = y + 12 + (int)(16.0F * (1.0F - Math.min(1.0F, capacity / 0.57F)));
      int y2 = y + 12 + 16;
      float vDistance = sprite.getV1() - sprite.getV0();
      float v0 = sprite.getV0() + vDistance * (1.0F - Math.min(1.0F, capacity / 0.57F));
      BnCGuiUtils.innerBlit(pose, atlas, x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
      BnCGuiUtils.innerBlit(pose, atlas, x + 16, x + 24, y1, y2, 0, sprite.getU0(), halfU, v0, sprite.getV1(), red, green, blue, alpha);
   }
}
