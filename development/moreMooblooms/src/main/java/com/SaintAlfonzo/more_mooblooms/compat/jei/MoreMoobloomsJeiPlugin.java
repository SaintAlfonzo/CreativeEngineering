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
 *   <li>an info page per variant egg: its flower pool with the odds, what converts a cow, shearing, regrowth,
 *       breeding food and which parents it is bred from</li>
 *   <li>"Moobloom Breeding" ({@link MoobloomBreedingCategory}): every combination rule, parent eggs -> calf egg</li>
 *   <li>with Buzzier Bees: hides the buttercup and its moobloom egg, and the Sunny effect entry that Just Enough
 *       Effect Descriptions adds (found by ingredient class, so there is no compile dependency on JEED)</li>
 * </ul>
 * The variant table is already synced when JEI starts (our packet is sent before the recipe and tag packets).
 */
@JeiPlugin
public class MoreMoobloomsJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(MoreMooblooms.ID, "jei");

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
        registration.addRecipeCategories(new MoobloomBreedingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get()), MoobloomBreedingCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<MoobloomBreedingCategory.Recipe> breeding = breedingRecipes();
        registration.addRecipes(MoobloomBreedingCategory.TYPE, breeding);

        String feedPercent = percent(MMConfig.num(MMConfig.FEED_CHANCE));
        boolean bareToCow = MMConfig.on(MMConfig.SHEAR_BARE_TO_COW);
        boolean bareDrops = MMConfig.on(MMConfig.BARE_SHEAR_DROPS);
        for (MoobloomVariant v : MoobloomVariants.values(true)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("jei.more_mooblooms.info.pool", poolOdds(v)));
            Component food = names(food(v));
            lines.add(Component.translatable("jei.more_mooblooms.info.feed", food, feedPercent));
            lines.add(Component.translatable("jei.more_mooblooms.info.shear"));
            for (MoobloomVariant.Flower f : v.pool()) {
                if (f.chance() < 1.0F) {
                    lines.add(Component.translatable("jei.more_mooblooms.info.drop_chance",
                            new ItemStack(f.drop()).getHoverName(), percent(f.chance())));
                }
            }
            if (bareToCow) lines.add(Component.translatable(bareDrops ? "jei.more_mooblooms.info.bare_drops" : "jei.more_mooblooms.info.bare"));
            lines.add(Component.translatable("jei.more_mooblooms.info.breed", food));
            ItemStack egg = MoobloomSpawnEggItem.forVariant(v.id());
            for (MoobloomBreedingCategory.Recipe r : breeding) {
                if (ItemStack.isSameItemSameTags(r.child(), egg)) {
                    lines.add(Component.translatable("jei.more_mooblooms.info.bred_from",
                            variantName(r.parentA()), variantName(r.parentB()), percent(r.chance())));
                }
            }
            if (v.breedWeight() == 0) lines.add(Component.translatable("jei.more_mooblooms.info.never_random"));
            registration.addIngredientInfo(egg, VanillaTypes.ITEM_STACK, lines.toArray(Component[]::new));
        }
    }

    /** One recipe per combination result whose variants all exist; chance = rule chance x result share. */
    private static List<MoobloomBreedingCategory.Recipe> breedingRecipes() {
        List<MoobloomBreedingCategory.Recipe> out = new ArrayList<>();
        for (MoobloomBreeding.Rule rule : MoobloomBreeding.rules(true)) {
            ResourceLocation a = rule.parentA(), b = rule.parentB();
            if (!MoobloomVariants.has(true, a) || !MoobloomVariants.has(true, b)) continue;
            // a pair an earlier rule already claims never reaches this one
            if (MoobloomBreeding.ruleFor(true, a, b) != rule) continue;
            List<MoobloomBreeding.Result> results = rule.knownResults(true);
            int total = results.stream().mapToInt(MoobloomBreeding.Result::weight).sum();
            for (MoobloomBreeding.Result r : results) {
                out.add(new MoobloomBreedingCategory.Recipe(MoobloomSpawnEggItem.forVariant(a),
                        MoobloomSpawnEggItem.forVariant(b), MoobloomSpawnEggItem.forVariant(r.variant()),
                        rule.chance() * r.weight() / total));
            }
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

    private static Component variantName(ItemStack egg) {
        MoobloomVariant v = MoobloomSpawnEggItem.clientVariant(egg);
        return v == null ? egg.getHoverName() : Component.translatable(v.translationKey());
    }

    /** Pool items, then feed items and the items in feed tags (tags are synced before JEI starts). */
    private static List<ItemStack> food(MoobloomVariant v) {
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
