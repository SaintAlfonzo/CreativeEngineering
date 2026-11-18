package com.SaintAlfonzo.creativedelight.crops;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.SaintAlfonzo.creativedelight.CDConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Canopy-proof wild crops (plan_post_scratch_1115 C4, E2): a placed feature in {@code #creativedelight:canopy_proof} whose
 * heightmap placement asks for {@code MOTION_BLOCKING} gets {@code MOTION_BLOCKING_NO_LEAVES} instead, so a patch under
 * trees starts on the ground, not on the leaves. Covers Java-registered features (Farmer's Delight's carrots and onions)
 * as well as JSON ones. Switch {@code [worldgen] wildCropCanopyProofHeightmap}, default off. Called from
 * {@code mixin/minecraft/HeightmapPlacementCanopyMixin}.
 */
public final class CanopyProofHeightmap {

	public static final TagKey<PlacedFeature> TAG = TagKey.create(Registry.PLACED_FEATURE_REGISTRY,
			new ResourceLocation("creativedelight", "canopy_proof"));

	/** The tag's features by identity, for the registry it was read from; rebuilt after a tag reload. */
	private static volatile Registry<PlacedFeature> cachedFor;
	private static volatile Set<PlacedFeature> cached = Set.of();

	private CanopyProofHeightmap() {}

	/** The heightmap to use for this placement. */
	public static Heightmap.Types swap(PlacementContext context, Heightmap.Types asked) {
		if (asked != Heightmap.Types.MOTION_BLOCKING || !on()) return asked;
		PlacedFeature feature = context.topFeature().orElse(null);
		if (feature == null) return asked;
		return canopyProof(context, feature) ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES : asked;
	}

	private static boolean on() {
		try {
			return CDConfig.WILD_CROP_CANOPY_PROOF.get();
		} catch (IllegalStateException e) { // config not loaded yet
			return false;
		}
	}

	private static boolean canopyProof(PlacementContext context, PlacedFeature feature) {
		Registry<PlacedFeature> registry = context.getLevel().registryAccess().registry(Registry.PLACED_FEATURE_REGISTRY).orElse(null);
		if (registry == null) return false;
		Set<PlacedFeature> set = cached;
		if (cachedFor != registry) {
			synchronized (CanopyProofHeightmap.class) {
				if (cachedFor != registry) {
					Set<PlacedFeature> s = Collections.newSetFromMap(new IdentityHashMap<>());
					registry.getTag(TAG).ifPresent(tag -> {
						for (Holder<PlacedFeature> h : tag) s.add(h.value());
					});
					cached = Collections.unmodifiableSet(s);
					cachedFor = registry;
				}
				set = cached;
			}
		}
		return set.contains(feature);
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		cachedFor = null;
	}
}
