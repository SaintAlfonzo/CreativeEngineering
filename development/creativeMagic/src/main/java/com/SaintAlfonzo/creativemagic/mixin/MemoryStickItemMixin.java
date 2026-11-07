package com.SaintAlfonzo.creativemagic.mixin;

import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.item.MemoryStickItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.SaintAlfonzo.creativemagic.fluid.FluidBridge;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;


/**
 * Targets confirmed via javap against the compiled MemoryStickItem.class:
 *
 *   lambda$use$0               - fill/drain interaction (2x "new FluidStack",
 *                                  1x XPFluidManager.getXPRatio lookup)
 *   lambda$appendHoverText$2   - tooltip XP-stored ratio lookup
 *   lambda$initCapabilities$4  - the tank's accepted-fluid predicate
 *
 * lambda$use$1 does NOT reference ModFluids.MEMORY_ESSENCE (it's just the
 * empty/full click sound logic) and is intentionally NOT targeted here.
 *
 * WARNING: these lambda names are compiler-assigned based on lambda
 * order-of-appearance in PC:R's source. If a future PC:R update adds,
 * removes, or reorders a lambda earlier in use()/appendHoverText(), these
 * names can silently shift (e.g. lambda$use$0 -> lambda$use$1) and this
 * Mixin will stop matching with NO error - Mixin just won't find the
 * instruction pattern and skip it quietly. Re-run javap against the new
 * MemoryStickItem.class after any PneumaticCraft update before trusting
 * this still applies. The startup/debug logging below is there specifically
 * so a silent match failure is at least visible in the log (no "swap fired"
 * messages appearing when a player uses a Memory Stick is your signal that
 * something has drifted).
 */
@Mixin(value = MemoryStickItem.class, remap = false)
public abstract class MemoryStickItemMixin {

		private static final Logger LOGGER = LogUtils.getLogger();

    @Redirect(
            method = "lambda$use$0",
            at = @At(
                    value = "FIELD",
                    target = "Lme/desht/pneumaticcraft/common/core/ModFluids;MEMORY_ESSENCE:Lnet/minecraftforge/registries/RegistryObject;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            ),
      			remap = false
    )
    private static RegistryObject<Fluid> xpfluidbridge$swapInUseHandler() {
        RegistryObject<Fluid> swapped = FluidBridge.resolveOrFallback(ModFluids.MEMORY_ESSENCE);
        LOGGER.debug("[XPFluidBridge] lambda$use$0 (fill/drain) fluid lookup redirected -> {}", swapped.getId());
        return swapped;
    }

    @Redirect(
            method = "lambda$appendHoverText$2",
            at = @At(
                    value = "FIELD",
                    target = "Lme/desht/pneumaticcraft/common/core/ModFluids;MEMORY_ESSENCE:Lnet/minecraftforge/registries/RegistryObject;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            ),
      			remap = false
    )
    private static RegistryObject<Fluid> xpfluidbridge$swapInTooltip() {
        RegistryObject<Fluid> swapped = FluidBridge.resolveOrFallback(ModFluids.MEMORY_ESSENCE);
        LOGGER.debug("[XPFluidBridge] lambda$appendHoverText$2 (tooltip) fluid lookup redirected -> {}", swapped.getId());
        return swapped;
    }

    @Redirect(
            method = "lambda$initCapabilities$4",
            at = @At(
                    value = "FIELD",
                    target = "Lme/desht/pneumaticcraft/common/core/ModFluids;MEMORY_ESSENCE:Lnet/minecraftforge/registries/RegistryObject;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            ),
      			remap = false
    )
    private static RegistryObject<Fluid> xpfluidbridge$swapInTankPredicate() {
        RegistryObject<Fluid> swapped = FluidBridge.resolveOrFallback(ModFluids.MEMORY_ESSENCE);
        // This one fires once per capability check (fairly often - opening
        // inventory, JEI hover, etc.) so it's logged at TRACE rather than
        // DEBUG to avoid flooding the log; bump your logger config to TRACE
        // if you specifically need to confirm this path is firing.
        LOGGER.trace("[XPFluidBridge] lambda$initCapabilities$4 (tank accept-predicate) fluid redirected -> {}", swapped.getId());
        return swapped;
    }

        /** 
     * initCapabilities builds the fluid tank with a hard-coded 512000 mB. At PC:R's
     * Memory Essence ratio (20 mB/XP) that was ~25600 XP. The stick now holds Create
     * experience at 1 mB/XP (see XPFluidManagerMixin), so scale the tank down 20x to
     * keep the effective XP capacity the same.
     */
    @ModifyConstant(method = "initCapabilities", constant = @Constant(intValue = 512000))
    private static int creativeengineering$scaleXpCapacity(int original) {
        return 25600;
    }
}
