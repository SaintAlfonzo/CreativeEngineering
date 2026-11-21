// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.bucket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Vanilla cauldron and dispenser hooks for the wooden water bucket and the empty one (plan_wood_bucket_batch_1121 R1,
 * D4). Run once from common setup (main thread). The other filled wooden buckets keep the default dispense (dropped
 * as an item).
 */
public final class WoodenBucketBehaviors {

	private WoodenBucketBehaviors() {}

	public static void register() {
		cauldrons();
		dispensers();
	}

	/** Wooden water bucket fills an empty or water cauldron to full; the empty one scoops a full water cauldron. */
	private static void cauldrons() {
		CauldronInteraction pour = (state, level, pos, player, hand, stack) -> {
			if (!level.isClientSide) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(WoodenBuckets.EMPTY.get())));
				player.awardStat(Stats.FILL_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
				level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		};
		CauldronInteraction.EMPTY.put(WoodenBuckets.WATER.get(), pour);
		CauldronInteraction.WATER.put(WoodenBuckets.WATER.get(), pour);
		CauldronInteraction.WATER.put(WoodenBuckets.EMPTY.get(), (state, level, pos, player, hand, stack) ->
				CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, new ItemStack(WoodenBuckets.WATER.get()),
						s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
	}

	private static void dispensers() {
		DispenserBlock.registerBehavior(WoodenBuckets.WATER.get(), new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

			@Override
			protected ItemStack execute(BlockSource source, ItemStack stack) {
				WoodenBucketItem bucket = (WoodenBucketItem) stack.getItem();
				BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
				Level level = source.getLevel();
				if (bucket.emptyContents(null, level, pos, null)) {
					bucket.checkExtraContent(null, level, stack, pos);
					return new ItemStack(WoodenBuckets.EMPTY.get());
				}
				return fallback.dispense(source, stack);
			}
		});
		DispenserBlock.registerBehavior(WoodenBuckets.EMPTY.get(), new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

			@Override
			protected ItemStack execute(BlockSource source, ItemStack stack) {
				BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
				Level level = source.getLevel();
				BlockState state = level.getBlockState(pos);
				FluidState fluid = level.getFluidState(pos);
				if (!fluid.isSource() || fluid.getType() != Fluids.WATER || !(state.getBlock() instanceof BucketPickup pickup)) {
					return fallback.dispense(source, stack);
				}
				if (pickup.pickupBlock(level, pos, state).isEmpty()) {
					return fallback.dispense(source, stack);
				}
				level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
				ItemStack filled = new ItemStack(WoodenBuckets.WATER.get());
				stack.shrink(1);
				if (stack.isEmpty()) {
					return filled;
				}
				if (source.<DispenserBlockEntity>getEntity().addItem(filled) < 0) {
					fallback.dispense(source, filled);
				}
				return stack;
			}
		});
	}
}
