package net.ribs.vintagedelight.block;

import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HayBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;
import net.ribs.vintagedelight.block.custom.CheeseMoldBlock;
import net.ribs.vintagedelight.block.custom.CucumberBlock;
import net.ribs.vintagedelight.block.custom.DirtWildCropBlock;
import net.ribs.vintagedelight.block.custom.FermentingJarBlock;
import net.ribs.vintagedelight.block.custom.GhostPepperBlock;
import net.ribs.vintagedelight.block.custom.GhostPepperWildCropBlock;
import net.ribs.vintagedelight.block.custom.GoldenEggBlock;
import net.ribs.vintagedelight.block.custom.LushGrassBlock;
import net.ribs.vintagedelight.block.custom.MagicVineBlock;
import net.ribs.vintagedelight.block.custom.MasonJarBlock;
import net.ribs.vintagedelight.block.custom.OatBlock;
import net.ribs.vintagedelight.block.custom.PeanutBlock;
import net.ribs.vintagedelight.block.custom.PizzaBlock;
import net.ribs.vintagedelight.block.custom.SaltBlock;
import net.ribs.vintagedelight.block.custom.SaltLampBlock;
import net.ribs.vintagedelight.block.custom.SaltLayerBlock;
import net.ribs.vintagedelight.block.custom.SaltPillarBlock;
import net.ribs.vintagedelight.block.custom.VineNetBlock;
import net.ribs.vintagedelight.item.ModItems;
import net.ribs.vintagedelight.worldgen.tree.MagicVineGrower;
import vectorwing.farmersdelight.common.block.PieBlock;

// Upstream's block list less the gearo bush and bag. 1.20 Properties.of().mapColor(...) and the bamboo-block copies
// become 1.19.2 Materials; the 17 salt lamp classes are one class; the mason jar blocks are one class (KOTS) and their
// items are registered in ModItems under the block ids (KOTS jar rename: no separate *_mason_jar items).
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VintageDelight.MODID);

    public static final RegistryObject<Block> DEFAULT_SALT_LAMP = registerBlock("salt_lamp_default", SaltLampBlock::new);
    public static final RegistryObject<Block> BLACK_SALT_LAMP = registerBlock("salt_lamp_black", SaltLampBlock::new);
    public static final RegistryObject<Block> BLUE_SALT_LAMP = registerBlock("salt_lamp_blue", SaltLampBlock::new);
    public static final RegistryObject<Block> BROWN_SALT_LAMP = registerBlock("salt_lamp_brown", SaltLampBlock::new);
    public static final RegistryObject<Block> CYAN_SALT_LAMP = registerBlock("salt_lamp_cyan", SaltLampBlock::new);
    public static final RegistryObject<Block> GRAY_SALT_LAMP = registerBlock("salt_lamp_gray", SaltLampBlock::new);
    public static final RegistryObject<Block> GREEN_SALT_LAMP = registerBlock("salt_lamp_green", SaltLampBlock::new);
    public static final RegistryObject<Block> LIGHT_BLUE_SALT_LAMP = registerBlock("salt_lamp_light_blue", SaltLampBlock::new);
    public static final RegistryObject<Block> LIGHT_GRAY_SALT_LAMP = registerBlock("salt_lamp_light_gray", SaltLampBlock::new);
    public static final RegistryObject<Block> LIME_SALT_LAMP = registerBlock("salt_lamp_lime", SaltLampBlock::new);
    public static final RegistryObject<Block> MAGENTA_SALT_LAMP = registerBlock("salt_lamp_magenta", SaltLampBlock::new);
    public static final RegistryObject<Block> ORANGE_SALT_LAMP = registerBlock("salt_lamp_orange", SaltLampBlock::new);
    public static final RegistryObject<Block> PINK_SALT_LAMP = registerBlock("salt_lamp_pink", SaltLampBlock::new);
    public static final RegistryObject<Block> PURPLE_SALT_LAMP = registerBlock("salt_lamp_purple", SaltLampBlock::new);
    public static final RegistryObject<Block> RED_SALT_LAMP = registerBlock("salt_lamp_red", SaltLampBlock::new);
    public static final RegistryObject<Block> WHITE_SALT_LAMP = registerBlock("salt_lamp_white", SaltLampBlock::new);
    public static final RegistryObject<Block> YELLOW_SALT_LAMP = registerBlock("salt_lamp_yellow", SaltLampBlock::new);

    public static final RegistryObject<Block> OAT_BAG = registerBlock("oat_bag", () -> new Block(Properties.copy(Blocks.WHITE_WOOL)));
    public static final RegistryObject<Block> PEANUT_CRATE = registerBlock("peanut_crate",
        () -> new Block(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> CUCUMBER_CRATE = registerBlock("cucumber_crate",
        () -> new Block(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GHOST_PEPPER_CRATE = registerBlock("ghost_pepper_crate",
        () -> new Block(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> OAT_BALE = registerBlock("oat_bale", () -> new HayBlock(Properties.copy(Blocks.HAY_BLOCK)));
    public static final RegistryObject<Block> CHEESE_MOLD = registerBlock("cheese_mold",
        () -> new CheeseMoldBlock(Properties.of(Material.METAL, MaterialColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN)));
    public static final RegistryObject<Block> FERMENTING_JAR = registerBlock("fermenting_jar",
        () -> new FermentingJarBlock(Properties.copy(Blocks.GLASS).strength(0.2F).sound(SoundType.GLASS).noOcclusion()
            .lightLevel(state -> 0).isViewBlocking((state, level, pos) -> false)));

    // Wild plants: oats, peanuts and cucumbers stand on any #minecraft:dirt block (feature_writeup/vintage_delight.md S6)
    public static final RegistryObject<Block> WILD_PEANUTS = registerBlock("wild_peanuts",
        () -> new DirtWildCropBlock(MobEffects.DAMAGE_RESISTANCE, 6, Properties.copy(Blocks.TALL_GRASS)));
    public static final RegistryObject<Block> WILD_GHOST_PEPPERS = registerBlock("wild_ghost_peppers",
        () -> new GhostPepperWildCropBlock(MobEffects.FIRE_RESISTANCE, 6, Properties.copy(Blocks.WARPED_FUNGUS)));
    public static final RegistryObject<Block> WILD_CUCUMBERS = registerBlock("wild_cucumbers",
        () -> new DirtWildCropBlock(MobEffects.JUMP, 6, Properties.copy(Blocks.TALL_GRASS)));
    public static final RegistryObject<Block> WILD_OATS = registerBlock("wild_oats",
        () -> new DirtWildCropBlock(MobEffects.SATURATION, 6, Properties.copy(Blocks.TALL_GRASS)));

    public static final RegistryObject<Block> OAT_CROP = BLOCKS.register("oat_crop", () -> new OatBlock(Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> PEANUT_CROP = BLOCKS.register("peanut_crop", () -> new PeanutBlock(Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> GHOST_PEPPER_CROP = BLOCKS.register("ghost_pepper_crop", () -> new GhostPepperBlock(Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> CUCUMBER_CROP = BLOCKS.register("cucumber_crop", () -> new CucumberBlock(Properties.copy(Blocks.WHEAT)));

    public static final RegistryObject<Block> LUSH_GRASS_BLOCK = registerBlock("lush_grass_block", () -> new LushGrassBlock(Properties.copy(Blocks.GRASS_BLOCK)));

    // Mason jars: block only here, the item (same id) is in ModItems
    public static final RegistryObject<Block> EMPTY_MASON_JAR = BLOCKS.register("empty_mason_jar", () -> jar(ModItems.EMPTY_MASON_JAR));
    public static final RegistryObject<Block> VINEGAR_JAR = BLOCKS.register("vinegar_jar", () -> jar(ModItems.VINEGAR_JAR));
    public static final RegistryObject<Block> PEPPER_JAM_JAR = BLOCKS.register("pepper_jam_jar", () -> jar(ModItems.PEPPER_JAM_JAR));
    public static final RegistryObject<Block> SWEET_BERRY_JAM_JAR = BLOCKS.register("sweet_berry_jam_jar", () -> jar(ModItems.SWEET_BERRY_JAM_JAR));
    public static final RegistryObject<Block> GLOW_BERRY_JAM_JAR = BLOCKS.register("glow_berry_jam_jar", () -> jar(ModItems.GLOW_BERRY_JAM_JAR));
    public static final RegistryObject<Block> APPLE_SAUCE_JAR = BLOCKS.register("apple_sauce_jar", () -> jar(ModItems.APPLE_SAUCE_JAR));
    public static final RegistryObject<Block> NUT_MASH_JAR = BLOCKS.register("nut_mash_jar", () -> jar(ModItems.NUT_MASH_JAR));
    public static final RegistryObject<Block> RELISH_JAR = BLOCKS.register("relish_jar", () -> jar(ModItems.RELISH_JAR));
    public static final RegistryObject<Block> HONEY_JAR = BLOCKS.register("honey_jar", () -> jar(ModItems.HONEY_JAR));
    // Pack additions (CreativeEngineering2 dishes/jams #4), not in upstream Vintage Delight
    public static final RegistryObject<Block> GREEN_GRAPE_JAM_JAR = BLOCKS.register("green_grape_jam_jar", () -> jar(ModItems.GREEN_GRAPE_JAM_JAR));
    public static final RegistryObject<Block> RED_GRAPE_JAM_JAR = BLOCKS.register("red_grape_jam_jar", () -> jar(ModItems.RED_GRAPE_JAM_JAR));
    public static final RegistryObject<Block> CHERRY_JAM_JAR = BLOCKS.register("cherry_jam_jar", () -> jar(ModItems.CHERRY_JAM_JAR));
    public static final RegistryObject<Block> SALMONBERRY_JAM_JAR = BLOCKS.register("salmonberry_jam_jar", () -> jar(ModItems.SALMONBERRY_JAM_JAR));
    public static final RegistryObject<Block> RASPBERRY_JAM_JAR = BLOCKS.register("raspberry_jam_jar", () -> jar(ModItems.RASPBERRY_JAM_JAR));

    public static final RegistryObject<Block> CHEESE_WHEEL = BLOCKS.register("cheese_wheel", () -> new PieBlock(Properties.copy(Blocks.CAKE), ModItems.CHEESE_SLICE));

    // Magic vine tree line (test set S1). Upstream copies 1.20's bamboo block; 1.19.2 has none, so plain wood.
    public static final RegistryObject<Block> MAGIC_VINE = registerBlock("magic_vine", () -> new MagicVineBlock(vineWood(MaterialColor.COLOR_GREEN)));
    public static final RegistryObject<Block> STRIPPED_MAGIC_VINE = registerBlock("stripped_magic_vine", () -> new MagicVineBlock(vineWood(MaterialColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> MAGIC_VINE_BLOCK = registerBlock("magic_vine_block", () -> new MagicVineBlock(vineWood(MaterialColor.COLOR_GREEN)));
    public static final RegistryObject<Block> STRIPPED_MAGIC_VINE_BLOCK = registerBlock("stripped_magic_vine_block", () -> new MagicVineBlock(vineWood(MaterialColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> MAGIC_PEANUT = registerBlock("magic_peanut", () -> new SaplingBlock(new MagicVineGrower(), Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> VINE_NET = registerBlock("vine_net",
        () -> new VineNetBlock(Properties.copy(Blocks.GREEN_CARPET).strength(0.2F).sound(SoundType.VINE)));
    public static final RegistryObject<Block> GOLDEN_EGG = registerBlock("golden_egg",
        () -> new GoldenEggBlock(Properties.copy(Blocks.DRAGON_EGG).strength(0.3F).sound(SoundType.GLASS)));

    public static final RegistryObject<Block> EVAPORATOR = registerBlock("evaporator", () -> new SaltBlock(Properties.copy(Blocks.STONE), 50.0F));
    public static final RegistryObject<Block> SALT_LAYER = registerBlock("salt", () -> new SaltLayerBlock(Properties.copy(Blocks.SAND)));
    public static final RegistryObject<Block> SALT_BLOCK = registerBlock("salt_block", () -> new SaltBlock(Properties.copy(Blocks.SAND), 10.0F));
    public static final RegistryObject<Block> SMOOTH_SALT_BLOCK = registerBlock("smooth_salt_block", () -> new Block(Properties.copy(Blocks.BRICKS)));
    public static final RegistryObject<Block> VINE_TILE = registerBlock("vine_tile", () -> new Block(Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> VINE_TILE_STAIRS = registerBlock("vine_tile_stairs",
        () -> new StairBlock(() -> VINE_TILE.get().defaultBlockState(), Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> VINE_TILE_SLAB = registerBlock("vine_tile_slab", () -> new SlabBlock(Properties.copy(Blocks.BRICK_SLAB)));
    public static final RegistryObject<Block> SALT_PILLAR = registerBlock("salt_pillar", () -> new SaltPillarBlock(Properties.copy(Blocks.BRICKS)));
    public static final RegistryObject<Block> SALT_BRICKS = registerBlock("salt_bricks", () -> new Block(Properties.copy(Blocks.BRICKS)));
    public static final RegistryObject<Block> SALT_BRICK_STAIRS = registerBlock("salt_brick_stairs",
        () -> new StairBlock(() -> SALT_BRICKS.get().defaultBlockState(), Properties.copy(Blocks.BRICK_STAIRS)));
    public static final RegistryObject<Block> SALT_BRICK_SLAB = registerBlock("salt_brick_slab", () -> new SlabBlock(Properties.copy(Blocks.BRICK_SLAB)));
    public static final RegistryObject<Block> SALT_BRICK_WALL = registerBlock("salt_brick_wall", () -> new WallBlock(Properties.copy(Blocks.BRICK_WALL)));
    public static final RegistryObject<Block> MIXED_SALT_BRICKS = registerBlock("mixed_salt_bricks", () -> new Block(Properties.copy(Blocks.BRICKS)));
    public static final RegistryObject<Block> MIXED_SALT_BRICK_STAIRS = registerBlock("mixed_salt_brick_stairs",
        () -> new StairBlock(() -> MIXED_SALT_BRICKS.get().defaultBlockState(), Properties.copy(Blocks.BRICK_STAIRS)));
    public static final RegistryObject<Block> MIXED_SALT_BRICK_SLAB = registerBlock("mixed_salt_brick_slab", () -> new SlabBlock(Properties.copy(Blocks.BRICK_SLAB)));
    public static final RegistryObject<Block> MIXED_SALT_BRICK_WALL = registerBlock("mixed_salt_brick_wall", () -> new WallBlock(Properties.copy(Blocks.BRICK_WALL)));
    public static final RegistryObject<Block> CHEESE_PIZZA = BLOCKS.register("cheese_pizza", () -> new PizzaBlock(Properties.copy(Blocks.CAKE), ModItems.CHEESE_PIZZA_SLICE));
    public static final RegistryObject<Block> MEAT_PIZZA = BLOCKS.register("meat_pizza", () -> new PizzaBlock(Properties.copy(Blocks.CAKE), ModItems.MEAT_PIZZA_SLICE));

    private static Properties vineWood(MaterialColor color) {
        return Properties.of(Material.WOOD, color).strength(1.5F).sound(SoundType.WOOD);
    }

    private static MasonJarBlock jar(Supplier<? extends Item> item) {
        return new MasonJarBlock(Properties.copy(Blocks.GLASS).strength(0.3F).sound(SoundType.GLASS), item);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), ModItems.props()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
