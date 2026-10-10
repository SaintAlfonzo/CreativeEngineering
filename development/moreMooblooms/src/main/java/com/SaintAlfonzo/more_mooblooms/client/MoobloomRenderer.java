package com.SaintAlfonzo.more_mooblooms.client;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cow model + {@link MoobloomFlowerLayer}. Texture: {@code <ns>:textures/entity/moobloom/<path>.png} of the
 * variant id, so a data-pack variant brings its texture in a resource pack at the same path.
 */
public class MoobloomRenderer extends MobRenderer<Moobloom, MoobloomModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(MoreMooblooms.ID, "moobloom"), "main");

    private static final Map<ResourceLocation, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();

    public MoobloomRenderer(EntityRendererProvider.Context context) {
        super(context, new MoobloomModel(context.bakeLayer(LAYER)), 0.7F);
        addLayer(new MoobloomFlowerLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public ResourceLocation getTextureLocation(Moobloom moobloom) {
        return TEXTURES.computeIfAbsent(moobloom.getVariant().id(),
                id -> new ResourceLocation(id.getNamespace(), "textures/entity/moobloom/" + id.getPath() + ".png"));
    }
}
