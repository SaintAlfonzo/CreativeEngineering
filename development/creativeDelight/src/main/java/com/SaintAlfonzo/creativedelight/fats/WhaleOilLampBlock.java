// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.fats;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Whale Oil Lamp (dairy-fats-and-batter #1): a lantern (standing or hanging, waterloggable, same shape) that is
 * placed unlit and gives a lantern's light once lit by hand with flint and steel or a fire charge. Not lit while
 * waterlogged. The oil tank, burn rate and refill of the row are not built yet.
 */
public class WhaleOilLampBlock extends LanternBlock {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	/** Light of a lit lamp: the vanilla lantern's. */
	public static final int LIGHT = 15;

	public WhaleOilLampBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false));
	}

	public static int light(BlockState state) {
		return state.getValue(LIT) ? LIGHT : 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIT);
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (state.getValue(LIT) || state.getValue(WATERLOGGED)) return InteractionResult.PASS;
		ItemStack held = player.getItemInHand(hand);
		boolean flint = held.is(Items.FLINT_AND_STEEL);
		if (!flint && !held.is(Items.FIRE_CHARGE)) return InteractionResult.PASS;
		if (!level.isClientSide) {
			level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL_IMMEDIATE);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			if (flint) {
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
						level.getRandom().nextFloat() * 0.4F + 0.8F);
				held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
			} else {
				level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F,
						(level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
				if (!player.getAbilities().instabuild) held.shrink(1);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighbor,
			net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		BlockState out = super.updateShape(state, direction, neighbor, level, pos, neighborPos);
		// Water puts the flame out
		if (out.is(this) && out.getValue(WATERLOGGED) && out.getValue(LIT)) out = out.setValue(LIT, false);
		return out;
	}
}
