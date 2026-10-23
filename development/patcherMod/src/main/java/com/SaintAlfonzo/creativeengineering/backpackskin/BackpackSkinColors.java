package com.SaintAlfonzo.creativeengineering.backpackskin;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;

/**
 * Replaces Sophisticated Backpacks' block colour handler (registered last, so it wins) with one that gives a
 * skinned backpack the skin block's colours and otherwise colours cloth and border exactly as SB does.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BackpackSkinColors {

    private BackpackSkinColors() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        event.register((state, level, pos, tint) -> {
            if (tint < 0 || pos == null || level == null) return -1;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof BackpackBlockEntity bpe)) return -1;
            Block skin = BackpackSkins.getSkin(bpe.getBackpackWrapper().getBackpack());
            if (skin != null) {
                BlockState ss = BackpackSkins.skinState(skin, state);
                return colors.getColor(ss, level, pos, tint);
            }
            if (tint > 1) return -1;
            return tint == 0 ? bpe.getBackpackWrapper().getMainColor() : bpe.getBackpackWrapper().getAccentColor();
        }, ModBlocks.BACKPACK.get(), ModBlocks.COPPER_BACKPACK.get(), ModBlocks.IRON_BACKPACK.get(),
                ModBlocks.GOLD_BACKPACK.get(), ModBlocks.DIAMOND_BACKPACK.get(), ModBlocks.NETHERITE_BACKPACK.get());
    }
}
