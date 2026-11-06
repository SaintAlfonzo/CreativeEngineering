package com.SaintAlfonzo.creativecommon.compat.jei;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;

import gg.hipposgrumm.armor_trims.api.ArmortrimsApi;
import gg.hipposgrumm.armor_trims.compat.jei.ArmortrimRecipeMaker;
import gg.hipposgrumm.armor_trims.compat.jei.ArmortrimsRecipe;
import gg.hipposgrumm.armor_trims.compat.jei.IArmortrimsRecipe;
import gg.hipposgrumm.armor_trims.config.Config;
// Upgrade is a top-level class literally named SmithingTemplate$Upgrade (no InnerClasses entry), so it
// can't be written SmithingTemplate.Upgrade.
import gg.hipposgrumm.armor_trims.item.SmithingTemplate$Upgrade;
import gg.hipposgrumm.armor_trims.util.LargeItemLists;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The only class that touches Armor Trims, so {@link SmithingTemplateJEI} loads fine without it.
 *
 * <p><b>Why this exists.</b> Armor Trims 1.3.3's own JEI plugin registers its two categories under
 * {@code RecipeType(uid, ArmortrimsRecipe.class)} but adds recipes under {@code RecipeType(uid,
 * IArmortrimsRecipe.class)}. JEI 11.8 matches on both, throws "no recipe category registered" on the first
 * {@code addRecipes}, and both pages stay empty (seen in logs/latest.log 2026-10-05). This adds the recipes
 * under the type the categories actually use:
 * <ul>
 *   <li>trimming: Armor Trims' own maker, unchanged (bounded lists, ran in under a second);</li>
 *   <li>upgrading: built here from the real smithing recipes that need a template, one entry per base item.
 *       Armor Trims' own upgrade maker is NOT used: it tries every JEI item x every template x every
 *       registered item, millions of recipe lookups.</li>
 * </ul>
 *
 * <p><b>Which recipes need a template.</b> Armor Trims' table ({@code SmithingMenuNew#createResult},
 * bytecode-read 2026-10-05) refuses a template-less recipe when the material is in a tag listed in
 * {@code ArmortrimsApi.upgradeBaseBlockedConditions} whose condition is on - by default
 * {@code #forge:ingots/netherite}, switched by {@code "Disable Vanilla Netherite Upgrading"} in
 * {@code config/armor_trims.toml}. With the template it needs the material in the template's tag and the
 * template's own condition on ({@code determiningUpgradeConditions}).
 */
final class ArmorTrimsSmithing {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RecipeType<ArmortrimsRecipe> TRIMMING =
            new RecipeType<>(new ResourceLocation("armor_trims", "armor_trimming"), ArmortrimsRecipe.class);
    private static final RecipeType<ArmortrimsRecipe> UPGRADING =
            new RecipeType<>(new ResourceLocation("armor_trims", "item_upgrading"), ArmortrimsRecipe.class);

    /** {@code UpgradeRecipe.base} is package-private; SRG name because the pack only runs reobfuscated. */
    private static Field baseField;

    private ArmorTrimsSmithing() {}

    /** True when Armor Trims' own smithing table and JEI categories are both in use. */
    static boolean active() {
        return Config.enableNewSmithingGUI() && Config.enableJei();
    }

    /** Armor Trims' trim recipes, under the type its trimming category was registered with. */
    static int registerTrims(IRecipeRegistration registration) {
        List<ArmortrimsRecipe> trims = new ArrayList<>();
        for (IArmortrimsRecipe r : ArmortrimRecipeMaker.getTrimmingRecipes(
                new ArmortrimRecipeMaker.ArmortrimsRecipeFactory(), registration.getIngredientManager())) {
            if (r instanceof ArmortrimsRecipe recipe) {
                trims.add(recipe);
            }
        }
        registration.addRecipes(TRIMMING, trims);
        return trims.size();
    }

    /**
     * Adds an Item Upgrading entry (base + template + material) for every smithing recipe that needs a
     * template, and returns the ids of the recipes now shown there - the ones to hide from the vanilla page.
     */
    static Set<ResourceLocation> registerUpgrades(IRecipeRegistration registration, Collection<UpgradeRecipe> smithing) {
        List<ArmortrimsRecipe> entries = new ArrayList<>();
        Set<ResourceLocation> shown = new HashSet<>();
        for (TagKey<Item> tag : templateOnlyMaterialTags()) {
            ItemStack template = templateFor(tag);
            if (template.isEmpty()) {
                continue; // template switched off or missing: the recipe can't be made at all, leave JEI as is
            }
            List<ItemStack> materials = new ArrayList<>();
            ForgeRegistries.ITEMS.tags().getTag(tag).forEach(item -> materials.add(new ItemStack(item)));
            for (UpgradeRecipe recipe : smithing) {
                ItemStack material = materials.stream().filter(recipe::isAdditionIngredient).findFirst().orElse(ItemStack.EMPTY);
                if (material.isEmpty()) {
                    continue;
                }
                ItemStack[] bases = base(recipe).getItems();
                for (ItemStack base : bases) {
                    entries.add(new ArmortrimsRecipe(base.copy(), template.copy(), material.copy()));
                }
                if (bases.length > 0) {
                    shown.add(recipe.getId());
                }
            }
        }
        registration.addRecipes(UPGRADING, entries);
        return shown;
    }

    /** Material tags a smithing recipe can't use without the matching upgrade template, right now. */
    private static List<TagKey<Item>> templateOnlyMaterialTags() {
        List<TagKey<Item>> out = new ArrayList<>();
        for (Map.Entry<Pair<TagKey<Item>, Item>, Supplier<Boolean>> e : ArmortrimsApi.upgradeBaseBlockedConditions.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue().get())) {
                out.add(e.getKey().getFirst());
            }
        }
        return out;
    }

    /** The upgrade template for {@code tag}, or EMPTY when there is none or its condition is off. */
    private static ItemStack templateFor(TagKey<Item> tag) {
        for (Map.Entry<Pair<TagKey<Item>, Item>, Supplier<Boolean>> e : ArmortrimsApi.determiningUpgradeConditions.entrySet()) {
            if (e.getKey().getFirst().equals(tag) && !Boolean.TRUE.equals(e.getValue().get())) {
                return ItemStack.EMPTY;
            }
        }
        for (Item item : LargeItemLists.getUpgradeSmithingTemplates()) {
            if (item instanceof SmithingTemplate$Upgrade upgrade && upgrade.getTag().equals(tag)) {
                return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

    private static Ingredient base(UpgradeRecipe recipe) {
        try {
            if (baseField == null) {
                baseField = ObfuscationReflectionHelper.findField(UpgradeRecipe.class, "f_44518_");
            }
            return (Ingredient) baseField.get(recipe);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("[jei] couldn't read the base of smithing recipe {}", recipe.getId(), e);
            return Ingredient.EMPTY;
        }
    }
}
