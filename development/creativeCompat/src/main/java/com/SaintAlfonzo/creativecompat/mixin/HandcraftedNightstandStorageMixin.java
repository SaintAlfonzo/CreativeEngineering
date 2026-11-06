package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.NightstandStorage;
import com.SaintAlfonzo.creativecompat.compat.NightstandStorage.NightstandContainer;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

/**
 * Handcrafted 2.0.6 nightstand block entities (and Every Compat's subclass) hold a chest's storage
 * ({@link NightstandStorage}).
 */
@Pseudo
@Mixin(targets = "earth.terrarium.handcrafted.common.block.table.nightstand.NightstandBlockEntity", remap = false)
public abstract class HandcraftedNightstandStorageMixin implements NightstandStorage.Holder {

    @Unique
    private NightstandContainer ce$storage;

    @Override
    public NightstandContainer ce$nightstandStorage() {
        if (ce$storage == null) {
            ce$storage = new NightstandContainer((BlockEntity) (Object) this);
        }
        return ce$storage;
    }
}
