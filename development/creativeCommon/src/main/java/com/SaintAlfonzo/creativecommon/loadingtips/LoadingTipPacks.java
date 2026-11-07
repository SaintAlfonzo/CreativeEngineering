package com.SaintAlfonzo.creativecommon.loadingtips;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The tip resource packs built into this jar ({@code resourcepacks/<folder>/}), registered as required client
 * packs so they are always on (feature_writeup/loading_tips.md). One pack per source, each under its own
 * namespace so the files never shadow each other: Loading Tips (filtered upstream text, MIT, see its
 * CREDITS.txt) and Creative Tips (the pack's own tips).
 */
@Mod.EventBusSubscriber(modid = CreativeCommon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LoadingTipPacks {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Folder under resourcepacks/ and the pack's display name. */
    private static final String[][] PACKS = {
            {"loading_tips", "Loading Tips"},
            {"creative_tips", "Creative Tips"},
    };

    private LoadingTipPacks() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        IModFile file = ModList.get().getModFileById(CreativeCommon.MODID).getFile();
        for (String[] p : PACKS) {
            Path root = file.findResource("resourcepacks", p[0]);
            if (!Files.isDirectory(root)) {
                LOGGER.warn("Loading tips: built-in pack resourcepacks/{} is missing from the jar", p[0]);
                continue;
            }
            String id = CreativeCommon.MODID + ":" + p[0];
            event.addRepositorySource((consumer, constructor) -> {
                Pack pack = Pack.create(id, true, () -> new PathPackResources(p[1], root),
                        constructor, Pack.Position.TOP, PackSource.BUILT_IN);
                if (pack != null) consumer.accept(pack);
            });
        }
    }
}
