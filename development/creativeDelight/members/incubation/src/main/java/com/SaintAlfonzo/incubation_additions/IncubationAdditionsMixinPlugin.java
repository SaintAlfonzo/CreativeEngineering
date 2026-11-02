package com.SaintAlfonzo.incubation_additions;

import com.SaintAlfonzo.incubation_additions.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Applies a mixin only when the mod it serves is installed, so the jar only needs Incubation:
 * without Alex's Mobs there is no emu to patch, without Untitled Duck Mod no duck or goose, and with
 * neither there are no new nests for LayEggInNestGoalMixin to guard, so nothing is applied at all.
 */
public class IncubationAdditionsMixinPlugin extends GatedMixinPlugin {

    public IncubationAdditionsMixinPlugin() {
        super("[incubation_additions]", Map.of(
                "EntityEmuEggLayerMixin", List.of("alexsmobs"),
                "DuckEntityEggLayerMixin", List.of("untitledduckmod"),
                "GooseEntityEggLayerMixin", List.of("untitledduckmod"),
                "LayEggInNestGoalMixin", List.of("alexsmobs", "untitledduckmod")));
    }
}
