// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port (P9d): Create 0.5.1's CustomUseEffectsItem returns a nullable Boolean (Create 6: catnip TriState); the
// BOOSTING_CHIP advancement award is removed (the pack drops advancements); fix B28 in finishUsingItem.
package org.patryk3211.powergrid.equipment;

import com.simibubi.create.foundation.item.CustomUseEffectsItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;

public class BoostingChipItem extends Item implements CustomUseEffectsItem {
    public BoostingChipItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack boostChip = player.getItemInHand(usedHand);
        ItemStack stack = player.getItemInHand(usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        var recipe = ItemBoostUtils.findRecipe(level, boostChip, stack);
        if (recipe != null) {
            if (!ItemBoostUtils.isBoosted(stack)) {
                player.startUsingItem(usedHand);
                return new InteractionResultHolder<>(InteractionResult.PASS, boostChip);
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        var boosted = player.getItemInHand(player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        // Port: fix B28 (power_grid.md 3.5). Upstream boosted whatever the other hand held when the 32 ticks ended: a
        // hotbar scroll while the chip (in the off hand) was in use put any item there (a sword got a Boosted tag, a
        // block nothing), and the chip was used up either way. The recipe is checked again here; without a match the
        // chip is kept and nothing is boosted.
        if (ItemBoostUtils.findRecipe(level, stack, boosted) == null || ItemBoostUtils.isBoosted(boosted))
            return stack;
        ItemBoostUtils.setBoosted(boosted, true);
        // Port: upstream awarded the BOOSTING_CHIP advancement here (removed; the pack drops advancements).
        stack.shrink(1);
        entity.broadcastBreakEvent(entity.getUsedItemHand());
        return stack;
    }

    @Override
    public SoundEvent getEatingSound() {
        return ModdedSoundEvents.BOOSTING.getMainEvent();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public Boolean shouldTriggerUseEffects(ItemStack stack, LivingEntity entity) {
        return Boolean.TRUE;
    }

    @Override
    public boolean triggerUseEffects(ItemStack stack, LivingEntity entity, int i, RandomSource random) {
        if((entity.getTicksUsingItem() - 6) % 7 == 0) {
            entity.playSound(entity.getEatingSound(stack), 0.9F + 0.2F * random.nextFloat(), random.nextFloat() * 0.2F + 0.9F);
        }
        return true;
    }
}
