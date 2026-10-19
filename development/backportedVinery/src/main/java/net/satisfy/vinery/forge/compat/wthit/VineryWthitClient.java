package net.satisfy.vinery.forge.compat.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.vinery.core.block.GrapevinePotBlock;
import net.satisfy.vinery.core.block.PaleStemBlock;
import net.satisfy.vinery.core.block.StemBlock;
import net.satisfy.vinery.core.block.StackableLogBlock;
import net.satisfy.vinery.core.block.entity.ApplePressBlockEntity;
import net.satisfy.vinery.core.block.entity.FermentationBarrelBlockEntity;
import net.satisfy.vinery.core.block.entity.GrapevinePotBlockEntity;
import net.satisfy.vinery.core.block.entity.StackableLogBlockEntity;
import net.satisfy.vinery.core.block.entity.StoragePotBlockEntity;
import net.satisfy.vinery.core.item.DrinkBlockItem;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.wine.GrapeType;
import net.satisfy.vinery.platform.PlatformHelper;

/**
 * WTHIT plugin, client half (feature_writeup/alcohol.md B4.6): the body lines. Grapevine pots sync to clients by
 * themselves; everything else comes from {@link VineryWthit}'s server data.
 */
public final class VineryWthitClient implements IWailaClientPlugin, IBlockComponentProvider {
    private static final String LANG = "wthit.vinery_backport.";

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, GrapevinePotBlockEntity.class);
        registrar.body(this, FermentationBarrelBlockEntity.class);
        registrar.body(this, ApplePressBlockEntity.class);
        registrar.body(this, StorageBlockEntity.class);
        registrar.body(this, StoragePotBlockEntity.class);
        registrar.body(this, StackableLogBlockEntity.class);
        registrar.body(this, StemBlock.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof StemBlock) {
            if (config.getBoolean(VineryWthit.STEM)) {
                stem(tooltip, accessor.getBlockState());
            }
            return;
        }
        BlockEntity target = accessor.getBlockEntity();
        if (target instanceof GrapevinePotBlockEntity pot) {
            if (config.getBoolean(VineryWthit.POT)) {
                pot(tooltip, accessor, pot);
            }
            return;
        }
        CompoundTag data = accessor.getServerData().getCompound(VineryWthit.KEY);
        if (data.isEmpty()) {
            return;
        }
        if (target instanceof FermentationBarrelBlockEntity) {
            if (config.getBoolean(VineryWthit.BARREL)) {
                barrel(tooltip, data);
            }
        } else if (target instanceof ApplePressBlockEntity) {
            if (config.getBoolean(VineryWthit.PRESS)) {
                press(tooltip, data);
            }
        } else if (target instanceof StackableLogBlockEntity) {
            if (config.getBoolean(VineryWthit.LOG)) {
                log(tooltip, accessor.getBlockState(), data);
            }
        } else if (config.getBoolean(VineryWthit.STORAGE)) {
            storage(tooltip, data.getList("Stacks", Tag.TAG_COMPOUND));
        }
    }

    /**
     * Grapevine stems and lattices (all lattice woods, 1.3.10's too): which grape grows there, ripe when it can be picked,
     * and the grapevine leaves still to come. Growth % is patcherMod's crop line (crop_growth.json), so not repeated.
     * Block state only, so no server data.
     */
    private static void stem(ITooltip tooltip, BlockState state) {
        GrapeType type = state.getValue(StemBlock.GRAPE);
        if (type == GrapeTypeRegistry.NONE || state.getValue(StemBlock.AGE) == 0) {
            tooltip.addLine(Component.translatable(LANG + "stem.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.addLine(Component.translatable(LANG + "stem.grapes", type.getFruit().getDescription()));
        if (state.getValue(StemBlock.AGE) >= 4) {
            tooltip.addLine(Component.translatable(LANG + "stem.ripe").withStyle(ChatFormatting.GREEN));
        }
        if (state.hasProperty(PaleStemBlock.LEAVES_PENDING) && state.getValue(PaleStemBlock.LEAVES_PENDING)) {
            tooltip.addLine(Component.translatable(LANG + "stem.leaves_pending").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    private static void pot(ITooltip tooltip, IBlockAccessor accessor, GrapevinePotBlockEntity pot) {
        BlockState state = accessor.getBlockState();
        int grapes = state.getValue(GrapevinePotBlock.STORAGE);
        GrapeType type = state.getValue(GrapevinePotBlock.GRAPEVINE_TYPE);
        if (grapes == 0 || type == GrapeTypeRegistry.NONE) {
            return;
        }
        tooltip.addLine(Component.translatable(LANG + "grapes", type.getFruit().getDescription(), grapes, GrapevinePotBlock.MAX_GRAPES));
        int stage = state.getValue(GrapevinePotBlock.STAGE);
        if (stage < GrapevinePotBlock.FULL_STAGE) {
            return;
        }
        if (stage < GrapevinePotBlock.MAX_STAGE) {
            tooltip.addLine(Component.translatable("hud.vinery.grapevine_pot.stomped", pot.getStomps(), GrapevinePotBlockEntity.stompsNeeded()));
            int bonus = PlatformHelper.getGrapevinePotHeavyArmorBonus();
            if (bonus > 0 && accessor.getPlayer().getArmorValue() >= GrapevinePotBlock.HEAVY_ARMOR) {
                tooltip.addLine(Component.translatable("hud.vinery.grapevine_pot.heavy_armor", bonus).withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.addLine(Component.translatable(LANG + "bottles_left", GrapevinePotBlock.bottlesLeft(state)).withStyle(ChatFormatting.GREEN));
        }
    }

    private static void barrel(ITooltip tooltip, CompoundTag data) {
        int fluid = data.getInt("Fluid");
        String juice = data.getString("Juice");
        if (fluid > 0 && !juice.isEmpty()) {
            tooltip.addLine(Component.translatable("tooltip.vinery.fermentation_barrel." + juice + "_juice_with_percentage",
                    percent(fluid, data.getInt("MaxFluid"))));
        } else {
            tooltip.addLine(Component.translatable("tooltip.vinery.fermentation_barrel.empty").withStyle(ChatFormatting.GRAY));
        }
        Item making = item(data.getString("Making"));
        if (making != null) {
            tooltip.addLine(Component.translatable(LANG + "brewing", making.getDescription(), percent(data.getInt("Time"), data.getInt("Total"))));
        }
        int bottles = data.getInt("Bottles");
        if (bottles > 0) {
            tooltip.addLine(Component.translatable(LANG + "bottles", bottles).withStyle(ChatFormatting.GRAY));
        }
    }

    private static void press(ITooltip tooltip, CompoundTag data) {
        int[] progress = data.getIntArray("Progress");
        if (progress.length < 4) {
            return;
        }
        step(tooltip, "mashing", data.getCompound("Mash"), progress[0], progress[1]);
        step(tooltip, "fermenting", data.getCompound("Ferment"), progress[2], progress[3]);
    }

    private static void step(ITooltip tooltip, String key, CompoundTag input, int progress, int total) {
        Item item = item(input.getString("Id"));
        if (item != null) {
            tooltip.addLine(Component.translatable(LANG + key, item.getDescription(), input.getInt("Count"), percent(progress, total)));
        }
    }

    private static void log(ITooltip tooltip, BlockState state, CompoundTag data) {
        if (state.hasProperty(StackableLogBlock.FIRED) && state.getValue(StackableLogBlock.FIRED)) {
            tooltip.addLine(Component.translatable(LANG + "burning", DrinkBlockItem.realTime(data.getInt("Burn"))).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.addLine(Component.translatable(LANG + "unlit").withStyle(ChatFormatting.GRAY));
        }
        for (Tag entry : data.getList("Cooking", Tag.TAG_COMPOUND)) {
            CompoundTag slot = (CompoundTag) entry;
            Item item = item(slot.getString("Id"));
            if (item != null) {
                tooltip.addLine(Component.translatable(LANG + "cooking", item.getDescription(), percent(slot.getInt("Time"), slot.getInt("Total"))));
            }
        }
    }

    private static void storage(ITooltip tooltip, ListTag stacks) {
        int shown = 0;
        for (Tag entry : stacks) {
            if (shown == VineryWthit.MAX_STACKS) {
                tooltip.addLine(Component.translatable(LANG + "more", stacks.size() - shown).withStyle(ChatFormatting.GRAY));
                return;
            }
            CompoundTag stack = (CompoundTag) entry;
            Item item = item(stack.getString("Id"));
            if (item == null) {
                continue;
            }
            MutableComponent line = Component.translatable(LANG + "stack", stack.getInt("Count"), item.getDescription());
            if (stack.contains("Years")) {
                line.append(Component.translatable(LANG + "wine", stack.getInt("Years"), stack.getInt("Level")).withStyle(ChatFormatting.GRAY));
                int rate = stack.getInt("Rate");
                if (rate > 100) {
                    line.append(Component.translatable(LANG + "cellar", String.format("%.1f", rate / 100.0)).withStyle(ChatFormatting.AQUA));
                }
            }
            tooltip.addLine(line);
            shown++;
        }
    }

    private static Item item(String id) {
        ResourceLocation key = id.isEmpty() ? null : ResourceLocation.tryParse(id);
        Item item = key == null ? null : ForgeRegistries.ITEMS.getValue(key);
        return item == Items.AIR ? null : item;
    }

    private static int percent(int part, int total) {
        return total <= 0 ? 0 : Math.min(100, Math.round(part * 100f / total));
    }
}
