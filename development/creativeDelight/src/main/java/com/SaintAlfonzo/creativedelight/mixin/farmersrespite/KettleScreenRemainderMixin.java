package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.cooking.client.RemainderSlotFrames;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.client.gui.KettleScreen;
import umpaz.farmersrespite.common.block.entity.container.KettleMenu;

/**
 * Farmer's Respite kettle screen: frames for the leftover buffer slots (KettleMenuRemainderMixin), cut from the container slot frame of
 * the station's own texture. renderBg is named twice: SRG for the pack, official for dev runs.
 */
@Mixin(value = KettleScreen.class, remap = false)
public abstract class KettleScreenRemainderMixin extends AbstractContainerScreen<KettleMenu> {

    @Unique
    private static final ResourceLocation CE_TEXTURE = new ResourceLocation("farmersrespite", "textures/gui/kettle.png");

    private KettleScreenRemainderMixin(KettleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = {"m_7286_", "renderBg"}, at = @At("TAIL"), require = 1)
    private void ce$drawRemainderSlots(PoseStack poseStack, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        RemainderSlotFrames.draw(poseStack, CE_TEXTURE, leftPos, topPos, 85, 54);
    }
}
