package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.ModelData;

/**
 * WTHIT plugin: draws the looked-at block's own model as the overlay icon when its item can't.
 *
 * <p>WTHIT's core icon is {@code Block#getCloneItemStack(level, pos, state)} (the vanilla 3-arg
 * method, not Forge's pick-block one). A block with no pick item (the 2026-10-20 audit found 272: 70
 * Display Delight plates, Colored Water's 64 cauldrons, Immersive Posts' 30 post parts, Delightful's and
 * Central Kitchen's pies, Create belt funnels...) leaves the icon empty, and an item whose model is missing
 * shows the purple/black cube. Which blocks qualify is
 * {@link BlockModelIcons#verdict} (any namespace since 2026-10-20; {@code [wthit] iconExclude} opts out). Before the
 * model it tries {@code [wthit] iconItems} and Forge's hit-aware pick block.
 * tools/gen_display_delight_models.py still adds DD's missing item models, for JEI.
 *
 * <p>Priority 1000 runs ahead of WTHIT's core block icon (1100) and returns null for every other
 * case, so the core icon still handles them. Facing is pinned to north so the icon matches how block
 * items are drawn; the stack count is kept, so a plate of three cookies shows three.
 */
public final class WthitBlockModelIcon implements IWailaClientPlugin, IBlockComponentProvider {

    // vanilla block/block gui transform, for models that have none
    private static final ItemTransform BLOCK_GUI = new ItemTransform(
            new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f));

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.icon(this, Block.class, 1000);
    }

    @Override
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        ItemStack clone = state.getBlock().getCloneItemStack(accessor.getLevel(), accessor.getPosition(), state);
        BlockModelIcons.Verdict verdict = BlockModelIcons.verdict(state, clone);
        if (verdict == BlockModelIcons.Verdict.SUBSTITUTE) {
            return new ItemComponent(BlockModelIcons.substitute(state));
        }
        if (verdict == BlockModelIcons.Verdict.ITEM || verdict == BlockModelIcons.Verdict.EXCLUDED) {
            return null;
        }
        if (CEConfig.WTHIT.iconForgePick.get()) {
            ItemStack picked = forgePick(state, accessor);
            if (BlockModelIcons.hasItemIcon(picked)) {
                return new ItemComponent(picked);
            }
        }
        return verdict == BlockModelIcons.Verdict.MODEL && CEConfig.WTHIT.iconBlockModel.get()
                ? new BlockModelComponent(BlockModelIcons.iconState(state)) : null;
    }

    /**
     * Forge's pick block, the one middle-click uses: it gets the hit, so blocks that hold several things answer with
     * the part looked at (Supplementaries' skull pile: the upper or lower skull). WTHIT only asks the vanilla method.
     */
    private static ItemStack forgePick(BlockState state, IBlockAccessor accessor) {
        try {
            return state.getCloneItemStack(accessor.getHitResult(), accessor.getLevel(), accessor.getPosition(),
                    accessor.getPlayer());
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
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
