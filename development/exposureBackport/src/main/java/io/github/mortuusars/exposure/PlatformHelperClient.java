package io.github.mortuusars.exposure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Upstream {@code PlatformHelperClient} with {@code PlatformHelperClientImpl} folded in. 1.19.2 has no standalone
 * {@code ModelResourceLocation}: extra models are registered and looked up by plain {@link ResourceLocation}.
 */
public class PlatformHelperClient {
    public static BakedModel getModel(ResourceLocation model) {
        return Minecraft.getInstance().getModelManager().getModel(model);
    }
}
