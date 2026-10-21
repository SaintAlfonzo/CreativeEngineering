package com.SaintAlfonzo.creativeengineering.util;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renames mods as players see them ({@code [mod_names] overrides} in creativeengineering-common.toml): the
 * blue mod line under every JEI tooltip, JEI's {@code @name} search, WTHIT's mod line and Forge's Mods screen
 * all read {@link IModInfo#getDisplayName()}, which returns the {@code displayName} of the jar's mods.toml.
 *
 * <p>That value is a private final field of FML's {@code ModInfo}, which lives in the loader's service layer:
 * mixins only reach the game layer, and the module doesn't open the package to reflection, so the field is
 * written with {@code sun.misc.Unsafe}. Applied at common setup (before JEI or WTHIT build their caches) and
 * again on a config reload; a mod taken out of the list gets its own name back.
 */
public final class ModDisplayNames {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** mod id -> the mods.toml name, recorded before the first rename. */
    private static final Map<String, String> ORIGINAL = new HashMap<>();

    private ModDisplayNames() {}

    /** Saving the toml (by hand or in Create's Configs screen). */
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CEConfig.COMMON_SPEC) apply();
    }

    public static synchronized void apply() {
        Map<String, String> wanted = new HashMap<>();
        for (String line : CEConfig.MOD_NAMES.overrides.get()) {
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            wanted.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
        }
        for (String id : List.copyOf(ORIGINAL.keySet())) {
            if (!wanted.containsKey(id)) set(id, ORIGINAL.remove(id));
        }
        wanted.forEach((id, name) -> {
            if (name.isEmpty()) return;
            String before = set(id, name);
            if (before != null) ORIGINAL.putIfAbsent(id, before);
        });
    }

    /** Writes the display name; returns the previous one, or null when the mod isn't loaded or the write failed. */
    private static String set(String modId, String name) {
        IModInfo info = ModList.get().getModContainerById(modId).map(c -> c.getModInfo()).orElse(null);
        if (info == null) {
            LOGGER.warn("[mod names] {} is not loaded; '{}' not applied", modId, name);
            return null;
        }
        String before = info.getDisplayName();
        if (before.equals(name)) return before;
        try {
            Field field = info.getClass().getDeclaredField("displayName");
            sun.misc.Unsafe unsafe = unsafe();
            unsafe.putObject(info, unsafe.objectFieldOffset(field), name);
            LOGGER.info("[mod names] {}: '{}' -> '{}'", modId, before, name);
            return before;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[mod names] could not rename {} ({})", modId, info.getClass().getName(), e);
            return null;
        }
    }

    private static sun.misc.Unsafe unsafe() throws ReflectiveOperationException {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}
