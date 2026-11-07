package com.SaintAlfonzo.creativecompat.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Supplementaries sacks (and Supp Squared's dyed ones, same {@link SackItem}) handled like bundles in an
 * inventory screen, keeping the sack's own slot count ({@code [functional.sack] slots}, 16 in this pack).
 *
 * <ul>
 *   <li>The first stored stack is the "selected" one: it is what comes out, and it is the first cell of the
 *       Quark contents tooltip.</li>
 *   <li>Scroll over a sack: the stored stacks rotate one step ({@link SackBundleScroll} on the client, the
 *       {@link Rotate} packet here). The stacks are compacted into the first slots.</li>
 *   <li>Right-click a sack in a slot with an empty cursor: the selected stack goes to the cursor
 *       ({@code mixin.SupplementariesSackItemMixin}).</li>
 *   <li>Unchanged, from Supplementaries' Quark drop-in: right-click an item onto a sack to put it in, and
 *       right-click a slot while carrying a sack to put 1 of the selected item there. A placed sack still opens
 *       its own screen.</li>
 * </ul>
 */
public final class SackBundle {

    private static final String PROTOCOL = "1";
    // 27 = Supplementaries' maximum for [functional.sack] slots
    private static final int MAX_SLOTS = 27;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(com.SaintAlfonzo.creativecompat.CreativeCompat.MOD_ID, "sack_bundle"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private SackBundle() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, Rotate.class, Rotate::encode, Rotate::decode, Rotate::handle);
    }

    /** Stored stacks in slot order, empties dropped; null when the stack is not an openable sack. */
    private static List<ItemStack> contents(ItemStack sack) {
        if (!(sack.getItem() instanceof SackItem) || sack.getCount() != 1) {
            return null;
        }
        CompoundTag tag = sack.getTagElement("BlockEntityTag");
        if (tag == null || tag.contains("LootTable")) {
            return null;
        }
        NonNullList<ItemStack> slots = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, slots);
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : slots) {
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }

    /** Writes the stacks back into slots 0..n-1; an empty sack loses its tag so it matches a new one. */
    private static void store(ItemStack sack, List<ItemStack> stacks) {
        CompoundTag tag = sack.getOrCreateTagElement("BlockEntityTag");
        tag.remove("Items");
        if (!stacks.isEmpty()) {
            NonNullList<ItemStack> slots = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
            for (int i = 0; i < stacks.size(); i++) {
                slots.set(i, stacks.get(i));
            }
            ContainerHelper.saveAllItems(tag, slots);
        }
        if (tag.isEmpty()) {
            sack.removeTagKey("BlockEntityTag");
            if (sack.getTag() != null && sack.getTag().isEmpty()) {
                sack.setTag(null);
            }
        }
    }

    public static boolean canRotate(ItemStack sack) {
        List<ItemStack> stacks = contents(sack);
        return stacks != null && stacks.size() > 1;
    }

    /** One step: forward moves the selected stack to the back, backward brings the last one to the front. */
    public static boolean rotate(ItemStack sack, boolean forward) {
        List<ItemStack> stacks = contents(sack);
        if (stacks == null || stacks.size() < 2) {
            return false;
        }
        if (forward) {
            stacks.add(stacks.remove(0));
        } else {
            stacks.add(0, stacks.remove(stacks.size() - 1));
        }
        store(sack, stacks);
        return true;
    }

    /** Removes and returns the selected stack, or EMPTY when there is none. */
    public static ItemStack takeSelected(ItemStack sack, Player player) {
        List<ItemStack> stacks = contents(sack);
        if (stacks == null || stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = stacks.remove(0);
        store(sack, stacks);
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + player.getLevel().getRandom().nextFloat() * 0.4F);
        return taken;
    }

    /**
     * Client to server: rotate the sack in a slot. {@code inventory} = player inventory index (creative screen,
     * whose slots are not the server menu's), otherwise a slot index of the open menu.
     */
    public record Rotate(int containerId, int slot, boolean inventory, boolean forward) {

        void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(containerId);
            buf.writeVarInt(slot);
            buf.writeBoolean(inventory);
            buf.writeBoolean(forward);
        }

        static Rotate decode(FriendlyByteBuf buf) {
            return new Rotate(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                if (player == null) {
                    return;
                }
                if (inventory) {
                    if (slot >= 0 && slot < player.getInventory().getContainerSize()
                            && rotate(player.getInventory().getItem(slot), forward)) {
                        player.getInventory().setChanged();
                    }
                    return;
                }
                AbstractContainerMenu menu = player.containerMenu;
                if (menu.containerId != containerId || slot < 0 || slot >= menu.slots.size()) {
                    return;
                }
                Slot s = menu.getSlot(slot);
                if (s.allowModification(player) && rotate(s.getItem(), forward)) {
                    s.setChanged();
                }
            });
            c.setPacketHandled(true);
        }
    }
}
