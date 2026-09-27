package com.SaintAlfonzo.creativeengineering.devtools;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * /dumpservermods - copies every server-side jar from mods/ into server_mods/ (game directory),
 * the in-game twin of tools/dump_server_mods.py. Both read the table in mod_sides.md:
 * side "both"/"server" is copied, "client" is skipped.
 *
 * Lives in Java rather than KubeJS because KubeJS's class filter blocks file copies; this is
 * also why the matching rules below mirror the Python script line for line (first mod id from
 * META-INF/mods.toml or fabric.mod.json, falling back to the exact file name for jars that
 * declare none), so the two can never disagree about which jars go on the server.
 *
 * Writes nothing if any jar has no row in mod_sides.md. Only meaningful in the dev instance -
 * a dedicated server has no mod_sides.md and just reports that.
 */
@Mod.EventBusSubscriber(modid = "creativeengineering", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerModsDump {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern MOD_ID = Pattern.compile("modId\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Set<String> SERVER_SIDES = Set.of("both", "server");
    private static final Set<String> VALID_SIDES = Set.of("both", "server", "client");

    private ServerModsDump() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dumpservermods")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> run(ctx.getSource())));
    }

    private static int run(CommandSourceStack source) {
        Path root = FMLPaths.GAMEDIR.get();
        Path doc = root.resolve("mod_sides.md");
        Path mods = FMLPaths.MODSDIR.get();
        Path out = root.resolve("server_mods");
        if (!Files.isRegularFile(doc)) {
            source.sendFailure(Component.literal("mod_sides.md not found in " + root + " - run this from the dev instance."));
            return 0;
        }
        try {
            Map<String, String> byModId = new HashMap<>();
            Map<String, String> byJar = new HashMap<>();
            readDoc(doc, byModId, byJar);

            List<String> copy = new ArrayList<>();
            List<String> skip = new ArrayList<>();
            List<String> unknown = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();
            Set<String> seenJars = new HashSet<>();
            for (Path jar : listJars(mods)) {
                String name = jar.getFileName().toString();
                String modId = jarModId(jar);
                String side = modId.isEmpty() ? null : byModId.get(modId);
                if (side != null) {
                    seenIds.add(modId);
                } else {
                    side = byJar.get(name);
                    if (side != null) seenJars.add(name);
                }
                if (side == null) {
                    unknown.add(name + " (mod id: " + (modId.isEmpty() ? "none" : modId) + ")");
                } else {
                    (SERVER_SIDES.contains(side) ? copy : skip).add(name);
                }
            }

            if (!unknown.isEmpty()) {
                source.sendFailure(Component.literal(unknown.size() + " jar(s) have no row in mod_sides.md - add them, then re-run. Nothing was copied:"));
                unknown.forEach(u -> source.sendFailure(Component.literal("  " + u)));
                return 0;
            }

            Set<String> stale = new TreeSet<>(byModId.keySet());
            stale.removeAll(seenIds);
            Set<String> staleJars = new TreeSet<>(byJar.keySet());
            staleJars.removeAll(seenJars);
            stale.addAll(staleJars);

            Files.createDirectories(out);
            try (DirectoryStream<Path> old = Files.newDirectoryStream(out, "*.jar")) {
                for (Path p : old) Files.delete(p);
            }
            for (String name : copy) {
                Files.copy(mods.resolve(name), out.resolve(name), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }

            String msg = "Copied " + copy.size() + " server jars to server_mods/; skipped " + skip.size() + " client-only.";
            LOGGER.info("[dumpservermods] {}", msg);
            source.sendSuccess(Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
            if (!stale.isEmpty()) {
                source.sendSuccess(Component.literal("mod_sides.md rows with no matching jar (removed mods?): " + String.join(", ", stale))
                        .withStyle(ChatFormatting.YELLOW), false);
            }
            return copy.size();
        } catch (IOException | IllegalStateException e) {
            LOGGER.error("[dumpservermods] failed", e);
            source.sendFailure(Component.literal("dumpservermods failed: " + e.getMessage()));
            return 0;
        }
    }

    /** Fills side lookups from mod_sides.md's mod table (the one with Side, Mod id and Jar columns). */
    private static void readDoc(Path doc, Map<String, String> byModId, Map<String, String> byJar) throws IOException {
        Map<String, Integer> cols = null;
        for (String raw : Files.readAllLines(doc, StandardCharsets.UTF_8)) {
            String line = raw.strip();
            if (!line.startsWith("|")) {
                cols = null;
                continue;
            }
            String[] cells = line.replaceAll("^\\||\\|$", "").split("\\|", -1);
            for (int i = 0; i < cells.length; i++) cells[i] = cells[i].strip();
            if (cols == null) {
                Map<String, Integer> header = new HashMap<>();
                for (int i = 0; i < cells.length; i++) header.put(cells[i].toLowerCase(Locale.ROOT), i);
                if (header.keySet().containsAll(Set.of("side", "mod id", "jar"))) cols = header;
                continue;
            }
            if (line.matches("[|\\-: ]+")) continue;
            String side = cells[cols.get("side")].toLowerCase(Locale.ROOT);
            if (!VALID_SIDES.contains(side)) throw new IllegalStateException("mod_sides.md: unknown side '" + side + "' in row: " + line);
            String modId = stripTicks(cells[cols.get("mod id")]);
            String jar = stripTicks(cells[cols.get("jar")]);
            if (!modId.isEmpty() && !modId.equals("-")) byModId.put(modId, side);
            else byJar.put(jar, side);
        }
        if (byModId.isEmpty() && byJar.isEmpty()) {
            throw new IllegalStateException("mod_sides.md: no mod table found (needs Side, Mod id and Jar columns)");
        }
    }

    private static String stripTicks(String s) {
        return s.replaceAll("^`+|`+$", "");
    }

    private static List<Path> listJars(Path mods) throws IOException {
        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(mods, "*.jar")) {
            for (Path p : stream) if (Files.isRegularFile(p)) jars.add(p);
        }
        jars.sort((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()));
        return jars;
    }

    /** First mod id the jar declares, or "" if none. */
    private static String jarModId(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry toml = zip.getEntry("META-INF/mods.toml");
            if (toml != null) {
                String text = new String(zip.getInputStream(toml).readAllBytes(), StandardCharsets.UTF_8);
                // Covers both [[mods]] tables and the inline `mods = [{ modId = '...' }]` form
                // (lowcodefml datapack jars); dependency tables name other mods, so skip those.
                for (String block : text.split("\\[\\[\\s*")) {
                    if (block.startsWith("dependencies")) continue;
                    Matcher m = MOD_ID.matcher(block);
                    if (m.find()) return m.group(1);
                }
            }
            ZipEntry fabric = zip.getEntry("fabric.mod.json");
            if (fabric != null) {
                try {
                    String text = new String(zip.getInputStream(fabric).readAllBytes(), StandardCharsets.UTF_8);
                    JsonElement id = JsonParser.parseString(text).getAsJsonObject().get("id");
                    if (id != null) return id.getAsString();
                } catch (RuntimeException ignored) {
                    // malformed fabric.mod.json: treat as no mod id, same as the Python script
                }
            }
        }
        return "";
    }
}
