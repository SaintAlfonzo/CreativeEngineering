// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraft.util.StringRepresentable;

/** The four aging stages, in vanilla copper order. */
public enum OxidationStage implements StringRepresentable {
	UNAFFECTED("unaffected"),
	EXPOSED("exposed"),
	WEATHERED("weathered"),
	OXIDIZED("oxidized");

	private final String serializedName;

	OxidationStage(String serializedName) {
		this.serializedName = serializedName;
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}

	public OxidationStage next() {
		return values()[Math.min(ordinal() + 1, values().length - 1)];
	}

	public OxidationStage previous() {
		return values()[Math.max(ordinal() - 1, 0)];
	}

	/** The stage with this serialized name, or null when there is none. */
	public static OxidationStage byName(String name) {
		for (OxidationStage stage : values()) {
			if (stage.serializedName.equals(name)) return stage;
		}
		return null;
	}

	/** As {@link #byName}, UNAFFECTED for an unknown name (upstream fromSerializedName). */
	public static OxidationStage fromSerializedName(String name) {
		OxidationStage stage = byName(name);
		return stage == null ? UNAFFECTED : stage;
	}
}
