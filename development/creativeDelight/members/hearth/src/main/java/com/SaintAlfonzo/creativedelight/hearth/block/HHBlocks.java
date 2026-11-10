package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.CanvasRugBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.WildCropBlock;

/** Blocks, ids and base properties as 1.4.1. The kept list is feature_writeup/hearth_and_harvest.md 2.1 and 3.1. */
public final class HHBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<Block> RASPBERRY_BUSH = BLOCKS.register("raspberry_bush",
            () -> new RaspberryBushBlock(Properties.copy(Blocks.SWEET_BERRY_BUSH)));

    public static final RegistryObject<Block> TREE_TAPPER = BLOCKS.register("tree_tapper",
            () -> new TreeTapperBlock(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> SAP_CAULDRON = BLOCKS.register("sap_cauldron",
            () -> new SapCauldronBlock(Properties.copy(Blocks.CAULDRON).strength(2.0F, 3.0F).sound(SoundType.METAL)));

    // Pies, waffle, cheese
    public static final RegistryObject<Block> RASPBERRY_PIE = BLOCKS.register("raspberry_pie",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), HHItems.RASPBERRY_PIE_SLICE));
    public static final RegistryObject<Block> GRAPE_PIE = BLOCKS.register("grape_pie",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), HHItems.GRAPE_PIE_SLICE));
    public static final RegistryObject<Block> PEANUT_BUTTER_PIE = BLOCKS.register("peanut_butter_pie",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), HHItems.PEANUT_BUTTER_PIE_SLICE));
    public static final RegistryObject<Block> WAFFLE = BLOCKS.register("waffle",
            () -> new FoodStackBlock(Properties.copy(Blocks.CAKE)));
    public static final RegistryObject<Block> GOAT_CHEESE_WHEEL = BLOCKS.register("goat_cheese_wheel",
            () -> new CheeseWheelBlock(HHItems.GOAT_CHEESE_SLICE, Properties.copy(Blocks.CAKE)));
    public static final RegistryObject<Block> UNRIPE_GOAT_CHEESE_WHEEL = BLOCKS.register("unripe_goat_cheese_wheel",
            () -> new UnripeCheeseWheelBlock(GOAT_CHEESE_WHEEL, Properties.copy(Blocks.CAKE)));

    // Hops on Vinery's lattice (N21), sunflower stem (N23-16), tilled end stone (N23-12)
    public static final RegistryObject<Block> HOPS = BLOCKS.register("hops",
            () -> new HopsLatticeBlock(Properties.of(Material.PLANT).strength(0.2F).noOcclusion().randomTicks().sound(SoundType.CROP)));
    public static final RegistryObject<Block> SUNFLOWER_STEM = BLOCKS.register("sunflower_stem",
            () -> new SunflowerStemBlock(Properties.of(Material.PLANT).noCollission().instabreak().randomTicks().sound(SoundType.STEM)));
    public static final RegistryObject<Block> TILLED_END_STONE = BLOCKS.register("tilled_end_stone",
            () -> new TilledEndStoneBlock(Properties.copy(Blocks.END_STONE).randomTicks().isViewBlocking((s, l, p) -> true).isSuffocating((s, l, p) -> true)));

    // Rugs, storage
    public static final RegistryObject<Block> HAY_RUG = rug("hay_rug");
    public static final RegistryObject<Block> STRAW_RUG = rug("straw_rug");
    public static final RegistryObject<Block> HOPS_BAG = BLOCKS.register("hops_bag", () -> new Block(Properties.copy(Blocks.WHITE_WOOL)));
    public static final RegistryObject<Block> CORN_HUSK_BUNDLE = BLOCKS.register("corn_husk_bundle",
            () -> new Block(Properties.copy(Blocks.DRIED_KELP_BLOCK).strength(2.0F, 3.0F).sound(SoundType.CROP)));
    public static final RegistryObject<Block> ROPE_COIL = pillar("rope_coil");
    public static final RegistryObject<Block> SPOOL = pillar("spool");
    public static final RegistryObject<Block> CRIMSON_FUNGUS_CRATE = crateSlab("crimson_fungus_crate");
    public static final RegistryObject<Block> WARPED_FUNGUS_CRATE = crateSlab("warped_fungus_crate");
    public static final RegistryObject<Block> GLISTERING_MELON_CRATE = crate("glistering_melon_crate");
    public static final RegistryObject<Block> POISONOUS_POTATO_CRATE = crate("poisonous_potato_crate");
    public static final RegistryObject<Block> ROTTEN_TOMATO_CRATE = crate("rotten_tomato_crate");

    // Manure family. The randomTicks() upstream set on the block and bricks did nothing and is dropped (B11).
    public static final RegistryObject<Block> MANURE_BAG = BLOCKS.register("manure_bag", () -> new Block(Properties.copy(Blocks.WHITE_WOOL)));
    public static final RegistryObject<Block> MANURE_BLOCK = BLOCKS.register("manure_block",
            () -> new Block(Properties.copy(Blocks.MUD).strength(1.0F, 2.0F).sound(SoundType.MUD)));
    public static final RegistryObject<Block> MANURE_BRICKS = brick("manure_bricks");
    public static final RegistryObject<Block> MANURE_BRICK_SLAB = BLOCKS.register("manure_brick_slab", () -> new SlabBlock(brickProps()));
    public static final RegistryObject<Block> MANURE_BRICK_STAIRS = BLOCKS.register("manure_brick_stairs",
            () -> new StairBlock(() -> MANURE_BRICKS.get().defaultBlockState(), brickProps()));
    public static final RegistryObject<Block> MANURE_BRICK_WALL = BLOCKS.register("manure_brick_wall", () -> new WallBlock(brickProps()));
    public static final RegistryObject<Block> POLISHED_MANURE = brick("polished_manure");
    public static final RegistryObject<Block> POLISHED_MANURE_SLAB = BLOCKS.register("polished_manure_slab", () -> new SlabBlock(brickProps()));
    public static final RegistryObject<Block> POLISHED_MANURE_STAIRS = BLOCKS.register("polished_manure_stairs",
            () -> new StairBlock(() -> POLISHED_MANURE.get().defaultBlockState(), brickProps()));
    public static final RegistryObject<Block> POLISHED_MANURE_WALL = BLOCKS.register("polished_manure_wall", () -> new WallBlock(brickProps()));

    // Plants
    public static final RegistryObject<Block> WILD_HOPS = BLOCKS.register("wild_hops",
            () -> new WildCropBlock(MobEffects.MOVEMENT_SPEED, 10, Properties.copy(Blocks.TALL_GRASS)));

    public static final Map<String, RegistryObject<Block>> MUMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Block>> POTTED_MUMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Block>> HALF_CABINETS = new LinkedHashMap<>();

    static {
        for (String colour : new String[]{"white", "red", "orange", "yellow", "light_blue", "blue", "purple", "pink"}) {
            String id = colour + "_mum";
            RegistryObject<Block> mum = BLOCKS.register(id, () -> new FlowerBlock(MobEffects.HEAL, 10, Properties.copy(Blocks.POPPY)));
            MUMS.put(id, mum);
            POTTED_MUMS.put(id, BLOCKS.register("potted_" + id,
                    () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, mum, Properties.copy(Blocks.POTTED_POPPY))));
        }
        for (String wood : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "crimson", "warped"}) {
            String id = wood + "_half_cabinet";
            HALF_CABINETS.put(id, BLOCKS.register(id, () -> new HalfCabinetBlock(Properties.copy(Blocks.BARREL))));
        }
    }

    /** Vanilla pot lookup, so a mum on an empty pot converts (common setup). */
    public static void registerPottedPlants() {
        FlowerPotBlock empty = (FlowerPotBlock) Blocks.FLOWER_POT;
        MUMS.forEach((id, mum) -> empty.addPlant(new ResourceLocation(HearthAndHarvestMod.MODID, id), POTTED_MUMS.get(id)));
    }

    private static Properties brickProps() {
        return Properties.copy(Blocks.MUD_BRICKS).strength(2.5F, 3.0F).sound(SoundType.MUD_BRICKS);
    }

    private static RegistryObject<Block> brick(String id) {
        return BLOCKS.register(id, () -> new Block(brickProps()));
    }

    private static RegistryObject<Block> rug(String id) {
        return BLOCKS.register(id, () -> new CanvasRugBlock(Properties.copy(Blocks.WHITE_CARPET).sound(SoundType.GRASS).strength(0.2F)));
    }

    private static RegistryObject<Block> pillar(String id) {
        return BLOCKS.register(id, () -> new RotatedPillarBlock(Properties.copy(Blocks.WHITE_WOOL).strength(2.0F, 3.0F).sound(SoundType.WOOL)));
    }

    private static RegistryObject<Block> crate(String id) {
        return BLOCKS.register(id, () -> new Block(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    }

    private static RegistryObject<Block> crateSlab(String id) {
        return BLOCKS.register(id, () -> new SlabBlock(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    }

    private HHBlocks() {
    }
}
