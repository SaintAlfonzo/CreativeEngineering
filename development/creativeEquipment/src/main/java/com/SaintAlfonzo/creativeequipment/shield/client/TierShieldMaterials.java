package com.SaintAlfonzo.creativeequipment.shield.client;

import com.SaintAlfonzo.creativeequipment.CreativeEquipment;

import java.util.EnumMap;
import java.util.Map;

import com.SaintAlfonzo.creativeequipment.shield.ShieldTier;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Per-tier replacements for {@code ModelBakery.SHIELD_BASE} / {@code NO_PATTERN_SHIELD} - vanilla
 * builds those two as {@code new Material(TextureAtlas.LOCATION_BLOCKS, "entity/shield_base[_nopattern]")},
 * i.e. plain sprites in the main block atlas (confirmed by decompiling {@code ModelBakery}'s
 * static init - {@code Sheets.SHIELD_SHEET} is just {@code TextureAtlas.LOCATION_BLOCKS} under
 * another name, not a separate shield-only sheet). {@link TierShieldRenderer} picks one of these
 * per tier instead of vanilla's pair, everything else about the render path (model, banner
 * pattern overlay, foil) stays vanilla's own.
 *
 * <p>Source PNGs: {@code assets/creativeengineering/textures/entity/shield/<tier>_shield_base[_nopattern].png}.
 */
@Mod.EventBusSubscriber(modid = CreativeEquipment.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TierShieldMaterials {

    public record Pair(Material base, Material noPattern) {}

    private static final Map<ShieldTier, Pair> BY_TIER = new EnumMap<>(ShieldTier.class);

    static {
        for (ShieldTier tier : ShieldTier.values()) {
            // IRON has no TierShieldItem of its own (it's the player's real Items.SHIELD - see
            // ShieldTier#of) so it has no renderer to feed and no texture pair here either.
            if (tier == ShieldTier.IRON) continue;
            BY_TIER.put(tier, new Pair(sprite(tier, "_shield_base"), sprite(tier, "_shield_base_nopattern")));
        }
    }

    private static Material sprite(ShieldTier tier, String suffix) {
        return new Material(TextureAtlas.LOCATION_BLOCKS,
                new ResourceLocation("creativeengineering", "entity/shield/" + tier.id() + suffix));
    }

    public static Pair of(ShieldTier tier) {
        return BY_TIER.get(tier);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        for (Pair pair : BY_TIER.values()) {
            event.addSprite(pair.base().texture());
            event.addSprite(pair.noPattern().texture());
        }
    }
}
