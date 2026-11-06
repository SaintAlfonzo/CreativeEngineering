package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Scroll over a sack holding 2+ stacks in any inventory screen: rotates its contents ({@link SackBundle}).
 * Cancelling the Pre event also skips Mouse Tweaks' wheel tweak, which runs on Post.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, value = Dist.CLIENT)
public final class SackBundleScroll {

    /** Only with Supplementaries (the sacks are its items; SackBundle registers its channel only then). */
    private static final boolean SUPPLEMENTARIES = net.minecraftforge.fml.ModList.get().isLoaded("supplementaries");

    private SackBundleScroll() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!SUPPLEMENTARIES) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || event.getScrollDelta() == 0) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !SackBundle.canRotate(slot.getItem())) {
            return;
        }
        boolean forward = event.getScrollDelta() < 0;
        if (slot.container instanceof Inventory) {
            SackBundle.CHANNEL.sendToServer(new SackBundle.Rotate(0, slot.getContainerSlot(), true, forward));
        } else if (screen instanceof CreativeModeInventoryScreen) {
            return;
        } else {
            SackBundle.CHANNEL.sendToServer(
                    new SackBundle.Rotate(screen.getMenu().containerId, slot.index, false, forward));
        }
        // Local copy too, so the tooltip turns at once; the server's slot sync then matches it
        SackBundle.rotate(slot.getItem(), forward);
        event.setCanceled(true);
    }
}
