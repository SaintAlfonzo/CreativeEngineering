package net.satisfy.foundation.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.foundation.Foundation;
import net.satisfy.foundation.seat.ChairEntity;

/** Entity types of the lib. Only the invisible chair seat for now. */
public final class FoundationEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Foundation.MOD_ID);

    public static final RegistryObject<EntityType<ChairEntity>> CHAIR = ENTITY_TYPES.register("chair", () -> EntityType.Builder.<ChairEntity>of(ChairEntity::new, MobCategory.MISC).sized(0.001F, 0.001F).build(Foundation.identifier("chair").toString()));

    private FoundationEntities() {
    }
}
