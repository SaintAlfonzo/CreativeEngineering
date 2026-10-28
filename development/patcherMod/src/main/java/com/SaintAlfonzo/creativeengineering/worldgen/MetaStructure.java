package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Meta structures: one structure made of other structures and placed features. An optional anchor (a village)
 * spawns at the start chunk exactly as it would on its own; each part then places 'count' copies of a structure
 * or feature at 'distance' blocks from the anchor's centre (towers around the village, driftwood on the sea).
 *
 * WHY ONE START: every chunk only looks 8 chunks around itself for structure starts that reach into it, so a
 * start's pieces must stay within 8 chunks of its start chunk. Satellites in starts of their own would need
 * every chunk to predict whether a meta anchor won a group slot nearby, which the weighted pick with biome
 * fall-through makes impossible to do cheaply. So all parts are pieces of the meta's start, kept within
 * {@link #REACH} chunks (one chunk under the limit, for the 12-block beard margin of terrain-adapted starts).
 *
 * PARTS KEEP THEIR OWN IDENTITY: the last piece is an {@link Index} listing which piece ranges came from which
 * structure. {@link #structureAt} and {@link #structureOf} answer with the part's structure, so the
 * structure-tag systems (template trim, soft beard, ground-match exemption) treat a tower inside a meta
 * village like a tower on its own. Earth fill checks each part by its own rules; the frequency roll is the anchor's.
 *
 * ANCHOR = A SLOT THE ANCHOR WON (MetaSlots): the meta is never picked by its group directly. When the group's
 * pick lands on one of its anchors and that anchor keeps its frequency roll, the slot becomes the meta with chance
 * 'share', built on that very anchor; if the meta falls short, the slot is the plain anchor. So every anchor keeps
 * its rate and every group its slot count; the meta only adds parts around anchors that spawn anyway. Without a
 * slot behind it (/place structure), the anchor is drawn by its weight in the meta's set, as a group pick would.
 *
 * CHOICES: a part's structures come from a structure tag (creativeengineering:meta/*, generated from
 * structure_tags.json) or ids, picked uniformly; each must pass its own biome list AND the meta's own valid-
 * biome predicate (which carries patcherMod's biome rules) at its start. Collisions are checked in x/z between
 * piece boxes grown by the part's margin. Tries go from the near end of the distance range outwards, so
 * satellites sit just outside the anchor. A part short of its minimum count makes the whole meta fail, and the
 * group's weighted pick falls through to its next member, as for a wrong biome.
 *
 * COASTS AND RIVERS (CEConfig [worldgen.meta_structures] avoidCoasts, off by default): no part may start in, or
 * reach with its box (centre and corners) into, a biome of creativeengineering:coast_river. A satellite there is
 * tried elsewhere, an anchor there fails the meta (the slot is then the plain anchor). Metas in the structure tag
 * creativeengineering:meta/coast_ok (fleets, flotillas, wrecks, sunken ruins) and feature parts are not checked.
 *
 * The json lives in kubejs/generation_scripts/worldgen/meta_structures.json; structure_overrides.js adds it to
 * the datapack.
 */
public final class MetaStructure extends Structure {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/meta-structures");

	/** Chunks a part's pieces may reach from the start chunk. */
	static final int REACH = 7;
	private static final int SALT = 0x6d657461;   // "meta"

	/** Coasts and rivers, which [worldgen.meta_structures] avoidCoasts keeps metas out of. */
	static final TagKey<Biome> COAST = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "coast_river"));
	/** Metas that belong on the water and its shores: avoidCoasts leaves them alone. */
	static final TagKey<Structure> COAST_OK = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "meta/coast_ok"));

	// ── registration ─────────────────────────────────────────────────────────

	public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
			DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, CreativeEngineering.MODID);
	public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
			DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, CreativeEngineering.MODID);

	public static final RegistryObject<StructureType<MetaStructure>> TYPE = STRUCTURE_TYPES.register("meta", () -> (StructureType<MetaStructure>) MetaStructure::codec);
	public static final RegistryObject<StructurePieceType> INDEX_PIECE = PIECE_TYPES.register("meta_index", () -> Index::new);
	public static final RegistryObject<StructurePieceType> FEATURE_PIECE = PIECE_TYPES.register("meta_feature", () -> MetaFeaturePiece::new);

	public static void register(IEventBus modBus) {
		STRUCTURE_TYPES.register(modBus);
		PIECE_TYPES.register(modBus);
	}

	// ── codec ────────────────────────────────────────────────────────────────

	/** [min, max] (both inclusive); a bare number n reads as [n, n]. */
	record Range(int min, int max) {
		static final Codec<Range> CODEC = Codec.either(Codec.INT, Codec.INT.listOf()).comapFlatMap(e -> e.map(
				n -> DataResult.success(new Range(n, n)),
				l -> l.size() == 2 && l.get(0) <= l.get(1) && l.get(0) >= 0
						? DataResult.success(new Range(l.get(0), l.get(1)))
						: DataResult.error("expected a number or [min, max] with 0 <= min <= max, got " + l)),
				r -> r.min == r.max ? Either.<Integer, List<Integer>>left(r.min) : Either.<Integer, List<Integer>>right(List.of(r.min, r.max)));

		int pick(RandomSource random) { return min + (max > min ? random.nextInt(max - min + 1) : 0); }
	}

	/**
	 * An optional field that is strict: absent = empty, but a present value that doesn't parse is an error. DFU's
	 * optionalFieldOf would quietly use the default instead (a count of [3, 1] became 1).
	 */
	static <A> MapCodec<Optional<A>> strict(Codec<A> codec, String name) {
		return new MapCodec<>() {
			@Override
			public <T> java.util.stream.Stream<T> keys(DynamicOps<T> ops) { return java.util.stream.Stream.of(ops.createString(name)); }

			@Override
			public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
				T value = input.get(name);
				return value == null ? DataResult.success(Optional.empty())
						: codec.parse(ops, value).map(Optional::of).mapError(e -> "\"" + name + "\": " + e);
			}

			@Override
			public <T> RecordBuilder<T> encode(Optional<A> value, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return value.isPresent() ? prefix.add(name, codec.encodeStart(ops, value.get())) : prefix;
			}
		};
	}

	static <A> MapCodec<A> strict(Codec<A> codec, String name, A fallback) {
		return strict(codec, name).xmap(o -> o.orElse(fallback), a -> a.equals(fallback) ? Optional.empty() : Optional.of(a));
	}

	/** "#tag" or an id, or a list of them. */
	static final Codec<List<String>> ENTRIES = Codec.either(Codec.STRING, Codec.STRING.listOf()).comapFlatMap(e -> {
		List<String> list = e.map(List::of, l -> l);
		for (String s : list) {
			if (ResourceLocation.tryParse(s.startsWith("#") ? s.substring(1) : s) == null) return DataResult.error("not an id or #tag: " + s);
		}
		return list.isEmpty() ? DataResult.error("empty list") : DataResult.success(list);
	}, l -> l.size() == 1 ? Either.<String, List<String>>left(l.get(0)) : Either.<String, List<String>>right(l));

	/**
	 * One part: 'structures' or 'features' (placed features), placed 'count' times at 'distance' blocks from the
	 * centre, with 'chance' of being tried at all (a part skipped by chance never fails the meta).
	 */
	record Part(Optional<List<String>> structures, Optional<List<String>> features, Range count, float chance, Range distance,
			int spacing, int margin, int tries, int waterDepth, int radius) {
		static final Codec<Part> CODEC = RecordCodecBuilder.<Part>create(i -> i.group(
				strict(ENTRIES, "structures").forGetter(Part::structures),
				strict(ENTRIES, "features").forGetter(Part::features),
				strict(Range.CODEC, "count", new Range(1, 1)).forGetter(Part::count),
				strict(Codec.floatRange(0, 1), "chance", 1f).forGetter(Part::chance),
				strict(Range.CODEC, "distance", new Range(0, 64)).forGetter(Part::distance),
				strict(Codec.intRange(0, 256), "spacing", 0).forGetter(Part::spacing),
				strict(Codec.intRange(0, 64), "margin", 4).forGetter(Part::margin),
				strict(Codec.intRange(1, 64), "tries", 16).forGetter(Part::tries),
				strict(Codec.intRange(0, 64), "water_depth", 0).forGetter(Part::waterDepth),
				strict(Codec.intRange(0, 16), "radius", 6).forGetter(Part::radius)
		).apply(i, Part::new)).flatXmap(Part::validate, DataResult::success);

		DataResult<Part> validate() {
			if (structures.isPresent() == features.isPresent()) return DataResult.error("a part needs exactly one of \"structures\" and \"features\"");
			if (distance.max > REACH * 16) return DataResult.error("distance " + distance.max + " is past the reach of a structure start (" + REACH * 16 + ")");
			return DataResult.success(this);
		}

		String label() { return structures.orElseGet(features::orElseThrow).toString(); }
	}

	public static final Codec<MetaStructure> CODEC = RecordCodecBuilder.create(i -> i.group(
			settingsCodec(i),
			strict(ENTRIES, "anchor").forGetter(s -> s.anchor),
			strict(Codec.floatRange(0, 1), "share", 0f).forGetter(s -> s.share),
			Part.CODEC.listOf().fieldOf("parts").forGetter(s -> s.parts)
	).apply(i, MetaStructure::new));

	private static Codec<MetaStructure> codec() { return CODEC; }

	private final Optional<List<String>> anchor;
	/** With an anchor: the chance that a slot one of its anchors won becomes this meta (MetaSlots). */
	private final float share;
	private final List<Part> parts;

	MetaStructure(StructureSettings settings, Optional<List<String>> anchor, float share, List<Part> parts) {
		super(settings);
		this.anchor = anchor;
		this.share = share;
		this.parts = List.copyOf(parts);
	}

	boolean anchored() { return anchor.isPresent(); }

	float share() { return share; }

	/** The structures the anchor entries name (existing ones, in order). */
	List<Structure> anchorCandidates(Registry<Structure> reg) {
		return anchor.isPresent() ? resolveStructures(reg, anchor.get()) : List.of();
	}

	/** Every structure this meta can contain: its anchors and its structure parts. */
	Set<Structure> contents(Registry<Structure> reg) {
		Set<Structure> out = partStructures(reg);
		out.addAll(anchorCandidates(reg));
		return out;
	}

	/** The structures its parts can place (not the anchors). */
	Set<Structure> partStructures(Registry<Structure> reg) {
		Set<Structure> out = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Part p : parts) p.structures().ifPresent(e -> out.addAll(resolveStructures(reg, e)));
		return out;
	}

	@Override
	public StructureType<?> type() { return TYPE.get(); }

	// ── generation ───────────────────────────────────────────────────────────

	/** One placed structure: its pieces (in order) and their union box. */
	record Placed(Structure structure, BlockPos position, List<StructurePiece> pieces, BoundingBox box) {}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
		if (!WorldgenConfig.metaStructuresEnabled()) return Optional.empty();
		Registry<Structure> structures = ctx.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureWithSalt(ctx.seed(), ctx.chunkPos().x, ctx.chunkPos().z, SALT);
		Layout layout = new Layout(ctx.chunkPos());
		List<String> log = WorldgenConfig.metaStructuresLog() ? new ArrayList<>() : null;

		StructureFrequencies.anchorChosen(this, null, 0, 0);
		Predicate<Holder<Biome>> avoid = avoidsCoasts(structures) ? b -> b.is(COAST) : null;
		BlockPos origin;
		int cx, cz;
		if (anchor.isPresent()) {
			Placed a = null;
			Structure forced = MetaSlots.forcedAnchor(this, ctx.chunkPos());   // the anchor whose slot this is
			List<Structure> candidates = forced != null ? new ArrayList<>(List.of(forced)) : resolveStructures(structures, anchor.get());
			Map<Structure, Integer> inSet = forced != null ? Map.of() : groupWeights(ctx.registryAccess());
			boolean weighted = candidates.stream().anyMatch(inSet::containsKey);
			if (weighted) candidates.removeIf(c -> !inSet.containsKey(c));
			Map<Structure, Integer> weights = weighted ? inSet : Map.of();
			while (a == null && !candidates.isEmpty()) {
				Structure s = weightedPick(candidates, weights, random);   // vanilla's pick: a failure re-picks among the rest
				candidates.remove(s);
				a = generate(s, ctx, ctx.chunkPos(), avoid);
			}
			// a slot's anchor that doesn't fit here fails on its own too (the group's biome fall-through): not worth a line
			if (a == null) return forced != null ? Optional.empty() : fail(log, ctx, "no anchor fits");
			layout.add(a);
			origin = a.position();
			cx = (a.box().minX() + a.box().maxX()) / 2;
			cz = (a.box().minZ() + a.box().maxZ()) / 2;
			if (log != null) log.add("anchor " + name(structures, a.structure()) + " at " + cx + " " + origin.getY() + " " + cz);
		} else {
			cx = ctx.chunkPos().getMiddleBlockX();
			cz = ctx.chunkPos().getMiddleBlockZ();
			int y = ctx.chunkGenerator().getBaseHeight(cx, cz, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
			origin = new BlockPos(cx, y, cz);
			if (!validBiome(origin, ctx, ctx.validBiome())) return Optional.empty();   // vanilla checks this too; skip the work
		}

		for (Part part : parts) {
			if (part.chance() < 1 && random.nextFloat() >= part.chance()) continue;
			int wanted = part.count().pick(random);
			int placed = part.structures().isPresent()
					? placeStructures(part, wanted, resolveStructures(structures, part.structures().get()), ctx, layout, random, cx, cz, log, avoid)
					: placeFeatures(part, wanted, ctx, layout, random, cx, cz);
			if (log != null) log.add(placed + "/" + wanted + " " + part.label());
			if (placed < part.count().min()) return fail(log, ctx, "part " + part.label() + " placed " + placed + " of at least " + part.count().min());
		}

		StructurePiecesBuilder builder = new StructurePiecesBuilder();
		layout.pieces.forEach(builder::addPiece);
		builder.addPiece(new Index(origin, layout.entries, structures));
		if (log != null) LOG.info("[meta-structures] {} at chunk [{}, {}]: {}", name(structures, this), ctx.chunkPos().x, ctx.chunkPos().z, String.join(", ", log));
		if (!layout.entries.isEmpty() && anchor.isPresent()) StructureFrequencies.anchorChosen(this, layout.entries.get(0).structure(), ctx.chunkPos().x, ctx.chunkPos().z);
		return Optional.of(new GenerationStub(origin, Either.<java.util.function.Consumer<StructurePiecesBuilder>, StructurePiecesBuilder>right(builder)));
	}

	private Optional<GenerationStub> fail(List<String> log, GenerationContext ctx, String why) {
		if (log != null) {
			Registry<Structure> reg = ctx.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
			LOG.info("[meta-structures] {} at chunk [{}, {}]: not placed, {}{}", name(reg, this), ctx.chunkPos().x, ctx.chunkPos().z, why,
					log.isEmpty() ? "" : " (had " + String.join(", ", log) + ")");
		}
		return Optional.empty();
	}

	/** Satellites: from the near end of the distance range outwards, each a structure that fits there. */
	private static int placeStructures(Part part, int wanted, List<Structure> pool, GenerationContext ctx, Layout layout,
			WorldgenRandom random, int cx, int cz, List<String> log, Predicate<Holder<Biome>> avoid) {
		if (pool.isEmpty()) return 0;
		Registry<Structure> reg = ctx.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		List<int[]> centres = new ArrayList<>();
		int placed = 0;
		for (int n = 0; n < wanted; n++) {
			for (int t = 0; t < part.tries(); t++) {
				double d = part.distance().min() + (part.distance().max() - part.distance().min()) * (t + random.nextDouble()) / part.tries();
				double angle = random.nextDouble() * Math.PI * 2;
				int px = cx + (int) Math.round(Math.cos(angle) * d), pz = cz + (int) Math.round(Math.sin(angle) * d);
				ChunkPos at = new ChunkPos(px >> 4, pz >> 4);
				if (!layout.inReach(at.x, at.z)) continue;
				Placed p = null;
				for (Structure s : shuffled(pool, random)) {
					p = generate(s, ctx, at, avoid);
					if (p != null) break;
				}
				if (p == null) continue;
				int[] c = {(p.box().minX() + p.box().maxX()) / 2, (p.box().minZ() + p.box().maxZ()) / 2};
				if (!layout.inReach(p) || layout.collides(p, part.margin()) || tooClose(centres, c[0], c[1], part.spacing())) continue;
				layout.add(p);
				centres.add(c);
				placed++;
				if (log != null) log.add(name(reg, p.structure()) + " at " + c[0] + " " + p.position().getY() + " " + c[1]);
				break;
			}
		}
		return placed;
	}

	/** Feature spots: a random point in the distance ring, clear of every placed structure, over deep enough water. */
	private static int placeFeatures(Part part, int wanted, GenerationContext ctx, Layout layout, WorldgenRandom random, int cx, int cz) {
		Registry<PlacedFeature> reg = ctx.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
		List<ResourceLocation> pool = resolve(reg, part.features().orElseThrow(), Registry.PLACED_FEATURE_REGISTRY);
		if (pool.isEmpty()) return 0;
		ChunkGenerator gen = ctx.chunkGenerator();
		List<int[]> spots = new ArrayList<>();
		int placed = 0;
		for (int n = 0; n < wanted; n++) {
			for (int t = 0; t < part.tries(); t++) {
				double r0 = part.distance().min(), r1 = part.distance().max();
				double d = Math.sqrt(r0 * r0 + (r1 * r1 - r0 * r0) * random.nextDouble());   // even over the ring's area
				double angle = random.nextDouble() * Math.PI * 2;
				int x = cx + (int) Math.round(Math.cos(angle) * d), z = cz + (int) Math.round(Math.sin(angle) * d);
				if (!layout.inReach(x >> 4, z >> 4) || tooClose(spots, x, z, part.spacing()) || layout.covers(x, z, part.margin())) continue;
				int surface = gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
				if (part.waterDepth() > 0) {
					int floor = gen.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());
					if (surface - floor < part.waterDepth()) continue;
				}
				ResourceLocation feature = pool.get(random.nextInt(pool.size()));
				layout.pieces.add(new MetaFeaturePiece(feature, new BlockPos(x, surface, z), part.radius()));
				spots.add(new int[]{x, z});
				placed++;
				break;
			}
		}
		return placed;
	}

	/**
	 * The structure's own start at that chunk, or null when it can't: its own refusal, or a biome its list, its
	 * biome rules (the water rule) or the meta's rejects. So a part only stands where it could stand alone. With
	 * {@code avoid} (avoidCoasts), also null when its start or its box's centre or a corner is in an avoided biome.
	 */
	private static Placed generate(Structure s, GenerationContext ctx, ChunkPos at, Predicate<Holder<Biome>> avoid) {
		Predicate<Holder<Biome>> biomes = StructureBiomeRules.filter(s, b -> ctx.validBiome().test(b) && s.biomes().contains(b) && (avoid == null || !avoid.test(b)));
		GenerationContext sub = new GenerationContext(ctx.registryAccess(), ctx.chunkGenerator(), ctx.biomeSource(), ctx.randomState(),
				ctx.structureTemplateManager(), ctx.seed(), at, ctx.heightAccessor(), biomes);
		try {
			Optional<GenerationStub> stub = s.findGenerationPoint(sub);
			if (stub.isEmpty() || !validBiome(stub.get().position(), sub, biomes)) return null;
			StructurePiecesBuilder builder = stub.get().getPiecesBuilder();
			if (builder.isEmpty()) return null;
			List<StructurePiece> pieces = builder.build().pieces();
			BoundingBox box = StructurePiece.createBoundingBox(pieces.stream());
			if (avoid != null && reaches(box, stub.get().position().getY(), sub, avoid)) return null;
			return new Placed(s, stub.get().position(), pieces, box);
		} catch (RuntimeException e) {
			if (FAILED.add(s)) LOG.error("[meta-structures] {} failed to generate as a part (skipped from now on in this session's log)", s, e);
			return null;
		}
	}

	private static final Set<Structure> FAILED = ConcurrentHashMap.newKeySet();

	/** Whether the box's centre or one of its corners, at height y, is in a biome the predicate matches. */
	private static boolean reaches(BoundingBox box, int y, GenerationContext ctx, Predicate<Holder<Biome>> biomes) {
		int[] xs = {box.minX(), box.maxX(), (box.minX() + box.maxX()) / 2}, zs = {box.minZ(), box.maxZ(), (box.minZ() + box.maxZ()) / 2};
		for (int i = 0; i < 5; i++) {
			int x = i < 4 ? xs[i & 1] : xs[2], z = i < 4 ? zs[i >> 1] : zs[2];
			if (biomes.test(ctx.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z),
					ctx.randomState().sampler()))) return true;
		}
		return false;
	}

	/** avoidCoasts applies to this meta: the switch is on and the meta isn't in meta/coast_ok. */
	private boolean avoidsCoasts(Registry<Structure> structures) {
		if (!WorldgenConfig.metaAvoidCoasts()) return false;
		return structures.getResourceKey(this).flatMap(structures::getHolder).map(h -> !h.is(COAST_OK)).orElse(true);
	}

	/** Structure#isValidBiome (private in 1.19.2): the biome at the stub's position. */
	private static boolean validBiome(BlockPos pos, GenerationContext ctx, Predicate<Holder<Biome>> biomes) {
		return biomes.test(ctx.chunkGenerator().getBiomeSource().getNoiseBiome(
				QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()), ctx.randomState().sampler()));
	}

	private static boolean tooClose(List<int[]> spots, int x, int z, int spacing) {
		if (spacing <= 0) return false;
		for (int[] s : spots) {
			long dx = s[0] - x, dz = s[1] - z;
			if (dx * dx + dz * dz < (long) spacing * spacing) return true;
		}
		return false;
	}

	/** Everything placed so far, in piece order, with the index entries for structure parts. */
	static final class Layout {
		final ChunkPos start;
		final List<StructurePiece> pieces = new ArrayList<>();
		final List<Index.Entry> entries = new ArrayList<>();
		private final List<BoundingBox> boxes = new ArrayList<>();

		Layout(ChunkPos start) { this.start = start; }

		void add(Placed p) {
			entries.add(new Index.Entry(p.structure(), pieces.size(), pieces.size() + p.pieces().size()));
			pieces.addAll(p.pieces());
			for (StructurePiece piece : p.pieces()) boxes.add(piece.getBoundingBox());
		}

		boolean inReach(int chunkX, int chunkZ) {
			return Math.abs(chunkX - start.x) <= REACH && Math.abs(chunkZ - start.z) <= REACH;
		}

		boolean inReach(Placed p) {
			BoundingBox b = p.box();
			return inReach(b.minX() >> 4, b.minZ() >> 4) && inReach(b.maxX() >> 4, b.maxZ() >> 4);
		}

		/** Any of p's pieces within margin (x/z) of a piece placed before. */
		boolean collides(Placed p, int margin) {
			for (StructurePiece piece : p.pieces()) {
				BoundingBox a = piece.getBoundingBox();
				for (BoundingBox b : boxes) {
					if (a.minX() - margin <= b.maxX() && a.maxX() + margin >= b.minX() && a.minZ() - margin <= b.maxZ() && a.maxZ() + margin >= b.minZ()) return true;
				}
			}
			return false;
		}

		/** The column (x, z) is within margin of a placed structure piece. */
		boolean covers(int x, int z, int margin) {
			for (BoundingBox b : boxes) {
				if (x >= b.minX() - margin && x <= b.maxX() + margin && z >= b.minZ() - margin && z <= b.maxZ() + margin) return true;
			}
			return false;
		}
	}

	// ── after placement ──────────────────────────────────────────────────────

	/** Each structure part's own afterPlace with only its pieces (the mansion's foundations, for one). */
	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
			BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
		List<StructurePiece> list = pieces.pieces();
		Index index = index(list);
		if (index == null) return;
		for (int i = 0; i < index.entries.size(); i++) {
			Structure s = index.resolved.get(i);
			Index.Entry e = index.entries.get(i);
			if (s == null || e.to() > list.size()) continue;
			s.afterPlace(level, structureManager, generator, random, box, chunkPos, new PiecesContainer(list.subList(e.from(), e.to())));
		}
	}

	// ── part lookups for the other worldgen systems ──────────────────────────

	/** The meta index of a start's pieces (the last piece), or null when the start isn't a meta structure. */
	static Index index(List<StructurePiece> pieces) {
		return !pieces.isEmpty() && pieces.get(pieces.size() - 1) instanceof Index i ? i : null;
	}

	/**
	 * The structure that built the template placed at pos: for a meta start, the part whose pieces contain pos
	 * (its anchor or a satellite); otherwise, or when no part does, the start's own structure.
	 */
	public static Structure structureAt(StructureStart start, BlockPos pos) {
		if (!(start.getStructure() instanceof MetaStructure)) return start.getStructure();
		List<StructurePiece> pieces = start.getPieces();
		Index index = index(pieces);
		if (index != null) {
			for (int i = 0; i < index.entries.size(); i++) {
				Index.Entry e = index.entries.get(i);
				for (int k = e.from(); k < e.to() && k < pieces.size(); k++) {
					if (pieces.get(k).getBoundingBox().isInside(pos)) {
						Structure s = index.resolved.get(i);
						return s != null ? s : start.getStructure();
					}
				}
			}
		}
		return start.getStructure();
	}

	/** Like {@link #structureAt}, for the piece whose bounding box is this very object (the Beardifier's rigids). */
	public static Structure structureOf(StructureStart start, BoundingBox box) {
		if (!(start.getStructure() instanceof MetaStructure)) return start.getStructure();
		List<StructurePiece> pieces = start.getPieces();
		Index index = index(pieces);
		if (index != null) {
			for (int i = 0; i < index.entries.size(); i++) {
				Index.Entry e = index.entries.get(i);
				for (int k = e.from(); k < e.to() && k < pieces.size(); k++) {
					if (pieces.get(k).getBoundingBox() == box) {
						Structure s = index.resolved.get(i);
						return s != null ? s : start.getStructure();
					}
				}
			}
		}
		return start.getStructure();
	}

	/** One structure part of a meta start, with its pieces (in order). */
	public record PartPieces(Holder<Structure> structure, List<StructurePiece> pieces) {}

	/**
	 * A meta start's structure parts (the anchor first), each with its own holder, or null when the start isn't a
	 * meta. Feature pieces and the index belong to no part. Earth fill checks each part by its own rules.
	 */
	public static List<PartPieces> parts(StructureStart start) {
		if (!(start.getStructure() instanceof MetaStructure)) return null;
		List<StructurePiece> pieces = start.getPieces();
		Index index = index(pieces);
		if (index == null) return null;
		List<PartPieces> out = new ArrayList<>();
		for (int i = 0; i < index.entries.size(); i++) {
			Index.Entry e = index.entries.get(i);
			Holder<Structure> h = index.holders.get(i);
			if (h != null && e.to() <= pieces.size() && e.from() < e.to()) out.add(new PartPieces(h, pieces.subList(e.from(), e.to())));
		}
		return out;
	}

	/** A part of a meta start: its structure and the box around its pieces. */
	public record Found(Structure structure, BoundingBox box) {}

	/** The first part of a meta start (the anchor first) built by one of these structures, or null. */
	static Found findPart(StructureStart start, Set<Structure> wanted) {
		List<StructurePiece> pieces = start.getPieces();
		Index index = index(pieces);
		if (index == null) return null;
		for (int i = 0; i < index.entries.size(); i++) {
			Structure s = index.resolved.get(i);
			Index.Entry e = index.entries.get(i);
			if (s == null || !wanted.contains(s) || e.to() > pieces.size() || e.from() >= e.to()) continue;
			return new Found(s, StructurePiece.createBoundingBox(pieces.subList(e.from(), e.to()).stream()));
		}
		return null;
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private static List<Structure> resolveStructures(Registry<Structure> reg, List<String> entries) {
		List<Structure> out = new ArrayList<>();   // mutable: the anchor pick removes from it
		for (ResourceLocation id : resolve(reg, entries, Registry.STRUCTURE_REGISTRY)) {
			Structure s = reg.get(id);
			if (s != null && !(s instanceof MetaStructure)) out.add(s);   // no meta inside a meta
		}
		return out;
	}

	/** "#tag" / id entries -> ids that exist, in order, without repeats. Unknown ids are logged once. */
	private static <T> List<ResourceLocation> resolve(Registry<T> reg, List<String> entries, ResourceKey<? extends Registry<T>> key) {
		Set<ResourceLocation> out = new LinkedHashSet<>();
		for (String entry : entries) {
			if (entry.startsWith("#")) {
				TagKey<T> tag = TagKey.create(key, new ResourceLocation(entry.substring(1)));
				for (Holder<T> h : reg.getTagOrEmpty(tag)) h.unwrapKey().ifPresent(k -> out.add(k.location()));
			} else {
				ResourceLocation id = new ResourceLocation(entry);
				if (reg.containsKey(id)) out.add(id);
				else if (MISSING.add(entry)) LOG.warn("[meta-structures] {} {} does not exist; skipped", key.location().getPath(), entry);
			}
		}
		return List.copyOf(out);
	}

	private static final Set<String> MISSING = ConcurrentHashMap.newKeySet();

	/** Weighted like vanilla's set pick; weights empty: uniform. */
	private static Structure weightedPick(List<Structure> candidates, Map<Structure, Integer> weights, RandomSource random) {
		if (weights.isEmpty()) return candidates.get(random.nextInt(candidates.size()));
		int total = 0;
		for (Structure c : candidates) total += weights.get(c);
		int r = random.nextInt(Math.max(1, total));
		for (Structure c : candidates) {
			r -= weights.get(c);
			if (r < 0) return c;
		}
		return candidates.get(candidates.size() - 1);
	}

	/** Member weights of the structure set that holds this meta (its group), per registry access (one per world load). */
	private Map<Structure, Integer> groupWeights(net.minecraft.core.RegistryAccess access) {
		Registry<StructureSet> sets = access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		Map<Structure, Integer> cached = weightsCache.get(sets);
		if (cached != null) return cached;
		Map<Structure, Integer> out = new IdentityHashMap<>();
		for (StructureSet set : sets) {
			if (set.structures().stream().noneMatch(e -> e.structure().value() == this)) continue;
			for (StructureSet.StructureSelectionEntry e : set.structures()) out.putIfAbsent(e.structure().value(), e.weight());
		}
		synchronized (weightsCache) { weightsCache.put(sets, out); }
		return out;
	}

	private final Map<Registry<StructureSet>, Map<Structure, Integer>> weightsCache = Collections.synchronizedMap(new WeakHashMap<>());

	private static <T> List<T> shuffled(List<T> list, RandomSource random) {
		List<T> copy = new ArrayList<>(list);
		Collections.shuffle(copy, new java.util.Random(random.nextLong()));
		return copy;
	}

	private static String name(Registry<Structure> reg, Structure s) {
		ResourceLocation id = reg.getKey(s);
		return id != null ? id.toString() : s.toString();
	}

	// ── the index piece ──────────────────────────────────────────────────────

	/**
	 * The last piece of a meta start: which piece ranges came from which structure. Places nothing, adds no
	 * beard (NONE through Forge's PieceBeardifierModifier), and its one-block box sits at the start's origin.
	 */
	public static final class Index extends StructurePiece implements net.minecraftforge.common.world.PieceBeardifierModifier {
		record Entry(Structure structure, int from, int to) {}

		final List<Entry> entries;
		/** Parallel to entries: the structure, or null when it no longer exists (a removed mod, on load). */
		final List<Structure> resolved;
		/** Parallel to entries: the registry holder (tags), or null. */
		final List<Holder<Structure>> holders;
		private final List<ResourceLocation> ids;

		Index(BlockPos at, List<Entry> entries, Registry<Structure> reg) {
			super(INDEX_PIECE.get(), 0, new BoundingBox(at.getX(), at.getY(), at.getZ(), at.getX(), at.getY(), at.getZ()));
			this.entries = List.copyOf(entries);
			this.resolved = entries.stream().map(Entry::structure).toList();
			this.ids = entries.stream().map(e -> reg.getKey(e.structure())).toList();
			this.holders = holders(reg, ids);
		}

		private static List<Holder<Structure>> holders(Registry<Structure> reg, List<ResourceLocation> ids) {
			List<Holder<Structure>> out = new ArrayList<>();
			for (ResourceLocation id : ids) {
				out.add(id == null ? null : reg.getHolder(ResourceKey.create(Registry.STRUCTURE_REGISTRY, id)).orElse(null));
			}
			return Collections.unmodifiableList(out);
		}

		Index(net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext ctx, net.minecraft.nbt.CompoundTag tag) {
			super(INDEX_PIECE.get(), tag);
			Registry<Structure> reg = ctx.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
			List<Entry> es = new ArrayList<>();
			List<Structure> rs = new ArrayList<>();
			List<ResourceLocation> is = new ArrayList<>();
			for (net.minecraft.nbt.Tag t : tag.getList("parts", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
				net.minecraft.nbt.CompoundTag c = (net.minecraft.nbt.CompoundTag) t;
				ResourceLocation id = ResourceLocation.tryParse(c.getString("structure"));
				Structure s = id == null ? null : reg.get(id);
				es.add(new Entry(s, c.getInt("from"), c.getInt("to")));
				rs.add(s);
				is.add(id);
			}
			this.entries = List.copyOf(es);
			this.resolved = Collections.unmodifiableList(rs);
			this.ids = Collections.unmodifiableList(is);
			this.holders = holders(reg, is);
		}

		@Override
		protected void addAdditionalSaveData(net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext ctx, net.minecraft.nbt.CompoundTag tag) {
			net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
			for (int i = 0; i < entries.size(); i++) {
				net.minecraft.nbt.CompoundTag c = new net.minecraft.nbt.CompoundTag();
				ResourceLocation id = ids.get(i);
				c.putString("structure", id == null ? "" : id.toString());
				c.putInt("from", entries.get(i).from());
				c.putInt("to", entries.get(i).to());
				list.add(c);
			}
			tag.put("parts", list);
		}

		@Override
		public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
				BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {}

		@Override public BoundingBox getBeardifierBox() { return boundingBox; }
		@Override public net.minecraft.world.level.levelgen.structure.TerrainAdjustment getTerrainAdjustment() { return net.minecraft.world.level.levelgen.structure.TerrainAdjustment.NONE; }
		@Override public int getGroundLevelDelta() { return 0; }
	}
}
