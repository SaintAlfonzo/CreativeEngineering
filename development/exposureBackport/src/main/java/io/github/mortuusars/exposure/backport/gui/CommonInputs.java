package io.github.mortuusars.exposure.backport.gui;

/** Backport of 1.19.4+ {@code net.minecraft.client.gui.navigation.CommonInputs}. */
public final class CommonInputs {
    private CommonInputs() {
    }

    public static boolean selected(int keyCode) {
        return keyCode == 257 || keyCode == 32 || keyCode == 335;
    }
}
