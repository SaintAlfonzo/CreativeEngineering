package com.SaintAlfonzo.creativedelight.hearth.salt;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage 0 of a salt lick: a Rock Salt Block placed after world generation. Setting The Salt's
 * {@code salt:raw_rock_salt_block} in a loaded level swaps it for this block ({@link SaltEvents#afterPlace}), so only placed
 * blocks are licks; generated Rock Salt Blocks (salt caves,
 * beach patches) stay The Salt's block, out of the POI type, never licked or worn. Looks like the Rock Salt Block, drops
 * it and picks it; no item of its own.
 */
public class PlacedRockSaltBlock extends LickableSaltBlock {
    public PlacedRockSaltBlock(Properties properties) {
        super(properties, 16);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        Block rock = SaltLick.saltBlock();
        return rock == null || rock == Blocks.AIR ? ItemStack.EMPTY : new ItemStack(rock);
    }
}
