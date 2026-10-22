package com.SaintAlfonzo.creativeengineering.mixin;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.SubContainer;
import net.blay09.mods.cookingforblockheads.tile.OvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cooking for Blockheads oven (13.3.4) fuel (TODO food/kitchen #8). The pack sets {@code ovenRequiresCookingOil =
 * false}, so the oven burns what a furnace burns; this adds:
 *
 *  - {@code getBurnTime}: cooking oil ({@code #balm:cooking_oil}) still burns, for CfB's oil time (800 before
 *    {@code ovenFuelTimeMultiplier}), as no furnace fuel list has it.
 *  - {@code serverTick}'s single {@code shrink(1)} on the fuel: CfB puts the fuel's container (bottle, bucket) back
 *    only when the fuel stack runs out, so a stack of 64 oils gave 1 bottle. Each item burned while more remain now
 *    gives its container to the output slots (merging first), or drops it above the oven when they are full. The
 *    last one still goes back into the fuel slot as before.
 */
@Mixin(value = OvenBlockEntity.class, remap = false)
public abstract class CfbOvenFuelMixin {

    private static final TagKey<Item> CE$COOKING_OIL = ItemTags.create(new ResourceLocation("balm", "cooking_oil"));
    private static final int CE$OIL_BURN_TIME = 800;

    @Shadow
    @Final
    private SubContainer outputContainer;

    @Inject(method = "getBurnTime", at = @At("RETURN"), cancellable = true)
    private static void ce$oilBurns(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() <= 0 && !stack.isEmpty() && stack.is(CE$COOKING_OIL)) {
            cir.setReturnValue(CE$OIL_BURN_TIME);
        }
    }

    @Redirect(method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41774_(I)V"))
    private void ce$shrinkFuel(ItemStack fuel, int amount) {
        ItemStack container = fuel.getCount() > amount ? Balm.getHooks().getCraftingRemainingItem(fuel) : ItemStack.EMPTY;
        fuel.shrink(amount);
        if (!container.isEmpty()) {
            ce$giveBack(container);
        }
    }

    private void ce$giveBack(ItemStack stack) {
        Container out = outputContainer;
        for (int i = 0; i < out.getContainerSize() && !stack.isEmpty(); ++i) {
            ItemStack slot = out.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack)) {
                int moved = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), out.getMaxStackSize()) - slot.getCount());
                if (moved > 0) {
                    slot.grow(moved);
                    stack.shrink(moved);
                }
            }
        }
        for (int i = 0; i < out.getContainerSize() && !stack.isEmpty(); ++i) {
            if (out.getItem(i).isEmpty()) {
                out.setItem(i, stack.copy());
                stack = ItemStack.EMPTY;
            }
        }
        out.setChanged();
        if (!stack.isEmpty()) {
            BlockEntity self = (BlockEntity) (Object) this;
            Level level = self.getLevel();
            BlockPos pos = self.getBlockPos();
            if (level != null) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, stack);
            }
        }
    }
}
