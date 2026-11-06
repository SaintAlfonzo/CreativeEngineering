package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.campfire.CampfireComfort;
import com.SaintAlfonzo.creativedelight.campfire.CampfireRain;
import com.SaintAlfonzo.creativecommon.campfire.CampfireRainTicks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs {@link CampfireRain} and {@link CampfireComfort} after every lit campfire's server tick ({@code cookTick}, {@code m_155306_}).
 * Unlit Campfire injects at the same RETURN; with its lit time at 0 it does nothing there, so order doesn't
 * matter. SRG name with {@code remap = false}, as in {@code AttachedStemExtraFruitMixin}.
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireRainMixin implements CampfireRainTicks {

    @Unique
    private int ce$rainTicks;

    @Override
    public int ce$getRainTicks() {
        return ce$rainTicks;
    }

    @Override
    public void ce$setRainTicks(int ticks) {
        ce$rainTicks = ticks;
    }

    @Inject(method = "m_155306_", at = @At("RETURN"), remap = false)
    private static void creativeengineering$rainDouses(Level level, BlockPos pos, BlockState state,
                                                     CampfireBlockEntity campfire, CallbackInfo ci) {
        CampfireRain.tick(level, pos, state, campfire);
        CampfireComfort.tick(level, pos);
    }
}
