package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.qol.RemainderBufferQol;
import com.SaintAlfonzo.creativedelight.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativedelight.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativedelight.cooking.RemainderOutputHandler;
import com.SaintAlfonzo.creativedelight.cooking.StationSlotRule;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleContainers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import vectorwing.farmersdelight.common.utility.ItemUtils;

/**
 * Farmer's Respite kettle (1.19-2.0): the same as CookingPotRemainderMixin for the pot. The kettle has no throw
 * method of its own: {@code processBrewing} calls FD's {@code ItemUtils.spawnItemEntity} inline for each ingredient
 * (slots 0-1) with a crafting remainder, so that call is redirected. A bottle leftover goes to the container slot when
 * the drink is served in bottles (spares wait there for the next drinks), else to the buffer ({@code LeftoverPlan}).
 * Plating waits {@code [cooking] recycleShowTicks} after a recycle ({@code useStoredContainersOnMeal} HEAD), as on the
 * pot. {@code <init>}: containers ({@link KettleContainers}) only in the container slot, never in an ingredient slot.
 * Creative Delight QoL-1 compartment (moved from patcherMod 2026-10-25): applies only with this mod loaded
 * (compat.CDMixinPlugin); new leftovers are stored only while {@code [qol] remainderBuffer} is on (qol.RemainderBufferQol).
 */
@Mixin(value = KettleBlockEntity.class, remap = false)
public abstract class KettleRemainderMixin implements RemainderBufferHolder {

    private static final int INGREDIENT_SLOTS = 2;

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

    @Shadow
    private boolean doesMealHaveContainer(ItemStack meal) {
        throw new AssertionError();
    }

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

    /** Containers only in the container slot, never in an ingredient slot (remainder_buffer.md 3.8). */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ce$slotRule(BlockPos pos, BlockState state, CallbackInfo ci) {
        StationSlotRule.apply(inventory, (slot, stack) -> {
            if (slot < INGREDIENT_SLOTS) return !ce$isContainer(stack);
            if (slot == KettleBlockEntity.CONTAINER_SLOT) return ce$isContainer(stack) || isContainerValid(stack);
            return true;
        });
    }

    @Unique
    private boolean ce$isContainer(ItemStack stack) {
        return KettleContainers.KETTLE.isContainer(((BlockEntity) (Object) this).getLevel(), stack);
    }

    @Redirect(method = "processBrewing", at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;spawnItemEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;DDDDDD)V"))
    private void ce$keepRemainder(Level level, ItemStack remainder, double x, double y, double z, double xMotion, double yMotion, double zMotion) {
        ItemStack rest = RemainderBufferQol.enabled()
                ? ce$getRemainderBuffer().store(remainder, inventory, KettleBlockEntity.CONTAINER_SLOT,
                stack -> isContainerValid(stack))
                : remainder;
        if (!rest.isEmpty()) {
            ItemUtils.spawnItemEntity(level, rest, x, y, z, xMotion, yMotion, zMotion);
        }
    }

    /** A leftover in the buffer that plates the waiting meal moves to the container slot (remainder_buffer.md 3.8). */
    @Inject(method = "brewingTick", at = @At("HEAD"))
    private static void ce$pullTick(Level level, BlockPos pos, BlockState state, KettleBlockEntity station, CallbackInfo ci) {
        ((KettleRemainderMixin) (Object) station).ce$pullForPlating();
    }

    @Unique
    private void ce$pullForPlating() {
        ItemStack meal = getMeal();
        int mealsWaiting = !meal.isEmpty() && doesMealHaveContainer(meal) ? meal.getCount() : 0;
        ce$getRemainderBuffer().pullForPlating(inventory, KettleBlockEntity.CONTAINER_SLOT, mealsWaiting, stack -> isContainerValid(stack));
    }

    @Inject(method = "useStoredContainersOnMeal", at = @At("HEAD"), cancellable = true)
    private void ce$showRecycledContainer(CallbackInfo ci) {
        if (ce$getRemainderBuffer().holdPlating()) {
            ci.cancel();
        }
    }

    @Inject(method = "canBrew", at = @At("RETURN"), cancellable = true)
    private void ce$roomForRemainders(KettleRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !RemainderBufferQol.stalls()) return;
        boolean fits = ce$getRemainderBuffer().fits(
                RemainderBuffer.leftovers(inventory, 0, INGREDIENT_SLOTS, null),
                inventory, KettleBlockEntity.CONTAINER_SLOT,
                RemainderBuffer.servedIn(recipe.getOutputContainer(), recipe.getResultItem()));
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
            IItemHandler respiteOutput = outputHandler.orElseThrow(IllegalStateException::new);
            RemainderBuffer buffer = ce$getRemainderBuffer();
            LazyOptional<IItemHandler> handler = LazyOptional.of(() -> new RemainderOutputHandler(respiteOutput, buffer));
            outputHandler.addListener(invalidated -> handler.invalidate());
            ce$sideHandler = handler;
        }
        cir.setReturnValue(ce$sideHandler.cast());
    }
}
