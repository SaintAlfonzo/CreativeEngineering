// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import de.maxhenkel.corpse.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.CorpseInventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The sectioned corpse screen, button half (feature_writeup/plan_corpses.md 3.6; creative-compat-publish #21). The
 * Corpse mod's {@code updateButtons} shows Transfer Items only while the main inventory holds a stack, so a corpse
 * whose only stacks sit in the cosmetic or curio sections (opened as the main screen by CorpseGuisMixin) had no
 * Transfer Items button. At its TAIL, when the main inventory is empty and a section slot holds a stack, the
 * buttons are rebuilt with Transfer Items (left of Additional Items when loose additional items remain, else
 * centered). The gate reads the corpse, not the {@code takeItems} field: updateButtons runs every container tick and
 * its all-empty branch clears the widgets without clearing {@code takeItems}, so a field check saw the previous
 * tick's button and left the screen without buttons from the second tick on (scratch r1125blk). The server's transfer already takes the whole additional list. Applied only with the Corpse mod
 * (CompatMixinPlugin). remap = false.
 */
@Mixin(value = CorpseInventoryScreen.class, remap = false)
public abstract class CorpseInventoryScreenMixin extends ScreenBase<CorpseInventoryContainer> {

	@Shadow(remap = false) private Button takeItems;
	@Shadow(remap = false) private Button additionalItems;

	@Shadow(remap = false) private Button addLeftButton(Component text, Button.OnPress press) { return null; }
	@Shadow(remap = false) private Button addRightButton(Component text, Button.OnPress press) { return null; }
	@Shadow(remap = false) private Button addCenterButton(Component text, Button.OnPress press) { return null; }

	private CorpseInventoryScreenMixin(ResourceLocation texture, CorpseInventoryContainer menu, Inventory inventory, Component title) {
		super(texture, menu, inventory, title);
	}

	@Inject(method = "updateButtons", at = @At("TAIL"), remap = false)
	private void creativecompat$sectionTransfer(CallbackInfo ci) {
		if (!this.menu.getCorpse().isMainInventoryEmpty()) return;
		if (!creativecompat$sectionsHold()) {
			if (this.menu.getCorpse().isAdditionalInventoryEmpty()) this.takeItems = null;
			return;
		}
		this.clearWidgets();
		if (!this.menu.getCorpse().isAdditionalInventoryEmpty()) {
			this.takeItems = addLeftButton(CorpseInventoryScreen.TRANSFER_ITEMS, CorpseInventoryScreen.PRESS_TRANSFER_ITEMS);
			this.additionalItems = addRightButton(CorpseInventoryScreen.ADDITIONAL_ITEMS, CorpseInventoryScreen.PRESS_ADDITIONAL_ITEMS);
		} else {
			this.takeItems = addCenterButton(CorpseInventoryScreen.TRANSFER_ITEMS, CorpseInventoryScreen.PRESS_TRANSFER_ITEMS);
			this.additionalItems = null;
		}
		this.takeItems.active = this.menu.isEditable();
	}

	private boolean creativecompat$sectionsHold() {
		for (Slot slot : this.menu.slots) {
			if (slot instanceof CorpseSections.SectionSlot && slot.hasItem()) return true;
		}
		return false;
	}
}
