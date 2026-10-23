package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.mooshroom.MooshroomFamily;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Animal.canMate} (m_7848_) wants the same class, so a mooshroom and a Fungi Delight cow (FDMCow, a
 * MushroomCow subclass) never pair. Any two mooshroom-family members ({@link MooshroomFamily#MEMBERS}) that are both
 * in love and not the same entity can. MushroomCow declares no canMate, so this sits on Animal; other animals are
 * untouched. Only the MushroomCow parent's breed goal finds a Fungi partner (BreedGoal searches its own class).
 */
@Mixin(value = Animal.class, remap = false)
public abstract class AnimalCanMateMooshroomMixin {

    @Inject(method = "m_7848_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$mooshroomFamily(Animal other, CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (other == self || !MooshroomFamily.isMember(self) || !MooshroomFamily.isMember(other)) return;
        cir.setReturnValue(self.isInLove() && other.isInLove());
    }
}
