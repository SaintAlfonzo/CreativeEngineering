package com.SaintAlfonzo.creativeengineering.compat;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class IECompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    // -------------------------------------------------------------------------
    // register() — called from CreativeEngineering.commonSetup()
    // Runs during mod loading (FMLCommonSetupEvent).
    // Safe for: registering recipe types, allowEnumeratedItemsForRecycling
    //           (lazy lambdas, evaluated later when tags are available).
    // NOT safe for: reading tag contents directly (tags not loaded yet).
    // NOT safe for: prefixToIngotMap (gets overwritten later by other mods).
    // -------------------------------------------------------------------------
    public static void register() {
        LOGGER.info("IECompat.register() called");

        if (!ModList.get().isLoaded("immersiveengineering")) {
            LOGGER.info("IE not loaded, skipping IECompat");
            return;
        }

        // Register spring items for arc recycling via lazy tag enumeration.
        // The lambda is evaluated later when tags are populated, so this is safe here.
        String[][] springMetals = {
            {"iron",   "iron"},
            {"copper", "copper"},
            {"gold",   "gold"},
            {"steel",  "steel"},
            {"zinc",   "zinc"},
            {"aluminum", "aluminum"},
            {"silver", "silver"},
            {"nickel", "nickel"},
            {"brass", "brass"},
            {"constantan", "constantan"},
            {"electrum", "electrum"},
        };
        for (String[] metal : springMetals) {
            String tagName  = metal[0]; // used in forge:springs/iron
            String itemName = metal[1]; // used for logging
            ArcRecyclingChecker.allowEnumeratedItemsForRecycling((tags) -> {
                var registry      = tags.registryOrThrow(Registry.ITEM_REGISTRY);
                var springTag     = TagKey.create(Registry.ITEM_REGISTRY,
                        new ResourceLocation("forge", "springs/" + tagName));
                var smallSpringTag = TagKey.create(Registry.ITEM_REGISTRY,
                        new ResourceLocation("forge", "small_springs/" + tagName));
                var springs       = TagUtils.elementStream(registry, springTag).toList();
                var smallSprings  = TagUtils.elementStream(registry, smallSpringTag).toList();
                springs.forEach(item ->
                        LOGGER.info("  Spring item ({}): {}", itemName, item));
                smallSprings.forEach(item ->
                        LOGGER.info("  Small spring item ({}): {}", itemName, item));
                return java.util.stream.Stream.concat(springs.stream(), smallSprings.stream());
            });
        }
        LOGGER.info("Registered spring enumeration for {} metals", springMetals.length);

        // Register coiling recipe type (Vintage Improvements) for arc recycling
        var coilingType = ForgeRegistries.RECIPE_TYPES
                .getValue(new ResourceLocation("vintageimprovements", "coiling"));
        LOGGER.info("Coiling recipe type: {}", coilingType);
        if (coilingType != null) {
            ArcRecyclingChecker.allowRecipeTypeForRecycling(coilingType);
            LOGGER.info("Registered coiling recipe type for recycling");
        } else {
            LOGGER.warn("Coiling recipe type not found - vintageimprovements may not be loaded");
        }

        // Register rolling recipe type (Create Addition) for arc recycling
        var rollingType = ForgeRegistries.RECIPE_TYPES
                .getValue(new ResourceLocation("createaddition", "rolling"));
        LOGGER.info("Rolling recipe type: {}", rollingType);
        if (rollingType != null) {
            ArcRecyclingChecker.allowRecipeTypeForRecycling(rollingType);
            LOGGER.info("Registered rolling recipe type for recycling");
        } else {
            LOGGER.warn("Rolling recipe type not found - createaddition may not be loaded");
        }
        
        // Enumerate all items under forge:<prefix>/<metal> tags dynamically.
				// This bypasses the tag-iteration-order bug where forge:rods/all_metal
				// matches before forge:rods/iron, causing air output.
				//
				// For each prefix in the map (rods, wires, springs, etc.), we collect
				// every forge:<prefix>/<something> tag, skip known non-metal suffixes,
				// and register all matching items for explicit recycling lookup.
				String[] prefixesToEnumerate = {"rods", "wires", "springs", "small_springs"};

				// Suffixes that are not metal names and should be skipped
				/*java.util.Set<String> skipSuffixes = java.util.Set.of(
								"all_metal", "all", "wooden", "stone"
				);

				for (String prefix : prefixesToEnumerate) {
						ArcRecyclingChecker.allowEnumeratedItemsForRecycling((tags) -> {
								var registry = tags.registryOrThrow(Registry.ITEM_REGISTRY);

								// Collect all items whose tags match forge:<prefix>/<anything>
								// but skip non-metal suffixes like all_metal
								return registry.holders()
								    .flatMap(holder -> holder.tags()
								        .filter(tag -> {
								            String path = tag.location().getPath(); // e.g. "rods/iron"
								            if (!tag.location().getNamespace().equals("forge")) return false;
								            if (!path.startsWith(prefix + "/")) return false;
								            String suffix = path.substring(prefix.length() + 1); // e.g. "iron"
								            return !skipSuffixes.contains(suffix);
								        })
								        .map(tag -> holder.value())
								    )
								    .distinct();
						});
				}*/

        LOGGER.info("IECompat.register() complete");
    }

    // -------------------------------------------------------------------------
    // onServerAboutToStart — FORGE bus, EventPriority.LOWEST
    //
    // This is where prefixToIngotMap overrides live.
    //
    // WHY HERE and not in register():
    //   Other mods (Create Addition, Immersive Posts, etc.) write their own
    //   values to prefixToIngotMap during server startup, overwriting anything
    //   set in register(). We need to run AFTER them.
    //
    // WHY NOT ServerStartedEvent:
    //   IE's arc recycling profiler bakes its recipe cache during
    //   ServerAboutToStart. By ServerStarted the cache is already built and
    //   changing the map has no effect.
    //
    // WHY LOWEST priority:
    //   EventPriority.LOWEST runs after all other mods' NORMAL/HIGH priority
    //   handlers for the same event, so we win the write order.
    // -------------------------------------------------------------------------
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!ModList.get().isLoaded("immersiveengineering")) return;

        LOGGER.info("Overriding prefixToIngotMap at LOWEST priority ServerAboutToStart");

        // 2/9 = two nuggets per item (1 ingot = 9 nuggets, rod/wire = 2 nuggets)
        IEApi.prefixToIngotMap.put("spool",         new Integer[]{1, 9});
        IEApi.prefixToIngotMap.put("wires",         new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("rods",          new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("springs",       new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("small_springs", new Integer[]{2, 9});

        LOGGER.info("spool now:         {}", java.util.Arrays.toString(IEApi.prefixToIngotMap.get("spool")));
        LOGGER.info("wires now:         {}", java.util.Arrays.toString(IEApi.prefixToIngotMap.get("wires")));
        LOGGER.info("rods now:          {}", java.util.Arrays.toString(IEApi.prefixToIngotMap.get("rods")));
        LOGGER.info("springs now:       {}", java.util.Arrays.toString(IEApi.prefixToIngotMap.get("springs")));
        LOGGER.info("small_springs now: {}", java.util.Arrays.toString(IEApi.prefixToIngotMap.get("small_springs")));
    }
}

/*package com.SaintAlfonzo.creativeengineering.compat;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class IECompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register() {
        LOGGER.info("***IECOMPAT***");
        if (!ModList.get().isLoaded("immersiveengineering")) {
            LOGGER.info("IE not loaded, skipping IECompat");
            return;
        }
        LOGGER.info("Registering IE recycling prefixes");

        IEApi.prefixToIngotMap.put("springs", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("small_springs", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("wires", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("rods", new Integer[]{2, 9});
        LOGGER.info("Added springs, wires, rods to prefixToIngotMap");

        String[][] springMetals = {
            {"iron",   "iron"},
            {"copper", "copper"},
            {"gold",   "golden"},
            {"steel",  "steel"}
        };
        for (String[] metal : springMetals) {
            String tagName = metal[0];
            String itemName = metal[1];
            ArcRecyclingChecker.allowEnumeratedItemsForRecycling((tags) -> {
                var registry = tags.registryOrThrow(Registry.ITEM_REGISTRY);
                var springTag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "springs/" + tagName));
                var smallSpringTag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "small_springs/" + tagName));
                var springs = TagUtils.elementStream(registry, springTag).toList();
                var smallSprings = TagUtils.elementStream(registry, smallSpringTag).toList();
                springs.forEach(item -> LOGGER.info("  Spring item ({}): {}", itemName, item));
                smallSprings.forEach(item -> LOGGER.info("  Small spring item ({}): {}", itemName, item));
                return java.util.stream.Stream.concat(springs.stream(), smallSprings.stream());
            });
        }
        LOGGER.info("Registered spring enumeration for {} metals", springMetals.length);

        var coilingType = ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation("vintageimprovements", "coiling"));
        LOGGER.info("Coiling recipe type: {}", coilingType);
        if (coilingType != null) {
            ArcRecyclingChecker.allowRecipeTypeForRecycling(coilingType);
            LOGGER.info("Registered coiling recipe type for recycling");
        } else {
            LOGGER.warn("Coiling recipe type not found - vintageimprovements may not be loaded");
        }

        var rollingType = ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation("createaddition", "rolling"));
        LOGGER.info("Rolling recipe type: {}", rollingType);
        if (rollingType != null) {
            ArcRecyclingChecker.allowRecipeTypeForRecycling(rollingType);
            LOGGER.info("Registered rolling recipe type for recycling");
        } else {
            LOGGER.warn("Rolling recipe type not found - createaddition may not be loaded");
        }

        LOGGER.info("IE recycling prefixes registered");
        
        // In your register() method alongside the spring enumeration
				String[] ironWireItems = {"creativeengineering:iron_wire"};
				String[] ironRodItems  = {"immersiveengineering:stick_iron", "immersiveposts:stick_iron"};
				
				ArcRecyclingChecker.allowEnumeratedItemsForRecycling((tags) -> {
						var registry = tags.registryOrThrow(Registry.ITEM_REGISTRY);
						var wireTag   = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "wires/iron"));
						var rodTag    = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "rods/iron"));
						return java.util.stream.Stream.concat(
								blusunrize.immersiveengineering.api.utils.TagUtils.elementStream(registry, wireTag),
								blusunrize.immersiveengineering.api.utils.TagUtils.elementStream(registry, rodTag)
						);
				});
				
				// Test getPreferredTagStack for ingots directly
				LOGGER.info("=== Testing getPreferredTagStack for ingots ===");
				String[] ingotsToTest = {"iron", "copper", "gold", "steel", "lead", "uranium", "constantan", "nickel", "aluminum"};
				for (String metal : ingotsToTest) {
						try {
								var ingotTag = blusunrize.immersiveengineering.api.IETags.getIngot(metal);
								LOGGER.info("IETags.getIngot({}) = {}", metal, ingotTag);
								var wrapper = blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper(ingotTag);
								var preferred = blusunrize.immersiveengineering.api.IEApi.getPreferredTagStack(registryAccess, wrapper);
								LOGGER.info("getPreferredTagStack({}) = {}", metal, preferred);
						} catch (Exception e) {
								LOGGER.error("Error testing getPreferredTagStack for {}: {}", metal, e.getMessage());
						}
				}

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
						{"immersiveengineering", "wire_copper",      "IE Copper Wire"},
						{"minecraft",            "iron_ingot",       "Vanilla Iron Ingot"},
						{"immersiveengineering", "ingot_iron",       "IE Iron Ingot"},
				};

				for (String[] entry : testItems) {
						String namespace = entry[0];
						String path      = entry[1];
						String label     = entry[2];

						LOGGER.info("=== Testing {} ({}) ===", label, namespace + ":" + path);
						var item = net.minecraftforge.registries.ForgeRegistries.ITEMS
								    .getValue(new ResourceLocation(namespace, path));
						if (item == null || item == net.minecraft.world.item.Items.AIR) {
								LOGGER.warn("  Item not found in registry, skipping");
								continue;
						}
						net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
						LOGGER.info("  Stack: {}", stack);
						stack.getTags().forEach(tag -> LOGGER.info("  Tag: {}", tag.location()));

						com.mojang.datafixers.util.Pair<net.minecraft.world.item.ItemStack, Double> result =
								    blusunrize.immersiveengineering.api.ApiUtils.breakStackIntoPreciseIngots(registryAccess, stack);
						if (result != null) {
								LOGGER.info("  breakStackIntoPreciseIngots: {} x {}", result.getFirst(), result.getSecond());
						} else {
								LOGGER.info("  breakStackIntoPreciseIngots: returned null - item not recognized");
						}
				}
    }
}
/*package com.SaintAlfonzo.creativeengineering.compat;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class IECompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register() {
        LOGGER.info("***IECOMPAT***");
        if (!ModList.get().isLoaded("immersiveengineering")) {
            LOGGER.info("IE not loaded, skipping IECompat");
            return;
        }
        LOGGER.info("Registering IE recycling prefixes");
        
        // set springs, rods and wires as 2 nugget recycle output

        IEApi.prefixToIngotMap.put("springs", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("small_springs", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("wires", new Integer[]{2, 9});
        IEApi.prefixToIngotMap.put("rods", new Integer[]{2, 9});
        LOGGER.info("Added springs, wires, rods to prefixToIngotMap");

        String[][] springMetals = {
						{"iron",   "iron"},
						{"copper", "copper"},
						{"gold",   "golden"},
						{"steel",  "steel"}
				};
				for (String[] metal : springMetals) {
						String tagName = metal[0];   // used in forge:springs/iron
						String itemName = metal[1];  // used for logging
						ArcRecyclingChecker.allowEnumeratedItemsForRecycling((tags) -> {
								var registry = tags.registryOrThrow(Registry.ITEM_REGISTRY);
								var springTag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "springs/" + tagName));
								var smallSpringTag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "small_springs/" + tagName));
								var springs = TagUtils.elementStream(registry, springTag).toList();
								var smallSprings = TagUtils.elementStream(registry, smallSpringTag).toList();
								springs.forEach(item -> LOGGER.info("  Spring item ({}): {}", itemName, item));
								smallSprings.forEach(item -> LOGGER.info("  Small spring item ({}): {}", itemName, item));
								return java.util.stream.Stream.concat(springs.stream(), smallSprings.stream());
						});
				}
				LOGGER.info("Registered spring enumeration for {} metals", springMetals.length);

        var coilingType = ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation("vintageimprovements", "coiling"));
        LOGGER.info("Coiling recipe type: {}", coilingType);
        if (coilingType != null) {
            ArcRecyclingChecker.allowRecipeTypeForRecycling(coilingType);
            LOGGER.info("Registered coiling recipe type for recycling");
        } else {
            LOGGER.warn("Coiling recipe type not found - vintageimprovements may not be loaded");
        }

        LOGGER.info("IE recycling prefixes registered");
    }
    
    @net.minecraftforge.eventbus.api.SubscribeEvent
		public static void onServerAboutToStart(net.minecraftforge.event.server.ServerAboutToStartEvent event)
		{
				LOGGER.info("Overriding prefixToIngotMap at ServerAboutToStart");
				blusunrize.immersiveengineering.api.IEApi.prefixToIngotMap.put("wires", new Integer[]{2, 9});
				blusunrize.immersiveengineering.api.IEApi.prefixToIngotMap.put("rods", new Integer[]{2, 9});
				blusunrize.immersiveengineering.api.IEApi.prefixToIngotMap.put("springs", new Integer[]{2, 9});
				blusunrize.immersiveengineering.api.IEApi.prefixToIngotMap.put("small_springs", new Integer[]{2, 9});
				LOGGER.info("Override complete - map now contains:");
				blusunrize.immersiveengineering.api.IEApi.prefixToIngotMap.forEach((k, v) -> {
				    LOGGER.info("  {} -> {}", k, java.util.Arrays.toString(v));
				});
		}
}*/
