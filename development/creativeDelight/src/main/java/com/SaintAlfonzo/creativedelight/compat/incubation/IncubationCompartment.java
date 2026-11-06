package com.SaintAlfonzo.creativedelight.compat.incubation;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.compat.incubation.client.DuckModEggModels;
import com.SaintAlfonzo.creativedelight.compat.incubation.crate.WaterfowlEggCrates;
import com.SaintAlfonzo.creativedelight.compat.incubation.nest.EmuNests;
import com.SaintAlfonzo.creativedelight.compat.incubation.nest.WaterfowlNests;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Incubation compartment of Creative Delight (feature_writeup/mobs/nests.md; plan_cb_publish.md 3b): the pack's
 * additions to Incubation (Team Abnormals), the incubation_additions member until M8 (2026-11-04). One content set per
 * bird mod, each switched on only when its mod is installed:
 * - Alex's Mobs: emu nest ({@link EmuNests}, mixin.alexsmobs.EntityEmuEggLayerMixin)
 * - Untitled Duck Mod: duck and goose nests ({@link WaterfowlNests}, mixin.untitledduckmod.Duck/GooseEntityEggLayerMixin),
 *   duck and goose egg crates ({@link WaterfowlEggCrates}), new item sprites for the duck mod's eggs
 *   (client/DuckModEggModels), and with Vintage Delight too the goose golden egg (goldenegg.GoldenGooseEgg, chance in
 *   CDConfig [incubation])
 * - either: mixin.incubation.LayEggInNestGoalMixin keeps every bird off nests that have no variant for its egg
 *
 * Load guard: {@link #init} is called by {@link CreativeDelight} only when Incubation is loaded and the
 * {@code [compartments] incubation} switch is on ({@code compat.Compartments}); only this package and the mixins above
 * name Incubation's types. A set's assets, recipes, loot tables, tags and worldgen are the built-in packs
 * {@code packs/incubation/<bird mod>/} ({@code compat.CompartmentPacks}), added only with Incubation and that bird mod.
 * Ids are {@code creativedelight:*} since M8 (plan D6): data/creativedelight/swaps/compartments.json carries the
 * {@code incubation_additions:*} ids and the older {@code creativeengineering:*} ones (patcherMod up to 1.0.5) over.
 */
public final class IncubationCompartment {

    public static final String ID = CreativeDelight.MOD_ID;

    /** One content set per bird mod. */
    private record ContentSet(String modId, Consumer<IEventBus> register, List<Runnable> setup) {
        boolean loaded() {
            return ModList.get().isLoaded(modId);
        }
    }

    private static final List<ContentSet> SETS = List.of(
            new ContentSet("alexsmobs", EmuNests::register, List.of(EmuNests::registerFlammables)),
            new ContentSet("untitledduckmod",
                    bus -> {
                        WaterfowlNests.register(bus);
                        WaterfowlEggCrates.register(bus);
                        // Client only; the lambda keeps client classes out of the server's class loading.
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DuckModEggModels.register(bus));
                    },
                    List.of(WaterfowlNests::registerFlammables, WaterfowlEggCrates::registerFlammables)));

    private IncubationCompartment() {
    }

    public static void init(IEventBus modEventBus) {
        List<ContentSet> active = SETS.stream().filter(ContentSet::loaded).toList();
        if (active.isEmpty()) return;
        active.forEach(set -> set.register().accept(modEventBus));
        modEventBus.addListener((FMLCommonSetupEvent event) ->
                active.forEach(set -> set.setup().forEach(event::enqueueWork)));
    }
}
