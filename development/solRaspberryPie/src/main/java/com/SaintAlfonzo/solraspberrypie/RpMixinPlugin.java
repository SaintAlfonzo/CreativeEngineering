package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/** FleetFootedMixin applies only with Alex's Mobs; every other mixin always applies. */
public class RpMixinPlugin extends GatedMixinPlugin {
    public RpMixinPlugin() {
        super("[solraspberrypie]", Map.of("FleetFootedMixin", List.of("alexsmobs")));
    }
}
