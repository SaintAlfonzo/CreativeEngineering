package com.SaintAlfonzo.creativelabels.mixin;

import com.SaintAlfonzo.creativelabels.crafting.LabelledCrafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code blockLabelledCrafting} under FastWorkbench (found on the scratch server 2026-10-21): FastWorkbench 7.1.4
 * overwrites {@code slotsChanged} of both the inventory menu and the crafting table menu to queue its own
 * {@code FastBenchUtil.slotChangedCraftingGrid}, so vanilla's {@code CraftingMenu.slotChangedCraftingGrid} (where
 * {@link CraftingMenuResultMixin} sits) never runs and labelled items crafted freely. Same check at the head of
 * FastWorkbench's copy. Its grid parameter is FastWorkbench's {@code CraftingInventoryExt}, taken here as its
 * vanilla supertype (@Coerce; no compile dependency).
 * <p>
 * Pseudo and by name, gated on {@code fastbench} by {@code CLMixinPlugin}. FastWorkbench's own method is not
 * obfuscated, so nothing is remapped.
 */
@Pseudo
@Mixin(targets = "shadows.fastbench.util.FastBenchUtil", remap = false)
public class FastBenchResultMixin {

    @Inject(method = "slotChangedCraftingGrid(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lshadows/fastbench/util/CraftingInventoryExt;Lnet/minecraft/world/inventory/ResultContainer;)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativelabels$blockLabelled(Level level, Player player, @Coerce CraftingContainer grid,
                                                      ResultContainer result, CallbackInfo ci) {
        if (!LabelledCrafting.blocks(level, grid)) return;
        LabelledCrafting.clear(null, player, result);
        ci.cancel();
    }
}
