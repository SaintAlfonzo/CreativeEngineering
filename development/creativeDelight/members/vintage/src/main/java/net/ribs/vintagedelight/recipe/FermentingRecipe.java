package net.ribs.vintagedelight.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

// Upstream's recipe (type and serializer vintagedelight:fermenting) with the 1.19.2 Recipe API (no RegistryAccess).
public class FermentingRecipe implements Recipe<SimpleContainer> {
   private static final int FIRST_INPUT_SLOT = 0;
   private static final int LAST_INPUT_SLOT = 5;
   private static final int CONTAINER_SLOT = 6;
   private final NonNullList<Ingredient> inputItems;
   private final Ingredient containerIngredient;
   private final ItemStack output;
   private final ItemStack secondaryOutput;
   private final int processingTime;
   private final ResourceLocation id;
   private static final int defaultProcessingTime = 100;

   public FermentingRecipe(
      NonNullList<Ingredient> inputItems, Ingredient containerIngredient, ItemStack output, ItemStack secondaryOutput, int processingTime, ResourceLocation id
   ) {
      this.inputItems = inputItems;
      this.containerIngredient = containerIngredient;
      this.output = output;
      this.secondaryOutput = secondaryOutput;
      this.processingTime = processingTime;
      this.id = id;
   }

   public boolean matches(SimpleContainer pContainer, Level pLevel) {
      if (pLevel.isClientSide()) {
         return false;
      }
      if (this.assignSlots(pContainer) == null) {
         return false;
      }
      return this.containerIngredient.isEmpty() || this.containerIngredient.test(pContainer.getItem(CONTAINER_SLOT));
   }

   /**
    * Port fix (creativedelight members #37, plan_creative_delight.md I2): for each ingredient entry, the input slot
    * (0-5) it takes one item from, or null when the inputs do not make this recipe. Every non-empty slot feeds at least
    * one entry and no slot feeds more entries than it holds items; entries spread over distinct slots first (one item per
    * slot) and share a slot only when there are fewer slots than entries. The jar consumes exactly these slots, so a match
    * and its craft always agree (upstream matched one entry per slot but consumed from the first matching slot, draining
    * it, so the next batch no longer matched).
    */
   public int[] assignSlots(Container container) {
      int n = this.inputItems.size();
      int[] count = new int[LAST_INPUT_SLOT + 1];
      int nonEmpty = 0;
      long total = 0;
      for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
         ItemStack stack = container.getItem(i);
         count[i] = stack.isEmpty() ? 0 : stack.getCount();
         if (count[i] > 0) {
            nonEmpty++;
            total += count[i];
         }
      }
      if (n == 0 || nonEmpty > n || total < n) {
         return null;
      }
      boolean[][] fits = new boolean[n][LAST_INPUT_SLOT + 1];
      boolean[] slotUsable = new boolean[LAST_INPUT_SLOT + 1];
      for (int j = 0; j < n; j++) {
         boolean any = false;
         for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            if (count[i] > 0 && this.inputItems.get(j).test(container.getItem(i))) {
               fits[j][i] = true;
               slotUsable[i] = true;
               any = true;
            }
         }
         if (!any) {
            return null;
         }
      }
      for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
         if (count[i] > 0 && !slotUsable[i]) {
            return null;
         }
      }
      int[] assign = new int[n];
      return assign(0, fits, count, new int[LAST_INPUT_SLOT + 1], assign, nonEmpty) ? assign : null;
   }

   /** Backtracking: entry j to an unused slot first, then to a used slot with items left. */
   private static boolean assign(int j, boolean[][] fits, int[] count, int[] used, int[] assign, int unusedSlots) {
      if (j == assign.length) {
         return unusedSlots == 0;
      }
      if (assign.length - j < unusedSlots) {
         return false;
      }
      for (int pass = 0; pass < 2; pass++) {
         for (int i = FIRST_INPUT_SLOT; i <= LAST_INPUT_SLOT; i++) {
            if (!fits[j][i] || used[i] >= count[i] || (used[i] == 0) != (pass == 0)) {
               continue;
            }
            used[i]++;
            assign[j] = i;
            if (assign(j + 1, fits, count, used, assign, used[i] == 1 ? unusedSlots - 1 : unusedSlots)) {
               return true;
            }
            used[i]--;
         }
      }
      return false;
   }

   public ItemStack getSecondaryResultItem() {
      return this.secondaryOutput != null ? this.secondaryOutput.copy() : ItemStack.EMPTY;
   }

   public int getProcessingTime() {
      return this.processingTime;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.inputItems;
   }

   public ItemStack assemble(SimpleContainer pContainer) {
      return this.output.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack getResultItem() {
      return this.output.copy();
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return FermentingRecipe.Serializer.INSTANCE;
   }

   public RecipeType<?> getType() {
      return FermentingRecipe.Type.INSTANCE;
   }

   public boolean hasContainer() {
      return !this.containerIngredient.isEmpty();
   }

   public Ingredient getContainerIngredient() {
      return this.containerIngredient;
   }

   public ItemStack getContainerItemStack() {
      return this.containerIngredient.getItems().length > 0 ? this.containerIngredient.getItems()[0] : ItemStack.EMPTY;
   }

   public static class Serializer implements RecipeSerializer<FermentingRecipe> {
      public static final FermentingRecipe.Serializer INSTANCE = new FermentingRecipe.Serializer();
      public static final ResourceLocation ID = new ResourceLocation("vintagedelight", "fermenting");

      public FermentingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
         ItemStack output = this.readItemStackWithNBT(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));
         ItemStack secondaryOutput = ItemStack.EMPTY;
         if (pSerializedRecipe.has("secondaryOutput")) {
            secondaryOutput = this.readItemStackWithNBT(GsonHelper.getAsJsonObject(pSerializedRecipe, "secondaryOutput"));
         }

         JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
         NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);

         for (int i = 0; i < ingredients.size(); i++) {
            inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
         }

         Ingredient containerIngredient = Ingredient.EMPTY;
         if (pSerializedRecipe.has("container")) {
            containerIngredient = Ingredient.fromJson(pSerializedRecipe.get("container"));
         }

         int processingTime = GsonHelper.getAsInt(pSerializedRecipe, "processingTime", 100);
         return new FermentingRecipe(inputs, containerIngredient, output, secondaryOutput, processingTime, pRecipeId);
      }

      private ItemStack readItemStackWithNBT(JsonObject jsonObject) {
         String itemName = GsonHelper.getAsString(jsonObject, "item");
         int count = GsonHelper.getAsInt(jsonObject, "count", 1);
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
         if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + itemName + "'");
         } else {
            ItemStack stack = new ItemStack(item, count);
            if (jsonObject.has("nbt")) {
               try {
                  String nbtData = GsonHelper.getAsString(jsonObject, "nbt");
                  CompoundTag nbt = TagParser.parseTag(nbtData);
                  stack.setTag(nbt);
               } catch (CommandSyntaxException var8) {
                  throw new JsonSyntaxException("Invalid NBT data in 'nbt' tag", var8);
               }
            }

            return stack;
         }
      }

      public void toNetwork(FriendlyByteBuf pBuffer, FermentingRecipe pRecipe) {
         pBuffer.writeInt(pRecipe.inputItems.size());

         for (Ingredient ingredient : pRecipe.getIngredients()) {
            ingredient.toNetwork(pBuffer);
         }

         pBuffer.writeItemStack(pRecipe.getResultItem(), false);
         boolean hasSecondaryOutput = !pRecipe.getSecondaryResultItem().isEmpty();
         pBuffer.writeBoolean(hasSecondaryOutput);
         if (hasSecondaryOutput) {
            pBuffer.writeItemStack(pRecipe.getSecondaryResultItem(), false);
         }

         pBuffer.writeInt(pRecipe.getProcessingTime());
         pRecipe.containerIngredient.toNetwork(pBuffer);
      }

      public FermentingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         int inputCount = pBuffer.readInt();
         NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);

         for (int i = 0; i < inputCount; i++) {
            inputs.set(i, Ingredient.fromNetwork(pBuffer));
         }

         ItemStack output = pBuffer.readItem();
         ItemStack secondaryOutput = ItemStack.EMPTY;
         boolean hasSecondaryOutput = pBuffer.readBoolean();
         if (hasSecondaryOutput) {
            secondaryOutput = pBuffer.readItem();
         }

         int processingTime = pBuffer.readInt();
         Ingredient containerIngredient = Ingredient.fromNetwork(pBuffer);
         return new FermentingRecipe(inputs, containerIngredient, output, secondaryOutput, processingTime, pRecipeId);
      }
   }

   // Port: registered as vintagedelight:fermenting (ModRecipes); upstream never registered its type
   public static class Type implements RecipeType<FermentingRecipe> {
      public static final FermentingRecipe.Type INSTANCE = new FermentingRecipe.Type();
      public static final String ID = "fermenting";

      @Override
      public String toString() {
         return "vintagedelight:fermenting";
      }
   }
}
