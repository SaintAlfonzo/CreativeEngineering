package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeengineering.util.WorldgenFiles;
import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Biome modifier type {@code creativeengineering:bee_nests}: the per-biome bee nest rules from
 * kubejs/generation_scripts/worldgen/bee_nests.json, written as one modifier by
 * kubejs/server_scripts/worldgen/bee_nests.js (feature_writeup/bee_nests.md section 3).
 *
 * <p>Wild bee nests only come from the {@code minecraft:beehive} tree decorator: a tree feature lists it with a
 * probability, and each tree rolls it once. Which trees carry it is fixed per configured feature and shared by every
 * biome using that tree, so the rules act on the tree as it generates instead (mixin/TreeFeatureBeeNestsMixin):
 * <ul>
 *   <li>{@code scale}: the tree's own beehive chances times this (0 = no nests).</li>
 *   <li>{@code chance}: every tree has at least this chance of a nest; a tree without a beehive decorator gets one
 *       (leafless trees are skipped, so fallen logs and bare trunks never carry a nest).</li>
 * </ul>
 * The rules run in file order; each sets only the fields it names, so a later rule overrides an earlier one's
 * field for the biomes they share. Only world generation is changed: a sapling grown by a player keeps vanilla's
 * nest chances (the tree's level is then a ServerLevel, not a WorldGenRegion).
 *
 * <p>Extra bee-nest trees (bee_nests.json {@code trees}) are plain placed features + forge:add_features written by
 * bee_nests.js; they need nothing here.
 *
 * <p>Counters (trees and nests per biome of the tree's origin, chunks per biome at the chunk's centre surface) run
 * whether or not rules exist; /dumpbeenests writes them with the rules to kubejs/exported/bee_nests.json. They only
 * see chunks generated since the server started (or since {@code /dumpbeenests reset}).
 */
public record BeeNests(List<Rule> rules) implements BiomeModifier {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String OUT = "kubejs/exported/bee_nests.json";

	public record Rule(String id, HolderSet<Biome> biomes, Optional<Float> chance, Optional<Float> scale) {
		static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("id").forGetter(Rule::id),
				Biome.LIST_CODEC.fieldOf("biomes").forGetter(Rule::biomes),
				Codec.floatRange(0, 1).optionalFieldOf("chance").forGetter(Rule::chance),
				Codec.floatRange(0, 1000).optionalFieldOf("scale").forGetter(Rule::scale)
		).apply(i, Rule::new));
	}

	public static final Codec<BeeNests> CODEC = RecordCodecBuilder.create(i -> i.group(
			Rule.CODEC.listOf().optionalFieldOf("rules", List.of()).forGetter(BeeNests::rules)
	).apply(i, BeeNests::new));

	/** Holder class, so headless tests can load BeeNests without Forge's registries. */
	private static final class Registration {
		static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
				DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, CreativeWorldgen.NS);
		static final RegistryObject<Codec<BeeNests>> BEE_NESTS = SERIALIZERS.register("bee_nests", () -> CODEC);
	}

	public static void register(IEventBus modBus) {
		Registration.SERIALIZERS.register(modBus);
	}

	// ── resolved rules ───────────────────────────────────────────────────────

	/** What one biome's trees get after every matching rule: chance floor, scale, and the rules that set them. */
	public record Effective(float chance, float scale, List<String> rules) {
		/** The nest chance a tree with beehive chance {@code p} ends up with. */
		public float apply(float p) {
			return Math.min(1f, Math.max(p * scale, chance));
		}
	}

	private static final Map<ResourceKey<Biome>, Effective> BIOMES = new ConcurrentHashMap<>();
	private static volatile List<String> ruleIds = List.of();

	/** Rules in file order, folded per biome: each rule sets only the fields it names. Null when none matched. */
	static Effective resolve(List<Rule> rules, Holder<Biome> biome) {
		float chance = 0f, scale = 1f;
		List<String> ids = new ArrayList<>();
		for (Rule r : rules) {
			if (!r.biomes().contains(biome)) continue;
			if (r.chance().isPresent()) chance = r.chance().get();
			if (r.scale().isPresent()) scale = r.scale().get();
			ids.add(r.id());
		}
		return ids.isEmpty() ? null : new Effective(chance, scale, List.copyOf(ids));
	}

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase != Phase.AFTER_EVERYTHING) return;
		List<String> ids = new ArrayList<>(rules.size());
		for (Rule r : rules) ids.add(r.id());
		ruleIds = List.copyOf(ids);
		Effective e = resolve(rules, biome);
		biome.unwrapKey().ifPresent(k -> {
			if (e != null) BIOMES.put(k, e);
			else BIOMES.remove(k);
		});
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return Registration.BEE_NESTS.get();
	}

	/** One line for the load log. */
	public static String summary() {
		if (ruleIds.isEmpty()) return "no bee_nests modifier ran (no rules, switched off, or the world isn't loaded yet)";
		return ruleIds.size() + " rules, " + BIOMES.size() + " biomes tuned";
	}

	// ── the tree being placed (TreeFeatureBeeNestsMixin) ─────────────────────

	/** Duck interface on BeehiveDecorator (mixin/BeehiveDecoratorBeeNestsMixin): its private probability. */
	public interface ProbabilityHolder {
		float creativeengineering$probability();
	}

	/**
	 * A beehive decorator the rules added to a tree that had none: skipped unless the tree has both logs and leaves.
	 * Vanilla's place() reads logs().get(0) unguarded (fine for the trees that ship the decorator), and Terralith
	 * places leaf-only "trees" (e.g. terralith:birch/trees_forest_negative) that crashed worldgen without this.
	 */
	static final class AddedBeehiveDecorator extends BeehiveDecorator {
		AddedBeehiveDecorator(float probability) {
			super(probability);
		}

		@Override
		public void place(Context context) {
			if (context.logs().isEmpty() || context.leaves().isEmpty()) return;
			super.place(context);
		}
	}

	private static final class Tree {
		final String biome;
		final Effective rule;
		List<TreeDecorator> from, to;

		Tree(String biome, Effective rule) {
			this.biome = biome;
			this.rule = rule;
		}
	}

	private static final ThreadLocal<Tree> CURRENT = new ThreadLocal<>();

	/** TreeFeature.place HEAD. Saplings (not a WorldGenRegion) are neither tuned nor counted. */
	public static void beginTree(WorldGenLevel level, BlockPos origin) {
		if (!(level instanceof WorldGenRegion)) {
			CURRENT.remove();
			return;
		}
		Holder<Biome> holder = level.getBiome(origin);
		ResourceKey<Biome> key = holder.unwrapKey().orElse(null);
		Effective rule = key == null ? null : BIOMES.get(key);
		CURRENT.set(new Tree(key == null ? "?" : key.location().toString(), rule));
	}

	/** TreeFeature.place, both reads of config.decorators: the list with this biome's nest chances. */
	public static List<TreeDecorator> decorators(List<TreeDecorator> original) {
		Tree t = CURRENT.get();
		if (t == null || t.rule == null) return original;
		if (t.from != original) {
			t.from = original;
			t.to = tune(original, t.rule);
		}
		return t.to;
	}

	static List<TreeDecorator> tune(List<TreeDecorator> original, Effective rule) {
		List<TreeDecorator> out = new ArrayList<>(original.size() + 1);
		boolean hive = false, changed = false;
		for (TreeDecorator d : original) {
			if (d instanceof BeehiveDecorator) hive = true;
			if (d instanceof BeehiveDecorator && d instanceof ProbabilityHolder ph) {
				float p = ph.creativeengineering$probability();
				float np = rule.apply(p);
				if (np != p) {
					changed = true;
					if (np > 0) out.add(new BeehiveDecorator(np));
					continue;
				}
			}
			out.add(d);
		}
		if (!hive && rule.chance() > 0) {
			out.add(new AddedBeehiveDecorator(Math.min(1f, rule.chance())));
			changed = true;
		}
		return changed ? out : original;
	}

	/** TreeFeature.place RETURN. */
	public static void endTree(boolean placed) {
		Tree t = CURRENT.get();
		CURRENT.remove();
		if (t != null && placed) counts(t.biome).trees.increment();
	}

	/** BeehiveDecorator.place, as it sets the nest block. */
	public static void nestPlaced() {
		Tree t = CURRENT.get();
		if (t != null) counts(t.biome).nests.increment();
	}

	/** ChunkGenerator.applyBiomeDecoration HEAD: the chunk counts for the biome at its centre surface. */
	public static void chunkDecorated(WorldGenLevel level, ChunkAccess chunk) {
		int x = chunk.getPos().getMinBlockX() + 8, z = chunk.getPos().getMinBlockZ() + 8;
		int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x & 15, z & 15);
		level.getBiome(new BlockPos(x, y, z)).unwrapKey()
				.ifPresent(k -> counts(k.location().toString()).chunks.increment());
	}

	// ── counters + /dumpbeenests ─────────────────────────────────────────────

	private static final class Counts {
		final LongAdder chunks = new LongAdder(), trees = new LongAdder(), nests = new LongAdder();
	}

	private static final Map<String, Counts> COUNTS = new ConcurrentHashMap<>();
	private static volatile Instant since = Instant.now();

	private static Counts counts(String biome) {
		return COUNTS.computeIfAbsent(biome, k -> new Counts());
	}

	public static void reset() {
		COUNTS.clear();
		since = Instant.now();
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		BIOMES.clear();
		ruleIds = List.of();
		reset();
	}

	/** Writes {@link #OUT}; returns a one-line summary for chat. */
	public static String write() throws Exception {
		Map<String, Map<String, Object>> biomes = new TreeMap<>();
		long chunks = 0, trees = 0, nests = 0;
		for (Map.Entry<String, Counts> e : COUNTS.entrySet()) {
			Map<String, Object> b = biomes.computeIfAbsent(e.getKey(), k -> new LinkedHashMap<>());
			long c = e.getValue().chunks.sum(), t = e.getValue().trees.sum(), n = e.getValue().nests.sum();
			b.put("chunks", c);
			b.put("trees", t);
			b.put("nests", n);
			chunks += c;
			trees += t;
			nests += n;
		}
		for (Map.Entry<ResourceKey<Biome>, Effective> e : BIOMES.entrySet()) {
			Map<String, Object> b = biomes.computeIfAbsent(e.getKey().location().toString(), k -> new LinkedHashMap<>());
			b.put("chance", e.getValue().chance());
			b.put("scale", e.getValue().scale());
			b.put("rules", e.getValue().rules());
		}
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("generated", Instant.now().toString());
		root.put("counting_since", since.toString());
		root.put("rules", ruleIds);
		root.put("biomes", biomes);
		WorldgenFiles.writeText(OUT, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n");
		String line = chunks + " chunks, " + trees + " trees, " + nests + " nests in " + COUNTS.size() + " biomes since "
				+ since + "; " + summary() + " -> " + OUT;
		LOGGER.info("[dumpbeenests] {}", line);
		return line;
	}
}
