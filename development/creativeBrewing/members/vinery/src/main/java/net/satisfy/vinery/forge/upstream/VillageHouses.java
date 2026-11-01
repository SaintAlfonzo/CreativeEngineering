package net.satisfy.vinery.forge.upstream;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.forge.VineryConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The four 1.5.5 village houses (alcohol.md B5 D8; TODO alcohol/vineryport #14). Upstream ships the NBTs but adds them
 * to no pool. The port reads them from the donor jar (ARR data, nothing copied: D13), rewrites the palette to the
 * port's ids in memory and serves them as {@code vinery:vinery_<biome>}; at server start each one is appended to its
 * vanilla village houses pool with {@code [village_houses] weight} (0 = the pool's average piece weight, so the house
 * is as common as an average house there). PoolTuner (patcherMod, LOWEST priority) runs after this and records the
 * piece in pool_config.json like any mod-injected house, so it can be reweighted or disabled from there.
 */
public final class VillageHouses {
    /** Donor NBT name -> the village houses pool it joins. */
    public static final Map<String, ResourceLocation> HOUSES = new LinkedHashMap<>();
    /** 1.5.5 palette ids the port has no block for -> the port block that stands in. Applied before {@link IdMap}. */
    static final Map<String, String> BLOCK_SUBSTITUTES = Map.of(
            "vinery:apple_press", "vinery:wine_press",
            "vinery:red_grape_crate", "vinery:red_grape_bag",
            "vinery:white_grape_crate", "vinery:white_grape_bag",
            // 1.19.2 has no hanging signs (D9). A wall hanging sign is held by the blocks beside it, so a wall sign
            // (needs a block behind) and a lantern (needs one above) both popped on the scratch server; a chain stub
            // running like the sign's bar needs no support and stays
            "vinery:cherry_wall_hanging_sign", "minecraft:chain",
            "vinery:dark_cherry_wall_hanging_sign", "minecraft:chain");

    static {
        HOUSES.put("vinery_plains", new ResourceLocation("minecraft", "village/plains/houses"));
        HOUSES.put("vinery_taiga", new ResourceLocation("minecraft", "village/taiga/houses"));
        HOUSES.put("vinery_savanna", new ResourceLocation("minecraft", "village/savanna/houses"));
        HOUSES.put("vinery_desert", new ResourceLocation("minecraft", "village/desert/houses"));
    }

    private static final Field RAW_TEMPLATES = ObfuscationReflectionHelper.findField(StructureTemplatePool.class, "f_210559_");
    private static final Field TEMPLATES = ObfuscationReflectionHelper.findField(StructureTemplatePool.class, "f_210560_");
    private static final Field MAX_SIZE = ObfuscationReflectionHelper.findField(StructureTemplatePool.class, "f_210562_");

    private VillageHouses() {
    }

    // ---------------------------------------------------------------- data (UpstreamTranslator.buildData)

    /** Adds {@code data/vinery/structures/vinery_<biome>.nbt} with the palette rewritten; a missing or unreadable NBT is logged and skipped. */
    static void addStructures(Map<String, byte[]> out) {
        if (!VineryConfig.VILLAGE_HOUSES_ENABLED.get()) {
            return;
        }
        Map<String, byte[]> jar = UpstreamJars.vinery155();
        for (String name : HOUSES.keySet()) {
            byte[] raw = jar.get("data/vinery/structure/" + name + ".nbt");
            if (raw == null) {
                Vinery.LOGGER.warn("[vinery-houses] {} is not in the 1.5.5 jar", name);
                continue;
            }
            try {
                out.put("data/vinery/structures/" + name + ".nbt", translate(raw));
            } catch (IOException e) {
                Vinery.LOGGER.warn("[vinery-houses] could not read {}: {}", name, e.toString());
            }
        }
    }

    /** Palette names through the substitutes and the id map; DataVersion set to this game's (the file says 1.20.1). */
    static byte[] translate(byte[] raw) throws IOException {
        CompoundTag tag = NbtIo.readCompressed(new ByteArrayInputStream(raw));
        if (tag.contains("palette", Tag.TAG_LIST)) {
            rewritePalette(tag.getList("palette", Tag.TAG_COMPOUND));
        }
        if (tag.contains("palettes", Tag.TAG_LIST)) {
            for (Tag palette : tag.getList("palettes", Tag.TAG_LIST)) {
                if (palette instanceof ListTag list) {
                    rewritePalette(list);
                }
            }
        }
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, bytes);
        return bytes.toByteArray();
    }

    private static void rewritePalette(ListTag palette) {
        for (Tag entry : palette) {
            if (entry instanceof CompoundTag state && state.contains("Name", Tag.TAG_STRING)) {
                String name = rewriteBlock(state.getString("Name"));
                state.putString("Name", name);
                if (name.equals("minecraft:chain") && !state.getCompound("Properties").getString("facing").isEmpty()) {
                    // wall hanging sign properties (facing, waterlogged) -> chain's (axis across the facing, waterlogged)
                    CompoundTag old = state.getCompound("Properties");
                    String facing = old.getString("facing");
                    CompoundTag props = new CompoundTag();
                    props.putString("axis", facing.equals("north") || facing.equals("south") ? "x" : "z");
                    props.putString("waterlogged", old.getString("waterlogged").isEmpty() ? "false" : old.getString("waterlogged"));
                    state.put("Properties", props);
                }
            }
        }
    }

    static String rewriteBlock(String name) {
        String substitute = BLOCK_SUBSTITUTES.get(name);
        return IdMap.rewrite(substitute != null ? substitute : name);
    }

    // ---------------------------------------------------------------- pools (ServerAboutToStartEvent)

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!VineryConfig.VILLAGE_HOUSES_ENABLED.get()) {
            return;
        }
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        int configured = VineryConfig.VILLAGE_HOUSES_WEIGHT.get();
        for (Map.Entry<String, ResourceLocation> house : HOUSES.entrySet()) {
            StructureTemplatePool pool = pools.get(house.getValue());
            if (pool == null) {
                Vinery.LOGGER.warn("[vinery-houses] pool {} not found, {} not added", house.getValue(), house.getKey());
                continue;
            }
            String template = "vinery:" + house.getKey();
            try {
                append(pool, house.getValue(), template, configured);
            } catch (ReflectiveOperationException | RuntimeException e) {
                Vinery.LOGGER.error("[vinery-houses] could not add {} to {}", template, house.getValue(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void append(StructureTemplatePool pool, ResourceLocation poolId, String template, int configured) throws ReflectiveOperationException {
        List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) RAW_TEMPLATES.get(pool));
        long total = 0;
        for (Pair<StructurePoolElement, Integer> pair : raw) {
            if (pair.getFirst().toString().contains(template)) {
                Vinery.LOGGER.debug("[vinery-houses] {} already in {}", template, poolId);
                return; // singleplayer: builtin pools survive across world loads
            }
            total += pair.getSecond();
        }
        int weight = configured > 0 ? configured : Math.max(1, (int) Math.round(raw.isEmpty() ? 1 : (double) total / raw.size()));
        StructurePoolElement element = StructurePoolElement.legacy(template).apply(StructureTemplatePool.Projection.RIGID);
        raw.add(Pair.of(element, weight));
        RAW_TEMPLATES.set(pool, raw);
        ObjectArrayList<StructurePoolElement> templates = (ObjectArrayList<StructurePoolElement>) TEMPLATES.get(pool);
        for (int i = 0; i < weight; i++) {
            templates.add(element);
        }
        MAX_SIZE.setInt(pool, Integer.MIN_VALUE); // recomputed on the next getMaxSize
        Vinery.LOGGER.info("[vinery-houses] {} added to {} at weight {} ({} pieces, total weight {} -> {}{})", template, poolId, weight,
                raw.size() - 1, total, total + weight, configured > 0 ? ", configured" : ", pool average");
    }
}
