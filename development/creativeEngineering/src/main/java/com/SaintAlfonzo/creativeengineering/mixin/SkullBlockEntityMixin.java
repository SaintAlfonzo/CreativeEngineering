package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.Optional;

import com.SaintAlfonzo.creativeengineering.fan.CatalystUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Drives the vanilla dragon-head mouth animation from an Encased Fan as well as from a
 * redstone signal, so a dragon head used as the {@code dragon_breathing} fan catalyst
 * visibly chatters while it works. Ported from the idea behind Create: Halitosis'
 * {@code SkullBlockEntityMixin}.
 *
 * <p>MC 1.19.2's {@code SkullBlockEntity.dragonHeadAnimation} ({@code m_155733_}) ticker
 * only runs on the client and only for the two dragon-head blocks
 * ({@code AbstractSkullBlock.getTicker}), so this mixin lives in the {@code client} config
 * and never touches the server. It redirects that method's
 * {@code Level.hasNeighborSignal(pos)} ({@code m_46753_}) check: vanilla then sets
 * {@code isMovingMouth} / {@code mouthTickCount} on its own, so no accessor is needed.
 *
 * <p>The build ships no Mixin refmap and the pack only runs reobfuscated, so targets are
 * SRG names with {@code remap = false}; {@code build.gradle} disables the AP target
 * validator so those names are accepted against the dev-time (Mojmap) classes.
 */
@Mixin(SkullBlockEntity.class)
public abstract class SkullBlockEntityMixin {

    @Redirect(
            method = "m_155733_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_46753_(Lnet/minecraft/core/BlockPos;)Z"
            ),
            remap = false
    )
    private static boolean creativeengineering$fanDrivesMouth(Level level, BlockPos pos) {
        if (level.hasNeighborSignal(pos)) {
            return true;
        }
        BlockState state = level.getBlockState(pos);
        Optional<Direction> facing = CatalystUtils.getDirection(state);
        return facing.isPresent() && CatalystUtils.testFan(level, pos, facing.get());
    }
}
