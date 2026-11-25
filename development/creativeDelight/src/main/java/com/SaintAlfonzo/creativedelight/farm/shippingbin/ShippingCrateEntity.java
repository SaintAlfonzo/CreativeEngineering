package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * The crate a load sold under a roof waits in, on top of the bin, until its courier lifts it (it then rides on the courier
 * and leaves with it). An effect only: it holds no items. Gone when the bin is gone, or when no courier took it long after
 * the walk timer.
 */
public class ShippingCrateEntity extends Entity {
	private BlockPos bin = BlockPos.ZERO;

	public ShippingCrateEntity(EntityType<? extends ShippingCrateEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	void setBin(BlockPos bin) {
		this.bin = bin.immutable();
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide || isPassenger()) return;
		boolean binGone = level.isLoaded(bin) && !(level.getBlockEntity(bin) instanceof ShippingBinBlockEntity);
		if (binGone || tickCount > CDConfig.SHIPPING_BIN.courierWalkTimeoutTicks() * 3 + 1200) discard();
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		bin = NbtUtils.readBlockPos(tag.getCompound("Bin"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.put("Bin", NbtUtils.writeBlockPos(bin));
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
