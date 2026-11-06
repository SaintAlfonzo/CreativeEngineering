package com.SaintAlfonzo.creativecommon.mixin;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Two tweaks to IE's shared fluid-input predicate, used by the Bottling Machine (and the
 * Mechanical... IE Mixer / Refinery — the change is intentionally shared and strictly more permissive):
 *
 * <ol>
 *   <li><b>Fluid by id.</b> {@code FluidTagInput.deserialize} only understands a fluid
 *       <em>tag</em> ({@code "tag"}). We add a {@code "fluid"} branch so a recipe can name a
 *       fluid directly ({@code "fluid":{"fluid":"create:potion","amount":250,"nbt":"{...}"}}),
 *       matching the fluid JSON shape KubeJS emits. Implemented via the existing
 *       {@code Either.right(List<ResourceLocation>)} form that {@code testIgnoringAmount},
 *       {@code read} and {@code write} already handle.</li>
 *   <li><b>Subset NBT match.</b> When a recipe carries an {@code nbt} block IE requires the
 *       held fluid's tag to be <em>exactly equal</em> ({@code CompoundTag.equals}). We relax
 *       that to a subset check so e.g. {@code {Bottle:"REGULAR"}} matches a {@code create:potion}
 *       stack that also carries {@code Potion}/{@code CustomPotionEffects}. A recipe with no
 *       {@code nbt} is unaffected (it already matches regardless of stack NBT).</li>
 * </ol>
 */
@Mixin(value = FluidTagInput.class, remap = false)
public class FluidTagInputMixin {

    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;)Lblusunrize/immersiveengineering/api/crafting/FluidTagInput;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void creativecommon$deserializeFluidById(JsonElement input,
                                                                CallbackInfoReturnable<FluidTagInput> cir) {
        if (input == null || !input.isJsonObject()) return;
        JsonObject obj = input.getAsJsonObject();
        if (!obj.has("fluid") || obj.has("tag")) return;

        ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(obj, "fluid"));
        int amount = GsonHelper.getAsInt(obj, "amount");

        CompoundTag nbt = null;
        if (GsonHelper.isValidNode(obj, "nbt")) {
            try {
                nbt = ItemUtils.parseNbtFromJson(obj.get("nbt"));
            } catch (Exception e) {
                throw new com.google.gson.JsonParseException("Invalid 'nbt' in fluid input " + fluidId, e);
            }
        }

        cir.setReturnValue(new FluidTagInput(Either.right(List.of(fluidId)), amount, nbt));
    }

    @Redirect(
            method = "testIgnoringAmount(Lnet/minecraftforge/fluids/FluidStack;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;equals(Ljava/lang/Object;)Z")
    )
    private boolean creativecommon$nbtSubsetMatch(CompoundTag recipeNbt, Object stackTag) {
        return NbtUtils.compareNbt(recipeNbt, (Tag) stackTag, true);
    }
}
