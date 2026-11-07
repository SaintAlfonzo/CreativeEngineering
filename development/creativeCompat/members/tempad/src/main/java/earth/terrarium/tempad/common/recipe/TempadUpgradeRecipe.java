// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/recipe/TempadUpgradeRecipe.kt. Translated to Java for Forge
// 1.19.2: MapCodec -> JSON (same keys: item, download_time, upgrade), StreamCodec -> FriendlyByteBuf. Marked special
// so the 1.19.2 recipe book skips the unknown type (upstream has no recipe book entry for it either). Its user, the
// workstation, arrives in phase 2.
package earth.terrarium.tempad.common.recipe;

import com.google.gson.JsonObject;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.registries.ModRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record TempadUpgradeRecipe(ResourceLocation id, Ingredient upgrade, int downloadTime, ResourceLocation output) implements Recipe<UpgradeRecipeInput> {
    @Override
    public boolean matches(UpgradeRecipeInput input, Level level) {
        return upgrade.test(input.upgrade());
    }

    @Override
    public ItemStack assemble(UpgradeRecipeInput input) {
        ItemStack result = input.upgradable().copy();
        ModComponents.setInstalledUpgrades(result, ModComponents.getInstalledUpgrades(result).plus(output));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        ItemStack stack = new ItemStack(ModItems.tempad.get());
        ModComponents.setInstalledUpgrades(stack, ModComponents.getInstalledUpgrades(stack).plus(output));
        return stack;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.upgradeSerializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.upgradeRecipe.get();
    }

    public static class Serializer implements RecipeSerializer<TempadUpgradeRecipe> {
        @Override
        public TempadUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new TempadUpgradeRecipe(id, Ingredient.fromJson(json.get("item")), GsonHelper.getAsInt(json, "download_time"),
                    new ResourceLocation(GsonHelper.getAsString(json, "upgrade")));
        }

        @Override
        public TempadUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new TempadUpgradeRecipe(id, Ingredient.fromNetwork(buf), buf.readInt(), buf.readResourceLocation());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, TempadUpgradeRecipe recipe) {
            recipe.upgrade().toNetwork(buf);
            buf.writeInt(recipe.downloadTime());
            buf.writeResourceLocation(recipe.output());
        }
    }
}
