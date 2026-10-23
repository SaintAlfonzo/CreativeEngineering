package net.ribs.vintagedelight;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.item.ModItems;

// 1.19.2 creative tabs are CreativeModeTab subclasses. The tab lists ModItems.TAB_ORDER first (upstream's explicit
// tab order), then anything else of ours in registration order.
public class ModCreativeTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("vintagedelight_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.PEANUT.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            java.util.Set<net.minecraft.world.item.Item> seen = new java.util.HashSet<>();
            for (java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> s : ModItems.tabOrder()) {
                net.minecraft.world.item.Item item = s.get().asItem();
                if (seen.add(item)) {
                    items.add(new ItemStack(item));
                }
            }
            for (RegistryObject<net.minecraft.world.item.Item> o : ModItems.ITEMS.getEntries()) {
                if (seen.add(o.get())) {
                    items.add(new ItemStack(o.get()));
                }
            }
        }
    };
}
