package com.SaintAlfonzo.creativedelight.farmers.mixin;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Farmer villagers seek out the blocks in {@code [villagers] farmlandBlocks} as well as Farmland (1.3.4
 * VillagersTargetRichSoilMixin, there every FarmBlock; here the configured list). The sensor fills the farmer's
 * secondary job sites from the profession's block set; vanilla HarvestFarmland already plants and harvests on any
 * FarmBlock once the villager stands beside it, so this is the missing half.
 */
@Mixin(SecondaryPoiSensor.class)
public class VillagersTargetFarmlandMixin {

	@Redirect(method = "m_5578_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;)V",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;contains(Ljava/lang/Object;)Z", remap = false),
			remap = false)
	private boolean creativedelight$extraFarmland(ImmutableSet<?> set, Object block, ServerLevel level, Villager villager) {
		if (set.contains(block)) return true;
		return villager.getVillagerData().getProfession() == VillagerProfession.FARMER
				&& FDBackportConfig.villagerFarmland().contains(block);
	}
}
