package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fruits Delight 0.5.11's placed Mangosteen Cake builds each bite effect as
 * {@code new MobEffectInstance(effect, amplifier, duration / 3)}: duration and amplifier swapped, so a bite gives a
 * 0-tick effect at a huge level (fruits_delight.md 2.1). 1.1.3 passes {@code (effect, duration / 3, amplifier)}; this
 * swaps the two back, so a bite gives a third of the item's effects (Comfort 20 s, Sliding 40 s).
 */
@Pseudo
@Mixin(targets = "dev.xkmc.fruitsdelight.content.block.MangosteenCakeBlock", remap = false)
public abstract class MangosteenCakeEffectMixin {

	@Redirect(method = "eat", remap = false, require = 1,
			at = @At(value = "NEW", remap = false,
					target = "(Lnet/minecraft/world/effect/MobEffect;II)Lnet/minecraft/world/effect/MobEffectInstance;"))
	private MobEffectInstance creativedelight$biteEffect(MobEffect effect, int amplifier, int duration) {
		return new MobEffectInstance(effect, duration, amplifier);
	}
}
