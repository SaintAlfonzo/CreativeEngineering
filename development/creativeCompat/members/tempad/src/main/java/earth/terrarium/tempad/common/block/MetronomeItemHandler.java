// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/MetronomeItemHandler.kt. Translated to
// Java for Forge 1.19.2 (NeoForge ItemStackHandler -> Forge ItemStackHandler).
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class MetronomeItemHandler extends ItemStackHandler {
    public MetronomeItemHandler() {
        super(8);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        ChrononHandler chronons = ChrononHandler.of(stack);
        return slot < 4 && chronons != null && chronons.canExtract() || slot >= 4 && chronons != null && chronons.canInsert();
    }
}
