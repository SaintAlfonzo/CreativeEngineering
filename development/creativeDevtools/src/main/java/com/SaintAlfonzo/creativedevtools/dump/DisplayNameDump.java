package com.SaintAlfonzo.creativedevtools.dump;

import com.SaintAlfonzo.creativedevtools.CreativeDevtools;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.commands.Commands;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Client-side name dump for the names audit (feature_writeup/names.md). Unlike the server's /dumpnames
 * (kubejs/server_scripts/_lib/dump_names.js, missing keys only, single player only) this runs on any client,
 * with the client's own language loaded, and writes every name:
 * <ul>
 *   <li>{@code kubejs/exported/display_names.json}: per kind (block, item, effect, enchantment, entity, fluid,
 *       profession, potion) every entry's id, description id, the text the game shows and whether the key
 *       exists; plus {@code src}, the pack and namespace whose lang file won that key, and {@code mods}
 *       (id -> display name after {@code [mod_names]}).</li>
 *   <li>{@code kubejs/exported/names.json}: the missing-only file /dumpnames writes, same format, so
 *       tools/missing_names.py reads either.</li>
 * </ul>
 * Triggers: the client command {@code /cdevdumpnames}, or {@code [debug.names] dumpAtTitle} (once, when the title
 * screen first opens: a scratch client can dump without joining a world). tools/name_audit.py reads the file.
 *
 * <p>Where a key comes from: the game merges lang per namespace ({@code ClientLanguage.loadFrom} walks
 * {@code getNamespaces()}, then each namespace's pack stack bottom to top), so a key defined in two namespaces
 * goes to whichever namespace comes later, whatever the pack order. The dump repeats that walk, which is why
 * {@code src} names the namespace too: an override has to sit in the same namespace, in a higher pack.
 */
@Mod.EventBusSubscriber(modid = CreativeDevtools.MOD_ID, value = Dist.CLIENT)
public final class DisplayNameDump {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean dumpedAtTitle;

    private DisplayNameDump() {}

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cdevdumpnames").executes(ctx -> {
            String summary = dump();
            ctx.getSource().sendSuccess(Component.literal("[cdevdumpnames] " + summary
                    + " -> kubejs/exported/display_names.json").withStyle(ChatFormatting.GREEN), false);
            return 1;
        }));
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (dumpedAtTitle || !(event.getNewScreen() instanceof TitleScreen)) return;
        if (!DevtoolsConfig.DEBUG.dumpNamesAtTitle.get()) return;
        dumpedAtTitle = true;
        dump();
    }

    /** One lang key's winner: pack id and namespace of the lang file that set it last. */
    private record Source(String pack, String namespace, String value) {}

    public static String dump() {
        long start = System.nanoTime();
        Language lang = Language.getInstance();
        String code = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        Map<String, Source> sources = sources(Minecraft.getInstance().getResourceManager(), code);

        JsonObject entries = new JsonObject();
        JsonObject missing = new JsonObject();
        JsonObject totals = new JsonObject();
        Map<String, Integer> missingCounts = new LinkedHashMap<>();

        class Kind {
            final String name;
            final JsonArray all = new JsonArray();
            final JsonArray lacking = new JsonArray();
            Kind(String name) { this.name = name; }

            JsonObject add(String id, String key, String shown) {
                JsonObject row = new JsonObject();
                row.addProperty("id", id);
                row.addProperty("key", key);
                row.addProperty("name", shown);
                boolean has = lang.has(key);
                row.addProperty("has", has);
                Source src = sources.get(key);
                if (src != null) {
                    row.addProperty("pack", src.pack());
                    row.addProperty("ns", src.namespace());
                    // the walk above should agree with the live language; a mismatch means a runtime injection
                    if (!src.value().equals(lang.getOrDefault(key))) row.addProperty("runtime", true);
                }
                all.add(row);
                if (!has) {
                    JsonObject m = new JsonObject();
                    m.addProperty("id", id);
                    m.addProperty("key", key);
                    lacking.add(m);
                }
                return row;
            }

            void finish() {
                entries.add(name, all);
                missing.add(name, lacking);
                totals.addProperty(name, all.size());
                missingCounts.put(name, lacking.size());
            }
        }

        Kind blocks = new Kind("block");
        walk(ForgeRegistries.BLOCKS, (id, b) -> {
            String key = b.getDescriptionId();
            JsonObject row = blocks.add(id, key, safe(() -> b.getName().getString(), key));
            boolean hasItem = !"minecraft:air".equals(String.valueOf(ForgeRegistries.ITEMS.getKey(b.asItem())));
            row.addProperty("hasItem", hasItem);
            JsonArray props = new JsonArray();
            b.getStateDefinition().getProperties().forEach(p -> props.add(p.getName()));
            row.add("props", props);
            if (!row.get("has").getAsBoolean()) { // /dumpnames' block rows carry these too
                JsonObject m = blocks.lacking.get(blocks.lacking.size() - 1).getAsJsonObject();
                m.addProperty("hasItem", hasItem);
                m.add("props", props);
            }
        });
        blocks.finish();

        Kind items = new Kind("item");
        walk(ForgeRegistries.ITEMS, (id, it) -> {
            if ("minecraft:air".equals(id)) return;
            ItemStack stack = new ItemStack(it);
            String key = safe(() -> it.getDescriptionId(stack), it.getDescriptionId());
            items.add(id, key, safe(() -> stack.getHoverName().getString(), key));
        });
        items.finish();

        Kind effects = new Kind("effect");
        walk(ForgeRegistries.MOB_EFFECTS, (id, e) -> effects.add(id, e.getDescriptionId(), safe(() -> e.getDisplayName().getString(), e.getDescriptionId())));
        effects.finish();

        Kind enchants = new Kind("enchantment");
        walk(ForgeRegistries.ENCHANTMENTS, (id, e) -> enchants.add(id, e.getDescriptionId(), translate(e.getDescriptionId())));
        enchants.finish();

        Kind entities = new Kind("entity");
        walk(ForgeRegistries.ENTITY_TYPES, (id, t) -> entities.add(id, t.getDescriptionId(), safe(() -> t.getDescription().getString(), t.getDescriptionId())));
        entities.finish();

        Kind fluids = new Kind("fluid");
        walk(ForgeRegistries.FLUIDS, (id, f) -> {
            if ("minecraft:empty".equals(id)) return;
            String key = f.getFluidType().getDescriptionId();
            fluids.add(id, key, safe(() -> f.getFluidType().getDescription().getString(), key));
        });
        fluids.finish();

        // Forge's Villager.getTypeName: entity.minecraft.villager.<path>, or <ns>:<path> outside minecraft
        Kind professions = new Kind("profession");
        walk(ForgeRegistries.VILLAGER_PROFESSIONS, (id, p) -> {
            if ("minecraft:none".equals(id)) return;
            ResourceLocation rl = new ResourceLocation(id);
            String key = "entity.minecraft.villager." + ("minecraft".equals(rl.getNamespace()) ? rl.getPath() : id);
            professions.add(id, key, translate(key));
        });
        professions.finish();

        // Potions: one row per potion and form; the missing file keeps /dumpnames' one-row-per-potion shape.
        String[][] forms = {
                {"potion", "item.minecraft.potion.effect."},
                {"splash", "item.minecraft.splash_potion.effect."},
                {"lingering", "item.minecraft.lingering_potion.effect."},
                {"arrow", "item.minecraft.tipped_arrow.effect."},
        };
        Kind potions = new Kind("potion");
        JsonArray potionMissing = new JsonArray();
        walk(ForgeRegistries.POTIONS, (id, potion) -> {
            if ("minecraft:empty".equals(id)) return;
            JsonArray lacking = new JsonArray();
            for (String[] form : forms) {
                String key = potion.getName(form[1]);
                JsonObject row = potions.add(id, key, translate(key));
                row.addProperty("form", form[0]);
                if (!lang.has(key)) lacking.add(form[0]);
            }
            if (!lacking.isEmpty()) {
                JsonObject m = new JsonObject();
                m.addProperty("id", id);
                m.addProperty("key", potion.getName(forms[0][1]));
                m.add("forms", lacking);
                potionMissing.add(m);
            }
        });
        entries.add("potion", potions.all);
        missing.add("potion", potionMissing);
        totals.addProperty("potion", ForgeRegistries.POTIONS.getKeys().size() - 1);
        missingCounts.put("potion", potionMissing.size());

        JsonObject mods = new JsonObject();
        ModList.get().getMods().stream()
                .sorted((a, b) -> a.getModId().compareTo(b.getModId()))
                .forEach(m -> mods.addProperty(m.getModId(), m.getDisplayName()));

        String when = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JsonObject out = new JsonObject();
        out.addProperty("generated", when);
        out.addProperty("language", code);
        out.add("mods", mods);
        out.add("entries", entries);

        JsonObject legacy = new JsonObject();
        legacy.addProperty("dedicated", false);
        legacy.addProperty("generated", when);
        legacy.addProperty("source", "client /cdevdumpnames");
        legacy.add("totals", totals);
        legacy.add("missing", missing);

        Path dir = FMLPaths.GAMEDIR.get().resolve("kubejs/exported");
        try {
            Files.createDirectories(dir);
            var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Files.writeString(dir.resolve("display_names.json"), gson.toJson(out), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("names.json"), gson.toJson(legacy), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[cdevdumpnames] could not write {}", dir, e);
            return "write failed: " + e.getMessage();
        }
        StringBuilder summary = new StringBuilder("missing:");
        missingCounts.forEach((k, n) -> summary.append(' ').append(k).append(' ').append(n).append('/').append(totals.get(k).getAsInt()).append(','));
        summary.setLength(summary.length() - 1);
        LOGGER.info("[cdevdumpnames] {} lang keys traced, {} ({} ms)", sources.size(), summary,
                (System.nanoTime() - start) / 1_000_000);
        return summary.toString();
    }

    /** Repeats ClientLanguage.loadFrom: en_us first, then the selected language, each namespace bottom to top. */
    private static Map<String, Source> sources(ResourceManager rm, String code) {
        Map<String, Source> out = new HashMap<>();
        List<String> langs = new ArrayList<>(List.of("en_us"));
        if (!"en_us".equals(code)) langs.add(code);
        for (String l : langs) {
            String path = "lang/" + l + ".json";
            for (String ns : rm.getNamespaces()) {
                ResourceLocation rl;
                try {
                    rl = new ResourceLocation(ns, path);
                } catch (RuntimeException e) {
                    continue;
                }
                for (Resource res : rm.getResourceStack(rl)) {
                    String pack = res.sourcePackId();
                    try (InputStream in = res.open()) {
                        Language.loadFromJson(in, (k, v) -> out.put(k, new Source(pack, ns, v)));
                    } catch (IOException | RuntimeException e) {
                        LOGGER.warn("[cdevdumpnames] skipped {} from {}: {}", rl, pack, e.toString());
                    }
                }
            }
        }
        return out;
    }

    private static <V> void walk(IForgeRegistry<V> reg, BiConsumer<String, V> fn) {
        List<ResourceLocation> keys = new ArrayList<>(reg.getKeys());
        keys.sort(ResourceLocation::compareTo);
        for (ResourceLocation rl : keys) {
            V value = reg.getValue(rl);
            if (value == null) continue;
            try {
                fn.accept(rl.toString(), value);
            } catch (RuntimeException e) {
                LOGGER.warn("[cdevdumpnames] {} {}: {}", reg.getRegistryName(), rl, e.toString());
            }
        }
    }

    private static String translate(String key) {
        return Component.translatable(key).getString();
    }

    private static String safe(Supplier<String> fn, String fallback) {
        try {
            String s = fn.get();
            return s == null ? fallback : s;
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
