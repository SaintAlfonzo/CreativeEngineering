package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixed jars ({@code SupplementariesJarTileMixin}) as an item: draws every stored item as a layer. Supplementaries
 * reads only {@code Items[0]} and draws it once per count; its read returns an empty tag here (count 0, nothing
 * drawn) and the TAIL inject draws all of {@code Items} in slot order, one layer per item counted, so a jar picked
 * up before mixing (one stack of 5) still shows 5.
 */
@Mixin(value = JarItemRenderer.class, remap = false)
public abstract class SupplementariesJarItemRendererMixin {

    @Redirect(method = "renderContent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/ListTag;m_128728_(I)Lnet/minecraft/nbt/CompoundTag;"), remap = false)
    private CompoundTag creativeengineering$skipFirstStack(ListTag list, int index) {
        return new CompoundTag();
    }

    @Inject(method = "renderContent", at = @At("TAIL"), remap = false)
    private void creativeengineering$renderLayers(CompoundTag tag, ItemTransforms.TransformType transformType,
                                                  PoseStack pose, MultiBufferSource buffer, int light, int overlay,
                                                  CallbackInfo ci) {
        if (!tag.contains("Items", Tag.TAG_LIST)) {
            return;
        }
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        if (items.isEmpty()) {
            return;
        }
        Random rand = new Random(420L);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(RotHlpr.XN90);
        pose.translate(0.0, 0.0, -0.5);
        float scale = 0.5714286F;
        pose.scale(scale, scale, scale);
        for (ItemStack stack : creativeengineering$bySlot(items)) {
            for (int n = 0; n < stack.getCount(); n++) {
                pose.mulPose(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
                pose.translate(0.0, 0.0, 1.0F / (16.0F * scale));
                BakedModel model = itemRenderer.getModel(stack, null, null, 0);
                itemRenderer.render(stack, ItemTransforms.TransformType.FIXED, true, pose, buffer, light, overlay, model);
            }
        }
        pose.popPose();
    }

    /** The saved stacks in slot order (the list is written in slot order, but sort anyway). */
    private static List<ItemStack> creativeengineering$bySlot(ListTag items) {
        List<CompoundTag> tags = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            tags.add(items.getCompound(i));
        }
        tags.sort(Comparator.comparingInt(t -> t.getByte("Slot") & 255));
        List<ItemStack> out = new ArrayList<>();
        for (CompoundTag t : tags) {
            ItemStack s = ItemStack.of(t);
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }
}
