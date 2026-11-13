// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.compat.cc.redstone;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.electricity.redstoneconverter.RedstoneConverterBlockEntity;

public class RedstoneConverterPeripheral implements IPeripheral {
    private final RedstoneConverterBlockEntity converter;

    public RedstoneConverterPeripheral(RedstoneConverterBlockEntity converter) {
        this.converter = converter;
    }

    // Port fix B17: upstream's setValue and clearValue ran on the computer thread and wrote the block entity from there;
    // mainThread = true runs them on the server thread (CC: Tweaked queues the call and yields the computer).
    @LuaFunction(mainThread = true)
    public void setValue(double value) {
        converter.signalOverride = (float) value;
        converter.setUnsaved();
    }

    @LuaFunction(mainThread = true)
    public void clearValue() {
        converter.signalOverride = null;
        converter.setUnsaved();
    }

    @Override
    public @NotNull String getType() {
        return "powergrid_redstone_converter";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (other instanceof RedstoneConverterPeripheral that) {
            return this.converter == that.converter;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return converter.hashCode();
    }
}
