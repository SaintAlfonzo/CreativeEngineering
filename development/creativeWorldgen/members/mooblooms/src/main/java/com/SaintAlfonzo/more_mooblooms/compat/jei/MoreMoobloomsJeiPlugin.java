package com.SaintAlfonzo.more_mooblooms.compat.jei;

import com.SaintAlfonzo.more_mooblooms.MMConfig;
import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.compat.BuzzierBeesSet;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The jei content set:
 * <ul>
 *   <li>each variant's egg is its own JEI entry (subtypes by {@code EntityTag.Variant}, D14)</li>
 *   <li>a short info page per variant egg: its flower pool with the odds, what converts a cow, shearing, food</li>
 *   <li>"Moobloom Breeding" ({@link MoobloomBreedingCategory}): combination pairs with the miss, breeds-true pairs
 *       and the random calf, each parent with its food</li>
 *   <li>"Moobloom Shearing" ({@link MoobloomShearingCategory}): per variant, the drops per shear and the rendered
 *       moobloom</li>
 *   <li>"Moobloom Conversion" ({@link MoobloomConversionCategory}): per variant, cow + its food with the feed chance
 *       and the share of a food several variants have</li>
 *   <li>with Buzzier Bees: hides the buttercup and its moobloom egg, the Sunny effect entry that Just Enough
 *       Effect Descriptions adds (found by ingredient class, so there is no compile dependency on JEED) and Just
 *       Enough Breeding's page for BB's moobloom (found by recipe type id, no dependency on it either)</li>
 * </ul>
 * The variant table is already synced when JEI starts (our packet is sent before the recipe and tag packets).
 */
@JeiPlugin
public class MoreMoobloomsJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(MoreMooblooms.ID, "jei");
    private static final ResourceLocation JEBR_BREEDING = new ResourceLocation("justenoughbreeding", "breeding");
    private static final ResourceLocation BB_EGG = new ResourceLocation(BuzzierBeesSet.MOD_ID, "moobloom_spawn_egg");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get(),
                (stack, context) -> {
                    ResourceLocation id = MoobloomSpawnEggItem.variantOf(stack);
                    return id == null ? IIngredientSubtypeInterpreter.NONE : id.toString();
                });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new MoobloomBreedingCategory(gui), new MoobloomShearingCategory(gui),
                new MoobloomConversionCategory(gui));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get()), MoobloomBreedingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.SHEARS), MoobloomShearingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.COW_SPAWN_EGG), MoobloomConversionCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(MoobloomBreedingCategory.TYPE, breedingRecipes());
        registration.addRecipes(MoobloomShearingCategory.TYPE, shearingRecipes());
        registration.addRecipes(MoobloomConversionCategory.TYPE, conversionRecipes());

        String feedPercent = percent(MMConfig.num(MMConfig.FEED_CHANCE));
        boolean bareToCow = MMConfig.on(MMConfig.SHEAR_BARE_TO_COW);
        boolean bareDrops = MMConfig.on(MMConfig.BARE_SHEAR_DROPS);
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("jei.more_mooblooms.info.pool", poolOdds(v)));
            lines.add(Component.translatable("jei.more_mooblooms.info.feed", names(food(v)), feedPercent));
            lines.add(Component.translatable("jei.more_mooblooms.info.shear"));
            if (bareToCow) lines.add(Component.translatable(bareDrops ? "jei.more_mooblooms.info.bare_drops" : "jei.more_mooblooms.info.bare"));
            lines.add(Component.translatable("jei.more_mooblooms.info.breed"));
            if (v.breedWeight() == 0) lines.add(Component.translatable("jei.more_mooblooms.info.never_random"));
            registration.addIngredientInfo(MoobloomSpawnEggItem.forVariant(v.id()), VanillaTypes.ITEM_STACK, lines.toArray(Component[]::new));
        }
    }

    /**
     * Combination pairs whose variants all exist (each result at rule chance x result share, plus the miss: the
     * same kind for a same-kind pair, otherwise a random calf), every variant with no rule for itself breeding
     * true, and one page for two different kinds with no rule.
     */
    private static List<MoobloomBreedingCategory.Recipe> breedingRecipes() {
        MoobloomBreedingCategory.Outcome random = randomCalf(1.0);
        List<MoobloomBreedingCategory.Recipe> out = new ArrayList<>();
        for (MoobloomBreeding.Rule rule : MoobloomBreeding.rules(true)) {
            ResourceLocation a = rule.parentA(), b = rule.parentB();
            if (!MoobloomVariants.has(true, a) || !MoobloomVariants.has(true, b)) continue;
            // a pair an earlier rule already claims never reaches this one
            if (MoobloomBreeding.ruleFor(true, a, b) != rule) continue;
            List<MoobloomBreeding.Result> results = rule.knownResults(true);
            int total = results.stream().mapToInt(MoobloomBreeding.Result::weight).sum();
            List<MoobloomBreedingCategory.Outcome> outcomes = new ArrayList<>();
            double hit = total > 0 ? rule.chance() : 0.0;
            for (MoobloomBreeding.Result r : results) {
                outcomes.add(MoobloomBreedingCategory.Outcome.of(MoobloomSpawnEggItem.forVariant(r.variant()),
                        (double) rule.chance() * r.weight() / total));
            }
            if (hit < 1.0) {
                outcomes.add(a.equals(b) ? MoobloomBreedingCategory.Outcome.of(MoobloomSpawnEggItem.forVariant(a), 1.0 - hit)
                        : randomCalf(1.0 - hit));
            }
            out.add(new MoobloomBreedingCategory.Recipe(MoobloomSpawnEggItem.forVariant(a), MoobloomSpawnEggItem.forVariant(b),
                    food(MoobloomVariants.get(true, a)), food(MoobloomVariants.get(true, b)), outcomes, false));
        }
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            if (MoobloomBreeding.ruleFor(true, v.id(), v.id()) != null) continue;
            ItemStack egg = MoobloomSpawnEggItem.forVariant(v.id());
            out.add(new MoobloomBreedingCategory.Recipe(egg, egg, food(v), food(v),
                    List.of(MoobloomBreedingCategory.Outcome.of(egg, 1.0)), false));
        }
        if (!random.eggs().isEmpty()) {
            ItemStack plain = new ItemStack(MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get());
            out.add(new MoobloomBreedingCategory.Recipe(plain, plain, List.of(), List.of(), List.of(random), true));
        }
        return out;
    }

    /** A random calf at {@code chance}: every variant with a {@code breed_weight}, each its share of the total. */
    private static MoobloomBreedingCategory.Outcome randomCalf(double chance) {
        int total = MoobloomVariants.values(true).stream().mapToInt(MoobloomVariant::breedWeight).sum();
        List<ItemStack> eggs = new ArrayList<>();
        List<Double> shares = new ArrayList<>();
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            if (v.breedWeight() <= 0) continue;
            eggs.add(MoobloomSpawnEggItem.forVariant(v.id()));
            shares.add((double) v.breedWeight() / total);
        }
        return new MoobloomBreedingCategory.Outcome(eggs, shares, chance);
    }

    /** Per variant: a cow fed any of its food (a variant with no food has no page). */
    private static List<MoobloomConversionCategory.Recipe> conversionRecipes() {
        double feedChance = MMConfig.num(MMConfig.FEED_CHANCE);
        List<MoobloomConversionCategory.Recipe> out = new ArrayList<>();
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            List<ItemStack> food = food(v);
            if (food.isEmpty() || feedChance <= 0.0) continue;
            out.add(new MoobloomConversionCategory.Recipe(v, MoobloomSpawnEggItem.forVariant(v.id()), food, feedChance));
        }
        return out;
    }

    /** Per variant: each pool item (entries with the same item merged) with its chance per flower position. */
    private static List<MoobloomShearingCategory.Recipe> shearingRecipes() {
        boolean bareToCow = MMConfig.on(MMConfig.SHEAR_BARE_TO_COW);
        boolean bareDrops = MMConfig.on(MMConfig.BARE_SHEAR_DROPS);
        List<MoobloomShearingCategory.Recipe> out = new ArrayList<>();
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            Map<Item, Double> perSpot = new LinkedHashMap<>();
            for (MoobloomVariant.Flower f : v.pool()) {
                perSpot.merge(f.drop(), (double) f.weight() * f.chance() / v.poolWeight(), Double::sum);
            }
            List<Map.Entry<Item, Double>> sorted = new ArrayList<>(perSpot.entrySet());
            sorted.sort((x, y) -> Double.compare(y.getValue(), x.getValue()));
            out.add(new MoobloomShearingCategory.Recipe(v, MoobloomSpawnEggItem.forVariant(v.id()),
                    sorted.stream().map(e -> new ItemStack(e.getKey())).toList(),
                    sorted.stream().map(Map.Entry::getValue).toList(), bareToCow, bareDrops));
        }
        return out;
    }

    /** "40% Poppy, 40% Red Tulip, 20% Rose Bush": each pool item's share, entries with the same item summed. */
    private static Component poolOdds(MoobloomVariant v) {
        Map<Item, Integer> weights = new LinkedHashMap<>();
        for (MoobloomVariant.Flower f : v.pool()) weights.merge(f.drop(), f.weight(), Integer::sum);
        List<Map.Entry<Item, Integer>> sorted = new ArrayList<>(weights.entrySet());
        sorted.sort((x, y) -> y.getValue() - x.getValue());
        MutableComponent out = Component.empty();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(percent((double) sorted.get(i).getValue() / v.poolWeight()) + "% ")
                    .append(new ItemStack(sorted.get(i).getKey()).getHoverName());
        }
        return out;
    }

    /** Pool items, then feed items and the items in feed tags (tags are synced before JEI starts). */
    static List<ItemStack> food(MoobloomVariant v) {
        Set<Item> items = new LinkedHashSet<>(v.flowerItems());
        for (TagKey<Item> tag : v.feedTags()) {
            for (Holder<Item> h : Registry.ITEM.getTagOrEmpty(tag)) items.add(h.value());
        }
        return items.stream().map(ItemStack::new).toList();
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (!MoreMooblooms.isLoaded(BuzzierBeesSet.MOD_ID)) return;
        IIngredientManager ingredients = runtime.getIngredientManager();
        hideJustEnoughBreedingPage(runtime);

        List<ItemStack> hidden = BuzzierBeesSet.hiddenItems().stream().map(ItemStack::new).toList();
        List<ItemStack> listed = new ArrayList<>();
        for (ItemStack stack : ingredients.getAllIngredients(VanillaTypes.ITEM_STACK)) {
            if (hidden.stream().anyMatch(h -> h.is(stack.getItem()))) listed.add(stack);
        }
        if (!listed.isEmpty()) ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, listed);

        Optional<IIngredientType<MobEffectInstance>> effectType = ingredients.getIngredientTypeChecked(MobEffectInstance.class);
        if (effectType.isPresent()) {
            List<MobEffectInstance> sunny = ingredients.getAllIngredients(effectType.get()).stream()
                    .filter(e -> BuzzierBeesSet.SUNNY.equals(ForgeRegistries.MOB_EFFECTS.getKey(e.getEffect())))
                    .toList();
            if (!sunny.isEmpty()) ingredients.removeIngredientsAtRuntime(effectType.get(), sunny);
        }
    }

    /** Just Enough Breeding's hardcoded page for {@code buzzier_bees:moobloom}, a mob this mod replaces. */
    private static void hideJustEnoughBreedingPage(IJeiRuntime runtime) {
        Item bbEgg = ForgeRegistries.ITEMS.getValue(BB_EGG);
        if (bbEgg == null || bbEgg == Items.AIR) return;
        IRecipeManager recipes = runtime.getRecipeManager();
        recipes.getRecipeType(JEBR_BREEDING).ifPresent(type -> hide(runtime, recipes, type, new ItemStack(bbEgg)));
    }

    private static <T> void hide(IJeiRuntime runtime, IRecipeManager recipes, RecipeType<T> type, ItemStack stack) {
        IFocusFactory foci = runtime.getJeiHelpers().getFocusFactory();
        Set<T> found = new LinkedHashSet<>();
        for (RecipeIngredientRole role : List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT)) {
            recipes.createRecipeLookup(type).limitFocus(List.of(foci.createFocus(role, VanillaTypes.ITEM_STACK, stack)))
                    .includeHidden().get().forEach(found::add);
        }
        if (!found.isEmpty()) recipes.hideRecipes(type, found);
    }

    private static Component names(List<ItemStack> stacks) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < stacks.size(); i++) {
            if (i > 0) out.append(i == stacks.size() - 1 ? " / " : ", ");
            out.append(stacks.get(i).getHoverName());
        }
        return out;
    }

    static String percent(double chance) {
        double p = chance * 100.0;
        return p == Math.rint(p) ? String.valueOf((int) p) : String.format("%.1f", p);
    }
}
