package com.SaintAlfonzo.creativemagic.mixin;

import com.SaintAlfonzo.creativemagic.compat.TrinketCaseRules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.client.gui.box.BaubleBoxContainer;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.quark.api.IQuarkButtonAllowed;

/**
 * The Trinket Case menu. Shift-click into the case uses {@link TrinketCaseRules}, and the menu is
 * marked {@link IQuarkButtonAllowed}: with only 24 case slots it is under Quark's 27-slot cutoff,
 * so Quark would otherwise skip its sort / transfer buttons and search bar. The screen class must
 * also be in Quark's "Allowed Screens" config (keybind handlers check only that list).
 */
@Mixin(value = BaubleBoxContainer.class, remap = false)
public abstract class TrinketCaseMenuMixin implements IQuarkButtonAllowed {
    @Shadow(remap = false) @Final private ItemStack box;


    @Redirect(method = "m_7648_", remap = false, at = @At(value = "INVOKE",
            target = "Lvazkii/botania/common/handler/EquipmentHandler;isAccessory(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean creativeengineering$anyItem(EquipmentHandler handler, ItemStack stack) {
        return stack != box && (TrinketCaseRules.allowed(stack) || handler.isAccessory(stack));
    }
}
