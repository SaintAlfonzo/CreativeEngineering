package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code Holder.Reference.is(ResourceLocation)} (m_203373_) on a holder with no key returns false instead of building
 * vanilla's "Trying to access unbound value" message and throwing (feature_writeup/perf.md 13).
 *
 * <p>Copycats' {@code BlockStateBaseMixin.customOcclusion}, injected at HEAD of {@code canOcclude}, asks every block
 * state's holder {@code is(create:copycat_base)} inside a catch of IllegalStateException. The light engine and Distant
 * Horizons call canOcclude for nearly every block, and for unkeyed holders the string concat of the block and the whole
 * registry was about 60% of Copycats' worldgen samples. Copycats reads the exception as false, so its result is the
 * same; any other caller gets false where it would have thrown. The first few unkeyed values are logged once.
 */
@Mixin(value = Holder.Reference.class, remap = false)
public abstract class HolderReferenceUnboundMixin<T> {

    private static final Logger creativeengineering$LOGGER = LogUtils.getLogger();
    private static final int LOG_LIMIT = 20;
    private static final Set<String> creativeengineering$logged = ConcurrentHashMap.newKeySet();

    @Shadow(remap = false) private ResourceKey<T> f_205751_;
    @Shadow(remap = false) private T f_205752_;

    @Inject(method = "m_203373_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$unboundIsFalse(ResourceLocation location, CallbackInfoReturnable<Boolean> cir) {
        if (f_205751_ != null) return;
        cir.setReturnValue(false);
        if (creativeengineering$logged.size() < LOG_LIMIT) {
            String name = f_205752_ == null ? "null" : f_205752_.getClass().getName();
            if (creativeengineering$logged.add(name)) {
                creativeengineering$LOGGER.info("Holder with no registry key asked is({}): value class {}", location, name);
            }
        }
    }
}
