package net.mehdinoui.veggiesdelight.common.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.PieBlock;

public class PizzaBlock extends PieBlock {
    protected static final VoxelShape PIZZA_SHAPE = Block.box(0.0, 0.0, 0.0, 15.95, 2.0, 15.95);

    public PizzaBlock(Properties properties, Supplier<Item> pieSlice) {
        super(properties, pieSlice);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PIZZA_SHAPE;
    }

    // Pack (TODO dishes/meals #5, plan_food_batch_1119.md C1, R18): a pizza serves 8 slices. Each of its 4 bites gives
    // SLICES_PER_BITE slice items, cut with a knife or taken by hand: the block is never eaten from directly.
    public static final int SLICES_PER_BITE = 2;

    @Override
    protected InteractionResult consumeBite(Level level, BlockPos pos, BlockState state, Player player) {
        return this.cutSlice(level, pos, state, player);
    }

    @Override
    protected InteractionResult cutSlice(Level level, BlockPos pos, BlockState state, Player player) {
        int bites = state.getValue(BITES);
        if (bites < this.getMaxBites() - 1) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
        }
        Direction direction = player.getDirection().getOpposite();
        ItemStack slices = this.getPieSliceItem();
        slices.setCount(SLICES_PER_BITE);
        ItemUtils.spawnItemEntity(level, slices, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                direction.getStepX() * 0.15, 0.05, direction.getStepZ() * 0.15);
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResult.SUCCESS;
    }
}
