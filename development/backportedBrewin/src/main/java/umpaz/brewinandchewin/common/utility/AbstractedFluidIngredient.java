package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonElement;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import umpaz.brewinandchewin.forge.utility.KegCompatibleFluidIngredients;

// Port: CODEC / STREAM_CODEC -> fromJson / write / read (KegCompatibleFluidIngredients).
public interface AbstractedFluidIngredient {
   List<AbstractedFluidStack> displayStacks();

   boolean matches(AbstractedFluidStack stack);

   static AbstractedFluidIngredient fromJson(JsonElement json) {
      return KegCompatibleFluidIngredients.fromJson(json);
   }

   static void write(FriendlyByteBuf buf, AbstractedFluidIngredient ingredient) {
      KegCompatibleFluidIngredients.write(buf, ingredient);
   }

   static AbstractedFluidIngredient read(FriendlyByteBuf buf) {
      return KegCompatibleFluidIngredients.read(buf);
   }
}
