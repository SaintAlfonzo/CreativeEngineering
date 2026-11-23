// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedevtools.dump;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The reflective half of {@code /dumpcropharvest} (kubejs/data/creativedevtools/kubejs/dump/dump_crop_harvest.js,
 * farming/crops #3): Quark Simple Harvest's static lists, the block tags the audit reads, and whether a crop class
 * declares its own soil check. Kept in Java because KubeJS's class filter hides java.lang.reflect and Quark's private
 * {@code cropBlocks}. Quark is read reflectively, so the jar has no Quark dependency.
 *
 * Quark fills its lists in configChanged(): take the snapshot after the world loads (a command does).
 */
public final class CropHarvestProbe {
	private static final String QUARK_MODULE = "vazkii.quark.content.tweaks.module.SimpleHarvestModule";
	/** Block tag namespaces and paths the audit reports (prefix match on "namespace:path"). */
	private static final String[] TAG_PREFIXES = {
			"quark:simple_harvest_blacklisted", "creativedelight:crops/", "creativecalendar:crop_seasons/",
	};
	/** SRG and Mojmap names of Block.canSurvive and BushBlock.mayPlaceOn. */
	private static final Set<String> SOIL_METHODS = Set.of("m_7898_", "canSurvive", "m_6266_", "mayPlaceOn");

	private final Map<Block, List<String>> ripe = new LinkedHashMap<>();
	private final Set<Block> replantBlocks = new HashSet<>();
	private final Set<Block> click = new HashSet<>();
	private boolean quarkLoaded;
	private String quarkError = "";

	private CropHarvestProbe() {}

	public static CropHarvestProbe snapshot() {
		CropHarvestProbe p = new CropHarvestProbe();
		try {
			Class<?> c = Class.forName(QUARK_MODULE);
			Map<?, ?> crops = (Map<?, ?>) staticField(c, "crops");
			for (Map.Entry<?, ?> e : crops.entrySet()) {
				if (e.getKey() instanceof BlockState ripeState) {
					String to = e.getValue() instanceof BlockState s ? s.toString() : String.valueOf(e.getValue());
					p.ripe.computeIfAbsent(ripeState.getBlock(), b -> new ArrayList<>()).add(ripeState + " -> " + to);
				}
			}
			for (Object o : (Set<?>) staticField(c, "cropBlocks")) if (o instanceof Block b) p.replantBlocks.add(b);
			for (Object o : (Set<?>) staticField(c, "rightClickCrops")) if (o instanceof Block b) p.click.add(b);
			p.quarkLoaded = true;
		} catch (ClassNotFoundException e) {
			p.quarkError = "Quark not installed";
		} catch (Throwable t) {
			p.quarkError = t.toString();
		}
		return p;
	}

	private static Object staticField(Class<?> c, String name) throws ReflectiveOperationException {
		Field f = c.getDeclaredField(name);
		f.setAccessible(true);
		return f.get(null);
	}

	public boolean quarkLoaded() {
		return quarkLoaded;
	}

	public String quarkError() {
		return quarkError;
	}

	/** "ripe state -> replant state" for each Quark replant entry whose ripe state is this block. */
	public List<String> quarkRipeStates(Block b) {
		return ripe.getOrDefault(b, List.of());
	}

	/** In Quark's private cropBlocks: the block of some entry's replant state (not always the crop itself). */
	public boolean quarkReplantBlock(Block b) {
		return replantBlocks.contains(b);
	}

	/** In Quark's rightClickCrops: Quark (and its hoe area) right-clicks the block instead of replanting it. */
	public boolean quarkClick(Block b) {
		return click.contains(b);
	}

	/**
	 * Ids of blocks Quark lists (replant ripe states, cropBlocks or rightClickCrops) that are not in {@code seenIds}
	 * (newline-separated: a plain string crosses from Rhino without any list conversion).
	 */
	public List<String> quarkOnly(String seenIds) {
		Set<String> seen = new HashSet<>(java.util.Arrays.asList(seenIds.split("\n")));
		Set<Block> all = new HashSet<>(ripe.keySet());
		all.addAll(replantBlocks);
		all.addAll(click);
		Set<String> out = new TreeSet<>();
		for (Block b : all) {
			var key = ForgeRegistries.BLOCKS.getKey(b);
			if (key != null && !seen.contains(key.toString())) out.add(key.toString());
		}
		return new ArrayList<>(out);
	}

	public static Block block(String id) {
		return ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(id));
	}

	/** The block's tags under {@link #TAG_PREFIXES}, sorted. */
	@SuppressWarnings("deprecation")
	public static List<String> tags(Block b) {
		Set<String> out = new TreeSet<>();
		b.builtInRegistryHolder().tags().forEach(t -> {
			String id = t.location().toString();
			for (String p : TAG_PREFIXES) {
				if (id.startsWith(p)) {
					out.add(id);
					break;
				}
			}
		});
		return new ArrayList<>(out);
	}

	/**
	 * "Class#method" for each non-vanilla class in the block's chain that declares canSurvive or mayPlaceOn. Those
	 * crops may skip Forge's canSustainPlant, so pack soils (Rich Farmland, Fertile Soil) can refuse them.
	 */
	public static List<String> ownSoilCheck(Block b) {
		List<String> out = new ArrayList<>();
		for (Class<?> c = b.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
			if (c.getName().startsWith("net.minecraft.")) break;
			Set<String> names = new TreeSet<>();
			for (Method m : c.getDeclaredMethods()) {
				if (SOIL_METHODS.contains(m.getName())) names.add(m.getName());
			}
			for (String n : names) out.add(c.getName() + "#" + n);
		}
		return out;
	}
}
