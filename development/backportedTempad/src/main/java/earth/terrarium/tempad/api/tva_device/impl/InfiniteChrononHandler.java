// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/InfiniteChrononHandler.kt. Translated to Java for
// Forge 1.19.2.
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;

public final class InfiniteChrononHandler implements ChrononHandler {
    public static final InfiniteChrononHandler INSTANCE = new InfiniteChrononHandler();

    private InfiniteChrononHandler() {
    }

    @Override
    public int getPower() {
        return -1;
    }

    @Override
    public int getMaxPower() {
        return -1;
    }

    @Override
    public boolean canInsert() {
        return false;
    }

    @Override
    public int extract(int amount, ActionType action) {
        return amount;
    }

    @Override
    public int insert(int amount, ActionType action) {
        return 0;
    }
}
