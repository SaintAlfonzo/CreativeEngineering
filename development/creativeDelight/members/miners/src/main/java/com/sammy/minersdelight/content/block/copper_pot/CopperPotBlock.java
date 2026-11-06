package com.sammy.minersdelight.content.block.copper_pot;

import com.sammy.minersdelight.registry.MDBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.state.CookingPotSupport;
import vectorwing.farmersdelight.common.registry.ModSounds;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.MathUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

/**
 * The Copper Pot block: like Farmer's Delight's pot (tray on a tray heat source, handle when hung or shift-clicked with
 * an empty hand; right-click with the meal's container serves one portion, otherwise opens the menu), only smaller.
 */
public class CopperPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<CookingPotSupport> SUPPORT = EnumProperty.create("support", CookingPotSupport.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 8, 13);
    private static final VoxelShape SHAPE_WITH_TRAY = Shapes.or(SHAPE, Block.box(0, -1, 0, 16, 0, 16));

    public CopperPotBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SUPPORT, CookingPotSupport.NONE).setValue(WATERLOGGED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            CookingPotSupport support = state.getValue(SUPPORT) == CookingPotSupport.HANDLE ? trayState(level, pos) : CookingPotSupport.HANDLE;
            level.setBlockAndUpdate(pos, state.setValue(SUPPORT, support));
            level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
        } else if (!level.isClientSide && level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot) {
            ItemStack serving = pot.useHeldItemOnMeal(held);
            if (!serving.isEmpty()) {
                if (!player.getInventory().add(serving)) {
                    player.drop(serving, false);
                }
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                NetworkHooks.openScreen((ServerPlayer) player, pot, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SUPPORT) == CookingPotSupport.TRAY ? SHAPE_WITH_TRAY : SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        return state.setValue(SUPPORT, context.getClickedFace() == Direction.DOWN ? CookingPotSupport.HANDLE : trayState(level, pos));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (facing.getAxis() == Direction.Axis.Y && state.getValue(SUPPORT) != CookingPotSupport.HANDLE) {
            return state.setValue(SUPPORT, trayState(level, pos));
        }
        return state;
    }

    private static CookingPotSupport trayState(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModTags.TRAY_HEAT_SOURCES) ? CookingPotSupport.TRAY : CookingPotSupport.NONE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot) {
            CompoundTag meal = pot.writeMeal(new CompoundTag());
            if (!meal.isEmpty()) {
                stack.addTagElement("BlockEntityTag", meal);
            }
            if (pot.getCustomName() != null) {
                stack.setHoverName(pot.getCustomName());
            }
        }
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot) {
                Containers.dropContents(level, pos, pot.getDroppableInventory());
                pot.getUsedRecipesAndPopExperience(level, Vec3.atCenterOf(pos));
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ItemStack meal = CopperPotBlockEntity.getMealFromItem(stack);
        if (meal.isEmpty()) {
            tooltip.add(TextUtils.getTranslation("tooltip.cooking_pot.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add((meal.getCount() == 1
                ? TextUtils.getTranslation("tooltip.cooking_pot.single_serving")
                : TextUtils.getTranslation("tooltip.cooking_pot.many_servings", meal.getCount())).withStyle(ChatFormatting.GRAY));
        tooltip.add(meal.getHoverName().copy().withStyle(meal.getRarity().color));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SUPPORT, WATERLOGGED);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot) {
            pot.setCustomName(stack.getHoverName());
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot && pot.isHeated() && random.nextInt(10) == 0) {
            SoundEvent boil = pot.getMeal().isEmpty() ? ModSounds.BLOCK_COOKING_POT_BOIL.get() : ModSounds.BLOCK_COOKING_POT_BOIL_SOUP.get();
            level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, boil, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.2F + 0.9F, false);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CopperPotBlockEntity pot ? MathUtils.calcRedstoneFromItemHandler(pot.getInventory()) : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperPotBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, MDBlockEntities.COPPER_POT.get(), CopperPotBlockEntity::animationTick)
                : createTickerHelper(type, MDBlockEntities.COPPER_POT.get(), CopperPotBlockEntity::cookingTick);
    }
}
