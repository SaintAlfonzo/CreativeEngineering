package com.SaintAlfonzo.creativedelight.golden.content;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * Golden and enchanted golden cake (feature_writeup/golden_food.md 3.2). A cake (7 slices, the vanilla {@code bites}
 * property) whose slice feeds and gives effects like the golden foods, even when the eater is not hungry. As upstream,
 * a candle on an uneaten cake makes a vanilla candle cake (the golden cake is gone).
 */
public class GoldenCakeBlock extends CakeBlock {
    /** What one slice gives. Golden: 4 hunger, 1.2 saturation, Regeneration II 5 s, Absorption II 2 min. */
    public enum Slice {
        GOLDEN(4, 1.2F, List.of(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1),
                () -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 1))),
        /** 8 hunger, 8.0 saturation, Regeneration II 20 s, Resistance and Fire Resistance 5 min, Absorption IV 2 min. */
        ENCHANTED(8, 8.0F, List.of(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1),
                () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0),
                () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0),
                () -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3)));

        final int hunger;
        final float saturation;
        final List<Supplier<MobEffectInstance>> effects;

        Slice(int hunger, float saturation, List<Supplier<MobEffectInstance>> effects) {
            this.hunger = hunger;
            this.saturation = saturation;
            this.effects = effects;
        }
    }

    private final Slice slice;

    public GoldenCakeBlock(Properties properties, Slice slice) {
        super(properties);
        this.slice = slice;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ItemTags.CANDLES) && state.getValue(BITES) == 0 && Block.byItem(held.getItem()) instanceof CandleBlock candle) {
            if (!player.isCreative()) held.shrink(1);
            level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlockAndUpdate(pos, CandleCakeBlock.byCandle(candle));
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.awardStat(Stats.ITEM_USED.get(held.getItem()));
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide) {
            if (takeSlice(level, pos, state, player).consumesAction()) return InteractionResult.SUCCESS;
            if (held.isEmpty()) return InteractionResult.CONSUME;
        }
        return takeSlice(level, pos, state, player);
    }

    /** One slice: always eaten (no hunger check), then the next bite state or, after the seventh, the block is gone. */
    protected InteractionResult takeSlice(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        player.awardStat(Stats.EAT_CAKE_SLICE);
        player.getFoodData().eat(slice.hunger, slice.saturation);
        slice.effects.forEach(effect -> player.addEffect(effect.get()));
        int bites = state.getValue(BITES);
        level.gameEvent(player, GameEvent.EAT, pos);
        if (bites < MAX_BITES) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
