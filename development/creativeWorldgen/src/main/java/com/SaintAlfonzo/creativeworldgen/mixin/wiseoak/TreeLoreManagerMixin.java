package com.SaintAlfonzo.creativeworldgen.mixin.wiseoak;

import com.mojang.logging.LogUtils;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops the Wise Oak's web lookups (public IP, location, weather, quotes, facts, names), which otherwise start at launch
 * on clients and dedicated servers (feature_writeup/wise_oak.md 3 and 4.6). {@code initIfNeeded} queues all of them;
 * {@code readFromURL} is the single place a connection opens, cut as well in case anything else reaches it. The
 * mod's own offline fallbacks then apply.
 */
@Mixin(value = TreeLoreManager.class, remap = false)
public abstract class TreeLoreManagerMixin {

    @Unique private static final Logger creativeworldgen$LOGGER = LogUtils.getLogger();
    @Unique private static boolean creativeworldgen$logged;

    @Inject(method = "initIfNeeded", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void creativeworldgen$blockLookups(CallbackInfo ci) {
        if (!creativeworldgen$logged) {
            creativeworldgen$logged = true;
            creativeworldgen$LOGGER.info("Wise Oak web lookups disabled");
        }
        ci.cancel();
    }

    @Inject(method = "readFromURL(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void creativeworldgen$noRead(String url, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("");
    }

    @Inject(method = "readFromURL(Ljava/lang/String;Z)Ljava/lang/String;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void creativeworldgen$noReadKeyed(String url, boolean useKey, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("");
    }
}
