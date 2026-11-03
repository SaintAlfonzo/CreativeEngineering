package com.SaintAlfonzo.creativedevtools;

import com.SaintAlfonzo.creativedevtools.ceplace.MassPlacer;
import com.SaintAlfonzo.creativedevtools.generators.GeneratorCommand;
import com.SaintAlfonzo.creativedevtools.ie.IeRecyclingDiagnostics;
import com.SaintAlfonzo.creativedevtools.probe.BlockStateSyncDebug;
import com.SaintAlfonzo.creativedevtools.probe.LevelRandomProbe;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

/**
 * Creative Devtools (feature_writeup/creative_devtools.md): the pack's developer tools, carved out of patcherMod in the
 * Creative Split. Dev {@code mods/} only, never in a player payload. Holds {@code /cegen}, {@code /ceperf},
 * {@code /ceplace}, the {@code /dump*} commands (KubeJS server scripts under {@code data/creativedevtools/kubejs/}), the
 * block-state and Level-random probes (subscribed to patcherMod's hooks in {@code util/}), the IE recycling log, and
 * nests CE Perf Bot and CE Client Capture. Registers nothing.
 */
@Mod(CreativeDevtools.MOD_ID)
public class CreativeDevtools {

    public static final String MOD_ID = "creativedevtools";
    public static final Logger LOGGER = LogUtils.getLogger();
    /** The nested members' mod ids (bundle_members). */
    public static final List<String> MEMBERS = List.of("ceperfbot", "cecapture");

    public CreativeDevtools() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DevtoolsConfig.SPEC, "creativedevtools-common.toml");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Python generators (tools/generators.json): /cegen registers itself on the FORGE bus; these two run them at
        // startup when [debug.generators] runAtStartup is on, finishing before any datapack loads.
        modBus.addListener(GeneratorCommand::onCommonSetup);
        modBus.addListener(GeneratorCommand::onLoadComplete);

        // /ceplace: every structure or template matching a filter, laid out in rows (flat-world testing);
        // knobs in [worldgen.mass_place]. Built on Creative Worldgen's placement code (an optional dependency).
        if (ModList.get().isLoaded("creativeworldgen")) MinecraftForge.EVENT_BUS.register(MassPlacer.class);

        // Probes on patcherMod's hooks (util/BlockStateLookupHook, util/LevelRandomHook); inert while their
        // [debug.*] switch is off.
        BlockStateSyncDebug.register();
        LevelRandomProbe.register();

        if (ModList.get().isLoaded("immersiveengineering")) MinecraftForge.EVENT_BUS.register(IeRecyclingDiagnostics.class);

        List<String> missing = MEMBERS.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
        LOGGER.info("[creativedevtools] loaded, members: {}{}", MEMBERS, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
    }
}
