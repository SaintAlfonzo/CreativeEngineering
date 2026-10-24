package com.sammy.minersdelight.upstream;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A built-in pack over a path -> bytes map ({@link MinersTranslator}). The map is only built when the game first asks
 * for content, after the registries are complete; pack metadata comes from code.
 */
public class MemoryPackResources implements PackResources {
    private final String name;
    private final PackType type;
    private final Supplier<Map<String, byte[]>> files;

    public MemoryPackResources(String name, PackType type, Supplier<Map<String, byte[]>> files) {
        this.name = name;
        this.type = type;
        this.files = files;
    }

    private static String path(PackType type, ResourceLocation location) {
        return type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
    }

    @Override
    public @Nullable InputStream getRootResource(@NotNull String fileName) throws FileNotFoundException {
        throw new FileNotFoundException(fileName);
    }

    @Override
    public @NotNull InputStream getResource(@NotNull PackType packType, @NotNull ResourceLocation location) throws FileNotFoundException {
        byte[] bytes = packType == type ? files.get().get(path(packType, location)) : null;
        if (bytes == null) {
            throw new FileNotFoundException(path(packType, location));
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public @NotNull Collection<ResourceLocation> getResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull Predicate<ResourceLocation> filter) {
        if (packType != type) {
            return List.of();
        }
        String prefix = packType.getDirectory() + "/" + namespace + "/";
        String folder = prefix + path + "/";
        List<ResourceLocation> found = new ArrayList<>();
        for (String file : files.get().keySet()) {
            if (file.startsWith(folder)) {
                ResourceLocation location = ResourceLocation.tryParse(namespace + ":" + file.substring(prefix.length()));
                if (location != null && filter.test(location)) {
                    found.add(location);
                }
            }
        }
        return found;
    }

    @Override
    public boolean hasResource(@NotNull PackType packType, @NotNull ResourceLocation location) {
        return packType == type && files.get().containsKey(path(packType, location));
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType packType) {
        Set<String> namespaces = new HashSet<>();
        if (packType == type) {
            for (String file : files.get().keySet()) {
                String[] parts = file.split("/", 3);
                if (parts.length == 3) {
                    namespaces.add(parts[1]);
                }
            }
        }
        return namespaces;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getMetadataSection(@NotNull MetadataSectionSerializer<T> serializer) {
        if (serializer.getMetadataSectionName().equals("pack")) {
            JsonObject pack = new JsonObject();
            pack.addProperty("description", "Miner's Delight: art and data read from the upstream jar");
            pack.addProperty("pack_format", type == PackType.CLIENT_RESOURCES ? 9 : 10);
            return serializer.fromJson(pack);
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
