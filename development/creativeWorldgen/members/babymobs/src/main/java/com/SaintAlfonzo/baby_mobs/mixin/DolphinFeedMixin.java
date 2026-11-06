package com.SaintAlfonzo.baby_mobs.mixin;

import com.SaintAlfonzo.baby_mobs.BMConfig;
import com.SaintAlfonzo.baby_mobs.age.BabyAges;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1 {@code Dolphin.mobInteract}: a fish fed to a baby dolphin ages it up by 10% of the time left
 * ({@code AgeableMob.getSpeedUpSecondsWhenFeeding}) instead of setting the treasure-hunt flag. m_6071_ mobInteract.
 */
@Mixin(Dolphin.class)
public abstract class DolphinFeedMixin {

    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$feedBaby(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Dolphin self = (Dolphin) (Object) this;
        if (!BMConfig.DOLPHIN_FISH_FEEDING.get() || !BabyAges.isBaby(self)) return;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !stack.is(ItemTags.FISHES)) return;
        if (!self.level.isClientSide) {
            self.playSound(SoundEvents.DOLPHIN_EAT, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            BabyAges.ageUp(self, AgeableMob.getSpeedUpSecondsWhenFeeding(-BabyAges.getAge(self)));
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(self.level.isClientSide));
    }
}
