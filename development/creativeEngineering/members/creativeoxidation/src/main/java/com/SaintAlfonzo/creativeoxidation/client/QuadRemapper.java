// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.client;

import com.SaintAlfonzo.creativeoxidation.CreativeOxidation;
import com.SaintAlfonzo.creativeoxidation.OxidationProperties;
import com.SaintAlfonzo.creativeoxidation.OxidationStage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Swaps a quad's copper sprite for its staged copy {@code creativeoxidation:<block|item>/<stage>/[compat/<namespace>/]<path>}.
 * The source sprite is the quad's own when its UVs lie inside it, else the listed sprite whose atlas area holds all four
 * UVs (Create's connected textures keep the base sprite but move the UVs). A missing staged sprite keeps the quad.
 * Caches are cleared on every model bake; bounding them is P5 (B3).
 */
public final class QuadRemapper {
	private static final int U_OFFSET = 4;
	private static final int V_OFFSET = 5;
	private static final Set<String> OXIDIZABLE_TEXTURES = loadTextureManifest();
	private static final List<ResourceLocation> OXIDIZABLE_LOCATIONS = OXIDIZABLE_TEXTURES.stream().map(ResourceLocation::tryParse).toList();
	private static final ConcurrentHashMap<BakedQuad, Optional<ResourceLocation>> SOURCE_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<CacheKey, BakedQuad> CACHE = new ConcurrentHashMap<>();

	private QuadRemapper() {
	}

	public static void clearCaches() {
		SOURCE_CACHE.clear();
		CACHE.clear();
	}

	public static List<BakedQuad> remap(List<BakedQuad> quads, BlockState state) {
		if (state == null || !OxidationProperties.hasProperties(state)) return quads;
		return remap(quads, state.getValue(OxidationProperties.STAGE));
	}

	public static List<BakedQuad> remap(List<BakedQuad> quads, OxidationStage stage) {
		if (stage == OxidationStage.UNAFFECTED || quads.isEmpty()) return quads;
		List<BakedQuad> result = new ArrayList<>(quads.size());
		for (BakedQuad quad : quads) {
			Optional<ResourceLocation> source = SOURCE_CACHE.computeIfAbsent(quad, q -> Optional.ofNullable(actualSource(q)));
			if (source.isEmpty()) {
				result.add(quad);
			} else {
				result.add(CACHE.computeIfAbsent(new CacheKey(quad, stage), k -> remapQuad(quad, source.get(), stage)));
			}
		}
		return result;
	}

	private static Function<ResourceLocation, TextureAtlasSprite> blockAtlas() {
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
	}

	private static ResourceLocation actualSource(BakedQuad quad) {
		TextureAtlasSprite declared = quad.getSprite();
		ResourceLocation declaredLocation = declared.getName();
		if (OXIDIZABLE_TEXTURES.contains(declaredLocation.toString()) && containsAllUvs(declared, quad.getVertices())) {
			return declaredLocation;
		}
		Function<ResourceLocation, TextureAtlasSprite> atlas = blockAtlas();
		for (ResourceLocation location : OXIDIZABLE_LOCATIONS) {
			TextureAtlasSprite candidate = atlas.apply(location);
			if (candidate.getName().equals(location) && containsAllUvs(candidate, quad.getVertices())) return location;
		}
		return null;
	}

	private static boolean containsAllUvs(TextureAtlasSprite sprite, int[] vertices) {
		int stride = vertices.length / 4;
		float epsilon = 1.0E-5F;
		float minU = Math.min(sprite.getU0(), sprite.getU1()) - epsilon;
		float maxU = Math.max(sprite.getU0(), sprite.getU1()) + epsilon;
		float minV = Math.min(sprite.getV0(), sprite.getV1()) - epsilon;
		float maxV = Math.max(sprite.getV0(), sprite.getV1()) + epsilon;
		for (int vertex = 0; vertex < 4; vertex++) {
			int base = vertex * stride;
			float u = Float.intBitsToFloat(vertices[base + U_OFFSET]);
			float v = Float.intBitsToFloat(vertices[base + V_OFFSET]);
			if (u < minU || u > maxU || v < minV || v > maxV) return false;
		}
		return true;
	}

	/** The staged copy of a listed sprite: {@code creativeoxidation:<block|item>/<stage>/[compat/<namespace>/]<path>}, or null. */
	static ResourceLocation stagedLocation(ResourceLocation source, OxidationStage stage) {
		String path = source.getPath();
		String textureRoot;
		if (path.startsWith("block/")) {
			path = path.substring("block/".length());
			textureRoot = "block/";
		} else if (path.startsWith("item/")) {
			path = path.substring("item/".length());
			textureRoot = "item/";
		} else {
			return null;
		}
		String namespacePath = source.getNamespace().equals("create") ? "" : "compat/" + source.getNamespace() + "/";
		return new ResourceLocation(CreativeOxidation.MOD_ID, textureRoot + stage.getSerializedName() + "/" + namespacePath + path);
	}

	/**
	 * Every staged sprite whose PNG is in this jar. Minecraft 1.19.2 stitches only the textures models name (1.20's
	 * atlas sources took the whole folder upstream), so the client adds these to the block atlas before stitching.
	 */
	static List<ResourceLocation> stagedSprites() {
		List<ResourceLocation> sprites = new ArrayList<>();
		for (ResourceLocation source : OXIDIZABLE_LOCATIONS) {
			if (source == null) continue;
			for (OxidationStage stage : OxidationStage.values()) {
				if (stage == OxidationStage.UNAFFECTED) continue;
				ResourceLocation staged = stagedLocation(source, stage);
				if (staged != null && QuadRemapper.class.getResource("/assets/" + staged.getNamespace() + "/textures/" + staged.getPath() + ".png") != null) {
					sprites.add(staged);
				}
			}
		}
		return sprites;
	}

	private static BakedQuad remapQuad(BakedQuad quad, ResourceLocation source, OxidationStage stage) {
		ResourceLocation targetLocation = stagedLocation(source, stage);
		if (targetLocation == null) return quad;
		Function<ResourceLocation, TextureAtlasSprite> atlas = blockAtlas();
		TextureAtlasSprite target = atlas.apply(targetLocation);
		if (!target.getName().equals(targetLocation)) return quad;
		TextureAtlasSprite sourceSprite = atlas.apply(source);
		int[] vertices = quad.getVertices().clone();
		int stride = vertices.length / 4;
		for (int vertex = 0; vertex < 4; vertex++) {
			int base = vertex * stride;
			float oldU = Float.intBitsToFloat(vertices[base + U_OFFSET]);
			float oldV = Float.intBitsToFloat(vertices[base + V_OFFSET]);
			float localU = Math.max(0.0F, Math.min(16.0F, (float) sourceSprite.getUOffset(oldU)));
			float localV = Math.max(0.0F, Math.min(16.0F, (float) sourceSprite.getVOffset(oldV)));
			vertices[base + U_OFFSET] = Float.floatToRawIntBits(target.getU(localU));
			vertices[base + V_OFFSET] = Float.floatToRawIntBits(target.getV(localV));
		}
		return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), target, quad.isShade());
	}

	private static Set<String> loadTextureManifest() {
		InputStream stream = QuadRemapper.class.getResourceAsStream("/assets/" + CreativeOxidation.MOD_ID + "/oxidizable_textures.txt");
		if (stream == null) return Collections.emptySet();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			return reader.lines().map(String::trim).filter(line -> !line.isEmpty() && !line.startsWith("#"))
					.collect(Collectors.toUnmodifiableSet());
		} catch (IOException e) {
			throw new IllegalStateException("Could not read the oxidizable texture manifest", e);
		}
	}

	private record CacheKey(BakedQuad quad, OxidationStage stage) {
	}
}
