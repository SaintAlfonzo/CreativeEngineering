package com.SaintAlfonzo.creativecompat.surface;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

/**
 * No field of view change from walking speeds set by blocks (plan 1.3, phase 0 brief 6). Client only.
 *
 * <p>Vanilla widens the view with {@code (speed / walkSpeed + 1) / 2}. BlockRunner (option
 * {@code disable_field_of_view_changes}) recomputes that part without its own modifier, but only while its modifier
 * is present, so the pass-through modifier ({@link PassThrough#MODIFIER_UUID}) would still show. This replaces
 * BlockRunner's recomputation with one that skips both: {@code mixin/PassFovMixin} at the same point as BlockRunner's
 * own {@code @ModifyVariable} (the store after the speed multiply in {@code AbstractClientPlayer#m_108565_}), and
 * {@code mixin/BlockRunnerFovMixin} keeps BlockRunner's handler from overwriting it (its
 * {@code shouldRemoveBlockSpeedModifier} answers false unless asked from here), so the order the two handlers run in
 * does not matter. BlockRunner's UUID is skipped only when BlockRunner itself would skip it (its option on, not
 * scoping in first person); the pass-through one always (not scoping). Sprinting and potions still change the view.
 */
public final class PassFov {

	private PassFov() {}

	private static final boolean BLOCKRUNNER = ModList.get().isLoaded("blockrunner");

	/** Set while {@link #adjust} asks BlockRunner's own check, so {@code BlockRunnerFovMixin} lets it answer. */
	private static boolean querying;

	public static boolean querying() {
		return querying;
	}

	/** The speed part of the field of view {@code f} (as stored after vanilla's multiply), without block speeds. */
	public static float adjust(Player player, float f) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) return f;
		boolean own = speed.getModifier(PassThrough.MODIFIER_UUID) != null
				&& !(Minecraft.getInstance().options.getCameraType().isFirstPerson() && player.isScoping());
		boolean runner = false;
		if (BLOCKRUNNER) {
			querying = true;
			try {
				runner = BlockRunnerFov.shouldRemove(player);
			} finally {
				querying = false;
			}
		}
		if (!own && !runner) return f;
		double value = valueSkipping(speed, own ? PassThrough.MODIFIER_UUID : null,
				runner ? BlockRunnerFov.uuid() : null);
		float walking = player.getAbilities().getWalkingSpeed();
		float out = PassMath.fovSpeedPart(player.getAbilities().flying, value, walking);
		return walking == 0.0F || Float.isNaN(out) || Float.isInfinite(out) ? f : out;
	}

	/** The attribute's value without the modifiers with these UUIDs (null = none), clamped like the attribute. */
	static double valueSkipping(AttributeInstance speed, UUID skipA, UUID skipB) {
		double[] add = amounts(speed, AttributeModifier.Operation.ADDITION, skipA, skipB);
		double[] base = amounts(speed, AttributeModifier.Operation.MULTIPLY_BASE, skipA, skipB);
		double[] total = amounts(speed, AttributeModifier.Operation.MULTIPLY_TOTAL, skipA, skipB);
		return speed.getAttribute().sanitizeValue(PassMath.attributeValue(speed.getBaseValue(), add, base, total));
	}

	private static double[] amounts(AttributeInstance speed, AttributeModifier.Operation op, UUID skipA, UUID skipB) {
		return speed.getModifiers().stream().filter(m -> m.getOperation() == op)
				.filter(m -> !m.getId().equals(skipA) && !m.getId().equals(skipB))
				.mapToDouble(AttributeModifier::getAmount).toArray();
	}
}
