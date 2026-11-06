/*
 * Canned Goods, by BlueIvey. Licensed under the Academic Free License version 3.0
 * (NOTICE-upstream-AFL.txt in this jar).
 *
 * MODIFIED FILE (Attribution Notice, AFL 3.0 section 6): SaintAlfonzo modified this class of Canned Goods 1.19.2-3
 * on 2026-11-02 for the Creative Delight repack. Decompiled from the release jar and mapped to Mojang names;
 * getTextureLocation now returns the dyed texture (com.SaintAlfonzo.creativedelight.cannedgoods.CannedGolemDye),
 * marked "CE:" below. Everything else is the upstream code unchanged.
 */
package net.mcreator.cannedgoods.client.renderer;

import com.SaintAlfonzo.creativedelight.cannedgoods.CannedGolemDye;
import net.mcreator.cannedgoods.client.model.Modelcanned_golem;
import net.mcreator.cannedgoods.entity.CannedGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CannedGolemRenderer extends MobRenderer<CannedGolemEntity, Modelcanned_golem<CannedGolemEntity>> {
    public CannedGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new Modelcanned_golem<>(context.bakeLayer(Modelcanned_golem.LAYER_LOCATION)), 0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(CannedGolemEntity entity) {
        // CE: a dyed golem uses its colour's texture; red (the default) keeps the mod's own
        ResourceLocation dyed = CannedGolemDye.texture(entity.getColor());
        return dyed != null ? dyed : new ResourceLocation("canned_goods:textures/entities/big_canned_boy.png");
    }
}
