package com.SaintAlfonzo.creativeengineering.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderOutputHandler;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import vectorwing.farmersdelight.common.utility.ItemUtils;

/**
 * Farmer's Respite kettle (1.19-2.0): the same as CookingPotRemainderMixin for the pot. The kettle has no throw
 * method of its own: {@code processBrewing} calls FD's {@code ItemUtils.spawnItemEntity} inline for each ingredient
 * (slots 0-1) with a crafting remainder, so that call is redirected. Two bottles per "strong"/"long" upgrade: one
 * plates the drink through the container slot, one goes to the buffer. Plating waits {@code [cooking]
 * recycleShowTicks} after a recycle ({@code useStoredContainersOnMeal} HEAD), as on the pot.
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

    @Redirect(method = "processBrewing", at = @At(value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;spawnItemEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;DDDDDD)V"))
    private void ce$keepRemainder(Level level, ItemStack remainder, double x, double y, double z, double xMotion, double yMotion, double zMotion) {
        ItemStack rest = RemainderBuffer.enabled()
                ? ce$getRemainderBuffer().store(remainder, inventory, KettleBlockEntity.CONTAINER_SLOT,
                stack -> isContainerValid(stack), getMeal().getCount())
                : remainder;
        if (!rest.isEmpty()) {
            ItemUtils.spawnItemEntity(level, rest, x, y, z, xMotion, yMotion, zMotion);
        }
    }

    @Inject(method = "useStoredContainersOnMeal", at = @At("HEAD"), cancellable = true)
    private void ce$showRecycledContainer(CallbackInfo ci) {
        if (ce$getRemainderBuffer().holdPlating()) {
            ci.cancel();
        }
    }

    @Inject(method = "canBrew", at = @At("RETURN"), cancellable = true)
    private void ce$roomForRemainders(KettleRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !RemainderBuffer.stalls()) return;
        boolean fits = ce$getRemainderBuffer().fits(
                RemainderBuffer.leftovers(inventory, 0, INGREDIENT_SLOTS, null),
                inventory, KettleBlockEntity.CONTAINER_SLOT,
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
            IItemHandler respiteOutput = outputHandler.orElseThrow(IllegalStateException::new);
            RemainderBuffer buffer = ce$getRemainderBuffer();
            LazyOptional<IItemHandler> handler = LazyOptional.of(() -> new RemainderOutputHandler(respiteOutput, buffer));
            outputHandler.addListener(invalidated -> handler.invalidate());
            ce$sideHandler = handler;
        }
        cir.setReturnValue(ce$sideHandler.cast());
    }
}
