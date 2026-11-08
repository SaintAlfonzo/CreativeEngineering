// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/ItemUpgradeHandler.kt. Translated to Java for
// Forge 1.19.2: the installed_upgrades data component -> stack NBT (ModComponents).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.data.InstalledUpgradesComponent;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemUpgradeHandler implements UpgradeHandler {
    public final ItemStack stack;

    public ItemUpgradeHandler(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public List<ResourceLocation> getInstalledUpgrades() {
        return ModComponents.getInstalledUpgrades(stack).upgrades();
    }

    private void setInstalledUpgrades(InstalledUpgradesComponent value) {
        ModComponents.setInstalledUpgrades(stack, value);
    }

    @Override
    public boolean contains(ResourceLocation upgrade) {
        return getInstalledUpgrades().contains(upgrade);
    }

    @Override
    public void add(ResourceLocation upgrade) {
        if (!contains(upgrade)) setInstalledUpgrades(ModComponents.getInstalledUpgrades(stack).plus(upgrade));
    }

    @Override
    public void remove(ResourceLocation upgrade) {
        setInstalledUpgrades(ModComponents.getInstalledUpgrades(stack).minus(upgrade));
    }

    @Override
    public boolean willAccept(ResourceLocation upgrade) {
        return true;
    }

    @Override
    public boolean isRemovable(ResourceLocation upgrade) {
        return true;
    }
}
