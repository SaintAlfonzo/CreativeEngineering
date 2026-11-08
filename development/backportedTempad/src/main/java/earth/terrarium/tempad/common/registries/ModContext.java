// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModContext.kt. Translated to Java for Forge 1.19.2.
// Inventory, carried-item and workstation contexts; the Curios context and locator only when Curios is installed (TP-D8).
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.Priority;
import earth.terrarium.tempad.api.PriorityId;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.InventoryContext;
import earth.terrarium.tempad.api.context.WorkstationContext;
import earth.terrarium.tempad.common.compat.CuriosCompat;
import earth.terrarium.tempad.common.menu.CarriedMenuCtx;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class ModContext {
    private ModContext() {
    }

    public static void init() {
        ContextRegistry.register(InventoryContext.TYPE, InventoryContext::new);
        ContextRegistry.register(CarriedMenuCtx.TYPE, (player, unit) -> new CarriedMenuCtx(player));
        ContextRegistry.register(WorkstationContext.TYPE, WorkstationContext::new);
        ContextRegistry.registerLocator(new PriorityId(Tempad.id("inventory"), Priority.NORMAL), (player, filter) -> {
            for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                ItemStack stack = player.getInventory().getItem(index);
                if (filter.test(stack)) return new InventoryContext(player, index);
            }
            return null;
        });
        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.init();
        }
    }
}
