package com.SaintAlfonzo.babymobs.mixin.client;

import com.SaintAlfonzo.babymobs.client.family.UndeadBabies;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.1 baby armor for the humanoid babies (client/family/UndeadBabies): vanilla armor is drawn with the baked baby
 * armor piece for the slot and the 26.1 {@code equipment/humanoid_baby} texture (no trims, as 26.1). Modded armor has
 * no baby texture: it keeps 1.19.2's armor model, posed in the adult frame and drawn at young = true (the old
 * half-size baby armor). SRG names, remap = false: m_117118_ renderArmorPiece, m_117125_ setPartVisibility.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class UndeadArmorLayerMixin {

	private static final String PIECE = "m_117118_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V";

	@Inject(method = PIECE, at = @At("HEAD"), cancellable = true, remap = false)
	private void babyMobs$babyArmor(PoseStack pose, MultiBufferSource buffers, LivingEntity entity, EquipmentSlot slot, int light,
									HumanoidModel<?> adultModel, CallbackInfo ci) {
		UndeadBabies.ArmorSet set = UndeadBabies.activeArmor();
		if (set == null) return;
		ItemStack stack = entity.getItemBySlot(slot);
		if (!(stack.getItem() instanceof ArmorItem armor) || armor.getSlot() != slot) return;
		ResourceLocation texture = UndeadBabies.babyArmorTexture(stack, false);
		if (texture == null) return;
		ci.cancel();
		if (!(((RenderLayer<?, ?>) (Object) this).getParentModel() instanceof HumanoidModel<?> body)) return;
		UndeadBabies.BabyArmorModel model = set.model(slot);
		model.follow(body);
		boolean foil = stack.hasFoil();
		if (armor instanceof DyeableLeatherItem dyeable) {
			int c = dyeable.getColor(stack);
			babyMobs$draw(pose, buffers, light, foil, model, texture, (c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F);
			babyMobs$draw(pose, buffers, light, foil, model, UndeadBabies.babyArmorTexture(stack, true), 1.0F, 1.0F, 1.0F);
		} else {
			babyMobs$draw(pose, buffers, light, foil, model, texture, 1.0F, 1.0F, 1.0F);
		}
	}

	private static void babyMobs$draw(PoseStack pose, MultiBufferSource buffers, int light, boolean foil,
									  UndeadBabies.BabyArmorModel model, ResourceLocation texture, float r, float g, float b) {
		VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(texture), false, foil);
		model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
	}

	/** Modded armor on a remodelled baby: back to the adult frame and the 1.19.2 young flag. */
	@Inject(method = PIECE, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;m_117125_(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V"),
			remap = false)
	private void babyMobs$oldBabyArmor(PoseStack pose, MultiBufferSource buffers, LivingEntity entity, EquipmentSlot slot, int light,
									   HumanoidModel<?> adultModel, CallbackInfo ci) {
		if (UndeadBabies.activeArmor() == null) return;
		if (((RenderLayer<?, ?>) (Object) this).getParentModel() instanceof UndeadBabies.Rebased rebased
				&& ((RenderLayer<?, ?>) (Object) this).getParentModel() instanceof HumanoidModel<?> body) {
			rebased.rebase().copyAsAdult(body, adultModel);
		}
		adultModel.young = true;
	}
}
