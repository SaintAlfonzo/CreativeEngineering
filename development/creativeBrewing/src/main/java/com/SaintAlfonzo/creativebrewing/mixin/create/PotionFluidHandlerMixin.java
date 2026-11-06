package com.SaintAlfonzo.creativebrewing.mixin.create;

import com.github.ichhabehunger54.potionbundles.AbstractPotionBundle;
import com.github.ichhabehunger54.potionbundles.LingeringPotionBundle;
import com.github.ichhabehunger54.potionbundles.PotionBundleUtils;
import com.github.ichhabehunger54.potionbundles.SplashPotionBundle;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Potion Bundles extend vanilla PotionItem, so Create's emptying fast-path
 * ({@code GenericItemEmptying.emptyItem} -> {@code PotionFluidHandler.emptyPotion})
 * drains a 3-charge bundle as a single 250 mB bottle and discards the other two
 * charges. And splash / lingering bundles don't extend
 * SplashPotionItem/LingeringPotionItem, so {@code bottleTypeFromItem} reads them
 * as REGULAR — you'd get plain potion fluid out of a splash bundle.
 *
 * Fix both: scale the emptying result by the bundle's {@code Uses} NBT and stamp
 * the correct Bottle type. An Item Drain / hose pulley then yields the full
 * 750 mB of regular / splash / lingering potion fluid plus N glass bottles, and
 * consumes the bundle.
 */
@Mixin(value = PotionFluidHandler.class, remap = false)
public class PotionFluidHandlerMixin {

    @Inject(method = "emptyPotion", at = @At("HEAD"), cancellable = true, require = 1)
    private static void creativeengineering$emptyWholeBundle(
            ItemStack stack, boolean simulate,
            CallbackInfoReturnable<Pair<FluidStack, ItemStack>> cir) {

        Item item = stack.getItem();
        if (!(item instanceof AbstractPotionBundle)) return;

        int uses = PotionBundleUtils.getUses(stack);
        if (uses < 1) uses = 3;   // AbstractPotionBundle.getMaxUses() is hardcoded 3

        // Base fluid: correct potion + custom effects, 250 mB, Bottle = REGULAR
        // (or plain water for a water potion).
        FluidStack fluid = PotionFluidHandler.getFluidFromPotionItem(stack);
        fluid.setAmount(fluid.getAmount() * uses);

        // Bundles are never Splash/LingeringPotionItem, so correct the Bottle type.
        // Only when it's actually potion fluid (hasTag) — the water branch has none.
        PotionFluid.BottleType type =
                  item instanceof LingeringPotionBundle ? PotionFluid.BottleType.LINGERING
                : item instanceof SplashPotionBundle    ? PotionFluid.BottleType.SPLASH
                : null;
        if (type != null && fluid.hasTag()) {
            NBTHelper.writeEnum(fluid.getOrCreateTag(), "Bottle", type);
        }

        if (!simulate) stack.shrink(1);
        cir.setReturnValue(Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE, uses)));
    }
}
