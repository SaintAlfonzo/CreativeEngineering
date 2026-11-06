package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.CaveCarrotBlock;
import com.sammy.minersdelight.content.block.GossypiumBlock;
import com.sammy.minersdelight.content.block.StuffedSquidFeastBlock;
import com.sammy.minersdelight.content.block.TrayFeastBlock;
import com.sammy.minersdelight.content.block.WildCaveCarrotBlock;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlock;
import com.sammy.minersdelight.content.block.sticky_basket.StickyBasketBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The 10 blocks of Miner's Delight 1.4.5 (1.1.2 has the same ids less the potted gossypium). */
public final class MDBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MinersDelightMod.MODID);

    public static final RegistryObject<CopperPotBlock> COPPER_POT = BLOCKS.register("copper_pot",
            () -> new CopperPotBlock(Properties.copy(Blocks.COPPER_BLOCK).strength(0.5F, 6.0F).sound(SoundType.LANTERN)));
    public static final RegistryObject<StickyBasketBlock> STICKY_BASKET = BLOCKS.register("sticky_basket",
            () -> new StickyBasketBlock(Properties.of(Material.WOOD).strength(1.5F).sound(SoundType.WOOD)));
    public static final RegistryObject<StuffedSquidFeastBlock> STUFFED_SQUID = BLOCKS.register("stuffed_squid",
            () -> new StuffedSquidFeastBlock(Properties.copy(Blocks.CAKE)));
    public static final RegistryObject<TrayFeastBlock> FAKE_MEATLOAF = BLOCKS.register("fake_meatloaf",
            () -> new TrayFeastBlock(Properties.copy(Blocks.CAKE), MDItems.PLATE_OF_FAKE_MEATLOAF::get, TrayFeastBlock.FAKE_MEATLOAF));
    public static final RegistryObject<TrayFeastBlock> GLAZED_ARACHNID_LIMBS = BLOCKS.register("glazed_arachnid_limbs",
            () -> new TrayFeastBlock(Properties.copy(Blocks.CAKE), MDItems.PLATE_OF_GLAZED_ARACHNID_LIMBS::get, TrayFeastBlock.GLAZED_ARACHNID_LIMBS));
    public static final RegistryObject<WildCaveCarrotBlock> WILD_CAVE_CARROTS = BLOCKS.register("wild_cave_carrots",
            () -> new WildCaveCarrotBlock(Properties.copy(Blocks.TALL_GRASS).randomTicks()));
    public static final RegistryObject<GossypiumBlock> GOSSYPIUM = BLOCKS.register("gossypium",
            () -> new GossypiumBlock(Properties.copy(Blocks.TALL_GRASS)));
    public static final RegistryObject<FlowerPotBlock> POTTED_GOSSYPIUM = BLOCKS.register("potted_gossypium",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, GOSSYPIUM, Properties.copy(Blocks.POTTED_POPPY)));
    public static final RegistryObject<CaveCarrotBlock> CAVE_CARROTS = BLOCKS.register("cave_carrots",
            () -> new CaveCarrotBlock(Properties.copy(Blocks.CARROTS)));
    public static final RegistryObject<Block> CAVE_CARROT_CRATE = BLOCKS.register("cave_carrot_crate",
            () -> new Block(Properties.of(Material.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));

    private MDBlocks() {
    }
}
