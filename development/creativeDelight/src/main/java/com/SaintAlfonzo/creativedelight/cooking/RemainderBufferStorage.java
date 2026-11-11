package com.SaintAlfonzo.creativedelight.cooking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Saves the buffer of a mixin-given {@link RemainderBufferHolder} with the block entity, as an attached capability
 * provider (under {@code ForgeCaps/creativedelight:remainder_buffer}). Forge reads and writes it in
 * BlockEntity.load/saveWithoutMetadata, so no mixin into the stations' own load/save is needed. It exposes no
 * capability; automation reaches the buffer through the stations' item handlers.
 */
public final class RemainderBufferStorage {

    private static final ResourceLocation ID = new ResourceLocation(CreativeDelight.MOD_ID, "remainder_buffer");

    private RemainderBufferStorage() {}

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof RemainderBufferHolder holder) {
            event.addCapability(ID, new Provider(holder));
        }
    }

    private record Provider(RemainderBufferHolder holder) implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        // The holder is asked for its buffer only here, not in the constructor: the event fires inside the
        // BlockEntity constructor, before the mixin's fields are set up.
        @Override
        public CompoundTag serializeNBT() {
            return holder.ce$getRemainderBuffer().serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            holder.ce$getRemainderBuffer().deserializeNBT(tag);
        }
    }
}
