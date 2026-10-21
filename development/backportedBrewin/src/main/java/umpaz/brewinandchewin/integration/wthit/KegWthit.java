package umpaz.brewinandchewin.integration.wthit;

import java.util.Optional;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

/**
 * Port addition, WTHIT plugin (feature_writeup/wthit_lines.md 2.8): the keg's temperature and fermentation, which the
 * keg only syncs to clients when its inventory changes. WTHIT's own Forge lines already list the keg's items and tank.
 * Lines: "Temperature: Warm"; then, while a recipe matches, "Fermenting: Beer 45%" or, when the keg is the wrong
 * temperature for it, "Beer: Must Be Cold" (the recipe book's words). Declared in {@code waila_plugins.json}; off with
 * {@code wthitKeg} in brewinandchewin-client.toml.
 */
public final class KegWthit implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider,
        IDataProvider<KegBlockEntity> {

    private static final String KEY = "brewinandchewin";
    private static final ResourceLocation TEMPERATURE = new ResourceLocation("brewinandchewin", "keg_temperature");
    private static final ResourceLocation FERMENTING = new ResourceLocation("brewinandchewin", "keg_fermenting");

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(this, KegBlockEntity.class);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, KegBlockEntity.class);
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<KegBlockEntity> accessor, IPluginConfig config) {
        KegBlockEntity keg = accessor.getTarget();
        CompoundTag out = new CompoundTag();
        int temperature = keg.getTemperature();
        out.putInt("Temp", temperature);
        Optional<KegFermentingRecipe> recipe = keg.getRecipeWithoutTemperature();
        if (recipe.isPresent()) {
            KegFermentingRecipe r = recipe.get();
            out.put("Making", r.getResultItem().save(new CompoundTag()));
            out.putInt("Want", r.getTemperature());
            out.putBoolean("Valid", KegBlockEntity.isValidTemp(temperature, r.getTemperature()));
            out.putInt("Time", keg.getFermentTime());
            out.putInt("Total", r.getFermentTime());
        }
        data.raw().put(KEY, out);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getData().raw().getCompound(KEY);
        if (!data.contains("Temp") || !BnCConfiguration.wthitKeg.get()) {
            return;
        }
        tooltip.setLine(TEMPERATURE, new PairComponent(BnCTextUtils.getTranslation("wthit.keg.temperature"),
                temperatureName(data.getInt("Temp"))));
        if (!data.contains("Making")) {
            return;
        }
        Component making = ItemStack.of(data.getCompound("Making")).getHoverName();
        if (data.getBoolean("Valid")) {
            int total = data.getInt("Total");
            int percent = total <= 0 ? 0 : Math.min(99, data.getInt("Time") * 100 / total);
            tooltip.setLine(FERMENTING, new PairComponent(BnCTextUtils.getTranslation("wthit.keg.fermenting"),
                    Component.translatable("brewinandchewin.wthit.keg.progress", making, percent)));
        } else {
            tooltip.setLine(FERMENTING, new PairComponent(making.copy(),
                    Component.translatable("brewinandchewin.container.keg.temperature_requirement",
                            temperatureName(data.getInt("Want"))).withStyle(ChatFormatting.RED)));
        }
    }

    /** The keg GUI's names: 1 cold, 2 chilly, 3 normal, 4 warm, 5 hot. */
    private static MutableComponent temperatureName(int temperature) {
        return switch (temperature) {
            case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
            case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
            case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
            case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
            default -> BnCTextUtils.getTranslation("container.keg.normal");
        };
    }
}
