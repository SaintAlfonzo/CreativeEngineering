package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.BannerPatternAtlasCompat;
import com.hidoni.customizableelytra.items.CustomizableElytraItem;
import com.hidoni.customizableelytra.util.BannerCustomizationHandler;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code CustomizableElytraItem.getTextureLocation} resolves a banner pattern's texture as
 * {@code customizableelytra:entity/elytra_banner/<path>} (dropping the origin namespace
 * entirely), matching only the ~100 patterns it bundled a pre-baked, wing-shaped PNG for.
 * {@code BannerCustomizationHandler.render} already detects a miss (the resulting sprite comes
 * back as {@code MissingTextureAtlasSprite}) but just skips the layer instead of trying anything
 * else -- so any pattern id it didn't special-case (Botania, Buzzier Bees, most of Immersive
 * Engineering and MineColonies, ...) renders as nothing on the elytra.
 *
 * <p>An earlier version of this mixin fell back to the pattern's *real*
 * {@code <originNamespace>:entity/banner/<path>} texture when the curated lookup missed. That is
 * wrong the same way an earlier bedspreads fix was wrong (see {@code BedspreadsBannerPatternMixin}
 * for the full story there): customizableelytra's curated textures are hand-cropped/repositioned
 * for the wing model's own 128x64 (or 64x32 "low quality") canvas -- e.g. its
 * {@code elytra_banner/creeper.png} places an *unscaled* crop of the vanilla creeper glyph
 * centered at roughly (81,23), nothing like the glyph's position in the real banner-flag texture
 * -- so pasting the real texture onto that canvas puts the glyph in the wrong place, same failure
 * mode as the bed patterns had.
 *
 * <p>So instead: probe the curated location first (unchanged), and if that's missing, probe our
 * own {@code creativeengineering:entity/elytra_banner[_low]/<originNamespace>/<path>} compat
 * texture -- generated offline by {@code tools/gen_elytra_pattern_compat.py}, which reproduces
 * customizableelytra's own centered-crop convention for every other installed mod's patterns.
 * Only fall back to the original (missing) curated location -- i.e. render nothing, same as
 * customizableelytra's own un-patched behavior -- if neither exists. Every location here binds
 * against {@code TextureAtlas.LOCATION_BLOCKS}, matching the atlas {@code render} itself binds
 * against; {@link BannerPatternAtlasCompat} stitches whichever compat texture applies into that
 * atlas so it actually resolves.
 */
@Mixin(value = BannerCustomizationHandler.class, remap = false)
public class CustomizableElytraBannerPatternMixin {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hidoni/customizableelytra/items/CustomizableElytraItem;getTextureLocation(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private ResourceLocation creativeengineering$fallbackToGeneratedPatternTexture(ResourceKey bannerKey) {
        ResourceLocation curated = CustomizableElytraItem.getTextureLocation(bannerKey);
        if (!isMissing(curated)) {
            return curated;
        }

        ResourceLocation key = bannerKey.location();
        String elytraFolder = curated.getPath().contains("elytra_banner_low") ? "elytra_banner_low" : "elytra_banner";
        ResourceLocation generated = new ResourceLocation("creativeengineering",
                "entity/" + elytraFolder + "/" + key.getNamespace() + "/" + key.getPath());
        return isMissing(generated) ? curated : generated;
    }

    private static boolean isMissing(ResourceLocation location) {
        Material probe = new Material(TextureAtlas.LOCATION_BLOCKS, location);
        return probe.sprite().getName() == MissingTextureAtlasSprite.getLocation();
    }
}
