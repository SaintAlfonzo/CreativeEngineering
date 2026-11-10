package com.SaintAlfonzo.creativedelight.hearth.machine;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

/** Field Sprinkler, {@code hearthandharvest:sprinkler} (D9). */
public class SprinklerBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 8, 14), Block.box(7, 8, 7, 9, 13, 9), Block.box(1, 9, 6, 15, 13, 10), Block.box(6, 9, 1, 10, 13, 15));
    private static final double[][] NOZZLES = {{0.03125, 0.59375, 0.5}, {0.96875, 0.59375, 0.5}, {0.5, 0.59375, 0.03125}, {0.5, 0.59375, 0.96875}};

    public SprinklerBlock(Properties properties) {
        super(properties);
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
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SprinklerBlockEntity be ? MachineFluids.comparator(be.tank()) : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof SprinklerBlockEntity be) || be.tank().isEmpty()) return;
        for (double[] n : NOZZLES) {
            if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + n[0], pos.getY() + n[1], pos.getZ() + n[2], 0, 0, 0);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SprinklerBlockEntity be)) return InteractionResult.PASS;
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) return InteractionResult.CONSUME;
        ItemStack held = player.getItemInHand(hand);
        if (be.addFertilizer(held, player.getAbilities().instabuild) > 0) return InteractionResult.CONSUME;
        ItemStack fertilizer = be.fertilizer();
        Component line = fertilizer.isEmpty() ? Component.translatable("tooltip.hearthandharvest.sprinkler.fertilizer_none")
                : Component.translatable("tooltip.hearthandharvest.sprinkler.fertilizer_count", fertilizer.getCount(), fertilizer.getHoverName());
        player.displayClientMessage(Component.translatable("tooltip.hearthandharvest.sprinkler.status", be.tank().getFluidAmount(), SprinklerBlockEntity.CAPACITY, line), true);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, HHMachines.SPRINKLER_BE.get(), SprinklerBlockEntity::serverTick);
    }
}
