package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSectionsClient;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corpse.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.gui.CorpseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The sectioned corpse screen, drawing half (corpse/CorpseSectionsClient; feature_writeup/plan_corpses.md 3.6 item
 * 5). The Corpse screen inherits its background draw from the mod's {@code ScreenBase.m_7286_} (renderBg); at its
 * TAIL, for the corpse inventory screen only, the cosmetic and offhand slot frames and the curio panel are drawn.
 * Applied only with the Corpse mod (CompatMixinPlugin). remap = false.
 */
@Mixin(value = ScreenBase.class, remap = false)
public abstract class CorpseScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {

	private CorpseScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Inject(method = "m_7286_", at = @At("TAIL"), remap = false)
	private void creativecompat$sections(PoseStack pose, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		if (!((Object) this instanceof CorpseInventoryScreen)) return;
		CorpseSectionsClient.drawBackground(pose, this.leftPos, this.topPos, this.menu);
	}
}
