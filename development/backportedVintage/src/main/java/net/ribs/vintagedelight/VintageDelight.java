package net.ribs.vintagedelight;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.block.entity.ModBlockEntities;
import net.ribs.vintagedelight.config.VintageDelightCommonConfig;
import net.ribs.vintagedelight.item.ModItems;
import net.ribs.vintagedelight.recipe.ModRecipes;
import net.ribs.vintagedelight.screen.ModMenuTypes;
import net.ribs.vintagedelight.worldgen.tree.ModTreeDecoratorTypes;
import net.ribs.vintagedelight.worldgen.tree.ModTrunkPlacerTypes;

// Vintage Delight 0.1.6 ported to Forge 1.19.2 (feature_writeup/vintage_delight.md). Not ported: the gearo berry line,
// the Dehydrated effect, its potions and brewing hook, the pickled pitcher pod. KOTS 0.3.1's lush grass switch is the
// one config value.
@Mod(VintageDelight.MODID)
public class VintageDelight {
    public static final String MODID = "vintagedelight";

    public VintageDelight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VintageDelightCommonConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModTrunkPlacerTypes.register(modEventBus);
        ModTreeDecoratorTypes.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ComposterBlock.COMPOSTABLES.put(ModItems.PEANUT.get(), 0.4F);
            ComposterBlock.COMPOSTABLES.put(ModItems.GHOST_PEPPER.get(), 0.4F);
            ComposterBlock.COMPOSTABLES.put(ModItems.GHOST_PEPPER_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.CUCUMBER.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.CUCUMBER_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.OAT_SEEDS.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PICKLE.get(), 0.4F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PICKLED_PEPPER.get(), 0.4F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PICKLED_BEETROOT.get(), 0.6F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PICKLED_EGG.get(), 0.6F);
            ComposterBlock.COMPOSTABLES.put(ModItems.PICKLED_ONION.get(), 0.6F);
            ComposterBlock.COMPOSTABLES.put(ModItems.CENTURY_EGG.get(), 0.9F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.MAGIC_PEANUT.get().asItem(), 0.85F);
            ComposterBlock.COMPOSTABLES.put(ModItems.OAT.get(), 0.3F);
            registerItemSetAdditions();
        });
    }

    /**
     * Mob food (static fields opened by the access transformer). Every edit combines with the value the field holds
     * when this runs, so other mods that extend the same sets (the Veggies port) keep their additions whatever the
     * load order.
     */
    public static void registerItemSetAdditions() {
        Ingredient newChickenFood = Ingredient.of(ModItems.GHOST_PEPPER_SEEDS.get(), ModItems.CUCUMBER_SEEDS.get(), ModItems.OAT_SEEDS.get());
        Chicken.FOOD_ITEMS = new CompoundIngredient(Arrays.asList(Chicken.FOOD_ITEMS, newChickenFood)) {};
        Ingredient newPigFood = Ingredient.of(ModItems.CUCUMBER.get(), ModItems.GHOST_PEPPER.get(), ModItems.RAW_OAT.get(), ModItems.PEANUT.get());
        Pig.FOOD_ITEMS = new CompoundIngredient(Arrays.asList(Pig.FOOD_ITEMS, newPigFood)) {};
        Set<Item> parrotFood = new HashSet<>(Parrot.TAME_FOOD);
        Collections.addAll(parrotFood, ModItems.GHOST_PEPPER_SEEDS.get(), ModItems.CUCUMBER_SEEDS.get(), ModItems.OAT_SEEDS.get());
        Parrot.TAME_FOOD = parrotFood;
        Set<Item> wanted = new HashSet<>(Villager.WANTED_ITEMS);
        Collections.addAll(wanted, ModItems.GHOST_PEPPER.get(), ModItems.CUCUMBER.get(), ModItems.PEANUT.get(), ModItems.OAT.get());
        Villager.WANTED_ITEMS = ImmutableSet.copyOf(wanted);
    }
}
