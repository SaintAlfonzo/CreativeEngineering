package net.satisfy.foundation.client.wood;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.satisfy.foundation.wood.BoatWood;
import net.satisfy.foundation.wood.WoodBoatEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** Port: vanilla 1.19.2's boat renderer, with Forge's per-boat model hook picking the boat's wood. */
public class WoodBoatRenderer extends BoatRenderer {
    private final EntityRendererProvider.Context context;
    private final boolean chest;
    private final Map<BoatWood, Pair<ResourceLocation, BoatModel>> models = new HashMap<>();

    public WoodBoatRenderer(EntityRendererProvider.Context context, boolean chest) {
        super(context, chest);
        this.context = context;
        this.chest = chest;
    }

    public static ModelLayerLocation layer(BoatWood wood, boolean chest) {
        return new ModelLayerLocation(wood.modelName(chest), "main");
    }

    @Override
    public @NotNull Pair<ResourceLocation, BoatModel> getModelWithLocation(@NotNull Boat boat) {
        if (!(boat instanceof WoodBoatEntity woodBoat)) {
            return super.getModelWithLocation(boat);
        }
        return models.computeIfAbsent(woodBoat.getWood(), wood -> Pair.of(wood.texture(chest), new BoatModel(context.bakeLayer(layer(wood, chest)), chest)));
    }
}
