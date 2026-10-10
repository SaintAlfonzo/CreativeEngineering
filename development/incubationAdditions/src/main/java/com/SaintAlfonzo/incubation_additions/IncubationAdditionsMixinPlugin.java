package com.SaintAlfonzo.incubation_additions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Applies a mixin only when the mod it serves is installed, so the jar only needs Incubation:
 * without Alex's Mobs there is no emu to patch, without Untitled Duck Mod no duck or goose, and with
 * neither there are no new nests for LayEggInNestGoalMixin to guard, so nothing is applied at all.
 */
public class IncubationAdditionsMixinPlugin implements IMixinConfigPlugin {

    /** Mixin simple name -> mod ids it serves (applied when any of them is installed). */
    private static final Map<String, List<String>> NEEDS_MOD = Map.of(
            "EntityEmuEggLayerMixin", List.of("alexsmobs"),
            "DuckEntityEggLayerMixin", List.of("untitledduckmod"),
            "GooseEntityEggLayerMixin", List.of("untitledduckmod"),
            "LayEggInNestGoalMixin", List.of("alexsmobs", "untitledduckmod"));

    static boolean isLoaded(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> modIds = NEEDS_MOD.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        return modIds == null || modIds.stream().anyMatch(IncubationAdditionsMixinPlugin::isLoaded);
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
