package com.SaintAlfonzo.creativeworldgen.visceralheap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Biomes O' Plenty's mod pack with only the Visceral Heap's files visible. BOP's tag files name removed entries, and one
 * missing entry makes vanilla drop the whole tag, so everything else in the jar has to stay hidden. The files stay in the
 * jar under BOP's own ids; nothing is copied out.
 */
public final class HeapPackResources extends PathPackResources {

    private static final String SOLID = "flesh|porous_flesh|pus_bubble|flesh_tendons|flesh_tendons_strand|eyebulb|hair";
    private static final String SOUNDS_JSON = "sounds.json";
    private static final String HEAP_MUSIC_EVENT = "music.nether.visceral_heap";

    private static final Pattern DATA = Pattern.compile(
            "loot_tables/blocks/(" + SOLID + ")\\.json"
                    + "|tags/blocks/flesh\\.json"
                    + "|tags/fluids/blood\\.json");

    private static final Pattern ASSETS = Pattern.compile(
            "blockstates/(blood|" + SOLID + ")\\.json"
                    + "|models/block/(blood|eyebulb_bottom|eyebulb_top|flesh|flesh_tendons|flesh_tendons_alt"
                    + "|flesh_tendons_strand|flesh_tendons_strand_alt|hair|porous_flesh|pus_bubble)\\.json"
                    + "|models/item/(blood|blood_bucket|rose_quartz_bud|" + SOLID + ")\\.json"
                    + "|textures/block/(blood_flow|blood_still|eyebulb_bottom|eyebulb_top|flesh|flesh_tendons"
                    + "|flesh_tendons_alt|flesh_tendons_strand|flesh_tendons_strand_alt|hair|porous_flesh|pus_bubble)"
                    + "\\.png(\\.mcmeta)?"
                    + "|textures/item/(blood_bucket|eyebulb|hair|pus_bubble)\\.png(\\.mcmeta)?"
                    + "|particles/(dripping_blood|falling_blood|landing_blood|pus)\\.json"
                    + "|textures/particle/pus\\.png(\\.mcmeta)?"
                    + "|lang/[a-z0-9_]+\\.json"
                    + "|" + Pattern.quote(SOUNDS_JSON)
                    + "|sounds/music/game/nether/(mesh|redhead|blot|timber|cortisol)\\.ogg");

    private final IModFile file;

    public HeapPackResources(IModFileInfo info) {
        super(info.getFile().getFileName(), info.getFile().getFilePath());
        this.file = info.getFile();
    }

    private static boolean visible(PackType type, String namespace, String path) {
        if (!HeapAllowlist.MOD_ID.equals(namespace)) return false;
        return (type == PackType.SERVER_DATA ? DATA : ASSETS).matcher(path).matches();
    }

    @Override
    protected Path resolve(String... paths) {
        return file.findResource(paths);
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation location) {
        return visible(type, location.getNamespace(), location.getPath()) && super.hasResource(type, location);
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
        if (!visible(type, location.getNamespace(), location.getPath())) throw new FileNotFoundException(location.toString());
        if (SOUNDS_JSON.equals(location.getPath())) return heapSoundsOnly(super.getResource(type, location));
        return super.getResource(type, location);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, Predicate<ResourceLocation> filter) {
        if (!HeapAllowlist.MOD_ID.equals(namespace)) return Set.of();
        return super.getResources(type, namespace, path, filter).stream()
                .filter(location -> visible(type, location.getNamespace(), location.getPath()))
                .toList();
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Set.of(HeapAllowlist.MOD_ID);
    }

    /** BOP's sounds.json lists every BOP sound; the others point at hidden files and would each log a missing-file warning. */
    private static InputStream heapSoundsOnly(InputStream original) throws IOException {
        try (InputStream in = original; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            JsonObject all = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject kept = new JsonObject();
            kept.add(HEAP_MUSIC_EVENT, all.get(HEAP_MUSIC_EVENT));
            return new ByteArrayInputStream(kept.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
