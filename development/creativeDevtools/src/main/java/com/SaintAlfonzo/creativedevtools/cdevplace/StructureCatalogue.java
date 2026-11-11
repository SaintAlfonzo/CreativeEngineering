package com.SaintAlfonzo.creativedevtools.cdevplace;

import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig.VerticalAlign;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueLayout.Size;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Column;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Entry;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Kind;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Plan;
import com.SaintAlfonzo.creativecommon.util.WorldgenFiles;
import com.SaintAlfonzo.creativeworldgen.worldgen.BiomeLayoutTuner;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Structure Catalogue world type (feature_writeup/plan_structure_catalogue.md): every structure in the pack laid out
 * by family on a flat world, built by itself on the first start of a world of that preset.
 *
 * <pre>
 * /cdevplace catalogue build | preview | status | stop
 * </pre>
 *
 * Works one column at a time within /cdevplace's per-tick budget: measure every item in the column (template size, or
 * generating the structure), fix the column's width and the items' places, place them chunk by chunk, then release the
 * chunks and templates, so nothing is held for the whole world.
 */
public final class StructureCatalogue {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/cdevplace");
	static final String RULES = BiomeLayoutTuner.WORLDGEN_DIR + "structure_world.json";
	static final String REPORT = "kubejs/exported/structure_catalogue_report.json";
	static final String FEATURE = "structureCatalogue";
	private static final String MARKER = "creativedevtools_structure_catalogue";
	private static final int[] PRESET_HEIGHTS = {1, 125, 2, 1};
	private static final int FAILED_DEPTH = 3;
	private static final String[] MAPPED_LABELS = {"Category", "Subfamily", "Function", "Biome"};
	private static final String[] UNMAPPED_LABELS = {"Category", "Subfamily", "Function", "Mod"};

	/** One thing to place: measured first, then given its box once the column is laid out. */
	private static final class Item {
		final Entry entry;
		StructureStart start;
		StructureTemplate template;
		ResourceLocation templateId;
		BoundingBox measured;
		BlockPos templatePos;
		BoundingBox box;
		int xSpan = 1, zSpan = FAILED_DEPTH, minZ, loaded, placed;
		String failure;
		final List<ChunkPos> chunks = new ArrayList<>();
		Item(Entry entry) { this.entry = entry; }
	}

	private static final class Job {
		final ServerLevel level;
		final Plan plan;
		final List<Column> columns;
		final CatalogueLayout mappedCursor, unmappedCursor;
		final int ground;
		final long started = System.currentTimeMillis();
		int columnIdx, measureIdx, placeIdx, done, placed, dried;
		Column column;
		List<Item> items;
		CatalogueLayout.Placement at;
		List<String> lastMapped, lastUnmapped;
		final List<String[]> refused = new ArrayList<>();
		final Map<String, int[]> extents = new LinkedHashMap<>();

		Job(ServerLevel level, Plan plan, CatalogueRules rules, int ground) {
			this.level = level;
			this.plan = plan;
			this.columns = plan.columns();
			this.mappedCursor = new CatalogueLayout(rules.levelGaps, rules.variationGap, rules.originX, rules.originZ, false);
			this.unmappedCursor = new CatalogueLayout(rules.levelGaps, rules.variationGap, rules.originX, rules.originZ, true);
			this.ground = ground;
		}
	}

	/** Set once the catalogue has been built, or a build was stopped: a restart then leaves the world alone. */
	private static final class Marker extends SavedData {
		boolean handled;

		static Marker load(CompoundTag tag) {
			Marker m = new Marker();
			m.handled = tag.getBoolean("handled");
			return m;
		}

		@Override
		public CompoundTag save(CompoundTag tag) {
			tag.putBoolean("handled", handled);
			return tag;
		}
	}

	private static Job job;   // server thread only

	private StructureCatalogue() {}

	static boolean running() {
		return job != null;
	}

	private static Marker marker(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(Marker::load, Marker::new, MARKER);
	}

	private static void setHandled(ServerLevel level) {
		Marker m = marker(level);
		m.handled = true;
		m.setDirty();
	}

	// ── commands ─────────────────────────────────────────────────────────────

	static LiteralArgumentBuilder<CommandSourceStack> commandNode() {
		return Commands.literal("catalogue")
				.then(Commands.literal("build").executes(ctx -> build(ctx.getSource())))
				.then(Commands.literal("preview").executes(ctx -> preview(ctx.getSource())))
				.then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
				.then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())));
	}

	private static int error(CommandSourceStack src, String msg) {
		src.sendFailure(Component.literal(msg));
		return 0;
	}

	private static CatalogueRules loadRules() throws Exception {
		String text = WorldgenFiles.readText(RULES);
		if (text == null) throw new IllegalStateException(RULES + " is missing");
		return CatalogueRules.parse(text);
	}

	private static Plan loadPlan(MinecraftServer server, CatalogueRules rules) {
		Registry<Structure> reg = server.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		List<String> all = new ArrayList<>();
		for (ResourceLocation rl : reg.keySet()) all.add(rl.toString());
		all.sort(null);
		Map<String, Set<String>> tags = MassPlacer.packTags(all);
		if (tags.isEmpty()) throw new IllegalStateException("structure_tags.json could not be read");
		return rules.build(all, MassPlacer.templateIds(server), tags);
	}

	private static int build(CommandSourceStack src) {
		if (!DevtoolsConfig.feature(FEATURE)) return error(src, "[features] " + FEATURE + " is off");
		if (running() || MassPlacer.running()) return error(src, "a /cdevplace run is already going");
		ServerLevel level = src.getServer().overworld();
		if (!(level.getChunkSource().getGenerator() instanceof FlatLevelSource)) return error(src, "the catalogue only builds in a flat overworld");
		try {
			begin(level);
		} catch (Exception e) {
			LOG.error("[catalogue] could not start", e);
			return error(src, "could not start: " + e);
		}
		src.sendSuccess(Component.literal("Building the catalogue: " + job.plan.total() + " items in " + job.columns.size()
				+ " columns. /cdevplace catalogue status, /cdevplace catalogue stop.").withStyle(ChatFormatting.GOLD), false);
		return job.plan.total();
	}

	private static int preview(CommandSourceStack src) {
		Plan plan;
		try {
			plan = loadPlan(src.getServer(), loadRules());
		} catch (Exception e) {
			LOG.error("[catalogue] could not plan", e);
			return error(src, "could not plan: " + e);
		}
		src.sendSuccess(Component.literal(counts(plan)).withStyle(ChatFormatting.GOLD), false);
		Map<String, Integer> tree = new LinkedHashMap<>();
		for (Column c : plan.columns()) {
			String key = c.path().get(0) + (c.path().get(1) == null ? "" : " > " + c.path().get(1));
			tree.merge(key, c.items().size(), Integer::sum);
			LOG.info("[catalogue] preview {} ({}): {}", c.path(), c.items().size(), c.items().stream().map(Entry::id).toList());
		}
		tree.forEach((k, v) -> src.sendSuccess(Component.literal("  " + k + ": " + v).withStyle(ChatFormatting.GRAY), false));
		return plan.total();
	}

	private static String counts(Plan p) {
		return p.count(p.mapped(), Kind.PIECE) + " pieces, " + p.count(p.mapped(), Kind.WHOLE) + " whole, "
				+ p.count(p.unmapped(), Kind.WHOLE) + " unmapped; skipped " + p.markers().size() + " biome markers, "
				+ p.entityPieces().size() + " entity pieces; " + p.replaced().size() + " replaced by pieces, "
				+ p.noPieces().size() + " placed whole for lack of pieces";
	}

	private static int status(CommandSourceStack src) {
		Job j = job;
		if (j == null) return error(src, "no catalogue build going");
		String now = j.items == null || j.placeIdx >= j.items.size() ? "" : ", now " + j.items.get(j.placeIdx).entry.id();
		src.sendSuccess(Component.literal(j.done + "/" + j.plan.total() + " done, " + j.placed + " placed, " + j.refused.size()
				+ " refused, column " + (j.columnIdx + 1) + "/" + j.columns.size() + now), false);
		return j.done;
	}

	private static int stop(CommandSourceStack src) {
		Job j = job;
		if (j == null) return error(src, "no catalogue build going");
		job = null;
		setHandled(j.level);
		src.sendSuccess(Component.literal("stopped at " + j.done + "/" + j.plan.total() + "; a restart will not resume it, /cdevplace catalogue build starts over"), false);
		return j.done;
	}

	// ── world start ──────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		if (!DevtoolsConfig.feature(FEATURE)) return;
		MinecraftServer server = event.getServer();
		ServerLevel level = server.overworld();
		if (!isPreset(level) || marker(level).handled) return;
		try {
			begin(level);
		} catch (Exception e) {
			LOG.error("[catalogue] could not start", e);
			return;
		}
		level.setDefaultSpawnPos(new BlockPos(0, job.ground, 0), 0f);
		GameRules rules = level.getGameRules();
		rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_DOFIRETICK).set(false, server);
		rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
		LOG.info("[catalogue] new Structure Catalogue world: building {} items in {} columns", job.plan.total(), job.columns.size());
	}

	private static boolean isPreset(ServerLevel level) {
		if (!(level.getChunkSource().getGenerator() instanceof FlatLevelSource flat)) return false;
		List<FlatLayerInfo> layers = flat.settings().getLayersInfo();
		if (layers.size() != PRESET_HEIGHTS.length) return false;
		Block[] blocks = {Blocks.BEDROCK, Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK};
		for (int i = 0; i < blocks.length; i++)
			if (layers.get(i).getHeight() != PRESET_HEIGHTS[i] || !layers.get(i).getBlockState().is(blocks[i])) return false;
		return true;
	}

	private static void begin(ServerLevel level) throws Exception {
		CatalogueRules rules = loadRules();
		job = new Job(level, loadPlan(level.getServer(), rules), rules, MassPlacer.groundY(level, 0, 0));
		LOG.info("[catalogue] plan: {}", counts(job.plan));
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		job = null;
	}

	// ── the run ──────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || job == null) return;
		long deadline = System.nanoTime() + DevtoolsConfig.placeTickBudgetMs() * 1_000_000L;
		try {
			do step(job); while (job != null && System.nanoTime() < deadline);
		} catch (RuntimeException e) {
			LOG.error("[catalogue] build aborted", e);
			job = null;
		}
	}

	private static void step(Job j) {
		if (j.items == null) { beginColumn(j); return; }
		if (j.measureIdx < j.items.size()) {
			measure(j, j.items.get(j.measureIdx++));
			if (j.measureIdx == j.items.size()) layoutColumn(j);
			return;
		}
		if (j.placeIdx < j.items.size()) { stepPlace(j, j.items.get(j.placeIdx)); return; }
		endColumn(j);
	}

	private static void beginColumn(Job j) {
		if (j.columnIdx >= j.columns.size()) { finish(j); return; }
		j.column = j.columns.get(j.columnIdx);
		j.items = new ArrayList<>();
		for (Entry e : j.column.items()) j.items.add(new Item(e));
		j.measureIdx = 0;
		j.placeIdx = 0;
	}

	private static CatalogueLayout cursor(Job j) {
		return j.column.unmapped() ? j.unmappedCursor : j.mappedCursor;
	}

	private static void measure(Job j, Item it) {
		Entry e = it.entry;
		try {
			String templateId = e.template() != null ? e.template() : (e.kind() == Kind.PIECE ? e.id() : null);
			if (templateId != null) measureTemplate(j, it, templateId);
			else measureStructure(j, it);
		} catch (Throwable t) {
			LOG.error("[catalogue] {} failed", e.id(), t);
			it.failure = t.toString();
		}
	}

	private static void measureTemplate(Job j, Item it, String templateId) {
		ResourceLocation rl = new ResourceLocation(templateId);
		StructureTemplate t = j.level.getStructureManager().get(rl).orElse(null);
		if (t == null) { it.failure = "template not found: " + templateId; return; }
		Vec3i size = t.getSize();
		if (size.getX() <= 0 || size.getZ() <= 0) { it.failure = "empty template"; return; }
		it.template = t;
		it.templateId = rl;
		it.xSpan = size.getX();
		it.zSpan = size.getZ();
	}

	private static void measureStructure(Job j, Item it) {
		ServerLevel level = j.level;
		ResourceLocation id = new ResourceLocation(it.entry.id());
		Structure structure = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(id);
		if (structure == null) { it.failure = "not a registered structure"; return; }
		ChunkGenerator gen = level.getChunkSource().getGenerator();
		ChunkPos at = new ChunkPos(SectionPos.blockToSectionCoord(cursor(j).nextX()), 0);
		StructureStart start = structure.generate(level.registryAccess(), gen, gen.getBiomeSource(), level.getChunkSource().randomState(),
				level.getStructureManager(), level.getSeed(), at, 0, level, biome -> true);
		if (!start.isValid()) { it.failure = "did not generate on flat ground (height, water or room)"; return; }
		BoundingBox bb = BoundingBox.encapsulatingBoxes(start.getPieces().stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow();
		it.start = start;
		it.measured = bb;
		it.xSpan = bb.getXSpan();
		it.zSpan = bb.getZSpan();
	}

	/** Every item is measured: the column's width and each item's place are now known. */
	private static void layoutColumn(Job j) {
		List<Size> sizes = new ArrayList<>();
		for (Item it : j.items) sizes.add(new Size(it.xSpan, it.zSpan));
		List<String> path = j.column.path();
		j.at = cursor(j).place(path, sizes);
		for (int i = 0; i < j.items.size(); i++) {
			Item it = j.items.get(i);
			it.minZ = j.at.minZ()[i];
			if (it.failure != null) continue;
			if (it.template != null) {
				it.templatePos = new BlockPos(j.at.minX(), j.ground - 1, it.minZ);
				it.box = new BoundingBox(j.at.minX(), j.ground - 1, it.minZ, j.at.minX() + it.xSpan - 1, j.ground - 1 + it.template.getSize().getY() - 1, it.minZ + it.zSpan - 1);
			} else {
				BoundingBox m = it.measured;
				int dx = j.at.minX() - m.minX(), dz = it.minZ - m.minZ();
				int dy = MassPlacer.verticalShift(VerticalAlign.GROUND, m.minY(), j.ground, 0);
				for (StructurePiece piece : it.start.getPieces()) piece.move(dx, dy, dz);
				it.box = new BoundingBox(m.minX() + dx, m.minY() + dy, m.minZ() + dz, m.maxX() + dx, m.maxY() + dy, m.maxZ() + dz);
			}
			for (int cx = SectionPos.blockToSectionCoord(it.box.minX()); cx <= SectionPos.blockToSectionCoord(it.box.maxX()); cx++)
				for (int cz = SectionPos.blockToSectionCoord(it.box.minZ()); cz <= SectionPos.blockToSectionCoord(it.box.maxZ()); cz++)
					it.chunks.add(new ChunkPos(cx, cz));
		}
		levelSigns(j, path);
	}

	private static void levelSigns(Job j, List<String> path) {
		if (!DevtoolsConfig.placeLabels()) return;
		boolean unmapped = j.column.unmapped();
		List<String> previous = unmapped ? j.lastUnmapped : j.lastMapped;
		String[] labels = unmapped ? UNMAPPED_LABELS : MAPPED_LABELS;
		for (int level : CatalogueLayout.signLevels(previous, path)) {
			int count = 0;
			for (Column c : j.columns) {
				if (c.unmapped() == unmapped && c.path().subList(0, level + 1).equals(path.subList(0, level + 1))) count += c.items().size();
			}
			BlockPos pos = new BlockPos(j.at.minX(), j.ground, CatalogueLayout.signZ(level, path.size()));
			MassPlacer.sign(j.level, pos, CatalogueLayout.levelSign(labels[level], path.get(level), count));
		}
		if (unmapped) j.lastUnmapped = path; else j.lastMapped = path;
	}

	/** One unit of work on the current item: load a chunk, place a chunk (drying it in the same step), or finish. */
	private static void stepPlace(Job j, Item it) {
		if (it.failure != null) { finishItem(j, it); return; }
		ServerLevel level = j.level;
		try {
			if (it.loaded < it.chunks.size()) {
				ChunkPos c = it.chunks.get(it.loaded++);
				level.getChunkSource().addRegionTicket(MassPlacer.TICKET, c, 0, c);
				level.getChunk(c.x, c.z);
				return;
			}
			if (it.template != null) {
				if (it.placed++ == 0) {
					StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(JigsawReplacementProcessor.INSTANCE);
					it.template.placeInWorld(level, it.templatePos, it.templatePos, settings, level.getRandom(), Block.UPDATE_CLIENTS);
					if (it.entry.dry()) for (ChunkPos c : it.chunks) dryChunk(j, it, c);
					return;
				}
			} else if (it.placed < it.chunks.size()) {
				ChunkPos c = it.chunks.get(it.placed++);
				it.start.placeInChunk(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
						new BoundingBox(c.getMinBlockX(), level.getMinBuildHeight(), c.getMinBlockZ(), c.getMaxBlockX(), level.getMaxBuildHeight(), c.getMaxBlockZ()), c);
				if (it.entry.dry()) dryChunk(j, it, c);
				return;
			}
		} catch (Throwable t) {
			LOG.error("[catalogue] {} failed while placing", it.entry.id(), t);
			it.failure = t.toString();
		}
		finishItem(j, it);
	}

	/**
	 * Inside the item's box in this chunk: anything that is water without being waterloggable (water, bubble columns, kelp,
	 * seagrass) becomes air and waterlogged blocks are drained. Done in the same step as placing the chunk, so the water
	 * never gets a tick to flow out of the box. Flag 18 (clients, known shape) keeps it from waking neighbours.
	 */
	private static void dryChunk(Job j, Item it, ChunkPos c) {
		BoundingBox b = it.box;
		int minX = Math.max(b.minX(), c.getMinBlockX()), maxX = Math.min(b.maxX(), c.getMaxBlockX());
		int minZ = Math.max(b.minZ(), c.getMinBlockZ()), maxZ = Math.min(b.maxZ(), c.getMaxBlockZ());
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++)
			for (int z = minZ; z <= maxZ; z++)
				for (int y = b.minY(); y <= b.maxY(); y++) {
					pos.set(x, y, z);
					BlockState state = j.level.getBlockState(pos);
					if (!state.getFluidState().is(FluidTags.WATER)) continue;
					BlockState dried = state.hasProperty(BlockStateProperties.WATERLOGGED)
							? state.setValue(BlockStateProperties.WATERLOGGED, false) : Blocks.AIR.defaultBlockState();
					j.level.setBlock(pos, dried, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
					j.dried++;
				}
	}

	private static void finishItem(Job j, Item it) {
		String id = it.entry.id();
		j.done++;
		j.placeIdx++;
		int minX = j.at.minX();
		if (it.failure != null) {
			j.refused.add(new String[]{id, it.failure});
			LOG.info("[catalogue] refused {}: {}", id, it.failure);
			if (DevtoolsConfig.placeLabels()) {
				List<String> lines = new ArrayList<>(MassPlacer.signLines(id, 3));
				lines.add("(failed)");
				MassPlacer.sign(j.level, new BlockPos(minX, j.ground, it.minZ - 2), lines);
			}
		} else {
			j.placed++;
			if (DevtoolsConfig.placeLabels()) {
				List<String> lines = new ArrayList<>(MassPlacer.signLines(id, j.column.unmapped() ? 3 : 4));
				if (j.column.unmapped()) lines.add("(needs type tag)");
				MassPlacer.sign(j.level, new BlockPos(minX, j.ground, it.minZ - 2), lines);
			}
			int[] e = j.extents.computeIfAbsent(j.column.path().get(0), k -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0});
			e[0] = Math.min(e[0], it.box.minX());
			e[1] = Math.max(e[1], it.box.maxX());
			e[2] = Math.max(e[2], it.box.maxZ());
			e[3]++;
		}
		for (ChunkPos c : it.chunks) j.level.getChunkSource().removeRegionTicket(MassPlacer.TICKET, c, 0, c);
		it.start = null;
		if (j.done % 100 == 0) LOG.info("[catalogue] {}/{} ({} refused)", j.done, j.plan.total(), j.refused.size());
	}

	/** The column is built: let go of what it held. */
	private static void endColumn(Job j) {
		for (Item it : j.items) if (it.templateId != null) j.level.getStructureManager().remove(it.templateId);
		j.items = null;
		j.column = null;
		j.at = null;
		j.columnIdx++;
	}

	private static void finish(Job j) {
		job = null;
		setHandled(j.level);
		long secs = (System.currentTimeMillis() - j.started) / 1000;
		try {
			WorldgenFiles.writeText(REPORT, report(j, secs));
		} catch (Exception e) {
			LOG.error("[catalogue] could not write {}", REPORT, e);
		}
		int unmapped = j.plan.count(j.plan.unmapped(), Kind.WHOLE);
		String line = "Structure Catalogue built: " + j.placed + " of " + j.plan.total() + " placed, " + j.refused.size() + " refused, "
				+ unmapped + " need a type tag, " + secs + " s. Report: " + REPORT;
		LOG.info("[catalogue] {}", line);
		j.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(line).withStyle(ChatFormatting.GOLD), false);
	}

	private static String report(Job j, long secs) {
		Plan p = j.plan;
		JsonObject root = new JsonObject();
		JsonObject counts = new JsonObject();
		counts.addProperty("pieces", p.count(p.mapped(), Kind.PIECE));
		counts.addProperty("whole", p.count(p.mapped(), Kind.WHOLE));
		counts.addProperty("unmapped", p.count(p.unmapped(), Kind.WHOLE));
		counts.addProperty("total", p.total());
		counts.addProperty("placed", j.placed);
		counts.addProperty("refused", j.refused.size());
		counts.addProperty("replacedByPieces", p.replaced().size());
		counts.addProperty("skippedBiomeMarkers", p.markers().size());
		counts.addProperty("skippedEntityPieces", p.entityPieces().size());
		counts.addProperty("waterBlocksRemoved", j.dried);
		counts.addProperty("seconds", secs);
		root.add("counts", counts);

		JsonArray unmapped = new JsonArray();
		for (Column c : p.unmapped()) for (Entry e : c.items()) {
			JsonObject o = new JsonObject();
			o.addProperty("id", e.id());
			o.addProperty("note", "needs type tag");
			unmapped.add(o);
		}
		root.add("unmapped", unmapped);

		JsonArray refused = new JsonArray();
		for (String[] r : j.refused) {
			JsonObject o = new JsonObject();
			o.addProperty("id", r[0]);
			o.addProperty("reason", r[1]);
			refused.add(o);
		}
		root.add("refused", refused);
		root.add("placedWholeNoPieces", array(p.noPieces()));

		JsonObject skipped = new JsonObject();
		skipped.add("biomeMarkers", array(p.markers()));
		skipped.add("entityPieces", array(p.entityPieces()));
		root.add("skipped", skipped);

		JsonObject extents = new JsonObject();
		j.extents.forEach((category, e) -> {
			JsonObject o = new JsonObject();
			o.addProperty("minX", e[0]);
			o.addProperty("maxX", e[1]);
			o.addProperty("maxZ", e[2]);
			o.addProperty("items", e[3]);
			extents.add(category, o);
		});
		root.add("categories", extents);
		return new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n";
	}

	private static JsonArray array(List<String> values) {
		JsonArray out = new JsonArray();
		values.forEach(out::add);
		return out;
	}
}
