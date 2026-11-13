// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.equipment.portablebattery.forge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.patryk3211.powergrid.equipment.portablebattery.BatteryUtils;

public class PortableBatteryItemImpl {
    public static void tryTransferPower(ItemStack heldStack, ItemStack batteryStack, Player player) {
        LazyOptional<IEnergyStorage> heldEnergy = heldStack.getCapability(ForgeCapabilities.ENERGY);
        heldEnergy.ifPresent(handler -> {
            // Port fix B20 (power_grid.md 3.5): the request is capped at the worn battery's charge; upstream asked for the
            // item's whole free space, and a buffer larger than the charge (an AE2 wireless terminal, 3.2M FE) emptied
            // the battery and delivered nothing
            int maxEnergyDrain = Math.min(handler.getMaxEnergyStored() - handler.getEnergyStored(), BatteryUtils.availableEnergy(player));
            if (maxEnergyDrain < 1) return;

            int energyDrain = handler.receiveEnergy(maxEnergyDrain, true);
            if (energyDrain <= 0) return;

            float available = BatteryUtils.drawEnergy(player, energyDrain);
            if (available <= 0) return;

            handler.receiveEnergy((int) (available * energyDrain), false);
        });
    }
}
