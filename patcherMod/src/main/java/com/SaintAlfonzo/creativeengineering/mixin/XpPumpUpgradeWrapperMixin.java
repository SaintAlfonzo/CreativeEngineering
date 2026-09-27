package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.fluid.FluidBridge;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.material.Fluid;
import net.p3pp3rf1y.sophisticatedcore.upgrades.xppump.XpPumpUpgradeWrapper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * The XP Pump upgrade DRAINS via the #forge:experience tag (handled by the
 * KubeJS tag edit) but FILLS with a hard-coded ModFluids.XP_STILL. Swap the
 * fill fluid to Create: Enchantment Industry's experience so backpacks store
 * the unified fluid. Amount math is already corrected by XpHelperMixin.
 *
 * tryFillTankWithPlayerExperience(Player, IStorageFluidHandler, int, boolean)
 * makes exactly one IStorageFluidHandler.fill(TagKey, int, Fluid, FluidAction,
 * boolean) call; the Fluid is arg index 2.
 */
@Mixin(value = XpPumpUpgradeWrapper.class, remap = false)
public class XpPumpUpgradeWrapperMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @ModifyArg(
            method = "tryFillTankWithPlayerExperience(Lnet/minecraft/world/entity/player/Player;Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;IZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/p3pp3rf1y/sophisticatedcore/api/IStorageFluidHandler;fill(Lnet/minecraft/tags/TagKey;ILnet/minecraft/world/level/material/Fluid;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;Z)I"
            ),
            index = 2
    )
    private Fluid creativeengineering$fillWithCreateXp(Fluid original) {
        Fluid cei = FluidBridge.getTargetFluidNow();
        if (cei != null) {
            return cei;
        }
        LOGGER.warn("[XPFluidBridge] SB XP pump: CEI experience missing, filling with {}", original);
        return original;
    }
}
