package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;
import umpaz.brewinandchewin.common.utility.FluidUnit;

// Port: CODEC / STREAM_CODEC -> fromJson / write / read. JSON: {"ingredient": {...}, "amount": N, "unit": "..."}.
public record FluidIngredientWithAmount(AbstractedFluidIngredient ingredient, long amount, Optional<FluidUnit> unit) {
   public FluidIngredientWithAmount {
      if (ingredient.matches(AbstractedFluidStack.EMPTY)) {
         throw new IllegalArgumentException("Fluid Ingredient must not accept empty.");
      } else if (amount <= 0L) {
         throw new IllegalArgumentException("Fluid Ingredient amount must be higher than 0.");
      }
   }

   public FluidUnit getUnit() {
      return this.unit().orElse(FluidUnit.getLoaderUnit());
   }

   public long loaderAmount() {
      FluidUnit unit = this.unit().orElse(FluidUnit.getLoaderUnit());
      return unit.convertToLoader(this.amount);
   }

   public static FluidIngredientWithAmount fromJson(JsonObject json) {
      AbstractedFluidIngredient ingredient = AbstractedFluidIngredient.fromJson(json.get("ingredient"));
      // Port: upstream falls back to the first display stack's amount (1000); tags aren't bound yet while recipes
      // load on 1.19.2, so the fallback is 1000 directly.
      long amount = GsonHelper.getAsLong(json, "amount", 1000L);
      if (amount < 1L) {
         throw new JsonParseException("Fluid Ingredient amount must be at least 1.");
      }
      Optional<FluidUnit> unit = json.has("unit") ? Optional.of(BnCJson.unit(json.get("unit"))) : Optional.empty();
      return new FluidIngredientWithAmount(ingredient, amount, unit);
   }

   public static void write(FriendlyByteBuf buf, FluidIngredientWithAmount value) {
      AbstractedFluidIngredient.write(buf, value.ingredient());
      buf.writeLong(value.amount());
      buf.writeOptional(value.unit(), FluidUnit::write);
   }

   public static FluidIngredientWithAmount read(FriendlyByteBuf buf) {
      AbstractedFluidIngredient ingredient = AbstractedFluidIngredient.read(buf);
      long amount = buf.readLong();
      Optional<FluidUnit> unit = buf.readOptional(FluidUnit::read);
      return new FluidIngredientWithAmount(ingredient, amount, unit);
   }
}
