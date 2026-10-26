package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.cooking.client.RemainderSlotFrames;
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
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

/**
 * Farmer's Delight cooking pot screen: frames for the leftover buffer slots (CookingPotMenuRemainderMixin), cut from the container slot frame of
 * the station's own texture. renderBg is named twice: SRG for the pack, official for dev runs.
 */
@Mixin(value = CookingPotScreen.class, remap = false)
public abstract class CookingPotScreenRemainderMixin extends AbstractContainerScreen<CookingPotMenu> {

    @Unique
    private static final ResourceLocation CE_TEXTURE = new ResourceLocation("farmersdelight", "textures/gui/cooking_pot.png");

    private CookingPotScreenRemainderMixin(CookingPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = {"m_7286_", "renderBg"}, at = @At("TAIL"), require = 1)
    private void ce$drawRemainderSlots(PoseStack poseStack, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        RemainderSlotFrames.draw(poseStack, CE_TEXTURE, leftPos, topPos, 91, 54);
    }
}
