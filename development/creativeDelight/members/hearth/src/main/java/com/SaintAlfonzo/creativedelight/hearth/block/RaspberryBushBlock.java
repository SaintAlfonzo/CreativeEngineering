package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Raspberry bush, as 1.4.1 with Berry Good loaded (the pack ships it): a sweet berry bush (age 0-3, growth, bone meal
 * and the age 2 / 3 break drops are vanilla's or the loot table's) that differs in three ways. Picking by hand works
 * at age 3 only and gives 2-3 Raspberries (sweet berries: from age 2). It slows walkers but never hurts them. Pick
 * block gives the Raspberry Pips, the bush's planting item.
 */
public class RaspberryBushBlock extends SweetBerryBushBlock implements BerryBush {
    public RaspberryBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Item getBerry() {
        return HHItems.RASPBERRY.get();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(HHItems.RASPBERRY_PIPS.get());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        // not ripe: pass, so a held item (bone meal) acts instead
        if (age < MAX_AGE) {
            return InteractionResult.PASS;
        }
        int count = 2 + level.random.nextInt(2);
        popResource(level, pos, new ItemStack(getBerry(), count));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                0.8F + level.random.nextFloat() * 0.4F);
        BlockState picked = state.setValue(AGE, 1);
        level.setBlock(pos, picked, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75D, 0.8F));
        }
    }
}
