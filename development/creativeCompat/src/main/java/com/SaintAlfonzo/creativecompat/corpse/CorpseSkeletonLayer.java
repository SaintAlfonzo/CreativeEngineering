package com.SaintAlfonzo.creativecompat.corpse;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.DummySkeleton;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;

/**
 * Curios on a corpse skeleton (feature_writeup/plan_corpses.md 3.6 round 2). Added to the vanilla skeleton renderer
 * and to Compat's wither look; draws only for the Corpse mod's {@link DummySkeleton} while {@link CorpseRender} draws
 * its corpse. Each shown curio (cosmetic first, render-toggled ones left out) goes through its {@code ICurioRenderer};
 * an elytra in a curio slot (Elytra Slot's back slot) with no curio renderer is drawn by a vanilla elytra layer with
 * the elytra in the skeleton's chest slot for that one draw, so it shows over a chestplate as Elytra Slot does on
 * players. The wings use the vanilla elytra texture.
 */
public class CorpseSkeletonLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final RenderLayerParent<T, M> parent;
	private final ElytraLayer<T, M> elytra;

	public CorpseSkeletonLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
		super(parent);
		this.parent = parent;
		this.elytra = new ElytraLayer<>(parent, models) {
			@Override
			public boolean shouldRender(ItemStack stack, T entity) {
				return stack.getItem() instanceof ElytraItem;
			}
		};
	}

	@Override
	public void render(PoseStack pose, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!(entity instanceof DummySkeleton dummy) || !Main.SERVER_CONFIG.renderEquipment.get()) return;
		CorpseEntity corpse = CorpseRender.renderingFor(dummy);
		if (corpse == null) return;
		for (CorpseSectionsClient.Worn w : CorpseSectionsClient.shownCurios(corpse)) {
			boolean drawn = false;
			if (CorpseSections.CURIOS && CorpseCuriosClient.hasRenderer(w.stack())) {
				CorpseCuriosClient.render(w, entity, parent, pose, buffer, light, limbSwing, limbSwingAmount, partialTicks,
						ageInTicks, netHeadYaw, headPitch);
				drawn = true;
			}
			if (!drawn && w.stack().getItem() instanceof ElytraItem) {
				ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
				entity.setItemSlot(EquipmentSlot.CHEST, w.stack());
				try {
					elytra.render(pose, buffer, light, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
				} finally {
					entity.setItemSlot(EquipmentSlot.CHEST, chest);
				}
			}
		}
	}
}
