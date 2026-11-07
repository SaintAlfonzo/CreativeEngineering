package com.SaintAlfonzo.creativemagic.mixin;

import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.SaintAlfonzo.creativemagic.fluid.FluidBridge;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Targets PneumaticCraftUtils.fillTankWithOrb - the method that runs when a
 * Memory Stick set to "absorb XP orbs" picks up XP in the world.
 *
 * Unlike MemoryStickItemMixin's targets, this method name comes directly
 * from PC:R's real source (not a compiler-generated lambda name), so it's
 * NOT subject to the lambda-renumbering fragility noted there - this target
 * should stay stable across PC:R updates unless the method itself is
 * renamed or removed.
 *
 * Two GETSTATIC reads of ModFluids.MEMORY_ESSENCE live in this method body:
 * one feeding the ratio lookup, one inside "new FluidStack(...)". A single
 * field redirect covers both in one shot.
 */
@Mixin(value = PneumaticCraftUtils.class, remap = false)
public abstract class PneumaticCraftUtilsMixin {

	private static final Logger LOGGER = LogUtils.getLogger();

    @Redirect(
            method = "fillTankWithOrb",
            at = @At(
                    value = "FIELD",
                    target = "Lme/desht/pneumaticcraft/common/core/ModFluids;MEMORY_ESSENCE:Lnet/minecraftforge/registries/RegistryObject;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            ),
      			remap = false
    )
    private static RegistryObject<Fluid> xpfluidbridge$swapInOrbAbsorb() {
        RegistryObject<Fluid> swapped = FluidBridge.resolveOrFallback(ModFluids.MEMORY_ESSENCE);
        LOGGER.debug("[XPFluidBridge] fillTankWithOrb (auto-absorb) fluid lookup redirected -> {}", swapped.getId());
        return swapped;
    }
}
