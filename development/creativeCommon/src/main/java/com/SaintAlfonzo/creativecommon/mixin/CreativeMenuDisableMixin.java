package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code CreativeModeInventoryScreen} builds its full item-search cache (every item from every
 * loaded mod) the first time it is constructed each session - with this pack's mod count that's
 * a multi-second freeze on first "e" press in creative. {@code InventoryScreen#init}/{@code
 * #containerTick} are vanilla's only two call sites that build one ({@code new
 * CreativeModeInventoryScreen(...)}, both gated on {@code hasInfiniteItems()} - confirmed the
 * only construction sites by scanning every class in the official-mapped client jar for
 * references to the class). Redirecting that one check to always report {@code false}, scoped to
 * just these two methods, keeps creative players on the plain survival-style inventory (2x2
 * crafting grid) instead - JEI's cheat-mode panel already covers item browsing/giving without it.
 * Only while {@code [creative_menu] disabled} is true (default false, vanilla; the pack sets true).
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} - same idiom as
 * {@link LivingEntityShieldMixin} (no refmap; the pack only runs reobfuscated).
 */
@Mixin(value = InventoryScreen.class, remap = false)
public class CreativeMenuDisableMixin {

    @Redirect(
            method = {"m_7856_", "m_181908_"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;m_105290_()Z")
    )
    private boolean creativecommon$neverOpenCreativeTabs(MultiPlayerGameMode gameMode) {
        // [creative_menu] disabled (default false = vanilla; the pack sets true)
        return !CommonConfig.CREATIVE_MENU.disabled.get() && gameMode.hasInfiniteItems();
    }
}
