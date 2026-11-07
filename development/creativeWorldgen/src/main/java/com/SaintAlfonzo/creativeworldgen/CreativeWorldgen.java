package com.SaintAlfonzo.creativeworldgen;

import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.SaintAlfonzo.creativeworldgen.block.FastLeafDecayPort;
import com.SaintAlfonzo.creativeworldgen.forgivingvoid.ForgivingVoidBridge;
import com.SaintAlfonzo.creativeworldgen.mobs.NamedMobs;
import com.SaintAlfonzo.creativeworldgen.mooshroom.MooshroomFamily;
import com.SaintAlfonzo.creativeworldgen.worldgen.BeeNests;
import com.SaintAlfonzo.creativeworldgen.worldgen.DimensionFilter;
import com.SaintAlfonzo.creativeworldgen.worldgen.EarthFill;
import com.SaintAlfonzo.creativeworldgen.worldgen.GroundMatch;
import com.SaintAlfonzo.creativeworldgen.worldgen.LocateCommand;
import com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots;
import com.SaintAlfonzo.creativeworldgen.worldgen.MetaStructure;
import com.SaintAlfonzo.creativeworldgen.worldgen.PlacementPriority;
import com.SaintAlfonzo.creativeworldgen.worldgen.PoolTuner;
import com.SaintAlfonzo.creativeworldgen.worldgen.SeabedClearance;
import com.SaintAlfonzo.creativeworldgen.worldgen.SkyIslands;
import com.SaintAlfonzo.creativeworldgen.worldgen.SpawnTuning;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeFits;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeRules;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureFrequencies;
import com.SaintAlfonzo.creativeworldgen.worldgen.StructureSpawnBoxes;
import com.SaintAlfonzo.creativeworldgen.worldgen.TemplateBlockSwaps;
import com.SaintAlfonzo.creativeworldgen.worldgen.TemplateTrim;
import com.SaintAlfonzo.creativeworldgen.worldgen.TerrainAdaptation;
import com.SaintAlfonzo.creativeworldgen.worldgen.donor.FarmersDonor;
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
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creative Worldgen (feature_writeup/creative_worldgen.md): the pack's world generation and fauna, carved out of
 * patcherMod on 2026-11-02 (creative_split.md 7.6, wave 5). Structure placement, frequencies, biome rules and fits,
 * locate, earth and water fill, terrain adaptation, meta structures, sky islands, bee nests, mob spawns and caps, named
 * mobs, mooshrooms, leaf decay, raids glowing, Forgiving Void, the Farmer's Structures donor. Nests More Mooblooms and
 * Baby Mobs. Requires patcherMod ({@code creativeengineering}, later Creative Common) and Creative Compat.
 *
 * Ids stay: the biome modifier codecs, the meta structure and piece types, the loot condition, tags and lang keys keep
 * the {@code creativeengineering} namespace ({@link #NS}). This mod id is only the jar's identity, its config file and
 * its script packs. The registrations below are the ones patcherMod's constructor made, in the same order.
 */
@Mod(CreativeWorldgen.MOD_ID)
public class CreativeWorldgen {

    public static final String MOD_ID = "creativeworldgen";
    /** The namespace of every id this mod uses: patcherMod's, unchanged by the carve. */
    public static final String NS = "creativeengineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String OLD_FILE = "creativeengineering-common.toml";
    private static final String FILE = MOD_ID + "-common.toml";

    public CreativeWorldgen() {
        // The sections came from patcherMod's file: the first boot copies the player's values over (then patcherMod
        // drops them from its file when Forge corrects it, and this file drops the [perf] keys that stayed there).
        // Must run before Forge reads the COMMON configs.
        boolean carried = ConfigRename.carrySections(OLD_FILE, FILE, CWGConfig.SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CWGConfig.SPEC, FILE);
        if (!carried) warnOldKeys();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(CreativeWorldgen::commonSetup);
        // Farmer's Structures 1.0.6 data, translated in memory from the donor jar in farmers_upstream/ (worldgen/donor).
        modEventBus.addListener(FarmersDonor::onAddPackFinders);
        // packs/<modid>/: worldgen data naming a mod this jar does not require, added only while that mod is loaded.
        modEventBus.addListener(com.SaintAlfonzo.creativeworldgen.compat.ModDataPacks::add);

        // The creativeengineering:biome_tag loot condition moved to Creative Delight (loot/BiomeTagCondition) 2026-11-06.
        // Meta structures: the creativeengineering:meta structure type and its piece types (worldgen/MetaStructure; the
        // structures themselves are kubejs meta_structures.json).
        MetaStructure.register(modEventBus);

        // Leaf decay ([leaf_decay]): Fast Leaf Decay's air-neighbour trigger, ported in; the range and timing rules are
        // mixin.LeavesDecayRangeMixin + block.LeafDecayRange.
        MinecraftForge.EVENT_BUS.register(FastLeafDecayPort.class);

        // Jigsaw pool tuning (village houses etc.: pool_config.json / pool_tags.json) + the loot container swaps that let
        // Lootr convert modded structure chests. ServerAboutToStartEvent, LOWEST priority, so every mod has injected its
        // village pieces first.
        MinecraftForge.EVENT_BUS.register(PoolTuner.class);
        // Structure groups: per-structure frequency inside a structure_set, staged by structure_overrides.js and frozen
        // at ServerAboutToStartEvent.
        MinecraftForge.EVENT_BUS.register(StructureFrequencies.class);
        // Meta structures with an anchor take over slots their anchors won (worldgen/MetaSlots); its index is per world.
        MinecraftForge.EVENT_BUS.register(MetaSlots.class);
        MinecraftForge.EVENT_BUS.register(PlacementPriority.class);
        MinecraftForge.EVENT_BUS.register(TemplateBlockSwaps.class);
        // Spawn boxes on pillager and witch structures and their meta parts (spawn_boxes.json); box evokers drop no totem.
        MinecraftForge.EVENT_BUS.register(StructureSpawnBoxes.class);
        // Mob spawn tuning: biome modifier type creativeengineering:tune_spawns (mob_spawns.json); its change record
        // feeds /dumpspawns and is cleared on stop.
        SpawnTuning.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(SpawnTuning.class);
        // Brown mooshrooms on natural spawns (mob_spawns.json brown_chance); the family's breeding is in mixins.
        MinecraftForge.EVENT_BUS.register(MooshroomFamily.class);
        // Named mobs ([named_mobs]): a rare random name on natural spawns, deaths announced in chat.
        MinecraftForge.EVENT_BUS.register(NamedMobs.class);
        // Bee nest tuning: biome modifier type creativeengineering:bee_nests (bee_nests.json) + the tree/nest/chunk
        // counters behind /dumpbeenests.
        BeeNests.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(BeeNests.class);
        // Structure biome rules (biome_rules in structure_groups.json): only allowed structures start in the water biomes.
        MinecraftForge.EVENT_BUS.register(StructureBiomeRules.class);
        // Biome fits (biome_fits.json + the tower/* structure tags), frozen at ServerAboutToStartEvent (HIGHEST).
        MinecraftForge.EVENT_BUS.register(StructureBiomeFits.class);
        // Ground match ([worldgen.ground_match]): registered for its per-world sample cache reset.
        MinecraftForge.EVENT_BUS.register(GroundMatch.class);
        // Earth and water fill ([worldgen.earth_fill], [worldgen.water_fill]): registered to clear its template caches.
        MinecraftForge.EVENT_BUS.register(EarthFill.class);
        // Structures no biome of a dimension can hold are not tried there; registered to clear its cache.
        MinecraftForge.EVENT_BUS.register(DimensionFilter.class);
        // Seabed clearance and Sky Structures' islands: registered for their load / stop log lines.
        MinecraftForge.EVENT_BUS.register(SeabedClearance.class);
        MinecraftForge.EVENT_BUS.register(SkyIslands.class);
        // Terrain adaptation by structure tag and template trim ([worldgen.terrain_adaptation]): tag table and caches.
        MinecraftForge.EVENT_BUS.register(TerrainAdaptation.class);
        MinecraftForge.EVENT_BUS.register(TemplateTrim.class);
        // /cwglocate batch | census | stats ([worldgen.locate]).
        MinecraftForge.EVENT_BUS.register(LocateCommand.class);

        LOGGER.info("[creativeworldgen] loaded (config/{}){}", FILE,
                ModList.get().isLoaded("asynclocator") ? "" : ", Async Locator not loaded");
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // ForgivingVoidMixin applies lazily on the first void-fall bounce; pre-load the config bridge it references so a
        // late transform can resolve it.
        forceLoad("com.SaintAlfonzo.creativeworldgen.forgivingvoid.ForgivingVoidBridge");
        LOGGER.info("[forgiving-void] {}", ForgivingVoidBridge.summary());
    }

    /** Define a class now (with linking) so it exists before any late Mixin transform needs it. */
    private static void forceLoad(String className) {
        try {
            Class.forName(className, true, CreativeWorldgen.class.getClassLoader());
        } catch (Throwable t) {
            LOGGER.error("[creativeworldgen] failed to pre-load {}", className, t);
        }
    }

    /** Lines of creativeengineering-common.toml that held the sections and [perf] keys moved here. */
    private static final Pattern OLD_KEY = Pattern.compile(
            "^\\s*(\\[(?:forgiving_void|leaf_decay|raids|worldgen|named_mobs)(?:\\.[a-z_.]+)?\\]"
                    + "|(?:mobCap[A-Za-z0-9]*|spawnChunksLevel[12]|spawnBand[A-Za-z0-9]*)(?=\\s*=))", Pattern.MULTILINE);

    /**
     * The sections moved out of patcherMod's file. When this file already existed the carry-over did not run, and Forge
     * drops the old sections from patcherMod's file on its next correction, so an edited value left there is lost: say
     * which ones were still there. Runs at construction, before Forge loads and corrects the COMMON configs.
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
        LOGGER.warn("[creativeworldgen] config/{} still has sections and keys that moved to config/{}: {}. "
                + "Creative Worldgen reads only its own file; copy any edited value over (patcherMod drops these from its file).",
                OLD_FILE, FILE, found);
    }
}
