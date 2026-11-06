package com.SaintAlfonzo.creativeengineering.shrapnel;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CreativeEngineeringEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CreativeEngineering.MODID);

    public static final RegistryObject<EntityType<ShrapnelEntity>> SHRAPNEL = ENTITIES.register("shrapnel",
            () -> EntityType.Builder.<ShrapnelEntity>of(ShrapnelEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(32)
                    .updateInterval(10)
                    .noSave()
                    .fireImmune()
                    .build("creativeengineering:shrapnel"));

    private CreativeEngineeringEntities() {}

    // Call from CreativeEngineering's constructor, same place every other DeferredRegister
    // gets hooked up - without this, ENTITIES never registers with the mod event bus.
    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
