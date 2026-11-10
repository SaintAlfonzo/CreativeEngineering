package com.SaintAlfonzo.creativedelight.hearth.mixin;

import com.SaintAlfonzo.creativedelight.hearth.event.ManureDrops;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A sheep that finishes grazing schedules a manure drop (1.4.1 SheepGrassManureMixin). Forge has no event for it. */
@Mixin(Sheep.class)
public abstract class SheepAteMixin {
    @Inject(method = "m_8035_", at = @At("HEAD"), remap = false)
    private void hearthandharvest$onAte(CallbackInfo ci) {
        ManureDrops.schedule((Sheep) (Object) this);
    }
}
