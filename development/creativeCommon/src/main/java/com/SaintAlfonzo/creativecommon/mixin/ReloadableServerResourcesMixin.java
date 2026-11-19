// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.DataNamespaces;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Records the data namespaces of every reload's resource manager ({@link DataNamespaces}) before the reload listeners
 * run (m_206861_ = loadResources; KubeJS has already wrapped the manager with its own packs by then).
 */
@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	@Inject(method = "m_206861_(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
			at = @At("HEAD"), require = 1, remap = false)
	private static void creativecommon$dataNamespaces(ResourceManager resources, RegistryAccess.Frozen registries,
			Commands.CommandSelection selection, int permissionLevel, Executor background, Executor main,
			CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
		DataNamespaces.capture(resources);
	}
}
