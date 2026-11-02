package com.SaintAlfonzo.creativebrewing.fluidbrewing;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Alex's Mobs registers every modifier step (e.g. {@code lava_vision + redstone -> long_lava_vision})
 * as a Forge {@link BrewingRecipe}. The pack ALSO re-adds those steps as vanilla
 * {@link PotionBrewing.Mix} entries (via {@code potions.js} {@code AM_POTION_MIXES}) so they work
 * container-agnostically on the brewing stand + drive splash/lingering parity.
 *
 * <p>Both Create's {@code PotionMixingRecipes.createRecipes()} and IE's
 * {@code PotionHelper.applyToAllPotionRecipes()} walk the {@code Mix} list AND
 * {@code BrewingRecipeRegistry.getRecipes()} — so any AM step that exists in both lists produces
 * a <b>duplicate</b> mixer recipe. This trims the registry list down for those two callers:
 * drop any {@link BrewingRecipe} whose {@code from -> to} potion pair a {@code Mix} already covers.
 */
public final class PotionMixDedup {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PotionMixDedup() {}

    /** {@code BrewingRecipeRegistry.getRecipes()} minus the entries a {@code PotionBrewing.Mix} covers. */
    public static List<IBrewingRecipe> withoutMixDuplicates(List<IBrewingRecipe> registry) {
        Set<String> covered = coveredPairs();
        if (covered.isEmpty()) {
            return registry;
        }

        List<IBrewingRecipe> out = new ArrayList<>(registry.size());
        int dropped = 0;
        for (IBrewingRecipe r : registry) {
            if (r instanceof BrewingRecipe br && isCovered(br, covered)) {
                dropped++;
                continue;
            }
            out.add(r);
        }
        if (dropped > 0) {
            LOGGER.info("[CE] potion mixer dedup: hid {} BrewingRecipe(s) already covered by a PotionBrewing.Mix",
                    dropped);
        }
        return out;
    }

    private static boolean isCovered(BrewingRecipe br, Set<String> covered) {
        ItemStack[] inputs = br.getInput().getItems();
        if (inputs.length == 0) {
            return false;
        }
        Potion from = PotionUtils.getPotion(inputs[0]);
        Potion to = PotionUtils.getPotion(br.getOutput());
        if (from == Potions.EMPTY || to == Potions.EMPTY) {
            return false; // bottle-based base brew (lava/poison bottle) — handled elsewhere
        }
        ResourceLocation fromId = ForgeRegistries.POTIONS.getKey(from);
        ResourceLocation toId = ForgeRegistries.POTIONS.getKey(to);
        return fromId != null && toId != null && covered.contains(fromId + ">" + toId);
    }

    private static Set<String> coveredPairs() {
        Set<String> out = new HashSet<>();
        for (PotionBrewing.Mix<Potion> m : potionMixes()) {
            ResourceLocation from = ForgeRegistries.POTIONS.getKey(m.from.value());
            ResourceLocation to = ForgeRegistries.POTIONS.getKey(m.to.value());
            if (from != null && to != null) {
                out.add(from + ">" + to);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<PotionBrewing.Mix<Potion>> potionMixes() {
        for (String name : new String[] { "f_43494_", "POTION_MIXES" }) {
            try {
                Field f = PotionBrewing.class.getDeclaredField(name);
                f.setAccessible(true);
                return (List<PotionBrewing.Mix<Potion>>) f.get(null);
            } catch (ReflectiveOperationException ignored) {
                // try the next name
            }
        }
        LOGGER.error("[CE] PotionBrewing.POTION_MIXES not found - potion mixer dedup disabled");
        return List.of();
    }
}
