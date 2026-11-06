package net.mehdinoui.fungidelight.common.event;

import java.util.Arrays;
import net.mehdinoui.fungidelight.common.entity.custom.FDMCow;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.mehdinoui.fungidelight.common.potion.ModBrewingRecipes;
import net.mehdinoui.fungidelight.common.registry.ModEntities;
import net.mehdinoui.fungidelight.common.registry.ModItems;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

// Port changes: no compost hook (the pack's compost engine owns the table); no config switches; the Burrowing potions
// are vanilla PotionBrewing mixes (ModBrewingRecipes) instead of Forge BrewingRecipes.
@EventBusSubscriber(modid = "fungidelight", bus = Bus.MOD)
public class ModCommonEvent {
   @SubscribeEvent
   public static void onCommonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         ModBrewingRecipes.register();
         // Combines with the value the field holds when this runs, so the Veggies and Vintage ports keep their additions.
         Ingredient newPigFood = Ingredient.of(
            Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, ModItems.INKY_CAP_MUSHROOM.get(), ModItems.MOREL_MUSHROOM.get(), ModItems.TRUFFLE.get()
         );
         Pig.FOOD_ITEMS = new CompoundIngredient(Arrays.asList(Pig.FOOD_ITEMS, newPigFood)) {};
      });
   }

   @SubscribeEvent
   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(ModEntities.INKY_CAP_COW.get(), Cow.createAttributes().build());
      event.put(ModEntities.MOREL_COW.get(), Cow.createAttributes().build());
   }

   // The vanilla mooshroom rule (mycelium below, light), typed for the Fungi cows.
   @SuppressWarnings("unchecked")
   private static boolean mushroomCowRules(EntityType<FDMCow> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
      return MushroomCow.checkMushroomSpawnRules((EntityType<MushroomCow>)(EntityType<?>)type, level, spawnType, pos, random);
   }

   @SubscribeEvent
   public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
      event.register(ModEntities.INKY_CAP_COW.get(), Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES,
         ModCommonEvent::mushroomCowRules,
         Operation.REPLACE);
      event.register(ModEntities.MOREL_COW.get(), Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES,
         ModCommonEvent::mushroomCowRules,
         Operation.REPLACE);
   }
}
