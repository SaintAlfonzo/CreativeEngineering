package net.mehdinoui.veggiesdelight.common.registry;

import net.mehdinoui.veggiesdelight.VeggiesDelight;
import net.mehdinoui.veggiesdelight.common.block.CarrotCakeBlock;
import net.mehdinoui.veggiesdelight.common.block.DandelionBlock;
import net.mehdinoui.veggiesdelight.common.block.LasagnaPanBlock;
import net.mehdinoui.veggiesdelight.common.block.PizzaBlock;
import net.mehdinoui.veggiesdelight.common.block.StuffedBellpeppersBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.BroccoliCropBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.CauliflowerCropBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.GarlicCropBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.SweetPotatoCropBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.TurnipCropBlock;
import net.mehdinoui.veggiesdelight.common.block.crops.ZucchiniCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.PieBlock;

// Upstream's blocks minus the bell pepper crop, crate and wild patch (Rustic Delight's peppers are used instead).
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, VeggiesDelight.MOD_ID);

    public static final RegistryObject<Block> BROCCOLI_CRATE = BLOCKS.register("broccoli_crate", ModBlocks::crate);
    public static final RegistryObject<Block> CAULIFLOWER_CRATE = BLOCKS.register("cauliflower_crate", ModBlocks::crate);
    public static final RegistryObject<Block> GARLIC_CRATE = BLOCKS.register("garlic_crate", ModBlocks::crate);
    public static final RegistryObject<Block> SWEET_POTATO_CRATE = BLOCKS.register("sweet_potato_crate", ModBlocks::crate);
    public static final RegistryObject<Block> TURNIP_CRATE = BLOCKS.register("turnip_crate", ModBlocks::crate);
    public static final RegistryObject<Block> ZUCCHINI_CRATE = BLOCKS.register("zucchini_crate", ModBlocks::crate);

    public static final RegistryObject<Block> BROCCOLI_CROP = BLOCKS.register("broccoli_crop", () -> new BroccoliCropBlock(cropProperties()));
    public static final RegistryObject<Block> CAULIFLOWER_CROP = BLOCKS.register("cauliflower_crop", () -> new CauliflowerCropBlock(cropProperties()));
    public static final RegistryObject<Block> GARLIC_CROP = BLOCKS.register("garlic_crop", () -> new GarlicCropBlock(cropProperties()));
    public static final RegistryObject<Block> SWEET_POTATO_CROP = BLOCKS.register("sweet_potato_crop", () -> new SweetPotatoCropBlock(cropProperties()));
    public static final RegistryObject<Block> TURNIP_CROP = BLOCKS.register("turnip_crop", () -> new TurnipCropBlock(cropProperties()));
    public static final RegistryObject<Block> ZUCCHINI_CROP = BLOCKS.register("zucchini_crop", () -> new ZucchiniCropBlock(cropProperties()));

    public static final RegistryObject<Block> VEGAN_PIZZA = BLOCKS.register("vegan_pizza",
            () -> new PizzaBlock(Properties.copy(Blocks.CAKE), ModItems.VEGAN_PIZZA_SLICE));
    public static final RegistryObject<Block> SWEET_POTATO_PIE = BLOCKS.register("sweet_potato_pie",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), ModItems.SWEET_POTATO_PIE_SLICE));
    public static final RegistryObject<Block> BEETROOT_BROWNIE_TRAY = BLOCKS.register("beetroot_brownie_tray",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), ModItems.BEETROOT_BROWNIE));
    public static final RegistryObject<Block> CARROT_CAKE = BLOCKS.register("carrot_cake",
            () -> new CarrotCakeBlock(Properties.copy(Blocks.CAKE)));
    public static final RegistryObject<Block> STUFFED_BELLPEPPERS_BLOCK = BLOCKS.register("stuffed_bellpeppers_block",
            () -> new StuffedBellpeppersBlock(Properties.copy(Blocks.CAKE), ModItems.STUFFED_BELLPEPPER, true));
    public static final RegistryObject<Block> ZUCCHINI_QUICHE = BLOCKS.register("zucchini_quiche",
            () -> new PieBlock(Properties.copy(Blocks.CAKE), ModItems.ZUCCHINI_QUICHE_SLICE));
    public static final RegistryObject<Block> LASAGNA_PAN = BLOCKS.register("lasagna_pan",
            () -> new LasagnaPanBlock(Properties.copy(Blocks.CAKE), ModItems.LASAGNA_SLICE, true));

    public static final RegistryObject<Block> MATURE_DANDELION = BLOCKS.register("mature_dandelion",
            () -> new DandelionBlock(Properties.copy(Blocks.DANDELION).noCollission().noOcclusion()));
    public static final RegistryObject<Block> WILD_BROCCOLI = BLOCKS.register("wild_broccoli", () -> wild(false));
    public static final RegistryObject<Block> WILD_CAULIFLOWERS = BLOCKS.register("wild_cauliflowers", () -> wild(false));
    public static final RegistryObject<Block> WILD_GARLIC = BLOCKS.register("wild_garlic", () -> wild(false));
    public static final RegistryObject<Block> WILD_SWEET_POTATOES = BLOCKS.register("wild_sweet_potatoes", () -> wild(false));
    public static final RegistryObject<Block> WILD_TURNIPS = BLOCKS.register("wild_turnips", () -> wild(true));
    public static final RegistryObject<Block> WILD_ZUCCHINIS = BLOCKS.register("wild_zucchinis", () -> wild(false));

    private static Block crate() {
        return new Block(Properties.copy(Blocks.OAK_PLANKS).strength(2.0F, 3.0F).sound(SoundType.WOOD));
    }

    private static Properties cropProperties() {
        return Properties.copy(Blocks.WHEAT).noOcclusion().noCollission();
    }

    // Wild plants are flowers (Luck in suspicious stew). Wild turnips also stand on sand and red sand (beaches).
    private static Block wild(boolean onSand) {
        Properties properties = Properties.copy(Blocks.ALLIUM).noCollission().noOcclusion();
        if (!onSand) {
            return new FlowerBlock(() -> MobEffects.LUCK, 5, properties);
        }
        return new FlowerBlock(() -> MobEffects.LUCK, 5, properties) {
            @Override
            protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
                return super.mayPlaceOn(state, level, pos) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
            }
        };
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
