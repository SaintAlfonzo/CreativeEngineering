package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBases;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.crafting.KettleRecipe;

/**
 * {@code farmersrespite:brewing} in the pack's format (farmers_respite.md K8): 2.1's shape with one item result, a
 * {@code base} tank fluid ({@link KettleBases}) in place of {@code needwater}, and the brew time read from
 * {@code cookingtime}, the key every recipe writes (2.0 reads {@code brewingtime}, which no recipe has, so every brew
 * took its 200-tick default). The base also travels to clients after the 2.0 fields.
 */
@Mixin(value = KettleRecipe.Serializer.class, remap = false)
public abstract class KettleRecipeSerializerMixin {

	@Redirect(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lumpaz/farmersrespite/common/crafting/KettleRecipe;",
			require = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/GsonHelper;m_13824_(Lcom/google/gson/JsonObject;Ljava/lang/String;I)I"))
	private int ce$cookingTime(JsonObject json, String key, int fallback) {
		return GsonHelper.getAsInt(json, "cookingtime", GsonHelper.getAsInt(json, key, fallback));
	}

	/** needWater (does a brew use a unit) follows the base; a 2.0 recipe without base keeps its needwater. */
	@Redirect(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lumpaz/farmersrespite/common/crafting/KettleRecipe;",
			require = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/util/GsonHelper;m_13855_(Lcom/google/gson/JsonObject;Ljava/lang/String;Z)Z"))
	private boolean ce$needsBase(JsonObject json, String key, boolean fallback) {
		if (!KettleBases.hasBaseKey(json)) return GsonHelper.getAsBoolean(json, key, fallback);
		JsonObject probe = new JsonObject();
		probe.add("base", json.get("base"));
		return !KettleBases.read(new ResourceLocation("creativedelight", "probe"), probe).isEmpty();
	}

	@Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lumpaz/farmersrespite/common/crafting/KettleRecipe;",
			at = @At("RETURN"), require = 1)
	private void ce$readBase(ResourceLocation id, JsonObject json, CallbackInfoReturnable<KettleRecipe> cir) {
		KettleBases.set(cir.getReturnValue(), KettleBases.read(id, json));
	}

	@Inject(method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lumpaz/farmersrespite/common/crafting/KettleRecipe;",
			at = @At("RETURN"), require = 1)
	private void ce$readBaseNet(ResourceLocation id, FriendlyByteBuf buf, CallbackInfoReturnable<KettleRecipe> cir) {
		KettleBases.set(cir.getReturnValue(), KettleBases.readNet(buf));
	}

	@Inject(method = "toNetwork(Lnet/minecraft/network/FriendlyByteBuf;Lumpaz/farmersrespite/common/crafting/KettleRecipe;)V",
			at = @At("TAIL"), require = 1)
	private void ce$writeBaseNet(FriendlyByteBuf buf, KettleRecipe recipe, CallbackInfo ci) {
		KettleBases.write(buf, KettleBases.get(recipe));
	}
}
