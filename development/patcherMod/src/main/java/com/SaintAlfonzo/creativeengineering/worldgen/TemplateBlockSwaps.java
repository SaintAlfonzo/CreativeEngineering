package com.SaintAlfonzo.creativeengineering.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Per-template, per-position block swaps, applied to a structure template once when it is loaded, so every later
 * placement (any rotation, any structure that uses the template) has the new block. Used for the lone "meat" blocks
 * (red glazed terracotta in butcher stalls) that become Architect's Palette fish blocks standing upright.
 *
 * Config: kubejs/generation_scripts/worldgen/meat_block_swaps.json (tools/meat_block_scan.py) and
 * nether_block_swaps.json (tools/gen_nether_swaps.py from nether_swap_rules.json), both in the same format:
 * {"templates": {"template id": [{"pos": [x, y, z], "from": "block id", "to": {"Name": ..., "Properties": {...}}}]}}.
 * A swap applies only where the template still has "from" at "pos" (a mod update that moved things is skipped and
 * logged). Read at every world load; templates load lazily, so a template loaded before (none: they are per server)
 * would keep its blocks. Switch: CEConfig [worldgen.systems] templateBlockSwaps.
 *
 * Mixin: StructureTemplateManagerSwapMixin (StructureTemplateManager#get RETURN).
 */
public final class TemplateBlockSwaps {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/template-swaps");
	/** Read in order; a later file's entry for the same template position replaces an earlier one's. */
	static final List<String> CONFIGS = List.of(BiomeLayoutTuner.WORLDGEN_DIR + "meat_block_swaps.json",
			BiomeLayoutTuner.WORLDGEN_DIR + "nether_block_swaps.json");

	private static final Field PALETTES = ObfuscationReflectionHelper.findField(StructureTemplate.class, "f_74482_");
	private static final Field PALETTE_BLOCKS = ObfuscationReflectionHelper.findField(StructureTemplate.Palette.class, "f_74645_");
	private static final Field PALETTE_CACHE = ObfuscationReflectionHelper.findField(StructureTemplate.Palette.class, "f_74646_");

	record Swap(BlockPos pos, Block from, BlockState to) {}

	private static volatile Map<ResourceLocation, List<Swap>> swaps = Map.of();
	private static final Set<StructureTemplate> DONE = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private TemplateBlockSwaps() {}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		load();
	}

	static void load() {
		Map<ResourceLocation, Map<BlockPos, Swap>> byPos = new HashMap<>();
		String reading = null;
		try {
			for (String config : CONFIGS) {
			reading = config;
			String text = WorldgenFiles.readText(config);
			if (text != null) {
				JsonObject doc = PoolTuner.parseLenient(text);
				JsonObject templates = doc.has("templates") && doc.get("templates").isJsonObject() ? doc.getAsJsonObject("templates") : new JsonObject();
				for (Map.Entry<String, JsonElement> e : templates.entrySet()) {
					ResourceLocation id = ResourceLocation.tryParse(e.getKey());
					if (id == null) continue;
					List<Swap> list = new ArrayList<>();
					for (JsonElement el : e.getValue().getAsJsonArray()) {
						JsonObject o = el.getAsJsonObject();
						var p = o.getAsJsonArray("pos");
						ResourceLocation fromId = ResourceLocation.tryParse(o.get("from").getAsString());
						BlockState to = state(o.getAsJsonObject("to"));
						if (fromId == null || !ForgeRegistries.BLOCKS.containsKey(fromId) || to == null) {
							LOG.warn("[template-swaps] {}: bad entry {}, skipped", id, o);
							continue;
						}
						list.add(new Swap(new BlockPos(p.get(0).getAsInt(), p.get(1).getAsInt(), p.get(2).getAsInt()),
								ForgeRegistries.BLOCKS.getValue(fromId), to));
					}
					Map<BlockPos, Swap> at = byPos.computeIfAbsent(id, k -> new java.util.LinkedHashMap<>());
					for (Swap sw : list) at.put(sw.pos(), sw);
				}
			}
			}
		} catch (Exception ex) {
			LOG.error("[template-swaps] could not read {}, no swaps this session", reading, ex);
			byPos.clear();
		}
		Map<ResourceLocation, List<Swap>> out = new HashMap<>();
		int n = 0;
		for (Map.Entry<ResourceLocation, Map<BlockPos, Swap>> e : byPos.entrySet()) {
			out.put(e.getKey(), List.copyOf(e.getValue().values()));
			n += e.getValue().size();
		}
		swaps = Map.copyOf(out);
		DONE.clear();
		LOG.info("[template-swaps] {} block swap(s) in {} template(s){}", n, out.size(),
				WorldgenConfig.templateBlockSwapsEnabled() ? "" : " (off in CEConfig [worldgen.systems])");
	}

	/** {"Name": id, "Properties": {...}}: null for an unknown block. */
	private static BlockState state(JsonObject o) {
		ResourceLocation id = ResourceLocation.tryParse(o.get("Name").getAsString());
		if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) return null;
		CompoundTag tag = new CompoundTag();
		tag.putString("Name", id.toString());
		if (o.has("Properties")) {
			CompoundTag props = new CompoundTag();
			for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("Properties").entrySet()) props.putString(e.getKey(), e.getValue().getAsString());
			tag.put("Properties", props);
		}
		BlockState s = NbtUtils.readBlockState(tag);
		return s.isAir() && !id.equals(ForgeRegistries.BLOCKS.getKey(Blocks.AIR)) ? null : s;
	}

	/** StructureTemplateManager#get: swap the template's marked blocks, once per loaded template. */
	@SuppressWarnings("unchecked")
	public static void apply(ResourceLocation id, StructureTemplate template) {
		List<Swap> list = swaps.get(id);
		if (list == null || !WorldgenConfig.templateBlockSwapsEnabled() || !DONE.add(template)) return;
		int done = 0, missed = 0;
		try {
			for (Object palette : (List<Object>) PALETTES.get(template)) {
				List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>((List<StructureTemplate.StructureBlockInfo>) PALETTE_BLOCKS.get(palette));
				for (Swap s : list) {
					boolean hit = false;
					for (int i = 0; i < blocks.size(); i++) {
						StructureTemplate.StructureBlockInfo info = blocks.get(i);
						if (info.pos.equals(s.pos()) && info.state.is(s.from())) {
							blocks.set(i, new StructureTemplate.StructureBlockInfo(info.pos, s.to(), null));
							hit = true;
							done++;
							break;
						}
					}
					if (!hit) missed++;
				}
				PALETTE_BLOCKS.set(palette, blocks);
				((Map<?, ?>) PALETTE_CACHE.get(palette)).clear();
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOG.error("[template-swaps] {}: could not swap, template left as it is", id, e);
			return;
		}
		if (missed > 0) LOG.warn("[template-swaps] {}: {} marked position(s) no longer hold their block (template changed?), skipped", id, missed);
		LOG.debug("[template-swaps] {}: {} block(s) swapped", id, done);
	}
}
