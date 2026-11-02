package com.SaintAlfonzo.creativedelight.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/** The Curios half of {@link CornDelightCompat}: only loaded when Curios is. */
final class CornDelightCurios {

    private CornDelightCurios() {}

    /** Puts one of {@code stack} in the first empty slot of the player's {@code slot} curio type, if it has that type. */
    static boolean equip(Player player, String slot, ItemStack stack) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(player)
                .resolve()
                .flatMap(h -> h.getStacksHandler(slot))
                .map(sh -> {
                    IDynamicStackHandler stacks = sh.getStacks();
                    for (int n = 0; n < stacks.getSlots(); n++) {
                        if (stacks.getStackInSlot(n).isEmpty()) {
                            stacks.setStackInSlot(n, stack.split(1));
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }
}
