package com.SaintAlfonzo.creativedelight.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Farmer's Respite 2.0 registers {@code potted_wild_tea_bush} (block, model, loot) but never calls
 * {@link FlowerPotBlock#addPlant}, so a Wild Tea Bush does nothing on a flower pot. Registers it by id, without
 * Respite's classes (feature_writeup/farmers_respite.md 2.2). The coffee bushes stay out: Respite's coffee is purged.
 */
public final class RespitePottedTeaBush {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation PLANT = new ResourceLocation("farmersrespite", "wild_tea_bush");
    private static final ResourceLocation POTTED = new ResourceLocation("farmersrespite", "potted_wild_tea_bush");

    private RespitePottedTeaBush() {}

    public static void register() {
        Block potted = ForgeRegistries.BLOCKS.getValue(POTTED);
        if (potted == null || potted == Blocks.AIR || !(Blocks.FLOWER_POT instanceof FlowerPotBlock pot)) {
            LOGGER.warn("[respite] {} not found, Wild Tea Bush can't go in a flower pot", POTTED);
            return;
        }
        pot.addPlant(PLANT, () -> potted);
    }
}
