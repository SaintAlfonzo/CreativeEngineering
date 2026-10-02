package com.SaintAlfonzo.creativeengineering.camerabag;

import java.util.function.Supplier;

import com.SaintAlfonzo.creativeengineering.CEConfig;

/** One camera bag item per tier; slot counts come from CEConfig [camera_bag]. */
public enum CameraBagTier {
    LEATHER("leather", false, () -> CEConfig.CAMERA_BAG.leatherSlots.get()),
    IRON("iron", false, () -> CEConfig.CAMERA_BAG.ironSlots.get()),
    NETHERITE("netherite", true, () -> CEConfig.CAMERA_BAG.netheriteSlots.get());

    private final String id;
    private final boolean fireproof;
    private final Supplier<Integer> slots;

    CameraBagTier(String id, boolean fireproof, Supplier<Integer> slots) {
        this.id = id;
        this.fireproof = fireproof;
        this.slots = slots;
    }

    /** Registry path: {@code creativeengineering:<id>_camera_bag}. */
    public String itemId() {
        return id + "_camera_bag";
    }

    public boolean fireproof() {
        return fireproof;
    }

    public int slots() {
        return slots.get();
    }
}
