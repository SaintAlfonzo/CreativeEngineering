package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig.VerticalAlign;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code /ceplace}: every structure (or raw template) matching a filter, laid out in rows for inspection,
 * typically in a flat world with Generate Structures off.
 *
 * <pre>
 * /ceplace structures &lt;filter...&gt;                      rows by type/* tag (structure_tags.json)
 * /ceplace structures by &lt;type|group|mod|none&gt; &lt;filter...&gt;
 * /ceplace templates &lt;glob...&gt;                         raw templates, one row per folder
 * /ceplace preview structures|templates ...             list the rows without placing
 * /ceplace status | stop
 * </pre>
 *
 * Filter terms (space separated, like structure_tags.json values): an id, a glob with *, {@code @namespace},
 * {@code #pack/tag} (structure_tags.json), {@code #ns:tag} (a real structure tag), {@code !term} to exclude.
 * Rows start at the player and run +X, each new row further +Z; a sign names each row and each structure.
 *
 * <p>Structures are generated the way /place structure does it (any biome allowed), then their pieces are
 * moved so the structure's box starts at the row cursor, and placed chunk by chunk. That keeps the whole
 * structure, jigsaw layout and all, including GroundMatch (the placing start is known). A few per tick
 * step by step within a per-tick time budget (CEConfig [worldgen.mass_place] tickBudgetMs), so the server keeps
 * ticking (and a dedicated server's 60 s watchdog never fires) and /ceplace stop works.
 */
public final class MassPlacer {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/ceplace");
	static final String TAGS = BiomeLayoutTuner.WORLDGEN_DIR + "structure_tags.json";
	private static final String TEMPLATE_DIR = "structures";

	enum RowBy { TYPE, GROUP, MOD, NONE }

	record Row(String name, List<ResourceLocation> ids) {}

	private static final class Job {
		final ServerLevel level;
		final CommandSourceStack source;
		final boolean templates;
		final List<Row> rows;
		final int total, originX;
		final long started = System.currentTimeMillis();
		int rowIdx, itemIdx, done, placed;
		int cursorX, rowZ, rowDepth;
		boolean rowLabelled;
		Placement current;   // the structure/template being worked through, one step at a time
		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		final List<String> failed = new ArrayList<>();

		Job(ServerLevel level, CommandSourceStack source, boolean templates, List<Row> rows, BlockPos origin) {
			this.level = level; this.source = source; this.templates = templates; this.rows = rows;
			this.total = rows.stream().mapToInt(r -> r.ids().size()).sum();
			this.originX = origin.getX();
			this.cursorX = origin.getX();
			this.rowZ = origin.getZ() + 3;   // the first aisle (labels) is right in front of the player
		}
	}

	/**
	 * One structure or template in progress. Big structures cover dozens of chunks and generating a chunk of real
	 * terrain can take a second, so the work is split into steps (lay out, load one chunk, place one chunk, label)
	 * and the tick handler runs steps until its time budget is spent: a dedicated server's watchdog kills a tick
	 * over 60 s, and a 144 x 127 village generated in one tick did exactly that.
	 */
	private static final class Placement {
		final ResourceLocation id;
		StructureStart start;             // structure mode
		StructureTemplate template;       // template mode
		BlockPos templatePos;
		BoundingBox box;
		final List<ChunkPos> chunks = new ArrayList<>();
		int loaded, placed;
		Placement(ResourceLocation id) { this.id = id; }
	}

	/** Keeps a chunk loaded between being generated and being built on; expires by itself after a minute. */
	private static final net.minecraft.server.level.TicketType<ChunkPos> TICKET =
			net.minecraft.server.level.TicketType.create("creativeengineering_ceplace", Comparator.comparingLong(ChunkPos::toLong), 1200);

	private static Job job;   // server thread only

	private MassPlacer() {}

	// ── commands ─────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ceplace").requires(s -> s.hasPermission(2))
				.then(structuresNode(false))
				.then(templatesNode(false))
				.then(Commands.literal("preview").then(structuresNode(true)).then(templatesNode(true)))
				.then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
				.then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())));
		event.getDispatcher().register(root);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> structuresNode(boolean preview) {
		return Commands.literal("structures")
				.then(Commands.literal("by").then(Commands.argument("rows", StringArgumentType.word()).suggests(ROWS)
						.then(Commands.argument("filter", StringArgumentType.greedyString()).suggests(STRUCTURE_FILTER)
								.executes(ctx -> structures(ctx, parseRowBy(StringArgumentType.getString(ctx, "rows")), preview)))))
				.then(Commands.argument("filter", StringArgumentType.greedyString()).suggests(STRUCTURE_FILTER)
						.executes(ctx -> structures(ctx, RowBy.TYPE, preview)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> templatesNode(boolean preview) {
		return Commands.literal("templates")
				.then(Commands.argument("glob", StringArgumentType.greedyString()).suggests(TEMPLATE_FILTER)
						.executes(ctx -> templates(ctx, preview)));
	}

	private static RowBy parseRowBy(String s) {
		try {
			return RowBy.valueOf(s.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static int structures(CommandContext<CommandSourceStack> ctx, RowBy by, boolean preview) {
		CommandSourceStack src = ctx.getSource();
		if (by == null) return error(src, "rows must be one of: type, group, mod, none");
		String filter = StringArgumentType.getString(ctx, "filter");
		Registry<Structure> reg = src.getServer().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		List<String> all = new ArrayList<>();
		for (ResourceLocation rl : reg.keySet()) all.add(rl.toString());
		all.sort(null);
		Map<String, Set<String>> packTags = packTags(all);
		List<String> problems = new ArrayList<>();
		Set<String> ids = select(filter, all, packTags, term -> registryTag(reg, term), problems);
		List<Row> rows = rowsFor(ids, by, packTags);
		return start(src, rows, false, preview, problems, filter + " (rows: " + by.name().toLowerCase(Locale.ROOT) + ")");
	}

	private static int templates(CommandContext<CommandSourceStack> ctx, boolean preview) {
		CommandSourceStack src = ctx.getSource();
		String glob = StringArgumentType.getString(ctx, "glob");
		List<String> all = templateIds(src.getServer());
		List<String> problems = new ArrayList<>();
		Set<String> ids = select(glob, all, Map.of(), term -> null, problems);
		return start(src, templateRows(ids), true, preview, problems, glob + " (rows: folder)");
	}

	private static int start(CommandSourceStack src, List<Row> rows, boolean templates, boolean preview, List<String> problems, String what) {
		problems.forEach(p -> src.sendFailure(Component.literal(p)));
		int total = rows.stream().mapToInt(r -> r.ids().size()).sum();
		if (total == 0) return error(src, "nothing matches " + what);
		String kind = templates ? "template" : "structure";
		src.sendSuccess(Component.literal(total + " " + kind + (total == 1 ? "" : "s") + " in " + rows.size() + " row(s) for " + what + ":")
				.withStyle(ChatFormatting.GOLD), false);
		for (Row r : rows) {
			List<String> shown = r.ids().stream().limit(6).map(ResourceLocation::toString).toList();
			src.sendSuccess(Component.literal("  " + r.name() + " (" + r.ids().size() + "): " + String.join(", ", shown)
					+ (r.ids().size() > shown.size() ? ", ..." : "")).withStyle(ChatFormatting.GRAY), false);
			LOG.info("[ceplace] {} row {} ({}): {}", preview ? "preview" : "plan", r.name(), r.ids().size(), r.ids());
		}
		if (preview) return total;

		if (job != null) return error(src, "a /ceplace run is already going (" + job.done + "/" + job.total + "); /ceplace stop first");
		int max = WorldgenConfig.placeMaxItems();
		if (total > max) return error(src, total + " matches is more than [worldgen.mass_place] maxItems (" + max + "): narrow the filter or raise it");
		ServerLevel level = src.getLevel();
		ChunkGenerator gen = level.getChunkSource().getGenerator();
		if (!(gen instanceof FlatLevelSource)) {
			src.sendSuccess(Component.literal("Not a flat world: everything is built at this spot's height, over whatever terrain is here.")
					.withStyle(ChatFormatting.RED), false);
		}
		job = new Job(level, src, templates, rows, new BlockPos(net.minecraft.util.Mth.floor(src.getPosition().x), net.minecraft.util.Mth.floor(src.getPosition().y), net.minecraft.util.Mth.floor(src.getPosition().z)));
		src.sendSuccess(Component.literal("Placing from " + job.originX + ", " + (job.rowZ - 3) + ": rows go +X, the next row +Z. /ceplace status, /ceplace stop.")
				.withStyle(ChatFormatting.GOLD), false);
		return total;
	}

	private static int status(CommandSourceStack src) {
		if (job == null) return error(src, "no /ceplace run going");
		Placement p = job.current;
		String now = p == null ? "" : ", now " + p.id + " (chunks loaded " + p.loaded + "/" + p.chunks.size()
				+ (p.template == null ? ", placed " + p.placed + "/" + p.chunks.size() : "") + ")";
		src.sendSuccess(Component.literal(job.done + "/" + job.total + " started, " + job.placed + " placed, " + job.failed.size() + " failed, row "
				+ (job.rowIdx + 1) + "/" + job.rows.size() + now), false);
		return job.done;
	}

	private static int stop(CommandSourceStack src) {
		if (job == null) return error(src, "no /ceplace run going");
		Job j = job;
		job = null;
		src.sendSuccess(Component.literal("stopped at " + j.done + "/" + j.total), false);
		return j.done;
	}

	private static int error(CommandSourceStack src, String msg) {
		src.sendFailure(Component.literal(msg));
		return 0;
	}

	// ── the run ──────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || job == null) return;
		long deadline = System.nanoTime() + WorldgenConfig.placeTickBudgetMs() * 1_000_000L;
		do step(job); while (job != null && System.nanoTime() < deadline);
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		job = null;
	}

	/** One unit of work: start the next item, load one chunk, place one chunk, or finish the item. */
	private static void step(Job j) {
		if (j.current == null) { begin(j); return; }
		Placement p = j.current;
		try {
			if (p.loaded < p.chunks.size()) {
				ChunkPos c = p.chunks.get(p.loaded++);
				j.level.getChunkSource().addRegionTicket(TICKET, c, 0, c);
				j.level.getChunk(c.x, c.z);   // generates it if new
				return;
			}
			if (p.template != null) {
				if (p.placed++ == 0) {
					StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor(JigsawReplacementProcessor.INSTANCE);
					p.template.placeInWorld(j.level, p.templatePos, p.templatePos, settings, j.level.getRandom(), Block.UPDATE_CLIENTS);
					return;
				}
			} else if (p.placed < p.chunks.size()) {
				ChunkPos c = p.chunks.get(p.placed++);
				ServerLevel level = j.level;
				p.start.placeInChunk(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
						new BoundingBox(c.getMinBlockX(), level.getMinBuildHeight(), c.getMinBlockZ(), c.getMaxBlockX(), level.getMaxBuildHeight(), c.getMaxBlockZ()), c);
				return;
			}
		} catch (Throwable t) {
			LOG.error("[ceplace] {} failed while placing", p.id, t);
			j.current = null;
			failed(j, p.id, t.toString());
			return;
		}
		j.current = null;
		done(j, p);
	}

	/** Row changes and labels, then lays out the next structure/template (or finishes the run). */
	private static void begin(Job j) {
		while (j.rowIdx < j.rows.size() && j.itemIdx >= j.rows.get(j.rowIdx).ids().size()) {
			j.rowZ += j.rowDepth + WorldgenConfig.placeRowGap();
			j.cursorX = j.originX;
			j.rowDepth = 0;
			j.rowIdx++;
			j.itemIdx = 0;
			j.rowLabelled = false;
		}
		if (j.rowIdx >= j.rows.size()) { finish(j); return; }
		Row row = j.rows.get(j.rowIdx);
		if (!j.rowLabelled) {
			j.rowLabelled = true;
			if (WorldgenConfig.placeLabels()) {
				BlockPos at = new BlockPos(j.originX - 4, groundY(j.level, j.originX - 4, j.rowZ - 2), j.rowZ - 2);
				sign(j.level, at, rowSignLines(row.name(), row.ids().size()));
			}
			j.source.sendSuccess(Component.literal("row " + (j.rowIdx + 1) + "/" + j.rows.size() + ": " + row.name()
					+ " (" + row.ids().size() + ")").withStyle(ChatFormatting.GRAY), false);
			return;
		}
		ResourceLocation id = row.ids().get(j.itemIdx++);
		j.done++;
		try {
			Placement p = j.templates ? layoutTemplate(j, id) : layoutStructure(j, id);
			if (p == null) { failed(j, id, null); return; }
			j.current = p;
		} catch (Throwable t) {
			LOG.error("[ceplace] {} failed", id, t);
			failed(j, id, t.toString());
		}
	}

	/** The failure's reason is already in j.failed when {@code reason} is null. */
	private static void failed(Job j, ResourceLocation id, String reason) {
		if (reason != null && j.failed.size() < 1000) j.failed.add(id + ": " + reason);
		if (WorldgenConfig.placeLabels()) {
			List<String> lines = new ArrayList<>(signLines(id.toString(), 3));
			lines.add("(failed)");
			sign(j.level, new BlockPos(j.cursorX, groundY(j.level, j.cursorX, j.rowZ - 2), j.rowZ - 2), lines);
		}
		j.cursorX += 3 + WorldgenConfig.placeGap();
	}

	private static void done(Job j, Placement p) {
		BoundingBox box = p.box;
		j.placed++;
		if (WorldgenConfig.placeLabels()) sign(j.level, new BlockPos(box.minX(), groundY(j.level, box.minX(), box.minZ() - 2), box.minZ() - 2), signLines(p.id.toString(), 4));
		LOG.info("[ceplace] {} at {} {} {} ({} x {} x {}, {} chunks)", p.id, box.minX(), box.minY(), box.minZ(), box.getXSpan(), box.getYSpan(), box.getZSpan(), p.chunks.size());
		j.cursorX = box.maxX() + 1 + WorldgenConfig.placeGap();
		j.rowDepth = Math.max(j.rowDepth, box.maxZ() - j.rowZ + 1);
		j.minX = Math.min(j.minX, box.minX()); j.maxX = Math.max(j.maxX, box.maxX());
		j.minZ = Math.min(j.minZ, box.minZ()); j.maxZ = Math.max(j.maxZ, box.maxZ());
		if (j.done % 10 == 0) j.source.sendSuccess(Component.literal(j.done + "/" + j.total).withStyle(ChatFormatting.DARK_GRAY), false);
	}

	private static void finish(Job j) {
		job = null;
		long secs = (System.currentTimeMillis() - j.started) / 1000;
		String area = j.placed == 0 ? "" : " in x " + j.minX + ".." + j.maxX + ", z " + j.minZ + ".." + j.maxZ;
		j.source.sendSuccess(Component.literal("/ceplace done: " + j.placed + " of " + j.total + " placed" + area + " (" + secs + " s)")
				.withStyle(ChatFormatting.GOLD), false);
		if (!j.failed.isEmpty()) {
			j.source.sendFailure(Component.literal(j.failed.size() + " failed (all in latest.log):"));
			j.failed.stream().limit(8).forEach(f -> j.source.sendFailure(Component.literal("  " + f)));
		}
		LOG.info("[ceplace] done: {} of {} placed{}, {} failed: {}", j.placed, j.total, area, j.failed.size(), j.failed);
	}

	/** Generates the structure like /place structure and moves it onto the row cursor. Null = it didn't generate. */
	private static Placement layoutStructure(Job j, ResourceLocation id) {
		ServerLevel level = j.level;
		Structure structure = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(id);
		if (structure == null) throw new IllegalStateException("not a registered structure");
		ChunkGenerator gen = level.getChunkSource().getGenerator();
		ChunkPos at = new ChunkPos(SectionPos.blockToSectionCoord(j.cursorX), SectionPos.blockToSectionCoord(j.rowZ));
		StructureStart start = structure.generate(level.registryAccess(), gen, gen.getBiomeSource(), level.getChunkSource().randomState(),
				level.getStructureManager(), level.getSeed(), at, 0, level, biome -> true);
		if (!start.isValid()) {
			j.failed.add(id + ": did not generate here (the structure turned this terrain down: height, water or room)");
			return null;
		}
		List<StructurePiece> pieces = start.getPieces();
		BoundingBox bb = BoundingBox.encapsulatingBoxes(pieces.stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow();
		int ground = groundY(level, j.cursorX, j.rowZ);
		int dy = verticalShift(WorldgenConfig.placeVerticalAlign(), bb.minY(), ground, WorldgenConfig.placeAutoAlignBand());
		int dx = j.cursorX - bb.minX(), dz = j.rowZ - bb.minZ();
		for (StructurePiece piece : pieces) piece.move(dx, dy, dz);
		Placement p = new Placement(id);
		p.start = start;
		p.box = new BoundingBox(bb.minX() + dx, bb.minY() + dy, bb.minZ() + dz, bb.maxX() + dx, bb.maxY() + dy, bb.maxZ() + dz);
		addChunks(p);
		return p;
	}

	/** A raw template, bottom layer at ground level (replacing the top ground block), jigsaw blocks resolved. */
	private static Placement layoutTemplate(Job j, ResourceLocation id) {
		ServerLevel level = j.level;
		StructureTemplate template = level.getStructureManager().get(id).orElse(null);
		if (template == null) throw new IllegalStateException("template not found");
		Vec3i size = template.getSize();
		if (size.getX() <= 0 || size.getZ() <= 0) {
			j.failed.add(id + ": empty template");
			return null;
		}
		int ground = groundY(level, j.cursorX, j.rowZ);
		Placement p = new Placement(id);
		p.template = template;
		p.templatePos = new BlockPos(j.cursorX, ground - 1, j.rowZ);
		p.box = new BoundingBox(p.templatePos.getX(), p.templatePos.getY(), p.templatePos.getZ(),
				p.templatePos.getX() + size.getX() - 1, p.templatePos.getY() + size.getY() - 1, p.templatePos.getZ() + size.getZ() - 1);
		addChunks(p);
		return p;
	}

	private static void addChunks(Placement p) {
		for (int cx = SectionPos.blockToSectionCoord(p.box.minX()); cx <= SectionPos.blockToSectionCoord(p.box.maxX()); cx++)
			for (int cz = SectionPos.blockToSectionCoord(p.box.minZ()); cz <= SectionPos.blockToSectionCoord(p.box.maxZ()); cz++)
				p.chunks.add(new ChunkPos(cx, cz));
	}

	/** How far to move a structure whose box bottom generated at {@code minY}; the goal height is ground - 1. */
	static int verticalShift(VerticalAlign align, int minY, int groundY, int band) {
		int target = groundY - 1;
		return switch (align) {
			case KEEP -> 0;
			case GROUND -> target - minY;
			case AUTO -> Math.abs(minY - target) > band ? target - minY : 0;
		};
	}

	/** First free block above the ground at x, z (loads the chunk). */
	private static int groundY(ServerLevel level, int x, int z) {
		level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
		return level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
	}

	private static void sign(ServerLevel level, BlockPos pos, List<String> lines) {
		BlockState state = Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8);   // text faces -Z, the aisle
		level.setBlock(pos, state, Block.UPDATE_ALL);
		if (level.getBlockEntity(pos) instanceof SignBlockEntity be) {
			for (int i = 0; i < 4 && i < lines.size(); i++) be.setMessage(i, Component.literal(lines.get(i)));
			be.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
		}
	}

	// ── pure helpers (headless-tested) ───────────────────────────────────────

	static final int SIGN_WIDTH = 16;   // about what a sign line (90 px) holds; the renderer clips the rest

	/** Namespace on the first line, the path wrapped on '/' and '_' over the rest; {@code maxLines} lines at most. */
	static List<String> signLines(String id, int maxLines) {
		int colon = id.indexOf(':');
		String ns = colon < 0 ? "minecraft" : id.substring(0, colon);
		String path = colon < 0 ? id : id.substring(colon + 1);
		List<String> out = new ArrayList<>();
		out.add(fit(ns));
		StringBuilder line = new StringBuilder();
		int i = 0;
		while (i < path.length()) {
			int end = i;   // next token: up to and including the next '/' or '_'
			while (end < path.length() && path.charAt(end) != '/' && path.charAt(end) != '_') end++;
			String token = path.substring(i, Math.min(path.length(), end + 1));
			i += token.length();
			if (line.length() > 0 && line.length() + token.length() > SIGN_WIDTH) {
				out.add(line.toString());
				line.setLength(0);
			}
			line.append(token);
		}
		if (line.length() > 0) out.add(line.toString());
		if (out.size() > maxLines) {
			List<String> cut = new ArrayList<>(out.subList(0, maxLines));
			String last = cut.get(maxLines - 1);
			cut.set(maxLines - 1, last.length() > SIGN_WIDTH - 2 ? last.substring(0, SIGN_WIDTH - 2) + ".." : last + "..");
			out = cut;
		}
		for (int k = 0; k < out.size(); k++) out.set(k, fit(out.get(k)));
		return out;
	}

	private static String fit(String s) {
		return s.length() <= SIGN_WIDTH ? s : s.substring(0, SIGN_WIDTH - 2) + "..";
	}

	static List<String> rowSignLines(String row, int count) {
		List<String> out = new ArrayList<>(signLines(row.contains(":") ? row : "row:" + row, 3));
		if (!row.contains(":")) out.set(0, "row");
		out.add(count + (count == 1 ? " item" : " items"));
		return out;
	}

	/**
	 * Filter terms over a list of ids (structure_tags.json syntax): ids, globs with *, @namespace, #pack/tag,
	 * #ns:registry/tag (via {@code registryTag}; null = not supported), !term excludes (exclusions win).
	 */
	static Set<String> select(String filter, List<String> all, Map<String, Set<String>> packTags,
			java.util.function.Function<String, Collection<String>> registryTag, List<String> problems) {
		Set<String> include = new LinkedHashSet<>(), exclude = new LinkedHashSet<>();
		String[] terms = filter.trim().isEmpty() ? new String[0] : filter.trim().split("\\s+");
		if (terms.length == 0) problems.add("empty filter");
		for (String raw : terms) {
			boolean neg = raw.startsWith("!");
			String t = neg ? raw.substring(1) : raw;
			Collection<String> ids;
			if (t.startsWith("@")) {
				String prefix = t.substring(1) + ":";
				ids = all.stream().filter(id -> id.startsWith(prefix)).toList();
			} else if (t.startsWith("#") && t.contains(":")) {
				ids = registryTag.apply(t.substring(1));
				if (ids == null) { problems.add(raw + ": no such tag"); ids = List.of(); }
			} else if (t.startsWith("#")) {
				ids = packTags.get(t.substring(1));
				if (ids == null) { problems.add(raw + ": not a tag in structure_tags.json"); ids = List.of(); }
			} else if (t.contains("*")) {
				Pattern p = glob(t);
				ids = all.stream().filter(id -> p.matcher(id).matches()).toList();
			} else {
				ids = all.contains(t) ? List.of(t) : List.of();
			}
			if (ids.isEmpty()) problems.add(raw + " matches nothing");
			(neg ? exclude : include).addAll(ids);
		}
		include.removeAll(exclude);
		return new TreeSet<>(include);
	}

	/**
	 * One row per tag of the family (type/*, group/*), each structure in the smallest such tag that has it
	 * (so pillager outposts land in camp/fort/tower, not the enemy_building union); ties by name. Structures
	 * in no tag of the family get the last row. MOD = namespace; NONE = one row.
	 */
	static List<Row> rowsFor(Set<String> ids, RowBy by, Map<String, Set<String>> packTags) {
		String none = by == RowBy.GROUP ? "(no group: standalone)" : "(no " + by.name().toLowerCase(Locale.ROOT) + "/* tag)";
		Map<String, List<ResourceLocation>> rows = new TreeMap<>();
		List<ResourceLocation> leftover = new ArrayList<>();
		String prefix = by == RowBy.TYPE ? "type/" : "group/";
		List<Map.Entry<String, Set<String>>> family = packTags.entrySet().stream()
				.filter(e -> e.getKey().startsWith(prefix))
				.sorted(Comparator.<Map.Entry<String, Set<String>>>comparingInt(e -> e.getValue().size()).thenComparing(Map.Entry::getKey))
				.toList();
		for (String id : ids) {
			ResourceLocation rl = new ResourceLocation(id);
			String row = switch (by) {
				case NONE -> "all";
				case MOD -> rl.getNamespace();
				default -> family.stream().filter(e -> e.getValue().contains(id)).map(Map.Entry::getKey).findFirst().orElse(null);
			};
			if (row == null) leftover.add(rl);
			else rows.computeIfAbsent(row, k -> new ArrayList<>()).add(rl);
		}
		List<Row> out = new ArrayList<>();
		rows.forEach((name, list) -> out.add(new Row(name, list)));
		if (!leftover.isEmpty()) out.add(new Row(none, leftover));
		return out;
	}

	/** One row per template folder ("ns:village/plains/houses"), folders and ids in name order. */
	static List<Row> templateRows(Set<String> ids) {
		Map<String, List<ResourceLocation>> rows = new TreeMap<>();
		for (String id : ids) {
			ResourceLocation rl = new ResourceLocation(id);
			int slash = rl.getPath().lastIndexOf('/');
			String folder = rl.getNamespace() + ":" + (slash < 0 ? "" : rl.getPath().substring(0, slash));
			rows.computeIfAbsent(folder, k -> new ArrayList<>()).add(rl);
		}
		List<Row> out = new ArrayList<>();
		rows.forEach((name, list) -> out.add(new Row(name, list)));
		return out;
	}

	/** structure_tags.json resolved over the live structure ids (unknown ids are just skipped here). */
	static Map<String, Set<String>> packTags(List<String> all) {
		Map<String, List<String>> defs = new LinkedHashMap<>();
		try {
			String text = WorldgenFiles.readText(TAGS);
			if (text == null) return Map.of();
			JsonObject tags = PoolTuner.parseLenient(text).getAsJsonObject("tags");
			if (tags == null) return Map.of();
			for (Map.Entry<String, JsonElement> e : tags.entrySet()) {
				List<String> values = new ArrayList<>();
				JsonElement v = e.getValue().isJsonObject() ? e.getValue().getAsJsonObject().get("values") : null;
				if (v != null && v.isJsonArray()) for (JsonElement x : v.getAsJsonArray()) values.add(x.getAsString());
				defs.put(e.getKey(), values);
			}
		} catch (RuntimeException | java.io.IOException e) {
			LOG.warn("[ceplace] could not read {}: {}", TAGS, e.toString());
			return Map.of();
		}
		Map<String, Set<String>> done = new LinkedHashMap<>();
		for (String name : defs.keySet()) resolve(name, defs, all, done, new LinkedHashSet<>());
		return done;
	}

	private static Set<String> resolve(String name, Map<String, List<String>> defs, List<String> all, Map<String, Set<String>> done, Set<String> resolving) {
		if (done.containsKey(name)) return done.get(name);
		if (!defs.containsKey(name) || !resolving.add(name)) return Set.of();
		Set<String> inc = new LinkedHashSet<>(), exc = new LinkedHashSet<>();
		for (String raw : defs.get(name)) {
			boolean neg = raw.startsWith("!");
			String v = neg ? raw.substring(1) : raw;
			Collection<String> ids;
			if (v.startsWith("#")) ids = resolve(v.substring(1), defs, all, done, resolving);
			else if (v.contains("*")) { Pattern p = glob(v); ids = all.stream().filter(id -> p.matcher(id).matches()).toList(); }
			else ids = all.contains(v) ? List.of(v) : List.of();
			(neg ? exc : inc).addAll(ids);
		}
		resolving.remove(name);
		inc.removeAll(exc);
		done.put(name, inc);
		return inc;
	}

	static Pattern glob(String g) {
		StringBuilder sb = new StringBuilder("^");
		String[] parts = g.split("\\*", -1);
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(".*");
			sb.append(Pattern.quote(parts[i]));
		}
		return Pattern.compile(sb.append('$').toString());
	}

	private static Collection<String> registryTag(Registry<Structure> reg, String tagId) {
		ResourceLocation rl = ResourceLocation.tryParse(tagId);
		if (rl == null) return null;
		return reg.getTag(TagKey.create(Registry.STRUCTURE_REGISTRY, rl))
				.map(set -> set.stream().map(Holder::unwrapKey).filter(java.util.Optional::isPresent)
						.map(k -> k.get().location().toString()).toList())
				.orElse(null);
	}

	private static ResourceManager templateIdsFrom;
	private static List<String> templateIdsCache = List.of();

	/**
	 * Every template id: each data pack's {@code structures/**.nbt} plus the world's generated/ folder. Not vanilla
	 * StructureTemplateManager#listTemplates: the merged resource listing in this pack hands it a bare
	 * {@code structures.nbt} path and its substring throws (StringIndexOutOfBounds, 09_phase_9.md 3.1), which killed
	 * the whole templates mode. Per pack, a stray path is just skipped. Cached until the next /reload.
	 */
	private static synchronized List<String> templateIds(MinecraftServer server) {
		ResourceManager rm = server.getResourceManager();
		if (rm == templateIdsFrom) return templateIdsCache;
		Set<String> ids = new TreeSet<>();
		rm.listPacks().forEach(pack -> {
			for (String ns : pack.getNamespaces(PackType.SERVER_DATA)) {
				try {
					for (ResourceLocation rl : pack.getResources(PackType.SERVER_DATA, ns, TEMPLATE_DIR, p -> p.getPath().endsWith(".nbt"))) {
						String path = rl.getPath();
						if (path.startsWith(TEMPLATE_DIR + "/") && path.length() > TEMPLATE_DIR.length() + 1 + 4)
							ids.add(rl.getNamespace() + ":" + path.substring(TEMPLATE_DIR.length() + 1, path.length() - 4));
					}
				} catch (RuntimeException e) {
					LOG.warn("[ceplace] could not list templates in pack {} ({}): {}", pack.getName(), ns, e.toString());
				}
			}
		});
		Path generated = server.getWorldPath(LevelResource.GENERATED_DIR).normalize();
		if (Files.isDirectory(generated)) {
			try (Stream<Path> nsDirs = Files.list(generated)) {
				nsDirs.filter(Files::isDirectory).forEach(nsDir -> {
					Path dir = nsDir.resolve(TEMPLATE_DIR);
					if (!Files.isDirectory(dir)) return;
					try (Stream<Path> files = Files.walk(dir)) {
						files.filter(f -> f.toString().endsWith(".nbt")).forEach(f -> {
							String rel = dir.relativize(f).toString().replace('\\', '/');
							ids.add(nsDir.getFileName() + ":" + rel.substring(0, rel.length() - 4));
						});
					} catch (IOException e) {
						LOG.warn("[ceplace] could not list {}: {}", dir, e.toString());
					}
				});
			} catch (IOException e) {
				LOG.warn("[ceplace] could not list {}: {}", generated, e.toString());
			}
		}
		ids.removeIf(id -> ResourceLocation.tryParse(id) == null);
		templateIdsFrom = rm;
		templateIdsCache = List.copyOf(ids);
		return templateIdsCache;
	}

	// ── suggestions ──────────────────────────────────────────────────────────

	private static final SuggestionProvider<CommandSourceStack> ROWS = (ctx, b) ->
			SharedSuggestionProvider.suggest(List.of("type", "group", "mod", "none"), b);

	private static final SuggestionProvider<CommandSourceStack> STRUCTURE_FILTER = (ctx, b) -> {
		Registry<Structure> reg = ctx.getSource().registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		List<String> ids = reg.keySet().stream().map(ResourceLocation::toString).sorted().toList();
		Set<String> opts = new TreeSet<>(ids);
		ids.forEach(id -> opts.add("@" + id.substring(0, id.indexOf(':'))));
		packTags(ids).keySet().forEach(t -> opts.add("#" + t));
		reg.getTagNames().forEach(t -> opts.add("#" + t.location()));
		return suggestLastTerm(b, opts);
	};

	private static final SuggestionProvider<CommandSourceStack> TEMPLATE_FILTER = (ctx, b) -> {
		Set<String> opts = new TreeSet<>();
		for (String id : templateIds(ctx.getSource().getServer())) {
			int colon = id.indexOf(':');
			opts.add("@" + id.substring(0, colon));
			int slash = id.lastIndexOf('/');
			if (slash > colon) opts.add(id.substring(0, slash) + "/*");
		}
		return suggestLastTerm(b, opts);
	};

	/** Greedy argument with several terms: suggest for the one being typed (keeping a leading '!'). */
	private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestLastTerm(SuggestionsBuilder b, Collection<String> opts) {
		String rem = b.getRemaining();
		int cut = rem.lastIndexOf(' ') + 1;
		SuggestionsBuilder sb = b.createOffset(b.getStart() + cut);
		String term = rem.substring(cut);
		String neg = term.startsWith("!") ? "!" : "";
		return SharedSuggestionProvider.suggest(opts.stream().map(o -> neg + o).toList(), sb);
	}
}
