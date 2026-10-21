package com.SaintAlfonzo.creativeengineering.mixin;

import appeng.client.guidebook.document.interaction.ItemTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * AE2 guidebook item hovers: {@code ItemTooltip.getLines} turns the item's tooltip into plain text components,
 * so AppleSkin (and the item's own tooltip image) never get a say. Forge's
 * {@code gatherTooltipComponents} builds the same text plus every {@code GatherComponents} addition; the guide
 * screen already renders image components.
 */
@Mixin(value = ItemTooltip.class, remap = false)
public class AeGuideItemTooltipMixin {

    @Shadow
    @Final
    private ItemStack stack;

    @Inject(method = "getLines", remap = false, require = 1, at = @At("HEAD"), cancellable = true)
    private void creativeengineering$gatherWithStack(Screen screen, CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        Font font = Minecraft.getInstance().font;
        cir.setReturnValue(ForgeHooksClient.gatherTooltipComponents(
                this.stack, screen.getTooltipFromItem(this.stack), 0, screen.width, screen.height, font, font));
    }
}
