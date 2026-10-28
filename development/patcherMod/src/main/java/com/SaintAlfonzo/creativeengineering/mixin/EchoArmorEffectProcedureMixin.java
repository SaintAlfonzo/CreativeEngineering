package com.SaintAlfonzo.creativeengineering.mixin;

import create_deep_dark.procedures.EchoArmorEffectProcedure;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Create: Deep Dark's echo armor grants a hidden full-set bonus of Resistance II +
 * Strength II, reapplied every tick by {@code EchoArmorEffectProcedure.execute} (an
 * MCreator procedure — no config). This rewrites that pair to Night Vision +
 * {@code better_beacons:phantom_bane}.
 *
 * <p>Both {@code addEffect} calls only run after the procedure has confirmed all four
 * echo pieces are worn, so a single {@link ModifyArg} on the {@code MobEffectInstance}
 * argument is enough — no need to re-check the set here. Night Vision's duration is
 * stretched past the client "flashing" threshold (~200 ticks) because the procedure
 * otherwise keeps resetting it to 60. If {@code better_beacons} is not installed the
 * phantom-bane lookup is null and that slot is left as the original Strength effect.
 *
 * <p>Both are level I (amplifier 0): neither effect does anything with a level (Better Beacons only tests
 * {@code hasEffect}), and the procedure's amplifier 1 showed them as "II" (armor_effects.md F7).
 *
 * <p>{@code EchoArmorEffectProcedure} is an unobfuscated mod class and this project ships
 * no Mixin refmap, so {@code remap = false} throughout and {@code addEffect} is matched by
 * its SRG name {@code m_7292_}; the Mojmap references in the handler body are mapped back
 * by the reobf step.
 */
@Mixin(value = EchoArmorEffectProcedure.class, remap = false)
public class EchoArmorEffectProcedureMixin {

    private static final ResourceLocation PHANTOM_BANE =
            new ResourceLocation("better_beacons", "phantom_bane");

    /** Night Vision strobes client-side while its remaining time is under ~200 ticks. */
    private static final int NIGHT_VISION_TICKS = 300;

    @ModifyArg(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_7292_(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    remap = false
            ),
            remap = false
    )
    private static MobEffectInstance creativeengineering$swapEchoSetBonus(MobEffectInstance original) {
        MobEffect from = original.getEffect();

        if (from == MobEffects.DAMAGE_RESISTANCE) {                 // Resistance -> Night Vision
            return new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    Math.max(original.getDuration(), NIGHT_VISION_TICKS),
                    0, original.isAmbient(), original.isVisible());
        }

        if (from == MobEffects.DAMAGE_BOOST) {                      // Strength -> Phantom Bane
            MobEffect phantomBane = ForgeRegistries.MOB_EFFECTS.getValue(PHANTOM_BANE);
            if (phantomBane != null) {
                return new MobEffectInstance(
                        phantomBane, original.getDuration(),
                        0, original.isAmbient(), original.isVisible());
            }
        }

        return original;
    }
}
