package com.sammy.minersdelight.logic;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.registry.MDBlocks;
import com.sammy.minersdelight.registry.MDItems;
import com.sammy.minersdelight.registry.MDPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Common setup: brewing, compost chances, the potted gossypium and the cauldron rules for the water and snow cups. */
public final class MDSetup {
    private MDSetup() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MDPotions.registerBrewing();
            ComposterBlock.COMPOSTABLES.put(MDItems.CAVE_CARROT.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.BAKED_CAVE_CARROT.get(), 0.85F);
            ComposterBlock.COMPOSTABLES.put(MDItems.GOSSYPIUM.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(MDItems.SPIDER_LEG.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(MDItems.CRUNCHY_BAR.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.NUTRITIONAL_BAR.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(MDItems.GOLDEN_NUTRITIONAL_BAR.get(), 0.65F);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(MinersDelightMod.path("gossypium"), MDBlocks.POTTED_GOSSYPIUM);
            registerCauldronInteractions();
        });
    }

    /** A full cup fills an empty cauldron completely; an empty cup takes a full cauldron's whole content. */
    private static void registerCauldronInteractions() {
        CauldronInteraction.EMPTY.put(MDItems.WATER_CUP.get(), (state, level, pos, player, hand, stack) -> pour(level, pos, player, hand, stack,
                Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY));
        CauldronInteraction.EMPTY.put(MDItems.POWDER_SNOW_CUP.get(), (state, level, pos, player, hand, stack) -> pour(level, pos, player, hand, stack,
                Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW));
        CauldronInteraction.WATER.put(MDItems.COPPER_CUP.get(), (state, level, pos, player, hand, stack) -> CauldronInteraction.fillBucket(state, level, pos,
                player, hand, stack, new ItemStack(MDItems.WATER_CUP.get()), full -> full.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        CauldronInteraction.POWDER_SNOW.put(MDItems.COPPER_CUP.get(), (state, level, pos, player, hand, stack) -> CauldronInteraction.fillBucket(state, level,
                pos, player, hand, stack, new ItemStack(MDItems.POWDER_SNOW_CUP.get()), full -> full.getValue(LayeredCauldronBlock.LEVEL) == 3,
                SoundEvents.BUCKET_FILL_POWDER_SNOW));
    }

    private static InteractionResult pour(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, BlockState filled, SoundEvent sound) {
        if (!level.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(MDItems.COPPER_CUP.get())));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            level.setBlockAndUpdate(pos, filled);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
