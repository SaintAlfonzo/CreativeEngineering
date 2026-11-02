package com.SaintAlfonzo.creativedelight.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

/**
 * Canned Goods' canned golem, made dyeable by mixin.CannedGolemColorMixin. The colour is synced
 * entity data saved as NBT "Color" (DyeColor id) and defaults to red, which keeps the mod's own
 * big_canned_boy texture. Every other colour renders from KubeJS assets at
 * creativeengineering:textures/entity/canned_golem/<color>.png (mixin.CannedGolemRendererMixin).
 */
public interface DyeableCannedGolem {

    DyeColor DEFAULT_COLOR = DyeColor.RED;

    DyeColor ce$getColor();

    void ce$setColor(DyeColor color);

    /** Texture for a colour, or null for the default colour (use the mod's own texture). */
    static ResourceLocation texture(DyeColor color) {
        return color == DEFAULT_COLOR ? null : Textures.BY_COLOR[color.getId()];
    }

    final class Textures {
        private static final ResourceLocation[] BY_COLOR = new ResourceLocation[DyeColor.values().length];

        static {
            for (DyeColor color : DyeColor.values()) {
                BY_COLOR[color.getId()] = new ResourceLocation("creativeengineering",
                        "textures/entity/canned_golem/" + color.getName() + ".png");
            }
        }

        private Textures() {
        }
    }
}
