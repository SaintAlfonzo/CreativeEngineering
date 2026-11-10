package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.client.PitchforkItemRenderer;
import com.SaintAlfonzo.creativedelight.hearth.entity.ThrownPitchfork;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Straw tool (HHLootModifiers.PitchforkStraw) and thrown weapon; melee applies no effect (N23-5). */
public class PitchforkItem extends Item {
    private final Multimap<Attribute, AttributeModifier> modifiers;

    public PitchforkItem(Properties properties) {
        super(properties);
        modifiers = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 4.0D, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.6D, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? modifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, a -> a.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(HHTags.MINEABLE_WITH_PITCHFORK) ? Float.MAX_VALUE : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.LOYALTY || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ThrowableToolItem.startCharge(player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        ThrowableToolItem.release(stack, level, entity, getUseDuration(stack) - timeLeft, 10, 2.5F, SoundEvents.TRIDENT_THROW, 0.75F, ThrownPitchfork::new);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return PitchforkItemRenderer.getInstance();
            }
        });
    }
}
