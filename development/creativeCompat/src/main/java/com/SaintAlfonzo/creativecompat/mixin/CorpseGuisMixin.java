package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.Guis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The corpse screen's Additional Items button (feature_writeup/plan_corpses.md 3.6 item 3): the server writes "no
 * additional items" into the open-screen buffer when every additional stack is shown in a section, so the client
 * shows no button for them. Target: the buffer writer lambda of {@code Guis.openCorpseGUI}. With an empty main
 * inventory the mod opens Additional Items directly; when a section holds a stack the sectioned main screen opens
 * instead, or the sections could not be reached (creative-compat-publish #20). Applied only with the Corpse mod
 * (CompatMixinPlugin). remap = false.
 */
@Mixin(value = Guis.class, remap = false)
public abstract class CorpseGuisMixin {

	@Redirect(method = "lambda$openCorpseGUI$1", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/entities/CorpseEntity;isAdditionalInventoryEmpty()Z"), remap = false)
	private static boolean creativecompat$looseEmpty(CorpseEntity corpse) {
		return CorpseSections.looseEmpty(corpse);
	}

	@Redirect(method = "openCorpseGUI(Lnet/minecraft/server/level/ServerPlayer;Lde/maxhenkel/corpse/entities/CorpseEntity;ZZ)V",
			at = @At(value = "INVOKE", target = "Lde/maxhenkel/corpse/entities/CorpseEntity;isMainInventoryEmpty()Z"), remap = false)
	private static boolean creativecompat$mainOrSections(CorpseEntity corpse) {
		return corpse.isMainInventoryEmpty() && !CorpseSections.sectionsHold(corpse);
	}
}
