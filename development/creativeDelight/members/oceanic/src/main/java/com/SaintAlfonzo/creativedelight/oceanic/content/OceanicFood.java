package com.SaintAlfonzo.creativedelight.oceanic.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Every Oceanic Delight food and drink (ocean_delight.md 5.6): food values from the item properties, plus what upstream
 * did around eating. A dish hands back its container (bowl, bottle, stick or nautilus shell); some apply one more effect
 * when eaten (Farmer's Delight Comfort or Nourishment, or Instant Breath); those show one blue tooltip line.
 */
public class OceanicFood extends Item {
    private final Supplier<Item> container;
    private final int useTicks;
    private final boolean drink;
    @Nullable private final Supplier<MobEffect> bonus;
    private final int bonusTicks;
    /** A tooltip line for an effect the food properties already carry (upstream showed those too). */
    @Nullable private final Supplier<MobEffect> shown;
    private final int shownTicks;

    public OceanicFood(Builder b) {
        super(b.properties);
        this.container = b.container;
        this.useTicks = b.useTicks;
        this.drink = b.drink;
        this.bonus = b.bonus;
        this.bonusTicks = b.bonusTicks;
        this.shown = b.shown;
        this.shownTicks = b.shownTicks;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack left = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && bonus != null) {
            MobEffect effect = bonus.get();
            if (effect != null) entity.addEffect(new MobEffectInstance(effect, bonusTicks, 0));
        }
        if (container == null) return left;
        ItemStack back = new ItemStack(container.get());
        if (left.isEmpty()) return back;
        if (entity instanceof Player player && !player.getAbilities().instabuild && !player.getInventory().add(back)) {
            player.drop(back, false);
        }
        return left;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (useTicks > 0) return useTicks;
        return super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return drink ? UseAnim.DRINK : super.getUseAnimation(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (bonus != null) line(lines, bonus.get(), bonusTicks);
        if (shown != null) line(lines, shown.get(), shownTicks);
    }

    private static void line(List<Component> lines, @Nullable MobEffect effect, int ticks) {
        if (effect == null) return;
        Component name = effect.getDisplayName();
        if (effect.isInstantenous()) {
            lines.add(name.copy().withStyle(ChatFormatting.BLUE));
        } else {
            int seconds = ticks / 20;
            String time = (seconds / 60) + ":" + String.format("%02d", seconds % 60);
            lines.add(Component.translatable("tooltip.oceanic_delight.effect", name, time).withStyle(ChatFormatting.BLUE));
        }
    }

    /** A Farmer's Delight effect, looked up when used (Farmer's Delight registers after us is fine). */
    public static Supplier<MobEffect> farmers(String path) {
        ResourceLocation id = new ResourceLocation("farmersdelight", path);
        return () -> ForgeRegistries.MOB_EFFECTS.getValue(id);
    }

    public static class Builder {
        final Item.Properties properties;
        Supplier<Item> container;
        int useTicks;
        boolean drink;
        Supplier<MobEffect> bonus;
        int bonusTicks;
        Supplier<MobEffect> shown;
        int shownTicks;

        public Builder(Item.Properties properties) {
            this.properties = properties;
        }

        public Builder container(Supplier<Item> item) {
            container = item;
            return this;
        }

        public Builder useTicks(int ticks) {
            useTicks = ticks;
            return this;
        }

        public Builder drink() {
            drink = true;
            return this;
        }

        /** One more effect applied when eaten, shown as a tooltip line. */
        public Builder bonus(Supplier<MobEffect> effect, int ticks) {
            bonus = effect;
            bonusTicks = ticks;
            return this;
        }

        /** A tooltip line only, for an effect the food properties carry. */
        public Builder shows(Supplier<MobEffect> effect, int ticks) {
            shown = effect;
            shownTicks = ticks;
            return this;
        }
    }
}
