package com.SaintAlfonzo.creativeengineering.compat;

import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WTHIT plugin: draws the looked-at block's own model as the overlay icon when its item can't.
 *
 * <p>WTHIT's core icon is {@code Block#getCloneItemStack(level, pos, state)} (the vanilla 3-arg
 * method, not Forge's pick-block one), so for a placed Display Delight food it shows that block's
 * own item. 70 of DD 1.2.0's 191 food blocks have no item (most stackable plates: cookies, burgers,
 * the ad_/cd_/df_ compat plates), which leaves the icon empty. A block item whose model is missing
 * would show the purple/black cube; tools/gen_display_delight_models.py adds those models, and this
 * also covers any that a DD update leaves out.
 *
 * <p>Priority 1000 runs ahead of WTHIT's core block icon (1100) and returns null for every other
 * case, so the core icon still handles them. Facing is pinned to north so the icon matches DD's
 * item models; the stack count is kept, so a plate of three cookies shows three.
 */
public final class WthitBlockModelIcon implements IWailaClientPlugin, IBlockComponentProvider {

    private static final Set<String> NAMESPACES = Set.of("displaydelight");
    // vanilla block/block gui transform, for the few DD models that have none
    private static final ItemTransform BLOCK_GUI = new ItemTransform(
            new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f));

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.icon(this, Block.class, 1000);
    }

    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null || !NAMESPACES.contains(id.getNamespace())) {
            return null;
        }
        if (hasItemIcon(state.getBlock().getCloneItemStack(accessor.getLevel(), accessor.getPosition(), state))) {
            return null;
        }
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            state = state.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        }
        return new BlockModelComponent(state);
    }

    private static boolean hasItemIcon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.getItemRenderer().getModel(stack, null, null, 0) != mc.getModelManager().getMissingModel();
    }

    /** An 18x18 icon slot like WTHIT's ItemComponent, drawn the way ItemRenderer#renderGuiItem draws a block item. */
    private record BlockModelComponent(BlockState state) implements ITooltipComponent {

        @Override
        public int getWidth() {
            return 18;
        }

        @Override
        public int getHeight() {
            return 18;
        }

        @Override
        public void render(PoseStack matrices, int x, int y, float delta) {
            Minecraft mc = Minecraft.getInstance();
            BakedModel model = mc.getBlockRenderer().getBlockModel(state);
            if (model == mc.getModelManager().getMissingModel()) {
                return;
            }
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1, 1, 1, 1);

            PoseStack view = RenderSystem.getModelViewStack();
            view.pushPose();
            view.translate(x + 1, y + 1, 100);
            view.translate(8, 8, 0);
            view.scale(1, -1, 1);
            view.scale(16, 16, 16);
            RenderSystem.applyModelViewMatrix();

            PoseStack pose = new PoseStack();
            if (model.getTransforms().getTransform(ItemTransforms.TransformType.GUI) == ItemTransform.NO_TRANSFORM) {
                BLOCK_GUI.apply(false, pose);
            } else {
                ForgeHooksClient.handleCameraTransforms(pose, model, ItemTransforms.TransformType.GUI, false);
            }
            pose.translate(-0.5, -0.5, -0.5);
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            mc.getBlockRenderer().renderSingleBlock(state, pose, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY, null);
            buffers.endBatch();

            RenderSystem.enableDepthTest();
            view.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }
}
