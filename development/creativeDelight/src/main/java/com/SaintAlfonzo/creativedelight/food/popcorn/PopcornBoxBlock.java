// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.food.popcorn;

import com.SaintAlfonzo.creativedelight.food.Flavors;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.block.FeastBlock;

/**
 * A placeable box of plain or flavored popcorn (food/seasoning #3, owner ruling): Corn Delight's popcorn box (4 servings,
 * its shape and models) serving Corn Delight's popcorn, carrying {@code flavor} when one is set. Taken by hand, no
 * container; the empty box is picked up as a paper, as Corn Delight's does.
 */
public class PopcornBoxBlock extends FeastBlock {

	/** Corn Delight's popcorn box shape. */
	protected static final VoxelShape BOX_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);
	private static final ResourceLocation POPCORN = new ResourceLocation("corn_delight", "popcorn");

	private final ResourceLocation flavor;

	public PopcornBoxBlock(Properties properties, ResourceLocation flavor) {
		super(properties, () -> ForgeRegistries.ITEMS.getValue(POPCORN), true);
		this.flavor = flavor;
	}

	@Override
	public ItemStack getServingItem(BlockState state) {
		Item popcorn = ForgeRegistries.ITEMS.getValue(POPCORN);
		if (popcorn == null || popcorn == Items.AIR) return ItemStack.EMPTY;
		ItemStack stack = new ItemStack(popcorn);
		return flavor == null ? stack : Flavors.with(stack, List.of(flavor));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return BOX_SHAPE;
	}

	@Override
	protected InteractionResult takeServing(LevelAccessor level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
		int servings = state.getValue(SERVINGS);
		if (servings == 0) {
			level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
			level.removeBlock(pos, false);
			give(player, new ItemStack(Items.PAPER));
			return InteractionResult.SUCCESS;
		}
		ItemStack serving = getServingItem(state);
		if (serving.isEmpty()) return InteractionResult.PASS;
		level.setBlock(pos, state.setValue(SERVINGS, servings - 1), Block.UPDATE_ALL);
		give(player, serving);
		level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}

	private static void give(Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) player.drop(stack, false);
	}
}
