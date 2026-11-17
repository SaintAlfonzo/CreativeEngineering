package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.paintings.PaintingArt;
import immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placed Immersive Paintings store their painting as the full png id ({@code Motive}). Painting items carry no motive,
 * so nothing else needs this. Painting variants are not a Forge registry here, which is why the swap lists cannot do
 * it.
 */
@Mixin(value = ImmersivePaintingEntity.class, remap = false)
public abstract class ImmersivePaintingMotiveAliasMixin {

    /** The namespace the generated paintings used before the move; a permanent read alias. */
    private static final String LEGACY_NS = "creativeengineering";

    @Shadow
    public abstract ResourceLocation getMotive();

    @Shadow
    public abstract void setMotive(ResourceLocation motive);

    @Inject(method = "m_7378_", at = @At("TAIL"), remap = false)
    private void creativecompat$moveOldMotive(CompoundTag tag, CallbackInfo ci) {
        ResourceLocation motive = getMotive();
        if (motive != null && LEGACY_NS.equals(motive.getNamespace()) && motive.getPath().startsWith("paintings/")) {
            setMotive(new ResourceLocation(PaintingArt.OUT_NAMESPACE, motive.getPath()));
        }
    }
}
