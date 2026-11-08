package com.SaintAlfonzo.creativemagic.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.event.ForgeEventFactory;
import java.util.Optional;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.peripheral.portal_generation.IntrinsicPortalGeneration;

/**
 * Dark Fire's route into Immersive Portals' Nether portal generation (feature_writeup/plan_portal_changes.md 1, N1).
 * Touches IP classes, so it is only loaded when {@code imm_ptl_core} is (DarkFireBlock checks ModList first).
 *
 * mixin/IPFireGateMixin makes IP's own fire hook never build a portal, so ordinary fire falls through to Forge's
 * PortalSpawnEvent and PortalFailureHandler. Dark Fire, after its vanilla rectangle check passed, calls
 * {@link #light}: the thread-local pass lets the gate through for this one call, and IP builds its seamless portal
 * (and the matching frame on the other side) instead of vanilla portal blocks.
 */
public final class IPNetherPortalBridge {

    private static final ThreadLocal<Boolean> PASS = ThreadLocal.withInitial(() -> false);

    private IPNetherPortalBridge() {}

    /** True while Dark Fire's own call is running; read by IPFireGateMixin. */
    public static boolean passing() {
        return PASS.get();
    }

    /**
     * Ordinary fire next to obsidian, with IP's own portal gated off: posts Forge's PortalSpawnEvent for the vanilla
     * shape, as vanilla's {@code BaseFireBlock#onPlace} does without IP (IP's redirect returns an empty shape in
     * {@code normal} mode, so vanilla never posts it). PortalFailureHandler cancels it and ruins the frame.
     */
    public static void postVanillaSpawnEvent(ServerLevel level, BlockPos firePos) {
        Optional<PortalShape> shape = PortalShape.findEmptyPortalShape(level, firePos, Direction.Axis.X);
        ForgeEventFactory.onTrySpawnPortal(level, firePos, shape);
    }

    /**
     * Asks IP to build a Nether portal from the frame around {@code firePos}. False when IP's mode is vanilla or
     * disabled, the dimension has no Nether link (IP only links the Overworld and the Nether), or IP found no frame;
     * the caller then places vanilla portal blocks as before.
     */
    public static boolean light(ServerLevel level, BlockPos firePos) {
        IPGlobal.NetherPortalMode mode = IPGlobal.netherPortalMode;
        if (mode == IPGlobal.NetherPortalMode.vanilla || mode == IPGlobal.NetherPortalMode.disabled) return false;
        PASS.set(true);
        try {
            return IntrinsicPortalGeneration.onFireLitOnObsidian(level, firePos);
        } finally {
            PASS.set(false);
        }
    }
}
