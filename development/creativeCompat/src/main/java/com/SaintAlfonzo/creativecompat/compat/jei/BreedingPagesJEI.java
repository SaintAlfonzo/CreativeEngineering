package com.SaintAlfonzo.creativecompat.compat.jei;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Just Enough Breeding pages for the pack's animals that the mod has no entry for (feature_writeup/breeding_audit.md 8).
 *
 * <p>Just Enough Breeding ships a hard-coded table per supported mod. Entries here are pack animals missing from it:
 * Untitled Duck's duck and goose (wild-seed list read from the mod's classes), Buzzier Bees' grizzly bear (its item tag),
 * Fungi Delight's inky cap and morel cows, Cozy's Improved Cats' cat. The pages are built through Just Enough Breeding's
 * own recipe class by reflection (no compile dependency), and every entry is skipped when its mod, entity or item is
 * absent. Tag-driven pages read the live tag, so tag edits show without a rebuild.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class BreedingPagesJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeCompat.MOD_ID, "breeding_pages");
    private static final String JEBR = "justenoughbreeding";

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        JeiGate.run("breedingPages", () -> {
            try {
                register(registration);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("[jei] Just Enough Breeding extra pages skipped: {}", e.toString());
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void register(IRecipeRegistration registration) throws ReflectiveOperationException {
        if (!ModList.get().isLoaded(JEBR)) {
            return;
        }
        Class<?> category = Class.forName("com.christofmeg.justenoughbreeding.jei.BreedingCategory");
        RecipeType type = (RecipeType) category.getField("TYPE").get(null);
        Class<?> recipe = Class.forName("com.christofmeg.justenoughbreeding.recipe.BreedingRecipe");
        Constructor<?> ctor = recipe.getConstructor(EntityType.class, Ingredient.class, ItemStack.class, Boolean.class,
                Ingredient.class, Ingredient.class, Boolean.class);

        List<Object> out = new ArrayList<>();
        add(out, ctor, "untitledduckmod:duck",
                Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS));
        add(out, ctor, "untitledduckmod:goose",
                Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS,
                        Items.KELP, Items.SEAGRASS));
        add(out, ctor, "buzzier_bees:grizzly_bear",
                Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("buzzier_bees", "grizzly_bear_food"))));
        add(out, ctor, "fungidelight:inky_cap_cow", Ingredient.of(Items.WHEAT));
        add(out, ctor, "fungidelight:morel_cow", Ingredient.of(Items.WHEAT));
        add(out, ctor, "cozys-improved-cats:improved_cat", Ingredient.of(Items.COD, Items.SALMON));

        if (!out.isEmpty()) {
            registration.addRecipes(type, out);
        }
        LOGGER.info("[jei] Just Enough Breeding: {} extra breeding page(s)", out.size());
    }

    private static void add(List<Object> out, Constructor<?> ctor, String entityId, Ingredient food)
            throws ReflectiveOperationException {
        ResourceLocation id = new ResourceLocation(entityId);
        if (!ModList.get().isLoaded(id.getNamespace()) || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            return;
        }
        EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(id);
        ForgeSpawnEggItem egg = entity == null ? null : ForgeSpawnEggItem.fromEntityType(entity) instanceof ForgeSpawnEggItem f ? f : null;
        if (egg == null || food.isEmpty()) {
            return;
        }
        out.add(ctor.newInstance(entity, food, new ItemStack(egg), false, Ingredient.EMPTY, Ingredient.EMPTY, false));
    }
}
