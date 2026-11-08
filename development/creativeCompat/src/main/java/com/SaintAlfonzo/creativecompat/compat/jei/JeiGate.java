package com.SaintAlfonzo.creativecompat.compat.jei;

import com.SaintAlfonzo.creativecompat.shared.GatedMixinPlugin;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * Switch for every piece of Creative Compat that is written against JEI's internals (the JEI plugins, the pack
 * ingredient order, the mixins): on only when JEI is loaded and inside the version range the pieces were tested on.
 * The mixins are held to the same range by CompatMixinPlugin (its table uses {@link #RANGE}); the plugins and the
 * script entry point ask here at run time, so a JEI outside the range gets Creative Compat's pages skipped with one
 * warning instead of errors from half-matching internals. Holds no JEI types, so it is safe to call without JEI.
 */
public final class JeiGate {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Tested JEI versions (Maven range): the pack's 11.8.1.1034 up to the next minor release. */
    public static final String RANGE = "[11.8.1.1034,11.9)";

    private static Boolean supported;

    private JeiGate() {}

    public static synchronized boolean supported() {
        if (supported == null) {
            supported = check();
        }
        return supported;
    }

    private static boolean check() {
        ModList mods = ModList.get();
        if (mods == null) {
            return false;
        }
        return mods.getModContainerById("jei").map(c -> {
            var version = c.getModInfo().getVersion();
            if (GatedMixinPlugin.inRange(RANGE, version)) {
                return true;
            }
            LOGGER.warn("[creativecompat] jei {} is outside the tested range {}: Creative Compat's JEI pages, order and tooltips are off",
                    version, RANGE);
            return false;
        }).orElse(false);
    }

    /**
     * Runs one JEI feature when the gate is on. A feature that fails, including a JEI method or class it expects not
     * being there (a LinkageError), is logged and skipped, so the other features and JEI itself keep loading.
     */
    public static void run(String feature, Runnable body) {
        if (!supported()) {
            return;
        }
        try {
            body.run();
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn("[creativecompat] JEI feature '{}' failed and is skipped", feature, e);
        }
    }
}
