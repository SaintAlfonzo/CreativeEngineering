// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.bucket;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The wooden cauldron's contents: one {@link FluidTank} of {@link #CAPACITY} mB holding one of {@link #ACCEPTED}.
 * Buckets move a whole 1000 mB; pipes may move any amount. The block's {@code level} (0 to 3) follows the amount
 * ({@link #levelOf}); the renderer draws the surface from the exact amount.
 */
public class WoodenCauldronBlockEntity extends BlockEntity {

	public static final int CAPACITY = 1000;
	/** One level (rain adds one): 333, 666, then 1000 for the third. */
	public static final int LEVEL_MB = 333;

	/** Water, cow milk, goat milk, nut milk, Create honey (plan_wood_bucket_batch_1121 R10, R11). Matched by id. */
	public static final Set<ResourceLocation> ACCEPTED = Set.of(
			WoodenBuckets.WATER_ID, WoodenBuckets.MILK_ID, WoodenBuckets.GOAT_MILK_ID, WoodenBuckets.NUT_MILK_ID,
			WoodenBuckets.HONEY_ID);

	private final FluidTank tank = new FluidTank(CAPACITY, WoodenCauldronBlockEntity::accepts) {
		@Override
		protected void onContentsChanged() {
			changed();
		}
	};
	private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> tank);

	public WoodenCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(WoodenBuckets.CAULDRON_BLOCK_ENTITY.get(), pos, state);
	}

	public static boolean accepts(FluidStack stack) {
		return !stack.isEmpty() && accepts(stack.getFluid());
	}

	public static boolean accepts(Fluid fluid) {
		return ACCEPTED.contains(ForgeRegistries.FLUIDS.getKey(fluid));
	}

	public static int levelOf(int amount) {
		return amount >= CAPACITY ? 3 : Math.min(2, amount / LEVEL_MB);
	}

	public FluidTank tank() {
		return tank;
	}

	public FluidStack fluid() {
		return tank.getFluid();
	}

	/** Replaces the contents (bucket pour, bucket scoop, rain). */
	public void set(FluidStack stack) {
		tank.setFluid(stack.isEmpty() ? FluidStack.EMPTY : stack.copy());
		changed();
	}

	private void changed() {
		setChanged();
		if (level == null || level.isClientSide) {
			return;
		}
		BlockState state = getBlockState();
		int lvl = tank.isEmpty() ? 0 : levelOf(tank.getFluidAmount());
		if (state.hasProperty(WoodenCauldronBlock.LEVEL) && state.getValue(WoodenCauldronBlock.LEVEL) != lvl) {
			level.setBlock(worldPosition, state.setValue(WoodenCauldronBlock.LEVEL, lvl), Block.UPDATE_ALL);
		} else {
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tank.readFromNBT(tag.getCompound("Tank"));
		if (!tank.isEmpty() && !accepts(tank.getFluid())) {
			tank.setFluid(FluidStack.EMPTY);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("Tank", tank.writeToNBT(new CompoundTag()));
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.FLUID_HANDLER) {
			return handler.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		handler.invalidate();
	}
}
