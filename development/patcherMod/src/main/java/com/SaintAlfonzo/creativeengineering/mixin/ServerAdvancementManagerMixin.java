package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code [advancements] enabled = false}: the advancement JSONs are dropped before the tree is built (m_5787_ =
 * apply), so the game has no advancements at all: nothing to show, toast, award or reward. Players' saved progress
 * for unknown advancements is ignored by vanilla. The few item rewards that came only from advancements (Vinery
 * banner, Lootr trophy, MCA and Yuushya books, IE shader bags) go with them.
 */
@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin {

	@ModifyVariable(method = "m_5787_(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("HEAD"), argsOnly = true, remap = false)
	private Map<ResourceLocation, JsonElement> creativeengineering$noAdvancements(Map<ResourceLocation, JsonElement> advancements) {
		if (CEConfig.COMMON_SPEC.isLoaded() && !CEConfig.ADVANCEMENTS.enabled.get() && !advancements.isEmpty()) {
			LogUtils.getLogger().info("[advancements] {} advancements left out ([advancements] enabled = false)", advancements.size());
			return new HashMap<>();
		}
		return advancements;
	}
}
