package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativeengineering.shield.TierShieldItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;

/**
 * Lets every tier shield take a banner in the crafting grid, like the vanilla (iron-tier) shield.
 * Vanilla's {@code ShieldDecorationRecipe} ({@code minecraft:crafting_special_shielddecoration})
 * identifies the shield with {@code stack.is(Items.SHIELD)} - once in {@code matches} ({@code m_5818_})
 * and once in {@code assemble} ({@code m_5874_}), bytecode-checked on the 1.19.2 client jar. Both are
 * widened to {@link TierShieldItem}; the rest of the recipe (copy the shield, write the banner into
 * {@code BlockEntityTag}, refuse an already-decorated shield) is untouched, and the tier renderer
 * already draws banner patterns.
 *
 * <p>Vanilla-class target, SRG names with {@code remap = false}, full descriptors so the synthetic
 * {@code Container} bridge methods aren't matched - same idiom as {@link LivingEntityShieldMixin}.
 */
@Mixin(ShieldDecorationRecipe.class)
public abstract class ShieldDecorationRecipeMixin {

    @Redirect(method = {
                    "m_5818_(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
                    "m_5874_(Lnet/minecraft/world/inventory/CraftingContainer;)Lnet/minecraft/world/item/ItemStack;"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"),
            remap = false)
    private boolean creativeengineering$anyTierShield(ItemStack stack, Item item) {
        return stack.is(item) || (item == Items.SHIELD && stack.getItem() instanceof TierShieldItem);
    }
}
