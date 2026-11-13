// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Replaces upstream's 1.20 creative tab registration (PowerGridImpl.TABS, CreativeModeTab.builder()).
package org.patryk3211.powergrid.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;

/**
 * The "Power Grid" creative tab ({@code itemGroup.powergrid.main}). Icon: the Electric Motor, as upstream (since phase P4;
 * earlier phases showed a copper ingot). Items join it through Registrate's default tab (set by the phase
 * that ports ModdedItems), so the tab lists only registered content.
 */
public class PowerGridCreativeTab extends CreativeModeTab {
    public static PowerGridCreativeTab MAIN;

    private PowerGridCreativeTab() {
        super(PowerGrid.MOD_ID + ".main");
    }

    public static PowerGridCreativeTab create() {
        if(MAIN == null)
            MAIN = new PowerGridCreativeTab();
        return MAIN;
    }

    // Since P9a: upstream's tab exclusions (ItemDisplay.makeExclusionPredicate): transitional items, the placeable portable
    // battery, and the items that only make sense with a schematic in them (a plain circuit board removes itself when
    // placed). Ids not registered in this phase are skipped.
    private static final java.util.Set<String> EXCLUDED = java.util.Set.of(
            "incomplete_transformer_core", "incomplete_battery", "incomplete_circuit", "incomplete_electrical_gizmo",
            "incomplete_unetched_circuit", "incomplete_punch_card", "incomplete_bjt_npn", "incomplete_bjt_pnp",
            "incomplete_solar_panel", "portable_battery_placeable", "circuit_schematic", "unetched_circuit", "circuit_board");

    @Override
    public void fillItemList(net.minecraft.core.NonNullList<ItemStack> items) {
        super.fillItemList(items);
        items.removeIf(stack -> {
            var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
            return id != null && id.getNamespace().equals(PowerGrid.MOD_ID) && EXCLUDED.contains(id.getPath());
        });
    }

    @Override
    public ItemStack makeIcon() {
        // Upstream's icon (the Electric Motor) since phase P4.
        return new ItemStack(org.patryk3211.powergrid.collections.ModdedBlocks.ELECTRIC_MOTOR.get());
    }
}
