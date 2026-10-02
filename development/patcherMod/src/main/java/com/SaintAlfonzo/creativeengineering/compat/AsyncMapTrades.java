package com.SaintAlfonzo.creativeengineering.compat;

import brightspark.asynclocator.AsyncLocator;
import brightspark.asynclocator.logic.CommonLogic;
import brightspark.asynclocator.logic.MerchantLogic;
import brightspark.asynclocator.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modded explorer-map villager trades through Async Locator:
 *   - Lightman's Currency ItemsForMapTrade (the Cashier's ocean and woodland maps, from
 *     config/trades/lightmanscurrency/custom_cashier_trades.json), see LCItemsForMapTradeMixin
 *   - Cloud Storage SellingMap (the Balloon Salesman's big balloon and sky temple maps), see CSSellingMapMixin
 *
 * THE BUG:
 *   Both listings call findNearestMapStructure on the server thread from getOffer. In unexplored terrain that
 *   loads chunks synchronously up to 100 chunks out: a scratch server's /dumptrades froze the tick for 60 s
 *   until the watchdog killed it (2026-10-02, LC), and Cloud Storage's pair alone held a tick for ~20 s. A
 *   villager reaching that trade level on the live server does the same. Async Locator only patches vanilla's
 *   TreasureMapForEmeralds, so these never reached it.
 *
 * THE FIX (same flow as Async Locator's own TreasureMapForEmeraldsMixin):
 *   return the offer at once with Async Locator's pending map ("Working...", and its SlotMixin stops anyone
 *   taking it), run the search on Async Locator's thread, then fill in the map on the server thread and apply
 *   the mod's own finishing touch (LC locks its maps, Cloud Storage tints them). Nothing found: Async Locator
 *   removes the offer or marks it out of stock (its removeOffer config). A trader that has never been in a
 *   world one server task later (the /dumptrades sample villagers) gets the pending map with no search:
 *   nothing could ever hold that offer.
 *   Async Locator's villagerTradeEnabled config switches this off along with its vanilla patch; then the
 *   mods' original synchronous code runs.
 */
public final class AsyncMapTrades {
	private AsyncMapTrades() {}

	/** Starts the search and fills in the map when it finishes. */
	private interface Search {
		void start(ServerLevel level, BlockPos from, Consumer<BlockPos> onServerThread);
	}

	/** Lightman's Currency: the offer to return in place of LC's, or null to let LC's own code run. */
	public static MerchantOffer lightmansCurrency(Entity trader, ItemStack price1, ItemStack price2,
			TagKey<Structure> destination, String displayName, MapDecoration.Type decoration, int maxTrades, int xp,
			float priceMult) {
		return offer(trader, displayName, decoration,
				(level, from, then) -> AsyncLocator.locate(level, destination, from, 100, true).thenOnServerThread(then),
				(level, map) -> MapItem.lockMap(level, map),
				map -> new MerchantOffer(price1, price2, map, maxTrades, xp, priceMult));
	}

	/** Cloud Storage: the offer to return in place of SellingMap's, or null to let its own code run. */
	public static MerchantOffer cloudStorage(Entity trader, ResourceKey<Structure> structure, int mapColor,
			ItemStack priceA, ItemStack priceB, String displayName, MapDecoration.Type decoration, int maxUses, int xp) {
		if (!(trader != null && trader.level instanceof ServerLevel level)
				|| !level.getServer().getWorldData().worldGenSettings().generateStructures()) {
			return null; // SellingMap returns no offer here either
		}
		HolderSet<Structure> target = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY)
				.getHolder(structure).<HolderSet<Structure>>map(HolderSet::direct).orElse(null);
		if (target == null) return null;
		return offer(trader, displayName, decoration,
				(lvl, from, then) -> AsyncLocator.locate(lvl, target, from, 100, false)
						.thenOnServerThread(found -> then.accept(found == null ? null : found.getFirst())),
				(lvl, map) -> map.getOrCreateTagElement("display").putInt("MapColor", mapColor),
				map -> new MerchantOffer(priceA, priceB, map, maxUses, xp, 0.2F));
	}

	private static MerchantOffer offer(Entity trader, String displayName, MapDecoration.Type decoration, Search search,
			BiConsumer<ServerLevel, ItemStack> finish, Function<ItemStack, MerchantOffer> build) {
		if (trader == null || !(trader.level instanceof ServerLevel level) || !Services.CONFIG.villagerTradeEnabled()) {
			return null;
		}
		ItemStack map = CommonLogic.createEmptyMap();
		if (trader instanceof AbstractVillager merchant) {
			// Decided one server task later, and "has ever been in a world" rather than "is in it now":
			// - mods fill a new villager's offers from EntityJoinLevelEvent, before Forge marks it added;
			// - Minecraft Comes Alive discards every vanilla villager, builds its offers, and hands that same
			//   MerchantOffers object to the MCA villager replacing it (SpawnQueue), so the map we fill in is the
			//   one the new villager trades.
			// Only a trader that was never added (the /dumptrades sample villagers) skips the search.
			MinecraftServer server = level.getServer();
			server.tell(new TickTask(server.getTickCount(), () -> {
				if (!merchant.isAddedToWorld() && !merchant.isRemoved()) return;
				search.start(level, merchant.blockPosition(), pos -> {
					// Fills in the map (or drops the offer) and resends the offers to a player trading right now.
					MerchantLogic.handleLocationFound(level, merchant, map, displayName, decoration, pos);
					if (pos != null) finish.accept(level, map); // after: lockMap needs the map's data to exist
				});
			}));
		}
		return build.apply(map);
	}
}
