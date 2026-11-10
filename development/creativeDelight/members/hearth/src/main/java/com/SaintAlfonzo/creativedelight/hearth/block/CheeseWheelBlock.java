package com.SaintAlfonzo.creativedelight.hearth.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A ripe cheese wheel (1.4.1 CheeseWheelBlock without the cask vintages): a knife takes one of 4 slices. */
public class CheeseWheelBlock extends Block {
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);
    private static final TagKey<Item> KNIVES = ItemTags.create(new ResourceLocation("forge", "tools/knives"));
    private static final VoxelShape[] SHAPES = {
            Block.box(8, 0, 2, 14, 6, 8),
            Block.box(2, 0, 2, 14, 6, 8),
            Shapes.or(Block.box(2, 0, 2, 14, 6, 8), Block.box(2, 0, 8, 8, 6, 14)),
            Block.box(2, 0, 2, 14, 6, 14)
    };
    private final Supplier<Item> slice;

    public CheeseWheelBlock(Supplier<Item> slice, Properties properties) {
        super(properties);
        this.slice = slice;
        registerDefaultState(stateDefinition.any().setValue(SERVINGS, 3));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SERVINGS)];
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(KNIVES)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            popResource(level, pos, new ItemStack(slice.get()));
            int servings = state.getValue(SERVINGS);
            if (servings > 0) level.setBlock(pos, state.setValue(SERVINGS, servings - 1), Block.UPDATE_ALL);
            else level.destroyBlock(pos, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(SERVINGS);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
}
