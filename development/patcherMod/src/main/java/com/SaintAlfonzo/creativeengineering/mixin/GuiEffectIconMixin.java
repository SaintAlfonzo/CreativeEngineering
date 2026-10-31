package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativeengineering.potion.EffectRules;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The HUD effect icons ({@code Gui#renderEffects}, {@code m_93028_}) skip an instance whose {@code showIcon()}
 * ({@code m_19575_}, called once in the method) is false. Effects in CEConfig {@code [effects] forceHudIcon} draw anyway
 * ({@code potions/effects #8}): Intoxication comes with every alcoholic drink as {@code visible=false}, which the
 * five-argument constructor also turns into no icon. The inventory list never checks {@code showIcon}, and
 * {@code IClientMobEffectExtensions#isVisibleInGui} (Immersive Petroleum's Anti-Fire marker) is left alone.
 */
@Mixin(Gui.class)
public abstract class GuiEffectIconMixin {

    @Redirect(method = "m_93028_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;m_19575_()Z"),
            remap = false)
    private boolean creativeengineering$forceIcon(MobEffectInstance instance) {
        return instance.showIcon() || EffectRules.forceHudIcon(instance.getEffect());
    }
}
