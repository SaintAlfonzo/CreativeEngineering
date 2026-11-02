package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.BannerPatternAtlasCompat;
import com.illusivesoulworks.bedspreads.client.DecoratedBedBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code DecoratedBedBlockEntityRenderer.renderPatterns} always resolves each pattern layer's
 * texture as {@code bedspreads:entity/<originNamespace>/<originPath>}. That only has a real file
 * behind it for the handful of mods bedspreads bundled a compat copy for ({@code minecraft},
 * {@code alexsmobs}, {@code iceandfire}, {@code rats}, {@code additionalbanners}) — every other
 * banner-pattern mod in this pack (Botania, Buzzier Bees, Immersive Engineering, MineColonies)
 * renders blank on decorated beds.
 *
 * <p>Naively redirecting to the pattern's *real* {@code <originNamespace>:entity/banner/<path>}
 * texture (what an earlier version of this mixin did) is wrong, not just incomplete: bedspreads
 * renders the pattern overlay using vanilla's own bed {@code ModelPart} geometry (headboard/
 * footboard/mattress), not a banner-flag model, and its curated compat textures are hand
 * redrawn/recentered copies of each pattern's glyph specifically repositioned to land on that
 * geometry's UV layout. A pattern's real texture is authored for the banner *flag's* UV
 * footprint instead — landing it on the bed model puts the glyph in an unrelated spot, offset
 * and cropped wrong (confirmed by comparing bedspreads' bundled
 * {@code entity/minecraft/creeper.png} against vanilla's real {@code entity/banner/creeper.png}:
 * same glyph, different size and position entirely). That mismatch affected every pattern this
 * mixin touched, including ones bedspreads already had working curated textures for, which is
 * why redirecting unconditionally broke *all* decorated beds rather than just the unsupported
 * mods.
 *
 * <p>So: if bedspreads already ships a curated texture for this pattern, leave its own location
 * alone. Otherwise fall back to {@code creativeengineering:entity/<originNamespace>/<path>} — a
 * texture generated offline by {@code tools/gen_bedspreads_pattern_compat.py}, which reproduces
 * bedspreads' own recentering convention for every other installed mod's patterns. Both that and
 * bedspreads' own textures are bound against {@code InventoryMenu.BLOCK_ATLAS} (confirmed
 * identical to {@code TextureAtlas.LOCATION_BLOCKS}); {@link BannerPatternAtlasCompat} stitches
 * whichever one applies into the block atlas so it actually resolves. A pattern with neither
 * (e.g. a mod installed after the generator last ran) falls back to bedspreads' own
 * {@code bedspreadsLocation} — rendering blank, same as bedspreads' un-patched behavior — rather
 * than the real banner-flag texture, since that's confirmed actively wrong on this model.
 */
@Mixin(value = DecoratedBedBlockEntityRenderer.class, remap = false)
public class BedspreadsBannerPatternMixin {

    @Redirect(
            method = "renderPatterns",
            at = @At(value = "NEW", target = "Lnet/minecraft/resources/ResourceLocation;")
    )
    private static ResourceLocation creativeengineering$realPatternTexture(String bedspreadsNamespace, String path) {
        ResourceLocation bedspreadsLocation = new ResourceLocation(bedspreadsNamespace, path);

        // bedspreadsLocation is a Material-style location ("bedspreads:entity/ns/id") - the atlas
        // expands that to "textures/entity/ns/id.png" internally when it actually loads the
        // sprite. ResourceManager#getResource does no such expansion, so the existence check
        // needs the real file path spelled out, or it always misses and this mixin ends up
        // redirecting every pattern - including ones bedspreads already has working art for.
        ResourceLocation bedspreadsFile = new ResourceLocation(bedspreadsNamespace, "textures/" + path + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(bedspreadsFile).isPresent()) {
            return bedspreadsLocation;
        }

        String rest = path.startsWith("entity/") ? path.substring("entity/".length()) : path;
        int slash = rest.indexOf('/');
        if (slash < 0) {
            return bedspreadsLocation;
        }
        String originNamespace = rest.substring(0, slash);
        String originPath = rest.substring(slash + 1);

        String relativePath = "entity/" + originNamespace + "/" + originPath;
        ResourceLocation generatedFile = new ResourceLocation("creativeengineering", "textures/" + relativePath + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(generatedFile).isPresent()) {
            return new ResourceLocation("creativeengineering", relativePath);
        }
        return bedspreadsLocation;
    }
}
