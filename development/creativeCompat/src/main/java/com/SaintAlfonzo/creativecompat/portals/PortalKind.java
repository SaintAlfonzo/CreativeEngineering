package com.SaintAlfonzo.creativecompat.portals;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import qouteall.imm_ptl.core.portal.Mirror;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalLike;
import qouteall.imm_ptl.core.portal.nether_portal.NetherPortalEntity;
import qouteall.imm_ptl.core.render.PortalGroup;

/**
 * The portal types the pack sets rendering rules for (feature_writeup/plan_portal_changes.md 2, 4). Touches Immersive
 * Portals classes: only used from the IP mixins, which apply only with {@code imm_ptl_core}. Portal Gun portals are
 * matched by entity type id, so this class never loads Portal Gun code.
 */
public enum PortalKind {
    NETHER, GUN, MIRROR, OTHER;

    private static final ResourceLocation GUN_ID = new ResourceLocation("portalgun", "custom_portal");

    public static PortalKind of(PortalLike like) {
        Portal portal = like instanceof PortalGroup group ? group.getFirstPortal() : like instanceof Portal p ? p : null;
        if (portal == null) return OTHER;
        if (portal instanceof NetherPortalEntity) return NETHER;
        if (portal instanceof Mirror) return MIRROR;
        Entity entity = portal; // vanilla-typed, so getType() reobfuscates
        return GUN_ID.equals(EntityType.getKey(entity.getType())) ? GUN : OTHER;
    }
}
