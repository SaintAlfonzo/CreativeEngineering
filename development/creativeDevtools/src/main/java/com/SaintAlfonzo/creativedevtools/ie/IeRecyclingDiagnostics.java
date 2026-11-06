package com.SaintAlfonzo.creativedevtools.ie;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * IE arc-recycling diagnostics, logged once the server has started (moved from patcherMod's
 * {@code CreativeEngineering.onServerStarted}, feature_writeup/creative_devtools.md 2). Used ONLY for diagnostics:
 * the actual prefixToIngotMap override lives in patcherMod's {@code IECompat.onServerAboutToStart}, which runs BEFORE
 * the arc recycling profiler bakes its cache. Registered by {@code CreativeDevtools} only when Immersive Engineering
 * is loaded, so the IE API classes are never touched without it.
 */
public final class IeRecyclingDiagnostics {

    private static final Logger LOGGER = LogUtils.getLogger();

    private IeRecyclingDiagnostics() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        net.minecraft.core.RegistryAccess registryAccess = event.getServer().registryAccess();
        var itemRegistry = registryAccess.registryOrThrow(Registry.ITEM_REGISTRY);

        // --- 1. Dump the current prefixToIngotMap so we can confirm our values stuck ---
        LOGGER.info("=== prefixToIngotMap at server start ===");
        IEApi.prefixToIngotMap.forEach((k, v) ->
                LOGGER.info("  {} -> {}", k, java.util.Arrays.toString(v)));

        // --- 2. Check that forge:ingots/<metal> tags resolve to real items ---
        LOGGER.info("=== forge:ingots tag resolution ===");
        String[] ingotsToTest = {"iron", "copper", "gold", "zinc", "steel",
                                 "lead", "uranium", "constantan", "nickel", "aluminum"};
        for (String metal : ingotsToTest) {
            var ingotTag = TagKey.create(Registry.ITEM_REGISTRY,
                    new ResourceLocation("forge", "ingots/" + metal));
            var items = TagUtils.elementStream(itemRegistry, ingotTag).toList();
            LOGGER.info("  forge:ingots/{} contains: {}", metal, items);
        }

        // --- 3. Test IETags.getIngot + getPreferredTagStack for each metal ---
        // This is the exact internal call breakStackIntoPreciseIngots makes.
        // If a metal returns air/empty here, that's why it recycles to nothing.
        LOGGER.info("=== Testing getPreferredTagStack for each metal ===");
        for (String metal : ingotsToTest) {
            try {
                // IETags.getIngot returns a ResourceLocation, not a TagKey
                ResourceLocation ingotRL = IETags.getIngot(metal);
                LOGGER.info("  IETags.getIngot({}) = {}", metal, ingotRL);

                // createItemWrapper needs a TagKey, so wrap the ResourceLocation
               // CORRECT - pass the ResourceLocation directly
								var wrapper = TagUtils.createItemWrapper(ingotRL);

                var preferred = IEApi.getPreferredTagStack(registryAccess, wrapper);
                LOGGER.info("  getPreferredTagStack({}) = {}", metal, preferred);
            } catch (Exception e) {
                LOGGER.error("  Error testing getPreferredTagStack for {}: {}", metal, e.getMessage());
            }
        }

        // --- 4. Run breakStackIntoPreciseIngots on specific items ---
        // Tells us the final recycling output IE would calculate for each item.
        LOGGER.info("=== breakStackIntoPreciseIngots per item ===");
        String[][] testItems = {
            {"immersiveengineering", "stick_iron",       "IE Iron Rod"},
            {"immersiveposts",       "stick_copper",     "IP Copper Rod"},
            {"immersiveposts",       "stick_lead",       "IP Lead Rod"},
            {"immersiveposts",       "stick_uranium",    "IP Uranium Rod"},
            {"immersiveposts",       "stick_constantan", "IP Constantan Rod"},
            {"immersiveposts",       "stick_nickel",     "IP Nickel Rod"},
            {"immersiveposts",       "stick_gold",       "IP Gold Rod"},
            {"immersiveengineering", "stick_aluminum",   "IE Aluminum Rod"},
            {"creativeengineering",  "iron_wire",        "CE Iron Wire"},
            {"creativeengineering",  "gold_wire",        "CE Gold Wire"},
            {"creativeengineering",  "zinc_wire",        "CE Zinc Wire"},
            {"immersiveengineering", "wire_copper",      "IE Copper Wire"},
            {"minecraft",            "iron_ingot",       "Vanilla Iron Ingot"},
            {"immersiveengineering", "ingot_iron",       "IE Iron Ingot"},
            {"vintageimprovements", "zinc_rod", "VI Zinc Rod"},
        };

        for (String[] entry : testItems) {
            String namespace = entry[0];
            String path      = entry[1];
            String label     = entry[2];

            LOGGER.info("  --- {} ({}) ---", label, namespace + ":" + path);

            var item = ForgeRegistries.ITEMS
                    .getValue(new ResourceLocation(namespace, path));
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                LOGGER.warn("    Item not found in registry, skipping");
                continue;
            }

            ItemStack stack = new ItemStack(item);
            LOGGER.info("    Stack: {}", stack);
            stack.getTags().forEach(tag -> LOGGER.info("    Tag: {}", tag.location()));

            var result = blusunrize.immersiveengineering.api.ApiUtils
                    .breakStackIntoPreciseIngots(registryAccess, stack);
            if (result != null) {
                LOGGER.info("    breakStackIntoPreciseIngots: {} x {}",
                        result.getFirst(), result.getSecond());
            } else {
                LOGGER.info("    breakStackIntoPreciseIngots: null (item not recognized)");
            }
        }
        
        var zincSpringTag = TagKey.create(Registry.ITEM_REGISTRY,
								new ResourceLocation("forge", "springs/zinc"));
				var zincSmallSpringTag = TagKey.create(Registry.ITEM_REGISTRY,
								new ResourceLocation("forge", "small_springs/zinc"));
				LOGGER.info("forge:springs/zinc contains: {}", TagUtils.elementStream(itemRegistry, zincSpringTag).toList());
				LOGGER.info("forge:small_springs/zinc contains: {}", TagUtils.elementStream(itemRegistry, zincSmallSpringTag).toList());
    }
}
