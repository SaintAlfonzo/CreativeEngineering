package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillBlockEntity;
import com.SaintAlfonzo.creativeengineering.sawmill.SawbladeRouting;
import com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillProcessExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Makes the sawmill's per-item process resolve its recipe through the installed sawblade's
 * {@code {Profile}} NBT instead of IE's blade-blind first-match.
 *
 * <p>{@code SawmillProcess} is a {@code static} nested class with no back-reference to the
 * block entity, and {@code getRecipeDependentData(Level)} — the single call site of
 * {@code SawmillRecipe.findRecipe} — only gets a {@code Level}. So:
 * <ul>
 *   <li>{@code processStep} (server, every tick) copies {@code be.sawblade}'s profile onto the process;</li>
 *   <li>the redirect routes with that profile and records the chosen recipe id;</li>
 *   <li>{@code writeToNBT}/{@code readFromNBT} carry the profile + id so the client (which
 *       never runs {@code processStep} and rebuilds the queue on every sync) resolves the
 *       identical recipe for its render.</li>
 * </ul>
 */
@Mixin(value = SawmillBlockEntity.SawmillProcess.class, remap = false)
public class SawmillProcessMixin implements SawmillProcessExt {

    @Unique private String creativeengineering$bladeProfile;
    @Unique private String creativeengineering$resolvedRecipeId;

    @Override
    public String creativeengineering$getBladeProfile() {
        return creativeengineering$bladeProfile == null ? "" : creativeengineering$bladeProfile;
    }

    @Override
    public void creativeengineering$setBladeProfile(String profile) {
        this.creativeengineering$bladeProfile = profile == null ? "" : profile;
    }

    @Override
    public String creativeengineering$getResolvedRecipeId() {
        return creativeengineering$resolvedRecipeId == null ? "" : creativeengineering$resolvedRecipeId;
    }

    @Override
    public void creativeengineering$setResolvedRecipeId(String id) {
        this.creativeengineering$resolvedRecipeId = id == null ? "" : id;
    }

    @Inject(method = "processStep", at = @At("HEAD"))
    private void creativeengineering$captureBlade(SawmillBlockEntity be, Set<ItemStack> secondaryOutputs,
                                                 CallbackInfoReturnable<Boolean> cir) {
        this.creativeengineering$bladeProfile = SawbladeRouting.profileOf(be.sawblade);
    }

    @Redirect(
            method = "getRecipeDependentData",
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;findRecipe(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;"
            )
    )
    private SawmillRecipe creativeengineering$routeByBlade(Level level, ItemStack input) {
        // Client, or server after a reload: reuse the exact recipe the server already routed to.
        String saved = creativeengineering$getResolvedRecipeId();
        if (!saved.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(saved);
            if (id != null) {
                SawmillRecipe byId = SawmillRecipe.RECIPES.getById(level, id);
                if (byId != null) return byId;
            }
        }
        SawmillRecipe routed = SawbladeRouting.route(level, input, creativeengineering$getBladeProfile());
        this.creativeengineering$resolvedRecipeId =
                routed == null ? "" : ((Recipe<?>) routed).getId().toString();
        return routed;
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void creativeengineering$writeBladeData(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag == null) return;
        tag.putString("ce_BladeProfile", creativeengineering$getBladeProfile());
        tag.putString("ce_ResolvedRecipe", creativeengineering$getResolvedRecipeId());
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private static void creativeengineering$readBladeData(CompoundTag tag,
                                                         CallbackInfoReturnable<SawmillBlockEntity.SawmillProcess> cir) {
        SawmillBlockEntity.SawmillProcess process = cir.getReturnValue();
        if (process == null) return;
        SawmillProcessExt ext = (SawmillProcessExt) process;
        ext.creativeengineering$setBladeProfile(tag.getString("ce_BladeProfile"));
        ext.creativeengineering$setResolvedRecipeId(tag.getString("ce_ResolvedRecipe"));
    }
}
