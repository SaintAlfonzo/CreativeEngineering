// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/ChrononHandler.kt. Translated to Java for Forge 1.19.2:
// NeoForge Item/BlockCapability -> one Forge Capability (attached in forge/TempadCapabilities).
package earth.terrarium.tempad.api.tva_device;

import earth.terrarium.tempad.api.ActionType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.Nullable;

public interface ChrononHandler {
    /** Upstream {@code tempad:chronon} (item and block capability). */
    Capability<ChrononHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    int getPower();

    int getMaxPower();

    default boolean canInsert() {
        return true;
    }

    default boolean canExtract() {
        return true;
    }

    int extract(int amount, ActionType action);

    int insert(int amount, ActionType action);

    default boolean hasRoom() {
        return insert(1, ActionType.Simulate) == 1;
    }

    default void drainAndExecute(int amount, Runnable action) {
        int extracted = extract(amount, ActionType.Simulate);
        if (extracted == amount) {
            extract(amount, ActionType.Execute);
            action.run();
        }
    }

    static void move(ChrononHandler from, ChrononHandler to, int amount) {
        int extracted = from.extract(amount, ActionType.Simulate);
        int inserted = to.insert(extracted, ActionType.Execute);
        from.extract(inserted, ActionType.Execute);
    }

    /** Upstream {@code ItemStack.chronons}. */
    @Nullable
    static ChrononHandler of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return stack.getCapability(CAPABILITY).resolve().orElse(null);
    }

    /** Upstream {@code BlockEntity.chronons} (queried from the UP side). */
    @Nullable
    static ChrononHandler of(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null) return null;
        return blockEntity.getCapability(CAPABILITY, Direction.UP).resolve().orElse(null);
    }

    /** Upstream {@code ChrononHandler.tooltip}. */
    default ChrononData tooltip() {
        return new ChrononData(getPower(), getMaxPower());
    }
}
