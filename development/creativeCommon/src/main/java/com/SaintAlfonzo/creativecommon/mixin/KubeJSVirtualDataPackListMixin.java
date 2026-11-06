package com.SaintAlfonzo.creativecommon.mixin;

import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes KubeJS's in-memory datapack (ServerEvents.highPriorityData / lowPriorityData) listing
 * files from a sibling directory whose name merely starts with the requested one.
 *
 * THE BUG:
 *   VirtualKubeJSDataPack#getResources(type, namespace, dir, filter) keeps every added file with
 *   {@code path.startsWith(dir)} - no trailing "/". Vanilla registry loading lists
 *   "worldgen/structure", so every "worldgen/structure_set/*.json" added through
 *   event.add(...) also comes back and is decoded as a Structure. World creation then fails with
 *   "Failed to validate datapack: Error loading registry data: Input does not contain a key
 *   [type]" (see kubejs/server_scripts/worldgen/structure_overrides.js). Same trap for any
 *   dir that prefixes another (dimension / dimension_type, ...). Real folder/zip packs and
 *   KubeJS's own GeneratedResourcePack match on "dir/", so only this class is affected.
 *
 * THE FIX:
 *   Redirect the startsWith inside the filter lambda so the prefix is treated as a directory.
 *   An empty dir (whole namespace) is left alone, matching vanilla.
 *
 * require = 1: if a KubeJS update renames the lambda, boot fails here rather than every
 * world silently refusing to generate again.
 */
@Mixin(value = VirtualKubeJSDataPack.class, remap = false)
public class KubeJSVirtualDataPackListMixin {

	@Redirect(
			method = "lambda$getResources$1",
			at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"),
			require = 1,
			remap = false)
	private static boolean creativecommon$matchDirectory(String path, String dir) {
		if (dir.isEmpty() || dir.endsWith("/")) return path.startsWith(dir);
		return path.startsWith(dir + "/");
	}
}
