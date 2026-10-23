package com.SaintAlfonzo.creativeengineering.compat.jei.world;

import static com.SaintAlfonzo.creativeengineering.compat.jei.world.BurnRows.array;
import static com.SaintAlfonzo.creativeengineering.compat.jei.world.BurnRows.ing;
import static com.SaintAlfonzo.creativeengineering.compat.jei.world.BurnRows.row;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.campfire.CampfireRain;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Campfire Rules tab: how a campfire is placed, lit and put out in this pack. Sources (read 2026-10-22):
 * <ul>
 *   <li>Unlit Campfire 1.7.1.0 ({@code CampfireBlockMixin}): every {@code CampfireBlock} is placed unlit; lit time 0
 *       burns forever; its rain check is off. Its config is a SERVER config, not loaded on the client when JEI starts,
 *       so this reads the pack default {@code defaultconfigs/unlitcampfire-server.toml} (what a new world gets).</li>
 *   <li>Vanilla: flint and steel, fire charge or a burning projectile light it (not when waterlogged); a shovel,
 *       water placed in it or a splash water bottle put it out. A shovel's dowse keeps the food.</li>
 *   <li>Pack rain rule ({@link CampfireRain}, CEConfig [campfire], a COMMON config so read live): lit campfires in
 *       {@code #creativeengineering:campfire/rain_douses} go out after {@code rainOutTicks} of open rain unless on
 *       {@code rain_proof_fuel}. Comfort ({@code CampfireComfort}) from the same tick.</li>
 *   <li>Tinted Campfires 1.2.9: own block class (not a {@code CampfireBlock}), placed lit unless in water; neither
 *       Unlit Campfire nor the rain rule touches them.</li>
 * </ul>
 * Rows in the {@link WorldRow} JSON format.
 */
final class CampfireRows {
    private CampfireRows() {}

    private static final String UNLIT = "Unlit";
    private static final String LIT = "Lit";

    static List<JsonObject> rows() {
        List<JsonObject> rows = new ArrayList<>();
        boolean unlitMod = ModList.get().isLoaded("unlitcampfire");
        Path unlitCfg = FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve("unlitcampfire-server.toml");

        // Placing (Unlit Campfire)
        if (unlitMod) {
            int litTime = tomlInt(unlitCfg, "unlitcampfire.campfireLitTime", 0);
            String burn = litTime < 1 ? "Once lit, it burns forever." : "Burns " + litTime / 20 + " s; feed it fuel to burn longer.";
            rows.add(row(List.of(items("minecraft:campfire", "minecraft:soul_campfire", null)), null,
                    List.of(blocks(UNLIT)), List.of("Placed unlit.", burn), true));
        }

        // Lighting (vanilla FlintAndSteelItem / FireChargeItem / CampfireBlock#onProjectileHit)
        JsonObject lighters = new JsonObject();
        lighters.add("items", array(List.of("minecraft:flint_and_steel", "minecraft:fire_charge")));
        rows.add(row(List.of(blocks(UNLIT)), lighters, List.of(blocks(LIT)),
                List.of("A burning arrow lights it too.", "Not while waterlogged."), true));

        // Putting out: shovel, water
        rows.add(row(List.of(blocks(LIT)), tag("forge:tools/shovels", null), List.of(blocks(UNLIT)),
                List.of("Right-click with a shovel: puts it out.", "Food on it stays."), true));
        rows.add(row(List.of(blocks(LIT)), ing("item", "minecraft:water_bucket", null), List.of(blocks(UNLIT)),
                List.of("Water or a splash water bottle puts it out."), true));

        // Rain (pack rule)
        CEConfig.Campfire cfg = CEConfig.CAMPFIRE;
        if (cfg.rainDouses.get()) {
            int seconds = Math.max(1, Math.round(cfg.rainOutTicks.get() / 20f));
            JsonObject douses = ing("block_tag", CampfireRain.RAIN_DOUSES.location().toString(), LIT);
            JsonObject dousedOut = douses.deepCopy();
            dousedOut.addProperty("label", UNLIT);
            rows.add(row(List.of(douses), rain(), List.of(dousedOut),
                    List.of(seconds + " s of open rain puts it out.", "Food on it stays."), true));

            JsonObject onFuel = ing("block", "minecraft:campfire", LIT);
            JsonObject fuel = ing("block_tag", CampfireRain.RAIN_PROOF_FUEL.location().toString(), "Under the campfire");
            rows.add(row(List.of(onFuel, fuel), rain(), List.of(ing("block", "minecraft:campfire", "Stays lit")),
                    List.of("Rain never puts it out on coal or charcoal."), false));

            if (!Blocks.SOUL_CAMPFIRE.defaultBlockState().is(CampfireRain.RAIN_DOUSES)
                    && (!unlitMod || tomlInt(unlitCfg, "unlitcampfire.soulCampfireRainUnlitTime", -1) < 0)) {
                rows.add(row(List.of(ing("block", "minecraft:soul_campfire", LIT)), rain(),
                        List.of(ing("block", "minecraft:soul_campfire", "Stays lit")),
                        List.of("Rain never puts a soul campfire out."), true));
            }
        }

        // Comfort (Comfortable Campfires port)
        if (cfg.comfort.get()) {
            boolean fdComfort = cfg.useComfort.get() && ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation("farmersdelight", "comfort"));
            String effect = fdComfort ? "Comfort" : "Regeneration";
            String effectId = fdComfort ? "farmersdelight:comfort" : "minecraft:regeneration";
            rows.add(row(List.of(blocks(LIT)), null,
                    List.of(ing("effect", effectId, cfg.comfortSeconds.get() + " s, kept up nearby")),
                    List.of("Players within " + cfg.comfortDistance.get() + " blocks get " + effect + "."), false));
        }

        // Tinted Campfires
        if (ModList.get().isLoaded("tintedcampfires")) {
            List<String> tinted = new ArrayList<>();
            for (Block block : ForgeRegistries.BLOCKS) {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                if (id != null && id.getNamespace().equals("tintedcampfires") && id.getPath().endsWith("_campfire")) tinted.add(id.toString());
            }
            if (!tinted.isEmpty()) {
                JsonObject in = new JsonObject();
                in.add("items", array(tinted));
                JsonObject out = new JsonObject();
                out.add("blocks", array(tinted));
                out.addProperty("label", LIT);
                rows.add(row(List.of(in), null, List.of(out),
                        List.of("Placed lit.", "Rain and the unlit rule skip them."), true));
            }
        }
        return rows;
    }

    /** Campfire and soul campfire as blocks, with a state label. */
    private static JsonObject blocks(String label) {
        JsonObject o = new JsonObject();
        o.add("blocks", array(List.of("minecraft:campfire", "minecraft:soul_campfire")));
        o.addProperty("label", label);
        return o;
    }

    private static JsonObject items(String a, String b, String label) {
        JsonObject o = new JsonObject();
        o.add("items", array(List.of(a, b)));
        if (label != null) o.addProperty("label", label);
        return o;
    }

    private static JsonObject tag(String tag, String label) {
        return ing("tag", tag, label);
    }

    /** Stand-in for open rain: a cloud (or a water bucket) named "Rain". */
    private static JsonObject rain() {
        String id = ModList.get().isLoaded("cloudstorage") ? "cloudstorage:cloud" : "minecraft:water_bucket";
        JsonObject o = ing("item", id, "Open sky above");
        o.addProperty("nbt", "{display:{Name:'{\"text\":\"Rain\",\"italic\":false}'}}");
        return o;
    }

    private static int tomlInt(Path file, String path, int fallback) {
        Object value = BurnRows.tomlValue(file, path);
        return value instanceof Number n ? n.intValue() : fallback;
    }
}
