package com.SaintAlfonzo.creativecompat.corpse;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.DummyPlayer;
import de.maxhenkel.corpse.entities.DummySkeleton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Client drawing for corpses, called from {@code mixin/CorpseRendererMixin} in place of the Corpse mod's own body
 * draw (the name tag is still drawn by the mod). Same pose as the mod; two differences:
 * <ul>
 * <li>The dummy player or skeleton wears the corpse's current equipment each frame (the mod caches a dummy dressed
 * once, so armor taken out kept showing; ruling 4).</li>
 * <li>A withered corpse ({@link CorpseState#FLAG_WITHERED}, Nether) draws the skeleton with the vanilla wither skeleton
 * texture at normal skeleton size (ruling 20), read from its vanilla path, so resource packs apply.</li>
 * <li>While the dummy player is drawn, {@link #renderingFor} names its corpse, so {@link CorpseSectionsClient} can
 * dress it in the corpse's own cosmetic armor (plan_corpses.md 3.6 item 6) and fill its curios (round 2).</li>
 * <li>The skeleton wears the corpse's cosmetic armor over its real armor, and {@link CorpseSkeletonLayer} draws its
 * curios and an elytra from a curio slot (plan_corpses.md 3.6 round 2).</li>
 * </ul>
 */
public final class CorpseRender {
	private static final ResourceLocation WITHER_TEXTURE = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

	private static final Map<CorpseEntity, DummyPlayer> PLAYERS = new WeakHashMap<>();
	private static final Map<CorpseEntity, DummySkeleton> SKELETONS = new WeakHashMap<>();
	private static SkeletonRenderer witherLook;
	/** The corpse whose dummy player or skeleton is being drawn (read by CorpseSectionsClient and CorpseSkeletonLayer). */
	private static CorpseEntity rendering;

	private CorpseRender() {}

	/** Called when the Corpse renderer is built (each resource reload). */
	public static void init(EntityRendererProvider.Context context) {
		witherLook = new SkeletonRenderer(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR) {
			@Override
			public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
				return WITHER_TEXTURE;
			}
		};
		witherLook.addLayer(new CorpseSkeletonLayer<>(witherLook, context.getModelSet()));
	}

	public static void render(CorpseEntity corpse, float entityYaw, PoseStack pose, MultiBufferSource buffer, int light) {
		Minecraft mc = Minecraft.getInstance();
		pose.pushPose();
		pose.mulPose(Vector3f.YP.rotationDegrees(-corpse.getYRot()));
		if (Main.SERVER_CONFIG.spawnCorpseOnFace.get()) {
			pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			pose.translate(0.0, -1.0, -0.125625);
		} else {
			pose.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			pose.translate(0.0, -1.0, 0.125625);
		}
		NonNullList<ItemStack> equipment = corpse.getEquipment();
		if (corpse.isSkeleton()) {
			DummySkeleton skeleton = SKELETONS.computeIfAbsent(corpse, c -> new DummySkeleton(c.level, c.getEquipment()));
			dress(skeleton, CorpseSectionsClient.withCosmetics(corpse, equipment));
			boolean withered = corpse instanceof CorpseState s && s.creativecompat$has(CorpseState.FLAG_WITHERED);
			rendering = corpse;
			try {
				if (withered && witherLook != null) {
					witherLook.render(skeleton, entityYaw, 1.0F, pose, buffer, light);
				} else {
					mc.getEntityRenderDispatcher().getRenderer(skeleton).render(skeleton, entityYaw, 1.0F, pose, buffer, light);
				}
			} finally {
				rendering = null;
			}
		} else {
			DummyPlayer player = PLAYERS.computeIfAbsent(corpse, c -> new DummyPlayer((ClientLevel) c.level,
					new GameProfile(c.getCorpseUUID().orElse(new UUID(0L, 0L)), c.getCorpseName()), c.getEquipment(), c.getCorpseModel()));
			dress(player, equipment);
			rendering = corpse;
			try {
				mc.getEntityRenderDispatcher().getRenderer(player).render(player, 0.0F, 1.0F, pose, buffer, light);
			} finally {
				rendering = null;
			}
		}
		pose.popPose();
	}

	/** The corpse being drawn when {@code dummy} is its player model, else null. */
	public static CorpseEntity renderingFor(DummyPlayer dummy) {
		CorpseEntity corpse = rendering;
		return corpse != null && PLAYERS.get(corpse) == dummy ? corpse : null;
	}

	/** The corpse being drawn when {@code dummy} is its skeleton model, else null. */
	public static CorpseEntity renderingFor(DummySkeleton dummy) {
		CorpseEntity corpse = rendering;
		return corpse != null && SKELETONS.get(corpse) == dummy ? corpse : null;
	}

	private static void dress(LivingEntity dummy, NonNullList<ItemStack> equipment) {
		if (!Main.SERVER_CONFIG.renderEquipment.get()) return;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.ordinal() >= equipment.size()) continue;
			ItemStack want = equipment.get(slot.ordinal());
			if (!ItemStack.matches(dummy.getItemBySlot(slot), want)) dummy.setItemSlot(slot, want);
		}
	}
}
