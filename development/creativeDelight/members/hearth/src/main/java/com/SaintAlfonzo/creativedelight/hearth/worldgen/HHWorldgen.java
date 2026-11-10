package com.SaintAlfonzo.creativedelight.hearth.worldgen;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.worldgen.structure.CornMazePiece;
import com.SaintAlfonzo.creativedelight.hearth.worldgen.structure.CornMazeStructure;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Worldgen of the Hearth and Harvest port (hearth_and_harvest.md 2.2, 3.2, N23-8, N24-11): the salt cave feature, the
 * Corn Maze structure and the placeable Four-Leaf Clover block. Rates, biomes and spacing live in the pack tables
 * (wild_flowers.json, structure_groups.json, structure_tags.json); the salt caves' in hearthandharvest-common.toml.
 */
public final class HHWorldgen {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, HearthAndHarvestMod.MODID);

    /** Placed by Buzzier Bees' Four-Leaf Clover item (no item of its own: the block drops and picks that item). */
    public static final RegistryObject<Block> FOUR_LEAF_CLOVER = BLOCKS.register("four_leaf_clover", FourLeafCloverBlock::new);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SALT_CAVE =
            FEATURES.register("salt_cave", () -> new SaltCaveFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<StructureType<CornMazeStructure>> CORN_MAZE =
            STRUCTURE_TYPES.register("corn_maze", () -> () -> CornMazeStructure.CODEC);

    public static final RegistryObject<StructurePieceType> CORN_MAZE_PIECE =
            STRUCTURE_PIECES.register("corn_maze_piece", () -> CornMazePiece::new);

    private HHWorldgen() {
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        FEATURES.register(bus);
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECES.register(bus);
        MinecraftForge.EVENT_BUS.addListener(FourLeafCloverBlock::onRightClickBlock);
    }
}
