package com.SaintAlfonzo.more_mooblooms.entity;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The moobloom entity type: cow size, cow attributes, and the cow's spawn placement plus one extra: a block some
 * variant lists in {@code spawns_in_dark_on} (the witherbloom: soul sand, soul soil) passes with no grass or light
 * needed. That is only a first filter, made before any variant is known; {@code Moobloom.checkSpawnRules} picks
 * the variant and lets the block through only for a variant that lists it.
 */
public final class MMEntities {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MoreMooblooms.ID);

    public static final RegistryObject<EntityType<Moobloom>> MOOBLOOM = ENTITIES.register("moobloom",
            () -> EntityType.Builder.of(Moobloom::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F)
                    .clientTrackingRange(10)
                    .build(MoreMooblooms.ID + ":moobloom"));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener((EntityAttributeCreationEvent e) -> e.put(MOOBLOOM.get(), Cow.createAttributes().build()));
        // Only used by direct moobloom spawns (biome modifiers: the witherbloom pack); cow spawns are swapped
        // after the cow passed the cow's own checks.
        modBus.addListener((SpawnPlacementRegisterEvent e) -> e.register(MOOBLOOM.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MMEntities::checkMoobloomSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE));
    }

    public static boolean checkMoobloomSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType reason,
                                                  BlockPos pos, RandomSource random) {
        return MoobloomVariants.anySpawnsInDarkOn(level.getBlockState(pos.below()))
                || Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }

    private MMEntities() {}
}
