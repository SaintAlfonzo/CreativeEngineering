package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.mojang.logging.LogUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.slf4j.Logger;

/**
 * Fixes a bug in IE's TagUtils.getMatchingPrefixAndRemaining.
 *
 * THE BUG:
 *   getMatchingTagNames() returns a java.util.HashSet, which has no
 *   guaranteed iteration order. The original method returns the FIRST
 *   candidate it finds that starts with any of the given prefixes
 *   (e.g. "rods"). Items commonly have BOTH a specific tag
 *   (forge:rods/iron) and a generic grouping tag (forge:rods/all_metal).
 *   Whichever one happens to come first in that item's particular
 *   HashSet bucket order "wins" - a coincidence of hashing, not logic.
 *   When "all_metal" wins, IE tries to look up an ingot for material
 *   "all_metal", finds nothing, and the recycling recipe outputs air.
 *
 * THE FIX:
 *   Instead of returning the first candidate found, collect ALL
 *   candidates and return the first one whose material actually
 *   resolves to a real, non-empty forge:ingots/<material> tag (checked
 *   via the same IETags/TagUtils lookup IE itself uses downstream).
 *   A grouping tag like "all_metal" has no matching ingot tag, so it
 *   can never win against a real metal candidate. If NO candidate
 *   resolves to a real ingot, we fall back to the original first-found
 *   behavior so non-metal callers (e.g. plates, gears) are unaffected.
 *
 * WHY THIS NEEDS NO METAL/SUFFIX LIST:
 *   We don't need to know what "all_metal" is called or enumerate
 *   valid metals. We just ask "does this candidate's material have a
 *   real ingot backing it?" - which is exactly the question IE itself
 *   needs answered to produce a correct recipe.
 */
@Mixin(value=TagUtils.class, remap=false)
public class IETagUtilsMixin {
		private static final Logger LOGGER = LogUtils.getLogger();
		private static final java.util.Set<String> BLACKLISTED_MATERIALS = java.util.Set.of(
				"compressed_iron"
				// add any other tags here that should never be treated as a
				// legitimate material family for recycling purposes
		);

    @Overwrite
		public static String[] getMatchingPrefixAndRemaining(RegistryAccess tags, ItemStack stack, String... componentTypes) {
				LOGGER.info("Called arc recycling mixin func");
				String[] fallback = null;

				for (ResourceLocation name : TagUtils.getMatchingTagNames(tags, stack)) {
				    for (String componentType : componentTypes) {
				        if (!name.getPath().startsWith(componentType)) {
				            continue;
				        }

				        String material = name.getPath().substring(componentType.length());
				        if (material.startsWith("/")) {
				            material = material.substring(1);
				        }

				        if (material.isEmpty()) {
				            continue;
				        }

				        // NEW: skip denylisted materials entirely, even if they'd
				        // otherwise resolve to a real, non-empty ingot tag.
				        if (BLACKLISTED_MATERIALS.contains(material)) {
				            continue;
				        }

				        if (fallback == null) {
				            fallback = new String[]{componentType, material};
				        }

				        try {
				            ResourceLocation ingotTag = IETags.getIngot(material);
				            if (ingotTag != null && TagUtils.isNonemptyItemTag(tags, ingotTag)) {
				                return new String[]{componentType, material};
				            }
				        } catch (Exception ignored) {
				        }
				    }
				}

				return fallback;
		}
}
