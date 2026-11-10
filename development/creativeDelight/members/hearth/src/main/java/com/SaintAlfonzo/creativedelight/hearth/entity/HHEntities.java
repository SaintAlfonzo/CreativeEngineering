package com.SaintAlfonzo.creativedelight.hearth.entity;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HHEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HearthAndHarvestMod.MODID);

    public static final RegistryObject<EntityType<ThrownCleaver>> THROWN_CLEAVER = ENTITIES.register("thrown_cleaver",
            () -> EntityType.Builder.<ThrownCleaver>of(ThrownCleaver::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("thrown_cleaver"));
    public static final RegistryObject<EntityType<ThrownPitchfork>> THROWN_PITCHFORK = ENTITIES.register("thrown_pitchfork",
            () -> EntityType.Builder.<ThrownPitchfork>of(ThrownPitchfork::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("thrown_pitchfork"));
    public static final RegistryObject<EntityType<ManureProjectile>> MANURE_PROJECTILE = ENTITIES.register("manure_projectile",
            () -> EntityType.Builder.<ManureProjectile>of(ManureProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("manure_projectile"));

    public static final RegistryObject<EntityType<com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe>> THROWN_HORSESHOE = ENTITIES.register("thrown_horseshoe",
            () -> EntityType.Builder.<com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe>of(com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("thrown_horseshoe"));

    private HHEntities() {
    }
}
