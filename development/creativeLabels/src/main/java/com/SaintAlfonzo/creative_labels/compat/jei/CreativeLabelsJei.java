package com.SaintAlfonzo.creative_labels.compat.jei;

import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.CreativeLabels;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.item.NoteColours;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI (sticky_notes.md 3.6, BRIEF 5): the plain note and the four specials are separate entries (subtype on
 * {@code CreativeLabels.Special}; dyed notes are not, like leather armour), and the special recipes, which JEI
 * can't show from their {@code CustomRecipe} matchers, get example {@link ShapelessRecipe}s under
 * {@code creative_labels:jei/...}: one per dye (plain note + dye), one per special (plain note + ingredient), the
 * copy (one written + eight blank -> nine) and the erase (written note + eraser -> blank note).
 */
@JeiPlugin
public class CreativeLabelsJei implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(CreativeLabels.ID, "jei");
    public static final String EXAMPLE_TEXT = "Buy milk";

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(CLRegistry.STICKY_NOTE.get(), (stack, context) -> {
            String special = NoteData.readSpecial(stack);
            return special == null ? IIngredientSubtypeInterpreter.NONE : special;
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, examples());
    }

    /** The example recipes (built fresh each call; items are registered by now). */
    public static List<CraftingRecipe> examples() {
        List<CraftingRecipe> out = new ArrayList<>();
        ItemStack plain = new ItemStack(CLRegistry.STICKY_NOTE.get());
        for (DyeColor dye : DyeColor.values()) {
            ItemStack result = plain.copy();
            NoteData.writeColor(result, NoteColours.of(dye));
            out.add(shapeless("dye_" + dye.getName(), result, plain, new ItemStack(DyeItem.byColor(dye))));
        }
        for (NoteColours.Special s : NoteColours.SPECIALS) {
            Item ingredient = s.ingredientItem();
            if (ingredient == null) continue;
            out.add(shapeless("special_" + s.name(), CLRegistry.STICKY_NOTE.get().special(s), plain, new ItemStack(ingredient)));
        }
        ItemStack written = plain.copy();
        NoteData.writeText(written, EXAMPLE_TEXT);
        ItemStack nine = written.copy();
        nine.setCount(9);
        ItemStack[] copyInputs = new ItemStack[9];
        copyInputs[0] = written;
        for (int i = 1; i < 9; i++) copyInputs[i] = plain;
        out.add(shapeless("copy", nine, copyInputs));
        out.add(shapeless("erase", plain, written, new ItemStack(CLRegistry.ERASER.get())));
        return out;
    }

    private static ShapelessRecipe shapeless(String name, ItemStack result, ItemStack... inputs) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack in : inputs) ingredients.add(Ingredient.of(in));
        return new ShapelessRecipe(new ResourceLocation(CreativeLabels.ID, "jei/" + name), "", result, ingredients);
    }
}
