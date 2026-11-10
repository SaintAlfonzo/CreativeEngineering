package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HHBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HearthAndHarvestMod.MODID);

    public static final RegistryObject<BlockEntityType<TreeTapperBlockEntity>> TREE_TAPPER = BLOCK_ENTITIES.register("tree_tapper",
            () -> BlockEntityType.Builder.of(TreeTapperBlockEntity::new, HHBlocks.TREE_TAPPER.get()).build(null));

    private HHBlockEntities() {
    }
}
