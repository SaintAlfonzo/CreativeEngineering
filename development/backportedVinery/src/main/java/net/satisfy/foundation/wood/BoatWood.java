package net.satisfy.foundation.wood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record BoatWood(ResourceLocation id, Supplier<? extends Item> boat, Supplier<? extends Item> chestBoat) {
    private static final Map<ResourceLocation, BoatWood> WOODS = new LinkedHashMap<>();

    public static BoatWood register(ResourceLocation id, Supplier<? extends Item> boat, Supplier<? extends Item> chestBoat) {
        BoatWood wood = new BoatWood(id, boat, chestBoat);
        WOODS.put(id, wood);
        return wood;
    }

    public static @Nullable BoatWood get(ResourceLocation id) {
        return WOODS.get(id);
    }

    public static List<BoatWood> all() {
        return Collections.unmodifiableList(new ArrayList<>(WOODS.values()));
    }

    public static List<BoatWood> byNamespace(String namespace) {
        return WOODS.values().stream().filter(wood -> wood.id.getNamespace().equals(namespace)).toList();
    }

    public Item item(boolean chest) {
        return chest ? chestBoat.get() : boat.get();
    }

    public ResourceLocation texture(boolean chest) {
        return new ResourceLocation(id.getNamespace(), "textures/entity/" + (chest ? "chest_boat/" : "boat/") + id.getPath() + ".png");
    }

    public ResourceLocation modelName(boolean chest) {
        return new ResourceLocation(id.getNamespace(), (chest ? "chest_boat/" : "boat/") + id.getPath());
    }
}
