package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.fluid.FluidBridge;
import me.desht.pneumaticcraft.common.XPFluidManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The XP-fluid bridge unifies liquid XP onto create_enchantment_industry:experience
 * (see MemoryStickItemMixin, XpHelperMixin). PneumaticCraft's XPFluidManager only
 * learns fluid->XP ratios from the #forge:experience tag at TagsUpdatedEvent, and in
 * this pack that resolves to 0 for the bridge fluid at the point
 * MemoryStickItem.lambda$use$0 divides by it -> ArithmeticException: / by zero
 * (confirmed: the MemoryStickItemMixin redirect fires and hands getXPRatio the CEI
 * fluid, which is still unregistered).
 *
 * Force a correct, non-zero ratio here, independent of tag-binding order:
 *   create_enchantment_industry:experience -> 1 mB / XP point (ExperienceFluid.xpRatio == 1;
 *       matches XpHelperMixin and MemoryStickItemMixin's 25600 mB tank)
 *   pneumaticcraft:memory_essence          -> 20 (PNC's native value; the pack strips it
 *       from #forge:experience so PNC no longer self-registers it) — safety net.
 */
@Mixin(value = XPFluidManager.class, remap = false)
public class XPFluidManagerMixin {

    @Inject(method = "getXPRatio", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$bridgeXpRatio(Fluid fluid, CallbackInfoReturnable<Integer> cir) {
        if (fluid == null) return;
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) return;
        if (id.equals(FluidBridge.LIQUID_XP_ID)) {
            cir.setReturnValue(1);
        } else if (id.getNamespace().equals("pneumaticcraft")
                && id.getPath().equals("memory_essence")) {
            cir.setReturnValue(20);
        }
    }
}
