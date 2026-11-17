package net.minecraftforge.fml;

/**
 * Headless stand-in for Forge's ModList, first on the classpath of BiomeLayoutCacheTest only (test_worldgen_tools.sh
 * section 11). Without FML the real ModList.get() is null; BiomeLayoutTuner asks it whether optional biome mods
 * (Biomes O' Plenty) are loaded. Every mod counts as loaded, as in the full pack.
 */
public class ModList {
	private static final ModList INSTANCE = new ModList();

	public static ModList get() { return INSTANCE; }

	public boolean isLoaded(String modId) { return true; }
}
