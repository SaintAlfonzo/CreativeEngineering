package com.SaintAlfonzo.creativedevtools.perf;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.surface.PassCountCache;
import com.SaintAlfonzo.creativecompat.surface.PassItems;
import com.SaintAlfonzo.creativecompat.surface.PassThrough;
import com.SaintAlfonzo.creativecompat.surface.PassTier;
import com.SaintAlfonzo.creativecompat.surface.PassValues;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

/**
 * {@code /cdevbench passthrough [calls]} (plan_passthrough.md 7.1): micro benchmark of the pass-through code in Creative
 * Compat ({@code surface/PassThrough}). Builds fixtures near the build limit above the caller (16x16 wheat field, a
 * sparse field of every other block, a 5x5x5 cobweb cube, one lone carrot, plain air), times each case after a
 * warmup in 20 batches of {@code calls / 20}, and reports ns per call (median and 99th percentile of the batches) to
 * chat and to {@code kubejs/exported/perf/passthrough_bench.json}. The fixture blocks are put back afterwards. The
 * server thread is busy for the whole run (seconds to tens of seconds at the default 1,000,000 calls).
 *
 * <p>{@code /cdevbench passthrough enabled <true|false>}: sets {@code [surface.passthrough] enabled} in
 * creativecompat-common.toml, saves it and re-sends the values to players at once; the perf suite plan
 * ({@code tools/perf/suite/plans/passthrough.py}) measures every scene both ways with it.
 *
 * <p>Registered only when Creative Compat is loaded ({@code CreativeDevtools}). Permission level 3.
 */
public final class PassBenchCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String OUT = "kubejs/exported/perf/passthrough_bench.json";
	private static final int BATCHES = 20;
	/** Plan 7.2 budget for the per-entity tick in the dense field and the web cube. */
	private static final double BUDGET_NS = 1000.0;
	private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

	private PassBenchCommand() {}

	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cdevbench")
				.requires(src -> src.hasPermission(3))
				.then(Commands.literal("passthrough")
						.executes(ctx -> bench(ctx.getSource(), 1_000_000))
						.then(Commands.argument("calls", IntegerArgumentType.integer(BATCHES * 10, 100_000_000))
								.executes(ctx -> bench(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "calls"))))
						.then(Commands.literal("enabled")
								.executes(ctx -> showEnabled(ctx.getSource()))
								.then(Commands.argument("value", BoolArgumentType.bool())
										.executes(ctx -> setEnabled(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))));
	}

	private static void say(CommandSourceStack src, String text, ChatFormatting color) {
		src.sendSuccess(Component.literal("[cdevbench] ").withStyle(ChatFormatting.GOLD)
				.append(Component.literal(text).withStyle(color)), true);
	}

	private static int showEnabled(CommandSourceStack src) {
		say(src, "[surface.passthrough] enabled = " + PassValues.local().enabled, ChatFormatting.WHITE);
		return 1;
	}

	private static int setEnabled(CommandSourceStack src, boolean value) {
		CompatConfig.SURFACE.passthrough.enabled.set(value);
		CompatConfig.SPEC.save();
		PassValues.refresh();
		LOGGER.info("[cdevbench] [surface.passthrough] enabled set to {}", value);
		say(src, "[surface.passthrough] enabled = " + value + " (saved, sent to players)", ChatFormatting.GREEN);
		return 1;
	}

	/** One timed case. */
	private record Case(String id, String what, LongSupplier call) {}

	private static int bench(CommandSourceStack src, int calls) {
		ServerLevel level = src.getLevel();
		BlockPos at = new BlockPos(src.getPosition());
		int y = level.getMaxBuildHeight() - 12;
		int bx = at.getX(), bz = at.getZ() - 8;
		// x layout: air cell bx-8, lone carrot bx-4, wheat field bx+4..+19, sparse field bx+22..+37, web cube bx+40..+44
		int fieldX = bx + 4, sparseX = bx + 22, webX = bx + 40;
		if (!level.hasChunksAt(bx - 10, y, bz - 2, bx + 46, y + 6, bz + 18)) {
			say(src, "the fixture area (x " + (bx - 10) + ".." + (bx + 46) + ", z " + (bz - 2) + ".." + (bz + 18)
					+ ") is not all loaded; stand in a loaded area with view distance 4 or more", ChatFormatting.RED);
			return 0;
		}
		boolean tagged = PassThrough.tierOf(Blocks.WHEAT) == PassTier.DENSE_CROP && PassThrough.tierOf(Blocks.COBWEB) == PassTier.COBWEB;
		if (!tagged) say(src, "wheat or cobweb is not in its pass-through tier (tags missing?): results show the untiered path",
				ChatFormatting.YELLOW);

		Map<BlockPos, BlockState> saved = new LinkedHashMap<>();
		BlockState farmland = Blocks.FARMLAND.defaultBlockState();
		BlockState wheat = ((CropBlock) Blocks.WHEAT).getStateForAge(7);
		BlockState carrot = ((CropBlock) Blocks.CARROTS).getStateForAge(7);
		BlockState web = Blocks.COBWEB.defaultBlockState();
		try {
			for (int dx = 0; dx < 16; dx++) {
				for (int dz = 0; dz < 16; dz++) {
					put(level, saved, new BlockPos(fieldX + dx, y, bz + dz), farmland);
					put(level, saved, new BlockPos(fieldX + dx, y + 1, bz + dz), wheat);
					put(level, saved, new BlockPos(sparseX + dx, y, bz + dz), farmland);
					put(level, saved, new BlockPos(sparseX + dx, y + 1, bz + dz), (dx + dz) % 2 == 0 ? wheat : Blocks.AIR.defaultBlockState());
				}
			}
			for (int dx = 0; dx < 5; dx++) {
				for (int dy = 0; dy < 5; dy++) {
					for (int dz = 0; dz < 5; dz++) put(level, saved, new BlockPos(webX + dx, y + dy, bz + dz), web);
				}
			}
			put(level, saved, new BlockPos(bx - 4, y, bz), farmland);
			put(level, saved, new BlockPos(bx - 4, y + 1, bz), carrot);
			put(level, saved, new BlockPos(bx - 8, y + 1, bz), Blocks.AIR.defaultBlockState());
			put(level, saved, new BlockPos(bx - 8, y + 2, bz), Blocks.AIR.defaultBlockState());

			BlockPos fieldMid = new BlockPos(fieldX + 8, y + 1, bz + 8);
			BlockPos fieldEdge = new BlockPos(fieldX, y + 1, bz + 8);
			BlockPos webMid = new BlockPos(webX + 2, y + 2, bz + 2);
			Entity air = mob(level, EntityType.ZOMBIE, bx - 7.5, y + 1, bz + 0.5);
			Entity lone = mob(level, EntityType.ZOMBIE, bx - 3.5, y + 1, bz + 0.5);
			Entity inField = mob(level, EntityType.ZOMBIE, fieldX + 8.5, y + 1, bz + 8.5);
			Entity inSparse = mob(level, EntityType.ZOMBIE, sparseX + 8.5, y + 1, bz + 8.5);
			Entity inWeb = mob(level, EntityType.ZOMBIE, webX + 2.5, y + 2, bz + 2.5);
			Entity horse = mob(level, EntityType.HORSE, fieldX + 8.0, y + 1, bz + 8.0);
			ItemEntity item = new ItemEntity(level, webX + 2.4, y + 4.6, bz + 2.6, new ItemStack(Items.WHEAT_SEEDS));
			double ix = webX + 2.3, iz = bz + 2.6;
			double[] itemTo = {webX + 2.4, y + 4.6, iz};

			List<Case> cases = List.of(
					new Case("ring_count", "ring count, field middle (8 lookups)",
							() -> PassThrough.neighbours(level, fieldMid, PassTier.DENSE_CROP, PassTier.Neighbours.SIDES)),
					new Case("ring_count_edge", "ring count, field edge",
							() -> PassThrough.neighbours(level, fieldEdge, PassTier.DENSE_CROP, PassTier.Neighbours.SIDES)),
					new Case("cube_count", "cube count, web cube middle (26 lookups)",
							() -> PassThrough.neighbours(level, webMid, PassTier.COBWEB, PassTier.Neighbours.FACES)),
					new Case("tick_air", "entity tick, zombie in air (fast path)", () -> tick(air)),
					new Case("tick_lone_crop", "entity tick, zombie in a lone carrot", () -> tick(lone)),
					new Case("tick_field", "entity tick, zombie in the middle of the wheat field (first in its cells this tick)",
							() -> cold(inField)),
					new Case("tick_field_shared", "entity tick, wheat field, cells already counted this tick (herd)",
							() -> tick(inField)),
					new Case("tick_sparse", "entity tick, zombie in the sparse field", () -> tick(inSparse)),
					new Case("tick_web_cube", "entity tick, zombie in the middle of the web cube (first in its cells this tick)",
							() -> cold(inWeb)),
					new Case("tick_web_cube_shared", "entity tick, web cube, cells already counted this tick (crowd)",
							() -> tick(inWeb)),
					new Case("tick_horse_field", "entity tick, horse over 2x2 field cells (first in its cells this tick)",
							() -> cold(horse)),
					new Case("item_roll", "item stick roll with its path clip (top of the web cube)",
							() -> stuck(PassItems.pathStop(item, level, PassValues.local(), ix, y + 5.2, iz,
									itemTo[0], itemTo[1], itemTo[2]))));

			long sink = 0;
			int per = Math.max(1, calls / BATCHES);
			int warm = Math.max(10_000, calls / 5);
			List<Map<String, Object>> rows = new ArrayList<>();
			long started = System.nanoTime();
			for (Case c : cases) {
				for (int i = 0; i < warm; i++) sink += c.call.getAsLong();
				double[] ns = new double[BATCHES];
				for (int b = 0; b < BATCHES; b++) {
					long t = System.nanoTime();
					for (int i = 0; i < per; i++) sink += c.call.getAsLong();
					ns[b] = (System.nanoTime() - t) / (double) per;
				}
				double[] sorted = ns.clone();
				Arrays.sort(sorted);
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("id", c.id);
				row.put("what", c.what);
				row.put("median_ns", round(sorted[BATCHES / 2]));
				row.put("p99_ns", round(sorted[(int) Math.ceil(0.99 * BATCHES) - 1]));
				row.put("min_ns", round(sorted[0]));
				row.put("mean_ns", round(Arrays.stream(ns).average().orElse(0)));
				rows.add(row);
			}
			double seconds = (System.nanoTime() - started) / 1e9;

			Map<String, Object> root = new LinkedHashMap<>();
			root.put("date", LocalDateTime.now().toString());
			root.put("calls", per * BATCHES);
			root.put("batches", BATCHES);
			root.put("warmup", warm);
			root.put("enabled", PassValues.local().enabled);
			root.put("tagged", tagged);
			root.put("budget_ns", BUDGET_NS);
			root.put("seconds", round(seconds));
			root.put("cases", rows);
			root.put("sink", sink);
			Path out = FMLPaths.GAMEDIR.get().resolve(OUT);
			Files.createDirectories(out.getParent());
			Files.writeString(out, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n",
					StandardCharsets.UTF_8);

			say(src, (per * BATCHES) + " calls per case, " + round(seconds) + " s, enabled=" + PassValues.local().enabled
					+ ", tagged=" + tagged, ChatFormatting.GRAY);
			for (Map<String, Object> r : rows) {
				double med = (Double) r.get("median_ns");
				boolean budgeted = "tick_field".equals(r.get("id")) || "tick_web_cube".equals(r.get("id"));
				ChatFormatting color = budgeted ? (med < BUDGET_NS ? ChatFormatting.GREEN : ChatFormatting.RED) : ChatFormatting.WHITE;
				say(src, r.get("id") + ": " + r.get("median_ns") + " ns (p99 " + r.get("p99_ns") + ")", color);
			}
			say(src, "written to " + OUT, ChatFormatting.GRAY);
			LOGGER.info("[cdevbench] passthrough: {} calls per case in {} s, written to {}", per * BATCHES, round(seconds), OUT);
			return 1;
		} catch (Exception e) {
			LOGGER.warn("[cdevbench] passthrough failed", e);
			say(src, "failed: " + e, ChatFormatting.RED);
			return 0;
		} finally {
			List<Map.Entry<BlockPos, BlockState>> back = new ArrayList<>(saved.entrySet());
			for (int i = back.size() - 1; i >= 0; i--) level.setBlock(back.get(i).getKey(), back.get(i).getValue(), FLAGS);
		}
	}

	private static void put(ServerLevel level, Map<BlockPos, BlockState> saved, BlockPos pos, BlockState state) {
		saved.putIfAbsent(pos.immutable(), level.getBlockState(pos));
		level.setBlock(pos, state, FLAGS);
	}

	/** A mob that is never added to the level: the hook only reads its box, motion, attributes and type. */
	private static Entity mob(ServerLevel level, EntityType<?> type, double x, double y, double z) {
		Entity e = type.create(level);
		if (e == null) throw new IllegalStateException("cannot create " + type);
		e.setPos(x, y, z);
		e.setDeltaMovement(new Vec3(0.05, 0.0, 0.0));
		return e;
	}

	private static long tick(Entity e) {
		PassThrough.afterInsideBlocks(e);
		return Double.doubleToRawLongBits(e.getDeltaMovement().x) & 1L;
	}

	/** The tick of the first entity in its cells this tick: the per-tick neighbor count cache emptied first. */
	private static long cold(Entity e) {
		PassCountCache.get().clear();
		return tick(e);
	}

	private static long stuck(double[] p) {
		return p == null ? 0L : 1L;
	}

	private static double round(double v) {
		return Math.round(v * 10.0) / 10.0;
	}
}
