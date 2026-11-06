package com.SaintAlfonzo.creativedelight.golden.registry;

import com.SaintAlfonzo.creativedelight.golden.GoldenFoodMod;
import com.SaintAlfonzo.creativedelight.golden.content.GoldenCakeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The two Golden Food blocks, upstream ids. Both are cakes (the vanilla {@code bites} property 0 to 6, as upstream), with
 * upstream's block settings: wool material, strength 0.5, wool sound; no loot table (a cake drops nothing).
 */
public final class GoldenBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GoldenFoodMod.MODID);

    public static final RegistryObject<Block> GOLDEN_CAKE = BLOCKS.register("golden_cake",
            () -> new GoldenCakeBlock(cake(), GoldenCakeBlock.Slice.GOLDEN));
    public static final RegistryObject<Block> ENCHANTED_GOLDEN_CAKE = BLOCKS.register("enchanted_golden_cake",
            () -> new GoldenCakeBlock(cake(), GoldenCakeBlock.Slice.ENCHANTED));

    private GoldenBlocks() {
    }

    private static BlockBehaviour.Properties cake() {
        return BlockBehaviour.Properties.of(Material.WOOL).strength(0.5F).sound(SoundType.WOOL);
    }
}
