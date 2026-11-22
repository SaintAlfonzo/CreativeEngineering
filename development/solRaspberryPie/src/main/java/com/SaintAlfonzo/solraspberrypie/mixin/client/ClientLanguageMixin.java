// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.LangOverrides;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

/** Applies {@link LangOverrides} to the finished translation map, after every namespace's lang file is merged. */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
	@ModifyArg(method = "loadFrom", at = @At(value = "INVOKE",
			target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", remap = false))
	private static Map<String, String> solrp$overrides(Map<String, String> map) {
		LangOverrides.apply(map);
		return map;
	}
}
