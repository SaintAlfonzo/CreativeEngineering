package com.SaintAlfonzo.creativecompat.compat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * Chest storage in Handcrafted 2.0.6's nightstands (every wood, Every Compat's included: their block and block entity
 * extend Handcrafted's). {@code mixin/HandcraftedNightstandStorageMixin} gives the block entity a 27-slot
 * {@link NightstandContainer}; {@code mixin/HandcraftedItemHoldingSaveMixin} saves it under its own key
 * ({@value #KEY}) beside Handcrafted's {@code Item} (the sheet on top), which is left as it was;
 * {@code mixin/HandcraftedNightstandUseMixin} opens it as a chest and drops it when the block is broken. The item
 * handler attached here lets hoppers and pipes use it. Saved by mixin rather than as a capability, as
 * ItemHoldingBlockEntity's load does not call BlockEntity's, so ForgeCaps would never be read back.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NightstandStorage {

    public static final String KEY = "CENightstandItems";
    public static final int SIZE = 27;

    private static final ResourceLocation ID = new ResourceLocation(CreativeCompat.NS, "nightstand_storage");

    private NightstandStorage() {}

    /** The nightstand block entity, given storage by mixin. */
    public interface Holder {
        NightstandContainer ce$nightstandStorage();
    }

    /** A three-row chest menu on the nightstand's container, titled with the block's name. */
    public static MenuProvider menu(NightstandContainer container, Component title) {
        return new SimpleMenuProvider((id, inventory, player) -> ChestMenu.threeRows(id, inventory, container), title);
    }

    public static void save(Object blockEntity, CompoundTag tag) {
        if (blockEntity instanceof Holder holder) {
            tag.put(KEY, holder.ce$nightstandStorage().save());
        }
    }

    public static void load(Object blockEntity, CompoundTag tag) {
        if (blockEntity instanceof Holder holder && tag.contains(KEY, Tag.TAG_LIST)) {
            holder.ce$nightstandStorage().load(tag.getList(KEY, Tag.TAG_COMPOUND));
        }
    }

    /** Marks the block entity for saving on every change and closes the menu when the nightstand goes or the player walks off. */
    public static final class NightstandContainer extends SimpleContainer {
        private final BlockEntity owner;

        public NightstandContainer(BlockEntity owner) {
            super(SIZE);
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

        ListTag save() {
            ListTag items = new ListTag();
            for (int i = 0; i < getContainerSize(); ++i) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag item = stack.save(new CompoundTag());
                    item.putByte("Slot", (byte) i);
                    items.add(item);
                }
            }
            return items;
        }

        void load(ListTag items) {
            clearContent();
            for (int i = 0; i < items.size(); ++i) {
                CompoundTag item = items.getCompound(i);
                int slot = item.getByte("Slot") & 255;
                if (slot < getContainerSize()) {
                    setItem(slot, ItemStack.of(item));
                }
            }
        }
    }

    /** Only with Handcrafted (its mixins add the storage; without them nothing is a Holder anyway). */
    private static final boolean HANDCRAFTED = net.minecraftforge.fml.ModList.get().isLoaded("handcrafted");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!HANDCRAFTED) return;
        if (event.getObject() instanceof Holder holder) {
            Provider provider = new Provider(holder);
            event.addCapability(ID, provider);
            event.addListener(provider.items::invalidate);
        }
    }

    private static final class Provider implements ICapabilityProvider {
        // Built lazily: the event fires inside the BlockEntity constructor, before the mixin's fields are set.
        private final LazyOptional<IItemHandler> items;

        Provider(Holder holder) {
            this.items = LazyOptional.of(() -> new InvWrapper(holder.ce$nightstandStorage()));
        }

        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, items);
        }
    }
}
