package com.SaintAlfonzo.creativecompat.compat.jei.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Item slot renderer for the world tabs (TODO jei/jei-coverage #19, #22). Stacks {@link WorldRow} marks are drawn as
 * what they stand for; every other stack, and any marked one that can't be drawn, goes to JEI's own item renderer:
 * <ul>
 *   <li>{@link #ENTITY_TAG} (spawn egg or named name tag): the mob, feet on the slot floor, turning slowly. A mob
 *       that can't be created on the client or throws while drawing falls back to the egg for the rest of the
 *       session; namespaces in {@link #SKIP_ENTITY_MODS} always do.</li>
 *   <li>{@link #BLOCK_TAG} (a Barrier named after an itemless block): the block's model in the GUI block pose, at
 *       its highest {@code age} so crops show grown. Blocks with no model (invisible, block-entity drawn) stay a
 *       Barrier.</li>
 * </ul>
 * The tooltip always comes from the stack, so it still names the block or mob.
 */
public class WorldSlotRenderer implements IIngredientRenderer<ItemStack> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String BLOCK_TAG = "CreativeEngineeringJeiBlock";
    public static final String ENTITY_TAG = "CreativeEngineeringJeiEntity";

    /** Mobs never drawn: MCA villagers build their look from server-side genetics. */
    private static final Set<String> SKIP_ENTITY_MODS = Set.of("mca");

    // vanilla block/block gui transform, for models that have none (as WthitBlockModelIcon)
    private static final ItemTransform BLOCK_GUI = new ItemTransform(
            new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f));

    private final IIngredientRenderer<ItemStack> items;
    private final Map<EntityType<?>, Entity> entities = new HashMap<>();
    private final Set<EntityType<?>> failed = new HashSet<>();

    public WorldSlotRenderer(IIngredientRenderer<ItemStack> items) {
        this.items = items;
    }

    /** Whether any stack in the slot needs this renderer. */
    public static boolean wanted(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            CompoundTag tag = stack.getTag();
            if (tag != null && (tag.contains(BLOCK_TAG) || tag.contains(ENTITY_TAG))) return true;
        }
        return false;
    }

    @Override
    public void render(PoseStack pose, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ENTITY_TAG) && renderEntity(pose, tag.getString(ENTITY_TAG))) return;
        if (tag != null && tag.contains(BLOCK_TAG) && renderBlock(pose, tag.getString(BLOCK_TAG))) return;
        items.render(pose, stack);
    }

    @Override
    public List<Component> getTooltip(ItemStack stack, TooltipFlag flag) {
        return items.getTooltip(stack, flag);
    }

    private boolean renderEntity(PoseStack pose, String id) {
        Minecraft mc = Minecraft.getInstance();
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
        if (type == null || mc.level == null || failed.contains(type)) return false;
        if (SKIP_ENTITY_MODS.contains(ForgeRegistries.ENTITY_TYPES.getKey(type).getNamespace())) return false;
        Entity entity = entities.get(type);
        if (entity == null || entity.level != mc.level) {
            try {
                entity = type.create(mc.level);
            } catch (RuntimeException e) {
                entity = null;
            }
            if (entity == null) {
                failed.add(type);
                return false;
            }
            entities.put(type, entity);
        }

        AABB box = entity.getBoundingBox();
        float size = (float) Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize()));
        float scale = 15.0F / Math.max(size, 0.25F);
        float yaw = (Util.getMillis() % 7200L) / 20.0F; // one turn every 7.2 s
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            living.yBodyRot = yaw;
            living.yBodyRotO = yaw;
            living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
        }

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        pose.pushPose();
        try {
            pose.translate(8, 16, 50);
            pose.scale(scale, scale, scale);
            pose.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            Lighting.setupForEntityInInventory();
            dispatcher.overrideCameraOrientation(Quaternion.ONE);
            dispatcher.setRenderShadow(false);
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, pose, buffers, 15728880);
            buffers.endBatch();
            return true;
        } catch (RuntimeException e) {
            buffers.endBatch();
            failed.add(type);
            LOGGER.warn("[jei_world] can't draw {}, showing its egg: {}", id, e.toString());
            return false;
        } finally {
            dispatcher.setRenderShadow(true);
            Lighting.setupFor3DItems();
            pose.popPose();
        }
    }

    private boolean renderBlock(PoseStack pose, String id) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        if (block == null) return false;
        BlockState state = grown(block.defaultBlockState());
        if (state.getRenderShape() != RenderShape.MODEL) return false;
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        if (model == mc.getModelManager().getMissingModel()) return false;

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        PoseStack view = RenderSystem.getModelViewStack();
        view.pushPose();
        view.mulPoseMatrix(pose.last().pose());
        view.translate(8, 8, 100);
        view.scale(1, -1, 1);
        view.scale(16, 16, 16);
        RenderSystem.applyModelViewMatrix();
        try {
            PoseStack model3d = new PoseStack();
            if (model.getTransforms().getTransform(ItemTransforms.TransformType.GUI) == ItemTransform.NO_TRANSFORM) {
                BLOCK_GUI.apply(false, model3d);
            } else {
                ForgeHooksClient.handleCameraTransforms(model3d, model, ItemTransforms.TransformType.GUI, false);
            }
            model3d.translate(-0.5, -0.5, -0.5);
            Lighting.setupFor3DItems();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            mc.getBlockRenderer().renderSingleBlock(state, model3d, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY, null);
            buffers.endBatch();
        } finally {
            RenderSystem.enableDepthTest();
            view.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        return true;
    }

    /** The state at its highest {@code age}, so a crop shows grown. */
    private static BlockState grown(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && property.getName().equals("age")) {
                int max = age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                return state.setValue(age, max);
            }
        }
        return state;
    }
}
