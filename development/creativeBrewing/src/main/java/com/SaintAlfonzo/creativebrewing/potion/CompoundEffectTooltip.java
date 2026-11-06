package com.SaintAlfonzo.creativebrewing.potion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Item tooltips: the parts of every compound effect the item gives ({@link CompoundEffects}) go under "When Applied:".
 * Effects come from the potion NBT (potions, tipped arrows, potion fluids in bottles), the food properties (Farmer's
 * Delight style foods and drinks, Fruits Delight jellies) and Vinery's wine table (read by reflection: Vinery is a
 * separate jar). Client only, after every other tooltip handler.
 */
@Mod.EventBusSubscriber(modid = CreativeBrewing.MOD_ID, value = Dist.CLIENT)
public final class CompoundEffectTooltip {

    private static Method wineGet;
    private static boolean wineLookedUp;

    private CompoundEffectTooltip() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<MobEffectInstance> effects = new ArrayList<>(PotionUtils.getMobEffects(stack));
        if (stack.isEdible()) {
            FoodProperties food = stack.getItem().getFoodProperties(stack, event.getEntity());
            if (food != null) {
                for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
                    if (pair.getFirst() != null) effects.add(pair.getFirst());
                }
            }
        }
        effects.addAll(wineEffects(stack.getItem()));
        if (effects.isEmpty()) return;

        boolean compound = false;
        for (MobEffectInstance instance : effects) {
            if (!CompoundEffects.parts(instance.getEffect()).isEmpty()) {
                compound = true;
                break;
            }
        }
        if (!compound) return;

        List<Component> tooltip = event.getToolTip();
        int fallback = 1;
        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();
            for (MobEffectInstance instance : effects) {
                if (line.contains(instance.getEffect().getDisplayName().getString())) fallback = i + 1;
            }
        }
        CompoundEffects.insert(tooltip, effects, fallback);
    }

    /** Vinery's {@code WineEffects.get(item)}: its effects at the wine's base level, or none for other items. */
    private static List<MobEffectInstance> wineEffects(Item item) {
        if (!wineLookedUp) {
            wineLookedUp = true;
            try {
                wineGet = Class.forName("net.satisfy.vinery.core.wine.WineEffects").getMethod("get", Item.class);
            } catch (ReflectiveOperationException | LinkageError absent) {
                wineGet = null;
            }
        }
        if (wineGet == null) return List.of();
        try {
            Object wine = wineGet.invoke(null, item);
            if (wine == null) return List.of();
            List<?> list = (List<?>) wine.getClass().getMethod("effects").invoke(wine);
            int amplifier = (Integer) wine.getClass().getMethod("amplifier").invoke(wine);
            List<MobEffectInstance> out = new ArrayList<>();
            for (Object effect : list) {
                if (effect instanceof MobEffect me) out.add(new MobEffectInstance(me, 1, amplifier));
            }
            return out;
        } catch (ReflectiveOperationException | ClassCastException e) {
            return List.of();
        }
    }
}
