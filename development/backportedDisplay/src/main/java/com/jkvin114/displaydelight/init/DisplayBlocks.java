package com.jkvin114.displaydelight.init;

import com.jkvin114.displaydelight.block.DrinkBlock;
import com.jkvin114.displaydelight.block.EmptyPlateBlock;
import com.jkvin114.displaydelight.block.EmptySmallPlateBlock;
import com.jkvin114.displaydelight.block.FoodBlock;
import com.jkvin114.displaydelight.block.HotDrinkBlock;
import com.jkvin114.displaydelight.block.SimplePlatedFoodBlock;
import com.jkvin114.displaydelight.block.SmallPlatedFoodBlock;
import com.jkvin114.displaydelight.block.StackablePlateFoodBlock;
import com.jkvin114.displaydelight.block.WideFoodBlock;
import com.jkvin114.displaydelight.block.fiery.FieryFoodBlock;
import com.jkvin114.displaydelight.block.fiery.FieryPlatedFoodBlock;
import com.jkvin114.displaydelight.block.fiery.FierySmallPlatedFoodBlock;
import com.jkvin114.displaydelight.block.fiery.FieryWideFoodBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The plates and every food display block. 1.7.0 splits the food blocks over DisplayBlocks, PlatedBlocks and
 * SmallPlatedBlocks; the port keeps one register, filled from {@link DisplayFoods}.
 */
public class DisplayBlocks {
   public static List<RegistryObject<Block>> blocks = new ArrayList<>();
   public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, "displaydelight");
   public static final RegistryObject<Block> PLATE = REGISTRY.register(
      "food_plate", () -> new EmptyPlateBlock(Properties.of(Material.WOOD).instabreak().sound(SoundType.WOOD))
   );
   public static final RegistryObject<Block> SMALL_PLATE = REGISTRY.register(
      "small_food_plate", () -> new EmptySmallPlateBlock(Properties.of(Material.WOOD).instabreak().sound(SoundType.WOOD))
   );

   public static List<Block> getAll() {
      return blocks.stream().<Block>map(RegistryObject::get).toList();
   }

   // 1.19.2: Material.WOOD in place of 1.20's map colour; the piston reaction is on the block classes
   private static Properties baseProps() {
      return Properties.of(Material.WOOD).noOcclusion().instabreak();
   }

   private static RegistryObject<Block> create(String name, SoundType sound, Function<Properties, Block> factory) {
      RegistryObject<Block> block = REGISTRY.register(name, () -> factory.apply(baseProps().sound(sound)));
      blocks.add(block);
      return block;
   }

   static RegistryObject<Block> food(String name) {
      return create(name, SoundType.WOOD, FoodBlock::new);
   }

   static RegistryObject<Block> wide(String name) {
      return create(name, SoundType.WOOD, WideFoodBlock::new);
   }

   static RegistryObject<Block> drink(String name) {
      return create(name, SoundType.GLASS, DrinkBlock::new);
   }

   static RegistryObject<Block> hotDrink(String name) {
      return create(name, SoundType.GLASS, HotDrinkBlock::new);
   }

   static RegistryObject<Block> fieryFood(String name) {
      return create(name, SoundType.WOOD, FieryFoodBlock::new);
   }

   static RegistryObject<Block> fieryWide(String name) {
      return create(name, SoundType.WOOD, FieryWideFoodBlock::new);
   }

   static RegistryObject<Block> plated(String name) {
      return create(name, SoundType.WOOD, SimplePlatedFoodBlock::new);
   }

   static RegistryObject<Block> stackable(String name, int maxStacks) {
      return create(name, SoundType.WOOD, props -> new StackablePlateFoodBlock(props, maxStacks));
   }

   static RegistryObject<Block> fieryStackable(String name, int maxStacks) {
      return create(name, SoundType.WOOD, props -> new FieryPlatedFoodBlock(props, maxStacks));
   }

   static RegistryObject<Block> small(String name) {
      return create(name, SoundType.WOOD, SmallPlatedFoodBlock::new);
   }

   static RegistryObject<Block> fierySmall(String name) {
      return create(name, SoundType.WOOD, FierySmallPlatedFoodBlock::new);
   }
}
