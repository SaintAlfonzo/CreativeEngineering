// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModBlocks.kt. Translated to Java for Forge 1.19.2:
// ResourcefulRegistry -> DeferredRegister (blocks and block entity types).
// Phases 2 and 3 register every upstream block and block entity, in upstream's order.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.block.MetronomeBe;
import earth.terrarium.tempad.common.block.MetronomeBlock;
import earth.terrarium.tempad.common.block.RudimentaryTempadBE;
import earth.terrarium.tempad.common.block.RudimentaryTempadBlock;
import earth.terrarium.tempad.common.block.WorkstationBE;
import earth.terrarium.tempad.common.block.WorkstationBlock;
import earth.terrarium.tempad.common.block.WorkstationChildBlock;
import earth.terrarium.tempad.common.block.timedoor_marker.ChronomarkBE;
import earth.terrarium.tempad.common.block.timedoor_marker.ChronomarkBlock;
import earth.terrarium.tempad.common.block.timedoor_marker.TimedoorMarkerBE;
import earth.terrarium.tempad.common.block.timedoor_marker.TimedoorMarkerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Tempad.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Tempad.MOD_ID);

    public static final RegistryObject<RudimentaryTempadBlock> timedoorProjector = BLOCKS.register("timedoor_projector", RudimentaryTempadBlock::new);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<RudimentaryTempadBE>> timedoorProjectorBE = BLOCK_ENTITIES.register("timedoor_projector",
            () -> BlockEntityType.Builder.of(RudimentaryTempadBE::new, timedoorProjector.get()).build(null));

    public static final RegistryObject<TimedoorMarkerBlock> timedoorMarker = BLOCKS.register("timedoor_marker", TimedoorMarkerBlock::new);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<TimedoorMarkerBE>> timedoorMarkerBE = BLOCK_ENTITIES.register("timedoor_marker",
            () -> BlockEntityType.Builder.of(TimedoorMarkerBE::new, timedoorMarker.get()).build(null));

    public static final RegistryObject<ChronomarkBlock> chronomark = BLOCKS.register("chronomark", ChronomarkBlock::new);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<ChronomarkBE>> chronomarkBE = BLOCK_ENTITIES.register("chronomark",
            () -> BlockEntityType.Builder.of(ChronomarkBE::new, chronomark.get()).build(null));

    public static final RegistryObject<WorkstationBlock> workstation = BLOCKS.register("workstation", WorkstationBlock::new);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<WorkstationBE>> workstationBE = BLOCK_ENTITIES.register("workstation",
            () -> BlockEntityType.Builder.of(WorkstationBE::new, workstation.get()).build(null));

    public static final RegistryObject<WorkstationChildBlock> workstationChild = BLOCKS.register("workstation_child", WorkstationChildBlock::new);

    public static final RegistryObject<MetronomeBlock> metronome = BLOCKS.register("metronome", MetronomeBlock::new);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<MetronomeBe>> metronomeBe = BLOCK_ENTITIES.register("metronome",
            () -> BlockEntityType.Builder.of(MetronomeBe::new, metronome.get()).build(null));

    private ModBlocks() {
    }
}
