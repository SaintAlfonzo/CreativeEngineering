package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * A falling layer block ({@link FallingLayers}, plan_post_scratch_1115.md G7.5), id {@code creativecompat:falling_layer}.
 * Moves like vanilla falling sand and is drawn by vanilla's falling block renderer; on landing it stacks onto the
 * same layer family ({@link FallingLayers#land}) instead of vanilla's place-or-drop. Saved and reloaded like sand.
 */
public class FallingLayerEntity extends FallingBlockEntity {

	public FallingLayerEntity(EntityType<? extends FallingLayerEntity> type, Level level) {
		super(type, level);
	}

	/** Lifts the layer at {@code pos} into a falling entity (the spot keeps its water if the layer was waterlogged). */
	public static FallingLayerEntity fall(ServerLevel level, BlockPos pos, BlockState state) {
		FallingLayerEntity e = new FallingLayerEntity(FallingLayerEntities.FALLING_LAYER.get(), level);
		e.blocksBuilding = true;
		double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;
		e.setPos(x, y, z);
		e.xo = x;
		e.yo = y;
		e.zo = z;
		e.setDeltaMovement(Vec3.ZERO);
		e.setStartPos(e.blockPosition());
		BlockState carried = state.hasProperty(BlockStateProperties.WATERLOGGED)
				? state.setValue(BlockStateProperties.WATERLOGGED, false) : state;
		CompoundTag tag = new CompoundTag();
		tag.put("BlockState", NbtUtils.writeBlockState(carried));
		tag.putInt("Time", 0);
		tag.putBoolean("DropItem", true);
		tag.putBoolean("HurtEntities", false);
		e.readAdditionalSaveData(tag);
		level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
		level.addFreshEntity(e);
		return e;
	}

	@Override
	public void tick() {
		BlockState state = getBlockState();
		if (level.isClientSide || state.isAir()) {
			super.tick(); // client: vanilla motion; an empty state discards itself
			return;
		}
		time++;
		if (!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
		move(MoverType.SELF, getDeltaMovement());
		BlockPos pos = blockPosition();
		if (onGround) {
			if (!FallingLayers.land((ServerLevel) level, pos, state)) dropAll(pos, state);
			discard();
			return;
		}
		if (time > 600 || (time > 100 && (pos.getY() <= level.getMinBuildHeight() || pos.getY() > level.getMaxBuildHeight()))) {
			dropAll(pos, state);
			discard();
			return;
		}
		setDeltaMovement(getDeltaMovement().scale(0.98));
	}

	private void dropAll(BlockPos pos, BlockState state) {
		if (dropItem && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) FallingLayers.drop(level, pos, state);
	}
}
