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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The Whale Oil Lamp (dairy-fats-and-batter #1, #3): a lantern (standing or hanging, waterloggable, same shape) that is
 * placed unlit and gives a lantern's light once lit by hand with flint and steel or a fire charge. Not lit while
 * waterlogged or empty. It burns oil from its tank ({@link WhaleOilLampBlockEntity}) and goes out when empty; a Whale
 * Oil bottle or a fluid container holding whale oil refills it.
 */
public class WhaleOilLampBlock extends LanternBlock implements EntityBlock {

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
		ItemStack held = player.getItemInHand(hand);
		InteractionResult refill = refill(level, pos, player, hand, held);
		if (refill != null) return refill;
		if (state.getValue(LIT) || state.getValue(WATERLOGGED)) return InteractionResult.PASS;
		if (level.getBlockEntity(pos) instanceof WhaleOilLampBlockEntity be && be.oil() <= 0) return InteractionResult.PASS;
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

	/** Whale oil fluid ({@code creativedelight:whale_oil}, a KubeJS fluid). */
	private static final ResourceLocation WHALE_OIL = new ResourceLocation("creativedelight", "whale_oil");

	/**
	 * Refill from the held item: a Whale Oil bottle adds {@code bottleMb} when it fits (the bottle comes back), any
	 * fluid container holding whale oil gives up to {@code bucketMb}. Null when the item is not whale oil.
	 */
	private static InteractionResult refill(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held) {
		if (held.isEmpty() || !(level.getBlockEntity(pos) instanceof WhaleOilLampBlockEntity be)) return null;
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(held.getItem());
		if (WHALE_OIL.equals(id)) {
			int bottle = com.SaintAlfonzo.creativedelight.CDConfig.LAMP.bottleMb();
			if (be.space() < bottle) return InteractionResult.PASS;
			if (!level.isClientSide) {
				be.fill(bottle);
				if (!player.getAbilities().instabuild) {
					ItemStack remainder = held.getCraftingRemainingItem();
					held.shrink(1);
					if (!remainder.isEmpty() && !player.getInventory().add(remainder)) player.drop(remainder, false);
				}
				level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		FluidStack inside = FluidUtil.getFluidContained(held).orElse(FluidStack.EMPTY);
		if (inside.isEmpty() || !WHALE_OIL.equals(ForgeRegistries.FLUIDS.getKey(inside.getFluid()))) return null;
		int want = Math.min(be.space(), com.SaintAlfonzo.creativedelight.CDConfig.LAMP.bucketMb());
		if (want <= 0) return InteractionResult.PASS;
		if (level.isClientSide) return InteractionResult.SUCCESS;
		ItemStack one = held.copy();
		one.setCount(1);
		var handler = FluidUtil.getFluidHandler(one).orElse(null);
		if (handler == null) return InteractionResult.PASS;
		FluidStack drained = handler.drain(new FluidStack(inside.getFluid(), want), IFluidHandler.FluidAction.EXECUTE);
		if (drained.isEmpty()) return InteractionResult.PASS;
		be.fill(drained.getAmount());
		if (!player.getAbilities().instabuild) {
			held.shrink(1);
			ItemStack back = handler.getContainer();
			if (held.isEmpty()) player.setItemInHand(hand, back);
			else if (!player.getInventory().add(back)) player.drop(back, false);
		}
		SoundEvent sound = inside.getFluid().getFluidType().getSound(net.minecraftforge.common.SoundActions.BUCKET_EMPTY);
		level.playSound(null, pos, sound != null ? sound : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		return InteractionResult.CONSUME;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WhaleOilLampBlockEntity(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide || type != WhaleOilLamp.BLOCK_ENTITY.get()) return null;
		return (l, p, s, be) -> WhaleOilLampBlockEntity.serverTick(l, p, s, (WhaleOilLampBlockEntity) be);
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
