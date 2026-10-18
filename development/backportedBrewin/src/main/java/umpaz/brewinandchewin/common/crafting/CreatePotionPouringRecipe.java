package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCJson;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

/**
 * Pours Create's potion fluid into bottles and back. Port: 1.21's potion_contents component is the 1.19.2 potion NBT
 * ("Potion", "CustomPotionEffects", "CustomPotionColor"), and Create 0.5.1's potion fluid also carries "Bottle"
 * (REGULAR / SPLASH / LINGERING), which is filled in from the container item so poured-back fluid matches Create's own.
 */
public class CreatePotionPouringRecipe extends KegPouringRecipe {
   public CreatePotionPouringRecipe(ResourceLocation id, Optional<ItemStack> container, ItemStack result, long amount, Optional<FluidUnit> unit, boolean canFill) {
      super(id, new AbstractedFluidStack(BrewinAndChewin.getHelper().getCreatePotionFluid(), amount), container, result, unit, false, canFill);
   }

   @Override
   public ItemStack assemble(KegRecipeWrapper recipeWrapper) {
      ItemStack stack = super.getResultItem().copy();
      AbstractedFluidStack fluidStack = recipeWrapper.getFluid();
      CompoundTag tag = fluidStack.tag();
      if (tag != null && (PotionUtils.getPotion(tag) != Potions.EMPTY || !PotionUtils.getCustomEffects(tag).isEmpty())) {
         PotionUtils.setPotion(stack, PotionUtils.getPotion(tag));
         if (tag.contains("CustomPotionEffects")) {
            stack.getOrCreateTag().put("CustomPotionEffects", tag.get("CustomPotionEffects").copy());
         }
         if (tag.contains("CustomPotionColor")) {
            stack.getOrCreateTag().putInt("CustomPotionColor", tag.getInt("CustomPotionColor"));
         }
      }

      return stack;
   }

   @Override
   public AbstractedFluidStack getFluid(ItemStack container) {
      AbstractedFluidStack fluidStack = super.getFluid(container);
      CompoundTag tag = container.hasTag() ? container.getTag().copy() : new CompoundTag();
      tag.remove("Damage");
      tag.putString("Bottle", container.is(Items.LINGERING_POTION) ? "LINGERING" : container.is(Items.SPLASH_POTION) ? "SPLASH" : "REGULAR");
      return new AbstractedFluidStack(fluidStack.fluid(), fluidStack.amount(), tag, this.getUnit());
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return BnCRecipeSerializers.CREATE_POTION_POURING;
   }

   public static class Serializer implements RecipeSerializer<CreatePotionPouringRecipe> {
      @Override
      public CreatePotionPouringRecipe fromJson(ResourceLocation id, JsonObject json) {
         Optional<ItemStack> container = json.has("container")
            ? Optional.of(BnCJson.itemStack(GsonHelper.getAsJsonObject(json, "container")))
            : Optional.empty();
         ItemStack output = BnCJson.itemStack(GsonHelper.getAsJsonObject(json, "output"));
         long amount = GsonHelper.getAsLong(json, "fluid_amount");
         Optional<FluidUnit> unit = json.has("unit") ? Optional.of(BnCJson.unit(json.get("unit"))) : Optional.empty();
         boolean canFill = GsonHelper.getAsBoolean(json, "can_fill", true);
         return new CreatePotionPouringRecipe(id, container, output, amount, unit, canFill);
      }

      @Override
      public CreatePotionPouringRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
         Optional<ItemStack> container = buf.readOptional(FriendlyByteBuf::readItem);
         ItemStack output = buf.readItem();
         long amount = buf.readLong();
         Optional<FluidUnit> unit = buf.readOptional(FluidUnit::read);
         boolean canFill = buf.readBoolean();
         return new CreatePotionPouringRecipe(id, container, output, amount, unit, canFill);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buf, CreatePotionPouringRecipe recipe) {
         buf.writeOptional(recipe.getRawContainer(), FriendlyByteBuf::writeItem);
         buf.writeItem(recipe.getOutput());
         buf.writeLong(recipe.getRawFluid().amount());
         buf.writeOptional(recipe.getRawUnit(), FluidUnit::write);
         buf.writeBoolean(recipe.canFill());
      }
   }
}
