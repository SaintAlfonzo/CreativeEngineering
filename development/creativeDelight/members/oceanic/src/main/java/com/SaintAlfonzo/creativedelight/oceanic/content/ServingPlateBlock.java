package com.SaintAlfonzo.creativedelight.oceanic.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * The two placed dishes (ocean_delight.md 5.6): Paella Plate and Stuffed Pufferfish, four servings each. With a bowl in
 * either hand a use hands over one serving and takes the bowl (not in creative); without one the player is told so.
 * The pufferfish is gone with its last serving. The paella pan stays, empty; using the empty pan picks it up through its
 * loot table (pan and skillet, upstream data). Both stand where a cake could.
 */
public class ServingPlateBlock extends WaterloggedPlantBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 4);

    private final Supplier<Item> serving;
    private final boolean keepsEmptyPan;
    private final IntFunction<VoxelShape> shape;

    public ServingPlateBlock(Properties properties, Supplier<Item> serving, boolean keepsEmptyPan, IntFunction<VoxelShape> shape) {
        super(properties);
        this.serving = serving;
        this.keepsEmptyPan = keepsEmptyPan;
        this.shape = shape;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(SERVINGS, 4));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, SERVINGS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(SERVINGS, 4);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape.apply(state.getValue(SERVINGS));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getMaterial().isSolid();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int servings = state.getValue(SERVINGS);
        if (servings == 0 && keepsEmptyPan) {
            if (!level.isClientSide) {
                dropResources(state, level, pos);
                level.destroyBlock(pos, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack bowl = player.getMainHandItem().is(Items.BOWL) ? player.getMainHandItem()
                : player.getOffhandItem().is(Items.BOWL) ? player.getOffhandItem() : ItemStack.EMPTY;
        if (bowl.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.oceanic_delight.needs_bowl"), true);
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) bowl.shrink(1);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(serving.get()));
            if (servings > 1 || keepsEmptyPan) {
                level.setBlock(pos, state.setValue(SERVINGS, servings - 1), 3);
            } else {
                level.removeBlock(pos, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
