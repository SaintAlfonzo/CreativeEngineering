// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.bucket;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * The wooden cauldron (plan_wood_bucket_batch_1121 D3, R10): vanilla cauldron shape, holds one of
 * {@link WoodenCauldronBlockEntity#ACCEPTED} at a time in 3 levels (1000 mB). Not a {@code CauldronBlock}, so no
 * vanilla cauldron interactions (dyes, banners, powder snow) and no salt or campfire evaporation. Any bucket item with a
 * fluid handler pours in a whole bucket (into an empty cauldron or one holding the same fluid, topping it up to full,
 * as vanilla does) or scoops a full one out. Rain fills it with water like a vanilla cauldron (D9). Create pipes reach
 * the tank through the fluid-handler capability.
 */
public class WoodenCauldronBlock extends BaseEntityBlock {

	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

	// Vanilla AbstractCauldronBlock's shapes.
	private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	private static final VoxelShape SHAPE = Shapes.join(Shapes.block(),
			Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
					box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE),
			BooleanOp.ONLY_FIRST);

	/** Vanilla cauldron's rain chance per precipitation tick. */
	private static final float RAIN_CHANCE = 0.05F;

	public WoodenCauldronBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WoodenCauldronBlockEntity(pos, state);
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
	public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return INSIDE;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return state.getValue(LEVEL);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof WoodenCauldronBlockEntity cauldron)) {
			return InteractionResult.PASS;
		}
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty()) {
			return InteractionResult.PASS;
		}
		ItemStack one = held.copy();
		one.setCount(1);
		Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(one).resolve();
		if (cap.isEmpty()) {
			return InteractionResult.PASS;
		}
		IFluidHandlerItem item = cap.get();
		FluidStack contents = cauldron.fluid();
		FluidStack inItem = item.drain(WoodenCauldronBlockEntity.CAPACITY, FluidAction.SIMULATE);

		if (!inItem.isEmpty()) {
			// Pour: a whole bucket of an accepted fluid into an empty cauldron or one of the same fluid.
			if (inItem.getAmount() < WoodenCauldronBlockEntity.CAPACITY || !WoodenCauldronBlockEntity.accepts(inItem)
					|| (!contents.isEmpty() && !contents.isFluidEqual(inItem))
					|| contents.getAmount() >= WoodenCauldronBlockEntity.CAPACITY) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide) {
				FluidStack poured = item.drain(WoodenCauldronBlockEntity.CAPACITY, FluidAction.EXECUTE);
				poured.setAmount(WoodenCauldronBlockEntity.CAPACITY);
				cauldron.set(poured);
				finish(level, pos, player, hand, held, item.getContainer(), sound(poured, SoundActions.BUCKET_EMPTY,
						SoundEvents.BUCKET_EMPTY), GameEvent.FLUID_PLACE);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		// Scoop: a full cauldron into an empty container that takes this fluid.
		if (contents.getAmount() < WoodenCauldronBlockEntity.CAPACITY) {
			return InteractionResult.PASS;
		}
		FluidStack full = contents.copy();
		full.setAmount(WoodenCauldronBlockEntity.CAPACITY);
		if (item.fill(full, FluidAction.SIMULATE) < WoodenCauldronBlockEntity.CAPACITY) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			item.fill(full, FluidAction.EXECUTE);
			cauldron.set(FluidStack.EMPTY);
			finish(level, pos, player, hand, held, item.getContainer(), sound(full, SoundActions.BUCKET_FILL,
					SoundEvents.BUCKET_FILL), GameEvent.FLUID_PICKUP);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	private static SoundEvent sound(FluidStack fluid, net.minecraftforge.common.SoundAction action, SoundEvent fallback) {
		SoundEvent sound = fluid.getFluid().getFluidType().getSound(fluid, action);
		return sound != null ? sound : fallback;
	}

	private static void finish(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held,
			ItemStack result, SoundEvent sound, GameEvent event) {
		player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, result));
		player.awardStat(Stats.ITEM_USED.get(held.getItem()));
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, event, pos);
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
		if (precipitation != Biome.Precipitation.RAIN || level.getRandom().nextFloat() >= RAIN_CHANCE) {
			return;
		}
		if (!(level.getBlockEntity(pos) instanceof WoodenCauldronBlockEntity cauldron)) {
			return;
		}
		FluidStack contents = cauldron.fluid();
		if (!contents.isEmpty() && contents.getFluid() != Fluids.WATER) {
			return;
		}
		int amount = contents.getAmount();
		if (amount >= WoodenCauldronBlockEntity.CAPACITY) {
			return;
		}
		int next = WoodenCauldronBlockEntity.levelOf(amount) + 1;
		cauldron.set(new FluidStack(Fluids.WATER,
				next >= 3 ? WoodenCauldronBlockEntity.CAPACITY : next * WoodenCauldronBlockEntity.LEVEL_MB));
		level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
	}
}
