// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/WorkstationChrononHandler.kt. Translated to Java
// for Forge 1.19.2. Kotlin's delegates are kept as bound: the handler follows the stack docked when this was created
// (`by block.inventory[0]::chronons`), power and maxPower the handler seen at creation (`by handler!!::power`).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.block.WorkstationBE;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public class WorkstationChrononHandler implements ChrononHandler {
    public final WorkstationBE block;
    private final ItemStack stack;
    private final ChrononHandler initial;

    public WorkstationChrononHandler(WorkstationBE block) {
        this.block = block;
        this.stack = block.inventory.getStackInSlot(0);
        this.initial = Objects.requireNonNull(ChrononHandler.of(stack));
    }

    public ChrononHandler getHandler() {
        return ChrononHandler.of(stack);
    }

    @Override
    public int getPower() {
        return initial.getPower();
    }

    @Override
    public int getMaxPower() {
        return initial.getMaxPower();
    }

    @Override
    public int extract(int amount, ActionType action) {
        int changed = Objects.requireNonNull(getHandler()).extract(amount, action);
        if (action == ActionType.Execute) {
            block.setChanged();
            Objects.requireNonNull(block.getLevel()).sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
        }
        return changed;
    }

    @Override
    public int insert(int amount, ActionType action) {
        int changed = Objects.requireNonNull(getHandler()).insert(amount, action);
        if (action == ActionType.Execute) {
            block.setChanged();
            Objects.requireNonNull(block.getLevel()).sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
        }
        return changed;
    }
}
