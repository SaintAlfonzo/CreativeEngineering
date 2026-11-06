package com.SaintAlfonzo.creativeequipment.shield;

import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Gives the player's real vanilla shield ({@code minecraft:shield}, folded into this feature as
 * {@link ShieldTier#IRON}) the same reduction / wear / on-block-effect tooltip lines the seven
 * dedicated tier shields get from {@code TierShieldItem#appendHoverText}. Vanilla's
 * {@code ShieldItem} has no such override for a subclass to extend - it only overrides
 * {@code appendHoverText} to add the banner-colour line, and we don't own that class - so this
 * goes through {@link ItemTooltipEvent} instead of a vanilla-class mixin. Registered on the Forge
 * bus in {@code CreativeEngineering}; fires client-side only (tooltips are never built on a
 * dedicated server), same as every other tooltip-line source in the game.
 */
public final class IronShieldTooltip {

    private IronShieldTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == Items.SHIELD) {
            ShieldTooltip.appendLines(ShieldTier.IRON, event.getFlags(), event.getToolTip());
        }
    }
}
