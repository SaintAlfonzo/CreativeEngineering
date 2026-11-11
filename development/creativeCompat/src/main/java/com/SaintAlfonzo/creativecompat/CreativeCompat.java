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
import com.SaintAlfonzo.creativecompat.cats.CatFeather;
import com.SaintAlfonzo.creativecompat.cats.CozyCats;
import com.SaintAlfonzo.creativecompat.mobs.MonsterBoxVault;
import com.SaintAlfonzo.creativecompat.shrapnel.ShrapnelEntities;
import com.SaintAlfonzo.creativecompat.shrapnel.ShrapnelSpawner;
import com.SaintAlfonzo.creativecompat.shrapnel.TntShrapnelHandler;
import com.SaintAlfonzo.creativecompat.surface.CamoSpeedTooltip;
import com.SaintAlfonzo.creativecompat.surface.LeafPileSlow;
import com.SaintAlfonzo.creativecompat.surface.SurfaceSpeeds;
import com.SaintAlfonzo.creativecommon.util.BurnBursts;
import com.SaintAlfonzo.creativecompat.compat.JarContents;
import com.SaintAlfonzo.creativecompat.compat.jei.world.WorldRows;
import com.SaintAlfonzo.creativecompat.compat.jei.world.WorldTab;
import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import com.SaintAlfonzo.creativecompat.portalgun.PortalGunRecipes;
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
 * Ids follow the owner (feature_writeup/namespace_move.md): the shrapnel entity, the botanist pot block entity, the
 * cardboard box, models, textures, tags and loot tables use this mod's namespace ({@link #NS}); until 2026-11-10 they
 * were {@code creativeengineering}, and data/creativecompat/swaps/owner_namespace.json carries old saves. Lang keys
 * and NBT keys kept their old text. This mod id is only the jar's identity, its config
 * file and its script packs.
 */
@Mod(CreativeCompat.MOD_ID)
public class CreativeCompat {

    public static final String MOD_ID = "creativecompat";
    /** The namespace of every id this mod uses (was creativeengineering until the 2026-11-10 namespace move). */
    public static final String NS = MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String OLD_FILE = "creativeengineering-common.toml";
    private static final String FILE = MOD_ID + "-common.toml";

    public CreativeCompat() {
        // The sections came from patcherMod's file: the first boot copies the player's values over (then patcherMod
        // drops them from its file when Forge corrects it). Must run before Forge reads the COMMON configs.
        boolean carried = ConfigRename.carrySections(OLD_FILE, FILE, CompatConfig.SECTIONS);
        // [shrapnel], [surface] and [vein_undo] came from Creative Engineering's file of the same name (2026-11-06),
        // after this file's first release: carried into a new file, or merged into an existing one.
        if (!ConfigRename.carrySections(OLD_FILE, FILE, CompatConfig.ENGINEERING_SECTIONS))
            ConfigRename.mergeSections(OLD_FILE, FILE, CompatConfig.ENGINEERING_SECTIONS);
        // [jei], [gui_title], [toasts], [tooltips], [coaster], [wthit] came from Creative Common's file with the interface
        // code (2026-11-07); before the 2026-11-04 swap they were in patcherMod's creativeengineering-common.toml.
        // Sections this file already has are kept; Common drops its copies when Forge corrects its file.
        if (!ConfigRename.carrySections("creativecommon-common.toml", FILE, CompatConfig.COMMON_SECTIONS))
            ConfigRename.mergeSections("creativecommon-common.toml", FILE, CompatConfig.COMMON_SECTIONS);
        ConfigRename.mergeSections(OLD_FILE, FILE, CompatConfig.COMMON_SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC, FILE);
        if (!carried) warnOldKeys();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(CreativeCompat::commonSetup);

        // Known harmless log lines repeated by the hundred thousand (compat/LogSpamFilter, perf.md 19)
        com.SaintAlfonzo.creativecompat.compat.LogSpamFilter.install();
        // Distant Horizons per dimension (compat/DhDimensions): no Nether generation is mixin.DhNoGenerationMixin;
        // the Nether's LOD radius and far fog are API overrides set on the client.
        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("distanthorizons")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecompat.compat.DhDimensionView.class);
        }
        // One deep red fog colour for the whole Nether (compat/NetherFogColour, perf.md 19.3)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecompat.compat.NetherFogColour.class);
        }

        // The TNT shrapnel entity (shrapnel/, from Creative Engineering 2026-11-06, id creativecompat:shrapnel):
        // fired on primed-TNT-entity detonation, and for the itemphysic.ItemBurnTransform burn-explosion rule through
        // Common's util.BurnBursts. Tuning: [shrapnel].
        ShrapnelEntities.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(TntShrapnelHandler.class);
        BurnBursts.register(ShrapnelSpawner::burst);

        // Portal Gun dyeing (portalgun/, plan_portal_gun.md 4): the serializer names the gun only by id, so it registers
        // always; its recipe JSON loads only with the mod.
        PortalGunRecipes.register(modEventBus);
        // Gun portals dissolve on the owner's logout or dimension change (plan_portal_changes.md 3.1)
        if (ModList.get().isLoaded("portalgun")) {
            MinecraftForge.EVENT_BUS.register(com.SaintAlfonzo.creativecompat.portalgun.PortalGunDissolve.class);
        }

        // Walking on blocks (surface/, from Creative Engineering 2026-11-06; [surface]). Framed / copycat blocks: say
        // on BlockRunner's speed tooltip line whether they walk like their camo. BlockRunner tier speeds come from
        // [surface.speed] (mixin/BlockRunnerSpeedConfigMixin): re-apply them when the toml is saved, and log an error
        // if the mixin never ran. The WTHIT Run Speed line (waila_plugins.json) reads [leaf_piles] slowFromLayers.
        MinecraftForge.EVENT_BUS.register(CamoSpeedTooltip.class);
        modEventBus.addListener(SurfaceSpeeds::onConfigReload);
        MinecraftForge.EVENT_BUS.register(SurfaceSpeeds.class);
        LeafPileSlow.set(() -> CompatConfig.SPEC.isLoaded() ? CompatConfig.LEAF_PILES.slowFromLayers.get() : LeafPileSlow.DEFAULT);

        // What Creative Common's WTHIT and JEI code reads from here, through its own holders (no Common class loads this
        // jar's classes). Read live, so config reloads apply.
        if (ModList.get().isLoaded("supplementaries")) {
            // Mixed jars (JarMixing): the WTHIT jar line groups the slots by item
            JarContents.set(JarMixing::kinds, JarMixing::total);
        }
        if (ModList.get().isLoaded("jei")) {
            // JEI World Interactions: Burning rows (Campfire Rules moved to Creative Delight 2026-11-04) (WorldRows is JEI-free; JEI loads its plugin later)
            WorldRows.provider(WorldTab.BURNING, BurnRows::rows);
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
        // Immersive Paintings ([paintings]): every modded painting variant on its Datapacks page, built from the
        // installed jars as a hidden data pack (paintings/PaintingPack; immersive_paintings.md). Vanilla's come from
        // the pack's datapack.
        if (ModList.get().isLoaded("immersive_paintings")) {
            modEventBus.addListener(com.SaintAlfonzo.creativecompat.paintings.PaintingPack::add);
        }
        // Banner patterns on Bedspreads beds and Customizable Elytra wings for patterns those mods bundle no art for:
        // built from the installed jars as a hidden resource pack (banners/BannerPatternPack; creative_compat.md 6.3).
        if (FMLEnvironment.dist == Dist.CLIENT
                && (ModList.get().isLoaded("bedspreads") || ModList.get().isLoaded("customizableelytra"))) {
            modEventBus.addListener(com.SaintAlfonzo.creativecompat.banners.BannerPatternPack::add);
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
            "^\\s*(\\[(?:leaf_piles|moss|vine_shearing|monster_box|trash_slot)\\])", Pattern.MULTILINE);

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

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ShrapnelEntities.SHRAPNEL.get(),
                    context -> new ThrownItemRenderer<>(context, 1.0F, false));
        }
    }
}
