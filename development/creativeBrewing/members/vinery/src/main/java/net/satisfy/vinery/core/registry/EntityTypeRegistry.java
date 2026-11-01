package net.satisfy.vinery.core.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.foundation.banner.CompletionistBannerEntity;
import net.satisfy.foundation.block.CabinetBlockEntity;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.foundation.wood.WoodBoat;
import net.satisfy.foundation.wood.WoodChestBoat;
import net.satisfy.foundation.wood.WoodSignBlockEntity;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.entity.*;
import net.satisfy.vinery.core.entity.TraderMuleEntity;
import net.satisfy.vinery.core.entity.WanderingWinemakerEntity;
import net.satisfy.vinery.legacy.entity.LegacyFlowerBoxBlockEntity;
import net.satisfy.vinery.legacy.entity.LegacyFlowerPotBlockEntity;

import java.util.HashSet;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

public class EntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Vinery.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Vinery.MOD_ID);

    public static final RegistryObject<BlockEntityType<WoodSignBlockEntity>> MOD_SIGN = BLOCK_ENTITY_TYPES.register("mod_sign", () -> BlockEntityType.Builder.of((pos, state) -> new WoodSignBlockEntity(EntityTypeRegistry.MOD_SIGN.get(), pos, state), DARK_CHERRY_SIGN.get(), DARK_CHERRY_WALL_SIGN.get()).build(null));

    public static final RegistryObject<BlockEntityType<StorageBlockEntity>> STORAGE_ENTITY = BLOCK_ENTITY_TYPES.register("storage", () ->
            BlockEntityType.Builder.of((pos, state) -> new StorageBlockEntity(EntityTypeRegistry.STORAGE_ENTITY.get(), pos, state), StorageTypeRegistry.registerBlocks(new HashSet<>()).toArray(new Block[0])).build(null));

    public static final RegistryObject<BlockEntityType<CabinetBlockEntity>> CABINET_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("cabinet", () ->
            BlockEntityType.Builder.of((pos, state) -> new CabinetBlockEntity(EntityTypeRegistry.CABINET_BLOCK_ENTITY.get(), pos, state), StorageTypeRegistry.getCabinetBlocks()).build(null));

    public static final RegistryObject<BlockEntityType<StoragePotBlockEntity>> STORAGE_POT_ENTITY = BLOCK_ENTITY_TYPES.register("storage_pot", () -> BlockEntityType.Builder.of(StoragePotBlockEntity::new, STORAGE_POT.get()).build(null));
    public static final RegistryObject<BlockEntityType<StackableLogBlockEntity>> STACKABLE_LOG = BLOCK_ENTITY_TYPES.register("stackable_log", () -> BlockEntityType.Builder.of(StackableLogBlockEntity::new, ObjectRegistry.STACKABLE_LOG.get()).build(null));
    /** Port: the 1.3.10 Wine Press block entity id, so old presses load straight into the Apple Press (slots 0 and 1 match). */
    public static final RegistryObject<BlockEntityType<ApplePressBlockEntity>> APPLE_PRESS_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("wine_press", () -> BlockEntityType.Builder.of(ApplePressBlockEntity::new, APPLE_PRESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<FermentationBarrelBlockEntity>> FERMENTATION_BARREL_ENTITY = BLOCK_ENTITY_TYPES.register("fermentation_barrel", () -> BlockEntityType.Builder.of(FermentationBarrelBlockEntity::new, FERMENTATION_BARREL.get()).build(null));
    public static final RegistryObject<BlockEntityType<CompletionistBannerEntity>> VINERY_STANDARD = BLOCK_ENTITY_TYPES.register("vinery_standard", () -> BlockEntityType.Builder.of(CompletionistBannerEntity::new, ObjectRegistry.VINERY_STANDARD.get(), VINERY_WALL_STANDARD.get()).build(null));
    public static final RegistryObject<BlockEntityType<DarkCherryBarrelBlockEntity>> DARK_CHERRY_BARREL_ENTITY = BLOCK_ENTITY_TYPES.register("cherry_barrel", () -> BlockEntityType.Builder.of(DarkCherryBarrelBlockEntity::new, DARK_CHERRY_BARREL.get()).build(null));
    public static final RegistryObject<BlockEntityType<GrapevinePotBlockEntity>> GRAPEVINE_POT = BLOCK_ENTITY_TYPES.register("grapevine_pot", () -> BlockEntityType.Builder.of(GrapevinePotBlockEntity::new, ObjectRegistry.GRAPEVINE_POT.get()).build(null));

    // 1.3.10 block entities of the D2 keepers, under their 1.3.10 ids
    public static final RegistryObject<BlockEntityType<LegacyFlowerBoxBlockEntity>> FLOWER_BOX = BLOCK_ENTITY_TYPES.register("flower_box", () -> BlockEntityType.Builder.of(LegacyFlowerBoxBlockEntity::new, ObjectRegistry.FLOWER_BOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<LegacyFlowerPotBlockEntity>> FLOWER_POT = BLOCK_ENTITY_TYPES.register("flower_pot", () -> BlockEntityType.Builder.of(LegacyFlowerPotBlockEntity::new, ObjectRegistry.FLOWER_POT.get()).build(null));

    public static final RegistryObject<EntityType<WoodBoat>> DARK_CHERRY_BOAT = ENTITY_TYPES.register("cherry_boat", () -> EntityType.Builder.<WoodBoat>of(WoodBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build(Vinery.identifier("cherry_boat").toString()));
    public static final RegistryObject<EntityType<WoodChestBoat>> DARK_CHERRY_CHEST_BOAT = ENTITY_TYPES.register("cherry_chest_boat", () -> EntityType.Builder.<WoodChestBoat>of(WoodChestBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build(Vinery.identifier("cherry_chest_boat").toString()));

    public static final RegistryObject<EntityType<TraderMuleEntity>> MULE = ENTITY_TYPES.register("mule", () -> EntityType.Builder.of(TraderMuleEntity::new, MobCategory.CREATURE).sized(0.9f, 1.4f).clientTrackingRange(10).build(Vinery.identifier("mule").toString()));
    public static final RegistryObject<EntityType<WanderingWinemakerEntity>> WANDERING_WINEMAKER = ENTITY_TYPES.register("wandering_winemaker", () -> EntityType.Builder.of(WanderingWinemakerEntity::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(10).build(Vinery.identifier("wandering_winemaker").toString()));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(MULE.get(), Llama.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.2f).build());
        event.put(WANDERING_WINEMAKER.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 16.0).build());
    }
}
