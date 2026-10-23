package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.mooshroom.MooshroomFamily;
import com.SaintAlfonzo.creativeengineering.mooshroom.MushroomTypeSetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.MushroomCow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code MushroomCow.getBreedOffspring} (m_142606_, the MushroomCow-returning one; the Cow/AgeableMob bridges call it):
 * for two mooshroom-family parents the calf is 50 % each parent's kind, Fungi cows included
 * ({@link MooshroomFamily#breed}). Replaces vanilla's red/brown rule; anything else falls through to vanilla.
 *
 * <p>Also exposes the private {@code setMushroomType} (m_28928_) as {@link MushroomTypeSetter}.
 */
@Mixin(value = MushroomCow.class, remap = false)
public abstract class MushroomCowBreedingMixin implements MushroomTypeSetter {

    @Shadow(remap = false)
    private void m_28928_(MushroomCow.MushroomType type) {
        throw new AssertionError();
    }

    @Override
    public void creativeengineering$setMushroomType(MushroomCow.MushroomType type) {
        m_28928_(type);
    }

    @Inject(method = "m_142606_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/MushroomCow;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$familyCalf(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<MushroomCow> cir) {
        if (!(partner instanceof MushroomCow other)) return;
        MushroomCow calf = MooshroomFamily.breed((MushroomCow) (Object) this, other, level);
        if (calf != null) cir.setReturnValue(calf);
    }
}
