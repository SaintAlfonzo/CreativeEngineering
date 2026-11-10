package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 8 uses (3.1, N24-3): a crop goes to its last age at once, a sapling grows, a stem fruits, anything else bone-meals. */
public class FertilizerBagItem extends Item {
    public FertilizerBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Player player = context.getPlayer();
        if (!(block instanceof SaplingBlock || block instanceof StemBlock || block instanceof CropBlock || block instanceof BonemealableBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            BoneMealItem.addGrowthParticles(level, pos, 15);
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            return InteractionResult.sidedSuccess(true);
        }
        ServerLevel server = (ServerLevel) level;
        boolean used = false;
        if (block instanceof SaplingBlock sapling) {
            if (state.hasProperty(BlockStateProperties.STAGE)) {
                server.setBlock(pos, state.setValue(BlockStateProperties.STAGE, 1), 3);
            }
            sapling.performBonemeal(server, server.random, pos, server.getBlockState(pos));
            used = true;
        } else if (block instanceof StemBlock) {
            server.setBlock(pos, state.setValue(BlockStateProperties.AGE_7, 7), 3);
            for (int i = 0; i < 16; i++) {
                BlockState current = server.getBlockState(pos);
                if (!(current.getBlock() instanceof StemBlock)) {
                    break;
                }
                current.randomTick(server, pos, server.random);
            }
            used = true;
        } else if (block instanceof CropBlock crop) {
            server.setBlock(pos, crop.getStateForAge(crop.getMaxAge()), 3);
            used = true;
        } else if (block instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(server, pos, state, false)) {
            bonemealable.performBonemeal(server, server.random, pos, state);
            used = true;
        }
        if (!used) {
            return InteractionResult.PASS;
        }
        if (player != null && !player.getAbilities().instabuild) {
            ItemStack stack = context.getItemInHand();
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(false);
    }
}
