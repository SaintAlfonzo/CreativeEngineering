package com.SaintAlfonzo.creativeengineering.gear;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Duplicates an Elytra: whichever Elytra is placed in the grid comes back to the player
 * untouched - enchantments and durability intact - while the catalyst is consumed and a
 * brand-new item (recipe-defined, normally a plain vanilla Elytra) is produced as output.
 *
 * <p>Modelled on Quark's own Dragon Scales module - see
 * {@code DecompiledMods/Quark-3.4-418/vazkii/quark/content/tweaks/recipe/ElytraDuplicationRecipe.class}.
 * That recipe hardcodes both {@code quark:dragon_scale} as the catalyst and vanilla
 * {@code minecraft:elytra} as the output in Java, so it can't recognise this pack's own
 * {@code dragonloot:dragon_scale} (Quark's own dragon scale item is unobtainable here -
 * see startup_scripts/_lib/removed_items.js). This class re-implements the same
 * matches()/assemble()/getRemainingItems() behaviour, but reads the catalyst ingredient
 * and the result item from the recipe JSON instead of naming either in Java, so it isn't
 * tied to one mod's items. Datapack / KubeJS type id: {@code creativeengineering:elytra_duplication}
 * (see NCH.elytraDuplication in kubejs/startup_scripts/func/neoCraftHelper.js).
 */
public class ElytraDuplicationRecipe extends CustomRecipe {

    private final Ingredient catalyst;
    private final ItemStack result;

    public ElytraDuplicationRecipe(ResourceLocation id, Ingredient catalyst, ItemStack result) {
        super(id);
        this.catalyst = catalyst;
        this.result = result;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level level) {
        boolean sawElytra = false;
        boolean sawCatalyst = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ElytraItem) {
                if (sawElytra) return false;
                sawElytra = true;
            } else if (catalyst.test(stack)) {
                if (sawCatalyst) return false;
                sawCatalyst = true;
            } else {
                // any third item in the grid invalidates the recipe entirely
                return false;
            }
        }
        return sawElytra && sawCatalyst;
    }

    @Override
    @Nonnull
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        return result.copy();
    }

    @Override
    @Nonnull
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    @Nonnull
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer inv) {
        // This is what makes it a *duplication*: the Elytra slot gets its own stack
        // (NBT and all - enchantments, durability) handed straight back instead of being
        // consumed. Everything else (the catalyst) defaults to ItemStack.EMPTY, i.e. gone.
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ElytraItem) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    @Nonnull
    public NonNullList<Ingredient> getIngredients() {
        // Cosmetic only (recipe book / JEI grid preview) - actual matching in matches()
        // above accepts ANY ElytraItem, not just vanilla's.
        NonNullList<Ingredient> ingredients = NonNullList.withSize(2, Ingredient.EMPTY);
        ingredients.set(0, Ingredient.of(Items.ELYTRA));
        ingredients.set(1, catalyst);
        return ingredients;
    }

    @Override
    @Nonnull
    public RecipeSerializer<?> getSerializer() {
        return ElytraDuplicationSerializers.SERIALIZER.get();
    }
}
