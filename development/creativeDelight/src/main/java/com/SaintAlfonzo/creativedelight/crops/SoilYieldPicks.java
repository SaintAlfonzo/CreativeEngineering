package com.SaintAlfonzo.creativedelight.crops;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.SaintAlfonzo.creativedelight.CDConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Soil yield on own picks (feature_writeup/plan_crop_yields_wave2.md 4, crop_yields.md). A crop or bush picked by its
 * own right-click (or a fungus colony sheared) never rolls its loot table, so the soil pools of {@code soil_yield.js}
 * do nothing there. The pick mixins ({@code mixin.<modid>.*PickSoilYieldMixin}) hand this class the stack the pick is
 * about to drop, through {@code Block.popResource}; it adds the soil extra to that stack: bonus x base items as
 * binomial(ceil(extra), extra / ceil(extra)), the same rule as the loot pools. Fortune is never read.
 *
 * <p>Bases, the crop switch keys and the soil list come from {@code /creativedelight/soil_yield.json}, the table the
 * script also reads ({@link #lootBase}), so both paths use the same numbers. Bonuses and switches are CDConfig
 * {@code [crop_drops.soil_yield]}.
 */
public final class SoilYieldPicks {
	private static final Logger LOG = LogUtils.getLogger();
	private static final String TABLE = "/creativedelight/soil_yield.json";
	/** Most blocks the soil walk passes (tomato vines on ropes, propelplant canes). */
	private static final int MAX_WALK = 24;

	private record Pick(String key, double base, Set<ResourceLocation> below, Map<String, String> ripe) {}

	private static final Map<ResourceLocation, String> SOILS = new HashMap<>();
	private static final Map<String, Double> LOOT = new HashMap<>();
	private static final Map<ResourceLocation, Pick> PICKS = new HashMap<>();
	private static final java.util.List<String> FORTUNE_FREE = new java.util.ArrayList<>();

	static {
		try (InputStream in = SoilYieldPicks.class.getResourceAsStream(TABLE)) {
			if (in == null) throw new IllegalStateException("missing " + TABLE);
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("soils").entrySet()) {
				SOILS.put(new ResourceLocation(e.getKey()), e.getValue().getAsString());
			}
			for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("loot").entrySet()) {
				LOOT.put(e.getKey(), e.getValue().getAsDouble());
			}
			for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("picks").entrySet()) {
				JsonObject o = e.getValue().getAsJsonObject();
				Set<ResourceLocation> below = new HashSet<>();
				if (o.has("below")) o.getAsJsonArray("below").forEach(b -> below.add(new ResourceLocation(b.getAsString())));
				Map<String, String> ripe = new HashMap<>();
				if (o.has("ripe")) o.getAsJsonObject("ripe").entrySet().forEach(r -> ripe.put(r.getKey(), r.getValue().getAsString()));
				PICKS.put(new ResourceLocation(e.getKey()), new Pick(o.get("key").getAsString(), o.get("base").getAsDouble(), below, ripe));
			}
			if (root.has("fortune_free")) root.getAsJsonArray("fortune_free").forEach(t -> FORTUNE_FREE.add(t.getAsString()));
		} catch (Exception ex) {
			LOG.error("[soil-yield] could not read {}: own picks get no soil extra", TABLE, ex);
		}
	}

	private SoilYieldPicks() {}

	/** The loot path's base for a crop key (soil_yield.js), or NaN when the table has none. */
	public static double lootBase(String key) {
		Double v = LOOT.get(key);
		return v == null ? Double.NaN : v;
	}

	/** Every loot key in the table (soil_yield.js warns about a crop row without a base). */
	public static String[] lootKeys() {
		return LOOT.keySet().toArray(new String[0]);
	}

	/** Crop loot tables whose Fortune terms soil_yield.js strips (wave 2 part A). */
	public static String[] fortuneFree() {
		return FORTUNE_FREE.toArray(new String[0]);
	}

	/**
	 * The stack a pick at {@code pos} drops, with the soil extra added. Server side only; any block without a pick row,
	 * an unripe state, a crop switched off or a soil without a bonus returns the stack unchanged.
	 */
	public static ItemStack withSoilExtra(Level level, BlockPos pos, ItemStack stack) {
		if (level == null || level.isClientSide || stack.isEmpty()) return stack;
		BlockState state = level.getBlockState(pos);
		Pick pick = PICKS.get(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
		if (pick == null || !ripe(state, pick.ripe)) return stack;
		CDConfig.SoilYield cfg = CDConfig.CROP_DROPS.SOIL_YIELD;
		if (!CDConfig.SPEC.isLoaded() || !Boolean.TRUE.equals(cfg.cropOn(pick.key))) return stack;

		BlockPos bottom = pos;
		for (int i = 0; i < MAX_WALK && pick.below.contains(id(level, bottom.below())); i++) bottom = bottom.below();
		String soilKey = SOILS.get(id(level, bottom.below()));
		if (soilKey == null) return stack;
		double extra = cfg.bonus(soilKey) * pick.base;
		if (!(extra > 0)) return stack;

		int n = (int) Math.ceil(extra - 1e-9);
		double p = Math.min(1.0, extra / n);
		int add = 0;
		for (int i = 0; i < n; i++) if (level.random.nextDouble() < p) add++;
		if (add > 0) stack.grow(add);
		return stack;
	}

	private static ResourceLocation id(Level level, BlockPos pos) {
		return ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());
	}

	private static boolean ripe(BlockState state, Map<String, String> ripe) {
		Block block = state.getBlock();
		for (Map.Entry<String, String> r : ripe.entrySet()) {
			Property<?> prop = block.getStateDefinition().getProperty(r.getKey());
			if (prop == null || !r.getValue().equals(name(state, prop))) return false;
		}
		return true;
	}

	private static <T extends Comparable<T>> String name(BlockState state, Property<T> prop) {
		return prop.getName(state.getValue(prop));
	}
}
