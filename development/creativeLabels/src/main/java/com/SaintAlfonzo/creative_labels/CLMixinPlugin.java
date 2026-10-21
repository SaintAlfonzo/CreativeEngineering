package com.SaintAlfonzo.creative_labels;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gates mixins on the mod they target being installed: {@code StickyItTooltipMixin} needs {@code sticky_it}
 * (phase 4). Everything else (vanilla targets) always applies.
 */
public class CLMixinPlugin implements IMixinConfigPlugin {

    /** Mixin simple name -> the mod it needs. */
    private static final Map<String, String> NEEDS_MOD = Map.of(
            "StickyItTooltipMixin", "sticky_it");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String modId = NEEDS_MOD.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        return modId == null || LoadingModList.get().getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
