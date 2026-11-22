package com.SaintAlfonzo.solraspberrypie.ladder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * One rung of the ladder (3). Kinds: HEALTH (amount = health points, 2 = one heart), ATTRIBUTE (id = attribute,
 * amount added), FLAT (id = effect, level), SLOT (index set by the ladder in threshold order), UNLOCK (id = effect,
 * level; never lost, driven by max diversity). FLAT and UNLOCK rungs may name a hover tagline (a lang key, 3.3 format
 * fields).
 */
public final class Rung {
	public enum Kind { HEALTH, ATTRIBUTE, FLAT, SLOT, UNLOCK }

	public final Kind kind;
	public final double threshold;
	public final ResourceLocation id;
	public final double amount;
	public final int level;
	/** FLAT and UNLOCK: lang key of the short hover tagline shown in the effect screens, or null (none). */
	public final String tagline;
	/** SLOT only: 0-based slot number. */
	public int slotIndex = -1;
	/** Position in the ladder (sorted); part of the key so two equal rungs stay distinct. */
	public int index;

	public Rung(Kind kind, double threshold, ResourceLocation id, double amount, int level) {
		this(kind, threshold, id, amount, level, null);
	}

	public Rung(Kind kind, double threshold, ResourceLocation id, double amount, int level, String tagline) {
		this.kind = kind;
		this.threshold = threshold;
		this.id = id;
		this.amount = amount;
		this.level = level;
		this.tagline = tagline == null || tagline.isBlank() ? null : tagline;
	}

	/** Stable key for gain/loss diffing within one ladder. */
	public String key() {
		return kind.name().charAt(0) + ":" + index;
	}

	void write(FriendlyByteBuf buf) {
		buf.writeEnum(kind);
		buf.writeDouble(threshold);
		buf.writeBoolean(id != null);
		if (id != null) buf.writeResourceLocation(id);
		buf.writeDouble(amount);
		buf.writeVarInt(level);
		buf.writeBoolean(tagline != null);
		if (tagline != null) buf.writeUtf(tagline);
	}

	static Rung read(FriendlyByteBuf buf) {
		Kind kind = buf.readEnum(Kind.class);
		double threshold = buf.readDouble();
		ResourceLocation id = buf.readBoolean() ? buf.readResourceLocation() : null;
		double amount = buf.readDouble();
		int level = buf.readVarInt();
		String tagline = buf.readBoolean() ? buf.readUtf() : null;
		return new Rung(kind, threshold, id, amount, level, tagline);
	}

	@Override
	public String toString() {
		return kind + "@" + threshold + (id != null ? " " + id : "") + (level > 0 ? " L" + level : "")
				+ (amount != 0 ? " " + amount : "");
	}
}
