package com.SaintAlfonzo.creativecompat.corpse;

/**
 * Compat's extra corpse state, added to the Corpse mod's {@code CorpseEntity} by {@code mixin/CorpseDecayMixin}. The
 * flags are synced to clients (the renderer and the name read them). Other jars (Creative Loot's ancient corpses) set
 * the state through the entity NBT compound {@link CorpseDecay#TAG}, so they need no compile dependency on Compat:
 * {@code Ancient} (byte 1: skeleton from the start, named "Ancient Corpse"), {@code Remains} (byte 1: never decays or
 * despawns, breakable, named "Ancient Remains"). A second synced value carries the corpse screen sections
 * ({@link #creativecompat$sections}).
 */
public interface CorpseState {
	byte FLAG_WITHERED = 1;
	byte FLAG_ANCIENT = 2;
	byte FLAG_REMAINS = 4;

	byte creativecompat$corpseFlags();

	/**
	 * The synced corpse section tag (corpse/CorpseSections): {@code Cap} (curio slot count, -1 = sections off) and
	 * {@code Cosmetic} (4 stack compounds, feet to head, each with a {@code Skin} flag) for the model.
	 */
	net.minecraft.nbt.CompoundTag creativecompat$sections();

	default boolean creativecompat$has(byte flag) {
		return (creativecompat$corpseFlags() & flag) != 0;
	}

	/**
	 * "Ancient Remains" / "Ancient Corpse" for a corpse with the Remains / Ancient flag, else null. Used for both the
	 * display name (kill replies, chat) and the plain name (WTHIT and other tooltips; creative-compat-publish #22).
	 */
	default net.minecraft.network.chat.Component creativecompat$ancientName() {
		if (creativecompat$has(FLAG_REMAINS)) return net.minecraft.network.chat.Component.translatable("entity.creativecompat.ancient_remains");
		if (creativecompat$has(FLAG_ANCIENT)) return net.minecraft.network.chat.Component.translatable("entity.creativecompat.ancient_corpse");
		return null;
	}
}
