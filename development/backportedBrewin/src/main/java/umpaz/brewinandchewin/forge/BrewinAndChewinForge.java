package umpaz.brewinandchewin.forge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCLootFunctions;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.forge.loot.BnCSlicingModifier;
import umpaz.brewinandchewin.forge.network.BnCNetwork;
import umpaz.brewinandchewin.forge.platform.BnCPlatformHelperForge;
import umpaz.brewinandchewin.forge.registry.BnCAttachments;
import umpaz.brewinandchewin.forge.registry.BnCFluidTypes;

@Mod(BrewinAndChewin.MODID)
public class BrewinAndChewinForge {
   public BrewinAndChewinForge() {
      IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
      BrewinAndChewin.init(new BnCPlatformHelperForge());
      ForgeMod.enableMilkFluid();
      BnCRecipeBookTypes.init();
      modBus.addListener(BrewinAndChewinForge::registerContent);
      modBus.addListener(BnCAttachments::registerCapabilities);
      modBus.addListener(BnCConfiguration::onConfigChanged);
      modBus.addListener(BrewinAndChewinForge::commonSetup);
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> umpaz.brewinandchewin.forge.client.BrewinAndChewinForgeClient.init(modBus));
   }

   private static void commonSetup(FMLCommonSetupEvent event) {
      BnCNetwork.register();
      // Port: 4.5.0's neoforge:compostables data map.
      event.enqueueWork(() -> {
         ComposterBlock.COMPOSTABLES.put(BnCItems.KIMCHI, 0.5F);
         ComposterBlock.COMPOSTABLES.put(BnCItems.PICKLED_PICKLES, 0.5F);
         ComposterBlock.COMPOSTABLES.put(BnCItems.QUICHE_SLICE, 0.85F);
         ComposterBlock.COMPOSTABLES.put(BnCItems.QUICHE, 0.85F);
      });
   }

   // Port: upstream's RegisterEvent dispatch. Forge fires effects and fluids before blocks and items, so the static fields
   // each registry class builds on first touch already find what they reference.
   private static void registerContent(RegisterEvent event) {
      event.register(ForgeRegistries.Keys.MOB_EFFECTS, BnCEffects::registerAll);
      event.register(ForgeRegistries.Keys.FLUIDS, BnCFluids::registerAll);
      event.register(ForgeRegistries.Keys.FLUID_TYPES, BnCFluidTypes::registerAll);
      event.register(ForgeRegistries.Keys.BLOCKS, BnCBlocks::registerAll);
      event.register(ForgeRegistries.Keys.ITEMS, BnCItems::registerAll);
      event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, BnCBlockEntityTypes::registerAll);
      event.register(ForgeRegistries.Keys.MENU_TYPES, BnCMenuTypes::registerAll);
      event.register(ForgeRegistries.Keys.PARTICLE_TYPES, BnCParticleTypes::registerAll);
      event.register(ForgeRegistries.Keys.RECIPE_TYPES, BnCRecipeTypes::registerAll);
      event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, BnCRecipeSerializers::registerAll);
      event.register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> helper.register(BnCSlicingModifier.ID, BnCSlicingModifier.CODEC.get()));
      event.register(Registry.LOOT_FUNCTION_REGISTRY, BnCLootFunctions::registerAll);
   }
}
