package com.SaintAlfonzo.incubation_additions;

import com.SaintAlfonzo.incubation_additions.client.DuckModEggModels;
import com.SaintAlfonzo.incubation_additions.crate.WaterfowlEggCrates;
import com.SaintAlfonzo.incubation_additions.nest.EmuNests;
import com.SaintAlfonzo.incubation_additions.nest.WaterfowlNests;
import com.SaintAlfonzo.incubation_additions.shared.BuiltInPacks;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Incubation Additions: this pack's additions to Incubation (Team Abnormals), one content set per bird mod.
 * Each set is only switched on when its mod is installed; with neither installed the mod does nothing
 * (no blocks, no mixins, no assets, no data).
 * - Alex's Mobs: emu nest ({@link EmuNests}, mixin/EntityEmuEggLayerMixin)
 * - Untitled Duck Mod: duck and goose nests ({@link WaterfowlNests}, mixin/Duck/GooseEntityEggLayerMixin),
 *   duck and goose egg crates ({@link WaterfowlEggCrates}), new item sprites for the duck mod's eggs
 *   (client/DuckModEggModels), and with Vintage Delight too the goose golden egg (goldenegg.GoldenGooseEgg,
 *   chance in {@link IAConfig})
 * - either: mixin/LayEggInNestGoalMixin keeps every bird off nests that have no variant for its egg
 *
 * A set's assets, recipes, loot tables and tags are not in the jar's root resources but in
 * {@code packs/<mod id>/}, added as a required built-in resource pack + data pack only when that mod is
 * loaded ({@link #addPacks}). The packs only hold incubation_additions files (plus tag additions), so
 * their position doesn't matter; at the bottom they stay below kubejs/assets, which can override them.
 *
 * Everything here lived in patcherMod (creativeengineering) up to 1.0.5; {@link #remapOldIds} carries
 * nests saved under the old ids over.
 */
@Mod(IncubationAdditions.ID)
public class IncubationAdditions {

    public static final String ID = "incubation_additions";

    /** One content set per bird mod. {@code modId} doubles as the folder name under packs/. */
    private record ContentSet(String modId, String title, Consumer<IEventBus> register, List<Runnable> setup) {
        boolean loaded() {
            return ModList.get().isLoaded(modId);
        }
    }

    private static final List<ContentSet> SETS = List.of(
            new ContentSet("alexsmobs", "Incubation Additions: Alex's Mobs",
                    EmuNests::register, List.of(EmuNests::registerFlammables)),
            new ContentSet("untitledduckmod", "Incubation Additions: Untitled Duck Mod",
                    bus -> {
                        WaterfowlNests.register(bus);
                        WaterfowlEggCrates.register(bus);
                        // Only setting so far is the goose golden egg chance.
                        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, IAConfig.SPEC);
                        // Client only; the lambda keeps client classes out of the server's class loading.
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DuckModEggModels.register(bus));
                    },
                    List.of(WaterfowlNests::registerFlammables, WaterfowlEggCrates::registerFlammables)));

    /** The nests were {@code creativeengineering:<id>} before they moved here (patcherMod up to 1.0.5). */
    private static final String OLD_NAMESPACE = "creativeengineering";

    private static final Map<String, RegistryObject<Block>> OLD_BLOCKS = Map.of(
            "emu_nest", EmuNests.EMU_NEST, "emu_egg_nest", EmuNests.EMU_EGG_NEST,
            "duck_nest", WaterfowlNests.DUCK_NEST, "duck_egg_nest", WaterfowlNests.DUCK_EGG_NEST,
            "goose_nest", WaterfowlNests.GOOSE_NEST, "goose_egg_nest", WaterfowlNests.GOOSE_EGG_NEST);

    private static final Map<String, RegistryObject<Item>> OLD_ITEMS = Map.of(
            "emu_nest", EmuNests.EMU_NEST_ITEM,
            "duck_nest", WaterfowlNests.DUCK_NEST_ITEM,
            "goose_nest", WaterfowlNests.GOOSE_NEST_ITEM);

    public IncubationAdditions() {
        List<ContentSet> active = SETS.stream().filter(ContentSet::loaded).toList();
        if (active.isEmpty()) return;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        active.forEach(set -> set.register().accept(modEventBus));
        modEventBus.addListener((FMLCommonSetupEvent event) ->
                active.forEach(set -> set.setup().forEach(event::enqueueWork)));
        modEventBus.addListener((AddPackFindersEvent event) -> addPacks(event, active));
        MinecraftForge.EVENT_BUS.addListener(IncubationAdditions::remapOldIds);
    }

    /** Adds packs/<mod id>/ of every active set, as a resource pack (client) and a data pack (server). */
    private static void addPacks(AddPackFindersEvent event, List<ContentSet> active) {
        Path root = ModList.get().getModFileById(ID).getFile().findResource("packs");
        for (ContentSet set : active) {
            BuiltInPacks.addFolder(event, root.resolve(set.modId()), ID + ":" + set.modId(), set.title(), Pack.Position.BOTTOM,
                    "Incubation Additions");
        }
    }

    /** Nests placed or held under the old creativeengineering ids load as the incubation_additions ones. */
    private static void remapOldIds(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, OLD_NAMESPACE)) {
            RegistryObject<Block> target = OLD_BLOCKS.get(mapping.getKey().getPath());
            if (target != null && target.isPresent()) mapping.remap(target.get());
        }
        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, OLD_NAMESPACE)) {
            RegistryObject<Item> target = OLD_ITEMS.get(mapping.getKey().getPath());
            if (target != null && target.isPresent()) mapping.remap(target.get());
        }
    }
}
