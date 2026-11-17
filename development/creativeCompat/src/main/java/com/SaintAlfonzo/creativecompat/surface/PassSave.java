package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

/**
 * The pass-through state that must survive a save (plan 4.2 "persisted in its save data", 5.4 "flag saved in the
 * arrow's data"): a stuck item or orb's cell, point and earlier NoGravity, and an arrow's demotion to vanilla's
 * minute. Written into the entity's own tag under {@value #KEY} only when there is something to keep
 * ({@code mixin/EntityPassMixin}: RETURN of {@code Entity#saveWithoutId}, TAIL of {@code Entity#load}).
 */
public final class PassSave {

	private PassSave() {}

	public static final String KEY = "CreativeCompatPass";

	public static void save(Entity e, CompoundTag tag) {
		PassState st = PassThrough.state(e, false);
		if (st == null || !st.needsSave()) return;
		CompoundTag t = new CompoundTag();
		if (st.itemStuck) {
			t.putLong("StuckCell", st.stuckCell);
			t.putDouble("StuckX", st.stuckX);
			t.putDouble("StuckY", st.stuckY);
			t.putDouble("StuckZ", st.stuckZ);
			t.putBoolean("PriorNoGravity", st.priorNoGravity);
		}
		if (st.shortLife) t.putBoolean("ShortLife", true);
		tag.put(KEY, t);
	}

	public static void load(Entity e, CompoundTag tag) {
		if (!tag.contains(KEY, Tag.TAG_COMPOUND)) return;
		CompoundTag t = tag.getCompound(KEY);
		PassState st = PassThrough.state(e, true);
		if (t.contains("StuckCell", Tag.TAG_LONG)) {
			st.itemStuck = true;
			st.stuckCell = t.getLong("StuckCell");
			st.stuckX = t.getDouble("StuckX");
			st.stuckY = t.getDouble("StuckY");
			st.stuckZ = t.getDouble("StuckZ");
			st.priorNoGravity = t.getBoolean("PriorNoGravity");
		}
		st.shortLife = t.getBoolean("ShortLife");
	}
}
