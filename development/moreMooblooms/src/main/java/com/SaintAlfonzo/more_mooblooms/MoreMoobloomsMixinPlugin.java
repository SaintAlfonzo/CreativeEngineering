package com.SaintAlfonzo.more_mooblooms;

import com.SaintAlfonzo.more_mooblooms.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Applies a content set's mixins only when its mod is installed. Core mixins (NaturalSpawnerMixin) always apply.
 * The Buzzier Bees mixins target vanilla classes only (Item, TagLoader, Mob), never Buzzier Bees itself.
 */
public class MoreMoobloomsMixinPlugin extends GatedMixinPlugin {

    public MoreMoobloomsMixinPlugin() {
        super("[more_mooblooms]", Map.of(
                "ItemHiddenMixin", List.of("buzzier_bees"),
                "TagLoaderStripMixin", List.of("buzzier_bees"),
                "MobSunBurnMixin", List.of("buzzier_bees")));
    }
}
