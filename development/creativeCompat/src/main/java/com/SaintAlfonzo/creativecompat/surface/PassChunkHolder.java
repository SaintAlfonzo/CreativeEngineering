package com.SaintAlfonzo.creativecompat.surface;

import javax.annotation.Nullable;

/**
 * Duck interface on {@code LevelChunk} ({@code mixin/LevelChunkPassMixin}): the chunk's baked neighbour counts
 * ({@link PassCounts}), the same object its capability saves, so a lookup is a field read.
 */
public interface PassChunkHolder {

    @Nullable
    PassCountData creativecompat$passCounts();

    void creativecompat$setPassCounts(PassCountData data);
}
