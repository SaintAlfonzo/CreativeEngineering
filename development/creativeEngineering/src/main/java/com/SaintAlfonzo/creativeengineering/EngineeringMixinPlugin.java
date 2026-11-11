package com.SaintAlfonzo.creativeengineering;

import com.SaintAlfonzo.creativeengineering.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mixin config plugin for creativeengineering.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies only when its mod is loaded (each one targets
 * a class of that mod, or serves only that mod). The rest target vanilla and always apply, as they did in patcherMod.
 * The mixins moved from patcherMod on 2026-11-04 (Creative Split wave 8) unchanged: same package, SRG names with
 * remap = false, creativeengineering$ member prefixes. The XP bridge, Ore Excavation, BlockRunner and camouflage-order
 * mixins moved on to Creative Magic and Creative Compat on 2026-11-06.
 */
public class EngineeringMixinPlugin extends GatedMixinPlugin {

    private static final List<String> IE = List.of("immersiveengineering");
    private static final List<String> POSTS = List.of("immersiveposts");
    private static final List<String> CREATE = List.of("create");
    private static final List<String> DREAMS = List.of("create_dd");
    private static final List<String> JEI = List.of("jei");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            // IE tag utils (arc recycling springs), concrete set time, the sawmill blade routing (sawmill/)
            Map.entry("IETagUtilsMixin", IE),
            Map.entry("IEConcreteSetTimeMixin", IE),
            Map.entry("SawmillRecipeMixin", IE),
            Map.entry("SawmillRecipeSerializerMixin", IE),
            Map.entry("SawmillProcessMixin", IE),
            // Immersive Posts materials (compat/ImmersivePostsMaterials); the TagLoader mixin serves only that mod
            Map.entry("ImmersivePostsRegistrationMixin", POSTS),
            Map.entry("ImmersivePostsValidItemMixin", POSTS),
            Map.entry("TagLoaderImmersivePostsMixin", POSTS),
            // Exclusive results on Create processing recipes (fan/)
            Map.entry("ProcessingRecipeMixin", CREATE),
            Map.entry("ProcessingRecipeSerializerMixin", CREATE),
            // Result pools (fan/ResultPools, plan_flesh_recipes.md 5.3): crushing and milling output cap, crusher inventory
            Map.entry("CrushingOutputCapMixin", CREATE),
            Map.entry("ProcessingInventoryMixin", CREATE),
            // From Creative Common 2026-11-07 (creative_engineering.md 7.1): sequenced assembly keeps gear NBT, Builder's
            // Tea pipe Haste icon, IE fluid inputs by id with subset NBT match, drink fluid effect lines in JEI
            Map.entry("SequencedAssemblyRecipeMixin", CREATE),
            Map.entry("CreateTeaPipeIconMixin", CREATE),
            Map.entry("FluidTagInputMixin", IE),
            Map.entry("JeiFluidHelperTooltipMixin", JEI),
            // Dreams and Desires drill and saw
            Map.entry("ExcavationDrillItemMixin", DREAMS),
            Map.entry("DeforesterSawItemMixin", DREAMS));

    /**
     * Tested version ranges (Maven syntax), from the version the pack runs to the next minor release; outside one, that
     * mod's mixins are skipped with one warning instead of crashing (GatedMixinPlugin). Added 2026-11-07 with the mixins
     * that came from Creative Common, whose ranges they had; the same bounds as Common's and Compat's tables.
     */
    private static final Map<String, String> MOD_RANGE = Map.ofEntries(
            Map.entry("create", "[0.5.1.i,0.5.2)"),
            Map.entry("immersiveengineering", "[1.19.2-9.2.4-170,1.19.2-9.3)"),
            Map.entry("jei", "[11.8.1.1034,11.9)"));

    public EngineeringMixinPlugin() {
        super("[creativeengineering]", NEEDS_MOD, MOD_RANGE);
    }

    /** Mixins on Create's JEI categories: they need Create and JEI both, where the table gate is any of its mods. */
    private static final Set<String> NEEDS_CREATE_AND_JEI = Set.of("JeiCrushingPoolsMixin", "JeiMillingPoolsMixin");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (NEEDS_CREATE_AND_JEI.contains(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1)))
            return modOn("create") && modOn("jei");
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}
