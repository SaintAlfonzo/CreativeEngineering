package net.mehdinoui.veggiesdelight.common.block;

import net.mehdinoui.veggiesdelight.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

// A vanilla-shaped cake (7 bites) that feeds a Carrot Cake Slice's food per bite and drops a slice when cut with a knife.
public class CarrotCakeBlock extends CakeBlock {
    public CarrotCakeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModTags.KNIVES)) {
            return this.cutSlice(level, pos, state);
        }
        if (level.isClientSide) {
            if (this.customEat(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (held.isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }
        return this.customEat(level, pos, state, player);
    }

    private InteractionResult customEat(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        player.awardStat(Stats.EAT_CAKE_SLICE);
        ItemStack slice = new ItemStack(ModItems.CARROT_CAKE_SLICE.get());
        FoodProperties food = slice.getItem().getFoodProperties(slice, player);
        if (food != null) {
            player.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
        }
        int bites = state.getValue(BITES);
        level.gameEvent(player, GameEvent.EAT, pos);
        if (bites < 6) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult cutSlice(Level level, BlockPos pos, BlockState state) {
        int bites = state.getValue(BITES);
        if (bites < 6) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
        }
        Direction direction = Direction.WEST;
        double x = pos.getX() + 0.5 + direction.getStepX() * 0.05;
        double y = pos.getY() + 0.3;
        double z = pos.getZ() + 0.5 + direction.getStepZ() * 0.05;
        ItemUtils.spawnItemEntity(level, new ItemStack(ModItems.CARROT_CAKE_SLICE.get()), x, y, z,
                direction.getStepX() * 0.05, 0.02, direction.getStepZ() * 0.05);
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResult.SUCCESS;
    }
}
