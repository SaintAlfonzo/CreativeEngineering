package com.SaintAlfonzo.creativecommon.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

/**
 * No recipe unlocks of any kind (design rule DR-C14, plan_rules_fixes.md section 12e, D23): the recipe book never
 * gains an entry and no "new recipe" toast shows. {@code ServerPlayer.awardRecipes(Collection)} ({@code m_7281_}) and
 * {@code awardRecipesByKey(ResourceLocation[])} ({@code m_7902_}) return at once. Every unlock path goes through them:
 * the crafting table's result slot, furnaces, smokers and blast furnaces, Farmer's Delight, the knowledge book,
 * advancement rewards, {@code /recipe give} and other mods. Cooking XP is untouched: a furnace pops its experience
 * before it calls awardRecipes. Entries already in a save stay.
 */
@Mixin(ServerPlayer.class)
public abstract class NoRecipeUnlockMixin {

	@Inject(method = "m_7281_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecommon$noAwardRecipes(Collection<Recipe<?>> recipes, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(0);
	}

	@Inject(method = "m_7902_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecommon$noAwardRecipesByKey(ResourceLocation[] keys, CallbackInfo ci) {
		ci.cancel();
	}
}
