package com.SaintAlfonzo.creativeworldgen.wiseoak;

import java.util.UUID;

/** Per-player trust on a Wise Oak block entity, implemented by {@code mixin/wiseoak/WiseOakTileMixin}. */
public interface WiseOakTrustAccess {

    /** 0 when the player has no record on this tree; reading does not create one. */
    int creativeworldgen$getTrust(UUID player);

    /** Replaces the player's record, which also clears their talk cooldown. */
    void creativeworldgen$setTrust(UUID player, int trust);
}
