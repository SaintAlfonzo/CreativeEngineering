// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Decision D4 (power_grid.md 5.1): an overheated device does not explode or break; it turns into this
// block, which keeps the device's block state and block entity data for inspection, renders the device with a crack
// overlay, has no item and drops the device only on a chance (config electricity.overheatedDropChance).
package org.patryk3211.powergrid.electricity.overheated;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedConfigs;

import java.util.List;

public class OverheatedComponentBlock extends Block implements IBE<OverheatedComponentBlockEntity> {
    public OverheatedComponentBlock(Properties properties) {
        super(properties);
    }

    /**
     * Replaces the device at {@code pos} with an Overheated Component holding its state and block entity data. Called by
     * every ThermalBehaviour overheat path (D4). Server side only; does nothing for air or an already overheated block.
     *
     * @return true if the block was replaced
     */
    public static boolean overheat(Level level, BlockPos pos) {
        if(level.isClientSide)
            return false;
        var state = level.getBlockState(pos);
        if(state.isAir() || state.getBlock() instanceof OverheatedComponentBlock)
            return false;
        var be = level.getBlockEntity(pos);
        CompoundTag data = be != null ? be.saveWithFullMetadata() : null;

        // Removing the device first runs its normal removal (IBE.onRemove: electric behaviour leaves the network,
        // attached wires drop as they do when the block is broken), then the overheated block takes its place.
        level.setBlock(pos, ModdedBlocks.OVERHEATED_COMPONENT.getDefaultState(), Block.UPDATE_ALL);
        if(level.getBlockEntity(pos) instanceof OverheatedComponentBlockEntity overheated) {
            overheated.setOriginal(state, data);
        } else {
            PowerGrid.LOGGER.warn("Overheated component at {} has no block entity", pos);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 0.8f);
        if(level instanceof ServerLevel server)
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.01);
        return true;
    }

    private static BlockState originalOf(BlockGetter level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof OverheatedComponentBlockEntity be)
            return be.getOriginalState();
        return null;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        var original = originalOf(level, pos);
        if(original == null || original.getBlock() instanceof OverheatedComponentBlock)
            return Shapes.block();
        try {
            return original.getShape(level, pos, context);
        } catch(RuntimeException e) {
            // A device shape that needs its own block entity or neighbours; fall back to a full block.
            return Shapes.block();
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // The original device is drawn by OverheatedComponentRenderer.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootContext.Builder builder) {
        var be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(!(be instanceof OverheatedComponentBlockEntity overheated))
            return List.of();
        var original = overheated.getOriginalState();
        if(original == null)
            return List.of();
        float chance = ModdedConfigs.server().electricity.overheatedDropChance.getF();
        if(builder.getLevel().getRandom().nextFloat() >= chance)
            return List.of();
        var item = original.getBlock().asItem();
        if(item == net.minecraft.world.item.Items.AIR)
            return List.of();
        return List.of(new ItemStack(item));
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, HitResult target, @NotNull BlockGetter level, @NotNull BlockPos pos, net.minecraft.world.entity.player.Player player) {
        // No item (D4): pick-block gives nothing.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState state) {
        return false;
    }

    @Override
    public int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return 0;
    }

    @Override
    public Class<OverheatedComponentBlockEntity> getBlockEntityClass() {
        return OverheatedComponentBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OverheatedComponentBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.OVERHEATED_COMPONENT.get();
    }
}
