package com.SaintAlfonzo.creativedelight.hearth;

import com.SaintAlfonzo.creativedelight.hearth.block.HHBlockEntities;
import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import com.SaintAlfonzo.creativedelight.hearth.block.SapCauldronBlock;
import com.SaintAlfonzo.creativedelight.hearth.client.HHClient;
import com.SaintAlfonzo.creativedelight.hearth.effect.EffectAuras;
import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import com.SaintAlfonzo.creativedelight.hearth.entity.FoxBerryGoals;
import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import com.SaintAlfonzo.creativedelight.hearth.event.AnimalEvents;
import com.SaintAlfonzo.creativedelight.hearth.event.CleaverEvents;
import com.SaintAlfonzo.creativedelight.hearth.event.ManureDrops;
import com.SaintAlfonzo.creativedelight.hearth.fluid.HHFluids;
import com.SaintAlfonzo.creativedelight.hearth.block.HopsLatticeBlock;
import com.SaintAlfonzo.creativedelight.hearth.gear.FarmersHatEvents;
import com.SaintAlfonzo.creativedelight.hearth.gear.HHGearRegistry;
import com.SaintAlfonzo.creativedelight.hearth.gear.HorseFeedEvents;
import com.SaintAlfonzo.creativedelight.hearth.gear.HorseshoeEvents;
import com.SaintAlfonzo.creativedelight.hearth.gear.QuarkCompat;
import com.SaintAlfonzo.creativedelight.hearth.gear.RoastingEvents;
import com.SaintAlfonzo.creativedelight.hearth.gear.TillingEvents;
import com.SaintAlfonzo.creativedelight.hearth.potion.HHPotions;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import com.SaintAlfonzo.creativedelight.hearth.loot.HHLootModifiers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Hearth and Harvest 1.4.1 ported to Forge 1.19.2 piece by piece, a Creative Delight member (feature_writeup/
 * hearth_and_harvest.md 4). The code is our own (GNU GPL3), written from the 1.4.1 behaviour; the art and data are the
 * MIT 1.4.1 files (NOTICE-upstream-MIT.txt). Ids match upstream. Ported so far: the raspberry bush, Raspberry and
 * Raspberry Pips (4.1); the watering can and its tiers (4.0, moved from patcherMod).
 */
@Mod(HearthAndHarvestMod.MODID)
public class HearthAndHarvestMod {
    public static final String MODID = "hearthandharvest";

    public HearthAndHarvestMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        com.SaintAlfonzo.creativedelight.hearth.salt.HHSaltRegistry.init(bus);
        com.SaintAlfonzo.creativedelight.hearth.nest.HHNestRegistry.init(bus);
        com.SaintAlfonzo.creativedelight.hearth.crow.HHCrowRegistry.init(bus);
        HHBlocks.BLOCKS.register(bus);
        HHItems.ITEMS.register(bus);
        HHFluids.TYPES.register(bus);
        HHFluids.FLUIDS.register(bus);
        HHEffects.EFFECTS.register(bus);
        HHBlockEntities.BLOCK_ENTITIES.register(bus);
        HHEntities.ENTITIES.register(bus);
        HHSounds.SOUNDS.register(bus);
        HHLootModifiers.SERIALIZERS.register(bus);
        com.SaintAlfonzo.creativedelight.hearth.worldgen.HHWorldgen.init(bus);
        HHGearRegistry.ENCHANTMENTS.register(bus);
        HHPotions.POTIONS.register(bus);
        com.SaintAlfonzo.creativedelight.hearth.machine.HHMachines.init(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HHConfig.SPEC, "hearthandharvest-common.toml");
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HHClient.init(bus));
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            HHBlocks.registerPottedPlants();
            if (ModList.get().isLoaded("quark")) {
                QuarkCompat.registerDispenser();
            }
        }));
        MinecraftForge.EVENT_BUS.addListener(FoxBerryGoals::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(HHEffects::onEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(HHEffects::onEffectAdded);
        MinecraftForge.EVENT_BUS.addListener(HHEffects::onLivingJump);
        MinecraftForge.EVENT_BUS.addListener(EffectAuras::onEffectAdded);
        MinecraftForge.EVENT_BUS.addListener(EffectAuras::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(EffectAuras::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(AnimalEvents::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, AnimalEvents::onBabySpawn);
        MinecraftForge.EVENT_BUS.addListener(ManureDrops::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(ManureDrops::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ManureDrops::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(CleaverEvents::onDeath);
        MinecraftForge.EVENT_BUS.addListener(CleaverEvents::onDrops);
        MinecraftForge.EVENT_BUS.addListener(SapCauldronBlock::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(HopsLatticeBlock::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(HorseshoeEvents::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(HorseshoeEvents::onAttack);
        MinecraftForge.EVENT_BUS.addListener(HorseshoeEvents::onDeath);
        MinecraftForge.EVENT_BUS.addListener(HorseFeedEvents::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(FarmersHatEvents::onBreak);
        MinecraftForge.EVENT_BUS.addListener(RoastingEvents::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, TillingEvents::onRightClickBlock);
        if (ModList.get().isLoaded("quark")) {
            QuarkCompat.init();
        }
    }
}
