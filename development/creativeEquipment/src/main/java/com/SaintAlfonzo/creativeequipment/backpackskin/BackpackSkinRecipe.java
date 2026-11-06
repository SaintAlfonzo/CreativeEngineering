package com.SaintAlfonzo.creativeequipment.backpackskin;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

/**
 * Smithing: any Sophisticated Backpack + a block item from the skin tag gives the same backpack (all data
 * kept) with the skin set. Vanilla smithing drops the base's data, so this is a custom {@link UpgradeRecipe}.
 * Type {@code creativeengineering:backpack_skin}; emitted by {@code global.NCH.backpackSkin}.
 */
public class BackpackSkinRecipe extends UpgradeRecipe {

    private final Ingredient skins;

    public BackpackSkinRecipe(ResourceLocation id, Ingredient skins) {
        super(id, backpacks(), skins, exampleResult());
        this.skins = skins;
    }

    private static Ingredient backpacks() {
        return Ingredient.of(ForgeRegistries.ITEMS.getValues().stream()
                .filter(i -> i instanceof BackpackItem).map(ItemStack::new));
    }

    private static ItemStack exampleResult() {
        return ForgeRegistries.ITEMS.getValues().stream().filter(i -> i instanceof BackpackItem)
                .findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    public Ingredient getSkins() {
        return skins;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack base = inv.getItem(0);
        ItemStack add = inv.getItem(1);
        return BackpackSkins.isBackpack(base) && skins.test(add) && BackpackSkins.skinBlockOf(add) != null;
    }

    @Override
    public ItemStack assemble(Container inv) {
        Block skin = BackpackSkins.skinBlockOf(inv.getItem(1));
        return skin == null ? ItemStack.EMPTY : BackpackSkins.withSkin(inv.getItem(0), skin);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BackpackSkinSerializers.SMITHING.get();
    }

    public static final class Serializer implements RecipeSerializer<BackpackSkinRecipe> {
        @Override
        public BackpackSkinRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new BackpackSkinRecipe(id, CraftingHelper.getIngredient(json.get("addition")));
        }

        @Override
        public BackpackSkinRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new BackpackSkinRecipe(id, Ingredient.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BackpackSkinRecipe recipe) {
            recipe.skins.toNetwork(buf);
        }
    }
}
