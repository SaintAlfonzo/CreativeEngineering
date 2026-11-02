package com.SaintAlfonzo.creativedelight.oceanic.logic;

import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicEffects;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

/** Compost chances, the Mining Fatigue brewing steps and the wandering trader offer (ocean_delight.md 5.6). */
public final class OceanicSetup {
    private OceanicSetup() {
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(OceanicItems.SPONGE_CAKE.get(), 1.0F);
            ComposterBlock.COMPOSTABLES.put(OceanicItems.SPONGE_CAKE_SLICE.get(), 0.85F);
            ComposterBlock.COMPOSTABLES.put(OceanicItems.WILD_SEA_GRAPE.get(), 0.65F);
            // Awkward + Elder Guardian Eye = Mining Fatigue; + redstone = longer; + glowstone = level II. For the drinkable,
            // splash and lingering bottles alike (plain Forge recipes, so JEI lists them).
            for (Item bottle : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)) {
                brew(bottle, Potions.AWKWARD, OceanicItems.ELDER_GUARDIAN_EYE.get(), OceanicEffects.MINING_FATIGUE.get());
                brew(bottle, OceanicEffects.MINING_FATIGUE.get(), Items.REDSTONE, OceanicEffects.MINING_FATIGUE_EXTENDED.get());
                brew(bottle, OceanicEffects.MINING_FATIGUE.get(), Items.GLOWSTONE_DUST, OceanicEffects.MINING_FATIGUE_TWO.get());
            }
        });
    }

    private static void brew(Item bottle, Potion from, Item ingredient, Potion to) {
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(StrictNBTIngredient.of(PotionUtils.setPotion(new ItemStack(bottle), from)),
                Ingredient.of(ingredient), PotionUtils.setPotion(new ItemStack(bottle), to)));
    }

    /** 3 emeralds for 2 Salmon Eggs, one trade per offer. */
    public static void wanderingTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 3), new ItemStack(OceanicItems.SALMON_EGGS.get(), 2), 1, 2, 0.03F));
    }
}
