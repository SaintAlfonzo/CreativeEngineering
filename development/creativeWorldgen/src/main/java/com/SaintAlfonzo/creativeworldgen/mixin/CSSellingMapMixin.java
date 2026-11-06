package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.compat.AsyncMapTrades;
import com.github.alexthe668.cloudstorage.block.CSBlockRegistry;
import com.github.alexthe668.cloudstorage.entity.villager.SellingMap;
import com.github.alexthe668.cloudstorage.item.CSItemRegistry;
import com.github.alexthe668.cloudstorage.world.CSStructureRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sends Cloud Storage's Balloon Salesman maps (big balloon, sky temple) through Async Locator instead of a
 * structure search on the server thread. See {@link AsyncMapTrades}. The prices, map colours and offer
 * numbers are copied from SellingMap#getOffer (Cloud Storage 1.3.4). getOffer implements
 * VillagerTrades.ItemListing#getOffer, so in the shipped jar it carries the SRG name m_213663_.
 */
@Mixin(value = SellingMap.class, remap = false)
public class CSSellingMapMixin {
	@Shadow @Final private int emeraldCost;
	@Shadow @Final private boolean balloon;
	@Shadow @Final private String displayName;
	@Shadow @Final private MapDecoration.Type destinationType;
	@Shadow @Final private int maxUses;
	@Shadow @Final private int villagerXp;

	@Inject(method = "m_213663_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/trading/MerchantOffer;",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$asyncMap(Entity trader, RandomSource rand, CallbackInfoReturnable<MerchantOffer> cir) {
		MerchantOffer offer = AsyncMapTrades.cloudStorage(trader,
				balloon ? CSStructureRegistry.BIG_BALLOON_KEY : CSStructureRegistry.SKY_TEMPLE_KEY,
				balloon ? 15149353 : 10924991,
				new ItemStack(Items.EMERALD, emeraldCost),
				new ItemStack(balloon ? CSItemRegistry.BALLOON.get() : CSBlockRegistry.CLOUD.get()),
				displayName, destinationType, maxUses, villagerXp);
		if (offer != null) cir.setReturnValue(offer);
	}
}
