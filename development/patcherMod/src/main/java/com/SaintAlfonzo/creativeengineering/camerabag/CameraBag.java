package com.SaintAlfonzo.creativeengineering.camerabag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Camera bags: an item inventory (like Spice of Life's lunch boxes) that only takes
 * {@code #creativeengineering:camera_equipment}, in any slot, at normal stack size.
 *
 * <p>Contents live in the bag's own NBT ({@code CameraBag.Items}, vanilla container format), not in a
 * Forge item capability. That way the client sees them (tooltip), and a smithing upgrade, which copies
 * the base item's NBT, keeps them. Recipes: kubejs server_scripts/equipment/camera_bag.js.
 */
public final class CameraBag {

    public static final TagKey<Item> CAMERA_EQUIPMENT =
            ItemTags.create(new ResourceLocation(CreativeEngineering.MODID, "camera_equipment"));

    /** Sub-tag of the bag's NBT holding the {@code Items} list. */
    public static final String NBT_KEY = "CameraBag";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CreativeEngineering.MODID);

    public static final Map<CameraBagTier, RegistryObject<Item>> BY_TIER = new EnumMap<>(CameraBagTier.class);
    public static final RegistryObject<MenuType<CameraBagMenu>> MENU =
            MENUS.register("camera_bag", () -> IForgeMenuType.create(CameraBagMenu::fromNetwork));

    static {
        for (CameraBagTier tier : CameraBagTier.values()) {
            BY_TIER.put(tier, ITEMS.register(tier.itemId(), () -> new CameraBagItem(tier)));
        }
    }

    private CameraBag() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
    }

    /** What a bag slot accepts. Never another bag, nor anything that refuses to go in containers (shulker boxes). */
    public static boolean canHold(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(CAMERA_EQUIPMENT)
                && !(stack.getItem() instanceof CameraBagItem)
                && stack.getItem().canFitInsideContainerItems();
    }

    /** The bag's first {@code size} slots; anything stored past them is left out (see {@link #spillOverflow}). */
    public static ItemStack[] load(ItemStack bag, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        CompoundTag data = bag.getTagElement(NBT_KEY);
        if (data != null) {
            ContainerHelper.loadAllItems(data, items);
        }
        return items.toArray(new ItemStack[0]);
    }

    /** Writes the container into the bag; an empty bag has no CameraBag tag at all, same as a fresh one. */
    public static void save(ItemStack bag, Container container) {
        NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < items.size(); slot++) {
            items.set(slot, container.getItem(slot));
        }
        CompoundTag data = new CompoundTag();
        ContainerHelper.saveAllItems(data, items, false);
        if (data.isEmpty()) {
            bag.removeTagKey(NBT_KEY);
        } else {
            bag.getOrCreateTag().put(NBT_KEY, data);
        }
    }

    /** Every non-empty stack in the bag, whatever its slot. */
    public static List<ItemStack> contents(ItemStack bag) {
        List<ItemStack> out = new ArrayList<>();
        CompoundTag data = bag.getTagElement(NBT_KEY);
        if (data == null) {
            return out;
        }
        ListTag list = data.getList("Items", Tag.TAG_COMPOUND);
        for (int n = 0; n < list.size(); n++) {
            ItemStack stack = ItemStack.of(list.getCompound(n));
            if (!stack.isEmpty()) {
                out.add(stack);
            }
        }
        return out;
    }

    /**
     * Server side, before opening: items stored in slots at or past {@code size} (the slot count was
     * lowered in the config) go to the player's inventory, or drop at their feet when it is full.
     */
    public static void spillOverflow(Player player, ItemStack bag, int size) {
        CompoundTag data = bag.getTagElement(NBT_KEY);
        if (data == null) {
            return;
        }
        ListTag list = data.getList("Items", Tag.TAG_COMPOUND);
        boolean changed = false;
        for (int n = list.size() - 1; n >= 0; n--) {
            CompoundTag entry = list.getCompound(n);
            if ((entry.getByte("Slot") & 255) < size) {
                continue;
            }
            list.remove(n);
            changed = true;
            ItemStack stack = ItemStack.of(entry);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
        if (changed && list.isEmpty()) {
            bag.removeTagKey(NBT_KEY);
        }
    }
}
