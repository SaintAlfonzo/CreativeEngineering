package com.SaintAlfonzo.creativeworldgen.compat;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import com.SaintAlfonzo.creativeworldgen.shared.BuiltInPacks;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Worldgen data that names a mod this jar does not require: {@code packs/<modid>/} in this jar, each added as a required,
 * hidden data pack only while that mod is loaded ({@code packs/<modA>+<modB>/}: only while all of them are) (Forge 1.19.2 has no conditions for worldgen registries, so a feature
 * or biome modifier naming an absent block would break world loading). Same pattern as Creative Equipment's
 * compat/ModDataPacks and Creative Delight's CompartmentPacks.
 * Moved from the pack's kubejs/data/creativeengineering on 2026-11-06 (ids unchanged): upgrade_aquatic (driftwood on
 * land, the flotilla driftwood meta part), farmersdelight and fungidelight (wild mushroom colonies and patches),
 * miners_delight (cave carrots), vintagedelight (wild ghost peppers).
 */
public final class ModDataPacks {

    private ModDataPacks() {}

    public static void add(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        Path root = ModList.get().getModFileById(CreativeWorldgen.MOD_ID).getFile().findResource("packs");
        if (!Files.isDirectory(root)) return;
        List<String> on = new ArrayList<>();
        List<String> off = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path dir : dirs.filter(Files::isDirectory).sorted().toList()) {
                String mod = dir.getFileName().toString().replace("/", "");
                // "a+b": every listed mod must be loaded (data naming two mods, e.g. felsic_gear+nullscape)
                if (!java.util.Arrays.stream(mod.split("\\+")).allMatch(ModList.get()::isLoaded)) {
                    off.add(mod);
                    continue;
                }
                on.add(mod);
                BuiltInPacks.addFolder(event, dir, CreativeWorldgen.MOD_ID + ":" + mod, "Creative Worldgen: " + mod,
                        Pack.Position.BOTTOM, "Creative Worldgen");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Creative Worldgen: cannot list packs/", e);
        }
        CreativeWorldgen.LOGGER.info("[creativeworldgen] mod data packs on: {}; off (mod not loaded): {}",
                on.isEmpty() ? "none" : on, off.isEmpty() ? "none" : off);
    }
}
