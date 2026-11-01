package net.satisfy.vinery.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.satisfy.vinery.core.Vinery;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ExtraModels {
    public static final int POT_STAGES = 6;
    private static final ResourceLocation[] RED_POT_CONTENT = potContent("red");
    private static final ResourceLocation[] WHITE_POT_CONTENT = potContent("white");

    /** alcohol.md 10.17: the port's own bottle variants, built in memory by forge.upstream.BottleLooks. */
    private static final String BOTTLE_NAMESPACE = "vinery_backport";
    /** A medium wine's look lying in the wine box and (smaller) the 4x rack. */
    public static final String BOX = "wine_box";

    private ExtraModels() {
    }

    public static List<ResourceLocation> all() {
        List<ResourceLocation> models = new ArrayList<>(List.of(RED_POT_CONTENT));
        models.addAll(List.of(WHITE_POT_CONTENT));
        models.addAll(bottleVariants(BOX));
        return models;
    }

    public static ResourceLocation potContent(boolean red, int stage) {
        return (red ? RED_POT_CONTENT : WHITE_POT_CONTENT)[stage - 1];
    }

    private static ResourceLocation[] potContent(String color) {
        ResourceLocation[] models = new ResourceLocation[POT_STAGES];
        for (int stage = 1; stage <= POT_STAGES; stage++) {
            models[stage - 1] = Vinery.identifier("block/" + color + "_grapevine_pot_content_stage" + stage);
        }
        return models;
    }

    /** Every model file in the variant's folder: BottleLooks decides which wines get one, so nothing here names a wine. */
    private static List<ResourceLocation> bottleVariants(String kind) {
        String folder = "models/block/" + kind;
        List<ResourceLocation> models = new ArrayList<>();
        Minecraft.getInstance().getResourceManager()
                .listResources(folder, id -> id.getNamespace().equals(BOTTLE_NAMESPACE) && id.getPath().endsWith(".json"))
                .keySet()
                .forEach(file -> models.add(new ResourceLocation(BOTTLE_NAMESPACE,
                        file.getPath().substring("models/".length(), file.getPath().length() - ".json".length()))));
        return models;
    }

    /** The wine's model of that kind, or null when it has none (it draws its own model). */
    public static @Nullable BakedModel bottleVariant(String kind, Block wine) {
        ResourceLocation id = new ResourceLocation(BOTTLE_NAMESPACE, "block/" + kind + "/" + Registry.BLOCK.getKey(wine).getPath());
        BakedModel model = get(id);
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? null : model;
    }

    public static BakedModel get(ResourceLocation id) {
        return net.minecraft.client.Minecraft.getInstance().getModelManager().getModel(id);
    }
}
