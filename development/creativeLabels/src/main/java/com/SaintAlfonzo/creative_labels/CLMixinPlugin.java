package com.SaintAlfonzo.creative_labels;

import com.SaintAlfonzo.creative_labels.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Gates mixins on the mod they target being installed: {@code StickyItTooltipMixin} needs {@code sticky_it}
 * (phase 4), {@code FastBenchResultMixin} needs {@code fastbench}. Everything else (vanilla targets) always applies.
 */
public class CLMixinPlugin extends GatedMixinPlugin {

    public CLMixinPlugin() {
        super("[creative_labels]", Map.of(
                "StickyItTooltipMixin", List.of("sticky_it"),
                "FastBenchResultMixin", List.of("fastbench")));
    }
}
