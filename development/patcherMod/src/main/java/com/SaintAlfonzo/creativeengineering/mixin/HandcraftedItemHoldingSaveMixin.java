package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.NightstandStorage;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Saves and loads a nightstand's chest storage ({@link NightstandStorage}) with Handcrafted 2.0.6's
 * ItemHoldingBlockEntity, the nightstand's parent, which declares saveAdditional and load (no super calls). Other
 * item-holding block entities are not {@link NightstandStorage.Holder}s and are left alone. SRG names with
 * {@code remap = false}: m_183515_ saveAdditional, m_142466_ load.
 */
@Pseudo
@Mixin(targets = "earth.terrarium.handcrafted.common.block.ItemHoldingBlockEntity", remap = false)
public abstract class HandcraftedItemHoldingSaveMixin {

    @Inject(method = "m_183515_", at = @At("TAIL"))
    private void ce$saveNightstand(CompoundTag tag, CallbackInfo ci) {
        NightstandStorage.save(this, tag);
    }

    @Inject(method = "m_142466_", at = @At("TAIL"))
    private void ce$loadNightstand(CompoundTag tag, CallbackInfo ci) {
        NightstandStorage.load(this, tag);
    }
}
