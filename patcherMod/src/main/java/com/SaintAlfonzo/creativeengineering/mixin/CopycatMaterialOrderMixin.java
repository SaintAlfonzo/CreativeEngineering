package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.function.Function;

import com.SaintAlfonzo.creativeengineering.surface.LastInsertedMaterial;
import com.copycatsplus.copycats.foundation.copycat.multistate.MaterialItemStorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Remembers which part of a Copycats+ multi-state copycat received a material most recently, for
 * {@code surface.CamoSpeed} ("walks like its newest camo"). {@code storeMaterialItem} is where
 * {@code setMaterial} lands; only a real (custom) material counts, so removing one with a wrench
 * doesn't. {@code remapStorage} renames parts when a contraption rotates the block.
 *
 * <p>Persisted as a flag inside the newest part's own entry, not as a key of its own:
 * {@code deserialize} turns every top-level key into a part, so an extra key would become a
 * phantom part. {@code MaterialItem.deserialize} ignores unknown fields, so the flag is harmless
 * if patcherMod is ever removed. Serialized data is used for both saving and client sync.
 */
@Mixin(value = MaterialItemStorage.class, remap = false)
public abstract class CopycatMaterialOrderMixin implements LastInsertedMaterial {

    @Unique
    private static final String CREATIVEENGINEERING$FLAG = "creativeengineering:newest";

    @Unique
    private String creativeengineering$lastProperty;

    @Override
    public String creativeengineering$lastProperty() {
        return this.creativeengineering$lastProperty;
    }

    @Inject(method = "storeMaterialItem", at = @At("RETURN"))
    private void creativeengineering$recordInsert(String property, MaterialItemStorage.MaterialItem item, CallbackInfo ci) {
        if (((MaterialItemStorage) (Object) this).hasCustomMaterial(property)) {
            this.creativeengineering$lastProperty = property;
        }
    }

    @Inject(method = "remapStorage", at = @At("RETURN"))
    private void creativeengineering$remap(Function<String, String> remap, CallbackInfo ci) {
        if (this.creativeengineering$lastProperty != null) {
            this.creativeengineering$lastProperty = remap.apply(this.creativeengineering$lastProperty);
        }
    }

    @Inject(method = {"serialize", "serializeSafe"}, at = @At("RETURN"))
    private void creativeengineering$write(CallbackInfoReturnable<CompoundTag> cir) {
        String last = this.creativeengineering$lastProperty;
        CompoundTag tag = cir.getReturnValue();
        if (last != null && tag.contains(last, Tag.TAG_COMPOUND)) {
            tag.getCompound(last).putBoolean(CREATIVEENGINEERING$FLAG, true);
        }
    }

    @Inject(method = "deserialize", at = @At("HEAD"))
    private void creativeengineering$read(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
        for (String part : tag.getAllKeys()) {
            if (tag.getCompound(part).getBoolean(CREATIVEENGINEERING$FLAG)) {
                this.creativeengineering$lastProperty = part;
                return;
            }
        }
    }
}
