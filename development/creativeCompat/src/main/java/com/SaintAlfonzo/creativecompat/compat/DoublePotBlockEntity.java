// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * The two pots of a {@link DoublePotBlock}: each holds a plant block id as the vanilla full-pots map keys it (pot A
 * the first, vanilla-height pot; pot B the taller one). As in {@link BotanistPotBlockEntity}, the {@code potted_*}
 * block is looked up from the map each time.
 */
public class DoublePotBlockEntity extends BlockEntity implements PottedPlantHolder {

	/** The potted blocks whose plants the client model draws in pot A and pot B; absent when that pot is empty. */
	public static final ModelProperty<Block> POTTED_A = new ModelProperty<>();
	public static final ModelProperty<Block> POTTED_B = new ModelProperty<>();

	@Nullable
	private ResourceLocation plantA;
	@Nullable
	private ResourceLocation plantB;

	public DoublePotBlockEntity(BlockPos pos, BlockState state) {
		super(DoublePot.TYPE.get(), pos, state);
	}

	@Nullable
	public ResourceLocation plant(boolean potB) {
		return potB ? plantB : plantA;
	}

	/** The first plant (pot A, else pot B), for readers of a single-plant holder. */
	@Nullable
	@Override
	public ResourceLocation plant() {
		return plantA != null ? plantA : plantB;
	}

	@Override
	public List<ResourceLocation> plants() {
		List<ResourceLocation> out = new ArrayList<>(2);
		if (plantA != null) out.add(plantA);
		if (plantB != null) out.add(plantB);
		return out;
	}

	void setPlant(boolean potB, @Nullable ResourceLocation plant) {
		if (potB) plantB = plant;
		else plantA = plant;
		changed();
	}

	void setPlants(@Nullable ResourceLocation a, @Nullable ResourceLocation b) {
		plantA = a;
		plantB = b;
		changed();
	}

	private void changed() {
		setChanged();
		if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		plantA = tag.contains(DoublePot.PLANT_A_TAG) ? ResourceLocation.tryParse(tag.getString(DoublePot.PLANT_A_TAG)) : null;
		plantB = tag.contains(DoublePot.PLANT_B_TAG) ? ResourceLocation.tryParse(tag.getString(DoublePot.PLANT_B_TAG)) : null;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (plantA != null) tag.putString(DoublePot.PLANT_A_TAG, plantA.toString());
		if (plantB != null) tag.putString(DoublePot.PLANT_B_TAG, plantB.toString());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		load(tag);
		refreshModel();
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag tag = pkt.getTag();
		load(tag == null ? new CompoundTag() : tag);
		refreshModel();
	}

	private void refreshModel() {
		requestModelDataUpdate();
		if (level != null && level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	@Override
	public ModelData getModelData() {
		Block a = BotanistPot.pottedFor(plantA);
		Block b = BotanistPot.pottedFor(plantB);
		if (a == null && b == null) return ModelData.EMPTY;
		ModelData.Builder builder = ModelData.builder();
		if (a != null) builder.with(POTTED_A, a);
		if (b != null) builder.with(POTTED_B, b);
		return builder.build();
	}
}
