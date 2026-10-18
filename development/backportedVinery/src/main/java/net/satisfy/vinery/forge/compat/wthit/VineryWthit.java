package net.satisfy.vinery.forge.compat.wthit;

import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.entity.ApplePressBlockEntity;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.block.entity.StackableLogBlockEntity;
import net.satisfy.vinery.core.block.entity.StoragePotBlockEntity;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.wine.WineYears;
import net.satisfy.vinery.platform.PlatformHelper;

import java.util.List;

/**
 * WTHIT plugin, common half (feature_writeup/alcohol.md B4.6, D6): one client toggle per line group, and the server
 * data the lines need. Barrel, press and log progress never reach the client otherwise, storage pots are plain
 * containers, and a wine's age is read on the server so it matches what the wine does when drunk.
 * The client half is {@link VineryWthitClient}; both are declared in {@code waila_plugins.json}.
 */
public final class VineryWthit implements IWailaCommonPlugin {
    public static final ResourceLocation POT = Vinery.identifier("grapevine_pot");
    public static final ResourceLocation BARREL = Vinery.identifier("fermentation_barrel");
    public static final ResourceLocation PRESS = Vinery.identifier("apple_press");
    public static final ResourceLocation STORAGE = Vinery.identifier("storage");
    public static final ResourceLocation LOG = Vinery.identifier("stackable_log");
    static final String KEY = "vinery";
    /** Storage lines shown before "...and N more". */
    static final int MAX_STACKS = 9;

    @Override
    public void register(ICommonRegistrar registrar) {
        for (ResourceLocation id : List.of(POT, BARREL, PRESS, STORAGE, LOG)) {
            registrar.localConfig(id, true);
        }
        Data data = new Data();
        registrar.blockData(data, FermentationBarrelBlockEntity.class);
        registrar.blockData(data, ApplePressBlockEntity.class);
        registrar.blockData(data, StorageBlockEntity.class);
        registrar.blockData(data, StoragePotBlockEntity.class);
        registrar.blockData(data, StackableLogBlockEntity.class);
    }

    private static final class Data implements IServerDataProvider<BlockEntity> {
        @Override
        public void appendServerData(CompoundTag data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
            BlockEntity target = accessor.getTarget();
            Level level = accessor.getLevel();
            CompoundTag out = new CompoundTag();
            if (target instanceof FermentationBarrelBlockEntity barrel) {
                out.putInt("Fluid", barrel.getFluidLevel());
                out.putInt("MaxFluid", PlatformHelper.getMaxFluidLevel());
                out.putString("Juice", barrel.getJuiceType());
                FermentationBarrelRecipe recipe = barrel.getCurrentRecipe();
                if (recipe != null && !recipe.getResultItem().isEmpty()) {
                    out.putString("Making", id(recipe.getResultItem()));
                    out.putInt("Time", barrel.getFermentationTime());
                    out.putInt("Total", PlatformHelper.getTotalFermentationTime());
                }
                out.putInt("Bottles", barrel.getItem(FermentationBarrelBlockEntity.WINE_BOTTLE_SLOT).getCount());
            } else if (target instanceof ApplePressBlockEntity press) {
                out.putIntArray("Progress", press.getProgress());
                out.put("Mash", stack(press.getItem(0)));
                out.put("Ferment", stack(press.getItem(1)));
            } else if (target instanceof StackableLogBlockEntity log) {
                out.putInt("Burn", log.getBurnTime());
                int[][] cooking = log.getCooking();
                ListTag slots = new ListTag();
                NonNullList<ItemStack> items = log.getItems();
                for (int i = 0; i < items.size(); i++) {
                    if (!items.get(i).isEmpty()) {
                        CompoundTag slot = stack(items.get(i));
                        slot.putInt("Time", cooking[0][i]);
                        slot.putInt("Total", cooking[1][i]);
                        slots.add(slot);
                    }
                }
                out.put("Cooking", slots);
            } else if (target instanceof StorageBlockEntity storage) {
                out.put("Stacks", stacks(storage.getInventory(), level));
            } else if (target instanceof Container container) {
                // from the saved tag, not getItem: that would roll a pending loot table just by looking at it
                NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
                ContainerHelper.loadAllItems(target.saveWithoutMetadata(), items);
                out.put("Stacks", stacks(items, level));
            }
            data.put(KEY, out);
        }

        /** Non-empty stacks; wines carry their age, level and storage rate as the server sees them. */
        private static ListTag stacks(List<ItemStack> items, Level level) {
            ListTag list = new ListTag();
            for (ItemStack item : items) {
                if (item.isEmpty()) {
                    continue;
                }
                CompoundTag tag = stack(item);
                if (item.getItem() instanceof DrinkBlockItem && (WineYears.hasWineYear(item) || WineYears.hasLegacyYear(item))) {
                    tag.putInt("Years", Math.max(0, WineYears.getWineAgeYears(item, level)));
                    tag.putInt("Level", WineYears.getEffectLevel(item, level));
                    if (WineYears.isStored(item)) {
                        tag.putInt("Rate", WineYears.currentStorageRate(item));
                    }
                }
                list.add(tag);
            }
            return list;
        }

        private static CompoundTag stack(ItemStack item) {
            CompoundTag tag = new CompoundTag();
            if (!item.isEmpty()) {
                tag.putString("Id", id(item));
                tag.putInt("Count", item.getCount());
            }
            return tag;
        }

        private static String id(ItemStack item) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item.getItem());
            return key == null ? "" : key.toString();
        }
    }
}
