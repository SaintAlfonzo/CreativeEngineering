package com.SaintAlfonzo.creativecompat.corpse;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import de.maxhenkel.corpse.corelib.death.Death;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Corpse mod decay on world time (feature_writeup/plan_corpses.md 3.1; rulings 1 to 4, 18, 19, 23). The Corpse mod
 * counts a private {@code age} while the chunk ticks and turns a corpse into a skeleton at {@code skeleton_time};
 * {@code mixin/CorpseDecayMixin} switches that off and asks {@link #target} once a second instead, comparing the game
 * time with the stored death and emptied times, so an unloaded corpse catches up when its chunk loads.
 *
 * <p>Stages: {@link #PLAYER} (player model), {@link #SKELETON}, {@link #WITHERED} (wither skeleton look, Nether rules
 * only). A stage never goes back. Three dimension kinds: normal, Nether ({@code netherDimensions}) and no-decay
 * ({@code noDecayDimensions}, the End). Config: {@code [corpses]} in creativecompat-common.toml.
 */
public final class CorpseDecay {
	public static final int PLAYER = 0;
	public static final int SKELETON = 1;
	public static final int WITHERED = 2;

	public enum Kind { NORMAL, NETHER, NO_DECAY }

	/** The NBT compound on the corpse entity holding Compat's corpse state. */
	public static final String TAG = "creativecompat:corpse";

	private CorpseDecay() {}

	/** Timer values in ticks; {@link #fromConfig} or the defaults in plan_corpses.md for tests. */
	public record Times(long skeleton, long emptySkeleton, long emptyDespawn, long netherSkeleton, long netherWither,
			long netherEmptySkeleton, long netherEmptyWither, long forceDespawn) {

		public static final Times DEFAULTS = new Times(168000, 600, 6000, 24000, 24000, 300, 600, -1);

		public static Times fromConfig(int forceDespawn) {
			CompatConfig.Corpses c = CompatConfig.CORPSES;
			return new Times(c.skeletonTime.get(), c.emptySkeletonTime.get(), c.emptyDespawnTime.get(), c.netherSkeletonTime.get(),
					c.netherWitherTime.get(), c.netherEmptySkeletonTime.get(), c.netherEmptyWitherTime.get(), forceDespawn);
		}
	}

	/** {@code target} = the stage the corpse should be at now; {@code remove} = the corpse is due to go. */
	public record Result(int target, boolean remove) {}

	/**
	 * The stage a corpse should have and whether it is due for removal.
	 *
	 * @param now       game time
	 * @param deathTime game time of death (or of the first check, for corpses from before Compat)
	 * @param emptiedAt game time it became empty, or -1 while it holds items
	 */
	public static Result target(Kind kind, Times t, long now, long deathTime, long emptiedAt) {
		long sinceDeath = now - deathTime;
		if (t.forceDespawn() > 0 && sinceDeath > t.forceDespawn()) return new Result(PLAYER, true);
		int stage = PLAYER;
		switch (kind) {
			case NORMAL -> {
				if (sinceDeath >= t.skeleton()) stage = SKELETON;
			}
			case NETHER -> {
				if (sinceDeath >= t.netherSkeleton()) stage = SKELETON;
				if (sinceDeath >= t.netherSkeleton() + t.netherWither()) stage = WITHERED;
			}
			case NO_DECAY -> { }
		}
		if (emptiedAt < 0) return new Result(stage, false);
		long sinceEmpty = now - emptiedAt;
		switch (kind) {
			case NORMAL -> {
				if (sinceEmpty >= t.emptySkeleton()) stage = Math.max(stage, SKELETON);
			}
			case NETHER -> {
				if (sinceEmpty >= t.netherEmptySkeleton()) stage = Math.max(stage, SKELETON);
				if (sinceEmpty >= t.netherEmptySkeleton() + t.netherEmptyWither()) stage = WITHERED;
			}
			case NO_DECAY -> { }
		}
		return new Result(stage, sinceEmpty >= t.emptyDespawn());
	}

	/** The dimension kind for a dimension id ({@code minecraft:the_nether}). */
	public static Kind kind(String dimension) {
		if (!CompatConfig.SPEC.isLoaded()) return Kind.NORMAL;
		if (CompatConfig.CORPSES.noDecayDimensions.get().contains(dimension)) return Kind.NO_DECAY;
		if (CompatConfig.CORPSES.netherDimensions.get().contains(dimension)) return Kind.NETHER;
		return Kind.NORMAL;
	}

	/**
	 * What the corpse model wears (ruling 4): armor and offhand from what is still in those corpse slots, the main hand
	 * from the death snapshot while an equal stack is still in the main inventory. Slots in {@link EquipmentSlot} order.
	 * Copies: the corpse menu shrinks the stacks it hands out in place, which a shared reference would hide.
	 */
	public static NonNullList<ItemStack> shown(Death death) {
		NonNullList<ItemStack> out = NonNullList.withSize(EquipmentSlot.values().length, ItemStack.EMPTY);
		NonNullList<ItemStack> armor = death.getArmorInventory();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.ARMOR && slot.getIndex() < armor.size()) {
				out.set(slot.ordinal(), armor.get(slot.getIndex()).copy());
			}
		}
		NonNullList<ItemStack> offhand = death.getOffHandInventory();
		if (!offhand.isEmpty()) out.set(EquipmentSlot.OFFHAND.ordinal(), offhand.get(0).copy());
		NonNullList<ItemStack> snapshot = death.getEquipment();
		ItemStack held = snapshot.size() > EquipmentSlot.MAINHAND.ordinal() ? snapshot.get(EquipmentSlot.MAINHAND.ordinal()) : ItemStack.EMPTY;
		if (!held.isEmpty()) {
			for (ItemStack s : death.getMainInventory()) {
				if (ItemStack.isSameItemSameTags(s, held)) {
					out.set(EquipmentSlot.MAINHAND.ordinal(), s.copy());
					break;
				}
			}
		}
		return out;
	}

	/** True when both lists show the same stacks (item, count, tag) slot by slot. */
	public static boolean sameShown(NonNullList<ItemStack> a, NonNullList<ItemStack> b) {
		if (a.size() != b.size()) return false;
		for (int i = 0; i < a.size(); i++) {
			if (!ItemStack.matches(a.get(i), b.get(i))) return false;
		}
		return true;
	}
}
