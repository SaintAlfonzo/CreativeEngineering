package net.satisfy.vinery.forge;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.util.VillagerUtil;

import java.util.List;

/**
 * Port: Fabric's POI / profession helpers and TradeOfferHelper. The workstation is the Aging Barrel (1.5.5; 1.3.10
 * used the wine press). Trades come from {@code [villager]} in vinery-common.toml, read when Forge builds the trade
 * tables.
 */
public final class VineryVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, Vinery.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, Vinery.MOD_ID);

    public static final RegistryObject<PoiType> WINEMAKER_POI = POI_TYPES.register("winemaker_poi",
            () -> new PoiType(ImmutableSet.copyOf(ObjectRegistry.FERMENTATION_BARREL.get().getStateDefinition().getPossibleStates()), 1, 12));
    public static final RegistryObject<VillagerProfession> WINEMAKER = PROFESSIONS.register("winemaker",
            () -> new VillagerProfession("winemaker", holder -> holder.is(WINEMAKER_POI.getKey()), holder -> holder.is(WINEMAKER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FARMER));

    private VineryVillagers() {
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != WINEMAKER.get()) {
            return;
        }
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        for (int level = 1; level <= 5; level++) {
            List<VillagerTrades.ItemListing> listings = trades.get(level);
            for (String entry : VineryConfig.VILLAGER_TRADES.get(level - 1).get()) {
                VillagerTrades.ItemListing listing = parse(entry);
                if (listing != null) {
                    listings.add(listing);
                }
            }
        }
    }

    /** {@code item|BUY or SELL|price|count|maxUses|experience}. */
    private static VillagerTrades.ItemListing parse(String entry) {
        String[] parts = entry.split("\\|");
        if (parts.length != 6) {
            Vinery.LOGGER.warn("Ignoring winemaker trade '{}': expected item|BUY or SELL|price|count|maxUses|experience", entry);
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            Vinery.LOGGER.warn("Ignoring winemaker trade '{}': unknown item", entry);
            return null;
        }
        try {
            int price = Integer.parseInt(parts[2].trim());
            int count = Integer.parseInt(parts[3].trim());
            int maxUses = Integer.parseInt(parts[4].trim());
            int experience = Integer.parseInt(parts[5].trim());
            if (price < 1 || price > 99 || maxUses < 1) {
                Vinery.LOGGER.warn("Ignoring winemaker trade '{}': price must be 1-99 and maxUses at least 1", entry);
                return null;
            }
            return switch (parts[1].trim().toUpperCase(java.util.Locale.ROOT)) {
                case "BUY" -> new VillagerUtil.BuyForOneEmeraldFactory(item, price, maxUses, experience);
                // Port: upstream passed maxUses as the count here; the count field is used as written
                case "SELL" -> new VillagerUtil.SellItemFactory(item, price, count, maxUses, experience);
                default -> null;
            };
        } catch (NumberFormatException e) {
            Vinery.LOGGER.warn("Ignoring winemaker trade '{}': numbers expected", entry);
            return null;
        }
    }
}
