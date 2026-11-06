package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.surface.LastInsertedCamo;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.api.data.CamoContainer;
import xfacthd.framedblocks.common.blockentity.FramedDoubleBlockEntity;

/**
 * Remembers which half of a FramedBlocks double block got a camo most recently, for
 * {@code surface.CamoSpeed} ("walks like its newest camo"). {@code setCamo(camo, secondary)} is
 * the one path both halves are set through; clearing a half doesn't count as inserting. The flag
 * rides along in the block entity's save NBT and in both client sync paths (block update packet
 * and chunk update tag), since client movement prediction needs it too. Extra top-level key, so
 * FramedBlocks ignores it if patcherMod is ever removed.
 *
 * <p>Third-party target; MC-overridden methods by their SRG names ({@code m_183515_} saveAdditional,
 * {@code m_142466_} load, {@code m_5995_} getUpdateTag), all with {@code remap = false}.
 */
@Mixin(value = FramedDoubleBlockEntity.class, remap = false)
public abstract class FramedDoubleCamoOrderMixin implements LastInsertedCamo {

    @Unique
    private static final String CREATIVEENGINEERING$KEY = "creativeengineering:last_camo_second";

    @Unique
    private boolean creativeengineering$lastSecond;

    @Override
    public boolean creativeengineering$lastCamoIsSecond() {
        return this.creativeengineering$lastSecond;
    }

    @Inject(method = "setCamo", at = @At("HEAD"))
    private void creativeengineering$recordInsert(CamoContainer camo, boolean secondary, CallbackInfo ci) {
        if (!camo.isEmpty()) {
            this.creativeengineering$lastSecond = secondary;
        }
    }

    @Inject(method = "m_183515_", at = @At("TAIL"))
    private void creativeengineering$save(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(CREATIVEENGINEERING$KEY, this.creativeengineering$lastSecond);
    }

    @Inject(method = "m_142466_", at = @At("TAIL"))
    private void creativeengineering$load(CompoundTag tag, CallbackInfo ci) {
        this.creativeengineering$read(tag);
    }

    @Inject(method = "writeToDataPacket", at = @At("TAIL"))
    private void creativeengineering$writePacket(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(CREATIVEENGINEERING$KEY, this.creativeengineering$lastSecond);
    }

    @Inject(method = "readFromDataPacket", at = @At("RETURN"))
    private void creativeengineering$readPacket(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
        this.creativeengineering$read(tag);
    }

    @Inject(method = "m_5995_", at = @At("RETURN"))
    private void creativeengineering$writeUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean(CREATIVEENGINEERING$KEY, this.creativeengineering$lastSecond);
    }

    @Inject(method = "handleUpdateTag", at = @At("TAIL"))
    private void creativeengineering$readUpdateTag(CompoundTag tag, CallbackInfo ci) {
        this.creativeengineering$read(tag);
    }

    @Unique
    private void creativeengineering$read(CompoundTag tag) {
        if (tag.contains(CREATIVEENGINEERING$KEY)) {
            this.creativeengineering$lastSecond = tag.getBoolean(CREATIVEENGINEERING$KEY);
        }
    }
}
