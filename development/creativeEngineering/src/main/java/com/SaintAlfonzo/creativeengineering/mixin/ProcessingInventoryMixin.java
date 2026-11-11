package com.SaintAlfonzo.creativeengineering.mixin;

import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Grows Create's {@code ProcessingInventory} (crushing wheel controller, mechanical saw) from 16 to 32 slots
 * (plan_flesh_recipes.md 5.3). The crushing wheel rolls a whole inserted stack at once, merges the results by item
 * and copies them into slots 1 and up; any distinct item past the last slot was lost. A 64 stack of flesh can roll
 * all 17 items of its pools, more than the 15 output slots. 31 output slots cover the raised output cap with room.
 *
 * <p>The size is set after construction, and again after loading: {@code ItemStackHandler.deserializeNBT} resizes
 * to the saved "Size", so a block saved before this change would load back at 16. The loaded stacks are kept.
 */
@Mixin(value = ProcessingInventory.class, remap = false)
public abstract class ProcessingInventoryMixin extends ItemStackHandler {

    @Unique
    private static final int creativeengineering$SLOTS = 32;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void creativeengineering$growOnCreate(CallbackInfo ci) {
        if (stacks.size() < creativeengineering$SLOTS) setSize(creativeengineering$SLOTS);
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void creativeengineering$growOnLoad(CompoundTag nbt, CallbackInfo ci) {
        if (stacks.size() >= creativeengineering$SLOTS) return;
        NonNullList<ItemStack> loaded = stacks;
        setSize(creativeengineering$SLOTS);
        for (int slot = 0; slot < loaded.size(); slot++) stacks.set(slot, loaded.get(slot));
    }
}
