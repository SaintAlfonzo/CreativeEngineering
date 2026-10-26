package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderSlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

/**
 * Farmer's Delight cooking pot menu: the two {@link RemainderSlot}s for the station's leftover buffer (CookingPotRemainderMixin), added after the
 * player inventory so the menu's own slot numbers (9-44 = player inventory) don't move. Shift-click moves
 * them to the player inventory; nothing can be put in. quickMoveStack is named twice: SRG for the pack, official
 * for dev runs.
 */
@Mixin(value = CookingPotMenu.class, remap = false)
public abstract class CookingPotMenuRemainderMixin extends AbstractContainerMenu {

    private static final int PLAYER_INV_START = 9;
    private static final int PLAYER_INV_END = 45;

    @Unique
    private int ce$firstRemainderSlot = -1;

    private CookingPotMenuRemainderMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lvectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity;Lnet/minecraft/world/inventory/ContainerData;)V",
            at = @At("TAIL"))
    private void ce$addRemainderSlots(int windowId, Inventory playerInventory, CookingPotBlockEntity station, ContainerData data, CallbackInfo ci) {
        if (!(station instanceof RemainderBufferHolder holder)) return;
        RemainderBuffer buffer = holder.ce$getRemainderBuffer();
        ce$firstRemainderSlot = slots.size();
        for (int i = 0; i < buffer.getSlots(); ++i) {
            addSlot(new RemainderSlot(buffer, i));
        }
    }

    @Inject(method = {"m_7648_", "quickMoveStack"}, at = @At("HEAD"), cancellable = true, require = 1)
    private void ce$quickMoveRemainder(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (ce$firstRemainderSlot < 0 || index < ce$firstRemainderSlot) return;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack stack = slot.getItem().copy();
        ItemStack copy = stack.copy();
        if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true) || stack.getCount() == copy.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        slot.set(stack);
        cir.setReturnValue(copy);
    }
}
