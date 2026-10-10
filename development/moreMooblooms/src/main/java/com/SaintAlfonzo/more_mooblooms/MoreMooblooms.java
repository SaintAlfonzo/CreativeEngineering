package com.SaintAlfonzo.more_mooblooms;

import com.SaintAlfonzo.more_mooblooms.client.ClientSetup;
import com.SaintAlfonzo.more_mooblooms.compat.BuzzierBeesSet;
import com.SaintAlfonzo.more_mooblooms.entity.CowFeeding;
import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.SaintAlfonzo.more_mooblooms.network.SyncVariantsPacket;
import com.SaintAlfonzo.more_mooblooms.spawn.MoobloomSpawns;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * More Mooblooms: a moobloom of our own with data-driven variants (feature_writeup/mooblooms.md section 4).
 *
 * <b>Core (always):</b> the entity ({@link MMEntities}), the variant table ({@link MoobloomVariants}) and breeding
 * rules ({@link MoobloomBreeding}), both synced to clients by {@link SyncVariantsPacket}, the spawn egg, cow feeding ({@link CowFeeding}), the natural spawn swap
 * ({@link MoobloomSpawns} + mixin/NaturalSpawnerMixin), rendering (client/), the config ({@link MMConfig}).
 * Its data (variants, loot table, {@code forge:milkable}, feed and spawn tags) is in the jar's own resources;
 * the witherbloom spawns are the built-in pack {@code packs/more_mooblooms/witherbloom_spawns/} (config switch).
 *
 * <b>Content sets:</b> one per optional mod, active only when that mod is loaded. A set's data is a hidden,
 * required built-in pack {@code packs/<mod id>/} ({@link #addPacks}); its code runs only then; the mixin plugin
 * gates its mixins.
 * <ul>
 *   <li>{@code buzzier_bees}: {@link BuzzierBeesSet}, with one sub-pack per config switch</li>
 *   <li>{@code jei}: compat/jei (loaded by JEI itself through @JeiPlugin; checks the set's own conditions)</li>
 *   <li>{@code blueprint}: the Two by Two advancement modifier</li>
 *   <li>{@code alexsmobs}, {@code supplementaries}, {@code abnormals_delight}, {@code botania}, {@code delightful}:
 *       the moobloom joins that mod's cow tags</li>
 * </ul>
 */
@Mod(MoreMooblooms.ID)
public class MoreMooblooms {

    public static final String ID = "more_mooblooms";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Content sets that are only a data pack (tag and advancement additions for that mod). */
    private static final List<String> TAG_PACK_MODS =
            List.of("blueprint", "alexsmobs", "supplementaries", "abnormals_delight", "botania", "delightful");

    public MoreMooblooms() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MMConfig.SPEC);

        MMEntities.register(modBus);
        MoobloomSpawnEggItem.register(modBus);
        modBus.addListener((FMLCommonSetupEvent e) -> SyncVariantsPacket.register());
        modBus.addListener(MoreMooblooms::addPacks);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.register(modBus));

        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> {
            e.addListener(new MoobloomVariants.Loader());
            e.addListener(new MoobloomSpawns.Loader());
            e.addListener(new MoobloomBreeding.Loader());
        });
        MinecraftForge.EVENT_BUS.addListener(SyncVariantsPacket::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(CowFeeding::onEntityInteract);

        if (isLoaded(BuzzierBeesSet.MOD_ID)) BuzzierBeesSet.register(modBus);
    }

    public static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * Adds the packs of every active content set. Tag/advancement packs go at the bottom (they only add).
     * The Buzzier Bees sub-packs replace Buzzier Bees' own files by path, so they go on top of the mod
     * resources; the pack's kubejs/data still loads above them.
     */
    private static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return; // every set is data only
        Path root = ModList.get().getModFileById(ID).getFile().findResource("packs");
        if (MMConfig.on(MMConfig.WITHERBLOOM_SPAWNS)) {
            addPack(event, root.resolve(ID).resolve("witherbloom_spawns"), ID + ":witherbloom_spawns",
                    "More Mooblooms: witherbloom spawns", Pack.Position.BOTTOM);
        }
        for (String mod : TAG_PACK_MODS) {
            if (isLoaded(mod)) addPack(event, root.resolve(mod), ID + ":" + mod, "More Mooblooms: " + mod, Pack.Position.BOTTOM);
        }

        if (isLoaded(BuzzierBeesSet.MOD_ID)) {
            for (BuzzierBeesSet.DataSwitch s : BuzzierBeesSet.dataSwitches()) {
                if (!MMConfig.on(s.value())) continue;
                addPack(event, root.resolve(BuzzierBeesSet.MOD_ID).resolve(s.folder()),
                        ID + ":" + BuzzierBeesSet.MOD_ID + "/" + s.folder(),
                        "More Mooblooms: Buzzier Bees " + s.folder(), Pack.Position.TOP);
            }
        }
    }

    private static void addPack(AddPackFindersEvent event, Path dir, String packId, String title, Pack.Position position) {
        event.addRepositorySource((consumer, constructor) -> {
            try (PathPackResources probe = new PathPackResources(packId, dir)) {
                PackMetadataSection meta = probe.getMetadataSection(PackMetadataSection.SERIALIZER);
                if (meta == null) throw new IOException("no pack.mcmeta in " + dir);
                // required: always on, can't be switched off; hidden: not listed in the pack screens.
                consumer.accept(constructor.create(packId, Component.literal(title), true,
                        () -> new PathPackResources(packId, dir), meta, position, PackSource.BUILT_IN, true));
            } catch (IOException e) {
                throw new IllegalStateException("More Mooblooms pack " + packId + " is broken", e);
            }
        });
    }
}
