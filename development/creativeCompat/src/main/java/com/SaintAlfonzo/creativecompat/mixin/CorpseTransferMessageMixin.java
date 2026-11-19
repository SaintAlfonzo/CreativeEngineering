package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseCurios;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.net.MessageTransferItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Transfer Items returns each tagged curio to its own slot first (corpse/CorpseCurios; feature_writeup/plan_corpses.md
 * 3.6 item 4, ruling 32). Hooked in the Corpse mod's transfer packet just before it calls {@code transferItems}, so it
 * runs before every {@code transferItems} HEAD hook (Corpse Curios Compat, Cosmetic Armor x Corpse Compat) whatever
 * their mixin order. Applied only with the Corpse mod and Curios (CompatMixinPlugin). remap = false.
 */
@Mixin(value = MessageTransferItems.class, remap = false)
public abstract class CorpseTransferMessageMixin {

	@Inject(method = "executeServerSide", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/gui/ITransferrable;transferItems()V"), remap = false)
	private void creativecompat$curiosFirst(NetworkEvent.Context context, CallbackInfo ci) {
		ServerPlayer player = context.getSender();
		if (player == null || !(player.containerMenu instanceof CorpseInventoryContainer menu) || !menu.isEditable()) return;
		CorpseCurios.returnToSlots(player, menu.getCorpse().getDeath().getAdditionalItems());
	}
}
