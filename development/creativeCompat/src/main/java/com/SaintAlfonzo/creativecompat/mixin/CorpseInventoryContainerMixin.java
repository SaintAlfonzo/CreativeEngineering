package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseCurios;
import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The sectioned corpse screen, menu half (corpse/CorpseSections; feature_writeup/plan_corpses.md 3.6 items 2 to 4).
 * <ul>
 * <li>{@code <init>} TAIL: moves the offhand slot (menu index 4) from x 98 to the end of the top row and adds the 4
 * cosmetic slots and the curio slots after the player inventory, on both sides, sized by the corpse's synced cap.</li>
 * <li>{@code m_38946_} (broadcastChanges) and {@code transferItems}: the Additional Items auto-open checks ignore the
 * stacks shown in the sections.</li>
 * <li>{@code transferItems} RETURN: strips the section tags from whatever reached the player.</li>
 * </ul>
 * Applied only with the Corpse mod (CompatMixinPlugin). SRG names, remap = false.
 */
@Mixin(value = CorpseInventoryContainer.class, remap = false)
public abstract class CorpseInventoryContainerMixin extends CorpseContainerBase {

	private CorpseInventoryContainerMixin(MenuType<?> type, int id, Inventory inv, CorpseEntity corpse, boolean editable, boolean history) {
		super(type, id, inv, corpse, editable, history);
	}

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativecompat$sections(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history,
			CallbackInfo ci) {
		int cap = CorpseSections.capOf(corpse);
		if (cap < 0) return;
		cap = Math.min(cap, CorpseSections.MAX_CAP);
		Slot offhand = this.slots.get(4);
		Slot moved = new CorpseSections.OffhandSlot(offhand.container, offhand.getContainerSlot(), CorpseSections.OFFHAND_X,
				CorpseSections.TOP_Y, editable);
		moved.index = 4;
		this.slots.set(4, moved);

		Container view = CorpseSections.container(corpse, cap);
		for (int i = 0; i < CorpseSections.COSMETIC + cap; i++) {
			this.addSlot(new CorpseSections.SectionSlot(view, i, CorpseSections.slotX(i), CorpseSections.slotY(i), editable,
					i >= CorpseSections.COSMETIC));
		}
	}

	@Redirect(method = "m_38946_", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/entities/CorpseEntity;isAdditionalInventoryEmpty()Z"), remap = false)
	private boolean creativecompat$looseEmptyTick(CorpseEntity corpse) {
		return CorpseSections.looseEmpty(corpse);
	}

	@Redirect(method = "transferItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;isEmpty()Z"),
			remap = false)
	private boolean creativecompat$looseEmptyTransfer(NonNullList<?> list) {
		return CorpseSections.looseEmpty(this.corpse);
	}

	@Inject(method = "transferItems", at = @At("RETURN"), remap = false)
	private void creativecompat$sweep(CallbackInfo ci) {
		if (this.playerInventory == null) return;
		CorpseSections.sweep(this.playerInventory.player);
		if (CorpseSections.CURIOS) CorpseCurios.sweep(this.playerInventory.player);
	}
}
