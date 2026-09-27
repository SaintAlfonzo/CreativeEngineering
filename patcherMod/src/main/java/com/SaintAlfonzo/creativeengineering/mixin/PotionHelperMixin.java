package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.common.crafting.PotionHelper;
import com.SaintAlfonzo.creativeengineering.fluidbrewing.PotionMixDedup;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * IE's {@code PotionHelper.applyToAllPotionRecipes} feeds every IE-Mixer potion recipe
 * ({@code immersiveengineering:mixer_potion_list}, via {@code PotionRecipeGenerators}).
 * It hard-skips any brewing mix whose OUTPUT potion is {@code MUNDANE} or {@code THICK},
 * so the IE Mixer can never produce {@code create:potion{minecraft:thick}} /
 * {@code {minecraft:mundane}} and any potion brewed from one of those bases
 * (health boost, absorption, hunger, nausea, blindness, darkness, wither, …) is
 * uncraftable on the multiblock.
 *
 * <p>Both skips are {@code mix.output == Potions.MUNDANE} / {@code == Potions.THICK}
 * identity checks — one {@code GETSTATIC} each. Redirect those two field reads to
 * {@code null}: the output potion is never {@code null}, so neither {@code ==} matches,
 * neither branch is taken, and the {@code water -> thick} / {@code water -> mundane}
 * base brews (plus anything derived from them) flow through to
 * {@code registerPotionRecipe} like every other mix.
 *
 * <p>SRG field names + {@code remap = false} (this project ships no Mixin refmap);
 * {@code f_43600_} = {@code MUNDANE}, {@code f_43601_} = {@code THICK}
 * (verified in {@code mappings.tsrg}). Same field-redirect shape as
 * {@code PneumaticCraftUtilsMixin}.
 */
@Mixin(value = PotionHelper.class, remap = false)
public class PotionHelperMixin {

    @Redirect(
            method = "applyToAllPotionRecipes",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/alchemy/Potions;f_43600_:Lnet/minecraft/world/item/alchemy/Potion;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private static Potion creativeengineering$allowMundaneBase() {
        return null;
    }

    @Redirect(
            method = "applyToAllPotionRecipes",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/alchemy/Potions;f_43601_:Lnet/minecraft/world/item/alchemy/Potion;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private static Potion creativeengineering$allowThickBase() {
        return null;
    }

    /**
     * IE's loop 2 walks {@code BrewingRecipeRegistry.getRecipes()}. Drop the Alex's Mobs
     * modifier steps that already exist as {@code PotionBrewing.Mix} entries (re-added in
     * {@code potions.js}) so the IE Mixer doesn't get a duplicate recipe for each. See
     * {@link PotionMixDedup}.
     */
    @Redirect(
            method = "applyToAllPotionRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/brewing/BrewingRecipeRegistry;getRecipes()Ljava/util/List;"
            )
    )
    private static List<IBrewingRecipe> creativeengineering$dedupBrewingRecipes() {
        return PotionMixDedup.withoutMixDuplicates(BrewingRecipeRegistry.getRecipes());
    }
}
