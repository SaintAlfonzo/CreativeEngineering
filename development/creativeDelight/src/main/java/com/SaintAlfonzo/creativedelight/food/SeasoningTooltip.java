package com.SaintAlfonzo.creativedelight.food;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One line per seasoning under the food's name ({@link Seasonings}): "Garlic: Speed (0:10)". Names are lang keys
 * {@code seasoning.<namespace>.<path>}.
 */
public final class SeasoningTooltip {

    private SeasoningTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<ResourceLocation> ids = Seasonings.of(stack);
        if (ids.isEmpty()) return;
        int at = Math.min(1, event.getToolTip().size());
        for (ResourceLocation id : ids) {
            event.getToolTip().add(at++, line(id));
        }
    }

    private static Component line(ResourceLocation id) {
        Seasonings.Definition def = Seasonings.get(id);
        // The lang key comes from Creative Delight's seasoning_lang.js; without it (TODO food/seasoning #16) the line
        // showed the raw key, so the script's name stands in
        String key = "seasoning." + id.getNamespace() + "." + id.getPath();
        MutableComponent name = (I18n.exists(key) || def == null || def.name().isEmpty()
                ? Component.translatable(key) : Component.literal(def.name()))
                .withStyle(ChatFormatting.GOLD);
        if (def == null) {
            return name;
        }
        MutableComponent bonus = Component.empty();
        boolean first = true;
        if (def.nutrition() > 0) {
            first = append(bonus, first, Component.translatable("creativeengineering.seasoning.nutrition", def.nutrition()));
        }
        if (def.saturation() > 0) {
            first = append(bonus, first, Component.translatable("creativeengineering.seasoning.saturation", number(def.saturation())));
        }
        if (def.heal() > 0) {
            float hearts = def.heal() / 2f;
            first = append(bonus, first, hearts == 1f
                    ? Component.translatable("creativeengineering.seasoning.heal_one")
                    : Component.translatable("creativeengineering.seasoning.heal", number(hearts)));
        }
        for (Seasonings.Effect e : def.effects()) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.effect());
            if (effect == null) continue;
            MutableComponent text = effect.getDisplayName().copy();
            if (e.amplifier() > 0) {
                text.append(" ").append(Component.translatable("potion.potency." + e.amplifier()));
            }
            if (!effect.isInstantenous()) {
                text.append(" (" + MobEffectUtil.formatDuration(new MobEffectInstance(effect, e.duration()), 1f) + ")");
            }
            first = append(bonus, first, text);
        }
        if (first) {
            return name;
        }
        return Component.translatable("creativeengineering.seasoning.line", name, bonus.withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.GRAY);
    }

    private static boolean append(MutableComponent to, boolean first, Component part) {
        if (!first) to.append(", ");
        to.append(part);
        return false;
    }

    private static String number(float v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
    }
}
