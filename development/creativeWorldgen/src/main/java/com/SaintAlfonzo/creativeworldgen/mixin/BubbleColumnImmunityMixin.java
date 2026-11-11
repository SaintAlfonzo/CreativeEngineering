package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bubble columns push entities through {@code BubbleColumnBlock.entityInside} (m_7892_), which
 * calls {@code Entity.onAboveBubbleCol} / {@code onInsideBubbleColumn}. Hooking the block instead
 * of the Entity methods also covers mobs that override those methods without calling super. Entity
 * types in {@code #creativeworldgen:bubble_column_immune} (KubeJS
 * {@code kubejs/server_scripts/tags/entitytags.js}) are skipped, so big ocean mobs don't get
 * launched out of the water or dragged to the floor.
 */
@Mixin(value = BubbleColumnBlock.class, remap = false)
public abstract class BubbleColumnImmunityMixin {

    @Unique
    private static final TagKey<EntityType<?>> creativeengineering$IMMUNE =
            TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
                    new ResourceLocation("creativeworldgen", "bubble_column_immune"));

    @Inject(method = "m_7892_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$skipImmune(BlockState state, Level level, BlockPos pos,
                                                Entity entity, CallbackInfo ci) {
        if (entity.getType().is(creativeengineering$IMMUNE)) {
            ci.cancel();
        }
    }
}
