package net.satisfy.foundation.wood;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WoodBoat extends Boat implements WoodBoatEntity {
    private static final EntityDataAccessor<String> WOOD = SynchedEntityData.defineId(WoodBoat.class, EntityDataSerializers.STRING);

    public WoodBoat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    public EntityDataAccessor<String> woodAccessor() {
        return WOOD;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(WOOD, "");
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readWood(tag);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeWood(tag);
    }

    @Override
    public @NotNull Item getDropItem() {
        return getWood().item(false);
    }
}
