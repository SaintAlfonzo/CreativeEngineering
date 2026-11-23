// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@code [mixins]} table of creativecompat-common.toml: one enable key, default on, for each mixin that changes
 * vanilla behavior with no third-party mod to gate it (publish_readiness.md PR-CP-04; owner ruling P3, 2026-11-22).
 * Read twice: {@link CompatMixinPlugin} parses the file as text at mixin time (before Forge loads any config) and skips
 * a mixin whose key is false; {@code CompatConfig} declares the same keys so Forge writes them with their comments.
 * A change applies on the next game start. Plain Java only: the plugin loads this class before the game does.
 */
public final class MixinSwitches {

	/** Mixin simple name -> what it does (the key's comment). Order is the file's order. */
	public static final Map<String, String> MIXINS = new LinkedHashMap<>();

	static {
		put("VillagerGolemCooldownMixin", "Iron golem farm cooldown ([golems]).");
		put("BoatIceFrictionMixin", "Boat-only block friction ([boat_friction]).");
		put("BlockItemOwnStackFillMixin", "Sweet berries and glow berries listed in the creative tabs and JEI again.");
		put("StructureTemplateBotanistPotMixin", "Botanist workbenches placed by structures start with a flower (Beautify).");
		put("VineBlockShearMixin", "Sheared vines stop growing ([vine_shearing]).");
		put("InsideBlocksMixin", "Pass-through blocks: each entity's pass-through state and the movement hook "
				+ "([surface.passthrough]). Off: entities walk through as vanilla.");
		put("BlockStatePassMixin", "Pass-through blocks: the tier cached on each block state (off: looked up each time).");
		put("SnowLayerPassMixin", "Snow layers can be walked through.");
		put("SnowLayerFallMixin", "Snow and salt layers fall like sand.");
		put("SnowLayerReplaceMixin", "A placed block replaces snow layers up to [surface] replaceMaxLayers deep.");
		put("LeafPilePathMixin", "Mobs do not path through deep leaf piles.");
		put("LayerJumpMixin", "Weaker jumps inside deep snow and leaf layers.");
		put("SnowMobMixin", "Mobs sunk in snow layers climb out when they walk into a block.");
		put("SnowPathMixin", "Path searches of mobs sunk in snow layers start from the layer.");
		put("LevelChunkPassMixin", "Pass-through neighbor counts stored in chunks (off: counted each time).");
		put("MakeStuckInBlockMixin", "Pass-through blocks set their own slowdown instead of the cobweb one.");
		put("EntityPassMixin", "Pass-through state saved with entities; items stuck in webs picked up on touch.");
		put("LivingClimbMixin", "Climbing and hanging in surrounded webs.");
		put("ArrowPassMixin", "Arrows hang in pass-through webs and despawn on their own timer.");
		put("FireworkPassMixin", "Fireworks slowed in webs.");
		put("ProjectilePassMixin", "Projectiles in surrounded webs hit the web block.");
		put("FallingBlockWaterSplashMixin", "Falling blocks splash when they enter water (client).");
		put("EntitySinkRenderMixin", "Entities standing on sinking blocks drawn lower (client).");
		put("CameraSinkMixin", "The camera lowered on sinking blocks (client).");
		put("ScreenTooltipTopMixin", "A tooltip taller than the screen keeps its top on screen (client).");
		put("ContainerTitlePlaqueMixin", "GUI resource pack title plaques stretched to long titles ([gui_title], client).");
		put("ToastFilterMixin", "Tutorial and advancement toasts filtered ([toasts], client).");
		put("ScreenBackgroundDimMixin", "[screens] dimWorldBehindScreens (client).");
		put("RenderSystemShaderTextureMixin", "Finds a container screen's background for the title plaques (client).");
		put("GuiEffectIconMixin", "HUD effect icons for effects listed in Creative Common's [effects] forceHudIcon (client).");
		put("SignTextDistanceMixin", "Sign text not drawn far from the camera (client).");
		put("PassFovMixin", "Field of view ignores block walking speeds (client).");
	}

	private MixinSwitches() {}

	private static void put(String mixin, String comment) {
		MIXINS.put(mixin, comment);
	}

	/** The toml key of a mixin: its simple name without {@code Mixin}, first letter lower case. */
	public static String key(String mixin) {
		String base = mixin.endsWith("Mixin") ? mixin.substring(0, mixin.length() - 5) : mixin;
		return Character.toLowerCase(base.charAt(0)) + base.substring(1);
	}

	/**
	 * The mixins whose key is {@code false} in the {@code [mixins]} table of {@code file}; empty when the file or the
	 * table is missing (first start: every key is on). A minimal reader: {@code key = true|false} lines only.
	 */
	public static Set<String> off(Path file) {
		Set<String> off = new HashSet<>();
		List<String> lines;
		try {
			if (!Files.isRegularFile(file)) return off;
			lines = Files.readAllLines(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return off;
		}
		Map<String, String> byKey = new LinkedHashMap<>();
		MIXINS.keySet().forEach(m -> byKey.put(key(m), m));
		boolean in = false;
		for (String raw : lines) {
			String line = raw.strip();
			if (line.startsWith("[")) {
				in = line.equals("[mixins]");
				continue;
			}
			if (!in || line.isEmpty() || line.startsWith("#")) continue;
			int eq = line.indexOf('=');
			if (eq < 0) continue;
			String k = line.substring(0, eq).strip();
			String v = line.substring(eq + 1).strip();
			int hash = v.indexOf('#');
			if (hash >= 0) v = v.substring(0, hash).strip();
			String mixin = byKey.get(k);
			if (mixin != null && v.equals("false")) off.add(mixin);
		}
		return off;
	}
}
