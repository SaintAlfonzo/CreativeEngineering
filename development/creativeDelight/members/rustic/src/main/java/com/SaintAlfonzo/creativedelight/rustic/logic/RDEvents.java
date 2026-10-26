package com.SaintAlfonzo.creativedelight.rustic.logic;

import com.SaintAlfonzo.creativedelight.rustic.content.PancakeStackBlock;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDBlocks;
import com.SaintAlfonzo.creativedelight.rustic.registry.RDItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Setup and game events: flower pots, compost chances, trades, putting pancakes back on a plate. */
public final class RDEvents {
    private static final float PRICE_MULTIPLIER = 0.05F;

    private RDEvents() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
            Map.of(RDBlocks.WILD_COTTON, RDBlocks.POTTED_WILD_COTTON, RDBlocks.WILD_BELL_PEPPERS, RDBlocks.POTTED_WILD_BELL_PEPPERS,
                    RDBlocks.WILD_PALE_BELL_PEPPERS, RDBlocks.POTTED_WILD_PALE_BELL_PEPPERS, RDBlocks.WILD_DARK_BELL_PEPPERS,
                    RDBlocks.POTTED_WILD_DARK_BELL_PEPPERS, RDBlocks.WILD_COFFEE, RDBlocks.POTTED_WILD_COFFEE)
                    .forEach((plant, potted) -> pot.addPlant(plant.getId(), potted));
            // Base chances only: the pack's compost framework (startup_scripts/compost/) sets its own values over these.
            compost(0.3F, RDItems.COTTON_SEEDS, RDItems.BELL_PEPPER_SEEDS, RDItems.PALE_BELL_PEPPER_SEEDS, RDItems.DARK_BELL_PEPPER_SEEDS,
                    RDItems.COFFEE_BEANS);
            compost(0.5F, RDItems.COTTON_BOLL);
            RDItems.PEPPERS.values().forEach(pepper -> ComposterBlock.COMPOSTABLES.put(pepper.get(), 0.65F));
            RDItems.PEPPER_SLICES.values().forEach(slice -> ComposterBlock.COMPOSTABLES.put(slice.get(), 0.3F));
            compost(0.65F, RDBlocks.WILD_COTTON, RDBlocks.WILD_BELL_PEPPERS, RDBlocks.WILD_PALE_BELL_PEPPERS, RDBlocks.WILD_DARK_BELL_PEPPERS,
                    RDBlocks.WILD_COFFEE);
        });
    }

    @SafeVarargs
    private static void compost(float chance, RegistryObject<? extends ItemLike>... entries) {
        for (Supplier<? extends ItemLike> entry : entries) {
            ComposterBlock.COMPOSTABLES.put(entry.get().asItem(), chance);
        }
    }

    /** Sneaking with an item in hand skips the block's use; let a pancake go back onto its own plate. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (block instanceof PancakeStackBlock plate && plate.isServing(event.getItemStack())) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

    /** Farmers buy cotton, red peppers and coffee beans, and sell golden coffee beans; fishermen take calamari. */
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.FARMER) {
            List<VillagerTrades.ItemListing> novice = event.getTrades().get(1);
            novice.add(buy(RDItems.COTTON_BOLL, 24, 16, 2));
            novice.add(buy(RDItems.PEPPERS.get("red"), 24, 16, 2));
            novice.add(buy(RDItems.COFFEE_BEANS, 26, 16, 2));
            event.getTrades().get(5).add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, 3),
                    new ItemStack(RDItems.GOLDEN_COFFEE_BEANS.get(), 3), 12, 30, PRICE_MULTIPLIER));
        } else if (event.getType() == VillagerProfession.FISHERMAN) {
            event.getTrades().get(1).add((trader, random) -> new MerchantOffer(new ItemStack(RDItems.CALAMARI.get(), 6),
                    new ItemStack(Items.EMERALD), new ItemStack(RDItems.COOKED_CALAMARI.get(), 6), 16, 1, PRICE_MULTIPLIER));
            event.getTrades().get(2).add(buy(RDItems.CALAMARI, 15, 16, 10));
        }
    }

    /** Seeds and beans at one emerald (the Pale and Dark seeds are pack trades: economy/wandering_trades.js). */
    public static void onWandererTrades(WandererTradesEvent event) {
        for (RegistryObject<? extends ItemLike> seed : List.of(RDItems.COTTON_SEEDS, RDItems.BELL_PEPPER_SEEDS, RDItems.COFFEE_BEANS)) {
            event.getGenericTrades().add((trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(seed.get()), 12, 2,
                    PRICE_MULTIPLIER));
        }
    }

    private static VillagerTrades.ItemListing buy(Supplier<? extends ItemLike> item, int count, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemStack(item.get(), count), new ItemStack(Items.EMERALD), maxUses, xp, PRICE_MULTIPLIER);
    }
}
