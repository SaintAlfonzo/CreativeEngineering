// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/MultiversalChrononHandler.kt. Translated to Java
// for Forge 1.19.2: the overworld attachment metronomeEnergy -> MetronomeData.get() (overworld SavedData).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.data.MetronomeData;

import java.util.UUID;

public class MultiversalChrononHandler implements ChrononHandler {
    public final UUID playerId;

    public MultiversalChrononHandler(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public int getPower() {
        MetronomeData data = MetronomeData.get();
        return data == null ? 0 : data.getStored(playerId);
    }

    @Override
    public int getMaxPower() {
        MetronomeData data = MetronomeData.get();
        return data == null ? 0 : data.getCapacity(playerId);
    }

    @Override
    public int extract(int amount, ActionType action) {
        int oldPower = getPower();
        int newPower = Math.max(getPower() - amount, 0);
        if (action == ActionType.Execute) {
            MetronomeData data = MetronomeData.get();
            if (data != null) data.stored.put(playerId, newPower);
        }
        return oldPower - newPower;
    }

    @Override
    public int insert(int amount, ActionType action) {
        int oldPower = getPower();
        int newPower = Math.min(getPower() + amount, getMaxPower());
        if (action == ActionType.Execute) {
            MetronomeData data = MetronomeData.get();
            if (data != null) data.stored.put(playerId, newPower);
        }
        return newPower - oldPower;
    }
}
