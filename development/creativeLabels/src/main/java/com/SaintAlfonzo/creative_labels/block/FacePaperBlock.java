package com.SaintAlfonzo.creative_labels.block;

import com.SaintAlfonzo.creative_labels.item.NoteColours;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * {@code creative_labels:face_paper}: the air cell in front of papered host faces (BRIEF 3). State: one boolean per
 * direction ({@link PipeBlock#PROPERTY_BY_DIRECTION}; face d = paper on the host at pos.relative(d)) + WATERLOGGED.
 * No item. The content is in {@link FacePaperBlockEntity}.
 *
 * <p>Scaffold stage: properties, state, shapes, drops, rotation and the clone stack are in; the behaviour methods
 * ({@link #use}, breaking one piece, host-gone checks, the gold spark) are stubs for phase 1.
 */
public class FacePaperBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public static final Map<Direction, BooleanProperty> FACE = PipeBlock.PROPERTY_BY_DIRECTION;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static Properties properties() {
        return Properties.of(Material.CLOTH_DECORATION)
                .strength(0.1f)
                .noCollission()
                .noOcclusion()
                .sound(SoundType.WOOL)
                .noLootTable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    public FacePaperBlock(Properties properties) {
        super(properties);
        BlockState def = stateDefinition.any().setValue(WATERLOGGED, false);
        for (BooleanProperty p : FACE.values()) def = def.setValue(p, false);
        registerDefaultState(def);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.NORTH, PipeBlock.EAST, PipeBlock.SOUTH, PipeBlock.WEST, PipeBlock.UP, PipeBlock.DOWN, WATERLOGGED);
    }

    // ----- state helpers -----

    public static boolean hasFace(BlockState state, Direction d) {
        return state.getValue(FACE.get(d));
    }

    public static BlockState withFace(BlockState state, Direction d, boolean on) {
        return state.setValue(FACE.get(d), on);
    }

    /** Bit = Direction ordinal of every set face boolean. */
    public static int mask(BlockState state) {
        return FacePaperBlockEntity.maskOf(state);
    }

    public static boolean anyFace(BlockState state) {
        return mask(state) != 0;
    }

    @Nullable
    public static FacePaperBlockEntity entity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FacePaperBlockEntity be ? be : null;
    }

    // ----- block entity -----

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FacePaperBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ----- shapes -----

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        FacePaperBlockEntity be = entity(level, pos);
        return be == null ? Shapes.empty() : be.shape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return false;
    }

    // ----- placement rules -----

    /** Q1: nothing can be placed into a papered cell. */
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    /** C1: carpet-like resistance for the ray maths, but the blast itself never removes paper. */
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {}

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    // ----- water -----

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ----- rotation (C7: the block entity remaps its faces from the saved Mask on load) -----

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Direction d : Direction.values()) out = withFace(out, rotation.rotate(d), hasFace(state, d));
        return out;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Direction d : Direction.values()) out = withFace(out, mirror.mirror(d), hasFace(state, d));
        return out;
    }

    // ----- drops -----

    /** Every remaining piece (pistons / explosions of the cell itself drop everything). */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return be instanceof FacePaperBlockEntity paper ? paper.allDrops() : List.of();
    }

    /** Hit-aware: the piece under the crosshair (a note with its data, a blank label). */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null) return ItemStack.EMPTY;
        Vec3 loc = target == null ? Vec3.atCenterOf(pos) : target.getLocation();
        PieceHit hit = PieceHit.resolve(state, be, loc);
        if (hit == null) return ItemStack.EMPTY;
        FaceContent c = be.face(hit.face());
        if (hit.isLabel() && c.label != null) return c.label.toItem();
        if (hit.isNote()) {
            NoteData n = c.note(hit.spot());
            return n == null ? ItemStack.EMPTY : n.toItem();
        }
        // Panel of a covered face: the note nearest to the hit.
        int spot = Spots.nearestSpot(hit.face(), c.rotation, loc.subtract(pos.getX(), pos.getY(), pos.getZ()), c.noteMask());
        NoteData n = spot < 0 ? null : c.note(spot);
        return n == null ? ItemStack.EMPTY : n.toItem();
    }

    // ----- behaviour -----

    /** Per-piece actions (table 3.4 for notes, 3.10 for labels), see {@link FacePaperInteractions#use}. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null) return InteractionResult.PASS;
        PieceHit piece = PieceHit.resolve(state, be, hit.getLocation());
        if (piece == null) return InteractionResult.PASS;
        return FacePaperInteractions.use(level, pos, state, be, piece, player, hand, hit);
    }

    /**
     * Only the piece under the player's pick breaks (both sides): it is removed and dropped (server, when
     * harvesting), the block stays and this returns false. The last piece goes through the vanilla path, which
     * drops it through {@link #getDrops}.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null || FacePaperInteractions.breakPiece(level, pos, state, be, player, willHarvest)) {
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
        playerWillDestroy(level, pos, state, player); // break particles, sound and game event for the piece
        return false;
    }

    /** The host on face {@code direction} changed: re-fit that face and drop what no longer fits. */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        if (!hasFace(state, direction)) return state;
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null) return state;
        return FacePaperInteractions.hostChanged(level, pos, state, be, direction, neighborState, neighborPos);
    }

    /** Block updates from a host (the second check of BRIEF 3): same re-fit as {@link #updateShape}. */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        Direction direction = Direction.fromNormal(fromPos.getX() - pos.getX(), fromPos.getY() - pos.getY(), fromPos.getZ() - pos.getZ());
        if (direction == null || !hasFace(state, direction)) return;
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null) return;
        BlockState next = FacePaperInteractions.hostChanged(level, pos, state, be, direction, level.getBlockState(fromPos), fromPos);
        if (next != state) level.setBlock(pos, next, UPDATE_ALL);
    }

    /** Gold spark (C13): a firework particle from each gold note, roughly once a second. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        FacePaperBlockEntity be = entity(level, pos);
        if (be == null) return;
        for (Direction d : Direction.values()) {
            FaceContent c = be.face(d);
            if (c.kind != FaceContent.Kind.NOTES) continue;
            for (int spot = 0; spot < Spots.COUNT; spot++) {
                NoteData n = c.notes[spot];
                if (n == null || !NoteColours.GOLD.name().equals(n.special)) continue;
                Vec3 p = Spots.noteBox(d, c.rotation, spot, c.inset).getCenter();
                level.addParticle(ParticleTypes.FIREWORK, pos.getX() + p.x, pos.getY() + p.y, pos.getZ() + p.z,
                        random.nextGaussian() * 0.03, random.nextGaussian() * 0.03, random.nextGaussian() * 0.03);
            }
        }
    }
}
