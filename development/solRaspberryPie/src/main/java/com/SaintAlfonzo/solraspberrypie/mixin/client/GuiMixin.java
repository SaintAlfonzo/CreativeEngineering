package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.EffectFilter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

/** HUD effect icons (9.5): flat diet effects never show; slot effects hide while the inventory widget is on. */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Redirect(method = "renderEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;"))
    private Collection<MobEffectInstance> solrp$filterHud(LocalPlayer player) {
        return EffectFilter.hud(player.getActiveEffects());
    }
}
