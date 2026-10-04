package com.SaintAlfonzo.creativeengineering.compat;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

/**
 * Replacement for the {@code this.is(BlockTags.LEAVES)} test at the top of Fruits Delight's
 * collision-shape handler. {@link CreativeEngineeringMixinPlugin} rewrites the handler to call this
 * instead. Same result as the original two tests together, with the cheap test first.
 */
public final class FruitsDelightHooks {
    private FruitsDelightHooks() {}

    /** True for a leaf block queried by a projectile; the only case the handler goes on to examine. */
    public static boolean isLeafForProjectile(BlockStateBase state, CollisionContext ctx) {
        return ctx instanceof EntityCollisionContext ectx
                && ectx.getEntity() instanceof Projectile
                && state.is(BlockTags.LEAVES);
    }
}
