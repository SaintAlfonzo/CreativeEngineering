package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.shared.BuiltInPacks;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The compartments' assets and data (plan_cb_publish.md 3b step 3): folders under {@code packs/} in CD's jar, each added as
 * a required, hidden built-in pack (resource pack and data pack) only while its compartment is on and every mod in its
 * row is loaded, so a missing mod gives no recipe, tag, loot or model errors. The packs hold creativedelight files plus
 * tag additions; at the bottom they stay below kubejs/assets, which can still override them. Textures made from other mods'
 * art are not in the folders: CompartmentArt builds them on the client into one more hidden pack.
 */
public final class CompartmentPacks {

	/** One pack: folder under packs/, its compartment, and the other mods it needs (all of them). */
	private record Row(String folder, String compartment, List<String> mods, String title) {
		boolean on() {
			return Compartments.on(compartment) && mods.stream().allMatch(Compartments::loaded);
		}
	}

	private static final List<Row> ROWS = List.of(
			new Row("cookingforblockheads", Compartments.CFB, List.of(), "Creative Delight: Cooking for Blockheads"),
			new Row("incubation/alexsmobs", Compartments.INCUBATION, List.of("alexsmobs"), "Creative Delight: Incubation, Alex's Mobs"),
			new Row("incubation/untitledduckmod", Compartments.INCUBATION, List.of("untitledduckmod"),
					"Creative Delight: Incubation, Untitled Duck Mod"));

	private CompartmentPacks() {}

	public static void add(AddPackFindersEvent event) {
		addWorldgen(event);
		List<Row> on = ROWS.stream().filter(Row::on).toList();
		if (on.isEmpty()) return;
		// Textures built from the installed mods' own art (compat/art/CompartmentArt): client resource pack only.
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
			boolean cfb = on.stream().anyMatch(r -> r.compartment().equals(Compartments.CFB));
			boolean incubation = on.stream().anyMatch(r -> r.compartment().equals(Compartments.INCUBATION));
			BuiltInPacks.addMemory(event, PackType.CLIENT_RESOURCES, CreativeDelight.MOD_ID + "_compartment_art",
					"Creative Delight: textures built from installed mods", Pack.Position.BOTTOM,
					() -> com.SaintAlfonzo.creativedelight.compat.art.CompartmentArt.files(cfb, incubation));
		}
		Path root = ModList.get().getModFileById(CreativeDelight.MOD_ID).getFile().findResource("packs");
		for (Row row : on) {
			BuiltInPacks.addFolder(event, root.resolve(row.folder()), CreativeDelight.MOD_ID + ":" + row.folder(), row.title(),
					Pack.Position.BOTTOM, "Creative Delight");
		}
	}

	/**
	 * The wild crop worldgen that names another mod (framework #13): {@code packs/worldgen/<modid>/}, split out of
	 * data/creativedelight/worldgen/ by the build (splitWorldgen, from wild_crops.json "requires"). Forge 1.19.2 has no
	 * conditions for worldgen registries, so each folder is a data pack added only while its mod is loaded; the biome
	 * rules (wild_crops.js) skip the same rules by the same key.
	 */
	private static void addWorldgen(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.SERVER_DATA) return;
		Path root = ModList.get().getModFileById(CreativeDelight.MOD_ID).getFile().findResource("packs", "worldgen");
		if (!Files.isDirectory(root)) return;
		List<String> on = new ArrayList<>();
		List<String> off = new ArrayList<>();
		try (Stream<Path> dirs = Files.list(root)) {
			for (Path dir : dirs.filter(Files::isDirectory).sorted().toList()) {
				String mod = dir.getFileName().toString().replace("/", "");
				if (!Compartments.loaded(mod)) {
					off.add(mod);
					continue;
				}
				on.add(mod);
				BuiltInPacks.addFolder(event, dir, CreativeDelight.MOD_ID + ":worldgen/" + mod, "Creative Delight: wild crops, " + mod,
						Pack.Position.BOTTOM, "Creative Delight");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Creative Delight: cannot list packs/worldgen", e);
		}
		CreativeDelight.LOGGER.info("[creativedelight] wild crop worldgen packs on: {}; off (mod not loaded): {}", on, off.isEmpty() ? "none" : off);
	}
}
