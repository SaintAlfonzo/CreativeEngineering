// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/MetronomeDataContainer.kt. Translated
// to Java for Forge 1.19.2: metronomeEnergy -> MetronomeData.get().
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.common.data.MetronomeData;
import net.minecraft.world.inventory.ContainerData;

import java.util.UUID;

public class MetronomeDataContainer implements ContainerData {
    public final UUID owner;

    public MetronomeDataContainer(UUID owner) {
        this.owner = owner;
    }

    @Override
    public int get(int index) {
        MetronomeData data = MetronomeData.get();
        return switch (index) {
            case 0 -> data == null ? 0 : data.getStored(owner);
            case 1 -> data == null ? 0 : data.getCapacity(owner);
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return 2;
    }
}
