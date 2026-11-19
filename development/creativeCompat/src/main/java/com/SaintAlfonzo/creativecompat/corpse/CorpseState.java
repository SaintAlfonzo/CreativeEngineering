package com.SaintAlfonzo.creativecompat.corpse;

/**
 * Compat's extra corpse state, added to the Corpse mod's {@code CorpseEntity} by {@code mixin/CorpseDecayMixin}. The
 * flags are synced to clients (the renderer and the name read them). Other jars (Creative Loot's ancient corpses) set
 * the state through the entity NBT compound {@link CorpseDecay#TAG}, so they need no compile dependency on Compat:
 * {@code Ancient} (byte 1: skeleton from the start, named "Ancient Corpse"), {@code Remains} (byte 1: never decays or
 * despawns, breakable, named "Ancient Remains").
 */
public interface CorpseState {
	byte FLAG_WITHERED = 1;
	byte FLAG_ANCIENT = 2;
	byte FLAG_REMAINS = 4;

	byte creativecompat$corpseFlags();

	default boolean creativecompat$has(byte flag) {
		return (creativecompat$corpseFlags() & flag) != 0;
	}
}
