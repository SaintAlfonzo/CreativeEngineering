// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/TempadChrononHandler.kt. Translated to Java for
// Forge 1.19.2: data components -> stack NBT (ModComponents).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TempadChrononHandler implements ChrononHandler {
    public final ItemStack stack;
    public final int tempadLimit;
    private final int maxPower;

    public TempadChrononHandler(ItemStack stack, int tempadLimit, int timeTwisterLimit) {
        this.stack = stack;
        this.tempadLimit = tempadLimit;
        this.maxPower = tempadLimit + (ModComponents.isTwisterEquipped(stack) ? timeTwisterLimit : 0);
    }

    @Nullable
    public static TempadChrononHandler create(ItemStack stack, int tempadLimit, int timeTwisterLimit) {
        if (tempadLimit + timeTwisterLimit <= 0) return null;
        return new TempadChrononHandler(stack, tempadLimit, timeTwisterLimit);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int getPower() {
        return ModComponents.getChrononContentTempad(stack) + ModComponents.getChrononContentTimeTwister(stack);
    }

    private void setPower(int value) {
        // put in tempad first, then time twister
        int diff = value - getPower();
        if (diff == 0) return;
        int tempadDiff = Math.min(diff, tempadLimit - ModComponents.getChrononContentTempad(stack));
        ModComponents.setChrononContentTempad(stack, ModComponents.getChrononContentTempad(stack) + tempadDiff);
        ModComponents.setChrononContentTimeTwister(stack, ModComponents.getChrononContentTimeTwister(stack) + diff - tempadDiff);
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
