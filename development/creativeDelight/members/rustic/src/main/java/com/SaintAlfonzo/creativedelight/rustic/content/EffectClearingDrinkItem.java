package com.SaintAlfonzo.creativedelight.rustic.content;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A coffee that clears one random effect milk would cure ("Clears 1 Effect"): the milk, honey, syrup, pumpkin and cherry
 * blossom coffees. With {@code harmfulOnly} (chocolate coffee) it picks among harmful effects only.
 */
public class EffectClearingDrinkItem extends DrinkableItem {
    private final boolean harmfulOnly;

    public EffectClearingDrinkItem(Properties properties, boolean harmfulOnly) {
        super(properties, true, true);
        this.harmfulOnly = harmfulOnly;
    }

    @Override
    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        ItemStack milk = new ItemStack(Items.MILK_BUCKET);
        List<MobEffectInstance> curable = new ArrayList<>();
        for (MobEffectInstance effect : consumer.getActiveEffects()) {
            if (effect.isCurativeItem(milk) && (!harmfulOnly || effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)) {
                curable.add(effect);
            }
        }
        if (curable.isEmpty()) {
            return;
        }
        // removeEffect posts Forge's MobEffectEvent.Remove itself.
        consumer.removeEffect(curable.get(level.random.nextInt(curable.size())).getEffect());
    }
}
