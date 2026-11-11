package com.SaintAlfonzo.creativedelight.roll.logic;

import com.SaintAlfonzo.creativedelight.roll.RollConfig;
import com.SaintAlfonzo.creativedelight.roll.registry.RollItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Chopsticks (roll_delight.md 3.3). Chopsticks in the main hand and a food of {@link #FOODS} in the off hand: the food
 * is eaten by vanilla as any off-hand meal (its use time, hunger gate, FoodData caps, Finish event, Spice of Life), so
 * B3 and B4 cannot happen. This class only watches that meal: every non-instant, non-ambient effect the food gave lasts
 * {@code lengthenFactor} times as long, and the chopsticks take 1 damage, both only when the meal finished (B5).
 * On a roll medley (the two Roll ones and FD's Rice Roll Medley) chopsticks eat one serving in place.
 */
public final class Chopsticks {
    /** The chopstick foods; built by the pack's tag scripts (CD roll_delight, global.tags). */
    public static final TagKey<Item> FOODS = ItemTags.create(new ResourceLocation("creativedelight", "chopstick_foods"));

    /** Effect durations when an off-hand chopstick meal started, so the Finish hook can tell which effects the food gave. */
    private static final Map<Player, Map<MobEffect, Integer>> STARTED = new WeakHashMap<>();

    private Chopsticks() {
    }

    public static boolean isChopsticks(ItemStack stack) {
        return stack.is(RollItems.CHOPSTICKS.get());
    }

    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player) || player.level.isClientSide) return;
        ItemStack food = event.getItem();
        if (food == player.getOffhandItem() && food.is(FOODS) && isChopsticks(player.getMainHandItem())) {
            STARTED.put(player, durations(player));
        } else {
            STARTED.remove(player);
        }
    }

    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level.isClientSide) return;
        Map<MobEffect, Integer> before = STARTED.remove(player);
        if (before == null || !player.isUsingItem() || player.getUsedItemHand() != InteractionHand.OFF_HAND) return;
        ItemStack chopsticks = player.getMainHandItem();
        if (!event.getItem().is(FOODS) || !isChopsticks(chopsticks)) return;
        lengthen(player, before);
        chopsticks.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    /** Chopsticks on Farmer's Delight's Rice Roll Medley; the Roll medleys call {@link #eatFromFeast} themselves. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !isChopsticks(event.getItemStack())) return;
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (state.getBlock() != ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get() || !(state.getBlock() instanceof FeastBlock feast)) return;
        InteractionResult result = eatFromFeast(feast, state, event.getLevel(), event.getPos(), event.getEntity(), event.getItemStack());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    /**
     * Eats one serving of a feast in place: the serving goes through the food's own finishUsingItem and the Finish event
     * (a normal meal for Spice of Life), never into the inventory; hunger gate as for eating by hand. Servings that need a
     * container (bowls) are left to the normal path.
     */
    public static InteractionResult eatFromFeast(FeastBlock feast, BlockState state, Level level, BlockPos pos, Player player, ItemStack chopsticks) {
        int left = state.getValue(feast.getServingsProperty());
        if (left == 0) return InteractionResult.PASS;
        ItemStack serving = feast.getServingItem(state);
        FoodProperties food = serving.getFoodProperties(player);
        if (food == null || serving.hasCraftingRemainingItem() || !player.canEat(food.canAlwaysEat())) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(feast.getServingsProperty(), left - 1), Block.UPDATE_ALL);
            Map<MobEffect, Integer> before = durations(player);
            ItemStack eaten = serving.copy();
            ItemStack rest = ForgeEventFactory.onItemUseFinish(player, eaten, 0, serving.finishUsingItem(level, player));
            if (!rest.isEmpty() && !player.getInventory().add(rest)) player.drop(rest, false);
            lengthen(player, before);
            chopsticks.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static Map<MobEffect, Integer> durations(Player player) {
        Map<MobEffect, Integer> out = new HashMap<>();
        for (MobEffectInstance effect : player.getActiveEffects()) out.put(effect.getEffect(), effect.getDuration());
        return out;
    }

    /**
     * Lengthens each effect that is new or longer than before the meal (so: given by the food). Health Boost is skipped:
     * re-applying it clamps health (memory effect-refresh-and-hud-icon-gotchas).
     */
    private static void lengthen(Player player, Map<MobEffect, Integer> before) {
        double factor = RollConfig.LENGTHEN_FACTOR.get();
        if (factor <= 1.0D) return;
        for (MobEffectInstance effect : player.getActiveEffects().toArray(new MobEffectInstance[0])) {
            MobEffect type = effect.getEffect();
            if (type.isInstantenous() || effect.isAmbient() || type == MobEffects.HEALTH_BOOST) continue;
            Integer was = before.get(type);
            if (was != null && effect.getDuration() <= was) continue;
            int longer = (int) Math.min(Integer.MAX_VALUE, Math.round(effect.getDuration() * factor));
            player.addEffect(new MobEffectInstance(type, longer, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
    }
}
