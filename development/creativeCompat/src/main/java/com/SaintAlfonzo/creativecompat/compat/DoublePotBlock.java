// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

/** Two flower pots in one block ({@link DoublePot}). */
public class DoublePotBlock extends Block implements EntityBlock {

	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(2, 0, 2, 8, 6, 8),
			Block.box(8, 0, 8, 14, 6 + DoublePot.TALL_RISE_PX, 14));

	public DoublePotBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DoublePotBlockEntity(pos, state);
	}

	/** Vanilla FlowerPotBlock.use on the clicked pot; a click that changes nothing passes (blocks place against it). */
	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof DoublePotBlockEntity pot)) return InteractionResult.PASS;
		boolean b = DoublePot.hitsPotB(pos, hit.getLocation());
		ItemStack held = player.getItemInHand(hand);
		ResourceLocation heldPlant = held.getItem() instanceof BlockItem item
				? ForgeRegistries.BLOCKS.getKey(item.getBlock()) : null;
		boolean pottable = BotanistPot.pottedFor(heldPlant) != null;
		ResourceLocation current = pot.plant(b);
		if (current == null) {
			if (!pottable) return InteractionResult.PASS;
			if (!level.isClientSide) {
				pot.setPlant(b, heldPlant);
				player.awardStat(Stats.POT_FLOWER);
				if (!player.getAbilities().instabuild) held.shrink(1);
			}
		} else {
			if (pottable) return InteractionResult.PASS;
			if (!level.isClientSide) {
				ItemStack out = plantStack(current);
				if (held.isEmpty()) player.setItemInHand(hand, out);
				else if (!player.addItem(out)) player.drop(out, false);
				pot.setPlant(b, null);
			}
		}
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	/** The two pots come from the loot table; the plants drop here (mined, exploded, replaced). */
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DoublePotBlockEntity pot) {
			for (ResourceLocation plant : pot.plants()) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), plantStack(plant));
			}
		}
		super.onRemove(state, level, pos, newState, moved);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		if (target instanceof BlockHitResult hit && level.getBlockEntity(pos) instanceof DoublePotBlockEntity pot) {
			ResourceLocation plant = pot.plant(DoublePot.hitsPotB(pos, hit.getLocation()));
			if (plant != null) {
				ItemStack stack = plantStack(plant);
				if (!stack.isEmpty()) return stack;
			}
		}
		return new ItemStack(Items.FLOWER_POT);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
		return false;
	}

	static ItemStack plantStack(ResourceLocation plant) {
		Block block = ForgeRegistries.BLOCKS.getValue(plant);
		return block == null ? ItemStack.EMPTY : new ItemStack(block);
	}
}
