package net.satisfy.foundation.seat;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Renders nothing, the chair entity is invisible. */
public class ChairRenderer extends EntityRenderer<ChairEntity> {
    public ChairRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public boolean shouldRender(@NotNull ChairEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    @SuppressWarnings("all")
    public ResourceLocation getTextureLocation(ChairEntity entity) {
        return null;
    }
}
