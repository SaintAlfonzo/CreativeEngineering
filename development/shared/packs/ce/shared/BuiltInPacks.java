package ce.shared;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.resource.PathPackResources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The built-in, always-on packs the in-tree mods add from {@code AddPackFindersEvent}. Shared source
 * (development/shared/packs, compiled into each jar under its own package; feature_writeup/pack_tooling.md).
 */
public final class BuiltInPacks {
    private BuiltInPacks() {
    }

    /**
     * A folder of this mod's jar ({@code packs/<...>/} with its own pack.mcmeta) as a required, hidden pack: always on,
     * can't be switched off, not listed in the pack screens. A missing pack.mcmeta stops the game, naming
     * {@code owner} (the mod's display name).
     */
    public static void addFolder(AddPackFindersEvent event, Path dir, String packId, String title, Pack.Position position, String owner) {
        event.addRepositorySource((consumer, constructor) -> {
            try (PathPackResources probe = new PathPackResources(packId, dir)) {
                PackMetadataSection meta = probe.getMetadataSection(PackMetadataSection.SERIALIZER);
                if (meta == null) throw new IOException("no pack.mcmeta in " + dir);
                consumer.accept(constructor.create(packId, Component.literal(title), true,
                        () -> new PathPackResources(packId, dir), meta, position, PackSource.BUILT_IN, true));
            } catch (IOException e) {
                throw new IllegalStateException(owner + " pack " + packId + " is broken", e);
            }
        });
    }

    /**
     * An in-memory pack ({@link MemoryPackResources}) of one pack type, required and hidden. Does nothing for the other
     * pack type.
     */
    public static void addMemory(AddPackFindersEvent event, PackType type, String packId, String description, Pack.Position position,
                                 Supplier<Map<String, byte[]>> files) {
        if (event.getPackType() != type) return;
        event.addRepositorySource((consumer, constructor) -> {
            Pack pack = Pack.create(packId, true, () -> new MemoryPackResources(packId, type, description, files),
                    constructor, position, PackSource.BUILT_IN);
            if (pack != null) consumer.accept(pack);
        });
    }

    /**
     * A port's upstream art and data as {@code <prefix>_assets} (resource pack) and {@code <prefix>_data} (data pack),
     * fixed at the bottom: resource packs, KubeJS and datapacks still override them.
     */
    public static void addUpstream(AddPackFindersEvent event, String prefix, String description,
                                   Supplier<Map<String, byte[]>> assets, Supplier<Map<String, byte[]>> data) {
        addMemory(event, PackType.CLIENT_RESOURCES, prefix + "_assets", description, Pack.Position.BOTTOM, assets);
        addMemory(event, PackType.SERVER_DATA, prefix + "_data", description, Pack.Position.BOTTOM, data);
    }
}
