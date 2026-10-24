package com.sammy.minersdelight.content.item;

import com.sammy.minersdelight.registry.MDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** A cup of a solid (powder snow): placing it puts the block down and leaves an empty Copper Cup. */
public class SolidCupItem extends BlockItem implements DispensibleContainerItem {
    private final SoundEvent placeSound;

    public SolidCupItem(Block block, SoundEvent placeSound, Properties properties) {
        super(block, properties);
        this.placeSound = placeSound;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        Player player = context.getPlayer();
        if (result.consumesAction() && player != null && !player.isCreative()) {
            player.setItemInHand(context.getHand(), new ItemStack(MDItems.COPPER_CUP.get()));
        }
        return result;
    }

    /** Not the block's item: powder snow keeps the vanilla bucket as its pick-block item. */
    @Override
    public void registerBlocks(Map<Block, Item> blockToItem, Item item) {
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state) {
        return placeSound;
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult hit) {
        if (!level.isInWorldBounds(pos) || !level.isEmptyBlock(pos)) {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, getBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(player, pos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
