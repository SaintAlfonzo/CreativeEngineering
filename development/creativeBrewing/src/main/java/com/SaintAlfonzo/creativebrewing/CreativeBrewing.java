package com.SaintAlfonzo.creativebrewing;

import com.SaintAlfonzo.creativebrewing.fluidbrewing.DragonBreathBrewing;
import com.SaintAlfonzo.creativebrewing.fluidbrewing.Lingerification;
import com.SaintAlfonzo.creativebrewing.potion.CEPotions;
import com.SaintAlfonzo.creativebrewing.potion.CopperPoisoningHandler;
import com.SaintAlfonzo.creativebrewing.potion.MagicResistanceEffect;
import com.SaintAlfonzo.creativebrewing.potion.PhantomBaneRules;
import com.SaintAlfonzo.creativebrewing.potion.PotionBucketDispenser;
import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creative Brewing (feature_writeup/creative_brewing.md): the brewing, alcohol and potion add-ons the pack owns, in one
 * jar, built like Creative Delight. Each member is its own mod, nested under META-INF/jarjar/ with its own mod id and
 * namespace (Vinery with its second id {@code foundation}, Brewin' and Chewin'). What CB adds is KubeJS scripts
 * (startup scripts in {@code kubejs_startup/}, server and client scripts under {@code data/} and
 * {@code assets/creativebrewing/kubejs/}) that call the pack's helpers, {@link CBConfig}, and the potion, effect,
 * potion machine and alcohol Java moved here from patcherMod on 2026-11-01 (creative_brewing.md 4.2 phases 2 and 3).
 *
 * Ids: everything CB registers or names (the potions, the Magic Resistance effect, the potion buckets, JEI recipe
 * types, lang keys, the lingerification list) is under {@code creativebrewing} ({@link #NS}, the mod id) since
 * 2026-11-04 (plan_cb_publish.md M4; they were {@code creativeengineering:} and {@code kubejs:}). The old ids are in
 * {@code data/creativebrewing/swaps/creative_split.json}, so saved effects, buckets and the registry table carry over.
 */
@Mod(CreativeBrewing.MOD_ID)
public class CreativeBrewing {

	public static final String MOD_ID = "creativebrewing";
	/** The namespace of every id CB registers and reads (plan_cb_publish.md M4; was creativeengineering). */
	public static final String NS = MOD_ID;
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final String OLD_FILE = "creativeengineering-common.toml";
	private static final String FILE = MOD_ID + "-common.toml";

	public CreativeBrewing() {
		// Mod id was creative_brewing up to 2026-11-01 (creative_split.md 7.6): keep the player's values.
		ConfigRename.carryOver("creative_brewing-common.toml", FILE);
		// [alcohol], [effects], [magic_resistance], [copper_poisoning] came from patcherMod's file (phase 3): the first
		// boot copies the player's values in (this file already exists, so merge, not carrySections); patcherMod then
		// drops them from its own file when Forge corrects it. Must run before Forge reads the COMMON configs.
		boolean carried = ConfigRename.carrySections(OLD_FILE, FILE, CBConfig.MOVED_SECTIONS)
				|| !ConfigRename.mergeSections(OLD_FILE, FILE, CBConfig.MOVED_SECTIONS).isEmpty();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CBConfig.SPEC, FILE);
		if (!carried) warnOldKeys();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		// Better Beacons fallback potions and the magic_resistance potion (creativebrewing:*).
		CEPotions.register(modEventBus);
		modEventBus.addListener(CreativeBrewing::commonSetup);
		// Damage-reduction half of creativebrewing:magic_resistance (the MobEffect itself is registered in KubeJS,
		// startup magic/magic_resistance.js).
		MinecraftForge.EVENT_BUS.register(MagicResistanceEffect.class);
		// Oxidized Everything Copper gear rolls for Poison ([copper_poisoning]; the effect-tick half is
		// mixin/everythingcopper/CopperPoisoningEffectMixin).
		MinecraftForge.EVENT_BUS.register(CopperPoisoningHandler.class);
		// Caffeinated carries Phantom Bane; Phantom Bane ending resets Time Since Rest (potions/effects #10).
		MinecraftForge.EVENT_BUS.register(PhantomBaneRules.class);
		// Dispensed potion buckets credit the dispenser's placer (PotionBucketDispenser, potions/potion-buckets #3)
		MinecraftForge.EVENT_BUS.register(PotionBucketDispenser.class);

		List<String> members = CBConfig.MEMBERS;
		List<String> missing = members.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
		LOGGER.info("[creativebrewing] members: {}{}", members, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
		members.forEach(CreativeBrewing::warnIfNotNested);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		// Create and IE are not CB dependencies; these classes import them (creative_split.md 7.16)
		ModList mods = ModList.get();
		if (mods.isLoaded("create")) event.enqueueWork(DragonBreathBrewing::build);
		if (mods.isLoaded("create") && mods.isLoaded("immersiveengineering")) event.enqueueWork(Lingerification::registerListGenerator);
		event.enqueueWork(PotionBucketDispenser::register);
	}

	/** Lines of creativeengineering-common.toml that held the sections moved here. */
	private static final Pattern OLD_KEY = Pattern.compile(
			"^\\s*(\\[(?:alcohol|effects|magic_resistance|copper_poisoning)\\])", Pattern.MULTILINE);

	/**
	 * When neither carry-over ran (this file already had the sections, or patcherMod's file had none), Forge drops the
	 * old sections from patcherMod's file on its next correction, so an edited value left there is lost: say which
	 * ones were still there. Runs at construction, before Forge loads and corrects the COMMON configs.
	 */
	private static void warnOldKeys() {
		Path old = FMLPaths.CONFIGDIR.get().resolve(OLD_FILE);
		if (!Files.isRegularFile(old)) return;
		String text;
		try {
			text = Files.readString(old, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return;
		}
		List<String> found = new ArrayList<>();
		Matcher m = OLD_KEY.matcher(text);
		while (m.find()) found.add(m.group(1));
		if (found.isEmpty()) return;
		LOGGER.warn("[creativebrewing] config/{} still has sections that moved to config/{}: {}. "
				+ "Creative Brewing reads only its own file; copy any edited value over (patcherMod drops these from its file).",
				OLD_FILE, FILE, found);
	}

	/**
	 * A jar in mods/ always beats a nested one with the same mod id, whatever the versions (creative_delight.md 14.1
	 * point 4). A nested member is loaded from the root of its own jij: file system; a top-level jar from the default
	 * one, so a member whose mod file lives there is a stale standalone jar (backportedVinery-*, backportedBrewin-*)
	 * left beside CB.
	 */
	private static void warnIfNotNested(String id) {
		IModFileInfo info = ModList.get().getModFileById(id);
		if (info == null) return;
		Path path = info.getFile().getFilePath();
		if (path.getFileSystem() != FileSystems.getDefault()) return;
		LOGGER.error("[creativebrewing] member {} is loaded from {} (version {}), not from the Creative Brewing jar: "
				+ "a jar in mods/ overrides the nested copy. Remove that jar.", id, path.getFileName(),
				info.versionString());
	}
}
