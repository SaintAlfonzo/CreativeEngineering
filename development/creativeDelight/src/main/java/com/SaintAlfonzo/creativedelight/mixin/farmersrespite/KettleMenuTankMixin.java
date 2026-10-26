package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBoxSlot;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;

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
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.block.entity.container.KettleMenu;

/**
 * Farmer's Respite Kettle menu: the fill and empty boxes (farmers_respite.md K4, {@link KettleBoxSlot}), added after
 * the player inventory so the menu's own slot numbers don't move. Shift-click from the inventory puts a pourable
 * drink in the fill box (and latches it); glass bottles keep 2.0's way (the plating slot). Shift-click on a box moves
 * it to the inventory. quickMoveStack is named twice: SRG for the pack, official for dev runs.
 */
@Mixin(value = KettleMenu.class, remap = false)
public abstract class KettleMenuTankMixin extends AbstractContainerMenu {

	@Unique private static final int CE_INV_START = 5;
	@Unique private static final int CE_INV_END = 41;

	@Unique private int ce$firstBox = -1;

	private KettleMenuTankMixin(MenuType<?> type, int containerId) {
		super(type, containerId);
	}

	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lumpaz/farmersrespite/common/block/entity/KettleBlockEntity;Lnet/minecraft/world/inventory/ContainerData;)V",
			at = @At("TAIL"))
	private void ce$addBoxes(int windowId, Inventory playerInventory, KettleBlockEntity kettle, ContainerData data, CallbackInfo ci) {
		if (!(kettle instanceof KettleTankHolder)) return;
		ce$firstBox = slots.size();
		addSlot(new KettleBoxSlot(kettle, KettleTankHolder.FILL_BOX));
		addSlot(new KettleBoxSlot(kettle, KettleTankHolder.EMPTY_BOX));
	}

	@Inject(method = {"m_7648_", "quickMoveStack"}, at = @At("HEAD"), cancellable = true, require = 1)
	private void ce$quickMoveBoxes(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (ce$firstBox < 0 || index < 0 || index >= slots.size()) return;
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return;
		boolean fromBox = index == ce$firstBox || index == ce$firstBox + 1;
		boolean fromInv = index >= CE_INV_START && index < CE_INV_END;
		if (!fromBox && !fromInv) return;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (fromBox) {
			if (!moveItemStackTo(stack, CE_INV_START, CE_INV_END, true)) {
				cir.setReturnValue(ItemStack.EMPTY);
				return;
			}
		} else {
			KettleBoxSlot fill = (KettleBoxSlot) slots.get(ce$firstBox);
			if (!fill.mayPlace(stack)) return;
			if (!moveItemStackTo(stack, ce$firstBox, ce$firstBox + 1, false)) return;
			fill.latch();
		}
		if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}
		slot.onTake(player, stack);
		cir.setReturnValue(copy);
	}
}
