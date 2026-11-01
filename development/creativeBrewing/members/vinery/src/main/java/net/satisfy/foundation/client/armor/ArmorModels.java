package net.satisfy.foundation.client.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Custom humanoid models for armor pieces. Port: Forge's {@code IClientItemExtensions#getHumanoidArmorModel} asks for
 * the model ({@code TexturedArmorItem}); the texture comes from the item's {@code getArmorTexture}.
 */
public final class ArmorModels {
    private static final Map<Item, Entry> ENTRIES = new HashMap<>();
    private static final Map<ModelLayerLocation, HumanoidModel<?>> BAKED = new HashMap<>();

    private ArmorModels() {
    }

    public static void register(ModelLayerLocation layer, Function<ModelPart, ? extends HumanoidModel<?>> factory, ItemLike... items) {
        Entry entry = new Entry(layer, factory);
        for (ItemLike item : items) {
            ENTRIES.put(item.asItem(), entry);
        }
    }

    public static boolean has(Item item) {
        return ENTRIES.containsKey(item);
    }

    public static boolean isHidden(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("Visible") && !stack.getTag().getBoolean("Visible");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static @Nullable HumanoidModel<?> getModel(ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        Entry entry = ENTRIES.get(stack.getItem());
        if (entry == null || isHidden(stack) || !(stack.getItem() instanceof ArmorItem armor) || armor.getSlot() != slot) {
            return null;
        }
        HumanoidModel<?> model = BAKED.computeIfAbsent(entry.layer, layer -> entry.factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer)));
        ((HumanoidModel) original).copyPropertiesTo((HumanoidModel) model);
        return model;
    }

    /** Models are baked once per layer; a resource reload rebuilds them. */
    public static void clear() {
        BAKED.clear();
    }

    private record Entry(ModelLayerLocation layer, Function<ModelPart, ? extends HumanoidModel<?>> factory) {
    }
}
