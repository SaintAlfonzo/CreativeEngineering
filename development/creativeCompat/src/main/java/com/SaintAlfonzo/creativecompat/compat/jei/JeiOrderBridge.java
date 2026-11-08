package com.SaintAlfonzo.creativecompat.compat.jei;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * What the client script kubejs/jei_order.js calls. Has no JEI types in any signature, so Rhino can always list its
 * methods, and it reaches {@link PackIngredientOrder} (which links against JEI's internals) only when the JEI gate is
 * on and only inside a guard.
 */
public final class JeiOrderBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    private JeiOrderBridge() {}

    public static void define(String json) {
        if (!JeiGate.supported()) {
            return;
        }
        try {
            PackIngredientOrder.define(json);
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn("[jei order] could not hand the pack's order to the JEI sorter, JEI keeps its own order", e);
        }
    }
}
