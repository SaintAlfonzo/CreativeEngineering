package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * {@code farmingforblockheads:courier_spawn}, the Courier Spawn Point (ruling D5b, our own id): a walking courier starts
 * at, and walks back to, the nearest one within {@code courierSpawnSearchRadius} of the bin (D9, D5c). Temporary look and
 * recipe (D8, TODO mod-integration #8, #9): the vanilla cartography table's textures, a chest in 8 trapdoors.
 */
public class CourierSpawnBlock extends Block {
	public CourierSpawnBlock(Properties properties) {
		super(properties);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (level instanceof ServerLevel server && !oldState.is(this)) CourierSpawnPoints.get(server).add(pos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (level instanceof ServerLevel server && !newState.is(this)) CourierSpawnPoints.get(server).remove(pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	@SuppressWarnings("deprecation")
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}
}
