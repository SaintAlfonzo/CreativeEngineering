package com.SaintAlfonzo.exposureextras.camerabag;

import java.util.function.Supplier;

import com.SaintAlfonzo.exposureextras.ExtrasConfig;

/** One camera bag item per tier; slot counts come from ExtrasConfig [camera_bag]. */
public enum CameraBagTier {
    LEATHER("leather", false, () -> ExtrasConfig.CAMERA_BAG.leatherSlots.get()),
    IRON("iron", false, () -> ExtrasConfig.CAMERA_BAG.ironSlots.get()),
    NETHERITE("netherite", true, () -> ExtrasConfig.CAMERA_BAG.netheriteSlots.get());

    private final String id;
    private final boolean fireproof;
    private final Supplier<Integer> slots;

    CameraBagTier(String id, boolean fireproof, Supplier<Integer> slots) {
        this.id = id;
        this.fireproof = fireproof;
        this.slots = slots;
    }

    /** Registry path: {@code exposureextras:<id>_camera_bag}. */
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
