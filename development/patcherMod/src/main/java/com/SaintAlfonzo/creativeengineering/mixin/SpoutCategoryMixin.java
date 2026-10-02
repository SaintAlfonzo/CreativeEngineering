package com.SaintAlfonzo.creativeengineering.mixin;

import com.github.ichhabehunger54.potionbundles.AbstractPotionBundle;
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create's JEI "Spout" category iterates every potion {@code ItemStack} and emits
 * a "glass bottle + potion fluid -> that item" filling recipe. Potion Bundles are
 * {@code PotionItem} subclasses, so it invents a bogus "spout can fill a potion
 * bundle" recipe — the spout only ever produces single bottles
 * ({@code PotionFluidHandler.fillBottle}), never bundles.
 *
 * Make {@code isPotionItem} report false for bundles inside {@code consumeRecipes}
 * so no filling recipe is generated for them. (Bundles have no fluid-handler
 * capability either, so the fallback branch skips them too.)
 */
@Mixin(value = SpoutCategory.class, remap = false)
public class SpoutCategoryMixin {

    @Redirect(
            method = "consumeRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/potion/PotionFluidHandler;isPotionItem(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean creativeengineering$skipBundleFilling(ItemStack stack) {
        if (stack.getItem() instanceof AbstractPotionBundle) return false;
        return PotionFluidHandler.isPotionItem(stack);
    }
}
