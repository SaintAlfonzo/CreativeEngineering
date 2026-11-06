package com.SaintAlfonzo.creativedelight.roll.content;

import com.SaintAlfonzo.creativedelight.roll.logic.Chopsticks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.List;
import java.util.function.Supplier;

/**
 * A roll medley (roll_delight.md 3.2, D8): built as Farmer's Delight's Rice Roll Medley, eight servings in a fixed order
 * taken with an empty hand, the empty plate stays at 0 (D15). Changes from FD: only an empty main hand or chopsticks
 * take a serving; any other held item passes, so a held block places against the medley (B9). Chopsticks eat the serving
 * in place (3.3). Breaking a part-eaten medley drops what is left and the bowl (loot table, RollTranslator).
 */
public class RollMedleyBlock extends FeastBlock {
    public static final int MAX = 8;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, MAX);

    private static final VoxelShape PLATE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);
    private static final VoxelShape FULL = Shapes.join(PLATE, Block.box(2.0D, 2.0D, 2.0D, 14.0D, 5.0D, 14.0D), BooleanOp.OR);

    /** Index = servings left - 1, so the last entry is handed out first. */
    private final List<Supplier<Item>> servings;

    public RollMedleyBlock(Properties properties, List<? extends Supplier<Item>> servings) {
        super(properties, servings.get(MAX - 1)::get, true);
        if (servings.size() != MAX) throw new IllegalArgumentException("a roll medley needs " + MAX + " servings");
        this.servings = List.copyOf(servings);
    }

    /** What is still on the plate at {@code left} servings, in serving order (for the loot table). */
    public List<Supplier<Item>> servings() {
        return servings;
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public int getMaxServings() {
        return MAX;
    }

    @Override
    public ItemStack getServingItem(BlockState state) {
        return new ItemStack(servings.get(Math.max(state.getValue(SERVINGS), 1) - 1).get());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (Chopsticks.isChopsticks(held)) return Chopsticks.eatFromFeast(this, state, level, pos, player, held);
        if (!held.isEmpty()) return InteractionResult.PASS;
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? PLATE : FULL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }
}
