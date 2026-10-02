package com.SaintAlfonzo.creativeengineering.mixin;

import com.github.ichhabehunger54.potionbundles.AbstractPotionBundle;
import com.github.ichhabehunger54.potionbundles.LingeringPotionBundle;
import com.github.ichhabehunger54.potionbundles.PotionBundleUtils;
import com.github.ichhabehunger54.potionbundles.SplashPotionBundle;
import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create's JEI "Item Drain" category builds its potion-emptying entries by calling
 * {@code PotionFluidHandler.getFluidFromPotionItem(stack)} directly rather than
 * {@code emptyPotion}, so {@link PotionFluidHandlerMixin}'s in-world fixes never
 * reach the JEI tooltip. Apply the same transform here for potion bundles:
 * scale the amount by the {@code Uses} NBT and stamp the correct Bottle type
 * (splash/lingering bundles aren't Splash/LingeringPotionItem, so Create reads
 * them as REGULAR).
 *
 * (Glass-bottle count in that JEI entry still renders as 1 — a purely cosmetic
 * Create limitation; the fluid is what matters.)
 */
@Mixin(value = ItemDrainCategory.class, remap = false)
public class ItemDrainCategoryMixin {

    @Redirect(
            method = "consumeRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/potion/PotionFluidHandler;getFluidFromPotionItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraftforge/fluids/FluidStack;"
            )
    )
    private static FluidStack creativeengineering$bundleDrainEntry(ItemStack stack) {
        FluidStack fluid = PotionFluidHandler.getFluidFromPotionItem(stack);
        Item item = stack.getItem();
        if (item instanceof AbstractPotionBundle) {
            int uses = PotionBundleUtils.getUses(stack);
            fluid.setAmount(fluid.getAmount() * (uses < 1 ? 3 : uses));

            PotionFluid.BottleType type =
                      item instanceof LingeringPotionBundle ? PotionFluid.BottleType.LINGERING
                    : item instanceof SplashPotionBundle    ? PotionFluid.BottleType.SPLASH
                    : null;
            if (type != null && fluid.hasTag()) {
                NBTHelper.writeEnum(fluid.getOrCreateTag(), "Bottle", type);
            }
        }
        return fluid;
    }
}
