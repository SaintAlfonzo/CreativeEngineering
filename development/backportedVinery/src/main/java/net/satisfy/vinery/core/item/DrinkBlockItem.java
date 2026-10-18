package net.satisfy.vinery.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.foundation.util.LibUtil;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;
import net.satisfy.vinery.core.wine.WineEffects;
import net.satisfy.vinery.core.wine.WineYears;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A wine: drinkable, placeable while sneaking (it then sits in a storage block entity). Its effect comes from the
 * {@code effects} setting; with aging it gains a level per {@code yearsPerEffectLevel} and duration per year.
 * Port: age, level and the time to the next level run on the vintage clock (alcohol.md B4.5).
 */
public class DrinkBlockItem extends BlockItem {
    private final boolean scaleDurationWithAge;
    private final BottleSize bottleSize;

    public DrinkBlockItem(Block block, Properties settings, boolean scaleDurationWithAge, BottleSize bottleSize) {
        super(block, settings);
        this.scaleDurationWithAge = scaleDurationWithAge;
        this.bottleSize = bottleSize;
    }

    public BottleSize getBottleSize() {
        return bottleSize;
    }

    private boolean ages() {
        return scaleDurationWithAge && WineYears.isAgingEnabled();
    }

    private int duration(ItemStack stack, Level level, WineEffects.WineEffect wineEffect) {
        return ages() ? WineYears.getEffectDuration(stack, level, wineEffect.duration()) : wineEffect.duration();
    }

    private int amplifier(ItemStack stack, Level level, WineEffects.WineEffect wineEffect) {
        return wineEffect.amplifier() + (ages() ? WineYears.getEffectLevel(stack, level) : 0);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    protected BlockState getPlacementState(@NotNull BlockPlaceContext context) {
        if (!Objects.requireNonNull(context.getPlayer()).isCrouching()) {
            return null;
        }
        BlockState blockState = this.getBlock().getStateForPlacement(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos blockPos, Level level, @Nullable Player player, @NotNull ItemStack itemStack, @NotNull BlockState blockState) {
        if (level.getBlockEntity(blockPos) instanceof StorageBlockEntity wineEntity) {
            ItemStack placed = itemStack.copy();
            placed.setCount(1);
            wineEntity.setStack(0, placed);
        }
        return super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        Level level = world != null ? world : clientLevel();

        WineEffects.WineEffect wineEffect = WineEffects.get(this);
        if (wineEffect != null && level != null) {
            MobEffect effect = wineEffect.effect();

            String effectName = effect.getDisplayName().getString();
            int amplifier = amplifier(stack, level, wineEffect);
            String amplifierRoman = amplifier > 0 ? " " + toRoman(amplifier + 1) : "";
            String tooltipText = effect.isInstantenous()
                    ? effectName + amplifierRoman
                    : effectName + amplifierRoman + " (" + formatDuration(duration(stack, level, wineEffect)) + ")";
            tooltip.add(Component.literal(tooltipText).withStyle(effect.getCategory().getTooltipFormatting()));
        } else {
            tooltip.add(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        }

        if (level != null && ages() && (WineYears.hasWineYear(stack) || WineYears.hasLegacyYear(stack))) {
            VintageClock.Snapshot clock = VintageClock.of(level);
            tooltip.add(Component.empty());
            int ageYears = Math.max(0, WineYears.getWineAgeYears(stack, level));
            int ageDays = WineYears.getWineAgeDays(stack, level) - ageYears * clock.daysPerYear();
            tooltip.add(Component.translatable("tooltip.vinery_backport.age", ageYears, ageDays).withStyle(ChatFormatting.WHITE));

            long ticksToNext = WineYears.getTicksUntilNextLevel(stack, level);
            if (ticksToNext < 0) {
                tooltip.add(Component.translatable("hud.vinery.wine.fully_aged").withStyle(ChatFormatting.GOLD));
            } else {
                int days = WineYears.getDaysUntilNextLevel(stack, level);
                long cellar = WineYears.isStored(stack) ? ticksToNext : (long) Math.ceil(ticksToNext * 100.0 / Math.max(100, clock.cellarPercent()));
                tooltip.add(Component.translatable("tooltip.vinery_backport.next_level", days, realTime(ticksToNext), realTime(cellar))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x93c47d))));
            }
            if (WineYears.isStored(stack) && WineYears.currentStorageRate(stack) > 100) {
                tooltip.add(Component.translatable("tooltip.vinery_backport.cellar", String.format("%.1f", WineYears.currentStorageRate(stack) / 100.0)).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    /** Clock ticks as real play time: "1 h 20 min", "53 min". */
    public static String realTime(long ticks) {
        long minutes = Math.max(1, Math.round(ticks / 1200.0));
        return minutes >= 60 ? (minutes / 60) + " h " + (minutes % 60) + " min" : minutes + " min";
    }

    private static Level clientLevel() {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().level);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack, Level level, @NotNull LivingEntity livingEntity) {
        WineEffects.WineEffect wineEffect = WineEffects.get(this);
        if (!level.isClientSide && wineEffect != null) {
            WineYears.convertLegacy(itemStack, level);
            if (!WineYears.hasWineYear(itemStack)) {
                WineYears.setWineYear(itemStack, level);
            }
            livingEntity.addEffect(new MobEffectInstance(wineEffect.effect(), duration(itemStack, level, wineEffect), amplifier(itemStack, level, wineEffect)));
        }
        itemStack.shrink(1);
        return LibUtil.convertStackAfterFinishUsing(livingEntity, itemStack, ObjectRegistry.WINE_BOTTLE.get(), this);
    }

    private String formatDuration(int ticks) {
        int totalSeconds = Math.max(0, ticks) / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand interactionHand) {
        return ItemUtils.startUsingInstantly(level, player, interactionHand);
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, Level level, @NotNull Player player) {
        super.onCraftedBy(stack, level, player);
        if (level != null && !level.isClientSide) {
            WineYears.setWineYear(stack, level);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!level.isClientSide) {
            WineYears.convertLegacy(stack, level);
            if (!WineYears.hasWineYear(stack)) {
                WineYears.setWineYear(stack, level);
            } else {
                WineYears.stopStorage(stack, level);
            }
        }
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    public enum BottleSize {
        SMALL, BIG
    }
}
