// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/UpgradeHandler.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.tva_device;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UpgradeHandler {
    /** Upstream {@code tempad:upgrades} (item and block capability). */
    Capability<UpgradeHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    List<ResourceLocation> getInstalledUpgrades();

    boolean contains(ResourceLocation upgrade);

    void add(ResourceLocation upgrade);

    void remove(ResourceLocation upgrade);

    default void install(ResourceLocation upgrade) {
        add(upgrade);
    }

    default void uninstall(ResourceLocation upgrade) {
        remove(upgrade);
    }

    boolean willAccept(ResourceLocation upgrade);

    boolean isRemovable(ResourceLocation upgrade);

    /** Upstream {@code ItemStack.upgrades}. */
    @Nullable
    static UpgradeHandler of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return stack.getCapability(CAPABILITY).resolve().orElse(null);
    }

    /** Upstream {@code BlockEntity.upgrades}. */
    @Nullable
    static UpgradeHandler of(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null) return null;
        return blockEntity.getCapability(CAPABILITY, null).resolve().orElse(null);
    }
}
