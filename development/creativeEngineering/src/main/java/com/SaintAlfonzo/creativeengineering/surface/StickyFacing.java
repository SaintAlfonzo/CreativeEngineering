package com.SaintAlfonzo.creativeengineering.surface;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Makes BlockRunner's sticky tier blockstate-aware. BlockRunner looks speeds up per
 * {@link Block}, so every sticky piston would slow you however it faces. Blocks in
 * {@link #FACE_UP} (filled in kubejs blocktags.js) only keep BlockRunner's speed modifier while
 * their sticky face is on top: a {@code facing} property must be UP, and a piston-head
 * {@code type} property must be STICKY (vanilla and Create heads share one block for both types).
 * Blocks without those properties (Create Things & Misc launchers) always count.
 *
 * <p>Called by {@code mixin/BlockRunnerBlockStateMixin} right after BlockRunner (re)applies its
 * modifier, on both sides, so client movement prediction agrees with the server. Off when CEConfig
 * {@code [surface.sticky] faceUpOnly} is false.
 */
public final class StickyFacing {

    private StickyFacing() {}

    public static final TagKey<Block> FACE_UP = BlockTags.create(
            new ResourceLocation(CreativeEngineering.MODID, "blockrunner_face_up"));

    public static void afterBlockRunner(LivingEntity entity) {
        if (!cancels(entity.getBlockStateOn())) {
            return;
        }
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID);
        }
    }

    /** True when {@code state} is a sticky-tier block whose sticky face isn't up, so it gives no slowdown. */
    static boolean cancels(BlockState state) {
        return CEConfig.SURFACE.sticky.faceUpOnly.get() && state.is(FACE_UP) && !stickyFaceUp(state);
    }

    static boolean stickyFaceUp(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            Object value = state.getValue(property);
            if (property.getName().equals("facing") && value instanceof Direction facing && facing != Direction.UP) {
                return false;
            }
            if (property.getName().equals("type") && value instanceof PistonType type && type != PistonType.STICKY) {
                return false;
            }
        }
        return true;
    }
}
