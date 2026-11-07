package com.SaintAlfonzo.creativecompat.compat;

import java.lang.reflect.Field;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import org.slf4j.Logger;

/**
 * ImmediatelyFast's HUD item batch, read by class name so this jar has no hard dependency on ImmediatelyFast
 * (mixin/GeckoItemHudBatchMixin; plan_portal_gun.md 2).
 */
public final class GeckoHudBatch {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field ITEM_MODEL_CONSUMER = find();

    private GeckoHudBatch() {}

    private static Field find() {
        try {
            return Class.forName("net.raphimc.immediatelyfast.feature.batching.BatchingBuffers")
                    .getField("ITEM_MODEL_CONSUMER");
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("[creativecompat] ImmediatelyFast BatchingBuffers.ITEM_MODEL_CONSUMER not found; GeckoLib items "
                    + "in the hotbar keep flushing the HUD batch: {}", e.toString());
            return null;
        }
    }

    /** True when {@code source} is ImmediatelyFast's active HUD item-model batch. */
    public static boolean isItemBatch(MultiBufferSource source) {
        if (ITEM_MODEL_CONSUMER == null || source == null) return false;
        try {
            return ITEM_MODEL_CONSUMER.get(null) == source;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}
