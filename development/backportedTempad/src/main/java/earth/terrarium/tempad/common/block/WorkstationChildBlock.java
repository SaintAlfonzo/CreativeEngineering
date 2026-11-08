// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/WorkstationChildBlock.kt. Translated to Java for
// Forge 1.19.2:
// - useWithoutItem (1.21) -> use() for the main hand only (the block has no useItemOn, so 1.21 reaches useWithoutItem
//   through the default PASS_TO_DEFAULT_BLOCK_INTERACTION, main hand only).
// - Properties.of() -> Properties.of(Material.STONE, MaterialColor.NONE), see WorkstationBlock.
// - getCloneItemStack(state, target, LevelReader, pos, player) -> Forge's IForgeBlock overload with a BlockGetter.
// - playerWillDestroy returns void.
package earth.terrarium.tempad.common.block;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.context.WorkstationContext;
import earth.terrarium.tempad.common.registries.ModApps;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class WorkstationChildBlock extends Block {
    public static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(1.0, 0.0, 0.0, 15.0, 2.0, 16.0),
            box(1.0, 2.0, 0.0, 15.0, 11.0, 10.0)
    );

    public static final VoxelShape SOUTH_SHAPE = Shapes.or(
            box(1.0, 0.0, 0.0, 15.0, 2.0, 16.0),
            box(1.0, 2.0, 6.0, 15.0, 11.0, 16.0)
    );

    public static final VoxelShape WEST_SHAPE = Shapes.or(
            box(0.0, 0.0, 1.0, 16.0, 2.0, 15.0),
            box(0.0, 2.0, 1.0, 10.0, 11.0, 15.0)
    );

    public static final VoxelShape EAST_SHAPE = Shapes.or(
            box(0.0, 0.0, 1.0, 16.0, 2.0, 15.0),
            box(6.0, 2.0, 1.0, 16.0, 11.0, 15.0)
    );

    public WorkstationChildBlock() {
        super(Properties.of(Material.STONE, MaterialColor.NONE).strength(3.0f, 1200f));
        this.registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.TRIGGERED, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        return useWithoutItem(state, level, pos, player);
    }

    /** Upstream {@code useWithoutItem}. */
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos storagePos = getPos(state, pos);
        if (!(level.getBlockEntity(storagePos) instanceof WorkstationBE storageBE)) return InteractionResult.PASS;
        WorkstationContext ctx = new WorkstationContext(player, storagePos);

        ItemStack stack = storageBE.inventory.getStackInSlot(0);
        if (stack.isEmpty()) return InteractionResult.PASS;

        GameProfile owner = storageBE.owner;
        if (owner == null) {
            storageBE.owner = player.getGameProfile();
        } else if (ModComponents.isLocked(stack) && !Objects.equals(owner.getId(), player.getGameProfile().getId())) {
            player.displayClientMessage(Component.translatable("error.tempad.block_locked", getName()).withStyle(WorkstationBlock.orange()), true);
            return InteractionResult.FAIL;
        }

        TempadApp<?> app = AppRegistry.get(ModComponents.getDefaultApp(stack), ctx, true);
        if (app == null) app = Objects.requireNonNull(AppRegistry.get(ModApps.PORTAL_SETUP, ctx, true));
        app.openMenu((ServerPlayer) player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING).add(BlockStateProperties.TRIGGERED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (facing == relativeDir(state) && facingState.getBlock() == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, neighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    public BlockPos getPos(BlockState state, BlockPos pos) {
        return pos.relative(relativeDir(state));
    }

    public Direction relativeDir(BlockState state) {
        return Direction.fromYRot(state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 90.0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockPos ogBlock = getPos(state, pos);
        boolean neighborPowered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above()) || level.hasNeighborSignal(ogBlock);
        BlockPos controllerPos = getPos(state, pos);
        if (!(level.getBlockEntity(controllerPos) instanceof WorkstationBE blockEntity)) return;
        if (neighborPowered && !blockEntity.isActive()) {
            blockEntity.activateLeft();
        } else if (!neighborPowered && blockEntity.isActive()) {
            blockEntity.deactivateLeft();
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos blockpos = getPos(state, pos);
            BlockState blockstate = level.getBlockState(blockpos);
            Direction dir = blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (blockstate.is(ModBlocks.workstation.get()) && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == dir) {
                level.destroyBlock(blockpos, !player.isCreative());
                level.levelEvent(player, 2001, blockpos, getId(blockstate));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(ModBlocks.workstation.get());
    }
}
