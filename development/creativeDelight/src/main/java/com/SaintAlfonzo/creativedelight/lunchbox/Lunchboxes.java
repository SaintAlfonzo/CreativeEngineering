package com.SaintAlfonzo.creativedelight.lunchbox;

import java.util.List;

import javax.annotation.Nullable;

import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Spice of Life lunchboxes (solapplepie): tiers, skins, dye and contents. The contents live in the
 * stack's item-handler capability (not its tag), so every recipe that turns one box into another copies
 * them across here. The skin is one string on the stack ({@link #TAG_SKIN}, the skin item's id); the dye
 * is vanilla's {@code display.color}. Feature doc: feature_writeup/lunchboxes.md.
 */
public final class Lunchboxes {

    public static final String SOL = "solapplepie";

    public static final ResourceLocation LUNCHBAG = new ResourceLocation(SOL, "lunchbag");
    /** Copper Lunchbox in game (the pack renames it). */
    public static final ResourceLocation LUNCHBOX = new ResourceLocation(SOL, "lunchbox");
    /** Iron Lunchbox in game (the pack renames it). */
    public static final ResourceLocation GOLDEN_LUNCHBOX = new ResourceLocation(SOL, "golden_lunchbox");
    /** Registered by the pack ({@link LunchboxItems}). */
    public static final ResourceLocation NETHERITE_LUNCHBOX = new ResourceLocation(SOL, "netherite_lunchbox");

    /** Lowest tier first. A box can wear the look of any tier below its own. */
    public static final List<ResourceLocation> TIERS = List.of(LUNCHBAG, LUNCHBOX, GOLDEN_LUNCHBOX, NETHERITE_LUNCHBOX);

    /**
     * Every skin, in model-override order: the item property {@code creativedelight:lunchbox_skin} is the
     * position here + 1 (0 = no skin). The model JSONs in kubejs/assets/solapplepie/models/item/ use the same
     * numbers (tools/gen_lunchbox_looks.py writes them), so only append to this list.
     */
    public static final List<ResourceLocation> SKINS = List.of(
            LUNCHBAG, LUNCHBOX, GOLDEN_LUNCHBOX,
            new ResourceLocation("fantasyfurniture", "decorations/berry_basket_empty"),
            new ResourceLocation("fantasyfurniture", "decorations/berry_basket_blueberry"),
            new ResourceLocation("fantasyfurniture", "decorations/berry_basket_strawberry"),
            new ResourceLocation("fantasyfurniture", "decorations/berry_basket_sweetberry"));

    /** Stack tag holding the skin item id. */
    public static final String TAG_SKIN = "CELunchboxSkin";

    private Lunchboxes() {}

    public static boolean isLunchbox(ItemStack stack) {
        return stack.getItem() instanceof FoodContainerItem;
    }

    @Nullable
    private static ResourceLocation idOf(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    /** Position in {@link #TIERS}, or -1. */
    public static int tier(ItemStack stack) {
        return stack.isEmpty() ? -1 : TIERS.indexOf(idOf(stack.getItem()));
    }

    // ---- skin ----

    @Nullable
    public static ResourceLocation getSkin(ItemStack box) {
        CompoundTag tag = box.getTag();
        if (tag == null || !tag.contains(TAG_SKIN, 8)) return null;
        return ResourceLocation.tryParse(tag.getString(TAG_SKIN));
    }

    /** The {@code lunchbox_skin} item property: 0 = own look. */
    public static int skinIndex(ItemStack box) {
        ResourceLocation skin = getSkin(box);
        return skin == null ? 0 : SKINS.indexOf(skin) + 1;
    }

    /** True when the box shows a lunchbox look (its own or a lower tier's), which is what dye recolours. */
    public static boolean showsLunchboxLook(ItemStack box) {
        ResourceLocation skin = getSkin(box);
        return skin == null || TIERS.contains(skin);
    }

    /**
     * Whether {@code skinItem} may be smithed onto {@code box}: a berry basket always; a lunchbox only when it is
     * a lower tier and empty (it is used up). Not when the box already wears that skin.
     */
    public static boolean canTakeSkin(ItemStack box, ItemStack skinItem) {
        if (!isLunchbox(box) || skinItem.isEmpty()) return false;
        ResourceLocation id = idOf(skinItem.getItem());
        if (id == null || !SKINS.contains(id) || id.equals(getSkin(box))) return false;
        if (!TIERS.contains(id)) return true;
        int own = tier(box);
        return own >= 0 && tier(skinItem) < own && isEmpty(skinItem);
    }

    public static ItemStack withSkin(ItemStack box, ResourceLocation skin) {
        ItemStack out = box.copy();
        out.setCount(1);
        out.getOrCreateTag().putString(TAG_SKIN, skin.toString());
        return out;
    }

    public static ItemStack withoutSkin(ItemStack box) {
        ItemStack out = box.copy();
        out.setCount(1);
        CompoundTag tag = out.getTag();
        if (tag != null) {
            tag.remove(TAG_SKIN);
            if (tag.isEmpty()) out.setTag(null);
        }
        return out;
    }

    // ---- dye (vanilla display.color, so the "Dyed" tooltip line comes for free) ----

    public static boolean isDyed(ItemStack box) {
        CompoundTag display = box.getTagElement("display");
        return display != null && display.contains("color", 99);
    }

    /** The dye colour as 0xRRGGBB, or -1. */
    public static int getColor(ItemStack box) {
        CompoundTag display = box.getTagElement("display");
        return display != null && display.contains("color", 99) ? display.getInt("color") : -1;
    }

    public static ItemStack withColor(ItemStack box, int rgb) {
        ItemStack out = box.copy();
        out.setCount(1);
        out.getOrCreateTagElement("display").putInt("color", rgb);
        return out;
    }

    public static ItemStack withoutColor(ItemStack box) {
        ItemStack out = box.copy();
        out.setCount(1);
        CompoundTag tag = out.getTag();
        CompoundTag display = out.getTagElement("display");
        if (tag != null && display != null) {
            display.remove("color");
            if (display.isEmpty()) tag.remove("display");
            if (tag.isEmpty()) out.setTag(null);
        }
        return out;
    }

    // ---- contents ----

    @Nullable
    private static IItemHandler handler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    public static int slots(ItemStack box) {
        IItemHandler h = handler(box);
        return h == null ? 0 : h.getSlots();
    }

    public static boolean isEmpty(ItemStack box) {
        IItemHandler h = handler(box);
        if (h == null) return true;
        for (int i = 0; i < h.getSlots(); i++) {
            if (!h.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    /** Whether every filled slot of {@code from} has a slot at the same position in {@code to}. */
    public static boolean contentsFit(ItemStack from, ItemStack to) {
        IItemHandler src = handler(from);
        if (src == null) return true;
        int room = slots(to);
        for (int i = room; i < src.getSlots(); i++) {
            if (!src.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    /** Copies the contents of {@code from} into {@code to}, slot for slot. Check {@link #contentsFit} first. */
    public static void copyContents(ItemStack from, ItemStack to) {
        IItemHandler src = handler(from);
        IItemHandler dst = handler(to);
        if (src == null || !(dst instanceof IItemHandlerModifiable out)) return;
        int n = Math.min(src.getSlots(), out.getSlots());
        for (int i = 0; i < n; i++) {
            out.setStackInSlot(i, src.getStackInSlot(i).copy());
        }
    }

    /**
     * The result of upgrading {@code from} into a box of {@code result}'s item: {@code result}'s count, the
     * source's tag (name, dye, skin) and its contents.
     */
    public static ItemStack upgraded(ItemStack from, ItemStack result) {
        ItemStack out = new ItemStack(result.getItem(), result.getCount());
        CompoundTag tag = from.getTag();
        if (tag != null) out.setTag(tag.copy());
        copyContents(from, out);
        return out;
    }

    /** A bundle that still holds items (crafting it into a box would delete them). */
    public static boolean isFilledBundle(ItemStack stack) {
        if (!(stack.getItem() instanceof BundleItem)) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && !tag.getList("Items", 10).isEmpty();
    }
}
