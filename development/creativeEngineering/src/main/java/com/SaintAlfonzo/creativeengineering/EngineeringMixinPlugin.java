package com.SaintAlfonzo.creativeengineering;

import com.SaintAlfonzo.creativeengineering.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativeengineering.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies only when its mod is loaded (each one targets
 * a class of that mod, or serves only that mod). The rest target vanilla and always apply, as they did in patcherMod.
 * The mixins moved from patcherMod on 2026-11-04 (Creative Split wave 8) unchanged: same package, SRG names with
 * remap = false, creativeengineering$ member prefixes.
 */
public class EngineeringMixinPlugin extends GatedMixinPlugin {

    private static final List<String> IE = List.of("immersiveengineering");
    private static final List<String> POSTS = List.of("immersiveposts");
    private static final List<String> PNC = List.of("pneumaticcraft");
    private static final List<String> SOPHISTICATED = List.of("sophisticatedcore");
    private static final List<String> CREATE = List.of("create");
    private static final List<String> DREAMS = List.of("create_dd");
    private static final List<String> ORE_EXCAVATION = List.of("oreexcavation");
    private static final List<String> BLOCKRUNNER = List.of("blockrunner");

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
            // Liquid XP bridge (fluid/FluidBridge)
            Map.entry("MemoryStickItemMixin", PNC),
            Map.entry("PneumaticCraftUtilsMixin", PNC),
            Map.entry("XPFluidManagerMixin", PNC),
            Map.entry("XpHelperMixin", SOPHISTICATED),
            Map.entry("XpPumpUpgradeWrapperMixin", SOPHISTICATED),
            // Exclusive results on Create processing recipes (fan/)
            Map.entry("ProcessingRecipeMixin", CREATE),
            Map.entry("ProcessingRecipeSerializerMixin", CREATE),
            // Dreams and Desires drill and saw
            Map.entry("ExcavationDrillItemMixin", DREAMS),
            Map.entry("DeforesterSawItemMixin", DREAMS),
            // Ore Excavation undo rules (excavation/)
            Map.entry("OreExcavationBlockHistoryMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationCommandUndoMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationHistoryMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationMiningAgentMixin", ORE_EXCAVATION),
            Map.entry("OreExcavationSchedulerMixin", ORE_EXCAVATION),
            // BlockRunner surfaces (surface/)
            Map.entry("BlockRunnerBlockStateMixin", BLOCKRUNNER),
            Map.entry("BlockRunnerSpeedConfigMixin", BLOCKRUNNER),
            // Camouflage order on Framed Blocks and Copycats+ (surface/LastInserted*)
            Map.entry("FramedDoubleCamoOrderMixin", List.of("framedblocks")),
            Map.entry("CopycatMaterialOrderMixin", List.of("copycats")));

    public EngineeringMixinPlugin() {
        super("[creativeengineering]", NEEDS_MOD);
    }
}
