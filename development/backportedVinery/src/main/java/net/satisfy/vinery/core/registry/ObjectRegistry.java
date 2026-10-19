package net.satisfy.vinery.core.registry;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.foundation.armor.TexturedArmorItem;
import net.satisfy.foundation.banner.BannerSettings;
import net.satisfy.foundation.banner.CompletionistBannerBlock;
import net.satisfy.foundation.banner.CompletionistWallBannerBlock;
import net.satisfy.foundation.block.BigTableBlock;
import net.satisfy.foundation.block.CabinetBlock;
import net.satisfy.foundation.block.ChairBlock;
import net.satisfy.foundation.block.FacingBlock;
import net.satisfy.foundation.block.WindowBlock;
import net.satisfy.foundation.storage.WallShelfBlock;
import net.satisfy.foundation.util.LibUtil;
import net.satisfy.foundation.wood.WoodBoatItem;
import net.satisfy.foundation.wood.WoodStandingSignBlock;
import net.satisfy.foundation.wood.WoodWallSignBlock;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.*;
import net.satisfy.vinery.core.block.state.properties.VineryWoodType;
import net.satisfy.vinery.core.item.*;
import net.satisfy.vinery.core.world.VineryTreeGrower;
import net.satisfy.vinery.core.world.VineryWorldGen;
import net.satisfy.vinery.legacy.block.*;
import net.satisfy.vinery.platform.PlatformHelper;

import java.util.function.Supplier;

/**
 * Port of upstream's ObjectRegistry with the D1 / D2 id map (feature_writeup/alcohol.md B4.1):
 * <ul>
 *     <li>Field names stay as upstream, so the ported code reads the same; the registered ids are the 1.3.10 ones
 *     where 1.5.5 only renamed something: {@code dark_cherry_*} registers as {@code cherry_*}, the Apple Press as
 *     {@code wine_press}, the juices and the winemaker armor under their 1.3.10 ids.</li>
 *     <li>1.5.5's vanilla-cherry lattice and racks merge into the dark cherry ones ({@code cherry_lattice},
 *     {@code cherry_wine_rack_*}); 1.19.2 has no vanilla cherry. The hanging sign is dropped (D9).</li>
 *     <li>Every 1.3.10 block 1.5.5 removed stays, at the end of the file (net.satisfy.vinery.legacy).</li>
 * </ul>
 */
public class ObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Vinery.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Vinery.MOD_ID);

    public static final RegistryObject<Block> DARK_CHERRY_SIGN = registerWithoutItem("cherry_sign", () -> new WoodStandingSignBlock(VineryWoodType.DARK_CHERRY, BlockBehaviour.Properties.copy(Blocks.OAK_SIGN), EntityTypeRegistry.MOD_SIGN));
    public static final RegistryObject<Block> DARK_CHERRY_WALL_SIGN = registerWithoutItem("cherry_wall_sign", () -> new WoodWallSignBlock(VineryWoodType.DARK_CHERRY, BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).lootFrom(DARK_CHERRY_SIGN), EntityTypeRegistry.MOD_SIGN));
    public static final RegistryObject<Item> DARK_CHERRY_SIGN_ITEM = registerItem("cherry_sign", () -> new SignItem(getSettings().stacksTo(16), ObjectRegistry.DARK_CHERRY_SIGN.get(), ObjectRegistry.DARK_CHERRY_WALL_SIGN.get()));
    public static final RegistryObject<Block> RED_GRAPE_BUSH = registerWithoutItem("red_grape_bush", () -> new GrapeBushBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.RED));
    public static final RegistryObject<Item> RED_GRAPE_SEEDS = registerItem("red_grape_seeds", () -> new GrapeBushSeedItem(RED_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.RED));
    public static final RegistryObject<Item> RED_GRAPE = registerItem("red_grape", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.RED, RED_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> WHITE_GRAPE_BUSH = registerWithoutItem("white_grape_bush", () -> new GrapeBushBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.WHITE));
    public static final RegistryObject<Item> WHITE_GRAPE_SEEDS = registerItem("white_grape_seeds", () -> new GrapeBushSeedItem(WHITE_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.WHITE));
    public static final RegistryObject<Item> WHITE_GRAPE = registerItem("white_grape", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.WHITE, WHITE_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> SAVANNA_RED_GRAPE_BUSH = registerWithoutItem("savanna_grape_bush_red", () -> new GrapeBushBlock.SavannaGrapeBush(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.SAVANNA_RED));
    public static final RegistryObject<Item> SAVANNA_RED_GRAPE_SEEDS = registerItem("savanna_grape_seeds_red", () -> new GrapeBushSeedItem(SAVANNA_RED_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.SAVANNA_RED));
    public static final RegistryObject<Item> SAVANNA_RED_GRAPE = registerItem("savanna_grapes_red", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.SAVANNA_RED, ObjectRegistry.SAVANNA_RED_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> SAVANNA_WHITE_GRAPE_BUSH = registerWithoutItem("savanna_grape_bush_white", () -> new GrapeBushBlock.SavannaGrapeBush(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.SAVANNA_WHITE));
    public static final RegistryObject<Item> SAVANNA_WHITE_GRAPE_SEEDS = registerItem("savanna_grape_seeds_white", () -> new GrapeBushSeedItem(SAVANNA_WHITE_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.SAVANNA_WHITE));
    public static final RegistryObject<Item> SAVANNA_WHITE_GRAPE = registerItem("savanna_grapes_white", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.SAVANNA_WHITE, ObjectRegistry.SAVANNA_WHITE_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> TAIGA_RED_GRAPE_BUSH = registerWithoutItem("taiga_grape_bush_red", () -> new GrapeBushBlock.TaigaGrapeBush(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.TAIGA_RED));
    public static final RegistryObject<Item> TAIGA_RED_GRAPE_SEEDS = registerItem("taiga_grape_seeds_red", () -> new GrapeBushSeedItem(TAIGA_RED_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.TAIGA_RED));
    public static final RegistryObject<Item> TAIGA_RED_GRAPE = registerItem("taiga_grapes_red", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.TAIGA_RED, ObjectRegistry.TAIGA_RED_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> TAIGA_WHITE_GRAPE_BUSH = registerWithoutItem("taiga_grape_bush_white", () -> new GrapeBushBlock.TaigaGrapeBush(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.TAIGA_WHITE));
    public static final RegistryObject<Item> TAIGA_WHITE_GRAPE_SEEDS = registerItem("taiga_grape_seeds_white", () -> new GrapeBushSeedItem(TAIGA_WHITE_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.TAIGA_WHITE));
    public static final RegistryObject<Item> TAIGA_WHITE_GRAPE = registerItem("taiga_grapes_white", () -> new GrapeItem(getSettings().food(Foods.SWEET_BERRIES), GrapeTypeRegistry.TAIGA_WHITE, ObjectRegistry.TAIGA_WHITE_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> JUNGLE_RED_GRAPE_BUSH = registerWithoutItem("jungle_grape_bush_red", () -> new GrapeVineBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.JUNGLE_RED));
    public static final RegistryObject<Item> JUNGLE_RED_GRAPE_SEEDS = registerItem("jungle_grape_seeds_red", () -> new GrapeBushSeedItem(JUNGLE_RED_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.JUNGLE_RED));
    public static final RegistryObject<Item> JUNGLE_RED_GRAPE = registerItem("jungle_grapes_red", () -> new GrapeItem(getSettings().food(Foods.BAKED_POTATO), GrapeTypeRegistry.JUNGLE_RED, ObjectRegistry.JUNGLE_RED_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> JUNGLE_WHITE_GRAPE_BUSH = registerWithoutItem("jungle_grape_bush_white", () -> new GrapeVineBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), GrapeTypeRegistry.JUNGLE_WHITE));
    public static final RegistryObject<Item> JUNGLE_WHITE_GRAPE_SEEDS = registerItem("jungle_grape_seeds_white", () -> new GrapeBushSeedItem(JUNGLE_WHITE_GRAPE_BUSH.get(), getSettings(), GrapeTypeRegistry.JUNGLE_WHITE));
    public static final RegistryObject<Item> JUNGLE_WHITE_GRAPE = registerItem("jungle_grapes_white", () -> new GrapeItem(getSettings().food(Foods.BAKED_POTATO), GrapeTypeRegistry.JUNGLE_WHITE, ObjectRegistry.JUNGLE_WHITE_GRAPE_SEEDS.get()));
    public static final RegistryObject<Block> DARK_CHERRY_SAPLING = registerWithItem("cherry_sapling", () -> new SaplingBlock(new VineryTreeGrower(VineryWorldGen.DARK_CHERRY_KEY, VineryWorldGen.DARK_CHERRY_KEY), BlockBehaviour.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> APPLE_TREE_SAPLING = registerWithItem("apple_tree_sapling", () -> new SaplingBlock(new VineryTreeGrower(VineryWorldGen.APPLE_KEY, VineryWorldGen.APPLE_VARIANT_KEY), BlockBehaviour.Properties.of(Material.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final RegistryObject<Item> CHERRY = registerItem("cherry", () -> new Item(getSettings().food(Foods.COOKIE)));
    public static final RegistryObject<Item> ROTTEN_CHERRY = registerItem("rotten_cherry", () -> new RottenCherryItem(getSettings().food(Foods.POISONOUS_POTATO)));
    public static final RegistryObject<Block> GRAPEVINE_LEAVES = registerWithItem("grapevine_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> DARK_CHERRY_LEAVES = registerWithItem("cherry_leaves", () -> new DarkCherryLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> APPLE_LEAVES = registerWithItem("apple_leaves", () -> new AppleLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> WHITE_GRAPE_BAG = registerWithItem("white_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> RED_GRAPE_BAG = registerWithItem("red_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> CHERRY_BAG = registerWithItem("cherry_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> APPLE_BAG = registerWithItem("apple_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> GRAPEVINE_POT = registerWithItem("grapevine_pot", () -> new GrapevinePotBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Block> FERMENTATION_BARREL = registerWithItem("fermentation_barrel", () -> new FermentationBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL).noOcclusion()));
    public static final RegistryObject<Block> APPLE_PRESS = registerWithItem("wine_press", () -> new ApplePressBlock(wood(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> DARK_CHERRY_CHAIR = registerWithItem("cherry_chair", () -> new ChairBlock(wood(2.0F, 3.0F)));
    public static final RegistryObject<Block> DARK_CHERRY_TABLE = registerWithItem("cherry_table", () -> new TableBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> DARK_CHERRY_CABINET = registerWithItem("cherry_cabinet", () -> new CabinetBlock(wood(2.0F, 3.0F), EntityTypeRegistry.CABINET_BLOCK_ENTITY, SoundEventRegistry.CABINET_OPEN, SoundEventRegistry.CABINET_CLOSE));
    public static final RegistryObject<Block> DARK_CHERRY_DRAWER = registerWithItem("cherry_drawer", () -> new CabinetBlock(wood(2.0F, 3.0F), EntityTypeRegistry.CABINET_BLOCK_ENTITY, SoundEventRegistry.DRAWER_OPEN, SoundEventRegistry.DRAWER_CLOSE));
    public static final RegistryObject<Block> DARK_CHERRY_WINE_RACK_BIG = registerWithItem("cherry_wine_rack_big", () -> new BottleStorageBlock(wood(2.0F, 3.0F).noOcclusion(), BottleStorageBlock.Layout.NINE));
    public static final RegistryObject<Block> DARK_CHERRY_WINE_RACK_SMALL = registerWithItem("cherry_wine_rack_small", () -> new BottleStorageBlock(wood(2.0F, 3.0F).noOcclusion(), BottleStorageBlock.Layout.FOUR));
    public static final RegistryObject<Block> DARK_CHERRY_WINE_RACK_MID = registerWithItem("cherry_wine_rack_mid", () -> new BigBottleStorageBlock(wood(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> DARK_CHERRY_BARREL = registerWithItem("cherry_barrel", () -> new DarkCherryBarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Block> APPLE_LOG = registerWithItem("apple_log", LibUtil::logBlock);
    public static final RegistryObject<Block> APPLE_WOOD = registerWithItem("apple_wood", LibUtil::logBlock);
    public static final RegistryObject<Block> STRIPPED_DARK_CHERRY_LOG = registerWithItem("stripped_cherry_log", LibUtil::logBlock);
    public static final RegistryObject<Block> DARK_CHERRY_LOG = registerWithItem("cherry_log", LibUtil::logBlock);
    public static final RegistryObject<Block> STRIPPED_DARK_CHERRY_WOOD = registerWithItem("stripped_cherry_wood", LibUtil::logBlock);
    public static final RegistryObject<Block> DARK_CHERRY_WOOD = registerWithItem("cherry_wood", LibUtil::logBlock);
    public static final RegistryObject<Block> DARK_CHERRY_BEAM = registerWithItem("cherry_beam", LibUtil::logBlock);
    public static final RegistryObject<Block> DARK_CHERRY_PLANKS = registerWithItem("cherry_planks", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_RED).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> DARK_CHERRY_FLOORBOARD = registerWithItem("cherry_floorboard", () -> new Block(BlockBehaviour.Properties.copy(DARK_CHERRY_PLANKS.get())));
    public static final RegistryObject<Block> DARK_CHERRY_STAIRS = registerWithItem("cherry_stairs", () -> new StairBlock(() -> DARK_CHERRY_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(DARK_CHERRY_PLANKS.get())));
    public static final RegistryObject<Block> DARK_CHERRY_SLAB = registerWithItem("cherry_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(DARK_CHERRY_PLANKS.get())));
    public static final RegistryObject<Block> DARK_CHERRY_FENCE = registerWithItem("cherry_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> DARK_CHERRY_FENCE_GATE = registerWithItem("cherry_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> DARK_CHERRY_BUTTON = registerWithItem("cherry_button", () -> new WoodButtonBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().strength(0.5F).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> DARK_CHERRY_PRESSURE_PLATE = registerWithItem("cherry_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE)));
    public static final RegistryObject<Block> DARK_CHERRY_DOOR = registerWithItem("cherry_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR)));
    public static final RegistryObject<Block> DARK_CHERRY_TRAPDOOR = registerWithItem("cherry_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR)));
    public static final RegistryObject<Item> DARK_CHERRY_BOAT = registerItem("cherry_boat", () -> new WoodBoatItem(EntityTypeRegistry.DARK_CHERRY_BOAT, VineryWoodType.DARK_CHERRY_ID, false, getSettings().stacksTo(1)));
    public static final RegistryObject<Item> DARK_CHERRY_CHEST_BOAT = registerItem("cherry_chest_boat", () -> new WoodBoatItem(EntityTypeRegistry.DARK_CHERRY_CHEST_BOAT, VineryWoodType.DARK_CHERRY_ID, true, getSettings().stacksTo(1)));
    public static final RegistryObject<Block> WINDOW_BLOCK = registerWithItem("window_block", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Block> WINDOW = registerWithItem("window", () -> new WindowBlock(BlockBehaviour.Properties.copy(Blocks.GLASS_PANE)));
    public static final RegistryObject<Block> COARSE_DIRT_SLAB = registerWithItem("coarse_dirt_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.COARSE_DIRT)));
    public static final RegistryObject<Block> DIRT_SLAB = registerWithItem("dirt_slab", () -> new DirtSlabBlock(BlockBehaviour.Properties.copy(Blocks.DIRT)));
    public static final RegistryObject<Block> GRASS_SLAB = registerWithItem("grass_slab", () -> new SpreadableGrassSlabBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)));
    public static final RegistryObject<Block> DIRT_PATH_SLAB = registerWithItem("dirt_path_slab", () -> new DirtPathSlabBlock(BlockBehaviour.Properties.copy(Blocks.DIRT_PATH)));
    public static final RegistryObject<Item> WINE_BOTTLE = registerItem("wine_bottle", () -> new Item(getSettings()));
    public static final RegistryObject<Item> APPLE_JUICE = registerItem("apple_juice", ObjectRegistry::juice);
    public static final RegistryObject<Item> RED_GRAPEJUICE = registerItem("red_grapejuice_wine_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> WHITE_GRAPEJUICE = registerItem("white_grapejuice_wine_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> RED_SAVANNA_GRAPEJUICE = registerItem("savanna_red_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> WHITE_SAVANNA_GRAPEJUICE = registerItem("savanna_white_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> RED_TAIGA_GRAPEJUICE = registerItem("taiga_red_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> WHITE_TAIGA_GRAPEJUICE = registerItem("taiga_white_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> RED_JUNGLE_GRAPEJUICE = registerItem("jungle_red_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Item> WHITE_JUNGLE_GRAPEJUICE = registerItem("jungle_white_grapejuice_bottle", ObjectRegistry::juice);
    public static final RegistryObject<Block> CHORUS_WINE = registerWithoutItem("chorus_wine", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> CHERRY_WINE = registerWithoutItem("cherry_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> MAGNETIC_WINE = registerWithoutItem("magnetic_wine", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> JO_SPECIAL_MIXTURE = registerWithoutItem("jo_special_mixture", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> CRISTEL_WINE = registerWithoutItem("cristel_wine", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> GLOWING_WINE = registerWithoutItem("glowing_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> CREEPERS_CRUSH = registerWithoutItem("creepers_crush", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> RED_WINE = registerWithoutItem("red_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> JELLIE_WINE = registerWithoutItem("jellie_wine", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> STAL_WINE = registerWithoutItem("stal_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> NOIR_WINE = registerWithoutItem("noir_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> BOLVAR_WINE = registerWithoutItem("bolvar_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> SOLARIS_WINE = registerWithoutItem("solaris_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> EISWEIN = registerWithoutItem("eiswein", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> CHENET_WINE = registerWithoutItem("chenet_wine", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> KELP_CIDER = registerWithoutItem("kelp_cider", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> AEGIS_WINE = registerWithoutItem("aegis_wine", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> CLARK_WINE = registerWithoutItem("clark_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> MELLOHI_WINE = registerWithoutItem("mellohi_wine", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> STRAD_WINE = registerWithoutItem("strad_wine", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> APPLE_CIDER = registerWithoutItem("apple_cider", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Block> APPLE_WINE = registerWithoutItem("apple_wine", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> LILITU_WINE = registerWithoutItem("lilitu_wine", () -> new WineBottleBlock(getWineSettings(), 1));
    public static final RegistryObject<Block> BOTTLE_MOJANG_NOIR = registerWithoutItem("bottle_mojang_noir", () -> new WineBottleBlock(getWineSettings(), 3));
    public static final RegistryObject<Block> VILLAGERS_FRIGHT = registerWithoutItem("villagers_fright", () -> new WineBottleBlock(getWineSettings(), 3));
    /**
     * D2: the 1.3.10 wine 1.5.5 removed, as a 1.5.5 wine (2 per placed block, as in 1.3.10). King Danis Wine is gone
     * (alcohol.md 10.8): 1.3.10 named it "Miss Lilitus Wine" and drew it with Lilitu's model, so it was a second Lilitu.
     */
    public static final RegistryObject<Block> PRAETORIAN_WINE = registerWithoutItem("praetorian_wine", () -> new WineBottleBlock(getWineSettings(), 2));
    public static final RegistryObject<Item> APPLE_MASH = registerItem("apple_mash", () -> new Item(getSettings().food(Foods.APPLE)));
    public static final RegistryObject<Block> GRAPEVINE_STEM = registerWithItem("grapevine_stem", () -> new PaleStemBlock(getGrapevineSettings()));
    public static final RegistryObject<Block> STORAGE_POT = registerWithItem("storage_pot", () -> new StoragePotBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD), () -> SoundEvents.DYE_USE, () -> SoundEvents.DYE_USE));
    public static final RegistryObject<Block> WINE_BOX = registerWithItem("wine_box", () -> new WineBoxBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> DARK_CHERRY_BIG_TABLE = registerWithItem("cherry_big_table", () -> new BigTableBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 2.0F)));
    public static final RegistryObject<Block> DARK_CHERRY_SHELF = registerWithItem("cherry_shelf", () -> new WallShelfBlock(wood(2.0F, 3.0F).noOcclusion(), EntityTypeRegistry.STORAGE_ENTITY, StorageTypeRegistry.SHELF, stack -> !(stack.getItem() instanceof BlockItem) || stack.is(TagRegistry.IGNORE_BLOCK_ITEM)));
    public static final RegistryObject<Block> STACKABLE_LOG = registerWithItem("stackable_log", () -> new StackableLogBlock(getLogBlockSettings().noOcclusion().lightLevel(state -> state.getValue(StackableLogBlock.FIRED) ? 13 : 0)));
    public static final RegistryObject<Item> STRAW_HAT = registerItem("straw_hat", () -> new TexturedArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, getSettings().rarity(Rarity.EPIC).durability(11 * 5), Vinery.identifier("textures/models/armor/winemaker.png")));
    public static final RegistryObject<Item> WINEMAKER_APRON = registerItem("vinemaker_apron", () -> new TexturedArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, getSettings().rarity(Rarity.EPIC).durability(16 * 5), Vinery.identifier("textures/models/armor/winemaker.png")));
    public static final RegistryObject<Item> WINEMAKER_LEGGINGS = registerItem("vinemaker_leggings", () -> new TexturedArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.LEGS, getSettings().rarity(Rarity.RARE).durability(15 * 5), Vinery.identifier("textures/models/armor/winemaker.png")));
    public static final RegistryObject<Item> WINEMAKER_BOOTS = registerItem("vinemaker_boots", () -> new TexturedArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.FEET, getSettings().rarity(Rarity.RARE).durability(13 * 5), Vinery.identifier("textures/models/armor/winemaker.png")));
    public static final RegistryObject<Item> MULE_SPAWN_EGG = registerItem("mule_spawn_egg", () -> new ForgeSpawnEggItem(EntityTypeRegistry.MULE, -1, -1, getSettings()));
    public static final RegistryObject<Item> WANDERING_WINEMAKER_SPAWN_EGG = registerItem("wandering_winemaker_spawn_egg", () -> new ForgeSpawnEggItem(EntityTypeRegistry.WANDERING_WINEMAKER, -1, -1, getSettings()));
    public static final RegistryObject<Block> POTTED_APPLE_TREE_SAPLING = registerWithoutItem("potted_apple_tree_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, APPLE_TREE_SAPLING, BlockBehaviour.Properties.copy(Blocks.POTTED_POPPY)));
    public static final RegistryObject<Block> POTTED_DARK_CHERRY_TREE_SAPLING = registerWithoutItem("potted_cherry_tree_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, DARK_CHERRY_SAPLING, BlockBehaviour.Properties.copy(Blocks.POTTED_POPPY)));
    public static final RegistryObject<Block> OAK_WINE_RACK_BIG = registerWithItem("oak_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> OAK_WINE_RACK_SMALL = registerWithItem("oak_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> OAK_WINE_RACK_MID = registerWithItem("oak_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> BIRCH_WINE_RACK_BIG = registerWithItem("birch_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> BIRCH_WINE_RACK_SMALL = registerWithItem("birch_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> BIRCH_WINE_RACK_MID = registerWithItem("birch_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> SPRUCE_WINE_RACK_BIG = registerWithItem("spruce_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> SPRUCE_WINE_RACK_SMALL = registerWithItem("spruce_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> SPRUCE_WINE_RACK_MID = registerWithItem("spruce_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> DARK_OAK_WINE_RACK_BIG = registerWithItem("dark_oak_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> DARK_OAK_WINE_RACK_SMALL = registerWithItem("dark_oak_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> DARK_OAK_WINE_RACK_MID = registerWithItem("dark_oak_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> JUNGLE_WINE_RACK_BIG = registerWithItem("jungle_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> JUNGLE_WINE_RACK_SMALL = registerWithItem("jungle_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> JUNGLE_WINE_RACK_MID = registerWithItem("jungle_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> ACACIA_WINE_RACK_BIG = registerWithItem("acacia_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> ACACIA_WINE_RACK_SMALL = registerWithItem("acacia_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> ACACIA_WINE_RACK_MID = registerWithItem("acacia_wine_rack_mid", ObjectRegistry::rackMid);
    public static final RegistryObject<Block> MANGROVE_WINE_RACK_BIG = registerWithItem("mangrove_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> MANGROVE_WINE_RACK_SMALL = registerWithItem("mangrove_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> MANGROVE_WINE_RACK_MID = registerWithItem("mangrove_wine_rack_mid", ObjectRegistry::rackMid);
    /** D12: bamboo racks and lattice kept; 1.19.2 has no bamboo planks, the recipes use quark:bamboo_planks. */
    public static final RegistryObject<Block> BAMBOO_WINE_RACK_BIG = registerWithItem("bamboo_wine_rack_big", ObjectRegistry::rackBig);
    public static final RegistryObject<Block> BAMBOO_WINE_RACK_SMALL = registerWithItem("bamboo_wine_rack_small", ObjectRegistry::rackSmall);
    public static final RegistryObject<Block> BAMBOO_WINE_RACK_MID = registerWithItem("bamboo_wine_rack_mid", ObjectRegistry::rackMid);
    private static final BannerSettings BANNER_SETTINGS = new BannerSettings(
            EntityTypeRegistry.VINERY_STANDARD,
            ObjectRegistry::wallStandard,
            Vinery.identifier("textures/banner/vinery_banner.png"),
            "tooltip.vinery.banner",
            PlatformHelper::getBannerEffect,
            () -> PlatformHelper.shouldGiveEffect() ? PlatformHelper.getBannerRadius() : 0,
            PlatformHelper::getBannerAmplifier,
            () -> PlatformHelper.getBannerDurationSeconds() * 20,
            () -> PlatformHelper.getBannerRefreshBelowSeconds() * 20,
            PlatformHelper::shouldShowTooltip);
    public static final RegistryObject<Block> VINERY_STANDARD = registerWithItem("vinery_standard", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(1F).noCollission().sound(SoundType.WOOD), BANNER_SETTINGS));
    public static final RegistryObject<Block> VINERY_WALL_STANDARD = registerWithoutItem("vinery_wall_standard", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(1F).noCollission().sound(SoundType.WOOD).lootFrom(VINERY_STANDARD), BANNER_SETTINGS));
    public static final RegistryObject<Block> OAK_LATTICE = registerWithItem("oak_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> SPRUCE_LATTICE = registerWithItem("spruce_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> BIRCH_LATTICE = registerWithItem("birch_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> DARK_OAK_LATTICE = registerWithItem("dark_oak_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> ACACIA_LATTICE = registerWithItem("acacia_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> BAMBOO_LATTICE = registerWithItem("bamboo_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> JUNGLE_LATTICE = registerWithItem("jungle_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> MANGROVE_LATTICE = registerWithItem("mangrove_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> DARK_CHERRY_LATTICE = registerWithItem("cherry_lattice", ObjectRegistry::lattice);
    public static final RegistryObject<Block> WHITE_SAVANNA_GRAPE_BAG = registerWithItem("white_savanna_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> RED_SAVANNA_GRAPE_BAG = registerWithItem("red_savanna_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> WHITE_TAIGA_GRAPE_BAG = registerWithItem("white_taiga_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> RED_TAIGA_GRAPE_BAG = registerWithItem("red_taiga_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> WHITE_JUNGLE_GRAPE_BAG = registerWithItem("white_jungle_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Block> RED_JUNGLE_GRAPE_BAG = registerWithItem("red_jungle_grape_bag", ObjectRegistry::bag);
    public static final RegistryObject<Item> APPLE_CIDER_ITEM = registerWineItem("apple_cider", APPLE_CIDER, true, DrinkBlockItem.BottleSize.BIG);
    /** alcohol.md B7.10: small again (3 per placed block, small racks), as in 1.3.10; 1.5.5 made it big. */
    public static final RegistryObject<Item> APPLE_WINE_ITEM = registerWineItem("apple_wine", APPLE_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> GLOWING_WINE_ITEM = registerWineItem("glowing_wine", GLOWING_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> SOLARIS_WINE_ITEM = registerWineItem("solaris_wine", SOLARIS_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> KELP_CIDER_ITEM = registerWineItem("kelp_cider", KELP_CIDER, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> EISWEIN_ITEM = registerWineItem("eiswein", EISWEIN, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> AEGIS_WINE_ITEM = registerWineItem("aegis_wine", AEGIS_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> VILLAGERS_FRIGHT_ITEM = registerWineItem("villagers_fright", VILLAGERS_FRIGHT, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> CLARK_WINE_ITEM = registerWineItem("clark_wine", CLARK_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> JELLIE_WINE_ITEM = registerWineItem("jellie_wine", JELLIE_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> NOIR_WINE_ITEM = registerWineItem("noir_wine", NOIR_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> RED_WINE_ITEM = registerWineItem("red_wine", RED_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> STRAD_WINE_ITEM = registerWineItem("strad_wine", STRAD_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> CHERRY_WINE_ITEM = registerWineItem("cherry_wine", CHERRY_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> CRISTEL_WINE_ITEM = registerWineItem("cristel_wine", CRISTEL_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> LILITU_WINE_ITEM = registerWineItem("lilitu_wine", LILITU_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> JO_SPECIAL_MIXTURE_ITEM = registerWineItem("jo_special_mixture", JO_SPECIAL_MIXTURE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> BOLVAR_WINE_ITEM = registerWineItem("bolvar_wine", BOLVAR_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> MAGNETIC_WINE_ITEM = registerWineItem("magnetic_wine", MAGNETIC_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> STAL_WINE_ITEM = registerWineItem("stal_wine", STAL_WINE, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> CHENET_WINE_ITEM = registerWineItem("chenet_wine", CHENET_WINE, true, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> BOTTLE_MOJANG_NOIR_ITEM = registerWineItem("bottle_mojang_noir", BOTTLE_MOJANG_NOIR, true, DrinkBlockItem.BottleSize.SMALL);
    public static final RegistryObject<Item> CHORUS_WINE_ITEM = registerWineItem("chorus_wine", CHORUS_WINE, false, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> CREEPERS_CRUSH_ITEM = registerWineItem("creepers_crush", CREEPERS_CRUSH, false, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> MELLOHI_WINE_ITEM = registerWineItem("mellohi_wine", MELLOHI_WINE, false, DrinkBlockItem.BottleSize.BIG);
    public static final RegistryObject<Item> PRAETORIAN_WINE_ITEM = registerWineItem("praetorian_wine", PRAETORIAN_WINE, true, DrinkBlockItem.BottleSize.BIG);

    // ------------------------------------------------------------------------------------------------------------
    // D2: 1.3.10 blocks that 1.5.5 removed or replaced, kept with their 1.3.10 behaviour and art.
    // ------------------------------------------------------------------------------------------------------------
    public static final RegistryObject<Block> WHITE_GRAPE_CRATE = registerWithItem("white_grape_crate", ObjectRegistry::crate);
    public static final RegistryObject<Block> RED_GRAPE_CRATE = registerWithItem("red_grape_crate", ObjectRegistry::crate);
    public static final RegistryObject<Block> CHERRY_CRATE = registerWithItem("cherry_crate", ObjectRegistry::crate);
    public static final RegistryObject<Block> APPLE_CRATE = registerWithItem("apple_crate", ObjectRegistry::crate);
    public static final RegistryObject<Block> GRAPEVINE_LATTICE = registerWithItem("grapevine_lattice", () -> new LegacyLatticeStemBlock(getGrapevineSettings()));
    public static final RegistryObject<Block> CHAIR = registerWithItem("chair", () -> new LegacyChairBlock(wood(2.0F, 3.0F)));
    public static final RegistryObject<Block> TABLE = registerWithItem("table", () -> new TableBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> BIG_TABLE = registerWithItem("big_table", () -> new BigTableBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2.0F, 2.0F)));
    public static final RegistryObject<Block> SHELF = registerWithItem("shelf", () -> new LegacyShelfBlock(wood(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> WINE_RACK_1 = registerWithItem("wine_rack_1", () -> new LegacyNineBottleStorageBlock(wood(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> WINE_RACK_2 = registerWithItem("wine_rack_2", () -> new LegacyFourBottleStorageBlock(wood(2.0F, 3.0F).noOcclusion()));
    public static final RegistryObject<Block> WINE_RACK_3 = registerWithItem("wine_rack_3", () -> new LegacyWineRackStorageBlock(wood(2.0F, 3.0F), SoundEventRegistry.WINE_RACK_3_OPEN, SoundEventRegistry.WINE_RACK_3_CLOSE));
    public static final RegistryObject<Block> WINE_RACK_5 = registerWithItem("wine_rack_5", () -> new LegacyWineRackStorageBlock(wood(2.0F, 3.0F), SoundEventRegistry.WINE_RACK_5_OPEN, SoundEventRegistry.WINE_RACK_5_CLOSE));
    public static final RegistryObject<Block> LOAM = registerWithItem("loam", () -> new Block(loam()));
    public static final RegistryObject<Block> LOAM_STAIRS = registerWithItem("loam_stairs", () -> new StairBlock(() -> LOAM.get().defaultBlockState(), loam()));
    public static final RegistryObject<Block> LOAM_SLAB = registerWithItem("loam_slab", () -> new SlabBlock(loam()));
    public static final RegistryObject<Block> FLOWER_BOX = registerWithItem("flower_box", () -> new LegacyFlowerBoxBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Block> FLOWER_POT = registerWithItem("flower_pot", () -> new LegacyFlowerPotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));
    public static final RegistryObject<Block> BASKET = registerWithItem("basket", () -> new LegacyBasketBlock(BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion(), 1));
    /** Already without a creative tab in 1.3.10: kept for old worlds and as Farmer's Delight heat sources. */
    public static final RegistryObject<Block> WOOD_FIRED_OVEN = registerWithoutItem("wood_fired_oven", () -> new LegacyStoveBlock(BlockBehaviour.Properties.copy(Blocks.STONE)));
    public static final RegistryObject<Item> WOOD_FIRED_OVEN_ITEM = registerItem("wood_fired_oven", () -> new BlockItem(WOOD_FIRED_OVEN.get(), new Item.Properties()));
    public static final RegistryObject<Block> STOVE = registerWithoutItem("stove", () -> new LegacyStoveBlock(BlockBehaviour.Properties.copy(Blocks.STONE).lightLevel(state -> 12)));
    public static final RegistryObject<Item> STOVE_ITEM = registerItem("stove", () -> new BlockItem(STOVE.get(), new Item.Properties()));
    public static final RegistryObject<Block> KITCHEN_SINK = registerWithoutItem("kitchen_sink", () -> new LegacyKitchenSinkBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));
    public static final RegistryObject<Item> KITCHEN_SINK_ITEM = registerItem("kitchen_sink", () -> new BlockItem(KITCHEN_SINK.get(), new Item.Properties()));
    public static final RegistryObject<Block> BARREL = registerWithoutItem("barrel", () -> new BarrelBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> BARREL_ITEM = registerItem("barrel", () -> new BlockItem(BARREL.get(), new Item.Properties()));

    private static Block wallStandard() {
        return VINERY_WALL_STANDARD.get();
    }

    static Item.Properties getSettings() {
        return new Item.Properties().tab(TabRegistry.VINERY_TAB);
    }

    private static BlockBehaviour.Properties wood(float strength, float resistance) {
        return BlockBehaviour.Properties.of(Material.WOOD).strength(strength, resistance).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties loam() {
        return BlockBehaviour.Properties.of(Material.DIRT).strength(2.0F, 3.0F).sound(SoundType.MUD);
    }

    private static Block crate() {
        return new Block(wood(2.0F, 3.0F));
    }

    private static Block bag() {
        return new FacingBlock(BlockBehaviour.Properties.of(Material.WOOL).strength(2.0F, 3.0F).sound(SoundType.WOOL));
    }

    private static Block rackBig() {
        return new BottleStorageBlock(wood(2.0F, 3.0F).noOcclusion(), BottleStorageBlock.Layout.NINE);
    }

    private static Block rackSmall() {
        return new BottleStorageBlock(wood(2.0F, 3.0F).noOcclusion(), BottleStorageBlock.Layout.FOUR);
    }

    private static Block rackMid() {
        return new BigBottleStorageBlock(wood(2.0F, 3.0F).noOcclusion());
    }

    private static Block lattice() {
        return new LatticeBlock(wood(2.0F, 3.0F).noOcclusion());
    }

    private static Item juice() {
        return new GrapejuiceBottleItem(getSettings().craftRemainder(ObjectRegistry.WINE_BOTTLE.get()));
    }

    private static RegistryObject<Item> registerWineItem(String name, Supplier<Block> wineBlock, boolean scaleDurationWithAge, DrinkBlockItem.BottleSize size) {
        return registerItem(name, () -> new DrinkBlockItem(
                wineBlock.get(),
                getSettings().food(new FoodProperties.Builder().alwaysEat().build()),
                scaleDurationWithAge,
                size
        ));
    }

    private static BlockBehaviour.Properties getGrapevineSettings() {
        return BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F).randomTicks().sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties getLogBlockSettings() {
        return BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties getWineSettings() {
        return BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion().instabreak();
    }

    public static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> block) {
        RegistryObject<T> registered = registerWithoutItem(name, block);
        registerItem(name, () -> new BlockItem(registered.get(), getSettings()));
        return registered;
    }

    public static <T extends Block> RegistryObject<T> registerWithoutItem(String path, Supplier<T> block) {
        return BLOCKS.register(path, block);
    }

    public static <T extends Item> RegistryObject<T> registerItem(String path, Supplier<T> itemSupplier) {
        return ITEMS.register(path, itemSupplier);
    }
}
