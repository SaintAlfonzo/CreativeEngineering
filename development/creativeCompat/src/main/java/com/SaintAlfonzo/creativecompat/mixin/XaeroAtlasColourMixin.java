package com.SaintAlfonzo.creativecompat.mixin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Xaero's World Map 1.40.11, accurate block colours ({@code block_colors = 0}): {@code MapWriter.loadBlockColourFromTexture}
 * caches one average colour per texture name ({@code textureColours}), but fills a miss by opening the PNG through the
 * resource manager and decoding it with ImageIO on the render thread. It never closes the resource stream (the
 * "Leaked resource ... Finalizer" warnings), and the cache is cleared on every resource reload, so every texture is
 * read from disk again after each reload.
 *
 * The same texture is already decoded in memory, stitched into the block atlas. On a cache miss this samples the atlas
 * sprite exactly as Xaero samples the PNG (first frame, square side min(w, h), step max(1, min(4, side / 8)), pixels
 * with alpha 0 or value 0 skipped, plain average of A, R, G, B) and stores it in Xaero's cache, so Xaero takes its
 * cached-colour branch and opens no file. A sprite that is not on the block atlas, or an all-black result (Xaero
 * throws on it and uses the material colour), returns null, and Xaero's own PNG path runs unchanged.
 * Pseudo: client-only mod, may be removed. feature_writeup/perf.md (Xaero colours).
 */
@Pseudo
@Mixin(targets = "xaero.map.MapWriter", remap = false)
public class XaeroAtlasColourMixin {

	/** Ordinal 0 is blockColours.get(state); ordinal 1 is textureColours.get(name). */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(method = "loadBlockColourFromTexture", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 1, remap = false,
					target = "Ljava/util/HashMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object creativecompat$atlasColour(HashMap map, Object key) {
		Object cached = map.get(key);
		if (cached != null || !(key instanceof String name)) {
			return cached;
		}
		Integer colour = creativecompat$sample(name);
		if (colour != null) {
			((Map<String, Integer>) map).put(name, colour);
		}
		return colour;
	}

	private static Integer creativecompat$sample(String name) {
		if (!name.endsWith(".png")) {
			return null;
		}
		ResourceLocation id;
		try {
			id = new ResourceLocation(name.substring(0, name.length() - 4));
		} catch (ResourceLocationException e) {
			return null;
		}
		TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
		TextureAtlasSprite sprite = atlas.getSprite(id);
		if (sprite == null || (sprite.getName().equals(MissingTextureAtlasSprite.getLocation())
				&& !id.equals(MissingTextureAtlasSprite.getLocation()))) {
			return null;
		}
		int side = Math.min(sprite.getWidth(), sprite.getHeight());
		long red = 0, green = 0, blue = 0, alpha = 0;
		int total = 0;
		if (side > 0) {
			int step = Math.max(1, Math.min(4, side / 8));
			int parts = side / step;
			for (int i = 0; i < parts; i++) {
				for (int j = 0; j < parts; j++) {
					int abgr = sprite.getPixelRGBA(0, i * step, j * step);
					int a = abgr >>> 24;
					if (abgr != 0 && a != 0) {
						red += abgr & 0xFF;
						green += (abgr >> 8) & 0xFF;
						blue += (abgr >> 16) & 0xFF;
						alpha += a;
						total++;
					}
				}
			}
		}
		if (total == 0) {
			total = 1;
		}
		int r = (int) (red / total), g = (int) (green / total), b = (int) (blue / total), a = (int) (alpha / total);
		if (r == 0 && g == 0 && b == 0) {
			return null;
		}
		return a << 24 | r << 16 | g << 8 | b;
	}
}
