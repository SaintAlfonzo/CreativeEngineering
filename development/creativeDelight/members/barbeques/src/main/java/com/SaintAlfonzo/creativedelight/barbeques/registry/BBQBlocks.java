package com.SaintAlfonzo.creativedelight.barbeques.registry;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.block.BasinBlock;
import com.SaintAlfonzo.creativedelight.barbeques.block.BasinBlockEntity;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlock;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockEntity;
import com.SaintAlfonzo.creativedelight.barbeques.block.TrayBlock;
import com.SaintAlfonzo.creativedelight.barbeques.block.TrayBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The three blocks and their block entities, ids and properties as 1.3.0's BBQDBlocks. */
public final class BBQBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BarbequesDelight.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BarbequesDelight.MODID);

    public static final RegistryObject<GrillBlock> GRILL = BLOCKS.register("grill", () -> new GrillBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN)
                    .noOcclusion().lightLevel(GrillBlock::lightLevel)));
    public static final RegistryObject<BasinBlock> BASIN = BLOCKS.register("basin",
            () -> new BasinBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final RegistryObject<TrayBlock> TRAY = BLOCKS.register("tray",
            () -> new TrayBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<GrillBlockEntity>> GRILL_BE = BLOCK_ENTITIES.register("grill",
            () -> BlockEntityType.Builder.of(GrillBlockEntity::new, GRILL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<BasinBlockEntity>> BASIN_BE = BLOCK_ENTITIES.register("basin",
            () -> BlockEntityType.Builder.of(BasinBlockEntity::new, BASIN.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<TrayBlockEntity>> TRAY_BE = BLOCK_ENTITIES.register("tray",
            () -> BlockEntityType.Builder.of(TrayBlockEntity::new, TRAY.get()).build(null));

    private BBQBlocks() {
    }
}
