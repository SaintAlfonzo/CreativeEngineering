package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.SophisticatedPolymorphCompat;
import com.illusivesoulworks.polymorph.common.network.client.CPacketPlayerRecipeSelection;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Polymorph stores a recipe picked in its selection widget it only calls
 * {@code slotsChanged} on the open menu, which a Sophisticated storage menu does not pass on to its
 * crafting upgrade. Recompute the open crafting upgrade so the picked recipe shows in its result slot.
 */
@Mixin(value = CPacketPlayerRecipeSelection.class, remap = false)
public abstract class PolymorphRecipeSelectionPacketMixin {

    @Inject(method = "handle", at = @At("TAIL"), remap = false, require = 1)
    private static void creativeengineering$refreshStorageCrafting(CPacketPlayerRecipeSelection packet, ServerPlayer player, CallbackInfo ci) {
        SophisticatedPolymorphCompat.refreshOpenCrafting(player.containerMenu);
    }
}
