package net.mehdinoui.fungidelight.common.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

// 1.19.2 creative tabs are CreativeModeTab subclasses. The tab lists upstream's explicit order first, then anything
// else of ours in registration order.
public class ModCreativeTab {
   public static final CreativeModeTab TAB = new CreativeModeTab("fungidelight_tab") {
      @Override
      public ItemStack makeIcon() {
         return new ItemStack(ModItems.INKY_CAP_MUSHROOM.get());
      }

      @Override
      public void fillItemList(NonNullList<ItemStack> items) {
         Set<Item> seen = new HashSet<>();
         for (Supplier<? extends ItemLike> s : order()) {
            Item item = s.get().asItem();
            if (seen.add(item)) {
               items.add(new ItemStack(item));
            }
         }
         for (RegistryObject<Item> o : ModItems.ITEMS.getEntries()) {
            if (seen.add(o.get())) {
               items.add(new ItemStack(o.get()));
            }
         }
      }
   };

   private static List<Supplier<? extends ItemLike>> order() {
      return List.of(
         ModItems.BROWN_MUSHROOM_CRATE, ModItems.INKY_CAP_MUSHROOM_CRATE, ModItems.MOREL_MUSHROOM_CRATE, ModItems.RED_MUSHROOM_CRATE,
         ModItems.TRUFFLE_CRATE, ModItems.INKY_CAP_MUSHROOM, ModItems.INKY_CAP_MUSHROOM_COLONY, ModItems.MOREL_MUSHROOM,
         ModItems.MOREL_MUSHROOM_COLONY, ModItems.INKY_CAP_MUSHROOM_BLOCK, ModItems.INKY_CAP_MUSHROOM_EDGE, ModItems.INKY_CAP_MUSHROOM_STEM,
         ModItems.INKY_GOO_VEIL, ModItems.MOREL_MUSHROOM_BLOCK, ModItems.MOREL_MUSHROOM_STEM, ModItems.TRUFFLE_DIRT,
         ModItems.INKY_CAP_COW_SPAWN_EGG, ModItems.MOREL_COW_SPAWN_EGG, ModItems.CLEANED_CAPS, ModItems.COOKED_CLEANED_CAPS,
         ModItems.TRUFFLE, ModItems.TRUFFLE_SLICE, ModItems.INKY_CAPS_SALAD, ModItems.CREAMY_MOREL_SOUP, ModItems.INKY_CAP_SCRAMBLED_EGGS,
         ModItems.TRUFFLE_PASTA, ModItems.STUFFED_MORELS, ModItems.STEAK_WITH_MUSHROOMS, ModItems.MUTTON_CHOPS_WITH_TRUFFLE,
         ModItems.RABBIT_WITH_MORELS, ModItems.PORK_MARSALA_WITH_MUSHROOMS, ModItems.TRUFFLE_ICE_CREAM
      );
   }
}
