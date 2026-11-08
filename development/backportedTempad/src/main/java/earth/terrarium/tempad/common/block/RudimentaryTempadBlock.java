// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/RudimentaryTempadBlock.kt (block
// tempad:timedoor_projector). Translated to Java for Forge 1.19.2:
// - useItemOn + useWithoutItem (1.21) -> use(): the held-item branch first, then (main hand only, when it passed) the
//   empty-hand branch, as 1.21's ServerPlayerGameMode calls them; PASS_TO_DEFAULT_BLOCK_INTERACTION -> PASS.
// - Properties.of() -> Properties.of(Material.STONE, MaterialColor.NONE) (as the workstation, PHASE2 5.1).
// - no block codec (1.19.2 has none); getDrops takes LootContext.Builder and reads the block entity with
//   getOptionalParameter (1.21's getParameter throws when it is missing).
// - card insert / remove also mark the block entity changed (upstream relies on the block state change to save it).
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class RudimentaryTempadBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_CARD = BooleanProperty.create("has_card");

    public static final VoxelShape BASE_SHAPE = box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static final VoxelShape WEST_SHAPE = Shapes.or(BASE_SHAPE, box(0.0, 2.0, 0.0, 8.0, 4.0, 8.0));
    public static final VoxelShape EAST_SHAPE = Shapes.or(BASE_SHAPE, box(8.0, 2.0, 8.0, 16.0, 4.0, 16.0));
    public static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE_SHAPE, box(0.0, 2.0, 8.0, 8.0, 4.0, 16.0));
    public static final VoxelShape NORTH_SHAPE = Shapes.or(BASE_SHAPE, box(8.0, 2.0, 0.0, 16.0, 4.0, 8.0));

    public RudimentaryTempadBlock() {
        super(Properties.of(Material.STONE, MaterialColor.NONE).strength(3.0f, 6.0f));
        this.registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.TRIGGERED, false)
                .setValue(HAS_CARD, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hasCard = ModComponents.getPortalTarget(context.getItemInHand()) != null;
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection()).setValue(HAS_CARD, hasCard);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        ItemStack stack = new ItemStack(ModItems.timedoorProjector.get());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof RudimentaryTempadBE blockEntity) {
            ModComponents.setChrononContent(stack, blockEntity.chrononContent);
            ModComponents.setPortalTarget(stack, blockEntity.portalTarget);
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.add(stack);
        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult withItem = useItemOn(player.getItemInHand(hand), state, level, pos, player);
        if (withItem != InteractionResult.PASS || hand != InteractionHand.MAIN_HAND) return withItem;
        return useWithoutItem(state, level, pos, player);
    }

    /** Upstream {@code useItemOn}; PASS = PASS_TO_DEFAULT_BLOCK_INTERACTION. */
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (stack.is(ModItems.locationCard.get()) && ModComponents.getPortalTarget(stack) != null) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof RudimentaryTempadBE blockEntity)) return InteractionResult.FAIL;
            if (blockEntity.portalTarget != null) {
                ItemStack card = new ItemStack(ModItems.locationCard.get());
                ModComponents.setPortalTarget(card, blockEntity.portalTarget);
                player.getInventory().placeItemBackInInventory(card);
            }
            blockEntity.portalTarget = ModComponents.getPortalTarget(stack);
            blockEntity.setChanged();
            level.setBlock(pos, state.setValue(HAS_CARD, true), 2);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Upstream {@code useWithoutItem}. */
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof RudimentaryTempadBE blockEntity)) return InteractionResult.FAIL;
        if (player.isShiftKeyDown()) {
            for (ItemStack drop : getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos))) {
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, drop);
                } else {
                    player.getInventory().placeItemBackInInventory(drop);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            return InteractionResult.SUCCESS;
        } else if (blockEntity.portalTarget != null) {
            ItemStack card = new ItemStack(ModItems.locationCard.get());
            ModComponents.setPortalTarget(card, blockEntity.portalTarget);
            player.getInventory().placeItemBackInInventory(card);
            blockEntity.portalTarget = null;
            blockEntity.setChanged();
            level.setBlock(pos, state.setValue(HAS_CARD, false), 2);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean neighborPowered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean currentlyPowered = state.getValue(BlockStateProperties.TRIGGERED);
        if (neighborPowered && !currentlyPowered) {
            level.scheduleTick(pos, this, 4);
            level.setBlock(pos, state.setValue(BlockStateProperties.TRIGGERED, true), 2);
        } else if (!neighborPowered && currentlyPowered) {
            level.setBlock(pos, state.setValue(BlockStateProperties.TRIGGERED, false), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof RudimentaryTempadBE blockEntity) blockEntity.openTimedoor();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RudimentaryTempadBE(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.TRIGGERED, HAS_CARD);
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
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> BASE_SHAPE;
        };
    }
}
