package com.SaintAlfonzo.creativedelight.oceanic.registry;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.SaintAlfonzo.creativedelight.oceanic.content.SeaGrapeFruitBlock;
import com.SaintAlfonzo.creativedelight.oceanic.content.SeaGrapePlantBlock;
import com.SaintAlfonzo.creativedelight.oceanic.content.ServingPlateBlock;
import com.SaintAlfonzo.creativedelight.oceanic.content.SpongeCakeBlock;
import com.SaintAlfonzo.creativedelight.oceanic.content.WildSeaGrapeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The eight Oceanic Delight blocks, upstream ids and block state properties (old worlds load unchanged). */
public final class OceanicBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, OceanicDelightMod.MODID);

    private static final VoxelShape PAN = Block.box(1, 0, 1, 15, 4, 15);
    private static final VoxelShape[] PUFFERFISH = {
            Block.box(3, 0, 3, 13, 4, 13), Block.box(3, 0, 3, 13, 4, 13), Block.box(3, 0, 3, 13, 6, 13),
            Block.box(3, 0, 3, 13, 8, 13), Block.box(3, 0, 3, 13, 10, 13)};

    public static final RegistryObject<Block> SEA_PICKLE_CRATE = BLOCKS.register("sea_pickle_crate", () -> new Block(crate()));
    public static final RegistryObject<Block> SPONGE_CAKE = BLOCKS.register("sponge_cake", () -> new SpongeCakeBlock(
            Properties.of(Material.CAKE).sound(SoundType.WOOL).strength(0.5F).noOcclusion().noLootTable().isRedstoneConductor((s, l, p) -> false)));
    public static final RegistryObject<Block> PAELLA_PLATE = BLOCKS.register("paella_plate", () -> new ServingPlateBlock(
            Properties.of(Material.METAL, MaterialColor.NONE).sound(SoundType.LANTERN).strength(1.25F, 11.0F).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> false),
            OceanicItems.BOWL_OF_PAELLA, true, servings -> PAN));
    public static final RegistryObject<Block> STUFFED_PUFFERFISH = BLOCKS.register("stuffed_pufferfish", () -> new ServingPlateBlock(
            Properties.of(Material.CAKE, MaterialColor.NONE).sound(SoundType.WOOL).strength(0.5F, 6.0F).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> false),
            OceanicItems.PLATE_OF_STUFFED_PUFFERFISH, false, servings -> PUFFERFISH[servings]));
    public static final RegistryObject<Block> SEA_GRAPE_PLANT = BLOCKS.register("sea_grape_plant", () -> new SeaGrapePlantBlock(
            plant().strength(0.0F, 1.0F).noCollission().randomTicks()));
    public static final RegistryObject<Block> SEA_GRAPE_FRUIT = BLOCKS.register("sea_grape_fruit", () -> new SeaGrapeFruitBlock(
            plant().strength(0.15F, 3.5F).lightLevel(s -> 2)));
    public static final RegistryObject<Block> SEA_GRAPE_CRATE = BLOCKS.register("sea_grape_crate", () -> new Block(crate()));
    public static final RegistryObject<Block> WILD_SEA_GRAPE = BLOCKS.register("wild_sea_grape", () -> new WildSeaGrapeBlock(
            plant().instabreak().noCollission().dynamicShape().offsetType(BlockBehaviour.OffsetType.XZ)));

    private OceanicBlocks() {
    }

    private static Properties crate() {
        return Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(2.0F, 3.0F);
    }

    private static Properties plant() {
        return Properties.of(Material.WATER_PLANT, MaterialColor.NONE).sound(SoundType.WET_GRASS).noOcclusion()
                .isRedstoneConductor((s, l, p) -> false);
    }
}
