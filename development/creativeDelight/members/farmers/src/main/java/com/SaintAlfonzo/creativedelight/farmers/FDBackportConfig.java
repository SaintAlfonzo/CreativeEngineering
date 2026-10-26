package com.SaintAlfonzo.creativedelight.farmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.FarmersDelight;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * {@code config/farmersdelight-ce-common.toml}: the switches for the backported 1.3.4 features (farmers_delight.md 3).
 * Forge re-reads the file when it changes on disk, so both values below take effect without a restart.
 */
public final class FDBackportConfig {

	public static final String FILE = "farmersdelight-ce-common.toml";

	/** Which mod's pie block a vanilla Pumpkin Pie places as. */
	public enum PiePlacement {
		FARMERSDELIGHT("farmersdelight"), DELIGHTFUL("delightful"), CREATE_CENTRAL_KITCHEN("create_central_kitchen"), OFF(null);

		public final String namespace;

		PiePlacement(String namespace) {
			this.namespace = namespace;
		}
	}

	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.EnumValue<PiePlacement> PIE_PLACEMENT;
	public static final ForgeConfigSpec.BooleanValue PIE_SNEAK_TO_PLACE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VILLAGER_FARMLAND;

	private static volatile Set<Block> villagerFarmland = Collections.emptySet();
	private static volatile boolean villagerFarmlandDirty;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.push("pumpkin_pie");
		PIE_PLACEMENT = b.comment("Which placed pie a vanilla Pumpkin Pie becomes when used on a block. Three mods have one:",
						"FARMERSDELIGHT (Farmer's Delight 1.3.4, slices into Slice of Pumpkin Pie), DELIGHTFUL (Delightful's),",
						"CREATE_CENTRAL_KITCHEN (Central Kitchen's). OFF leaves it to whichever of those mods acts first.",
						"Read live: edit and save the file, then place a pie to compare the three in game. A choice whose mod is",
						"missing falls back to OFF.")
				.defineEnum("placeAs", PiePlacement.FARMERSDELIGHT);
		PIE_SNEAK_TO_PLACE = b.comment("Only place the pie while sneaking (Farmer's Delight 1.3.4 enablePumpkinPieSneakToPlace).")
				.define("sneakToPlace", false);
		b.pop();
		b.push("villagers");
		VILLAGER_FARMLAND = b.comment("Blocks a Farmer villager walks to and works besides vanilla Farmland (Farmer's Delight 1.3.4",
						"VillagersTargetRichSoilMixin, widened to a list). Defaults: Rich Soil Farmland and Fertile Soil",
						"(Immersive Weathering mulch). Only farmland-type blocks are kept (villagers plant only on those);",
						"others are ignored with a warning in the log. Empty list: vanilla behaviour.")
				.defineListAllowEmpty(List.of("farmlandBlocks"),
						() -> List.of("farmersdelight:rich_soil_farmland", "immersive_weathering:mulch_block"),
						o -> o instanceof String s && ResourceLocation.isValidResourceLocation(s));
		b.pop();
		SPEC = b.build();
	}

	private FDBackportConfig() {}

	/** The pie block id to place, or null for none (OFF, or before the config is loaded). */
	public static ResourceLocation pieBlock() {
		if (!SPEC.isLoaded()) return null;
		String ns = PIE_PLACEMENT.get().namespace;
		return ns == null ? null : new ResourceLocation(ns, "pumpkin_pie");
	}

	public static boolean pieSneakToPlace() {
		return SPEC.isLoaded() && PIE_SNEAK_TO_PLACE.get();
	}

	/**
	 * Extra farmland a Farmer villager targets; empty until the config has loaded. Resolved on the first call after
	 * a load or reload (server thread), so it never runs before the block registry is filled.
	 */
	public static Set<Block> villagerFarmland() {
		if (villagerFarmlandDirty) {
			villagerFarmlandDirty = false;
			villagerFarmland = resolve();
		}
		return villagerFarmland;
	}

	static void onConfigLoad(ModConfigEvent.Loading event) {
		if (event.getConfig().getSpec() == SPEC) villagerFarmlandDirty = true;
	}

	static void onConfigChange(ModConfigEvent.Reloading event) {
		if (event.getConfig().getSpec() == SPEC) villagerFarmlandDirty = true;
	}

	private static Set<Block> resolve() {
		Set<Block> blocks = new HashSet<>();
		for (String id : VILLAGER_FARMLAND.get()) {
			ResourceLocation key = new ResourceLocation(id.toLowerCase(Locale.ROOT));
			Block block = ForgeRegistries.BLOCKS.containsKey(key) ? ForgeRegistries.BLOCKS.getValue(key) : null;
			if (block == null) {
				FarmersDelight.LOGGER.warn("[{}] villagers.farmlandBlocks: no block {} (mod not loaded?), skipped", FILE, id);
			} else if (!(block instanceof FarmBlock)) {
				FarmersDelight.LOGGER.warn("[{}] villagers.farmlandBlocks: {} is not a farmland block, skipped", FILE, id);
			} else {
				blocks.add(block);
			}
		}
		return Set.copyOf(blocks);
	}
}
