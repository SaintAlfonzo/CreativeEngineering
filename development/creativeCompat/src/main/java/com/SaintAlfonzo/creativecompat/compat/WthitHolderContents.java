package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.PairComponent;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * WTHIT plugin: what is inside a Supplementaries jar, goblet or cage. One "Contents" line: the soft fluid with its
 * amount in bottles against the holder's capacity ("Honey (6 / 12)", "Splash Potion of Healing (1 / 12)"; a goblet,
 * capacity 1, just "Water"), the cookies in a cookie jar ("Cookie (5 / 12)"; a mixed jar
 * "3 Cookie, 2 Lemon Cookie (5 / 12)"), or the captured mob ("Bee"). A potion's effects follow on their own lines, as its item tooltip writes them ({@code [wthit] holderPotionEffects}).
 *
 * <p>Client-side only: the jar, goblet and cage sync all of this to the client for their renderers. Any block
 * entity implementing Moonlight's {@code ISoftFluidTankProvider} or Supplementaries' {@code IMobContainerProvider}
 * qualifies. Off with {@code [wthit] holderContents}. feature_writeup/wthit_lines.md.
 */
public final class WthitHolderContents implements IWailaClientPlugin, IBlockComponentProvider {

    private static final ResourceLocation LINE = new ResourceLocation(CreativeCompat.MOD_ID, "holder_contents");
    private static volatile Field capacity;
    private static volatile boolean capacityLookedUp;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(this, Block.class);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof ISoftFluidTankProvider) && !(be instanceof IMobContainerProvider)
                || !com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.holderContents.get()) {
            return;
        }
        Component contents = contents(be);
        if (contents != null) {
            tooltip.setLine(LINE, new PairComponent(Component.translatable("creativecommon.wthit.contents"), contents));
            if (com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.holderPotionEffects.get() && be instanceof ISoftFluidTankProvider p) {
                ItemStack potion = potionOf(p.getSoftFluidTank());
                if (!potion.isEmpty()) {
                    effects(tooltip, potion);
                }
            }
        }
    }

    /** One line per effect, as the potion item's own tooltip writes it: "Instant Health II", "Regeneration (0:45)". */
    private static void effects(ITooltip tooltip, ItemStack potion) {
        float factor = potion.is(Items.LINGERING_POTION) ? 0.25f : 1.0f; // vanilla's lingering tooltip factor
        for (MobEffectInstance e : PotionUtils.getMobEffects(potion)) {
            MutableComponent line = Component.translatable(e.getDescriptionId());
            if (e.getAmplifier() > 0) {
                line = Component.translatable("potion.withAmplifier", line,
                        Component.translatable("potion.potency." + e.getAmplifier()));
            }
            if (e.getDuration() > 20) {
                line = Component.translatable("potion.withDuration", line, MobEffectUtil.formatDuration(e, factor));
            }
            tooltip.addLine(line.withStyle(e.getEffect().getCategory().getTooltipFormatting()));
        }
    }

    private static Component contents(BlockEntity be) {
        if (be instanceof ISoftFluidTankProvider p) {
            SoftFluidTank tank = p.getSoftFluidTank();
            if (tank != null && !tank.isEmpty() && tank.getCount() > 0) {
                ItemStack potion = potionOf(tank);
                Component name = potion.isEmpty() ? tank.getFluid().getTranslatedName() : potion.getHoverName();
                return amount(name, tank.getCount(), capacity(tank));
            }
        }
        if (be instanceof JarBlockTile jar) {
            // Mixed jars (Creative Compat JarMixing, through JarContents): one item per slot, slot count = capacity. Via Container so reobf maps the calls.
            Container items = (Container) jar;
            List<ItemStack> kinds = new ArrayList<>();
            for (int i = 0; i < items.getContainerSize(); i++) {
                kinds.add(items.getItem(i));
            }
            kinds = JarContents.kinds(kinds);
            if (kinds.size() == 1) {
                return amount(kinds.get(0).getHoverName(), kinds.get(0).getCount(), items.getContainerSize());
            }
            if (!kinds.isEmpty()) {
                MutableComponent list = Component.empty();
                for (int i = 0; i < kinds.size(); i++) {
                    if (i > 0) {
                        list.append(", ");
                    }
                    list.append(Component.translatable("creativecommon.wthit.contents.kind",
                            kinds.get(i).getCount(), kinds.get(i).getHoverName()));
                }
                return amount(list, JarContents.total(items), items.getContainerSize());
            }
        }
        if (be instanceof IMobContainerProvider p) {
            MobContainer mobs = p.getMobContainer();
            if (mobs != null && !mobs.isEmpty()) {
                Entity shown = mobs.getDisplayedMob();
                if (shown != null) {
                    return shown.getName();
                }
                MobContainer.MobNBTData data = mobs.getData();
                if (data != null && data.getName() != null) {
                    return Component.translatable(data.getName()); // a plain name saved at capture, or a key
                }
            }
        }
        return null;
    }

    /** A one-bottle holder (goblet) is just full or empty: the name alone. */
    private static Component amount(Component name, int count, int max) {
        if (max == 1 && com.SaintAlfonzo.creativecompat.CompatConfig.WTHIT.holderSingleName.get()) {
            return name;
        }
        return max > 0
                ? Component.translatable("creativecommon.wthit.contents.amount", name, count, max)
                : Component.translatable("creativecommon.wthit.contents.count", name, count);
    }

    /** The potion item a potion tank holds ("Splash Potion of Healing"), or empty for any other fluid. */
    private static ItemStack potionOf(SoftFluidTank tank) {
        CompoundTag nbt = tank == null ? null : tank.getNbt();
        if (nbt == null || !(nbt.contains("Potion") || nbt.contains("CustomPotionEffects"))) {
            return ItemStack.EMPTY;
        }
        Item bottle = switch (nbt.getString("Bottle").toLowerCase(Locale.ROOT)) {
            case "splash" -> Items.SPLASH_POTION;
            case "lingering" -> Items.LINGERING_POTION;
            default -> Items.POTION;
        };
        ItemStack potion = new ItemStack(bottle);
        CompoundTag tag = nbt.copy();
        tag.remove("Bottle");
        potion.setTag(tag);
        return potion;
    }

    /** {@code SoftFluidTank.capacity} is protected and has no getter in Moonlight 2.3; 0 when unreadable. */
    private static int capacity(SoftFluidTank tank) {
        if (!capacityLookedUp) {
            try {
                Field f = SoftFluidTank.class.getDeclaredField("capacity");
                f.setAccessible(true);
                capacity = f;
            } catch (ReflectiveOperationException | RuntimeException e) {
                capacity = null;
            }
            capacityLookedUp = true;
        }
        Field f = capacity;
        if (f == null) {
            return 0;
        }
        try {
            return f.getInt(tank);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }
}
