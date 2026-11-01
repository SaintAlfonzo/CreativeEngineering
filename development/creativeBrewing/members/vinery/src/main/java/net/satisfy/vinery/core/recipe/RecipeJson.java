package net.satisfy.vinery.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Port: reads 1.20.5 item stacks ({@code {"id": ..., "count": ...}}) and 1.19.2 ones ({@code "item"}) from recipe JSON. */
final class RecipeJson {
    private RecipeJson() {
    }

    static ItemStack stack(JsonObject json) {
        String id = json.has("id") ? GsonHelper.getAsString(json, "id") : GsonHelper.getAsString(json, "item");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) {
            throw new JsonParseException("Unknown item " + id);
        }
        return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
    }

    static boolean wineBottleRequired(JsonObject json) {
        return json.has("wine_bottle") && GsonHelper.getAsBoolean(GsonHelper.getAsJsonObject(json, "wine_bottle"), "required", false);
    }
}
