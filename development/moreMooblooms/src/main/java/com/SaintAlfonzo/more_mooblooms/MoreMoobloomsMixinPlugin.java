package com.SaintAlfonzo.more_mooblooms;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies a content set's mixins only when its mod is installed. Core mixins (NaturalSpawnerMixin) always apply.
 * The Buzzier Bees mixins target vanilla classes only (Item, TagLoader, Mob), never Buzzier Bees itself.
 */
public class MoreMoobloomsMixinPlugin implements IMixinConfigPlugin {

    /** Mixin simple name -> the mod it serves. */
    private static final Map<String, String> NEEDS_MOD = Map.of(
            "ItemHiddenMixin", "buzzier_bees",
            "TagLoaderStripMixin", "buzzier_bees",
            "MobSunBurnMixin", "buzzier_bees");

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
