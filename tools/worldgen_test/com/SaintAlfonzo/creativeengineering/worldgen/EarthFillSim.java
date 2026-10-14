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

		if (args.length > 2 && args[2].equals("beard")) {
			beardReach(access, generator, source, randomState, templates, structures, height, earth, seed, sites);
			System.exit(0);
		}

		System.out.printf("%-28s %5s %6s | %-27s | %-27s | cancelled %% at (water,air) limits %s | after beard: water %-19s | air %-19s | mound %-13s | sizes s/m/l/h | cancelled %% (by water/air/mound) | pruned starts %% / pieces dropped / cancelled after %% / unlinked pieces%n",
				"structure", "n", "earth", "over water p50/p90/p99/max", "over air p50/p90/p99/max", Arrays.deepToString(LIMITS), "p50/p90/p99/max", "p50/p90/p99/max", "p50/p90/p99/max");
		Random rnd = new Random(seed);
		for (Map.Entry<String, Set<String>> e : SITES.entrySet()) {
			Structure structure = structures.get(new ResourceLocation(e.getKey()));
			if (structure == null) { System.out.println(e.getKey() + ": not in the builtin registry"); continue; }
			List<int[]> res = new ArrayList<>();   // earth, water, air, then after the beard: air, mound, size, cancelled at the default sizes
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
				EarthFill.Terrain terrain = EarthFill.predicted(generator, randomState, height, 2);
				// pruning, as for structures in earth_fill/prune (the villages)
				List<StructurePiece> all = start.getPieces();
				List<net.minecraft.world.level.levelgen.structure.BoundingBox> boxes = new ArrayList<>();
				List<List<EarthFill.Joint>> joints = new ArrayList<>();
				for (StructurePiece p : all) {
					boxes.add(p.getBoundingBox());
					List<EarthFill.Joint> js = new ArrayList<>();
					if (p instanceof PoolElementStructurePiece pool) for (var j : pool.getJunctions()) js.add(new EarthFill.Joint(j.getSourceX(), j.getSourceZ(), j.getDeltaY()));
					joints.add(js);
				}
				int[] parentOf = EarthFill.links(boxes, joints)[0];
				int unlinked = 0;
				for (int i = 1; i < all.size(); i++) if (parentOf[i] < 0) unlinked++;
				List<StructurePiece> kept = EarthFill.pruned(all, p -> EarthFill.pieceEarth(p, templates, earth, 3), EarthFill.beardOf(structure), terrain, new ArrayList<>());
				List<EarthFill.EarthPiece> keptPieces = new ArrayList<>();
				for (StructurePiece p : kept) {
					long[] pe = EarthFill.pieceEarth(p, templates, earth, 3);
					if (pe.length > 0) keptPieces.add(new EarthFill.EarthPiece(pe, p.getBoundingBox().minY(), EarthFill.beardOf(structure)));
				}
				int keptSize = EarthFill.sizeOf(EarthFill.earthBlocks(keptPieces), WorldgenConfig.earthSizeBounds());
				List<? extends Integer> keptLimits = WorldgenConfig.earthSizeLimits().get(keptSize);
				boolean keptCancelled = !keptPieces.isEmpty() && EarthFill.countEarth(keptPieces, terrain,
						new EarthFill.EarthLimits(WorldgenConfig.earthMaxOverWater(), keptLimits.get(0), keptLimits.get(1)), EarthFill.reach(), false).cancelled();
				EarthFill.Result r = EarthFill.count(pieces, terrain, new EarthFill.Limits(-1, -1, -1), false);
				List<EarthFill.EarthPiece> earthPieces = new ArrayList<>();
				EarthFill.Beard beard = EarthFill.beardOf(structure);
				for (StructurePiece p : start.getPieces()) {
					long[] pe = EarthFill.pieceEarth(p, templates, earth, 3);
					if (pe.length > 0) earthPieces.add(new EarthFill.EarthPiece(pe, p.getBoundingBox().minY(), beard));
				}
				int size = EarthFill.sizeOf(EarthFill.earthBlocks(earthPieces), WorldgenConfig.earthSizeBounds());
				List<? extends Integer> sized = WorldgenConfig.earthSizeLimits().get(size);
				EarthFill.EarthResult er = EarthFill.countEarth(earthPieces, terrain, new EarthFill.EarthLimits(WorldgenConfig.earthMaxOverWater(), sized.get(0), sized.get(1)),
						EarthFill.reach(), false);
				res.add(new int[]{r.blocks(), r.water(), r.air(), er.air(), er.mound(), size, er.cancelled() ? 1 : 0, er.water(),
						er.water() > WorldgenConfig.earthMaxOverWater() ? 1 : 0, sized.get(0) >= 0 && er.air() > sized.get(0) ? 1 : 0, sized.get(1) >= 0 && er.mound() > sized.get(1) ? 1 : 0,
						kept.size() < all.size() ? 1 : 0, all.size() - kept.size(), keptCancelled ? 1 : 0, unlinked});
			}
			if (res.isEmpty()) { System.out.println(e.getKey() + ": no start in " + tries + " tries (" + noStart + " invalid)"); continue; }
			StringBuilder cancelled = new StringBuilder();
			for (int[] l : LIMITS) {
				int c = 0;
				for (int[] r : res) if (r[1] > l[0] || r[2] > l[1]) c++;
				cancelled.append(String.format("%4.0f", 100.0 * c / res.size()));
			}
			double meanEarth = res.stream().mapToInt(r -> r[0]).average().orElse(0);
			int[] sizes = new int[4];
			for (int[] r : res) sizes[r[5]]++;
			System.out.printf("%-28s %5d %6.0f | %-27s | %-27s | %s | %-32s | %-23s | %-19s | %d/%d/%d/%d | %.0f (%.0f/%.0f/%.0f) | %.0f / %.1f / %.0f / %d%n", e.getKey(), res.size(), meanEarth, pct(res, 1), pct(res, 2), cancelled,
					pct(res, 7), pct(res, 3), pct(res, 4), sizes[0], sizes[1], sizes[2], sizes[3], 100.0 * res.stream().filter(r -> r[6] == 1).count() / res.size(),
					100.0 * res.stream().filter(r -> r[8] == 1).count() / res.size(), 100.0 * res.stream().filter(r -> r[9] == 1).count() / res.size(),
					100.0 * res.stream().filter(r -> r[10] == 1).count() / res.size(),
					100.0 * res.stream().filter(r -> r[11] == 1).count() / res.size(), res.stream().filter(r -> r[11] == 1).mapToInt(r -> r[12]).average().orElse(0),
					100.0 * res.stream().filter(r -> r[13] == 1).count() / res.size(), res.stream().mapToInt(r -> r[14]).sum());
		}
		System.exit(0);
	}

	/**
	 * "beard" mode: how deep under a piece vanilla's beard still fills the ground (CEConfig [worldgen.earth_fill]
	 * beardReach). For every template earth block over noise-terrain air in real village starts, the gap down to the
	 * terrain is measured (d = air blocks between the terrain and the piece's bottom layer) and checked after the
	 * beard: noise density + Beardifier > 0 in every block of the gap. Each start is bearded three ways (beard_thin,
	 * beard_box, bury), with vanilla's own Beardifier and the same pieces. Prints the filled share by d.
	 */
	static void beardReach(RegistryAccess access, NoiseBasedChunkGenerator generator, MultiNoiseBiomeSource source, RandomState randomState,
			StructureTemplateManager templates, Registry<Structure> structures, LevelHeightAccessor height, List<TagKey<Block>> earth, long seed, int sites) {
		net.minecraft.world.level.levelgen.DensityFunction noise = randomState.router().finalDensity();
		net.minecraft.world.level.levelgen.structure.TerrainAdjustment[] modes = {
				net.minecraft.world.level.levelgen.structure.TerrainAdjustment.BEARD_THIN,
				net.minecraft.world.level.levelgen.structure.TerrainAdjustment.BEARD_BOX,
				net.minecraft.world.level.levelgen.structure.TerrainAdjustment.BURY};
		int maxD = 24, sea = 63;
		long[][] total = new long[modes.length][maxD + 2], filled = new long[modes.length][maxD + 2];
		Random rnd = new Random(seed);
		int starts = 0;
		for (Map.Entry<String, Set<String>> e : SITES.entrySet()) {
			if (!e.getKey().startsWith("minecraft:village_")) continue;
			Structure structure = structures.get(new ResourceLocation(e.getKey()));
			int got = 0, tries = 0;
			while (got < sites && tries++ < sites * 400) {
				int cx = rnd.nextInt(8000) - 4000, cz = rnd.nextInt(8000) - 4000;
				Holder<Biome> b = source.getNoiseBiome(cx * 4 + 2, 16, cz * 4 + 2, randomState.sampler());
				if (!e.getValue().contains(b.unwrapKey().map(k -> k.location().getPath()).orElse(""))) continue;
				StructureStart start = structure.generate(access, generator, source, randomState, templates, seed, new ChunkPos(cx, cz), 0, height, h -> true);
				if (!start.isValid()) continue;
				got++;
				starts++;
				for (int m = 0; m < modes.length; m++) {
					it.unimi.dsi.fastutil.objects.ObjectArrayList<net.minecraft.world.level.levelgen.Beardifier.Rigid> rigids = new it.unimi.dsi.fastutil.objects.ObjectArrayList<>();
					it.unimi.dsi.fastutil.objects.ObjectArrayList<net.minecraft.world.level.levelgen.structure.pools.JigsawJunction> junctions = new it.unimi.dsi.fastutil.objects.ObjectArrayList<>();
					for (StructurePiece p : start.getPieces()) {
						if (p instanceof PoolElementStructurePiece pool) {
							if (pool.getElement().getProjection() == net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID) {
								rigids.add(new net.minecraft.world.level.levelgen.Beardifier.Rigid(pool.getBoundingBox(), modes[m], pool.getGroundLevelDelta()));
							}
							junctions.addAll(pool.getJunctions());
						} else {
							rigids.add(new net.minecraft.world.level.levelgen.Beardifier.Rigid(p.getBoundingBox(), modes[m], 0));
						}
					}
					net.minecraft.world.level.levelgen.Beardifier beard = new net.minecraft.world.level.levelgen.Beardifier(rigids.listIterator(), junctions.listIterator());
					it.unimi.dsi.fastutil.longs.LongOpenHashSet seen = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
					for (StructurePiece p : start.getPieces()) {
						int bottom = p.getBoundingBox().minY();
						for (long l : EarthFill.pieceEarth(p, templates, earth, 3)) {
							if (!seen.add(l)) continue;
							int x = net.minecraft.core.BlockPos.getX(l), y = net.minecraft.core.BlockPos.getY(l), z = net.minecraft.core.BlockPos.getZ(l);
							if (density(noise, x, y, z) > 0) continue;   // on the ground already
							int floor = y - 1;
							while (floor > y - 40 && floor >= sea && density(noise, x, floor, z) <= 0) floor--;
							if (floor < sea || floor <= y - 40) continue;   // water or a cave below: not this measure
							int d = Math.max(0, bottom - 1 - floor);
							boolean ok = true;
							for (int yy = floor + 1; yy < Math.min(y, bottom) && ok; yy++) {
								ok = density(noise, x, yy, z) + beard.compute(new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, yy, z)) > 0;
							}
							int bin = Math.min(d, maxD + 1);
							total[m][bin]++;
							if (ok) filled[m][bin]++;
						}
					}
				}
			}
		}
		System.out.println("Beard reach: " + starts + " village starts, earth blocks over noise air; filled % by d (air blocks between the terrain and the piece's bottom)");
		StringBuilder head = new StringBuilder(String.format("%-11s", "d"));
		for (int d = 0; d <= maxD + 1; d++) head.append(String.format("%6s", d > maxD ? ">" + maxD : String.valueOf(d)));
		System.out.println(head);
		for (int m = 0; m < modes.length; m++) {
			StringBuilder n = new StringBuilder(String.format("%-11s", "n"));
			StringBuilder f = new StringBuilder(String.format("%-11s", modes[m].getSerializedName()));
			for (int d = 0; d <= maxD + 1; d++) {
				n.append(String.format("%6d", total[m][d]));
				f.append(total[m][d] == 0 ? "     -" : String.format("%6.0f", 100.0 * filled[m][d] / total[m][d]));
			}
			System.out.println(f);
			if (m == 0) System.out.println(n);
		}
	}

	static double density(net.minecraft.world.level.levelgen.DensityFunction f, int x, int y, int z) {
		return f.compute(new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, y, z));
	}

	static String pct(List<int[]> res, int col) {
		int[] v = res.stream().mapToInt(r -> r[col]).sorted().toArray();
		return String.format("%d/%d/%d/%d", v[v.length / 2], v[(int) (v.length * 0.9)], v[Math.min(v.length - 1, (int) (v.length * 0.99))], v[v.length - 1]);
	}
}
