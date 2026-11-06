package com.SaintAlfonzo.creativecommon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes creative pick block deleting the held item on the server (vanilla MC-197793 and its
 * duplicate MC-174436, "ghost items").
 *
 * <p>In creative, {@code Minecraft.pickBlock} calls {@code Inventory.setPickedItem} on the client
 * only. With a full hotbar that moves the held item into a free main-inventory slot (or swaps it
 * with the picked item if that is already in the main inventory), but vanilla then sends a
 * creative-slot packet for the selected hotbar slot alone. The server overwrites the held item and
 * never hears where it went, so the client is left showing a ghost that vanishes on the next
 * server-authoritative move or full inventory sync.
 *
 * <p>The redirect records which stack sat in each slot, runs {@code setPickedItem} unchanged, then
 * sends every other slot whose stack changed. Slots are compared by identity: {@code setPickedItem}
 * moves stack references and never copies them. The packet copies the stack itself.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} ({@code m_91280_} =
 * {@code pickBlock}, {@code m_36012_} = {@code setPickedItem}). The only {@code setPickedItem} call in
 * {@code pickBlock} is the creative branch. Client-only (listed under "client" in the mixin config).
 */
@Mixin(value = Minecraft.class, remap = false)
public abstract class CreativePickBlockSyncMixin {

    @Redirect(method = "m_91280_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;m_36012_(Lnet/minecraft/world/item/ItemStack;)V"))
    private void creativecommon$syncPickedItem(Inventory inventory, ItemStack picked) {
        ItemStack[] before = inventory.items.toArray(new ItemStack[0]);
        inventory.setPickedItem(picked);

        Minecraft mc = (Minecraft) (Object) this;
        if (mc.gameMode == null) return;
        for (int i = 0; i < before.length; i++) {
            // vanilla sends the selected hotbar slot itself right after this call
            if (i == inventory.selected) continue;
            ItemStack now = inventory.items.get(i);
            if (now != before[i]) {
                mc.gameMode.handleCreativeModeItemAdd(now, Inventory.isHotbarSlot(i) ? 36 + i : i);
            }
        }
    }
}
