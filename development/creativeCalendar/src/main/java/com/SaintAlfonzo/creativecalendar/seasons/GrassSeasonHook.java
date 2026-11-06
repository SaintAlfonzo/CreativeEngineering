package com.SaintAlfonzo.creativecalendar.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared body of the modded grass season mixins (IWGrassSpreadSeasonMixin, BotaniaGrassSeasonMixin,
 * VineryGrassSlabSeasonMixin, VintageLushGrassSeasonMixin; feature_writeup/crops.md 2.12.1). One mixin per target so a
 * missing mod cannot hide the others and each can be {@code require = 1}. The first call per target logs one line, so a
 * scratch run can tell "hook live, season neutral" from "hook never ran".
 * Serene Seasons is checked here, not in the mixin config gate (the gate checks only the grass mod). LoadingModList,
 * not ModList: IW builds its grass block in vanilla bootstrap, before ModList exists (boot crash 2026-11-02, r1102b).
 */
public final class GrassSeasonHook {

    private static final Logger LOG = LogUtils.getLogger();
    private static final boolean SEASONS = LoadingModList.get().getModFileById("sereneseasons") != null;
    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    private GrassSeasonHook() {}

    /** True = skip this spread try (the caller cancels). */
    public static boolean refuse(String target, BlockState state, ServerLevel level, BlockPos pos) {
        if (SEEN.add(target)) LOG.info("[creativecalendar] grass season hook live: {}", target);
        if (!SEASONS) return false;
        return CropSeasons.refuseSpread(level, pos, CropSeasons.typeOf(state));
    }
}
