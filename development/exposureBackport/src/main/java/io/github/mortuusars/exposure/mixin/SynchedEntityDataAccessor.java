package io.github.mortuusars.exposure.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Backs {@link io.github.mortuusars.exposure.backport.util.Backport#setEntityData}: 1.19.2 has no
 * {@code SynchedEntityData.set(key, value, force)}.
 */
@Mixin({SynchedEntityData.class})
public interface SynchedEntityDataAccessor {
   @Invoker("getItem")
   <T> SynchedEntityData.DataItem<T> invokeGetItem(EntityDataAccessor<T> key);

   @Accessor("entity")
   Entity getEntity();

   @Accessor("isDirty")
   void setDirty(boolean dirty);
}
