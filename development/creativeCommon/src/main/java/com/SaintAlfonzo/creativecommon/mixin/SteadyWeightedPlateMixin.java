package com.SaintAlfonzo.creativecommon.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativecommon.effect.SteadyRules;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.phys.AABB;

/**
 * Steady ({@code food-tuning #51}, {@link SteadyRules}): the weighted pressure plates do not count its holder. In 1.19.2
 * their {@code getSignalStrength} ({@code m_6693_}) counts every entity from {@code Level.getEntitiesOfClass}
 * ({@code m_45976_}) without asking {@code isIgnoringBlockTriggers}, so the list is filtered here.
 */
@Mixin(value = WeightedPressurePlateBlock.class, remap = false)
public abstract class SteadyWeightedPlateMixin {

	@Redirect(method = "m_6693_", remap = false, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/world/level/Level;m_45976_(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	private <T extends net.minecraft.world.entity.Entity> List<T> creativecommon$withoutSteady(Level level, Class<T> type, AABB box) {
		List<T> found = level.getEntitiesOfClass(type, box);
		if (found.isEmpty()) return found;
		return found.stream().filter(e -> !SteadyRules.isSteady(e)).toList();
	}
}
