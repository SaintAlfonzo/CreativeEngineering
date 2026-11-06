package com.SaintAlfonzo.creativedelight.compat.art;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client only. The compartment textures that were copies or recolours of other mods' art, built in memory from the
 * installed jars when the resource pack loads (like Creative Brewing's Vinery bottle looks), so CD's jar ships none of it
 * (licence audit 2026-11-04, TODO licence/cd #1 and mobs/nests #2). Served as the hidden resource pack
 * {@code creativedelight_compartment_art} at the bottom of the stack (CompartmentPacks); resource packs and kubejs/assets
 * still override any of the paths. Recipes in {@link ArtRecipes}; sources are read from each mod's own jar (vanilla from
 * the vanilla pack), never from the resource pack stack, so the result is the same with any resource pack on.
 */
public final class CompartmentArt {

	private static Map<String, byte[]> files;

	private CompartmentArt() {}

	/** @param cfb Cooking for Blockheads compartment on; @param incubation Incubation compartment on */
	public static Map<String, byte[]> files(boolean cfb, boolean incubation) {
		synchronized (CompartmentArt.class) {
			if (files == null) files = build(cfb, incubation);
			return files;
		}
	}

	private static Map<String, byte[]> build(boolean cfb, boolean incubation) {
		Map<String, byte[]> out = new HashMap<>();
		List<String> missing = new ArrayList<>();
		if (cfb) racks(out, missing);
		if (incubation) incubation(out, missing);
		CreativeDelight.LOGGER.info("[compartment art] {} textures built from installed mods{}", out.size(),
				missing.isEmpty() ? "" : "; sources not found (fallback or left out): " + missing);
		return out;
	}

	private static void racks(Map<String, byte[]> out, List<String> missing) {
		BufferedImage spice = image(ArtRecipes.CFB_SPICE_RACK, missing);
		BufferedImage tool = image(ArtRecipes.CFB_TOOL_RACK, missing);
		boolean[][] keep = null;
		if (tool != null) {
			keep = new boolean[tool.getHeight()][tool.getWidth()];
			for (int[] h : ArtRecipes.TOOL_RACK_HOOKS) keep[h[1]][h[0]] = true;
		}
		for (Map.Entry<String, String> e : ArtRecipes.PLANKS.entrySet()) {
			String wood = e.getKey();
			byte[] planksPng = bytes(e.getValue());
			if (planksPng == null) {
				missing.add(e.getValue());
				planksPng = bytes(ArtRecipes.FALLBACK_PLANKS);
				if (planksPng == null) continue;
			}
			try {
				BufferedImage planks = Recolour.read(planksPng);
				List<Integer> palette = Recolour.palette(planks);
				out.put(ArtRecipes.file(ArtRecipes.fruitBasket(wood)), planksPng);
				if (spice != null)
					out.put(ArtRecipes.file(ArtRecipes.rack(wood, "spice_rack")), Recolour.png(Recolour.paletteSwap(spice, palette, null)));
				if (tool != null)
					out.put(ArtRecipes.file(ArtRecipes.rack(wood, "tool_rack")), Recolour.png(Recolour.paletteSwap(tool, palette, keep)));
			} catch (Exception ex) {
				CreativeDelight.LOGGER.warn("[compartment art] {} racks: {}", wood, ex.toString());
			}
		}
	}

	private static void incubation(Map<String, byte[]> out, List<String> missing) {
		for (ArtRecipes.Derived d : ArtRecipes.INCUBATION) {
			if (!ModList.get().isLoaded(d.mod())) continue;
			byte[] src = bytes(d.source());
			if (src == null) {
				missing.add(d.source());
				continue;
			}
			try {
				out.put(ArtRecipes.file(d.out()), d.colours().isEmpty() ? src : Recolour.png(Recolour.recolour(Recolour.read(src), d.colours())));
			} catch (Exception ex) {
				CreativeDelight.LOGGER.warn("[compartment art] {}: {}", d.out(), ex.toString());
			}
		}
	}

	private static BufferedImage image(String id, List<String> missing) {
		byte[] b = bytes(id);
		if (b == null) {
			missing.add(id);
			return null;
		}
		try {
			return Recolour.read(b);
		} catch (Exception ex) {
			missing.add(id);
			return null;
		}
	}

	/** The texture's bytes from its own mod's jar (vanilla: the vanilla pack), or null. */
	static byte[] bytes(String id) {
		String file = ArtRecipes.file(id);
		String ns = id.substring(0, id.indexOf(':'));
		try {
			if (ns.equals("minecraft")) {
				ResourceLocation rl = new ResourceLocation(ns, file.substring(("assets/" + ns + "/").length()));
				try (InputStream in = Minecraft.getInstance().getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, rl)) {
					return in.readAllBytes();
				}
			}
			IModFileInfo mod = ModList.get().getModFileById(ns);
			if (mod == null) return null;
			Path p = mod.getFile().findResource(file.split("/"));
			return Files.isRegularFile(p) ? Files.readAllBytes(p) : null;
		} catch (Exception ex) {
			return null;
		}
	}
}
