package com.SaintAlfonzo.creativecompat.corpse;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.DummyPlayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * Curios on the corpse models (feature_writeup/plan_corpses.md 3.6 round 2). Only this class and CorpseCurios touch
 * the Curios API; callers check {@link CorpseSections#CURIOS} first.
 * <ul>
 * <li>{@link #fill}: the Corpse mod's dummy player is a client player, so Curios attaches a curios handler to it, empty
 * on the client until a sync that never comes (the dummy is not in the level). The handler is filled from the corpse's
 * synced curio list, one slot type per recorded slot id, sized to the highest recorded index, the player's render
 * toggles kept. Curios' own player layer then draws them, and Elytra Slot's layer draws an elytra in the back slot,
 * exactly as on the living player. Refilled only when the server resyncs the list (shown items follow contents).</li>
 * <li>{@link #render}: one curio drawn through its registered {@code ICurioRenderer} on another entity (the skeleton,
 * which carries no curios handler); called by {@link CorpseSkeletonLayer}.</li>
 * </ul>
 */
public final class CorpseCuriosClient {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<DummyPlayer, CompoundTag> FILLED = new WeakHashMap<>();
	private static boolean warned;

	private CorpseCuriosClient() {}

	public static void fill(DummyPlayer dummy, CorpseEntity corpse) {
		CompoundTag tag = CorpseSectionsClient.sectionTag(corpse);
		if (tag == null || FILLED.get(dummy) == tag) return;
		FILLED.put(dummy, tag);
		CuriosApi.getCuriosHelper().getCuriosHandler(dummy).ifPresent(handler -> {
			Map<String, Integer> sizes = new TreeMap<>();
			for (CorpseSectionsClient.Worn w : CorpseSectionsClient.worn(corpse)) sizes.merge(w.slot(), w.index() + 1, Math::max);
			Map<String, ICurioStacksHandler> curios = new TreeMap<>();
			sizes.forEach((id, size) -> curios.put(id, new CurioStacksHandler(handler, id, size, true, true)));
			for (CorpseSectionsClient.Worn w : CorpseSectionsClient.worn(corpse)) {
				ICurioStacksHandler type = curios.get(w.slot());
				if (type == null || w.index() < 0) continue;
				(w.cosmetic() ? type.getCosmeticStacks() : type.getStacks()).setStackInSlot(w.index(), w.stack());
				if (w.hidden() && w.index() < type.getRenders().size()) type.getRenders().set(w.index(), false);
			}
			handler.setCurios(curios);
		});
	}

	public static boolean hasRenderer(ItemStack stack) {
		return CuriosRendererRegistry.getRenderer(stack.getItem()).isPresent();
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void render(CorpseSectionsClient.Worn w, T entity,
			RenderLayerParent<T, M> parent, PoseStack pose, MultiBufferSource buffer, int light, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		CuriosRendererRegistry.getRenderer(w.stack().getItem()).ifPresent(renderer -> {
			pose.pushPose();
			try {
				renderer.render(w.stack(), new SlotContext(w.slot(), entity, w.index(), w.cosmetic(), true), pose, parent,
						buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
			} catch (RuntimeException e) {
				// a renderer written for players only (casts to PlayerModel and the like): skip it on the skeleton
				if (!warned) {
					warned = true;
					LOGGER.warn("[creativecompat] curio renderer for {} failed on a corpse skeleton; skipped", w.stack().getItem(), e);
				}
			} finally {
				pose.popPose();
			}
		});
	}
}
