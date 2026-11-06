package com.SaintAlfonzo.creativeequipment;

import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkinCurios;
import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkinSerializers;
import com.SaintAlfonzo.creativeequipment.compat.NockRecipeBookSync;
import com.SaintAlfonzo.creativeequipment.gear.AirBrakeHandler;
import com.SaintAlfonzo.creativeequipment.gear.ElytraDuplicationSerializers;
import com.SaintAlfonzo.creativeequipment.item.BottledAir;
import com.SaintAlfonzo.creativeequipment.shield.IronShieldTooltip;
import com.SaintAlfonzo.creativeequipment.shield.ShieldBlockHandler;
import com.SaintAlfonzo.creativeequipment.shield.ShieldItems;
import com.SaintAlfonzo.creativeequipment.shield.client.TierShieldClient;
import com.SaintAlfonzo.creativeequipment.stepstool.Stepstool;
import com.SaintAlfonzo.creativeequipment.stepstool.client.StepstoolClient;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

/**
 * Creative Equipment (feature_writeup/creative_equipment.md): the pack's gear, carved out of patcherMod on 2026-11-04
 * (creative_split.md 7.6, wave 7). Tier shields, stepstools, backpack skins, the air brake, Bottled Air, elytra
 * duplication and elytra chestplates, the copper tool tier, armor caps and enchantability, the bone meal charm, the
 * Nock Enough Arrows recipe book fix, and the gear scripts (tool and armor tuning, archery, End gear, dragon autorepair,
 * harvest tiers). Requires patcherMod ({@code creativeengineering}, later Creative Common: the damage tag framework and
 * {@code shield.EffectTickContext} stay there as Common APIs) and Creative Compat.
 *
 * Ids stay: the shields, the recipe serializers, the copper tier, the stepstool channel, tags and lang keys keep the
 * {@code creativeengineering} namespace ({@link #NS}). This mod id is only the jar's identity, its config file and its
 * script packs. The registrations below are the ones patcherMod's constructor made, in the same order.
 */
@Mod(CreativeEquipment.MOD_ID)
public class CreativeEquipment {

    public static final String MOD_ID = "creativeequipment";
    /** The namespace of every id this mod uses: patcherMod's, unchanged by the carve. */
    public static final String NS = "creativeengineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String OLD_FILE = "creativeengineering-common.toml";
    private static final String FILE = MOD_ID + "-common.toml";

    public CreativeEquipment() {
        // The sections came from patcherMod's file: the first boot copies the player's values over (then patcherMod
        // drops them from its file when Forge corrects it). Must run before Forge reads the COMMON configs.
        ConfigRename.carrySections(OLD_FILE, FILE, CEQConfig.SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CEQConfig.SPEC, FILE);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(CreativeEquipment::commonSetup);

        // Elytra duplication recipe serializer (gear/).
        ElytraDuplicationSerializers.register(modEventBus);
        // Tier shields (copper..dragon, iron = the vanilla shield): the items here, the block-check widening in
        // mixin/LivingEntityShieldMixin, everything tunable in CEQConfig.SHIELDS.
        ShieldItems.register(modEventBus);
        // Backpack skins: smithing + clearing recipes, Curios equip block (backpackskin/; the looks are mixin/BackpackSkin*).
        BackpackSkinSerializers.register(modEventBus);
        boolean curios = ModList.get().isLoaded("curios");
        if (curios) MinecraftForge.EVENT_BUS.register(BackpackSkinCurios.class);
        // Stepstools: stand a block higher on the stool in the Curios feet slot (stepstool/; the client half is
        // stepstool/client, the collision mixin.EntityStepstoolCollisionMixin; feature_writeup/stepstool.md). The
        // channel keeps its id, creativeengineering:stepstool.
        Stepstool.register();
        if (curios) MinecraftForge.EVENT_BUS.register(Stepstool.class);
        // Hands, face, feet Curios slots by IMC (compat/CuriosSlots; feature_writeup/curios_slots.md; from Creative
        // Common 2026-11-04).
        if (curios) modEventBus.addListener(com.SaintAlfonzo.creativeequipment.compat.CuriosSlots::enqueue);

        // Elytra air brake velocity effect (the item itself is registered by this jar's startup script
        // kubejs_startup/creativeequipment/items/air_brake.js) - see CEQConfig.AirBrake for the tuning knobs.
        MinecraftForge.EVENT_BUS.register(AirBrakeHandler.class);
        // Bottled Air port (item.BottledAir, [bottled_air]): glass bottle right-clicked in water refills air.
        MinecraftForge.EVENT_BUS.register(BottledAir.class);
        // Tier shields: per-category damage absorption, scaled wear and on-block potion effects
        // (ShieldBlockEvent listener - see CEQConfig.Shields).
        MinecraftForge.EVENT_BUS.register(ShieldBlockHandler.class);
        // Iron tier: folds the player's real vanilla shield into the tier system (ShieldTier.IRON, no new item); this
        // listener gives that item the tier tooltip lines.
        MinecraftForge.EVENT_BUS.register(IronShieldTooltip.class);

        // Copper tool tier, between stone and iron (CETiers; Everything Copper's pickaxe uses it, mixin/CopperPickaxeItemMixin).
        TierSortingRegistry.registerTier(
                CETiers.COPPER,
                new ResourceLocation(NS, "copper"),
                List.of(Tiers.STONE),   // this tier is after stone
                List.of(Tiers.IRON)     // and before iron
        );

        LOGGER.info("[creativeequipment] loaded (config/{})", FILE);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // Nock Enough Arrows' fletching RecipeBookType on a dedicated server (compat/NockRecipeBookSync).
        event.enqueueWork(NockRecipeBookSync::register);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            TierShieldClient.setup(event);
            if (ModList.get().isLoaded("curios")) {
                MinecraftForge.EVENT_BUS.register(StepstoolClient.class);
                // "Slot:" line on every Curios item, also where the item's mod hides it (ui/tooltips #8).
                MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativeequipment.compat.CuriosSlotTooltip.class);
            }
        }

        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            StepstoolClient.onRegisterKeys(event);
        }
    }
}
