package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Removes mob spawners of the CWGConfig [worldgen.systems] removeSpawners entities (pillagers by default) from a structure
 * template when it loads, so every placement of it, jar templates included, has air there instead. The spawn boxes
 * (StructureSpawnBoxes) respawn the structure's mobs instead. A spawner counts when its SpawnData entity or any
 * SpawnPotentials entry is listed; a template that loses one also loses its spawners with no mob at all (the
 * Mysticrift End tower's two empty ones). Feature_writeup/structure_tuning.md "Spawners and spawn boxes".
 *
 * Mixin: StructureTemplateManagerSwapMixin (StructureTemplateManager#get RETURN), after TemplateBlockSwaps.
 */
public final class TemplateSpawnerFilter {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/spawner-filter");
	private static final Set<StructureTemplate> DONE = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private TemplateSpawnerFilter() {}

	/** The entity ids a spawner's NBT can spawn (SpawnData and SpawnPotentials); empty for a spawner with no mob. */
	static List<String> entities(CompoundTag nbt) {
		List<String> out = new ArrayList<>();
		if (nbt == null) return out;
		String id = nbt.getCompound("SpawnData").getCompound("entity").getString("id");
		if (!id.isEmpty()) out.add(id);
		ListTag pots = nbt.getList("SpawnPotentials", Tag.TAG_COMPOUND);
		for (int i = 0; i < pots.size(); i++) {
			String p = pots.getCompound(i).getCompound("data").getCompound("entity").getString("id");
			if (!p.isEmpty()) out.add(p);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	public static void apply(ResourceLocation id, StructureTemplate template) {
		List<? extends String> listed = WorldgenConfig.removeSpawners();
		if (listed.isEmpty() || !DONE.add(template)) return;
		Set<String> ids = Set.copyOf(listed);
		try {
			List<Object> palettes = (List<Object>) TemplateBlockSwaps.PALETTES.get(template);
			boolean any = false;
			for (Object palette : palettes) {
				for (StructureTemplate.StructureBlockInfo info : (List<StructureTemplate.StructureBlockInfo>) TemplateBlockSwaps.PALETTE_BLOCKS.get(palette)) {
					if (info.state.is(Blocks.SPAWNER) && entities(info.nbt).stream().anyMatch(ids::contains)) { any = true; break; }
				}
				if (any) break;
			}
			if (!any) return;
			int removed = 0, empty = 0;
			for (Object palette : palettes) {
				List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>((List<StructureTemplate.StructureBlockInfo>) TemplateBlockSwaps.PALETTE_BLOCKS.get(palette));
				for (int i = 0; i < blocks.size(); i++) {
					StructureTemplate.StructureBlockInfo info = blocks.get(i);
					if (!info.state.is(Blocks.SPAWNER)) continue;
					List<String> mobs = entities(info.nbt);
					boolean listedMob = mobs.stream().anyMatch(ids::contains);
					if (!listedMob && !mobs.isEmpty()) continue;
					blocks.set(i, new StructureTemplate.StructureBlockInfo(info.pos, Blocks.AIR.defaultBlockState(), null));
					if (listedMob) removed++;
					else empty++;
				}
				TemplateBlockSwaps.PALETTE_BLOCKS.set(palette, blocks);
				((Map<?, ?>) TemplateBlockSwaps.PALETTE_CACHE.get(palette)).clear();
			}
			LOG.info("[spawner-filter] {}: {} spawner(s) of {} removed{}", id, removed, ids, empty > 0 ? ", plus " + empty + " with no mob" : "");
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOG.error("[spawner-filter] {}: could not filter, template left as it is", id, e);
		}
	}
}
