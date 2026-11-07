package com.SaintAlfonzo.creativecommon.loadingtips;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads the tip list from every resource pack: {@code assets/<namespace>/loading_tips/<name>.json}, each
 * {@code {"replace": false, "tips": ["text or lang key", ...]}} (the format of Loading Screen Tips by UltrusBot,
 * MIT). Files are read in id order. A file with {@code "replace": true} drops every tip read before it, so a
 * resource pack can swap the whole list out. A tip is shown as the translation when it is a lang key, else as
 * written.
 */
public final class LoadingTips extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile List<String> tips = List.of();

    private LoadingTips() {
        super(new Gson(), "loading_tips");
    }

    public static List<String> tips() {
        return tips;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> e : new TreeMap<>(files).entrySet()) {
            try {
                JsonObject o = GsonHelper.convertToJsonObject(e.getValue(), "loading tips");
                if (GsonHelper.getAsBoolean(o, "replace", false)) out.clear();
                for (JsonElement t : GsonHelper.getAsJsonArray(o, "tips")) {
                    String s = GsonHelper.convertToString(t, "tip").strip();
                    if (!s.isEmpty() && !out.contains(s)) out.add(s);
                }
            } catch (RuntimeException ex) {
                LOGGER.warn("Loading tips: skipped {}: {}", e.getKey(), ex.getMessage());
            }
        }
        tips = List.copyOf(out);
        LoadingTipBar.reset();
        LOGGER.info("Loading tips: {} tips from {} files", out.size(), files.size());
    }

    @Mod.EventBusSubscriber(modid = CreativeCommon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Reload {
        private Reload() {}

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new LoadingTips());
        }
    }
}
