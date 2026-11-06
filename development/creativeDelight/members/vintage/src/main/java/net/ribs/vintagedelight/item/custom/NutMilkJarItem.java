// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3.
package net.ribs.vintagedelight.item.custom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Nut milk in a mason jar: drunk like the nut milk bottle (Farmer's Delight's milk bottle: one random curable effect
 * is removed), placeable like every jar, and leaves the empty jar.
 */
public class NutMilkJarItem extends MasonJarItem {
    public NutMilkJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            List<MobEffectInstance> curable = new ArrayList<>();
            for (MobEffectInstance effect : consumer.getActiveEffects()) {
                if (effect.isCurativeItem(milk)) curable.add(effect);
            }
            if (!curable.isEmpty()) {
                consumer.removeEffect(curable.get(level.random.nextInt(curable.size())).getEffect());
            }
        }
        return super.finishUsingItem(stack, level, consumer);
    }
}
