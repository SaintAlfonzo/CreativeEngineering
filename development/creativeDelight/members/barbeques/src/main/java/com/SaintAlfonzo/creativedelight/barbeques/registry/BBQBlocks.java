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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * The grill, the basin and tray in each vanilla wood, and their block entities. Ids and properties as 1.3.0's BBQDBlocks;
 * oak keeps 1.3.0's plain {@code basin} / {@code tray} ids, the other woods are {@code <wood>_basin} / {@code <wood>_tray}.
 */
public final class BBQBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BarbequesDelight.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BarbequesDelight.MODID);

    public static final RegistryObject<GrillBlock> GRILL = BLOCKS.register("grill", () -> new GrillBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN)
                    .noOcclusion().lightLevel(GrillBlock::lightLevel)));
    /** The basin is made from the wood's stripped log, the tray from its planks; both copy the planks' properties. */
    public enum Wood {
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, CRIMSON, WARPED;

        public final String key = name().toLowerCase(Locale.ROOT);

        /** {@code basin} for oak (1.3.0's id), {@code <wood>_basin} for the rest. */
        public String id(String base) {
            return this == OAK ? base : key + "_" + base;
        }

        Block planks() {
            return switch (this) {
                case OAK -> Blocks.OAK_PLANKS;
                case SPRUCE -> Blocks.SPRUCE_PLANKS;
                case BIRCH -> Blocks.BIRCH_PLANKS;
                case JUNGLE -> Blocks.JUNGLE_PLANKS;
                case ACACIA -> Blocks.ACACIA_PLANKS;
                case DARK_OAK -> Blocks.DARK_OAK_PLANKS;
                case MANGROVE -> Blocks.MANGROVE_PLANKS;
                case CRIMSON -> Blocks.CRIMSON_PLANKS;
                case WARPED -> Blocks.WARPED_PLANKS;
            };
        }
    }

    public static final Map<Wood, RegistryObject<BasinBlock>> BASINS = woods("basin", BasinBlock::new);
    public static final Map<Wood, RegistryObject<TrayBlock>> TRAYS = woods("tray", TrayBlock::new);
    public static final RegistryObject<BasinBlock> BASIN = BASINS.get(Wood.OAK);
    public static final RegistryObject<TrayBlock> TRAY = TRAYS.get(Wood.OAK);

    private static <B extends Block> Map<Wood, RegistryObject<B>> woods(String base, Function<BlockBehaviour.Properties, B> factory) {
        Map<Wood, RegistryObject<B>> map = new EnumMap<>(Wood.class);
        for (Wood wood : Wood.values()) {
            map.put(wood, BLOCKS.register(wood.id(base), () -> factory.apply(BlockBehaviour.Properties.copy(wood.planks()).noOcclusion())));
        }
        return Collections.unmodifiableMap(map);
    }

    private static Block[] all(Map<Wood, ? extends RegistryObject<? extends Block>> map) {
        return map.values().stream().map(RegistryObject::get).toArray(Block[]::new);
    }

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<GrillBlockEntity>> GRILL_BE = BLOCK_ENTITIES.register("grill",
            () -> BlockEntityType.Builder.of(GrillBlockEntity::new, GRILL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<BasinBlockEntity>> BASIN_BE = BLOCK_ENTITIES.register("basin",
            () -> BlockEntityType.Builder.of(BasinBlockEntity::new, all(BASINS)).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<TrayBlockEntity>> TRAY_BE = BLOCK_ENTITIES.register("tray",
            () -> BlockEntityType.Builder.of(TrayBlockEntity::new, all(TRAYS)).build(null));

    private BBQBlocks() {
    }
}
