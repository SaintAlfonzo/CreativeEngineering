package com.SaintAlfonzo.more_mooblooms.mixin;

import com.SaintAlfonzo.more_mooblooms.compat.BuzzierBeesSet;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Takes removed elements (the buttercup item, the buttercup and potted buttercup blocks) out of every tag of
 * their registry after the tags are built ({@code TagLoader.build(Map)}, m_203898_). Forge 1.19.2 tag files have
 * no "remove" list, and stripping after the build also covers tags that pull them in through nested tags
 * (#minecraft:flowers -> #minecraft:small_flowers). Only applied when Buzzier Bees is installed.
 */
@Mixin(value = TagLoader.class, remap = false)
public abstract class TagLoaderStripMixin<T> {

    @Shadow(remap = false)
    @Final
    private String f_13449_; // directory, e.g. "tags/items"

    @Inject(method = "m_203898_", at = @At("RETURN"), remap = false)
    private void more_mooblooms$strip(Map<ResourceLocation, ?> unused, CallbackInfoReturnable<Map<ResourceLocation, Collection<T>>> cir) {
        Set<ResourceLocation> removed = BuzzierBeesSet.tagRemovals(f_13449_);
        if (removed.isEmpty()) return;
        int stripped = 0;
        for (Map.Entry<ResourceLocation, Collection<T>> e : cir.getReturnValue().entrySet()) {
            Collection<T> values = e.getValue();
            if (values.stream().noneMatch(v -> more_mooblooms$isRemoved(v, removed))) continue;
            ImmutableSet.Builder<T> kept = ImmutableSet.builder();
            for (T v : values) {
                if (!more_mooblooms$isRemoved(v, removed)) kept.add(v);
            }
            e.setValue(kept.build());
            stripped++;
        }
        if (stripped > 0) BuzzierBeesSet.logStripped(f_13449_, removed, stripped);
    }

    @Unique
    private static boolean more_mooblooms$isRemoved(Object value, Set<ResourceLocation> removed) {
        return value instanceof Holder<?> holder
                && holder.unwrapKey().map(k -> removed.contains(k.location())).orElse(false);
    }
}
