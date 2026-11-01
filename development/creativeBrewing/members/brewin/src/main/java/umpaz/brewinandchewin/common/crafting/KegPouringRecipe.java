package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

public class KegPouringRecipe implements Recipe<KegRecipeWrapper> {
   private final ResourceLocation id;
   private final AbstractedFluidStack fluid;
   private final Optional<ItemStack> container;
   private final ItemStack output;
   private final Optional<FluidUnit> unit;
   private final boolean strict;
   private final boolean filling;

   public KegPouringRecipe(
      ResourceLocation id, AbstractedFluidStack fluid, Optional<ItemStack> container, ItemStack output, Optional<FluidUnit> unit, boolean strict, boolean filling
   ) {
      if (container.isEmpty() && BrewinAndChewin.getHelper().getCraftingRemainingItem(output).isEmpty()) {
         throw new UnsupportedOperationException("'container' field must be specified as the output item stack doesn't have a crafting remainder item.");
      } else {
         this.id = id;
         this.fluid = fluid;
         this.container = container;
         this.output = output;
         this.unit = unit;
         this.strict = strict;
         this.filling = filling;
      }
   }

   @Override
   public ResourceLocation getId() {
      return this.id;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> ingredient = NonNullList.create();
      ingredient.add(Ingredient.of(this.getContainer()));
      return ingredient;
   }

   @Override
   public boolean matches(KegRecipeWrapper inv, Level level) {
      return Ingredient.of(this.getContainer()).test(inv.getItem(4));
   }

   @Override
   public ItemStack assemble(KegRecipeWrapper recipeWrapper) {
      return this.output.copy();
   }

   @Override
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack getContainer() {
      return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(this.output));
   }

   public ItemStack getContainer(ItemStack stack) {
      return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(stack));
   }

   public Optional<FluidUnit> getRawUnit() {
      return this.unit;
   }

   public FluidUnit getUnit() {
      return this.unit.orElse(FluidUnit.getLoaderUnit());
   }

   public long getLoaderAmount() {
      return this.getUnit().convertToLoader(this.fluid.amount());
   }

   public Optional<ItemStack> getRawContainer() {
      return this.container;
   }

   public ItemStack getOutput() {
      return this.output;
   }

   @Override
   public ItemStack getResultItem() {
      return this.output;
   }

   public AbstractedFluidStack getFluid(ItemStack container) {
      return this.fluid;
   }

   public AbstractedFluidStack getRawFluid() {
      return this.fluid;
   }

   public boolean hasSpecialFluid() {
      return false;
   }

   public boolean isStrict() {
      return this.strict;
   }

   public boolean canFill() {
      return this.filling;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return BnCRecipeSerializers.KEG_POURING;
   }

   @Override
   public RecipeType<?> getType() {
      return BnCRecipeTypes.KEG_POURING;
   }

   @Override
   public ItemStack getToastSymbol() {
      return new ItemStack(BnCItems.KEG);
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fluid, this.container, this.output, this.strict, this.filling);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         KegPouringRecipe that = (KegPouringRecipe)o;
         if (!this.output.equals(that.output)) {
            return false;
         } else if (!this.fluid.equals(that.fluid)) {
            return false;
         } else if (!this.container.equals(that.container)) {
            return false;
         } else {
            return this.strict != that.strict ? false : this.filling == that.filling;
         }
      } else {
         return false;
      }
   }

   public static class Serializer implements RecipeSerializer<KegPouringRecipe> {
      @Override
      public KegPouringRecipe fromJson(ResourceLocation id, JsonObject json) {
         AbstractedFluidStack fluid = AbstractedFluidStack.fromJson(GsonHelper.getAsJsonObject(json, "fluid"));
         Optional<ItemStack> container = json.has("container")
            ? Optional.of(BnCJson.itemStack(GsonHelper.getAsJsonObject(json, "container")))
            : Optional.empty();
         ItemStack output = BnCJson.itemStack(GsonHelper.getAsJsonObject(json, "output"));
         Optional<FluidUnit> unit = json.has("unit") ? Optional.of(BnCJson.unit(json.get("unit"))) : Optional.empty();
         boolean strict = GsonHelper.getAsBoolean(json, "strict", false);
         boolean canFill = GsonHelper.getAsBoolean(json, "can_fill", true);
         return new KegPouringRecipe(id, fluid, container, output, unit, strict, canFill);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buf, KegPouringRecipe recipe) {
         AbstractedFluidStack.write(buf, recipe.getRawFluid());
         buf.writeOptional(recipe.getRawContainer(), FriendlyByteBuf::writeItem);
         buf.writeItem(recipe.getOutput());
         buf.writeOptional(recipe.getRawUnit(), FluidUnit::write);
         buf.writeBoolean(recipe.isStrict());
         buf.writeBoolean(recipe.canFill());
      }

      @Override
      public KegPouringRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
         AbstractedFluidStack fluid = AbstractedFluidStack.read(buf);
         Optional<ItemStack> container = buf.readOptional(FriendlyByteBuf::readItem);
         ItemStack output = buf.readItem();
         Optional<FluidUnit> unit = buf.readOptional(FluidUnit::read);
         boolean strict = buf.readBoolean();
         boolean canFill = buf.readBoolean();
         return new KegPouringRecipe(id, fluid, container, output, unit, strict, canFill);
      }
   }
}
