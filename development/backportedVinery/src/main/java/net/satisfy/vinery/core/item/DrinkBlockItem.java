package net.satisfy.vinery.core.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
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
import net.satisfy.vinery.core.block.WineBottleBlock;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;
import net.satisfy.vinery.core.wine.WineEffects;
import net.satisfy.vinery.core.wine.WineYears;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A wine: drinkable, placeable while sneaking (it then sits in a storage block entity). Its effect comes from the
 * {@code effects} setting; with aging it gains a level per {@code monthsPerEffectLevel} and duration per month.
 * Port: age, level and the time to the next level run on the vintage clock (alcohol.md B4.5).
 */
public class DrinkBlockItem extends BlockItem {
    private final boolean scaleDurationWithAge;

    public DrinkBlockItem(Block block, Properties settings, boolean scaleDurationWithAge) {
        super(block, settings);
        this.scaleDurationWithAge = scaleDurationWithAge;
    }

    /** From the placed block's per-block limit, the one place a wine's size is set (alcohol.md 10.12). */
    public BottleSize getBottleSize() {
        return getBlock() instanceof WineBottleBlock wine ? BottleSize.of(wine.getMaxCount()) : BottleSize.LARGE;
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

    /** Port (alcohol/vineryport #18): the sneak key, not the crouch pose, which a flying player never takes. */
    @Override
    protected BlockState getPlacementState(@NotNull BlockPlaceContext context) {
        if (!Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
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
        } else if (!hasFoodEffects()) {
            tooltip.add(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        }
        addFoodEffectLines(tooltip);
        getBottleSize().addSizeLines(tooltip);

        if (level != null && ages() && WineYears.hasWineYear(stack)) {
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
                tooltip.add(Component.translatable("tooltip.vinery_backport.next_level", days, realTime(clock.realTicks(ticksToNext)), realTime(clock.realTicks(cellar)))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x93c47d))));
            }
            if (WineYears.isStored(stack)) {
                tooltip.add(Component.translatable("tooltip.vinery_backport.storage", WineYears.storageLabel(WineYears.currentStorageKind(stack)),
                        WineYears.formatRate(WineYears.currentStorageRate(stack))).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    /** Real game ticks as play time: "1 h 20 min", "53 min". Clock ticks go through {@link VintageClock.Snapshot#realTicks} first. */
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
            if (!WineYears.hasWineYear(itemStack)) {
                WineYears.setWineYear(itemStack, level);
            }
            livingEntity.addEffect(new MobEffectInstance(wineEffect.effect(), duration(itemStack, level, wineEffect), amplifier(itemStack, level, wineEffect)));
        }
        if (itemStack.isEdible()) {
            livingEntity.eat(level, itemStack);
        } else {
            itemStack.shrink(1);
        }
        return LibUtil.convertStackAfterFinishUsing(livingEntity, itemStack, ObjectRegistry.WINE_BOTTLE.get(), this);
    }

    /** Port: eat() plays this at the end of drinking, as Brewin's drinks do. */
    @Override
    public @NotNull SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    private boolean hasFoodEffects() {
        FoodProperties food = getFoodProperties();
        return food != null && !food.getEffects().isEmpty();
    }

    /** Port (alcohol.md 11): the food effects the pack gives every wine (Intoxication, Nausea), in the wine line's format. */
    private void addFoodEffectLines(List<Component> tooltip) {
        FoodProperties food = getFoodProperties();
        if (food == null) {
            return;
        }
        for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
            MobEffectInstance instance = pair.getFirst();
            MobEffect effect = instance.getEffect();
            String text = effect.getDisplayName().getString()
                    + (instance.getAmplifier() > 0 ? " " + toRoman(instance.getAmplifier() + 1) : "");
            if (!effect.isInstantenous()) {
                text += " (" + formatDuration(instance.getDuration()) + ")";
            }
            tooltip.add(Component.literal(text).withStyle(effect.getCategory().getTooltipFormatting()));
        }
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

    /**
     * alcohol.md 10.12: a wine's size is how many fit in one placed block (3 small, 2 medium, 1 large). Each container
     * names the largest size it takes: 9x rack small, 4x rack and wine box medium, the single-bottle rack large.
     */
    public enum BottleSize {
        LARGE(1), MEDIUM(2), SMALL(3);

        public final int perBlock;

        BottleSize(int perBlock) {
            this.perBlock = perBlock;
        }

        /** alcohol/vineryport #16: "Small bottle: 3 per placed block" and where it can be stored. */
        public void addSizeLines(List<Component> tooltip) {
            tooltip.add(Component.translatable("tooltip.vinery_backport.bottle_size", Component.translatable(key("label.vinery_backport.size.")), perBlock)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(key("tooltip.vinery_backport.fits.")).withStyle(ChatFormatting.GRAY));
        }

        /** alcohol/vineryport #16: a storage block's capacity, "Holds 4 wines (small or medium)". */
        public static void addHoldsLine(List<Component> tooltip, int slots, BottleSize largest) {
            Component sizes = Component.translatable(largest.key("label.vinery_backport.sizes_up_to."));
            tooltip.add((slots == 1 ? Component.translatable("tooltip.vinery_backport.holds_one", sizes)
                    : Component.translatable("tooltip.vinery_backport.holds", slots, sizes)).withStyle(ChatFormatting.GRAY));
        }

        private String key(String prefix) {
            return prefix + name().toLowerCase(Locale.ROOT);
        }

        public static BottleSize of(int perBlock) {
            return perBlock >= 3 ? SMALL : perBlock == 2 ? MEDIUM : LARGE;
        }

        /** True for a wine no larger than {@code largest}; false for anything that isn't a wine. */
        public static boolean fits(ItemStack stack, BottleSize largest) {
            return stack.getItem() instanceof DrinkBlockItem drink && drink.getBottleSize().perBlock >= largest.perBlock;
        }
    }
}
