package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Storage in Cooking for Blockheads' kitchen corner (TODO cooking/kitchen #15; moved from patcherMod 2026-10-31). CfB's corner is a counter-sized block
 * with only a kitchen connector; {@code mixin/CfbCornerStorageMixin} makes its block entity a Balm container provider
 * holding a {@link CornerContainer} (27 slots, 54 with CfB's {@code largeCounters}, like a counter), which gives it,
 * through CfB's and Balm's own code, item drops when broken (BlockKitchen#onRemove) and an item handler for hoppers.
 * The mixin also lists it as a kitchen item provider, so the cooking table uses what's inside, and
 * {@code mixin/CfbCornerUseMixin} opens it as a chest. Saved here as an attached capability
 * ({@code ForgeCaps/creativedelight:corner_storage}), as the corner block entity has no save of its own.
 */
public final class CornerStorage {

    /** Kept from patcherMod, where this lived until 2026-10-31, so corners already filled keep their contents. */
    private static final ResourceLocation ID = new ResourceLocation(CreativeDelight.MOD_ID, "corner_storage");

    private CornerStorage() {}

    /** The corner block entity, given storage by mixin. */
    public interface Holder {
        CornerContainer ce$cornerStorage();
    }

    /** A chest menu on the corner's container, titled with the block's name. */
    public static MenuProvider menu(CornerContainer container, Component title) {
        return new SimpleMenuProvider((id, inventory, player) -> container.getContainerSize() > 27
                ? ChestMenu.sixRows(id, inventory, container)
                : ChestMenu.threeRows(id, inventory, container), title);
    }

    /** Marks the block entity for saving on every change and closes the menu when the corner goes or the player walks off. */
    public static final class CornerContainer extends SimpleContainer {
        private final BlockEntity owner;

        public CornerContainer(BlockEntity owner, int size) {
            super(size);
            this.owner = owner;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            owner.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            BlockPos pos = owner.getBlockPos();
            return owner.getLevel() != null && owner.getLevel().getBlockEntity(pos) == owner
                    && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
        }

        CompoundTag save() {
            ListTag items = new ListTag();
            for (int i = 0; i < getContainerSize(); ++i) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag item = stack.save(new CompoundTag());
                    item.putInt("Slot", i);
                    items.add(item);
                }
            }
            CompoundTag tag = new CompoundTag();
            tag.put("Items", items);
            return tag;
        }

        void load(CompoundTag tag) {
            clearContent();
            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); ++i) {
                CompoundTag item = items.getCompound(i);
                int slot = item.getInt("Slot");
                if (slot >= 0 && slot < getContainerSize()) {
                    setItem(slot, ItemStack.of(item));
                }
            }
        }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof Holder holder) {
            event.addCapability(ID, new Provider(holder));
        }
    }

    private record Provider(Holder holder) implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }

        // Asked for lazily: the event fires inside the BlockEntity constructor, before the mixin's fields are set.
        @Override
        public CompoundTag serializeNBT() {
            return holder.ce$cornerStorage().save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            holder.ce$cornerStorage().load(tag);
        }
    }
}
