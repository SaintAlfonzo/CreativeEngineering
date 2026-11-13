package com.SaintAlfonzo.solraspberrypie;

import com.SaintAlfonzo.solraspberrypie.data.RpData;
import com.SaintAlfonzo.solraspberrypie.item.LiquidDietLogItem;
import com.SaintAlfonzo.solraspberrypie.logic.ServerEvents;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.recipe.DietBookRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spice of Life: Raspberry Pie Addition. Selectable diet bonuses on top of Spice of Life: Apple Pie Edition
 * (feature_writeup/raspberry_pie.md). Apple Pie keeps every diversity calculation and the food memory; this mod owns the
 * bonus ladder, slot choices, respecs, the drink split and the Liquid Diet Log.
 */
@Mod(SolRaspberryPie.MOD_ID)
public final class SolRaspberryPie {
    public static final String MOD_ID = "solraspberrypie";
    public static final Logger LOG = LoggerFactory.getLogger("Raspberry Pie");

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<Item> LIQUID_DIET_LOG = ITEMS.register("liquid_diet_log",
            () -> new LiquidDietLogItem(new Item.Properties().stacksTo(1).tab(net.minecraft.world.item.CreativeModeTab.TAB_MISC)));
    /** Curative-item marker on every diet effect instance (9.1). Nothing cures with it; it only identifies ours. */
    public static final RegistryObject<Item> MARKER = ITEMS.register("diet_marker", () -> new Item(new Item.Properties()));
    public static final RegistryObject<RecipeSerializer<DietBookRecipe>> DIET_BOOK =
            SERIALIZERS.register("diet_book", DietBookRecipe.Serializer::new);

    public SolRaspberryPie() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        SERIALIZERS.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener((RegisterCapabilitiesEvent e) -> e.register(RpData.class));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RpConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RpConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);
        net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> com.SaintAlfonzo.solraspberrypie.client.ClientSetup.init(modBus));
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(RpNetwork::register);
    }
}
