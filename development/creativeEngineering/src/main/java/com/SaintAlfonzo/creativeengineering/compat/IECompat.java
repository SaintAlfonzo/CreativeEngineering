package com.SaintAlfonzo.creativeengineering.compat;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.util.orientation.RotationUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class IECompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final TagKey<Block> HAMMER_NO_ROTATE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("creativeengineering", "hammer_no_rotate"));

    // -------------------------------------------------------------------------
    // register() — called from CreativeEngineering.commonSetup()
    // Runs during mod loading (FMLCommonSetupEvent).
    // Safe for: registering recipe types, allowEnumeratedItemsForRecycling
    //           (lazy lambdas, evaluated later when tags are available).
    // NOT safe for: reading tag contents directly (tags not loaded yet).
    // NOT safe for: prefixToIngotMap (gets overwritten later by other mods).
    // -------------------------------------------------------------------------
    // The Engineer's Hammer rotates any block it is used on (onItemUseFirst, before the block's own use), so on a
    // Farmer's Delight cutting board it would turn the board instead of running a hammer cutting recipe
    // (feature_writeup/oven.md 2.5). Blocks in #creativeengineering:hammer_no_rotate (KubeJS blocktags.js) refuse
    // the rotation; the hammer then passes and the block's use runs. Called through enqueueWork (IE's list is a plain
    // ArrayList that IE itself fills during setup).
    public static void registerHammerNoRotate() {
        RotationUtil.blacklist.add((level, pos) -> !level.getBlockState(pos).is(HAMMER_NO_ROTATE));
    }

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
