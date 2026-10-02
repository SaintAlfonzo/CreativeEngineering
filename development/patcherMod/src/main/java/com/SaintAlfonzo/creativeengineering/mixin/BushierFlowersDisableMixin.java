package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.world.level.block.Block;
import net.Pandarix.bushierflowers.block.ModBlocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

/**
 * Turns off Bushier Flowers' bone-meal growth; Flower Patch handles bone meal on flowers instead.
 *
 * <p>Both of the mod's growth paths (its {@code BoneMealItem.useOn} HEAD inject and its bone-meal
 * dispenser behaviour) only act when the target block is in {@code validFlowers} (vanilla flower
 * -> grown bush) or {@code grownFlowers} (grown bush -> pops a loose flower). Emptying both lists
 * makes every check miss, so both paths fall through to vanilla bone meal untouched. The grown
 * blocks stay registered (existing bushes, dye recipes, pots); they are crafted from 4 loose
 * flowers in kubejs/server_scripts/other/flowers.js. Worldgen is disabled separately by the
 * {@code forge:none} biome modifiers in kubejs/data/bushierflowers/forge/biome_modifier/.
 */
@Mixin(value = ModBlocks.class, remap = false)
public abstract class BushierFlowersDisableMixin {

    @Shadow @Final @Mutable public static List<Block> validFlowers;
    @Shadow @Final @Mutable public static Supplier<List<Block>> grownFlowers;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void creativeengineering$disableGrowth(CallbackInfo ci) {
        validFlowers = List.of();
        grownFlowers = List::of;
    }
}
