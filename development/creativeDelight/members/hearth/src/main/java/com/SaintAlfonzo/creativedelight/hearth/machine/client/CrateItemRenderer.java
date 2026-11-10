package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.CrateBlockEntity;
import com.SaintAlfonzo.creativedelight.hearth.machine.HHMachines;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** The crate item draws the same bottles as the placed block. */
public final class CrateItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static CrateItemRenderer instance;
    private final CrateBlockEntity dummy;
    private final CrateRenderer renderer;

    public static CrateItemRenderer instance() {
        if (instance == null) {
            Minecraft mc = Minecraft.getInstance();
            instance = new CrateItemRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return instance;
    }

    private CrateItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        BlockState state = HHMachines.CRATE.get().defaultBlockState();
        dummy = new CrateBlockEntity(BlockPos.ZERO, state);
        Minecraft mc = Minecraft.getInstance();
        renderer = new CrateRenderer(new BlockEntityRendererProvider.Context(dispatcher, mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), models, mc.font));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) dummy.loadItems(tag);
        else dummy.clearContent();
        pose.pushPose();
        pose.translate(0.0, 0.25, 0.0);
        mc.getBlockRenderer().renderSingleBlock(dummy.getBlockState(), pose, buffer, light, overlay);
        renderer.render(dummy, 0.0F, pose, buffer, light, overlay);
        pose.popPose();
    }
}
