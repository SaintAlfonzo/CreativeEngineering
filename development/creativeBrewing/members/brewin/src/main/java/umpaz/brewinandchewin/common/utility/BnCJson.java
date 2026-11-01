package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Port-only: the JSON readers 1.21's codecs gave upstream for free. Recipe files keep 4.5.0's field names; item stacks
 * are {"id" (or "item"), "count", "nbt"} and NBT replaces data components.
 */
public final class BnCJson {
   private BnCJson() {
   }

   public static FluidUnit unit(JsonElement json) {
      FluidUnit unit = FluidUnit.byName(GsonHelper.convertToString(json, "unit"));
      if (unit == null) {
         throw new JsonParseException("Unknown fluid unit '" + json + "', expected liters, millibuckets or droplets");
      }
      return unit;
   }

   /** An "nbt" field given either as a JSON object or as an SNBT string. */
   @Nullable
   public static CompoundTag optionalNbt(JsonObject json, String key) {
      if (!json.has(key)) {
         return null;
      }
      JsonElement element = json.get(key);
      try {
         if (element.isJsonPrimitive()) {
            return TagParser.parseTag(element.getAsString());
         }
         Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
         if (tag instanceof CompoundTag compound) {
            return compound;
         }
      } catch (CommandSyntaxException e) {
         throw new JsonParseException("Invalid NBT in '" + key + "': " + e.getMessage());
      }
      throw new JsonParseException("'" + key + "' must be an object or an SNBT string");
   }

   public static ItemStack itemStack(JsonObject json) {
      String idKey = json.has("id") ? "id" : "item";
      ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, idKey));
      Item item = ForgeRegistries.ITEMS.getValue(id);
      if (item == null || !ForgeRegistries.ITEMS.containsKey(id)) {
         throw new JsonParseException("Unknown item '" + id + "'");
      }
      ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
      CompoundTag tag = optionalNbt(json, "nbt");
      if (tag != null) {
         stack.setTag(tag);
      }
      return stack;
   }

   public static JsonObject itemStackToJson(ItemStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("id", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
      json.addProperty("count", stack.getCount());
      if (stack.hasTag()) {
         json.addProperty("nbt", stack.getTag().toString());
      }
      return json;
   }

   /** Like Ingredient.fromJson, but an empty array is Ingredient.EMPTY (4.5.0 pads ingredient lists with []). */
   public static Ingredient ingredient(JsonElement json) {
      if (json.isJsonArray() && json.getAsJsonArray().isEmpty()) {
         return Ingredient.EMPTY;
      }
      return Ingredient.fromJson(json);
   }

   public static JsonArray asArray(JsonObject json, String key) {
      return GsonHelper.getAsJsonArray(json, key);
   }
}
