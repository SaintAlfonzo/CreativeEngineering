package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.content.block.sticky_basket.StickyBasketBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MDBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MinersDelightMod.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<CopperPotBlockEntity>> COPPER_POT = BLOCK_ENTITIES.register("copper_pot",
            () -> BlockEntityType.Builder.of(CopperPotBlockEntity::new, MDBlocks.COPPER_POT.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<StickyBasketBlockEntity>> STICKY_BASKET = BLOCK_ENTITIES.register("sticky_basket",
            () -> BlockEntityType.Builder.of(StickyBasketBlockEntity::new, MDBlocks.STICKY_BASKET.get()).build(null));

    private MDBlockEntities() {
    }
}
