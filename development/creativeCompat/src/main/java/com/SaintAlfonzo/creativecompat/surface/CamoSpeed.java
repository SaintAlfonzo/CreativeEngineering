package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;

import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

/**
 * Lets framed / copycat blocks walk like the block they're disguised as. BlockRunner gives every
 * block in {@link #TAG} (kubejs blocktags.js: the camouflage tier) one flat speed; with
 * CompatConfig {@code [surface.camouflage] copyCamo} on, this swaps BlockRunner's modifier for the one
 * the camo block's own tier would give. Blocks holding several materials (FramedBlocks double
 * blocks, Copycats+ multi-state copycats) use the most recently inserted one; if that part has
 * since been emptied, whatever is left if it's all one material, otherwise the flat tier.
 *
 * <p>Runs from {@code mixin/BlockRunnerBlockStateMixin} on both sides. Both mods sync their camo
 * to clients, so client movement prediction agrees with the server - as long as both sides use
 * the same {@code copyCamo} value.
 */
public final class CamoSpeed {

    private CamoSpeed() {}

    public static final TagKey<Block> TAG = BlockTags.create(
            new ResourceLocation(CreativeCompat.NS, "camo_speed"));

    private static final boolean FRAMED = ModList.get().isLoaded("framedblocks");
    private static final boolean COPYCATS = ModList.get().isLoaded("copycats");

    public static void afterBlockRunner(LivingEntity entity) {
        if (entity instanceof Player player && player.getAbilities().flying) {
            return; // BlockRunner gives no block speed while flying either
        }
        Level level = entity.level;
        BlockPos pos = entity.getOnPos();
        BlockState camo = speedCamo(level, pos, level.getBlockState(pos));
        if (camo == null) {
            return; // keep the flat tier
        }
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID);
        double factor = BlockSpeedManager.INSTANCE.getSpeedFactor(camo.getBlock());
        if (factor != 1.0) {
            // same modifier BlockRunner itself would add for this factor
            double base = speed.getBaseValue();
            speed.addTransientModifier(new AttributeModifier(BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID,
                    "Block speed boost", factor * base - base, AttributeModifier.Operation.ADDITION));
        }
    }

    /**
     * The block whose speed the frame at {@code pos} walks like, or {@code null} to keep the flat tier:
     * not a frame, {@code copyCamo} off, no camo, or disguised as another frame. Shared with
     * {@link RunSpeed} so the WTHIT line always agrees with movement.
     */
    static BlockState speedCamo(Level level, BlockPos pos, BlockState state) {
        if (!CompatConfig.SURFACE.camouflage.copyCamo.get() || !state.is(TAG)) {
            return null;
        }
        BlockState camo = camo(level.getBlockEntity(pos));
        return camo == null || camo.is(TAG) ? null : camo;
    }

    static BlockState camo(BlockEntity be) {
        if (be == null) {
            return null;
        }
        if (FRAMED) {
            BlockState state = FramedCamo.camo(be);
            if (state != null) {
                return state;
            }
        }
        if (COPYCATS) {
            BlockState state = CopycatsPlusCamo.camo(be);
            if (state != null) {
                return state;
            }
        }
        if (be instanceof CopycatBlockEntity copycat) { // Create's own copycat panel / step
            return copycat.hasCustomMaterial() ? copycat.getMaterial() : null;
        }
        return null;
    }

    /** Replacement for BlockRunner's "Speed Multiplier" tooltip line on camouflage blocks. */
    public static MutableComponent tooltipLine(Block block) {
        if (!CompatConfig.SURFACE.camouflage.copyCamo.get()) {
            return Component.translatable("creativeengineering.tooltip.walk_speed.fixed",
                    BlockSpeedManager.INSTANCE.getSpeedFactor(block));
        }
        boolean multi = (FRAMED && FramedCamo.isMultiMaterial(block)) || (COPYCATS && CopycatsPlusCamo.isMultiMaterial(block));
        return Component.translatable(multi
                ? "creativeengineering.tooltip.walk_speed.newest_camo"
                : "creativeengineering.tooltip.walk_speed.camo");
    }
}
