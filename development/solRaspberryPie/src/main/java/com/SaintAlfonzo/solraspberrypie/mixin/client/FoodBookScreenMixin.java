package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.SaintAlfonzo.solraspberrypie.client.EffectScreen;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Diet Log (6.3): a button that opens the effect screen, and Raspberry Pie's ladder pages (progress line and
 * milestones) after Apple Pie's diversity page, in place of Apple Pie's own (empty) benefit pages.
 */
@Mixin(value = FoodBookScreen.class, remap = false)
public abstract class FoodBookScreenMixin extends Screen {
    protected FoodBookScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "initPages", at = @At("RETURN"))
    private void solrp$addLadderPages(CallbackInfo ci) {
        DietLogPages.insert((FoodBookScreen) (Object) this);
    }

    @Inject(method = "m_7856_", at = @At("TAIL"))
    private void solrp$addButton(CallbackInfo ci) {
        int w = 110;
        this.addRenderableWidget(new Button(this.width / 2 - w / 2, this.height / 2 + 98, w, 20,
                Component.translatable("solraspberrypie.dietlog.button"), b -> EffectScreen.open()));
    }
}
