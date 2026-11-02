package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.util.WorldgenFiles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Lootr only converts containers whose block it knows (chests, barrels, trapped chests, shulker
 * boxes, plus its additional_chests list, and even that list only works for
 * RandomizableContainerBlockEntity). Modded loot containers in structures (IE crates in the IE
 * village houses, Farmer's Delight cabinets/baskets in Chef's Delight houses, PneumaticCraft's
 * reinforced chest in the mechanic house) therefore stay per-world loot.
 *
 * This swaps such a block for a vanilla container when a structure template places it WITH a
 * loot table (mixin/StructureTemplateLootSwapMixin, on Forge's processBlockInfos, which every
 * template placement goes through). Lootr then converts the vanilla container as usual. Only the
 * loot table (and seed / custom name) is carried over; a container without a LootTable, e.g. a
 * crate the player places or a decorative one with fixed contents, is never touched.
 *
 * Config: kubejs/generation_scripts/worldgen/loot_container_swaps.json, read at every world load.
 */
public final class LootContainerSwaps {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/loot-swaps");
	static final String CONFIG = BiomeLayoutTuner.WORLDGEN_DIR + "loot_container_swaps.json";

	static volatile Map<Block, Block> swaps = Map.of();   // package-private for headless tests

	private LootContainerSwaps() {}

	static void load() {
		Map<Block, Block> out = new IdentityHashMap<>();
		try {
			String text = WorldgenFiles.readText(CONFIG);
			if (text != null) {
				JsonObject doc = PoolTuner.parseLenient(text);
				JsonObject map = doc.has("swaps") && doc.get("swaps").isJsonObject() ? doc.getAsJsonObject("swaps") : new JsonObject();
				for (Map.Entry<String, JsonElement> e : map.entrySet()) {
					ResourceLocation toId = ResourceLocation.tryParse(e.getValue().getAsString());
					if (toId == null || !ForgeRegistries.BLOCKS.containsKey(toId)) {   // getValue() would return air
						LOG.warn("[loot-swaps] {} -> {}: unknown target block, skipped", e.getKey(), e.getValue().getAsString());
						continue;
					}
					Block to = ForgeRegistries.BLOCKS.getValue(toId);
					List<Block> from = match(e.getKey());
					if (from.isEmpty()) LOG.warn("[loot-swaps] \"{}\" matches no block, skipped", e.getKey());
					for (Block b : from) if (b != to) out.put(b, to);
				}
			}
		} catch (Exception ex) {
			LOG.error("[loot-swaps] could not read {}, no swaps this session", CONFIG, ex);
			out.clear();
		}
		swaps = Map.copyOf(out);
		LOG.info("[loot-swaps] {} block(s) are swapped for a vanilla container when a structure places them with a loot table", swaps.size());
	}

	private static List<Block> match(String glob) {
		List<Block> out = new ArrayList<>();
		if (!glob.contains("*")) {
			ResourceLocation rl = ResourceLocation.tryParse(glob);
			if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) out.add(ForgeRegistries.BLOCKS.getValue(rl));
			return out;
		}
		StringBuilder re = new StringBuilder("^");
		String[] parts = glob.split("\\*", -1);
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) re.append(".*");
			re.append(Pattern.quote(parts[i]));
		}
		Pattern p = Pattern.compile(re.append('$').toString());
		for (Map.Entry<net.minecraft.resources.ResourceKey<Block>, Block> e : ForgeRegistries.BLOCKS.getEntries()) {
			if (p.matcher(e.getKey().location().toString()).matches()) out.add(e.getValue());
		}
		return out;
	}

	/** The processed block list with loot containers swapped, or null when nothing changed. */
	public static List<StructureBlockInfo> apply(List<StructureBlockInfo> infos) {
		Map<Block, Block> map = swaps;
		if (map.isEmpty() || infos == null || !WorldgenConfig.lootContainerSwapsEnabled()) return null;
		List<StructureBlockInfo> out = null;
		for (int i = 0; i < infos.size(); i++) {
			StructureBlockInfo info = infos.get(i);
			if (info.nbt == null || !info.nbt.contains("LootTable", Tag.TAG_STRING)) continue;
			Block to = map.get(info.state.getBlock());
			if (to == null) continue;
			CompoundTag nbt = new CompoundTag();
			for (String k : new String[] {"LootTable", "LootTableSeed", "CustomName"}) {
				if (info.nbt.contains(k)) nbt.put(k, info.nbt.get(k).copy());
			}
			if (out == null) out = new ArrayList<>(infos);
			out.set(i, new StructureBlockInfo(info.pos, copyProperties(info.state, to.defaultBlockState()), nbt));
		}
		return out;
	}

	/** Carries facing / waterlogged etc. across wherever the target has a same-named property that accepts the value. */
	private static BlockState copyProperties(BlockState from, BlockState to) {
		for (Property<?> p : from.getProperties()) {
			Property<?> tp = to.getBlock().getStateDefinition().getProperty(p.getName());
			if (tp != null) to = setIfValid(to, tp, from.getValue(p));
		}
		return to;
	}

	private static <T extends Comparable<T>> BlockState setIfValid(BlockState state, Property<T> prop, Comparable<?> value) {
		for (T v : prop.getPossibleValues()) {
			if (v.equals(value)) return state.setValue(prop, v);
		}
		return state;
	}
}
