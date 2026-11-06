package com.SaintAlfonzo.cecapture;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.nio.file.Path;

/**
 * CE Client Capture: a dev-only driver that runs a capture plan in a throwaway client
 * (feature_writeup/client_capture.md). tools/client_capture/run_captures.py builds its own game dir with this
 * jar in it and passes {@code -Dcecapture.plan=<plan json>}; without that property the mod does nothing.
 */
@Mod(CeCapture.ID)
public class CeCapture {

    public static final String ID = "cecapture";

    public CeCapture() {
        String plan = System.getProperty("cecapture.plan");
        if (FMLEnvironment.dist != Dist.CLIENT || plan == null || plan.isBlank()) return;
        MinecraftForge.EVENT_BUS.register(new CaptureDriver(CapturePlan.load(Path.of(plan))));
    }
}
