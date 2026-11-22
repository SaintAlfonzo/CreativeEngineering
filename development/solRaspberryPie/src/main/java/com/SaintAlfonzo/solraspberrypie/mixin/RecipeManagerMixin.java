// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Drops Apple Pie's book + potato recipe ({@code solapplepie:food_book}) before the recipe manager parses it
 * (plan_rp_standalone.md P4): the Diet Log is crafted from Raspberry Pie's own {@code solraspberrypie:diet_log} recipe
 * (book + any food that is not a drink, which leaves book + carrot to the Food Dictionary). Plain Java, so it also runs
 * on a server without KubeJS; a datapack that wants Apple Pie's recipe back can add it under another id. Priority 900:
 * KubeJS (1100) cancels {@code apply} at HEAD and parses the map itself, so this has to run first.
 */
@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {
	@Unique
	private static final ResourceLocation SOLRP$APPLE_PIE_BOOK = new ResourceLocation("solapplepie", "food_book");

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("HEAD"))
	private void solrp$dropAppleBook(Map<ResourceLocation, JsonElement> recipes, ResourceManager resources, ProfilerFiller profiler, CallbackInfo ci) {
		try {
			recipes.remove(SOLRP$APPLE_PIE_BOOK);
		} catch (UnsupportedOperationException ignored) {
			// an immutable map from another mod's hook: leave Apple Pie's recipe in rather than fail the reload
		}
	}
}
