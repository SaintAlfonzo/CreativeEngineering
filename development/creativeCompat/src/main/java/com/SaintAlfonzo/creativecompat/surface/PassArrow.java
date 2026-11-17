package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Duck interface on {@code AbstractArrow} ({@code mixin/ArrowPassMixin}): the private stuck fields the web hang (plan
 * 5.3) and the despawn rule (5.4) need.
 */
public interface PassArrow {

    /** Stops the arrow where it is and puts it into vanilla's stuck state with the web as its block (5.3). */
    void creativecompat$hangInWeb();

    boolean creativecompat$inGround();

    /** The block the arrow is stuck in ({@code lastState}), or null. */
    BlockState creativecompat$lastState();

    /** The stuck counter ({@code life}). */
    int creativecompat$life();

    /** {@code getPickupItem()} (protected in the class). */
    ItemStack creativecompat$pickupItem();
}
