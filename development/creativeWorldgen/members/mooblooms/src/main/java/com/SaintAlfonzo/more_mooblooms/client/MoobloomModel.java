package com.SaintAlfonzo.more_mooblooms.client;

import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * The cow model plus the sheep's grazing animation (D18): while eating grass the head drops and chews.
 * The head pivot is 4 px from the top on a cow (6 on a sheep); it drops 9 px like the sheep's.
 */
public class MoobloomModel extends CowModel<Moobloom> {

    private static final float HEAD_Y = 4.0F;
    private float headXRot;

    public MoobloomModel(ModelPart root) {
        super(root);
    }

    @Override
    public void prepareMobModel(Moobloom moobloom, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(moobloom, limbSwing, limbSwingAmount, partialTick);
        head.y = HEAD_Y + moobloom.getHeadEatPositionScale(partialTick) * 9.0F;
        headXRot = moobloom.getHeadEatAngleScale(partialTick);
    }

    @Override
    public void setupAnim(Moobloom moobloom, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(moobloom, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.xRot = headXRot;
    }
}
