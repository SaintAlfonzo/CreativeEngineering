package com.SaintAlfonzo.creativedelight.largemeals.registry;

import com.SaintAlfonzo.creativedelight.largemeals.LargeMealsMod;
import com.SaintAlfonzo.creativedelight.largemeals.content.MealFeastBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The three feast blocks (large_meals.md 3.2), cake properties as upstream. */
public final class MealBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LargeMealsMod.MODID);

    public static final RegistryObject<Block> OMURICE_BLOCK = BLOCKS.register("omurice_block",
            () -> new MealFeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), MealItems.OMURICE::get, 4.0D, 4.0D, 12.0D, 12.0D, 10.0D));
    public static final RegistryObject<Block> MUSHROOM_POT_PIE_BLOCK = BLOCKS.register("mushroom_pot_pie_block",
            () -> new MealFeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), MealItems.MUSHROOM_POT_PIE::get, 2.0D, 2.0D, 14.0D, 14.0D, 6.0D));
    public static final RegistryObject<Block> ROASTED_MUTTON_RACK_BLOCK = BLOCKS.register("roasted_mutton_rack_block",
            () -> new MealFeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), MealItems.ROASTED_MUTTON_RACK::get, 2.0D, 2.0D, 14.0D, 14.0D, 10.0D));

    private MealBlocks() {
    }
}
