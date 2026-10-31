package com.SaintAlfonzo.creativeengineering.compat;

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
 * The botanist workbench's pot ({@link BotanistPot}). Holds the plant's block id, as the vanilla full-pots map keys
 * it; the {@code potted_*} block is looked up from that map each time, so a plant whose entry goes away shows nothing
 * and still comes back out as its item.
 */
public class BotanistPotBlockEntity extends BlockEntity {

    /** The potted block whose plant the client model draws; absent when the pot is empty. */
    public static final ModelProperty<Block> POTTED = new ModelProperty<>();

    @Nullable
    private ResourceLocation plant;

    public BotanistPotBlockEntity(BlockPos pos, BlockState state) {
        super(BotanistPot.TYPE.get(), pos, state);
    }

    @Nullable
    public ResourceLocation plant() {
        return plant;
    }

    void setPlant(@Nullable ResourceLocation plant) {
        this.plant = plant;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        plant = tag.contains(BotanistPot.PLANT_TAG) ? ResourceLocation.tryParse(tag.getString(BotanistPot.PLANT_TAG)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (plant != null) tag.putString(BotanistPot.PLANT_TAG, plant.toString());
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
        Block potted = BotanistPot.pottedFor(plant);
        return potted == null ? ModelData.EMPTY : ModelData.builder().with(POTTED, potted).build();
    }
}
