package net.satisfy.foundation.wood;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

/**
 * Port: the wood of a {@link WoodBoat} or {@link WoodChestBoat}. Upstream's chest boat extends its boat; here it
 * extends vanilla's {@code ChestBoat} for the container code, so both share this interface instead.
 */
public interface WoodBoatEntity {
    EntityDataAccessor<String> woodAccessor();

    default Entity self() {
        return (Entity) this;
    }

    default BoatWood getWood() {
        SynchedEntityData data = self().getEntityData();
        ResourceLocation id = ResourceLocation.tryParse(data.get(woodAccessor()));
        BoatWood wood = id == null ? null : BoatWood.get(id);
        if (wood != null) {
            return wood;
        }
        List<BoatWood> own = BoatWood.byNamespace(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(self().getType())).getNamespace());
        return own.isEmpty() ? BoatWood.all().get(0) : own.get(0);
    }

    default void setWood(BoatWood wood) {
        self().getEntityData().set(woodAccessor(), wood.id().toString());
    }

    default void readWood(CompoundTag tag) {
        if (tag.contains("Type", 8)) {
            String name = tag.getString("Type");
            String namespace = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(self().getType())).getNamespace();
            ResourceLocation id = name.contains(":") ? ResourceLocation.tryParse(name) : ResourceLocation.tryParse(namespace + ":" + name);
            BoatWood wood = id == null ? null : BoatWood.get(id);
            if (wood != null) {
                setWood(wood);
            }
        }
    }

    default void writeWood(CompoundTag tag) {
        tag.putString("Type", getWood().id().toString());
    }
}
