package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** Plants another mod's block, found by id at use time (D3: Blueberry Pips plant Fruits Delight's bush). */
public class ForeignPlantItem extends Item {
    private final ResourceLocation plant;

    public ForeignPlantItem(ResourceLocation plant, Properties properties) {
        super(properties);
        this.plant = plant;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Block block = ForgeRegistries.BLOCKS.getValue(plant);
        if (block == null) {
            return InteractionResult.PASS;
        }
        BlockPlaceContext place = new BlockPlaceContext(context);
        if (!place.canPlace()) {
            return InteractionResult.PASS;
        }
        BlockState state = block.getStateForPlacement(place);
        if (state == null || !state.canSurvive(context.getLevel(), place.getClickedPos())) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            context.getLevel().setBlock(place.getClickedPos(), state, 11);
            context.getLevel().playSound(null, place.getClickedPos(), state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
