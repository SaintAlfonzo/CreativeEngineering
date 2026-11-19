package com.SaintAlfonzo.creativecompat.corpse;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import de.maxhenkel.corpse.corelib.inventory.LockedSlot;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The sectioned corpse screen (feature_writeup/plan_corpses.md 3.6 items 2 to 4; rulings 10, 31, 32): cosmetic armor
 * and curios keep their own slots on the corpse screen instead of the Additional Items pages. They stay in the Corpse
 * mod's {@code additionalItems} list (so {@code CorpseEntity.isEmpty} still counts them); this class decides which
 * tagged stacks are shown where:
 * <ul>
 * <li>Cosmetic armor: stacks tagged by Cosmetic Armor x Corpse Compat ({@link #COS_ITEM}, {@link #COS_INDEX},
 * {@link #COS_SKIN}); one per cosmetic index 0 (feet) to 3 (head).</li>
 * <li>Curios: stacks tagged {@link #CURIO} {Slot, Index, Cosmetic} at death by {@link CorpseCurios}; the first
 * {@code cap} of them in slot id, cosmetic, index order.</li>
 * </ul>
 * The slot count is fixed per menu on both sides from the corpse's synced section tag ({@link #capOf}): the client
 * corpse holds no items, so it cannot count them. Section slots are take-only; taking strips the four tags so the
 * item stacks like any other.
 */
public final class CorpseSections {
	/** Compat's curio origin tag on a stack inside a corpse. */
	public static final String CURIO = "creativecompat:curio";
	public static final String COS_ITEM = "CosmeticArmorItem";
	public static final String COS_INDEX = "CosmeticSlotIndex";
	public static final String COS_SKIN = "CosmeticSkinArmor";

	/** Curios is loaded: {@link CorpseCurios} may be called. */
	public static final boolean CURIOS = ModList.get().isLoaded("curios");

	public static final int COSMETIC = 4;
	/** Curio slots in one column of the panel left of the window. */
	public static final int COLUMN = 12;
	/** Section slot count the synced tag starts with (also used by history corpses, which never tick). */
	public static final int DEFAULT_CAP = 24;
	public static final int MAX_CAP = 48;

	/** x of the first cosmetic slot on the corpse screen's top row; the offhand moves to {@link #OFFHAND_X}. */
	public static final int COSMETIC_X = 80;
	public static final int OFFHAND_X = 152;
	public static final int TOP_Y = 18;
	/** Curio slot column 0 x; each further column is {@link #COLUMN_STEP} further left. */
	public static final int CURIO_X = -18;
	public static final int COLUMN_STEP = 26;

	private CorpseSections() {}

	// ---- tags ----

	public static boolean isCosmetic(ItemStack s) {
		CompoundTag t = s.getTag();
		return t != null && t.getBoolean(COS_ITEM) && t.contains(COS_INDEX);
	}

	public static boolean isCurio(ItemStack s) {
		CompoundTag t = s.getTag();
		return t != null && t.contains(CURIO, Tag.TAG_COMPOUND);
	}

	/** Removes Compat's curio tag and the three cosmetic tags; an emptied tag is dropped so the stack stacks again. */
	public static ItemStack strip(ItemStack s) {
		CompoundTag t = s.getTag();
		if (t == null) return s;
		if (!t.contains(CURIO) && !t.contains(COS_ITEM) && !t.contains(COS_INDEX) && !t.contains(COS_SKIN)) return s;
		t.remove(CURIO);
		t.remove(COS_ITEM);
		t.remove(COS_INDEX);
		t.remove(COS_SKIN);
		if (t.isEmpty()) s.setTag(null);
		return s;
	}

	/** Strips the section tags from every stack in the player's inventory (after Transfer Items). */
	public static void sweep(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (!s.isEmpty()) strip(s);
		}
	}

	// ---- layout ----

	/** Which stacks of an additional-items list fill which section slot. Entries are the list's own stack objects. */
	public record Layout(ItemStack[] cosmetic, ItemStack[] curios) {
		public boolean holds(ItemStack s) {
			for (ItemStack c : cosmetic) if (c == s) return true;
			for (ItemStack c : curios) if (c == s) return true;
			return false;
		}
	}

	public static Layout layout(List<ItemStack> additional, int cap) {
		ItemStack[] cosmetic = new ItemStack[COSMETIC];
		ItemStack[] curios = new ItemStack[Math.max(0, cap)];
		if (cap < 0) return new Layout(cosmetic, curios);
		List<ItemStack> tagged = new ArrayList<>();
		for (ItemStack s : additional) {
			if (s.isEmpty()) continue;
			if (isCosmetic(s)) {
				int idx = s.getTag().getInt(COS_INDEX);
				if (idx >= 0 && idx < COSMETIC && cosmetic[idx] == null) cosmetic[idx] = s;
			} else if (isCurio(s)) {
				tagged.add(s);
			}
		}
		tagged.sort(Comparator.<ItemStack, String>comparing(s -> s.getTag().getCompound(CURIO).getString("Slot"))
				.thenComparing(s -> s.getTag().getCompound(CURIO).getBoolean("Cosmetic"))
				.thenComparingInt(s -> s.getTag().getCompound(CURIO).getInt("Index")));
		for (int i = 0; i < curios.length && i < tagged.size(); i++) curios[i] = tagged.get(i);
		return new Layout(cosmetic, curios);
	}

	/** True when no stack outside the sections is left in the list (the Additional Items checks). */
	public static boolean looseEmpty(CorpseEntity corpse) {
		int cap = capOf(corpse);
		List<ItemStack> list = corpse.getDeath().getAdditionalItems();
		if (cap < 0) return list.stream().allMatch(ItemStack::isEmpty);
		Layout l = layout(list, cap);
		for (ItemStack s : list) {
			if (!s.isEmpty() && !l.holds(s)) return false;
		}
		return true;
	}

	// ---- synced section tag (CorpseState#creativecompat$sections) ----

	/** Section curio slot count for this corpse, or -1 when the sections are off. Same value on both sides. */
	public static int capOf(CorpseEntity corpse) {
		if (!(corpse instanceof CorpseState s)) return -1;
		CompoundTag t = s.creativecompat$sections();
		return t.contains("Cap") ? t.getInt("Cap") : DEFAULT_CAP;
	}

	public static CompoundTag defaultTag() {
		CompoundTag t = new CompoundTag();
		t.putInt("Cap", DEFAULT_CAP);
		return t;
	}

	/** The tag the server syncs: the curio slot cap, the cosmetic stacks shown on the model (item 6) and the curios worn on it (round 2). */
	public static CompoundTag buildTag(CorpseEntity corpse) {
		CompoundTag t = new CompoundTag();
		int cap = !CompatConfig.SPEC.isLoaded() ? DEFAULT_CAP
				: CompatConfig.CORPSES.sectionedScreen.get() ? CompatConfig.CORPSES.curioCap.get() : -1;
		t.putInt("Cap", cap);
		Layout l = layout(corpse.getDeath().getAdditionalItems(), Math.max(cap, 0));
		ListTag cos = new ListTag();
		for (ItemStack s : l.cosmetic()) {
			CompoundTag c = new CompoundTag();
			if (s != null && !s.isEmpty()) {
				ItemStack shown = strip(s.copy());
				shown.save(c);
				c.putBoolean("Skin", s.getTag() != null && s.getTag().getBoolean(COS_SKIN));
			}
			cos.add(c);
		}
		t.put("Cosmetic", cos);
		// round 2: every tagged curio still in the corpse, for the model (not capped: the cap is the screen's)
		ListTag curios = new ListTag();
		for (ItemStack s : corpse.getDeath().getAdditionalItems()) {
			if (s.isEmpty() || !isCurio(s)) continue;
			CompoundTag c = s.getTag().getCompound(CURIO).copy();
			c.put("Item", strip(s.copy()).save(new CompoundTag()));
			curios.add(c);
		}
		if (!curios.isEmpty()) t.put("Curios", curios);
		return t;
	}

	// ---- menu side ----

	/** Server view of the section slots: fixed to the stack objects laid out when the menu opened. */
	public static final class View implements Container {
		private final NonNullList<ItemStack> list;
		private final ItemStack[] refs;

		public View(NonNullList<ItemStack> list, int cap) {
			this.list = list;
			Layout l = layout(list, cap);
			refs = new ItemStack[COSMETIC + l.curios().length];
			// slot k shows cosmetic index 3 - k: helmet leftmost, as the real armor
			for (int k = 0; k < COSMETIC; k++) refs[k] = l.cosmetic()[COSMETIC - 1 - k];
			System.arraycopy(l.curios(), 0, refs, COSMETIC, l.curios().length);
		}

		private int find(ItemStack ref) {
			if (ref == null) return -1;
			for (int i = 0; i < list.size(); i++) if (list.get(i) == ref) return i;
			return -1;
		}

		private ItemStack live(int slot) {
			if (slot < 0 || slot >= refs.length) return null;
			ItemStack ref = refs[slot];
			if (ref == null) return null;
			if (find(ref) < 0) {
				refs[slot] = null;
				return null;
			}
			return ref;
		}

		private void drop(int slot) {
			int at = find(refs[slot]);
			if (at >= 0) list.set(at, ItemStack.EMPTY);
			refs[slot] = null;
		}

		@Override
		public int getContainerSize() {
			return refs.length;
		}

		@Override
		public boolean isEmpty() {
			for (int i = 0; i < refs.length; i++) {
				ItemStack s = live(i);
				if (s != null && !s.isEmpty()) return false;
			}
			return true;
		}

		@Override
		public ItemStack getItem(int slot) {
			ItemStack s = live(slot);
			return s == null ? ItemStack.EMPTY : s;
		}

		@Override
		public ItemStack removeItem(int slot, int amount) {
			ItemStack s = live(slot);
			if (s == null || s.isEmpty() || amount <= 0) return ItemStack.EMPTY;
			ItemStack out = s.split(amount);
			if (s.isEmpty()) drop(slot);
			return strip(out);
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			ItemStack s = live(slot);
			if (s == null) return ItemStack.EMPTY;
			drop(slot);
			return strip(s);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			if (slot < 0 || slot >= refs.length) return;
			if (stack.isEmpty()) {
				if (live(slot) != null) drop(slot);
				return;
			}
			int at = find(live(slot));
			if (at >= 0) list.set(at, stack);
			else list.add(stack);
			refs[slot] = stack;
		}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}

		@Override
		public void clearContent() {}
	}

	/** The container behind the section slots: the live view on the server, a plain buffer for menu sync on the client. */
	public static Container container(CorpseEntity corpse, int cap) {
		if (corpse.level.isClientSide) return new SimpleContainer(COSMETIC + cap);
		return new View(corpse.getDeath().getAdditionalItems(), cap);
	}

	/**
	 * A take-only section slot. Cosmetic slots are always active (their frames are always drawn); a curio slot is active
	 * once it has held a stack this open, so the client lays the panel out from what the menu sync sends.
	 */
	public static class SectionSlot extends LockedSlot {
		private final boolean curio;
		private boolean seen;

		public SectionSlot(Container container, int index, int x, int y, boolean editable, boolean curio) {
			super(container, index, x, y, true, !editable);
			this.curio = curio;
		}

		public boolean isCurio() {
			return curio;
		}

		@Override
		public boolean isActive() {
			if (!curio) return true;
			if (!seen && !getItem().isEmpty()) seen = true;
			return seen;
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			strip(stack);
			super.onTake(player, stack);
		}

		@Override
		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
			if (curio) return null;
			return Pair.of(InventoryMenu.BLOCK_ATLAS, ARMOR_ICONS[getContainerSlot()]);
		}
	}

	private static final ResourceLocation[] ARMOR_ICONS = {
			InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
			InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};

	/** The corpse offhand slot moved to the end of the top row ({@link #OFFHAND_X}). */
	public static class OffhandSlot extends LockedSlot {
		public OffhandSlot(Container container, int index, int x, int y, boolean editable) {
			super(container, index, x, y, true, !editable);
		}

		@Override
		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
			return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
		}
	}

	/** Screen position of section slot {@code i} (0 to 3 cosmetic, then curios). */
	public static int slotX(int i) {
		if (i < COSMETIC) return COSMETIC_X + i * 18;
		return CURIO_X - ((i - COSMETIC) / COLUMN) * COLUMN_STEP;
	}

	public static int slotY(int i) {
		if (i < COSMETIC) return TOP_Y;
		return TOP_Y + ((i - COSMETIC) % COLUMN) * 18;
	}
}
