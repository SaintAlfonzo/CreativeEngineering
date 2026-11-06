package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * The grill item replaces a plain campfire it is used on (1.3.0 GrillBlockItem), taking the campfire in. 1.3.0 merged a
 * lit campfire only; the port merges any plain, dry campfire, since the pack places campfires unlit (O11).
 */
public class GrillBlockItem extends BlockItem {

    public GrillBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext ctx) {
        // getClickedPos() is the block beside the one clicked unless that one is replaceable (a campfire is not)
        BlockPos clicked = ctx.replacingClickedOnBlock() ? ctx.getClickedPos() : ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite());
        if (GrillBlock.canMerge(ctx.getLevel().getBlockState(clicked))) {
            return new ReplaceClicked(ctx, new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), clicked, ctx.isInside()));
        }
        return ctx;
    }

    private static final class ReplaceClicked extends BlockPlaceContext {
        ReplaceClicked(BlockPlaceContext ctx, BlockHitResult hit) {
            super(ctx.getLevel(), ctx.getPlayer(), ctx.getHand(), ctx.getItemInHand(), hit);
            replaceClicked = true;
        }
    }
}
