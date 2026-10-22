package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.mixin.ItemBackedInventoryAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * What Botania's Trinket Case ({@code botania:bauble_box}) may hold beyond its own trinket / rod /
 * mana-item check. Used by the {@code TrinketCase*Mixin}s; when this says no, Botania's check runs.
 *
 * <p>Any item, shulker boxes and other cases included: the case works as a chest. The one exception
 * is the open case's own stack ({@link #isOpenCase}), in every mode: a hotbar-key swap onto an empty
 * case slot would put the case inside itself, and its inventory, written back into that moved stack,
 * would be lost with it. Botania's own check never accepted a case, so it never needed this.
 */
public final class TrinketCaseRules {

    private TrinketCaseRules() {}

    public static boolean allowed(ItemStack stack) {
        return CEConfig.BOTANIA.trinketCaseAnyItem.get() && !stack.isEmpty();
    }

    /** The stack is the case whose inventory {@code caseInventory} is (Botania's {@code ItemBackedInventory}). */
    public static boolean isOpenCase(Container caseInventory, ItemStack stack) {
        return caseInventory instanceof ItemBackedInventoryAccessor acc && acc.creativeengineering$stack() == stack;
    }
}
