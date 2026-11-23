// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.fats;

import com.SaintAlfonzo.creativedelight.CDConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Whale Oil Lamp's oil tank (dairy-fats-and-batter #3, owner values ruling F3; config {@code [whale_oil_lamp]},
 * {@link CDConfig.Lamp}). A new lamp starts full; while lit it uses {@code burnMb} every {@code burnTicks} and goes out
 * when the tank is empty. The oil is saved as {@code Oil} (mB) and carried to the item by the loot table
 * ({@code copy_nbt} -> {@code BlockEntityTag.Oil}), so a broken lamp keeps it; an item without the tag is a crafted,
 * full lamp.
 */
public class WhaleOilLampBlockEntity extends BlockEntity {

	public static final String OIL = "Oil";

	private int oil;
	private int burnTimer;

	public WhaleOilLampBlockEntity(BlockPos pos, BlockState state) {
		super(WhaleOilLamp.BLOCK_ENTITY.get(), pos, state);
		oil = CDConfig.LAMP.tankMb();
	}

	public int oil() {
		return oil;
	}

	public int space() {
		return Math.max(0, CDConfig.LAMP.tankMb() - oil);
	}

	/** Adds up to {@code mb}; returns what went in. */
	public int fill(int mb) {
		int added = Math.min(mb, space());
		if (added > 0) {
			oil += added;
			setChanged();
		}
		return added;
	}

	/** Oil stored on a lamp item: {@code BlockEntityTag.Oil}, or a full tank when the item has none (crafted). */
	public static int oilOf(ItemStack stack) {
		CompoundTag tag = BlockItemTag.get(stack);
		return tag != null && tag.contains(OIL) ? tag.getInt(OIL) : CDConfig.LAMP.tankMb();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, WhaleOilLampBlockEntity be) {
		if (!state.getValue(WhaleOilLampBlock.LIT)) return;
		if (++be.burnTimer < CDConfig.LAMP.burnTicks()) return;
		be.burnTimer = 0;
		be.oil = Math.max(0, be.oil - CDConfig.LAMP.burnMb());
		be.setChanged();
		if (be.oil <= 0) level.setBlock(pos, state.setValue(WhaleOilLampBlock.LIT, false), Block.UPDATE_ALL);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains(OIL)) oil = Math.max(0, tag.getInt(OIL));
		burnTimer = tag.getInt("BurnTimer");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt(OIL, oil);
		tag.putInt("BurnTimer", burnTimer);
	}

	/** {@code BlockEntityTag} of an item stack, or null. */
	static final class BlockItemTag {
		static CompoundTag get(ItemStack stack) {
			CompoundTag tag = stack.getTag();
			return tag != null && tag.contains("BlockEntityTag") ? tag.getCompound("BlockEntityTag") : null;
		}
	}
}
