package com.SaintAlfonzo.incubation_additions.client;

import com.SaintAlfonzo.incubation_additions.IncubationAdditions;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.Map;

/**
 * Client only. Gives Untitled Duck Mod's duck and goose egg items our sprites by swapping their baked
 * inventory models for incubation_additions:item/duck_egg / goose_egg (packs/untitledduckmod/assets).
 *
 * Swapping the baked model instead of shipping assets/untitledduckmod/... files keeps our pack free of
 * other mods' paths: inside Forge's mod_resources the duck mod's own jar would win the path, and moving
 * our pack above everything would also put it above kubejs/assets, which then couldn't override it.
 * The sprites stay ordinary incubation_additions textures, so a same-path PNG in
 * kubejs/assets/incubation_additions/ still replaces them.
 */
public final class DuckModEggModels {

    private static final List<String> EGGS = List.of("duck_egg", "goose_egg");

    private DuckModEggModels() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DuckModEggModels::registerModels);
        modEventBus.addListener(DuckModEggModels::swapModels);
    }

    private static ResourceLocation ourModel(String egg) {
        return new ResourceLocation(IncubationAdditions.ID, "item/" + egg);
    }

    private static void registerModels(ModelEvent.RegisterAdditional event) {
        EGGS.forEach(egg -> event.register(ourModel(egg)));
    }

    private static void swapModels(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (String egg : EGGS) {
            BakedModel ours = models.get(ourModel(egg));
            if (ours != null) models.put(new ModelResourceLocation("untitledduckmod", egg, "inventory"), ours);
        }
    }
}
