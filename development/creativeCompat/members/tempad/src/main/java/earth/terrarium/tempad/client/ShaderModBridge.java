// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/ShaderModBridge.kt. Translated to Java for Forge 1.19.2.
// On 1.19.2 the Iris API (net.irisshaders.iris.api.v0) comes with Oculus.
package earth.terrarium.tempad.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class ShaderModBridge {
    private static final MethodHandle SHADERS_ENABLED;

    static {
        MethodHandle handle = null;
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApi.getDeclaredMethod("getInstance").invoke(null);
            handle = MethodHandles.lookup().unreflect(irisApi.getDeclaredMethod("isShaderPackInUse")).bindTo(instance);
        } catch (Throwable ignored) {
        }
        SHADERS_ENABLED = handle;
    }

    private ShaderModBridge() {
    }

    public static boolean shadersEnabled() {
        if (SHADERS_ENABLED == null) return false;
        try {
            return (boolean) SHADERS_ENABLED.invoke();
        } catch (Throwable e) {
            return false;
        }
    }
}
