package com.SaintAlfonzo.creativedelight.crops;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Vine crops are not replaceable by placement (plan_post_scratch_1115 C2, SQ-HOP-04). Vanilla vines are replaceable
 * plants ({@code VineBlock.canBeReplaced} is true while the vine has a free face), so any block item, a Supplementaries
 * stick or a torch clicked onto a hops vine or a jungle grape vine took its place. For every block in
 * {@code #creativedelight:crops/vine_crops} {@code BlockStateBase.canBeReplaced(BlockPlaceContext)} is now false unless the
 * item places that same block (adding a face to a vine stays as vanilla). Hook: {@code mixin.minecraft.VineCropNoReplaceMixin}.
 */
public final class VineCropReplace {
    public static final TagKey<Block> VINE_CROPS = BlockTags.create(new ResourceLocation("creativedelight", "crops/vine_crops"));

    private VineCropReplace() {
    }

    /** True when placement must not replace this state. */
    public static boolean refuses(BlockBehaviour.BlockStateBase state, BlockPlaceContext context) {
        if (!state.is(VINE_CROPS)) {
            return false;
        }
        Item item = context.getItemInHand().getItem();
        return !(item instanceof BlockItem blockItem && blockItem.getBlock() == state.getBlock());
    }
}
