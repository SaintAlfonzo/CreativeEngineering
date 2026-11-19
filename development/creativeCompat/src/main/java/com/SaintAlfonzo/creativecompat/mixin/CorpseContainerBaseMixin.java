package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import de.maxhenkel.corpse.corelib.inventory.ContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shift-click on a corpse section slot (corpse/CorpseSections; feature_writeup/plan_corpses.md 3.6 item 2). The Corpse
 * mod's {@code m_7648_} (quickMoveStack, in its ContainerBase) treats every slot past the 41 corpse slots as the
 * player's and would move a section stack back into the locked corpse slots; this moves it into the player inventory
 * instead, without the section tags. Applied only with the Corpse mod (CompatMixinPlugin). remap = false.
 */
@Mixin(value = ContainerBase.class, remap = false)
public abstract class CorpseContainerBaseMixin extends AbstractContainerMenu {

	private CorpseContainerBaseMixin(MenuType<?> type, int id) {
		super(type, id);
	}

	@Inject(method = "m_7648_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecompat$quickMoveSection(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (!((Object) this instanceof CorpseInventoryContainer corpseMenu)) return;
		if (index < 0 || index >= this.slots.size()) return;
		Slot slot = this.slots.get(index);
		if (!(slot instanceof CorpseSections.SectionSlot)) return;
		cir.setReturnValue(ItemStack.EMPTY);
		ItemStack stack = slot.getItem();
		if (stack.isEmpty() || !slot.mayPickup(player)) return;
		ItemStack moving = CorpseSections.strip(stack.copy());
		int before = moving.getCount();
		int start = corpseMenu.getInventorySize();
		this.moveItemStackTo(moving, start, start + 36, true);
		int moved = before - moving.getCount();
		if (moved > 0) {
			ItemStack taken = slot.remove(moved);
			slot.onTake(player, taken);
			slot.setChanged();
		}
	}
}
