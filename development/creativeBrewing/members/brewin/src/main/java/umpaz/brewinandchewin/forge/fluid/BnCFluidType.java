package umpaz.brewinandchewin.forge.fluid;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants.Textures;

public class BnCFluidType extends FluidType {
   private final ResourceLocation stillTexture;
   private final ResourceLocation flowingTexture;
   private final int tint;

   public BnCFluidType(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tint) {
      super(
         Properties.create()
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
      );
      this.stillTexture = stillTexture;
      this.flowingTexture = flowingTexture;
      this.tint = tint;
   }

   public static BnCFluidType alcohol(int tint) {
      return new BnCFluidType(Textures.FLUID_STILL_TEXTURE, Textures.FLUID_FLOWING_TEXTURE, tint);
   }

   public static BnCFluidType honey(int tint) {
      return new BnCFluidType(Textures.HONEY_FLUID_STILL_TEXTURE, Textures.HONEY_FLUID_FLOWING_TEXTURE, tint);
   }

   @Override
   public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
      consumer.accept(new IClientFluidTypeExtensions() {
         @Override
         public ResourceLocation getStillTexture() {
            return BnCFluidType.this.stillTexture;
         }

         @Override
         public ResourceLocation getFlowingTexture() {
            return BnCFluidType.this.flowingTexture;
         }

         @Override
         public int getTintColor() {
            return BnCFluidType.this.tint;
         }
      });
   }
}
