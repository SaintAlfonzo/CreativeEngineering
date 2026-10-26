package com.SaintAlfonzo.creativedelight.rustic.upstream;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;

/**
 * Adds the upstream art and data as built-in packs, always on and fixed at the bottom: resource packs, KubeJS and
 * datapacks still override them (feature_writeup/rustic_delight.md 5).
 */
public final class UpstreamPacks {
    private UpstreamPacks() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        PackType type = event.getPackType();
        String id = type == PackType.CLIENT_RESOURCES ? "rustic_upstream_assets" : "rustic_upstream_data";
        event.addRepositorySource((consumer, constructor) -> {
            Pack pack = Pack.create(id, true,
                    () -> new MemoryPackResources(id, type, type == PackType.CLIENT_RESOURCES ? RusticTranslator::assets : RusticTranslator::data),
                    constructor, Pack.Position.BOTTOM, PackSource.BUILT_IN);
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }
}
