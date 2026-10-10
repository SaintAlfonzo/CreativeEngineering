package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Calibration for the earth-fill limits: generates real vanilla structures on the vanilla overworld terrain (builtin
 * registries, no mods) at random sites inside their biomes, runs the EarthFill counting on each, and prints how the
 * water / air counts are distributed and what share each candidate limit pair would cancel.
 * Needs client-extra.jar (the vanilla templates) on the classpath. Args: [seed] [sites per structure].
 */
public class EarthFillSim {

	static final Map<String, Set<String>> SITES = new java.util.LinkedHashMap<>();
	static {
		SITES.put("minecraft:village_plains", Set.of("plains", "sunflower_plains", "meadow"));
		SITES.put("minecraft:village_desert", Set.of("desert"));
		SITES.put("minecraft:village_savanna", Set.of("savanna", "savanna_plateau", "windswept_savanna"));
		SITES.put("minecraft:village_snowy", Set.of("snowy_plains", "ice_spikes"));
		SITES.put("minecraft:village_taiga", Set.of("taiga", "snowy_taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga"));
		SITES.put("minecraft:pillager_outpost", Set.of("plains", "desert", "savanna", "snowy_plains", "taiga", "meadow", "grove", "snowy_slopes", "jagged_peaks", "frozen_peaks", "stony_peaks"));
		SITES.put("minecraft:igloo", Set.of("snowy_plains", "snowy_taiga", "snowy_slopes"));
		SITES.put("minecraft:mansion", Set.of("dark_forest"));
	}

	/** Serves the vanilla templates from the classpath (client-extra.jar); Forge's own vanilla pack needs a running FML. */
	static final class ClasspathResources implements ResourceManager {
		public Set<String> getNamespaces() { return Set.of("minecraft"); }
		public Optional<Resource> getResource(ResourceLocation id) {
			String path = "data/" + id.getNamespace() + "/" + id.getPath();
			if (EarthFillSim.class.getClassLoader().getResource(path) == null) return Optional.empty();
			return Optional.of(new Resource("classpath", () -> EarthFillSim.class.getClassLoader().getResourceAsStream(path)));
		}
		public List<Resource> getResourceStack(ResourceLocation id) { return getResource(id).map(List::of).orElse(List.of()); }
		public Map<ResourceLocation, Resource> listResources(String p, Predicate<ResourceLocation> f) { return Map.of(); }
		public Map<ResourceLocation, List<Resource>> listResourceStacks(String p, Predicate<ResourceLocation> f) { return Map.of(); }
		public Stream<PackResources> listPacks() { return Stream.empty(); }
	}

	static final int[][] LIMITS = {{0, 0}, {8, 32}, {24, 64}, {48, 128}, {48, 160}, {96, 256}, {200, 400}};

	public static void main(String[] args) throws Exception {
		long seed = args.length > 0 ? Long.parseLong(args[0]) : 4242L;
		int sites = args.length > 1 ? Integer.parseInt(args[1]) : 150;
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();

		TagKey<Block> earthTag = BlockTags.create(new ResourceLocation("creativeengineering", "earth_fill/earth"));
		Map<TagKey<Block>, List<Holder<Block>>> tags = new HashMap<>();
		List<Holder<Block>> earthBlocks = new ArrayList<>();
		for (Block b : new Block[]{Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.MOSS_BLOCK, Blocks.MUD,
				Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.CLAY, Blocks.DIRT_PATH}) earthBlocks.add(b.builtInRegistryHolder());
		tags.put(earthTag, earthBlocks);
		Registry.BLOCK.bindTags(tags);
		List<TagKey<Block>> earth = List.of(earthTag);

		RegistryAccess access = RegistryAccess.BUILTIN.get();
		Registry<Biome> biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
		MultiNoiseBiomeSource source = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes);
		Holder<NoiseGeneratorSettings> settings = access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);
		var noises = access.registryOrThrow(Registry.NOISE_REGISTRY);
		NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY), noises, source, settings);
		RandomState randomState = RandomState.create(settings.value(), noises, seed);
		LevelHeightAccessor height = LevelHeightAccessor.create(-64, 384);

		Path tmp = Files.createTempDirectory("earthfillsim");
		StructureTemplateManager templates = new StructureTemplateManager(new ClasspathResources(), LevelStorageSource.createDefault(tmp).createAccess("sim"), net.minecraft.util.datafix.DataFixers.getDataFixer());
		Registry<Structure> structures = access.registryOrThrow(Registry.STRUCTURE_REGISTRY);

		System.out.printf("%-28s %5s %6s | %-27s | %-27s | cancelled %% at (water,air) limits %s%n", "structure", "n", "earth", "over water p50/p90/p99/max", "over air p50/p90/p99/max", Arrays.deepToString(LIMITS));
		Random rnd = new Random(seed);
		for (Map.Entry<String, Set<String>> e : SITES.entrySet()) {
			Structure structure = structures.get(new ResourceLocation(e.getKey()));
			if (structure == null) { System.out.println(e.getKey() + ": not in the builtin registry"); continue; }
			List<int[]> res = new ArrayList<>();   // earth, water, air
			int tries = 0, noStart = 0;
			while (res.size() < sites && tries++ < sites * 400) {
				int cx = rnd.nextInt(8000) - 4000, cz = rnd.nextInt(8000) - 4000;
				Holder<Biome> b = source.getNoiseBiome(cx * 4 + 2, 16, cz * 4 + 2, randomState.sampler());
				String bname = b.unwrapKey().map(k -> k.location().getPath()).orElse("");
				if (!e.getValue().contains(bname)) continue;
				StructureStart start = structure.generate(access, generator, source, randomState, templates, seed, new ChunkPos(cx, cz), 0, height, h -> true);
				if (!start.isValid()) { noStart++; continue; }
				List<long[]> pieces = new ArrayList<>();
				for (StructurePiece p : start.getPieces()) pieces.add(EarthFill.pieceEarth(p, templates, earth, 3));
				EarthFill.Result r = EarthFill.count(pieces, EarthFill.predicted(generator, randomState, height, 2), new EarthFill.Limits(-1, -1, -1), false);
				res.add(new int[]{r.blocks(), r.water(), r.air()});
			}
			if (res.isEmpty()) { System.out.println(e.getKey() + ": no start in " + tries + " tries (" + noStart + " invalid)"); continue; }
			StringBuilder cancelled = new StringBuilder();
			for (int[] l : LIMITS) {
				int c = 0;
				for (int[] r : res) if (r[1] > l[0] || r[2] > l[1]) c++;
				cancelled.append(String.format("%4.0f", 100.0 * c / res.size()));
			}
			double meanEarth = res.stream().mapToInt(r -> r[0]).average().orElse(0);
			System.out.printf("%-28s %5d %6.0f | %-27s | %-27s | %s%n", e.getKey(), res.size(), meanEarth, pct(res, 1), pct(res, 2), cancelled);
		}
		System.exit(0);
	}

	static String pct(List<int[]> res, int col) {
		int[] v = res.stream().mapToInt(r -> r[col]).sorted().toArray();
		return String.format("%d/%d/%d/%d", v[v.length / 2], v[(int) (v.length * 0.9)], v[Math.min(v.length - 1, (int) (v.length * 0.99))], v[v.length - 1]);
	}
}
