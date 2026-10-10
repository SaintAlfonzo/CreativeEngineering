package com.SaintAlfonzo.creativeengineering.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Runs tools/mob_spawns_test/cases.json through the real SpawnTuning.apply on vanilla spawn lists (built with
 * BiomeDefaultFeatures after Bootstrap) and prints, per case, the spawners before, after, and the change lines, plus
 * each rule mob's own category. tools/test_mob_spawns.sh feeds this to gen_spawn_table.apply_rule for parity.
 * Usage: SpawnTuningParity <cases.json> <out.json>
 */
public class SpawnTuningParity {

	static Map<String, Consumer<MobSpawnSettings.Builder>> BIOMES = new LinkedHashMap<>();
	static {
		BIOMES.put("plains", BiomeDefaultFeatures::plainsSpawns);
		BIOMES.put("desert", BiomeDefaultFeatures::desertSpawns);
		BIOMES.put("snowy", BiomeDefaultFeatures::snowySpawns);
		BIOMES.put("ocean", b -> BiomeDefaultFeatures.oceanSpawns(b, 3, 4, 10));
		BIOMES.put("mooshroom", b -> { BiomeDefaultFeatures.mooshroomSpawns(b); b.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 2, 1, 1)); });
		BIOMES.put("jungle", BiomeDefaultFeatures::baseJungleSpawns);
		BIOMES.put("plains_dup", b -> { BiomeDefaultFeatures.plainsSpawns(b); b.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 3, 1, 2)); });
	}

	public static void main(String[] a) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject in = gson.fromJson(Files.readString(Path.of(a[0])), JsonObject.class);
		Map<String, Object> out = new LinkedHashMap<>();
		Map<String, String> cats = new LinkedHashMap<>();
		int n = 0;
		for (JsonElement ce : in.getAsJsonArray("cases")) {
			JsonObject c = ce.getAsJsonObject();
			MobSpawnSettings.Builder vanilla = new MobSpawnSettings.Builder();
			BIOMES.get(c.get("biome").getAsString()).accept(vanilla);
			MobSpawnSettingsBuilder spawns = new MobSpawnSettingsBuilder(vanilla.build());
			List<SpawnTuning.Entry> before = SpawnTuning.snapshot(spawns);
			List<String> changes = new ArrayList<>();
			for (JsonElement re : c.getAsJsonArray("rules")) {
				JsonObject r = re.getAsJsonObject();
				ResourceLocation mob = new ResourceLocation(r.get("mob").getAsString());
				EntityType<?> type = Registry.ENTITY_TYPE.getOptional(mob).orElseThrow();
				cats.put(mob.toString(), type.getCategory().getName());
				SpawnTuning.Rule rule = new SpawnTuning.Rule("r" + (++n), HolderSet.direct(), mob,
						SpawnTuning.Action.valueOf(r.get("action").getAsString().toUpperCase()),
						r.has("category") ? Optional.of(MobCategory.byName(r.get("category").getAsString())) : Optional.empty(),
						r.has("weight") ? r.get("weight").getAsInt() : 0,
						r.has("factor") ? r.get("factor").getAsDouble() : 1.0,
						r.has("min") ? Optional.of(r.get("min").getAsInt()) : Optional.empty(),
						r.has("max") ? Optional.of(r.get("max").getAsInt()) : Optional.empty());
				String line = SpawnTuning.apply(spawns, rule, type);
				changes.add(line);
			}
			Map<String, Object> res = new LinkedHashMap<>();
			res.put("before", before);
			res.put("after", SpawnTuning.snapshot(spawns));
			res.put("changes", changes);
			out.put(c.get("name").getAsString(), res);
		}
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("categories", cats);
		root.put("cases", out);
		Files.writeString(Path.of(a[1]), new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(root));
		System.out.println(out.size() + " cases run");
	}
}
