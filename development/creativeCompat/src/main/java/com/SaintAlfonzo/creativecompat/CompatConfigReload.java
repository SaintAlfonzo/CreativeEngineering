package com.SaintAlfonzo.creativecompat;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Re-reads {@code config/creativecompat-common.toml} on demand and runs the same reload path a file-watcher reload runs
 * (plan_post_scratch_1115b.md E2): the file is loaded again, corrected like Forge corrects it, the spec's value caches
 * are reset, and {@link ModConfigEvent.Reloading} is posted on this mod's bus, so every reader ({@code PassValues},
 * {@code PassSync}'s re-send, {@code SurfaceSpeeds}) acts as on a watched save.
 *
 * <p>Why (E1, 2026-11-15): Forge's watcher is night-config 3.6.7's {@code FileWatcher}, which only acts on an
 * {@code ENTRY_MODIFY} event whose count is 1. An editor or script that saves by writing a new file and renaming it
 * over the old one ({@code sed -i}, atomic-save editors) raises create / delete events instead, and two writes folded
 * into one event (count 2) are skipped too, so such a save is never applied. ModernFix's
 * {@code NightConfigWatchThrottler} only adds a 1 s pause per watcher pass. Our readers read live values (no static
 * copies); the missed reload is the whole fault. Called by Creative Devtools' {@code /cdevreload compat}.
 */
public final class CompatConfigReload {

    private CompatConfigReload() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile ModConfig config;
    private static volatile IEventBus bus;

    /** Mod construction: the bus the reload event goes to. */
    static void init(IEventBus modBus) {
        bus = modBus;
        modBus.addListener(CompatConfigReload::onLoad);
    }

    private static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == CompatConfig.SPEC) config = event.getConfig();
    }

    /**
     * Reload the file now. Returns a one-line result for the command's feedback. Thread: any (the readers hand their
     * own work to the server thread); the command calls it on the server thread, or on the client thread for a
     * client-side reload.
     */
    public static synchronized String reload() {
        ModConfig c = config;
        IEventBus b = bus;
        if (c == null || b == null) return "creativecompat-common.toml is not loaded yet";
        if (!(c.getConfigData() instanceof CommentedFileConfig file)) return "creativecompat-common.toml has no file";
        try {
            file.load();
            int fixed = 0;
            if (!CompatConfig.SPEC.isCorrect(file)) {
                fixed = CompatConfig.SPEC.correct(file);
                file.save();
            }
            CompatConfig.SPEC.afterReload();
            b.post(new ModConfigEvent.Reloading(c));
            String msg = "creativecompat-common.toml reloaded" + (fixed > 0 ? " (" + fixed + " value(s) corrected)" : "");
            LOGGER.info("[creativecompat] {}", msg);
            return msg;
        } catch (Exception e) {
            LOGGER.error("[creativecompat] reloading creativecompat-common.toml failed", e);
            return "reload failed: " + e;
        }
    }
}
