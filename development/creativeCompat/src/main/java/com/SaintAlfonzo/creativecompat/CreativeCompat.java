package com.SaintAlfonzo.creativecompat;

import com.SaintAlfonzo.creativecompat.block.VineShearing;
import com.SaintAlfonzo.creativecompat.compat.BotanistPot;
import com.SaintAlfonzo.creativecompat.compat.CraftingMenuPolymorphClient;
import com.SaintAlfonzo.creativecompat.compat.CraftingMenuPolymorphCompat;
import com.SaintAlfonzo.creativecompat.compat.JarFluidPipes;
import com.SaintAlfonzo.creativecompat.compat.JarMixing;
import com.SaintAlfonzo.creativecompat.compat.PlantBoxModels;
import com.SaintAlfonzo.creativecompat.compat.PlantBoxModelsClient;
import com.SaintAlfonzo.creativecompat.compat.SackBundle;
import com.SaintAlfonzo.creativecompat.compat.SophisticatedPolymorphClient;
import com.SaintAlfonzo.creativecompat.compat.SophisticatedPolymorphCompat;
import com.SaintAlfonzo.creativecompat.compat.TrashSlotCompat;
import com.SaintAlfonzo.creativecompat.compat.jei.BurnRows;
import com.SaintAlfonzo.creativecompat.compat.jei.CampfireRows;
import com.SaintAlfonzo.creativecompat.cats.CatFeather;
import com.SaintAlfonzo.creativecompat.cats.CozyCats;
import com.SaintAlfonzo.creativecompat.mobs.MonsterBoxVault;
import com.SaintAlfonzo.creativeengineering.campfire.CampfireRainInfo;
import com.SaintAlfonzo.creativeengineering.compat.JarContents;
import com.SaintAlfonzo.creativeengineering.compat.jei.world.WorldRows;
import com.SaintAlfonzo.creativeengineering.compat.jei.world.WorldTab;
import com.SaintAlfonzo.creativeengineering.surface.LeafPileSlow;
import com.SaintAlfonzo.creativeengineering.swap.ConfigRename;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creative Compat (feature_writeup/creative_compat.md): the pack's tweaks to third-party mods that have no wider system
 * of their own, carved out of patcherMod on 2026-11-01 (creative_split.md 7.5, 7.6). Every other Creative jar requires
 * it; it requires patcherMod ({@code creativeengineering}, later Creative Common). Each tweak applies only when its mod
 * is loaded: mixins through compat/CompatMixinPlugin, listeners through the {@link ModList} checks below.
 *
 * Ids stay: the botanist pot block entity, the sack network channel, models, textures, tags, loot tables and lang keys
 * keep the {@code creativeengineering} namespace ({@link #NS}). This mod id is only the jar's identity, its config
 * file and its script packs.
 */
@Mod(CreativeCompat.MOD_ID)
public class CreativeCompat {

    public static final String MOD_ID = "creativecompat";
    /** The namespace of every id this mod uses: patcherMod's, unchanged by the carve. */
    public static final String NS = "creativeengineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String OLD_FILE = "creativeengineering-common.toml";
    private static final String FILE = MOD_ID + "-common.toml";

    public CreativeCompat() {
        // The sections came from patcherMod's file: the first boot copies the player's values over (then patcherMod
        // drops them from its file when Forge corrects it). Must run before Forge reads the COMMON configs.
        boolean carried = ConfigRename.carrySections(OLD_FILE, FILE, CompatConfig.SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC, FILE);
        if (!carried) warnOldKeys();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(CreativeCompat::commonSetup);

        // What patcherMod's WTHIT and JEI code reads from here, through its own holders (no patcherMod class loads this
        // jar's classes). Read live, so config reloads apply.
        LeafPileSlow.set(() -> CompatConfig.SPEC.isLoaded() ? CompatConfig.LEAF_PILES.slowFromLayers.get() : LeafPileSlow.DEFAULT);
        CampfireRainInfo.set(
                () -> CompatConfig.SPEC.isLoaded() && CompatConfig.CAMPFIRE.rainDouses.get(),
                () -> CompatConfig.SPEC.isLoaded() ? CompatConfig.CAMPFIRE.rainOutTicks.get() : 160);
        if (ModList.get().isLoaded("supplementaries")) {
            // Mixed jars (JarMixing): the WTHIT jar line groups the slots by item
            JarContents.set(JarMixing::kinds, JarMixing::total);
        }
        if (ModList.get().isLoaded("jei")) {
            // JEI World Interactions: Burning and Campfire rows (WorldRows is JEI-free; JEI loads its plugin later)
            WorldRows.provider(WorldTab.BURNING, BurnRows::rows);
            WorldRows.provider(WorldTab.CAMPFIRE, CampfireRows::rows);
        }

        // Beautify botanist workbench as a flower pot (compat/BotanistPot, mixin/BotanistWorkbenchPotMixin; flower_pots.md).
        if (ModList.get().isLoaded("beautify")) {
            BotanistPot.register(modEventBus);
        }
        // Flower box plant models: glow berries / lichen (Supplementaries, Another Furniture, the botanist pot).
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(PlantBoxModelsClient::onRegisterAdditional);
        }
        // Sacks work like bundles in inventory screens (compat.SackBundle, mixin.SupplementariesSackItemMixin): the
        // scroll-to-rotate packet. The scroll listener is compat.SackBundleScroll (client).
        if (ModList.get().isLoaded("supplementaries")) {
            SackBundle.register();
            // Jars and goblets take Forge fluid pipes (Create pumps): compat.JarFluidPipes, mixed_jars.md 10.
            JarFluidPipes.register();
        }
        // Polymorph recipe selection in the IE Engineer's Crafting Table and the Pretty Pipes Crafting Terminal, and in
        // the Sophisticated crafting upgrade (server half; the client half is ClientModEvents).
        if (ModList.get().isLoaded("polymorph")) {
            MinecraftForge.EVENT_BUS.register(CraftingMenuPolymorphCompat.class);
            if (ModList.get().isLoaded("sophisticatedcore")) {
                MinecraftForge.EVENT_BUS.register(SophisticatedPolymorphCompat.class);
            }
        }
        // Vine shearing ([vine_shearing]): shears stop modded vines and growing-plant tips growing.
        MinecraftForge.EVENT_BUS.register(VineShearing.class);
        // Quark monster boxes as vaults ([monster_box]): sneak-only breaking; the tick is mixin.MonsterBoxBlockEntityMixin.
        if (ModList.get().isLoaded("quark")) {
            MinecraftForge.EVENT_BUS.register(MonsterBoxVault.class);
        }
        // Pet cats (feature_writeup/cozy_cats.md 10): DI's Feather on a Stick hooks mobs for cats to attack ([cats]);
        // Cozy: no ancient fish trades, no gender line in the collar tooltip (the chat line and the DI / Cozy mode split
        // are mixins).
        if (ModList.get().isLoaded("domesticationinnovation")) {
            MinecraftForge.EVENT_BUS.register(CatFeather.class);
        }
        if (ModList.get().isLoaded("cozys_improved_cats")) {
            // after Cozy's own listener (normal priority) has added its ancient fish trades
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, CozyCats::onTrades);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                MinecraftForge.EVENT_BUS.register(CozyCats.class);
            }
        }

        LOGGER.info("[creativecompat] loaded (config/{})", FILE);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // ItemPhysicServerMixin applies when ItemPhysic first loads its server helper; pre-load the split registry it
        // references so a late transform can resolve it.
        if (ModList.get().isLoaded("itemphysic")) {
            forceLoad("com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit");
        }
        // Glow berries and glow lichen in Supplementaries' flower box (checks Supplementaries itself).
        event.enqueueWork(PlantBoxModels::commonSetup);
    }

    /** Define a class now (with linking) so it exists before any late Mixin transform needs it. */
    private static void forceLoad(String className) {
        try {
            Class.forName(className, true, CreativeCompat.class.getClassLoader());
        } catch (Throwable t) {
            LOGGER.error("[creativecompat] failed to pre-load {}", className, t);
        }
    }

    /** Lines of creativeengineering-common.toml that held the sections moved here. */
    private static final Pattern OLD_KEY = Pattern.compile(
            "^\\s*(\\[(?:leaf_piles|moss|vine_shearing|campfire|monster_box|trash_slot)\\])", Pattern.MULTILINE);

    /**
     * The sections moved out of patcherMod's file. When this file already existed the carry-over did not run, and
     * Forge drops the old sections from patcherMod's file on its next correction, so an edited value left there is
     * lost: say which ones were still there. Runs at construction, before Forge loads and corrects the COMMON configs.
     */
    private static void warnOldKeys() {
        Path old = FMLPaths.CONFIGDIR.get().resolve(OLD_FILE);
        if (!Files.isRegularFile(old)) return;
        String text;
        try {
            text = Files.readString(old, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }
        List<String> found = new ArrayList<>();
        Matcher m = OLD_KEY.matcher(text);
        while (m.find()) found.add(m.group(1));
        if (found.isEmpty()) return;
        LOGGER.warn("[creativecompat] config/{} still has sections that moved to config/{}: {}. "
                + "Creative Compat reads only its own file; copy any edited value over (patcherMod drops these from its file).",
                OLD_FILE, FILE, found);
    }

    /** Client half: MOD bus, client only. */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {

        private ClientModEvents() {}

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (ModList.get().isLoaded("trashslot")) {
                TrashSlotCompat.register();
            }
            if (ModList.get().isLoaded("polymorph")) {
                CraftingMenuPolymorphClient.register();
                if (ModList.get().isLoaded("sophisticatedcore")) {
                    SophisticatedPolymorphClient.register();
                }
            }
        }
    }
}
