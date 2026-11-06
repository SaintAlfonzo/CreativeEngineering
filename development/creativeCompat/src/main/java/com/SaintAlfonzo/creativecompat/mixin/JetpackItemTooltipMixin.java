package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uwu.lopyluna.create_dd.content.items.equipment.jetpack.JetpackItem;

/**
 * Create: Dreams &amp; Desires (1.2c Beta Mid-Dev) {@code JetpackItem.appendHoverText} calls
 * {@code level.isClientSide()} unguarded, but vanilla passes a null level when it builds the
 * creative search tree ({@code Minecraft#populateSearchTree}). That happens inside
 * {@code ClientPacketListener.handleUpdateTags} on every login / {@code /reload}, so the NPE
 * aborts the rest of the handler and the client {@code TagsUpdatedEvent} never fires.
 *
 * <p>Knock-on effect: JEI waits for that event to start in the background, so instead it starts
 * synchronously the first time any screen opens ("A Screen is opening but JEI hasn't started
 * yet"), a 6-18s freeze on the first inventory open.
 *
 * <p>Fix: treat a null level as "not client side", which just skips the air/cooldown/heat lines
 * for that search-tree pass. Real tooltips (non-null level) are unchanged.
 */
@Mixin(value = JetpackItem.class, remap = false)
public class JetpackItemTooltipMixin {

    @Redirect(method = "m_7373_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/level/Level;m_5776_()Z"))
    private boolean creativecompat$nullSafeIsClientSide(Level level) {
        return level != null && level.isClientSide();
    }
}
