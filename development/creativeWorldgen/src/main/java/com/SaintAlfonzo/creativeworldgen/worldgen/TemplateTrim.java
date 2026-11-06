package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drops the parts of a template that only exist to flatten the ground around it. Structure templates made
 * in a flat test world (most MCreator structure mods) save a whole box: the building, a grass/dirt pad
 * reaching several blocks past it, and every air block above. Placed on a slope, the pad is a flat square
 * slab and the air cuts a square notch into the hill.
 *
 * <p>For a structure in the structure tag {@code creativeengineering:template_trim} (kubejs
 * worldgen/terrain_adaptation.js), every template placement splits the template into columns. A column
 * with a built block (anything that isn't air, pad or plant, see {@link #kind}) is kept whole. In every
 * other column the air, the pad blocks (block tag {@code creativeengineering:template_trim/pad}) and the
 * plants on them ({@code creativeengineering:template_trim/plants}) are not placed, so the natural ground
 * stays there. A template with no built column at all (a foundation plate another piece stands on) is
 * left alone.
 *
 * <p>Structures in {@code creativeengineering:template_trim/air} get the mild form ({@link #trimAir}): only the
 * air in columns with nothing solid (no built block, pad or plant) is dropped. For a tall rigid structure whose
 * pieces carry a big air box (Terralith's spire) and whose snow cover rests on another piece's stone, where the
 * full trim would strip the snow.
 *
 * <p>Runs on the same hook as {@link GroundMatch} (mixin/StructureTemplateGroundMatchMixin, trim first)
 * and only for placements inside StructureStart#placeInChunk: world generation and /place structure.
 * {@link TerrainAdaptation}'s soft beard and {@link EarthFill} read the same columns from the raw template
 * through {@link #columns}, so the beard only flattens under what is placed and earth fill only counts
 * earth that is placed.
 */
public final class TemplateTrim {

	static final TagKey<Block> PAD = BlockTags.create(new ResourceLocation(CreativeWorldgen.NS, "template_trim/pad"));
	static final TagKey<Block> PLANTS = BlockTags.create(new ResourceLocation(CreativeWorldgen.NS, "template_trim/plants"));

	private TemplateTrim() {}

	/** What a template block is for the trim. MARKER = structure block, jigsaw, structure void: never placed as-is, never dropped. */
	enum Kind { AIR, PAD, PLANT, MARKER, BUILT }

	static Kind kind(BlockState state) {
		if (state.isAir()) return Kind.AIR;
		if (state.is(Blocks.STRUCTURE_BLOCK) || state.is(Blocks.JIGSAW) || state.is(Blocks.STRUCTURE_VOID)) return Kind.MARKER;
		if (state.is(PLANTS)) return Kind.PLANT;
		if (state.is(PAD)) return Kind.PAD;
		return Kind.BUILT;
	}

	static long column(int x, int z) { return ChunkPos.asLong(x, z); }

	// ── a template's columns (raw, local coordinates) ────────────────────────

	/**
	 * A template's columns in local coordinates: {@code solid} has any block that places something (pad and
	 * plants included), {@code built} a built block.
	 */
	record Columns(LongSet solid, LongSet built) {
		/** The columns the template covers once placed: built ones when trimmed, all solid ones otherwise or when nothing is built. */
		LongSet footprint(boolean trimmed) { return trimmed && !built.isEmpty() ? built : solid; }

		/** Whether a trimmed template still places blocks in this local column. */
		boolean placed(int x, int z) { return built.isEmpty() || built.contains(column(x, z)); }
	}

	private static final Map<StructureTemplate, Columns> COLUMNS = new ConcurrentHashMap<>();

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) { COLUMNS.clear(); }

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) { COLUMNS.clear(); }   // the pad and plant tags decide the kinds

	static Columns columns(StructureTemplate template) {
		return COLUMNS.computeIfAbsent(template, t -> columns(EarthFill.firstPalette(t)));
	}

	static Columns columns(List<StructureBlockInfo> blocks) {
		LongOpenHashSet solid = new LongOpenHashSet(), built = new LongOpenHashSet();
		for (StructureBlockInfo info : blocks) {
			Kind k = kind(info.state);
			if (k == Kind.AIR || k == Kind.MARKER) continue;
			long c = column(info.pos.getX(), info.pos.getZ());
			solid.add(c);
			if (k == Kind.BUILT) built.add(c);
		}
		return new Columns(solid, built);
	}

	// ── trimming a placement ─────────────────────────────────────────────────

	/** A trimmed block list and what was dropped from it. */
	record Result(List<StructureBlockInfo> infos, int columns, int air, int pad, int plants) {}

	/**
	 * The level-free core: drops air, pad and plants in every column (by the infos' own x, z) without a built
	 * block. Null when nothing changes, including a list with no built column at all.
	 */
	static Result trim(List<StructureBlockInfo> infos) {
		Kind[] kinds = new Kind[infos.size()];
		LongOpenHashSet built = new LongOpenHashSet();
		for (int i = 0; i < infos.size(); i++) {
			StructureBlockInfo info = infos.get(i);
			kinds[i] = kind(info.state);
			if (kinds[i] == Kind.BUILT) built.add(column(info.pos.getX(), info.pos.getZ()));
		}
		if (built.isEmpty()) return null;

		List<StructureBlockInfo> out = new ArrayList<>(infos.size());
		LongOpenHashSet trimmed = new LongOpenHashSet();
		int air = 0, pad = 0, plants = 0;
		for (int i = 0; i < infos.size(); i++) {
			StructureBlockInfo info = infos.get(i);
			long c = column(info.pos.getX(), info.pos.getZ());
			if (!built.contains(c)) {
				switch (kinds[i]) {
					case AIR -> { air++; trimmed.add(c); continue; }
					case PAD -> { pad++; trimmed.add(c); continue; }
					case PLANT -> { plants++; trimmed.add(c); continue; }
					default -> { }
				}
			}
			out.add(info);
		}
		if (air + pad + plants == 0) return null;
		return new Result(out, trimmed.size(), air, pad, plants);
	}

	/**
	 * The air-only core (template_trim/air): drops the air in every column without a solid block (built, pad or
	 * plant). Pad and plants are kept everywhere. Null when nothing changes.
	 */
	static Result trimAir(List<StructureBlockInfo> infos) {
		LongOpenHashSet solid = new LongOpenHashSet();
		for (StructureBlockInfo info : infos) {
			Kind k = kind(info.state);
			if (k != Kind.AIR && k != Kind.MARKER) solid.add(column(info.pos.getX(), info.pos.getZ()));
		}
		if (solid.isEmpty()) return null;

		List<StructureBlockInfo> out = new ArrayList<>(infos.size());
		LongOpenHashSet trimmed = new LongOpenHashSet();
		int air = 0;
		for (StructureBlockInfo info : infos) {
			long c = column(info.pos.getX(), info.pos.getZ());
			if (!solid.contains(c) && info.state.isAir()) { air++; trimmed.add(c); continue; }
			out.add(info);
		}
		if (air == 0) return null;
		return new Result(out, trimmed.size(), air, 0, 0);
	}

	/**
	 * Forge's processBlockInfos, the processed list of one template placement (world positions, the whole
	 * template): the trimmed list, or null to leave it alone.
	 */
	public static List<StructureBlockInfo> apply(LevelAccessor level, List<StructureBlockInfo> infos) {
		if (infos == null || infos.isEmpty()) return null;
		StructureStart placing = GroundMatch.placing();   // null: a structure block, /place template, a feature
		if (placing == null || !WorldgenConfig.templateTrimEnabled()) return null;
		// a meta structure's part trims as the structure it came from (a tower beside a village)
		Structure structure = MetaStructure.structureAt(placing, infos.get(0).pos);
		Result r = TerrainAdaptation.trims(structure) ? trim(infos) : TerrainAdaptation.airTrims(structure) ? trimAir(infos) : null;
		return r == null ? null : r.infos();
	}
}
