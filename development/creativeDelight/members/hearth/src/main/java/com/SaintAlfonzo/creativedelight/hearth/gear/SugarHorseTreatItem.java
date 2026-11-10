package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** N23-7: horse-only 3 minute boost; a separate effect, so it stacks with the horseshoe and with Supplementaries' sugar cube. */
public class SugarHorseTreatItem extends Item {
    private static final int BOOST_TICKS = 3600;

    public SugarHorseTreatItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHorse horse)) {
            return InteractionResult.PASS;
        }
        if (!player.level.isClientSide) {
            horse.addEffect(new MobEffectInstance(HHEffects.HORSE_BOOST.get(), BOOST_TICKS, 0, false, false, false));
            horse.level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(player.level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hearthandharvest.sugar_horse_treat.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
