package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

public class KegFermentingRecipe implements Recipe<KegRecipeWrapper> {
   public static final int INPUT_SLOTS = 4;
   private final ResourceLocation id;
   private final NonNullList<Ingredient> inputItems;
   private final Optional<FluidIngredientWithAmount> fluidIngredient;
   private final Optional<FluidUnit> unit;
   private final FermentingBookCategory tab;
   private final Either<AbstractedFluidStack, ItemStack> result;
   private final float experience;
   private final int fermentTime;
   private final int temperature;

   public KegFermentingRecipe(
      ResourceLocation id,
      NonNullList<Ingredient> inputItems,
      FermentingBookCategory tab,
      Optional<FluidIngredientWithAmount> fluidIngredient,
      Optional<FluidUnit> unit,
      Either<AbstractedFluidStack, ItemStack> result,
      float experience,
      int fermentTime,
      int temperature
   ) {
      this.id = id;
      this.inputItems = inputItems;
      this.tab = tab;
      this.fluidIngredient = fluidIngredient;
      this.unit = unit;
      if (unit.isPresent() && result.left().isPresent()) {
         AbstractedFluidStack fluid = result.left().get();
         this.result = Either.left(new AbstractedFluidStack(fluid.fluid(), fluid.amount(), fluid.tag(), unit.get(), null));
      } else {
         this.result = result;
      }

      this.experience = experience;
      this.fermentTime = fermentTime;
      this.temperature = temperature;
   }

   @Override
   public ResourceLocation getId() {
      return this.id;
   }

   public FermentingBookCategory getRecipeBookCategory() {
      return this.tab;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      return this.inputItems;
   }

   public Optional<FluidIngredientWithAmount> getFluidIngredient() {
      return this.fluidIngredient;
   }

   public Optional<FluidUnit> getRawUnit() {
      return this.unit;
   }

   public FluidUnit getUnit() {
      return this.unit.orElse(FluidUnit.getLoaderUnit());
   }

   public Either<AbstractedFluidStack, ItemStack> getResult() {
      return this.result;
   }

   @Override
   public ItemStack assemble(KegRecipeWrapper inv) {
      return ItemStack.EMPTY;
   }

   public float getExperience() {
      return this.experience;
   }

   public int getFermentTime() {
      return this.fermentTime;
   }

   public int getTemperature() {
      return this.temperature;
   }

   @Override
   public boolean matches(KegRecipeWrapper inv, Level level) {
      // Port: 1.21's CraftingInput.of(2, 2, inputs): one item per non-empty slot (accountStack(stack, 1)), and the
      // "one item, one ingredient" shortcut when the trimmed grid is a single slot.
      List<ItemStack> inputs = new ArrayList<>();
      StackedContents stackedContents = new StackedContents();

      for (int j = 0; j < INPUT_SLOTS; j++) {
         ItemStack itemstack = inv.getItem(j);
         if (!itemstack.isEmpty()) {
            inputs.add(itemstack);
            stackedContents.accountStack(itemstack, 1);
         }
      }

      return inputs.size() == 1 && this.inputItems.size() == 1
         ? this.inputItems.get(0).test(inputs.get(0))
         : stackedContents.canCraft(this, null)
            && (
               this.fluidIngredient.isEmpty() && inv.getFluid().isEmpty()
                  || this.fluidIngredient.isPresent()
                     && !inv.getFluid().isEmpty()
                     && this.fluidIngredient.get().ingredient().matches(inv.getFluid())
                     && inv.getFluid().amount() % this.fluidIngredient.get().amount() == 0L
            );
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= this.inputItems.size();
   }

   @Override
   public ItemStack getResultItem() {
      if (this.result.right().isPresent()) {
         return this.result.right().get().copy();
      } else {
         return this.result.left().isPresent()
            ? BnCRecipeUtils.getPouredItemFromFluid(
               new AbstractedFluidStack(
                  this.result.left().get().fluid(),
                  BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity(),
                  this.result.left().get().tag(),
                  BnCConfiguration.COMMON_CONFIG.get().keg().capacityUnit(),
                  null
               )
            )
            : ItemStack.EMPTY;
      }
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return BnCRecipeSerializers.FERMENTING;
   }

   @Override
   public RecipeType<?> getType() {
      return BnCRecipeTypes.FERMENTING;
   }

   @Override
   public ItemStack getToastSymbol() {
      return new ItemStack(BnCItems.KEG);
   }

   @Override
   public boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().allMatch(Ingredient::isEmpty);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         KegFermentingRecipe that = (KegFermentingRecipe)o;
         if (Float.compare(that.getExperience(), this.getExperience()) != 0) {
            return false;
         } else if (this.getFermentTime() != that.getFermentTime()) {
            return false;
         } else if (this.getTemperature() != that.getTemperature()) {
            return false;
         } else if (this.getResult() != that.getResult()) {
            return false;
         } else {
            return this.getFluidIngredient() != that.getFluidIngredient() ? false : this.inputItems.equals(that.inputItems);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inputItems, this.fluidIngredient, this.result, this.experience, this.fermentTime, this.temperature);
   }

   public static class Serializer implements RecipeSerializer<KegFermentingRecipe> {
      @Override
      public KegFermentingRecipe fromJson(ResourceLocation id, JsonObject json) {
         JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
         if (array.isEmpty() || array.size() > INPUT_SLOTS) {
            throw new JsonParseException("Fermenting recipes need 1 to 4 ingredients");
         }
         List<Ingredient> list = new ArrayList<>();
         for (JsonElement element : array) {
            list.add(BnCJson.ingredient(element));
         }
         NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new));

         FermentingBookCategory category = FermentingBookCategory.DRINKS;
         if (json.has("category")) {
            category = FermentingBookCategory.byName(GsonHelper.getAsString(json, "category"));
            if (category == null) {
               throw new JsonParseException("Unknown fermenting category '" + json.get("category") + "'");
            }
         }

         Optional<FluidIngredientWithAmount> baseFluid = json.has("base_fluid")
            ? Optional.of(FluidIngredientWithAmount.fromJson(GsonHelper.getAsJsonObject(json, "base_fluid")))
            : Optional.empty();
         Optional<FluidUnit> unit = json.has("unit") ? Optional.of(BnCJson.unit(json.get("unit"))) : Optional.empty();

         // Codec.either(fluid, item) as upstream: a result with an "amount" naming a fluid is a fluid, else an item.
         JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
         Either<AbstractedFluidStack, ItemStack> result;
         String resultId = GsonHelper.getAsString(resultJson, "id", GsonHelper.getAsString(resultJson, "item", ""));
         if (resultJson.has("amount") && ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(resultId))) {
            result = Either.left(AbstractedFluidStack.fromJson(resultJson));
         } else {
            result = Either.right(BnCJson.itemStack(resultJson));
         }

         float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
         int fermentingTime = GsonHelper.getAsInt(json, "fermenting_time", 9600);
         int temperature = GsonHelper.getAsInt(json, "temperature", 3);
         return new KegFermentingRecipe(id, ingredients, category, baseFluid, unit, result, experience, fermentingTime, temperature);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buf, KegFermentingRecipe recipe) {
         buf.writeVarInt(recipe.getIngredients().size());
         for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buf);
         }
         buf.writeVarInt(recipe.getRecipeBookCategory().id());
         buf.writeOptional(recipe.getFluidIngredient(), FluidIngredientWithAmount::write);
         buf.writeOptional(recipe.getRawUnit(), FluidUnit::write);
         buf.writeBoolean(recipe.getResult().left().isPresent());
         recipe.getResult().ifLeft(fluid -> AbstractedFluidStack.write(buf, fluid)).ifRight(buf::writeItem);
         buf.writeFloat(recipe.getExperience());
         buf.writeInt(recipe.getFermentTime());
         buf.writeInt(recipe.getTemperature());
      }

      @Override
      public KegFermentingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
         int size = buf.readVarInt();
         Ingredient[] list = new Ingredient[size];
         for (int i = 0; i < size; i++) {
            list[i] = Ingredient.fromNetwork(buf);
         }
         NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, list);
         FermentingBookCategory category = FermentingBookCategory.byId(buf.readVarInt());
         Optional<FluidIngredientWithAmount> fluidIngredient = buf.readOptional(FluidIngredientWithAmount::read);
         Optional<FluidUnit> fluidUnit = buf.readOptional(FluidUnit::read);
         Either<AbstractedFluidStack, ItemStack> result = buf.readBoolean()
            ? Either.left(AbstractedFluidStack.read(buf))
            : Either.right(buf.readItem());
         float experience = buf.readFloat();
         int fermentingTime = buf.readInt();
         int temperature = buf.readInt();
         return new KegFermentingRecipe(id, ingredients, category, fluidIngredient, fluidUnit, result, experience, fermentingTime, temperature);
      }
   }
}
