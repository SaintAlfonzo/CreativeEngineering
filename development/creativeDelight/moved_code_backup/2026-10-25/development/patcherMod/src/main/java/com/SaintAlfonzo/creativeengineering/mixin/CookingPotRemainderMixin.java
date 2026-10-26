package com.SaintAlfonzo.creativeengineering.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderOutputHandler;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

/**
 * Farmer's Delight cooking pot (1.19.2-1.2.4): ingredient leftovers (bottles, bowls, buckets) go to the container
 * slot or a {@link RemainderBuffer} instead of being thrown out of the pot's left side
 * (feature_writeup/remainder_buffer.md).
 *
 *  - {@code ejectIngredientRemainder}: FD's single throw point. Stores the leftover; only what doesn't fit is thrown.
 *  - {@code useStoredContainersOnMeal}: held for {@code [cooking] recycleShowTicks} after a recycle, so the
 *    container is seen in its slot ({@link RemainderBuffer#holdPlating}).
 *  - {@code canCook}: with {@code [cooking] stallWhenFull}, a craft whose leftovers wouldn't fit doesn't start (the pot
 *    waits as it does with a full meal slot).
 *  - {@code getDroppableInventory}: the buffer drops with the pot.
 *  - {@code getCapability}: the side/bottom handler also extracts from the buffer, after FD's output slot. It is
 *    invalidated together with FD's own output handler.
 * Saving is {@code cooking.RemainderBufferStorage}; the GUI slots are CookingPotMenuRemainderMixin.
 */
@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotRemainderMixin implements RemainderBufferHolder {

    private static final int INGREDIENT_SLOTS = 6;

    @Shadow
    @Final
    private ItemStackHandler inventory;

    @Shadow
    @Final
    private LazyOptional<IItemHandler> outputHandler;

    @Shadow
    public abstract boolean isContainerValid(ItemStack containerItem);

    @Shadow
    public abstract ItemStack getMeal();

    @Unique
    private RemainderBuffer ce$remainders;

    @Unique
    private LazyOptional<IItemHandler> ce$sideHandler;

    @Override
    public RemainderBuffer ce$getRemainderBuffer() {
        if (ce$remainders == null) {
            ce$remainders = new RemainderBuffer(() -> ((BlockEntity) (Object) this).setChanged());
        }
        return ce$remainders;
    }

    @Inject(method = "ejectIngredientRemainder", at = @At("HEAD"), cancellable = true)
    private void ce$keepRemainder(ItemStack remainder, CallbackInfo ci) {
        if (!RemainderBuffer.enabled()) return;
        ItemStack rest = ce$getRemainderBuffer().store(remainder, inventory, CookingPotBlockEntity.CONTAINER_SLOT,
                stack -> isContainerValid(stack), getMeal().getCount());
        if (rest.isEmpty()) {
            ci.cancel();
        } else {
            // A fresh stack (FD's getCraftingRemainingItem / getDefaultInstance): FD throws what's left of it.
            remainder.setCount(rest.getCount());
        }
    }

    @Inject(method = "useStoredContainersOnMeal", at = @At("HEAD"), cancellable = true)
    private void ce$showRecycledContainer(CallbackInfo ci) {
        if (ce$getRemainderBuffer().holdPlating()) {
            ci.cancel();
        }
    }

    @Inject(method = "canCook", at = @At("RETURN"), cancellable = true)
    private void ce$roomForRemainders(CookingPotRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !RemainderBuffer.stalls()) return;
        boolean fits = ce$getRemainderBuffer().fits(
                RemainderBuffer.leftovers(inventory, 0, INGREDIENT_SLOTS, CookingPotBlockEntity.INGREDIENT_REMAINDER_OVERRIDES),
                inventory, CookingPotBlockEntity.CONTAINER_SLOT,
                RemainderBuffer.servedIn(recipe.getOutputContainer(), recipe.getResultItem()),
                getMeal().getCount() + recipe.getResultItem().getCount());
        if (!fits) {
            cir.setReturnValue(false);
            BlockEntity self = (BlockEntity) (Object) this;
            if (self.getLevel() != null) ce$getRemainderBuffer().markStalled(self.getLevel().getGameTime());
        }
    }

    @Inject(method = "getDroppableInventory", at = @At("RETURN"))
    private void ce$dropRemainders(CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        RemainderBuffer buffer = ce$getRemainderBuffer();
        for (int i = 0; i < buffer.getSlots(); ++i) {
            cir.getReturnValue().add(buffer.getStackInSlot(i));
        }
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private <T> void ce$sideHandlerWithRemainders(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (cap != ForgeCapabilities.ITEM_HANDLER || side == null || side == Direction.UP || !outputHandler.isPresent()) return;
        if (ce$sideHandler == null || !ce$sideHandler.isPresent()) {
            IItemHandler fdOutput = outputHandler.orElseThrow(IllegalStateException::new);
            RemainderBuffer buffer = ce$getRemainderBuffer();
            LazyOptional<IItemHandler> handler = LazyOptional.of(() -> new RemainderOutputHandler(fdOutput, buffer));
            outputHandler.addListener(invalidated -> handler.invalidate());
            ce$sideHandler = handler;
        }
        cir.setReturnValue(ce$sideHandler.cast());
    }
}
