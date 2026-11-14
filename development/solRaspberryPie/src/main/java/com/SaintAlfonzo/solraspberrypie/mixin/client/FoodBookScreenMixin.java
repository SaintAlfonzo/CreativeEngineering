package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.SaintAlfonzo.solraspberrypie.client.EffectScreen;
import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Rectangle;

/**
 * The Diet Log (6.3, 12.4): a button that opens the effect screen; Raspberry Pie's ladder pages (progress bar and
 * milestones) after Apple Pie's diversity page; Apple Pie's own benefit pages removed; the diversity page's activation
 * count read from Raspberry Pie's gate counter (unique foods when configured); and the experimental double page view,
 * which moves the book left by half a page and draws a second book on the right.
 */
@Mixin(value = FoodBookScreen.class, remap = false)
public abstract class FoodBookScreenMixin extends Screen {
    @Unique
    private UIElement solrp$companion;

    protected FoodBookScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "initPages", at = @At(value = "INVOKE", target = "Lcom/tarinoita/solsweetpotato/tracking/FoodList;getFoodsEaten()I"))
    private int solrp$gateCount(FoodList list) {
        return GateFoods.count(list);
    }

    @Redirect(method = "m_7856_", at = @At(value = "INVOKE", target = "Lcom/tarinoita/solsweetpotato/client/gui/elements/UIImage;setCenterX(I)V"))
    private void solrp$placeBook(UIImage background, int centerX) {
        background.setCenterX(DietLogPages.doublePage(this.width) ? centerX - (DietLogPages.BOOK_W + DietLogPages.GAP) / 2 : centerX);
    }

    @Inject(method = "initPages", at = @At("RETURN"))
    private void solrp$addLadderPages(CallbackInfo ci) {
        solrp$companion = DietLogPages.insert((FoodBookScreen) (Object) this, this.width);
    }

    @Inject(method = "m_7856_", at = @At("TAIL"))
    private void solrp$addButton(CallbackInfo ci) {
        int w = 110;
        Rectangle book = ((FoodBookScreenAccessor) (Object) this).solrp$background().frame;
        int cx = DietLogPages.doublePage(this.width) ? book.x + book.width + DietLogPages.GAP / 2 : book.x + book.width / 2;
        this.addRenderableWidget(new Button(cx - w / 2, this.height / 2 + 98, w, 20,
                Component.translatable("solraspberrypie.dietlog.button"), b -> EffectScreen.open()));
    }

    @Inject(method = "m_6305_", at = @At("TAIL"))
    private void solrp$renderCompanion(PoseStack pose, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!DietLogPages.doublePage(this.width)) return;
        DietLogPages.renderCompanion(pose, solrp$companion, ((FoodBookScreenAccessor) (Object) this).solrp$background().frame, mouseX, mouseY);
    }
}
