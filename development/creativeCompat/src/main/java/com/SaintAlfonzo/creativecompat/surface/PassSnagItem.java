package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.world.item.ItemStack;

/**
 * Duck interface on the thrown tools of the {@code snag} group that normally fly back on their own (plan 5.1, 5.5):
 * Quark pickarangs ({@code mixin/QuarkPickarangPassMixin}) and the Botania thorn chakram
 * ({@code mixin/BotaniaChakramPassMixin}). Snagged, they are picked up like an arrow and drop as an item when the
 * web breaks ({@link PassProjectiles}).
 */
public interface PassSnagItem {

    /** The thrown tool's own stack (damage and enchantments kept). */
    ItemStack creativecompat$snagStack();

    /** Called every tick while snagged: stop the timers that would send it home. Server and client. */
    void creativecompat$holdTimers();
}
