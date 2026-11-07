// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/WorkstationBlock.kt. Translated to Java for Forge 1.19.2:
// - useItemOn + useWithoutItem (1.21) -> use(): the held-item branch first, then (main hand only, when it passed) the
//   empty-hand branch, as 1.21's ServerPlayerGameMode calls them; PASS_TO_DEFAULT_BLOCK_INTERACTION -> PASS.
// - Properties.of() -> Properties.of(Material.STONE, MaterialColor.NONE) (1.19.2 needs a material; STONE is solid,
//   not replaceable, normal push reaction, as 1.21's default properties; NONE is 1.21's default map colour).
// - no block codec (1.19.2 has none); playerWillDestroy returns void; getDrops takes LootContext.Builder.
// - Holder.direct(sound) in ClientboundSoundPacket -> the SoundEvent; playSound(player, pos, sound, source) -> volume
//   and pitch 1 (the 1.21 overload's defaults).
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.recipe.TempadUpgradeRecipe;
import earth.terrarium.tempad.common.recipe.UpgradeRecipeInput;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.registries.ModRecipes;
import earth.terrarium.tempad.common.registries.ModSounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkstationBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_TAPE = BooleanProperty.create("has_tape");

    public static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(0.0, 0.0, 1.0, 14.0, 2.0, 15.0),
            box(0.0, 2.0, 1.0, 14.0, 4.0, 5.0)
    );

    public static final VoxelShape SOUTH_SHAPE = Shapes.or(
            box(2.0, 0.0, 1.0, 16.0, 2.0, 15.0),
            box(2.0, 2.0, 11.0, 16.0, 4.0, 15.0)
    );

    public static final VoxelShape WEST_SHAPE = Shapes.or(
            box(1.0, 0.0, 2.0, 15.0, 2.0, 16.0),
            box(1.0, 2.0, 2.0, 5.0, 4.0, 16.0)
    );

    public static final VoxelShape EAST_SHAPE = Shapes.or(
            box(1.0, 0.0, 0.0, 15.0, 2.0, 14.0),
            box(11.0, 2.0, 0.0, 15.0, 4.0, 14.0)
    );

    public WorkstationBlock() {
        super(Properties.of(Material.STONE, MaterialColor.NONE).noOcclusion().strength(3.0f, 1200f));
        this.registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(HAS_TAPE, false));
    }

    static Style orange() {
        return Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        InteractionResult withItem = useItemOn(player.getItemInHand(hand), state, level, pos, player, hand);
        if (withItem != InteractionResult.PASS || hand != InteractionHand.MAIN_HAND) return withItem;
        return useWithoutItem(state, level, pos, player);
    }

    /** Upstream {@code useItemOn} (server side); PASS = PASS_TO_DEFAULT_BLOCK_INTERACTION. */
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof WorkstationBE blockEntity)) return InteractionResult.PASS;
        if (blockEntity.inventory.getStackInSlot(0).isEmpty() && stack.is(ModItems.tempad.get())) {
            ModComponents.setOwner(stack, player.getGameProfile());
            blockEntity.inventory.setStackInSlot(0, stack);
            blockEntity.setChanged();
            player.setItemInHand(hand, ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, UPDATE_ALL);
            return InteractionResult.SUCCESS;
        } else if (!blockEntity.inventory.getStackInSlot(0).isEmpty() && blockEntity.downloadTime == 0) {
            TempadUpgradeRecipe recipe = level.getRecipeManager()
                    .getRecipeFor(ModRecipes.upgradeRecipe.get(), new UpgradeRecipeInput(blockEntity.inventory.getStackInSlot(0), stack), level)
                    .orElse(null);
            if (recipe == null || Objects.requireNonNull(UpgradeHandler.of(blockEntity)).contains(recipe.output())) return InteractionResult.PASS;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSoundPacket(ModSounds.upgradePlaceMono.get(), SoundSource.BLOCKS,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, 1.0f, 0));
                level.playSound(serverPlayer, pos, ModSounds.upgradePlaceStereo.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            level.playSound(null, pos, ModSounds.upgradeInstalling.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            blockEntity.downloadTime = recipe.downloadTime();
            blockEntity.maxDownloadTime = recipe.downloadTime();
            blockEntity.recipe = recipe.output();
            stack.shrink(1);
            blockEntity.setChanged();
            level.setBlock(pos, state.setValue(HAS_TAPE, true), UPDATE_ALL);
            if (stack.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Upstream {@code useWithoutItem} (server side). */
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof WorkstationBE blockEntity)) return InteractionResult.PASS;
        if (blockEntity.maxDownloadTime > 0) return InteractionResult.PASS;
        ItemStack stack = blockEntity.inventory.getStackInSlot(0);
        GameProfile owner = ModComponents.getOwner(stack);
        if (ModComponents.isLocked(stack) && (owner == null || !Objects.equals(owner.getId(), player.getGameProfile().getId()))) {
            player.displayClientMessage(Component.translatable("error.tempad.block_locked", getName()).withStyle(orange()), true);
            return InteractionResult.FAIL;
        }
        ModComponents.setOwner(stack, null);
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        } else {
            player.getInventory().placeItemBackInInventory(stack.copy());
        }
        blockEntity.inventory.setStackInSlot(0, ItemStack.EMPTY);
        level.sendBlockUpdated(pos, state, state, UPDATE_ALL);
        blockEntity.setChanged();
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlocks.workstationBE.get(), (l, p, s, be) -> be.tick());
    }

    public BlockPos getPos(Direction dir, BlockPos pos) {
        return pos.relative(Direction.fromYRot(dir.toYRot() - 90.0));
    }

    public Direction relativeDir(BlockState state) {
        return Direction.fromYRot(state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() - 90.0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        BlockPos childPos = getPos(direction, context.getClickedPos());
        Level level = context.getLevel();
        if (level.getBlockState(childPos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(childPos)) {
            return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockState newState = ModBlocks.workstationChild.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
            BlockPos blockpos = getPos(dir, pos);
            level.setBlock(blockpos, newState, UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            newState.updateNeighbourShapes(level, pos, UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING).add(HAS_TAPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkstationBE(pos, state);
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

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos blockpos = getPos(dir, pos);
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(ModBlocks.workstationChild.get()) && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == dir) {
                // Fix (TODO gameplay/tnt-and-portal #12): upstream's flags 35 still send the shape update back to this
                // pad, whose updateShape turns it to air through Block.updateOrDestroy with drops (the workstation and
                // the docked Tempad) before the creative removal. UPDATE_KNOWN_SHAPE skips that update.
                level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), UPDATE_ALL | UPDATE_KNOWN_SHAPE | UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, blockpos, getId(blockstate));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockE = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockE instanceof WorkstationBE station && !station.inventory.getStackInSlot(0).isEmpty()) {
            drops.add(station.inventory.getStackInSlot(0));
        }
        return List.copyOf(drops);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean neighborPowered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        if (!(level.getBlockEntity(pos) instanceof WorkstationBE blockEntity)) return;
        if (neighborPowered && !blockEntity.isActive()) {
            blockEntity.activateRight();
        } else if (!neighborPowered && blockEntity.isActive()) {
            blockEntity.deactivateRight();
        }
    }
}
