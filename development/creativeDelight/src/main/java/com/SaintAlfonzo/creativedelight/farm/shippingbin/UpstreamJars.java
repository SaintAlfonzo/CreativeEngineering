package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.shared.DonorJars.Jar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The unmodified Farming for Blockheads jar the Shipping Bin port reads its art from (feature_writeup/farming_for_blockheads.md
 * 4 step 0). It sits in {@code <game dir>/blockheads_upstream/}, never in {@code mods/} (a NeoForge 26.3 jar). Pinned by
 * sha1. Only the bin's art is exposed ({@link #assets()}); the donor's data (market recipes, advancements, loot) is never
 * loaded, our jar ships its own.
 */
public final class UpstreamJars {
	public static final String FOLDER = "blockheads_upstream";

	public static final Jar FFB_26302 = new Jar("farmingforblockheads-neoforge-26.3-26.3.0.2.jar", "cb9f011d91847eb328752e36655ebfaf9af2fdc4",
			"CurseForge or Modrinth, Farming for Blockheads 26.3.0.2 for NeoForge 26.3 (file farmingforblockheads-neoforge-26.3-26.3.0.2.jar)");

	/** Donor paths the resource pack exposes (prefix match). Everything else in the jar stays unread. */
	private static final List<String> EXPOSED = List.of(
			"assets/farmingforblockheads/blockstates/shipping_bin",
			"assets/farmingforblockheads/models/block/shipping_bin",
			"assets/farmingforblockheads/textures/block/shipping_bin",
			"assets/farmingforblockheads/textures/entity/shipping_balloon.png",
			"assets/farmingforblockheads/textures/entity/merchant.png",
			"assets/farmingforblockheads/textures/gui/shipping_bin.png",
			"assets/farmingforblockheads/textures/gui/sprites/container/shipping_bin/");

	private static Map<String, byte[]> assets;

	private UpstreamJars() {
	}

	/** Stops the game with a clear message when the jar is missing or not the expected file. */
	public static void verifyOrFail() {
		DonorJars.verifyOrFail(FOLDER, "The Shipping Bin (Creative Delight)", "the original Farming for Blockheads jar",
				CreativeDelight.LOGGER::error, FFB_26302);
	}

	/** The exposed art, unchanged, under its upstream paths. */
	public static synchronized Map<String, byte[]> assets() {
		if (assets == null) {
			Map<String, byte[]> out = new HashMap<>();
			DonorJars.read(DonorJars.path(FOLDER, FFB_26302), "assets/farmingforblockheads/").forEach((path, bytes) -> {
				if (EXPOSED.stream().anyMatch(path::startsWith)) out.put(path, bytes);
			});
			assets = out;
		}
		return assets;
	}
}
