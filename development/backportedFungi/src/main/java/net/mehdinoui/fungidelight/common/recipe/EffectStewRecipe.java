package net.mehdinoui.fungidelight.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehdinoui.fungidelight.common.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Port addition (feature_writeup/fungi_delight.md 5.1.5). A shapeless recipe whose result is a suspicious stew carrying
 * one effect named by registry id. Upstream stored numeric EffectIds in the result NBT, which are registry slots and
 * point at a different effect in every pack. The effect is resolved when the stew is made and written with vanilla's
 * own saver, so the NBT always holds the live id.
 *
 * JSON: the shapeless fields ("ingredients", "result", optional "group") plus "effect" (registry id) and "duration"
 * (ticks).
 */
public class EffectStewRecipe extends ShapelessRecipe {
   private final ItemStack plainResult;
   private final ResourceLocation effectId;
   private final int duration;

   public EffectStewRecipe(ShapelessRecipe base, ResourceLocation effectId, int duration) {
      super(base.getId(), base.getGroup(), base.getResultItem(), base.getIngredients());
      // Item and count only: a result read back from the network already carries the effect NBT, and
      // saveMobEffect appends to the list.
      ItemStack result = base.getResultItem();
      this.plainResult = new ItemStack(result.getItem(), result.getCount());
      this.effectId = effectId;
      this.duration = duration;
   }

   private ItemStack stew() {
      ItemStack stack = this.plainResult.copy();
      MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(this.effectId);
      if (effect != null) {
         SuspiciousStewItem.saveMobEffect(stack, effect, this.duration);
      }
      return stack;
   }

   // JEI and the recipe book show the stew with its effect too.
   @Override
   public ItemStack getResultItem() {
      return this.stew();
   }

   @Override
   public ItemStack assemble(CraftingContainer container) {
      return this.stew();
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return ModRecipes.EFFECT_STEW.get();
   }

   public static class Serializer implements RecipeSerializer<EffectStewRecipe> {
      @Override
      public EffectStewRecipe fromJson(ResourceLocation id, JsonObject json) {
         ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
         ResourceLocation effect = new ResourceLocation(GsonHelper.getAsString(json, "effect"));
         int duration = GsonHelper.getAsInt(json, "duration");
         if (duration <= 0) {
            throw new JsonParseException("Effect stew duration must be positive: " + id);
         }
         return new EffectStewRecipe(base, effect, duration);
      }

      @Override
      public EffectStewRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
         ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf);
         ResourceLocation effect = buf.readResourceLocation();
         int duration = buf.readVarInt();
         return new EffectStewRecipe(base, effect, duration);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buf, EffectStewRecipe recipe) {
         RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
         buf.writeResourceLocation(recipe.effectId);
         buf.writeVarInt(recipe.duration);
      }
   }
}
