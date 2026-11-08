package com.SaintAlfonzo.creativecommon.gravity;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.mixin.CreateGravityConfigAccessor;
import com.mojang.logging.LogUtils;
import de.cadentem.create_gravity.config.ServerConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

/**
 * Create: Gravity's biome gravity factor for any entity, on both sides (creative_engineering.md 7.4).
 *
 * Create: Gravity only lowers the gravity attribute of living entities. Items, falling blocks, TNT, XP orbs, arrows,
 * thrown projectiles, boats and minecarts each add a fixed pull per tick instead; Common's LowGravity* mixins scale that
 * pull by {@link #pullScale}. They run on the server AND the client: the client predicts these entities' motion itself
 * (and moves a boat its player steers), so when both sides use the same scale no extra motion packets are needed.
 *
 * Server: the mod's own lookup ({@code ServerConfig.getBiomeConfig}). Client: that lookup is empty on a dedicated client
 * (each entry needs the server object), so the client parses the mod's biome_configs strings itself. Forge sends the
 * server's copy of that SERVER config to every client at login, so both sides read the same list. Same rules as the
 * mod: "biome;oxygen;gravity", a leading # marks a biome tag, an empty gravity means -0.8, the first match wins, a
 * factor outside -1..0 is ignored.
 *
 * Dimension limit: with CommonConfig perf.gravityLimitDimensions on, entities outside perf.gravityDimensions skip the
 * lookup (vanilla pull). The client reads its own copy of Common's config; the pack ships the same file to both sides.
 */
public final class LowGravity {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** Create: Gravity's default gravity factor (ServerConfig.GRAVITY_FACTOR_DEFAULT, private). */
	private static final double DEFAULT_FACTOR = -0.8;

	private static List<? extends String> dimensionSource;
	private static Set<ResourceLocation> dimensions = Set.of();

	private static List<? extends String> clientSource;
	private static List<Entry> clientEntries = List.of();

	private record Entry(TagKey<Biome> tag, ResourceLocation id, double factor) {}

	private LowGravity() {}

	/**
	 * Multiplier for the entity's fixed vanilla pull at its position: 1 = vanilla, 0.2 = factor -0.8, 0 = factor -1.
	 */
	public static double pullScale(Entity entity) {
		Level level = entity.getLevel();
		if (!dimensionAllowed(level)) return 1.0;
		Holder<Biome> biome = level.getBiome(entity.blockPosition());
		double factor = level.isClientSide ? clientFactor(biome) : serverFactor(biome);
		if (factor >= 0.0 || factor < -1.0) return 1.0;
		return 1.0 + factor;
	}

	/** False when the dimension limit is on and this level is not in perf.gravityDimensions. */
	public static boolean dimensionAllowed(Level level) {
		if (!CommonConfig.PERF.gravityLimitDimensions.get()) return true;
		List<? extends String> list = CommonConfig.PERF.gravityDimensions.get();
		if (list != dimensionSource) {
			Set<ResourceLocation> set = new HashSet<>();
			for (String id : list) {
				ResourceLocation rl = ResourceLocation.tryParse(id);
				if (rl != null) set.add(rl);
			}
			dimensions = set;
			dimensionSource = list;
		}
		return dimensions.contains(level.dimension().location());
	}

	private static double serverFactor(Holder<Biome> biome) {
		ServerConfig.BiomeConfig config = ServerConfig.getBiomeConfig(biome);
		return config == null ? 0.0 : config.gravityFactor();
	}

	private static double clientFactor(Holder<Biome> biome) {
		List<? extends String> source = CreateGravityConfigAccessor.creativecommon$biomeConfigs().get();
		if (source != clientSource) {
			clientEntries = parse(source);
			clientSource = source;
			// one line per (re)parse: the scratch client rows (CQ-CGE-03) check that the synced list arrived
			LOGGER.info("[low gravity] client biome list: {} of {} entries {}", clientEntries.size(), source.size(), source);
		}
		for (Entry e : clientEntries) {
			if (e.tag != null ? biome.is(e.tag) : biome.is(e.id)) return e.factor;
		}
		return 0.0;
	}

	private static List<Entry> parse(List<? extends String> source) {
		List<Entry> out = new ArrayList<>();
		for (String raw : source) {
			String[] parts = raw.split(";", -1);
			if (parts.length == 0 || parts[0].isBlank()) continue;
			String name = parts[0].trim();
			double factor = DEFAULT_FACTOR;
			if (parts.length > 2 && !parts[2].isBlank()) {
				try {
					factor = Double.parseDouble(parts[2].trim());
				} catch (NumberFormatException ex) {
					continue;
				}
			}
			if (factor < -1.0 || factor > 0.0) continue;
			boolean isTag = name.startsWith("#");
			ResourceLocation rl = ResourceLocation.tryParse(isTag ? name.substring(1) : name);
			if (rl == null) continue;
			out.add(isTag ? new Entry(TagKey.create(Registry.BIOME_REGISTRY, rl), null, factor) : new Entry(null, rl, factor));
		}
		return List.copyOf(out);
	}
}
