package net.mehdinoui.fungidelight.common.event;

import java.util.List;
import net.mehdinoui.fungidelight.common.registry.ModItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "fungidelight"
)
public class ModVillagerEvents {
   @SubscribeEvent
   public static void onVillagerTrades(VillagerTradesEvent event) {
      if (event.getType() == VillagerProfession.FARMER) {
         List<ItemListing> level3Trades = (List<ItemListing>)event.getTrades().get(3);
         level3Trades.add(
            (entity, random) -> new MerchantOffer(
                  new ItemStack((ItemLike)ModItems.INKY_CAP_MUSHROOM_COLONY.get(), 4), new ItemStack(Items.EMERALD, 1), 12, 20, 0.05F
               )
         );
         level3Trades.add(
            (entity, random) -> new MerchantOffer(
                  new ItemStack((ItemLike)ModItems.MOREL_MUSHROOM_COLONY.get(), 4), new ItemStack(Items.EMERALD, 1), 12, 20, 0.05F
               )
         );
         level3Trades.add(
            (entity, random) -> new MerchantOffer(
                  new ItemStack((ItemLike)vectorwing.farmersdelight.common.registry.ModItems.BROWN_MUSHROOM_COLONY.get(), 4),
                  new ItemStack(Items.EMERALD, 1),
                  12,
                  20,
                  0.05F
               )
         );
         level3Trades.add(
            (entity, random) -> new MerchantOffer(
                  new ItemStack((ItemLike)vectorwing.farmersdelight.common.registry.ModItems.RED_MUSHROOM_COLONY.get(), 4),
                  new ItemStack(Items.EMERALD, 1),
                  12,
                  20,
                  0.05F
               )
         );
         level3Trades.add(
            (entity, random) -> new MerchantOffer(new ItemStack((ItemLike)ModItems.TRUFFLE.get(), 4), new ItemStack(Items.EMERALD, 1), 12, 20, 0.05F)
         );
      }
   }

   @SubscribeEvent
   public static void onWandererTrades(WandererTradesEvent event) {
      List<ItemListing> trades = event.getGenericTrades();
      trades.add(new BasicItemListing(1, new ItemStack((ItemLike)ModItems.INKY_CAP_MUSHROOM.get()), 12, 1, 0.05F));
      trades.add(new BasicItemListing(1, new ItemStack((ItemLike)ModItems.MOREL_MUSHROOM.get()), 12, 1, 0.05F));
      trades.add(new BasicItemListing(1, new ItemStack((ItemLike)ModItems.INKY_GOO_VEIL.get()), 12, 1, 0.05F));
   }
}
