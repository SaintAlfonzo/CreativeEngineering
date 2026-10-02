package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.AsyncMapTrades;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.ItemsForMapTrade;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sends Lightman's Currency explorer-map trades through Async Locator instead of a structure search on the
 * server thread. See {@link AsyncMapTrades}.
 * getOffer implements VillagerTrades.ItemListing#getOffer, so in the shipped LC jar it carries the SRG name
 * m_213663_.
 */
@Mixin(value = ItemsForMapTrade.class, remap = false)
public class LCItemsForMapTradeMixin {
	@Shadow @Final protected ItemStack price1;
	@Shadow @Final protected ItemStack price2;
	@Shadow @Final protected TagKey<Structure> destination;
	@Shadow @Final protected String displayName;
	@Shadow @Final protected MapDecoration.Type mapDecorationType;
	@Shadow @Final protected int maxTrades;
	@Shadow @Final protected int xp;
	@Shadow @Final protected float priceMult;

	@Inject(method = "m_213663_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/trading/MerchantOffer;",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$asyncMap(Entity trader, RandomSource rand, CallbackInfoReturnable<MerchantOffer> cir) {
		MerchantOffer offer = AsyncMapTrades.lightmansCurrency(trader, price1, price2, destination, displayName, mapDecorationType,
				maxTrades, xp, priceMult);
		if (offer != null) cir.setReturnValue(offer);
	}
}
