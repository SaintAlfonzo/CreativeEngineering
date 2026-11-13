// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Upstream tests `level instanceof PonderLevel` (Create 6 ponder library, common code). On Create 0.5.1 the
// ponder level is PonderWorld, a client class; this keeps the check off dedicated servers (a ponder level is always a
// client level).
package org.patryk3211.powergrid.utility;

import net.minecraft.world.level.LevelAccessor;

public final class PonderLevels {
    private PonderLevels() {}

    public static boolean isPonder(LevelAccessor level) {
        return level != null && level.isClientSide() && Client.isPonder(level);
    }

    private static final class Client {
        static boolean isPonder(LevelAccessor level) {
            return level instanceof com.simibubi.create.foundation.ponder.PonderWorld;
        }
    }
}
