package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import com.SaintAlfonzo.creativecompat.corpse.LooseAdditionalItems;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Additional Items without the corpse's section stacks (corpse/CorpseSections; feature_writeup/plan_corpses.md 3.6
 * item 3). On the server the container's {@code inventory} (the Corpse mod's view of the whole additional-items list)
 * is swapped at {@code <init>} TAIL for a filtered view over the stacks the sections do not show, and the page is
 * rebuilt with {@code setSlots(0)}, so the pages, Transfer Items and Corpse Curios Compat all see the filtered list.
 * The client container keeps the mod's own (it holds placeholder stacks only).
 * <p>{@code setSlots} HEAD trims the menu's {@code lastSlots} and {@code remoteSlots} ({@link CorpseSections#trimSynced}): the mod clears only
 * {@code slots}, so each call grew them by 90 and the next full sync (opening the menu) sent 180 stacks to a 90-slot
 * client menu, IndexOutOfBounds 90 on every content packet (creative-compat-publish #20; page switches too).
 * The filtered view is corpse/LooseAdditionalItems, outside the mixin package (a nested class here threw
 * IllegalClassLoadError on first use; agent note mixin-package-helpers). Applied only with the Corpse mod
 * (CompatMixinPlugin). remap = false.
 */
@Mixin(value = CorpseAdditionalContainer.class, remap = false)
public abstract class CorpseAdditionalContainerMixin extends CorpseContainerBase {

	@Shadow
	public abstract void setSlots(int start);

	private CorpseAdditionalContainerMixin(MenuType<?> type, int id, Inventory inv, CorpseEntity corpse, boolean editable, boolean history) {
		super(type, id, inv, corpse, editable, history);
	}

	@Inject(method = "setSlots", at = @At("HEAD"), remap = false)
	private void creativecompat$trimSynced(int start, CallbackInfo ci) {
		CorpseSections.trimSynced(this);
	}

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativecompat$filter(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history,
			CallbackInfo ci) {
		if (corpse.level.isClientSide) return;
		int cap = CorpseSections.capOf(corpse);
		if (cap < 0) return;
		this.inventory = new LooseAdditionalItems(corpse.getDeath().getAdditionalItems(), Math.min(cap, CorpseSections.MAX_CAP));
		setSlots(0);
	}
}
