package com.SaintAlfonzo.creativeengineering.mixin;

import net.blay09.mods.trashslot.TrashSlotSaveState;
import net.blay09.mods.trashslot.client.ContainerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** TrashSlot's per-screen settings map, for {@code compat.TrashSlotCompat}. */
@Mixin(value = TrashSlotSaveState.class, remap = false)
public interface TrashSlotSaveStateAccessor {
    @Accessor("cachedSettings")
    Map<String, ContainerSettings> creativeengineering$getCachedSettings();
}
