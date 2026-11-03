package com.SaintAlfonzo.creativedelight.barbeques.block;

import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import vectorwing.farmersdelight.common.registry.ModSounds;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Grill (1.3.0 GrillBlock + GrillPlace). Two slots on top: a raw skewer goes on an empty slot, sneak with an empty
 * hand flips, an empty hand takes, a seasoning seasons.
 *
 * Built-in campfire, port rule O11 (barbeques_delight.md 3.4.1): placed on a plain campfire (or a campfire clicked onto
 * its side) the grill takes it in, unlit, as the pack's campfires are placed. From then on it is a vanilla campfire:
 * {@code lit} gives heat and light 15; flint and steel, a fire charge or a burning projectile light it; water
 * (waterlogging), a splash water bottle and a shovel put it out; rain puts it out by the pack rule (GrillBlockEntity).
 * Breaking drops the grill and the campfire (loot table). 1.3.0's campfire was permanently lit (B4).
 */
public class GrillBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_CAMPFIRE = BooleanProperty.create("has_campfire");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape OUTER = Block.box(1.0, 12.0, 1.0, 15.0, 15.0, 15.0);
    public static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Block.box(1.0, 15.0, 1.0, 15.0, 16.0, 15.0), BooleanOp.ONLY_FIRST);

    public GrillBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_CAMPFIRE, false)
                .setValue(LIT, false).setValue(WATERLOGGED, false));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(HAS_CAMPFIRE) && state.getValue(LIT) ? 15 : 0;
    }

    /** A plain campfire the grill may take in (soul campfires are not merged, as 1.3.0). */
    public static boolean canMerge(BlockState state) {
        return state.is(Blocks.CAMPFIRE) && !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_CAMPFIRE, LIT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        boolean campfire = canMerge(level.getBlockState(pos));
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(HAS_CAMPFIRE, campfire)
                .setValue(LIT, false).setValue(WATERLOGGED, !campfire && level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrillBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BBQBlocks.GRILL_BE.get(), GrillBlockEntity::tick);
    }

    // ----- campfire rules (O11)

    public static void extinguish(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            level.levelEvent(null, 1009, pos, 0);
        }
        level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
    }

    private static boolean canLight(BlockState state) {
        return state.getValue(HAS_CAMPFIRE) && !state.getValue(LIT) && !state.getValue(WATERLOGGED);
    }

    @Nullable
    private static InteractionResult campfireUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                 ItemStack held, BlockHitResult hit) {
        if (!state.getValue(HAS_CAMPFIRE)) {
            if (held.is(Items.CAMPFIRE) && hit.getDirection().getAxis() != Direction.Axis.Y && !state.getValue(WATERLOGGED)) {
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos, state.setValue(HAS_CAMPFIRE, true).setValue(LIT, false));
                    level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return null;
        }
        if (canLight(state) && (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE))) {
            if (held.is(Items.FLINT_AND_STEEL)) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            } else {
                level.playSound(player, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.getValue(LIT) && held.getItem() instanceof ShovelItem) {
            extinguish(level, pos, state);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos pos = hit.getBlockPos();
        if (!level.isClientSide && projectile.isOnFire() && projectile.mayInteract(level, pos) && canLight(state)) {
            level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    /** A splash water bottle puts the campfire out, as vanilla's ThrownPotion#dowseFire does for campfires. */
    public static void onSplashWater(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion) || potion.level.isClientSide
                || !(event.getRayTraceResult() instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ItemStack item = potion.getItem();
        if (PotionUtils.getPotion(item) != Potions.WATER || !PotionUtils.getMobEffects(item).isEmpty()) {
            return;
        }
        Level level = potion.level;
        BlockPos at = hit.getBlockPos().relative(hit.getDirection());
        List<BlockPos> around = new ArrayList<>(List.of(at, hit.getBlockPos()));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            around.add(at.relative(d));
        }
        for (BlockPos p : around) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof GrillBlock && s.getValue(HAS_CAMPFIRE) && s.getValue(LIT)) {
                extinguish(level, p, s);
            }
        }
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if (state.getValue(WATERLOGGED) || fluid.getType() != Fluids.WATER) {
            return false;
        }
        boolean lit = state.getValue(LIT);
        if (lit && !level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(LIT, false), Block.UPDATE_ALL);
        level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, dir, neighbour, level, pos, neighbourPos);
    }

    // ----- slots

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        InteractionResult camp = campfireUse(state, level, pos, player, hand, held, hit);
        if (camp != null) {
            return camp;
        }
        if (!(level.getBlockEntity(pos) instanceof GrillBlockEntity grill)) {
            return InteractionResult.PASS;
        }
        int i = grill.getSlotForHitting(hit, level);
        if (i < 0 || i >= grill.size()) {
            return InteractionResult.PASS;
        }
        ItemStack item = grill.getStack(i);
        if (held.isEmpty()) {
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown()) {
                return grill.flip(i) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.FAIL;
            }
            if (item.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                ItemStack ret = item.split(1);
                player.setItemInHand(hand, ret);
                grill.inventoryChanged();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (item.isEmpty()) {
            return grill.addItem(i, held) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.FAIL;
        }
        // 1.3.0 seasoned a skewer on the grill by right-click here; retired at the seasoning fold (food/seasoning #11)
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.isBarbecuing() && random.nextInt(8) == 0) {
            level.playLocalSound(x, y, z, ModSounds.BLOCK_SKILLET_SIZZLE.get(), SoundSource.BLOCKS, 0.4F, random.nextFloat() * 0.2F + 0.9F, false);
        }
        if (state.getValue(HAS_CAMPFIRE) && state.getValue(LIT)) {
            if (random.nextInt(10) == 0) {
                level.playLocalSound(x, y + 0.5, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }
            if (random.nextFloat() < 0.11F) {
                CampfireBlock.makeParticles(level, pos, false, false);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill) {
                for (GrillBlockEntity.ItemEntry e : grill.entries) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), e.stack);
                }
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }
}
