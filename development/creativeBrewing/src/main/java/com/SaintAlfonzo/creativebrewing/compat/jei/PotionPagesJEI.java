package com.SaintAlfonzo.creativebrewing.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativebrewing.CreativeBrewing;
import com.SaintAlfonzo.creativecommon.compat.jei.RecipeRows;
import com.SaintAlfonzo.creativecommon.compat.jei.RowPagesCategory;
import com.SaintAlfonzo.creativecommon.compat.jei.RowPagesCategory.Page;
import com.SaintAlfonzo.creativecommon.compat.jei.RowPagesCategory.Row;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One JEI page per potion for the pack's per-potion recipes, instead of hundreds of near-identical recipes spread over
 * four machine pages:
 * <ul>
 *   <li><b>Tipped Arrows</b>: by hand (vanilla's 8 arrows + a lingering potion, which JEI shows as its own
 *       {@code minecraft:jei.tipped.arrow.*} crafts), the Spout and the Bottling Machine
 *       ({@code kubejs/server_scripts/equipment/tipped_arrows.js});</li>
 *   <li><b>Potion Buckets</b>, one page per potion and bucket: fill and drain by hand, the Spout, the Item Drain and
 *       the Bottling Machine ({@code kubejs/server_scripts/engineering/potion_bottling.js}).</li>
 * </ul>
 * Recipes are found by their id ({@code creativebrewing:<prefix>/<potion key>}); rows of one key share a page.
 * {@link #onRuntimeAvailable} hides exactly what was re-shown. Gated by {@code [jei] tippedArrowCategory} and
 * {@code [jei] potionBucketCategory}; re-run on every JEI reload.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class PotionPagesJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeBrewing.NS, "potion_pages");

    public static final RecipeType<Page> TIPPED_ARROWS = RecipeType.create(CreativeBrewing.NS, "tipped_arrows", Page.class);
    public static final RecipeType<Page> POTION_BUCKETS = RecipeType.create(CreativeBrewing.NS, "potion_buckets", Page.class);

    private static final ResourceLocation SPOUT = new ResourceLocation("create", "spout");
    private static final ResourceLocation ITEM_DRAIN = new ResourceLocation("create", "item_drain");
    private static final ResourceLocation BOTTLING_MACHINE = new ResourceLocation("immersiveengineering", "bottling_machine");
    private static final ResourceLocation POTION_BUCKET = new ResourceLocation(CreativeBrewing.NS, "potion_bucket");

    /** JEI page ids the moved recipes came from. */
    private static final ResourceLocation PAGE_CRAFTING = RecipeTypes.CRAFTING.getUid();
    private static final ResourceLocation PAGE_SPOUT = new ResourceLocation("create", "spout_filling");
    private static final ResourceLocation PAGE_DRAIN = new ResourceLocation("create", "draining");
    private static final ResourceLocation PAGE_BOTTLING = new ResourceLocation("immersiveengineering", "bottling_machine");

    /** JEI's own crafting pages for vanilla's tipped arrow craft, one per potion. */
    private static final String JEI_TIPPED_ARROW = "jei.tipped.arrow";
    private static final ResourceLocation VANILLA_TIPPED_ARROW = new ResourceLocation("minecraft", "tipped_arrow");

    /** A row kind: the id prefix of its recipes, the page it came from, how to read it, which machine. */
    private record Kind(String prefix, ResourceLocation page, BiFunction<Recipe<?>, ItemStack, Row> reader, ResourceLocation machine) {
    }

    private static final ResourceLocation TABLE = new ResourceLocation("minecraft", "crafting_table");

    /** Row order on a Potion Buckets page. */
    private static final List<Kind> BUCKET_KINDS = List.of(
            new Kind("shapeless/potion_bucket_fill/", PAGE_CRAFTING, RecipeRows::crafting, TABLE),
            new Kind("shapeless/potion_bucket_drain/", PAGE_CRAFTING, RecipeRows::crafting, TABLE),
            new Kind("create_filling/potion_bucket/", PAGE_SPOUT, RecipeRows::create, SPOUT),
            new Kind("create_emptying/potion_bucket/", PAGE_DRAIN, RecipeRows::create, ITEM_DRAIN),
            new Kind("bottling/potion_bucket/", PAGE_BOTTLING, RecipeRows::bottling, BOTTLING_MACHINE));

    /** Row order on a Tipped Arrows page, after the hand craft. */
    private static final List<Kind> ARROW_KINDS = List.of(
            new Kind("createfilling/tipped_arrow/", PAGE_SPOUT, RecipeRows::create, SPOUT),
            new Kind("bottling/tipped_arrow/", PAGE_BOTTLING, RecipeRows::bottling, BOTTLING_MACHINE));

    /** Recipe ids now on these pages, per page they came from (set by the last reload). */
    private Map<ResourceLocation, Set<ResourceLocation>> moved = Map.of();
    private boolean hideJeiTippedArrows;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        ItemStack arrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.HEALING);
        ItemStack bucket = RecipeRows.stack(POTION_BUCKET);
        bucket = bucket.isEmpty() ? new ItemStack(Items.WATER_BUCKET) : PotionUtils.setPotion(bucket, Potions.HEALING);
        registration.addRecipeCategories(
                new RowPagesCategory(gui, TIPPED_ARROWS, "creativebrewing.jei.tipped_arrows",
                        gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, arrow), 1 + ARROW_KINDS.size()),
                new RowPagesCategory(gui, POTION_BUCKETS, "creativebrewing.jei.potion_buckets",
                        gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, bucket), BUCKET_KINDS.size()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ResourceLocation machine : List.of(TABLE, SPOUT, BOTTLING_MACHINE)) {
            ItemStack stack = RecipeRows.stack(machine);
            if (!stack.isEmpty()) {
                registration.addRecipeCatalyst(stack, TIPPED_ARROWS);
            }
        }
        for (ResourceLocation machine : List.of(TABLE, SPOUT, ITEM_DRAIN, BOTTLING_MACHINE)) {
            ItemStack stack = RecipeRows.stack(machine);
            if (!stack.isEmpty()) {
                registration.addRecipeCatalyst(stack, POTION_BUCKETS);
            }
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        moved = Map.of();
        hideJeiTippedArrows = false;
        if (Minecraft.getInstance().level == null) {
            return;
        }
        boolean arrows = CommonConfig.JEI.tippedArrowCategory.get();
        boolean buckets = CommonConfig.JEI.potionBucketCategory.get();
        if (!arrows && !buckets) {
            return;
        }
        Map<ResourceLocation, Set<ResourceLocation>> movedNow = new HashMap<>();
        try {
            RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
            Map<String, Row[]> arrowRows = new TreeMap<>();
            Map<String, Row[]> bucketRows = new TreeMap<>();

            if (arrows && manager.byKey(VANILLA_TIPPED_ARROW).isPresent()) {
                addHandTipping(arrowRows);
                hideJeiTippedArrows = true;
            }
            for (Recipe<?> recipe : manager.getRecipes()) {
                ResourceLocation id = recipe.getId();
                if (!id.getNamespace().equals(CreativeBrewing.NS)) {
                    continue;
                }
                if (arrows) {
                    collect(recipe, ARROW_KINDS, 1, arrowRows, movedNow);
                }
                if (buckets) {
                    collect(recipe, BUCKET_KINDS, 0, bucketRows, movedNow);
                }
            }

            List<Page> arrowPages = pages("tipped_arrows/", arrowRows);
            List<Page> bucketPages = pages("potion_buckets/", bucketRows);
            registration.addRecipes(TIPPED_ARROWS, arrowPages);
            registration.addRecipes(POTION_BUCKETS, bucketPages);
            moved = movedNow;
            LOGGER.info("[jei] Tipped Arrows: {} page(s); Potion Buckets: {} page(s); moved {} recipe(s)",
                    arrowPages.size(), bucketPages.size(), movedNow.values().stream().mapToInt(Set::size).sum());
        } catch (Throwable t) {
            moved = Map.of();
            hideJeiTippedArrows = false;
            LOGGER.warn("[jei] couldn't build the Tipped Arrows / Potion Buckets pages, leaving the recipes where they are", t);
        }
    }

    /**
     * Puts the recipe on its key's page if its id has one of {@code kinds}' prefixes and it reads as a row; the row
     * goes to slot {@code offset + kind index}.
     */
    private static void collect(Recipe<?> recipe, List<Kind> kinds, int offset, Map<String, Row[]> rows,
                                Map<ResourceLocation, Set<ResourceLocation>> movedNow) {
        String path = recipe.getId().getPath();
        for (int k = 0; k < kinds.size(); k++) {
            Kind kind = kinds.get(k);
            if (!path.startsWith(kind.prefix())) {
                continue;
            }
            ItemStack machine = RecipeRows.stack(kind.machine());
            Row row = machine.isEmpty() ? null : kind.reader().apply(recipe, machine);
            if (row != null) {
                rows.computeIfAbsent(path.substring(kind.prefix().length()), key -> new Row[offset + kinds.size()])[offset + k] = row;
                movedNow.computeIfAbsent(kind.page(), page -> new HashSet<>()).add(recipe.getId());
            }
            return;
        }
    }

    /** Vanilla's tipped arrow craft for every potion, as row 0 of its page (key as in tipped_arrows.js). */
    private static void addHandTipping(Map<String, Row[]> rows) {
        ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
        for (Potion potion : ForgeRegistries.POTIONS) {
            ResourceLocation id = ForgeRegistries.POTIONS.getKey(potion);
            if (potion == Potions.EMPTY || id == null) {
                continue;
            }
            Row row = RecipeRows.craft(table,
                    List.of(List.of(new ItemStack(Items.ARROW, 8)),
                            List.of(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion))),
                    PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), potion));
            if (row != null) {
                String key = id.toString().replace(':', '_').replace('/', '_');
                rows.computeIfAbsent(key, k -> new Row[1 + ARROW_KINDS.size()])[0] = row;
            }
        }
    }

    private static List<Page> pages(String idPrefix, Map<String, Row[]> rows) {
        List<Page> pages = new ArrayList<>(rows.size());
        for (Map.Entry<String, Row[]> entry : rows.entrySet()) {
            List<Row> present = new ArrayList<>();
            for (Row row : entry.getValue()) {
                if (row != null) {
                    present.add(row);
                }
            }
            if (!present.isEmpty()) {
                pages.add(new Page(new ResourceLocation(CreativeBrewing.NS, idPrefix + entry.getKey()), present));
            }
        }
        return pages;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        try {
            IRecipeManager recipes = runtime.getRecipeManager();
            int hidden = RecipeRows.hide(recipes, moved);
            if (hideJeiTippedArrows) {
                List<CraftingRecipe> jeiArrows = recipes.createRecipeLookup(RecipeTypes.CRAFTING).includeHidden().get()
                        .filter(r -> r.getId().getNamespace().equals("minecraft") && r.getId().getPath().startsWith(JEI_TIPPED_ARROW))
                        .toList();
                if (!jeiArrows.isEmpty()) {
                    recipes.hideRecipes(RecipeTypes.CRAFTING, jeiArrows);
                }
                hidden += jeiArrows.size();
            }
            LOGGER.info("[jei] hid {} tipped arrow / potion bucket recipe(s) from their machine pages", hidden);
        } catch (Throwable t) {
            LOGGER.warn("[jei] couldn't hide the tipped arrow / potion bucket recipes from their machine pages", t);
        }
    }
}
