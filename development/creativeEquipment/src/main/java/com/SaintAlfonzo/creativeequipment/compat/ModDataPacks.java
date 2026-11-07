package com.SaintAlfonzo.creativeequipment.compat;

import com.SaintAlfonzo.creativeequipment.CreativeEquipment;
import com.SaintAlfonzo.creativeequipment.shared.BuiltInPacks;
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
 * Equipment data that names another mod's blocks or items: {@code packs/<modid>/} in this jar, each added as a required,
 * hidden data pack only while that mod is loaded ({@code packs/<modA>+<modB>/}: only while all of them are) (Forge 1.19.2 has no conditions for worldgen registries, so a biome
 * modifier naming an absent block would break world loading). Same pattern as Creative Delight's CompartmentPacks.
 * First folder: {@code felsic_gear} (End felsic ore worldgen and the felsic block loot table, moved from the pack's
 * kubejs/data on 2026-11-06, TODO `creativebrewing/publish #22`).
 */
public final class ModDataPacks {

    private ModDataPacks() {}

    public static void add(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        Path root = ModList.get().getModFileById(CreativeEquipment.MOD_ID).getFile().findResource("packs");
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
                BuiltInPacks.addFolder(event, dir, CreativeEquipment.MOD_ID + ":" + mod, "Creative Equipment: " + mod,
                        Pack.Position.BOTTOM, "Creative Equipment");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Creative Equipment: cannot list packs/", e);
        }
        CreativeEquipment.LOGGER.info("[creativeequipment] mod data packs on: {}; off (mod not loaded): {}",
                on.isEmpty() ? "none" : on, off.isEmpty() ? "none" : off);
    }
}
