package com.SaintAlfonzo.creativedelight.compat;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The optional Java compartments folded into Creative Delight's own jar at M8 (plan_cb_publish.md 3b, D3): Cooking for
 * Blockheads ({@code compat.cookingforblockheads}) and Incubation ({@code compat.incubation}). A compartment is on when
 * every mod it needs is loaded and its {@code [compartments]} switch in creativedelight-common.toml is on. The switch is
 * read straight from the file (default on), because the mixin plugin and the mod constructor ask before Forge loads the
 * COMMON configs; so a change needs a restart. This class names no type of the optional mods (and no Minecraft type), so
 * the mixin plugin can call it.
 */
public final class Compartments {
	public static final String CFB = "cookingforblockheads";
	public static final String INCUBATION = "incubation";

	/** Compartment -> the mods it needs (all of them). */
	private static final Map<String, List<String>> NEEDS = Map.of(
			CFB, List.of("cookingforblockheads", "balm"),
			INCUBATION, List.of("incubation"));

	/** Compartment -> the [members] switch it replaced (carried once by {@link MemberConfigMove#carryCompartmentSwitches}). */
	public static final Map<String, String> OLD_MEMBER_SWITCH = Map.of(
			CFB, "blockheads_additions",
			INCUBATION, "incubation_additions");

	private static final Map<String, Boolean> ON = new ConcurrentHashMap<>();

	private Compartments() {}

	public static boolean isCompartment(String id) {
		return NEEDS.containsKey(id);
	}

	/** True if compartment {@code id} is active (its mods loaded, its switch on); false for an unknown id. */
	public static boolean on(String id) {
		if (!NEEDS.containsKey(id)) return false;
		return ON.computeIfAbsent(id, k -> NEEDS.get(k).stream().allMatch(Compartments::loaded) && switchOn(k));
	}

	/** True if {@code modId} is loaded (early-safe: the loading mod list). */
	public static boolean loaded(String modId) {
		LoadingModList list = LoadingModList.get();
		return list != null && list.getModFileById(modId) != null;
	}

	private static boolean switchOn(String id) {
		Path file = FMLPaths.CONFIGDIR.get().resolve("creativedelight-common.toml");
		if (!Files.isRegularFile(file)) return true;
		try (CommentedFileConfig cfg = CommentedFileConfig.builder(file, TomlFormat.instance()).build()) {
			cfg.load();
			Object v = cfg.get("compartments." + id);
			if (v == null) v = cfg.get("members." + OLD_MEMBER_SWITCH.get(id));
			return !(v instanceof Boolean b) || b;
		} catch (Exception e) {
			return true;
		}
	}
}
