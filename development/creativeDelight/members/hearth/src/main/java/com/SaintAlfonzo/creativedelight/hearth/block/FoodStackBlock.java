package com.SaintAlfonzo.creativedelight.hearth.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

/** A stack of up to 4 waffles (1.4.1 FoodStackBlock); eaten from the block, syrup adds 2 hunger. */
public class FoodStackBlock extends Block {
    public static final int MAX_STACK = 4;
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, MAX_STACK);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty SYRUP = BooleanProperty.create("syrup");
    /** The pack's one syrup (hearth_and_harvest.md D4), resolved by id: Rustic Delight is another member. */
    private static final ResourceLocation SYRUP_ITEM = new ResourceLocation("rusticdelight", "syrup");
    private static final int SYRUP_NUTRITION_BONUS = 2;
    private static final VoxelShape[] SHAPES = {
            Block.box(2, 0, 2, 14, 2, 14), Block.box(2, 0, 2, 14, 4, 14), Block.box(2, 0, 2, 14, 6, 14), Block.box(2, 0, 2, 14, 8, 14)
    };

    public FoodStackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COUNT, 1).setValue(FACING, Direction.NORTH).setValue(SYRUP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COUNT, FACING, SYRUP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(COUNT) - 1];
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        return existing.is(this)
                ? existing.setValue(COUNT, Math.min(MAX_STACK, existing.getValue(COUNT) + 1))
                : defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return (!context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(COUNT) < MAX_STACK)
                || super.canBeReplaced(state, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getMaterial().isSolid() && !(below.getBlock() instanceof FoodStackBlock);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(asItem())) {
            // Placing goes through the block item (canBeReplaced); a full stack just swallows the click.
            return state.getValue(COUNT) < MAX_STACK ? InteractionResult.PASS : InteractionResult.CONSUME;
        }
        if (held.is(ForgeRegistries.ITEMS.getValue(SYRUP_ITEM))) {
            if (state.getValue(SYRUP)) return InteractionResult.CONSUME;
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(SYRUP, true), Block.UPDATE_ALL);
                ItemStack remainder = held.hasCraftingRemainingItem() ? held.getCraftingRemainingItem() : ItemStack.EMPTY;
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, remainder));
                level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        ItemStack single = new ItemStack(asItem());
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                player.getInventory().placeItemBackInInventory(single);
                removeOne(state, level, pos);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.2F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        FoodProperties food = single.getFoodProperties(player);
        if (food == null || !player.canEat(food.canAlwaysEat())) return InteractionResult.PASS;
        if (!level.isClientSide) {
            boolean syrup = state.getValue(SYRUP);
            player.eat(level, single);
            if (syrup) player.getFoodData().eat(SYRUP_NUTRITION_BONUS, food.getSaturationModifier());
            removeOne(state, level, pos);
            level.gameEvent(player, GameEvent.EAT, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void removeOne(BlockState state, Level level, BlockPos pos) {
        int count = state.getValue(COUNT);
        if (count > 1) level.setBlock(pos, state.setValue(COUNT, count - 1), Block.UPDATE_ALL);
        else level.removeBlock(pos, false);
    }
}
