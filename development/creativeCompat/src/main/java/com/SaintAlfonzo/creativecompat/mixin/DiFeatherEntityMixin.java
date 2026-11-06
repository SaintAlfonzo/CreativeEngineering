package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.cats.CatFeather;
import com.SaintAlfonzo.creativecompat.cats.CozyCats;
import com.github.alexthe668.domesticationinnovation.server.entity.FeatherEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Domestication Innovation's Feather on a Stick as a cat command ({@link CatFeather}). DI's feather overrides
 * {@code canHitEntity} (m_5603_) to false and {@code onHitEntity} (m_5790_) to nothing, so it passes through mobs:
 * here it hits the mobs {@link CatFeather#canHook} allows and sticks to them through the fishing hook's own hooked
 * state ({@code setHookedEntity}, m_150157_). {@code tick} (m_8119_) feeds {@link CatFeather#tick}. DI's
 * {@code isPetAmbulatory} (its own name) also refuses Cozy cats in Stay mode, for the ground lure and the attack.
 */
@Mixin(value = FeatherEntity.class, remap = false)
public abstract class DiFeatherEntityMixin {

    @Inject(method = "m_5603_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativecompat$hitMobs(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (CatFeather.canHook((FeatherEntity) (Object) this, entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "m_5790_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativecompat$stickToMob(EntityHitResult result, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        if (!hook.level.isClientSide && CatFeather.canHook((FeatherEntity) (Object) this, result.getEntity())) {
            CatFeather.hookInto(hook, result.getEntity());
            ci.cancel();
        }
    }

    @Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
    private void creativecompat$featherTick(CallbackInfo ci) {
        CatFeather.tick((FeatherEntity) (Object) this);
    }

    @Inject(method = "isPetAmbulatory", at = @At("RETURN"), cancellable = true, remap = false)
    private void creativecompat$cozyStay(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && ModList.get().isLoaded("cozys_improved_cats") && CozyCats.inStay(entity)) {
            cir.setReturnValue(false);
        }
    }
}
