// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/ItemChrononHandler.kt. Translated to Java for
// Forge 1.19.2: the chronon_content data component -> stack NBT (ModComponents).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemChrononHandler implements ChrononHandler {
    public final ItemStack stack;
    private final int maxPower;
    public boolean canInsert = true;
    public boolean canExtract = true;

    public ItemChrononHandler(ItemStack stack, int maxPower) {
        this.stack = stack;
        this.maxPower = maxPower;
    }

    @Nullable
    public static ItemChrononHandler create(ItemStack stack, int maxPower) {
        if (maxPower <= 0) return null;
        return new ItemChrononHandler(stack, maxPower);
    }

    @Override
    public boolean canInsert() {
        return canInsert;
    }

    @Override
    public boolean canExtract() {
        return canExtract;
    }

    @Override
    public int getPower() {
        return ModComponents.getChrononContent(stack);
    }

    private void setPower(int power) {
        ModComponents.setChrononContent(stack, power);
    }

    @Override
    public int getMaxPower() {
        return maxPower;
    }

    @Override
    public int extract(int amount, ActionType action) {
        int extracted = Mth.clamp(getPower(), 0, amount);
        if (action == ActionType.Execute) setPower(getPower() - extracted);
        return extracted;
    }

    @Override
    public int insert(int amount, ActionType action) {
        int inserted = Mth.clamp(amount, 0, maxPower - getPower());
        if (action == ActionType.Execute) setPower(getPower() + inserted);
        return inserted;
    }
}
