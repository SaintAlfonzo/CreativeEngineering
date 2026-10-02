package io.github.mortuusars.exposure.backport.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.Exposure;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * Recipe JSON that reads both the 1.19.2 and the 1.20.5+ shapes, so upstream's recipe data (and addon/pack data
 * written for 1.21) parses here:
 * <ul>
 * <li>item stack: {@code {"item"|"id": ..., "count": n, "nbt": ...|"components": {...}}}</li>
 * <li>ingredient: {@code {"items": "id"|"#tag"}}, and NeoForge's {@code "neoforge:components"} ingredient (mapped to
 * Forge's {@code forge:partial_nbt}; component data -> NBT via {@link #componentsToNbt})</li>
 * </ul>
 */
public final class BackportRecipes {
    private BackportRecipes() {
    }

    public static ItemStack itemStackFromJson(JsonObject json) {
        JsonObject copy = json.deepCopy();
        if (!copy.has("item") && copy.has("id")) {
            copy.add("item", copy.remove("id"));
        }
        JsonElement components = copy.remove("components");
        ItemStack stack = CraftingHelper.getItemStack(copy, true, true);
        if (components != null && components.isJsonObject()) {
            CompoundTag nbt = componentsToNbt(components.getAsJsonObject());
            if (!nbt.isEmpty()) {
                stack.getOrCreateTag().merge(nbt);
            }
        }
        return stack;
    }

    public static Ingredient ingredientFromJson(JsonElement json) {
        return Ingredient.fromJson(normalizeIngredient(json));
    }

    public static JsonElement normalizeIngredient(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement element : json.getAsJsonArray()) {
                array.add(normalizeIngredient(element));
            }
            return array;
        }
        if (!json.isJsonObject()) {
            return json;
        }
        JsonObject obj = json.getAsJsonObject().deepCopy();
        JsonElement items = obj.get("items");
        if (items != null && items.isJsonPrimitive()) {
            obj.remove("items");
            String value = items.getAsString();
            if (value.startsWith("#")) {
                obj.addProperty("tag", value.substring(1));
            } else {
                obj.addProperty("item", value);
            }
        }
        if ("neoforge:components".equals(GsonHelper.getAsString(obj, "type", ""))) {
            JsonObject partial = new JsonObject();
            partial.addProperty("type", "forge:partial_nbt");
            if (obj.has("item")) {
                partial.add("item", obj.get("item"));
            } else if (obj.has("items") && obj.get("items").isJsonArray()) {
                partial.add("items", obj.get("items"));
            } else {
                throw new JsonParseException("neoforge:components ingredient without items: " + json);
            }
            JsonObject components = GsonHelper.getAsJsonObject(obj, "components", new JsonObject());
            partial.addProperty("nbt", componentsToNbt(components).toString());
            return partial;
        }
        return obj;
    }

    /**
     * 1.21 item components -> the 1.19.2 NBT that holds the same data. Exposure's own components keep their id as
     * the NBT key (see DataComponentType). Vanilla ones are mapped where 1.19.2 has an equivalent.
     */
    public static CompoundTag componentsToNbt(JsonObject components) {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            String key = entry.getKey().startsWith("!") ? null : new ResourceLocation(entry.getKey()).toString();
            if (key == null) {
                continue;
            }
            JsonElement value = entry.getValue();
            switch (key) {
                case "minecraft:potion_contents" -> {
                    String potion = value.isJsonPrimitive() ? value.getAsString() : GsonHelper.getAsString(value.getAsJsonObject(), "potion", null);
                    if (potion != null) {
                        nbt.putString("Potion", potion);
                    }
                }
                case "minecraft:custom_name" -> {
                    CompoundTag display = nbt.getCompound("display");
                    display.putString("Name", value.isJsonPrimitive() ? value.getAsString() : value.toString());
                    nbt.put("display", display);
                }
                case "minecraft:custom_data" -> {
                    try {
                        nbt.merge(value.isJsonPrimitive() ? TagParser.parseTag(value.getAsString())
                                : (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, value));
                    } catch (CommandSyntaxException | ClassCastException e) {
                        throw new JsonParseException("Invalid custom_data: " + value, e);
                    }
                }
                default -> {
                    if (key.startsWith("minecraft:")) {
                        Exposure.LOGGER.warn("Item component '{}' has no 1.19.2 NBT mapping, ignored: {}", key, value);
                    } else {
                        Tag tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, value);
                        nbt.put(key, tag);
                    }
                }
            }
        }
        return nbt;
    }

    public static Item itemFromId(String id) {
        return Registry.ITEM.getOptional(new ResourceLocation(id)).orElseThrow(() -> new JsonParseException("Unknown item '" + id + "'"));
    }
}
