package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.perf.MobCapLevels;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The two places vanilla natural spawning reads a category's cap read it from {@link MobCapLevels#cap} instead
 * (perf.md 17.2): the dimension-wide cap in {@code SpawnState.canSpawnForCategory} and the per-player cap in
 * {@code LocalMobCapCalculator$MobCounts.canSpawn}. Each target has one of the two methods.
 */
@Mixin(targets = {"net.minecraft.world.level.NaturalSpawner$SpawnState",
		"net.minecraft.world.level.LocalMobCapCalculator$MobCounts"}, remap = false)
public abstract class MobCapMixin {

	// m_186548_ = SpawnState.canSpawnForCategory, m_186522_ = MobCounts.canSpawn, m_21608_ = getMaxInstancesPerChunk
	@Redirect(method = {"m_186548_", "m_186522_"}, remap = false,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;m_21608_()I", remap = false))
	private int creativeengineering$levelCap(MobCategory category) {
		return MobCapLevels.cap(category);
	}
}
