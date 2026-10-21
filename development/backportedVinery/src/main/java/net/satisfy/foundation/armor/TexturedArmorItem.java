package net.satisfy.foundation.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TexturedArmorItem extends ArmorItem {
    private final ResourceLocation texture;

    public TexturedArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties properties, ResourceLocation texture) {
        super(material, slot, properties);
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    /** Port: Forge's model hook stands in for upstream's armor renderer (custom humanoid models, client only). */
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public @org.jetbrains.annotations.NotNull net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(net.minecraft.world.entity.LivingEntity entity, ItemStack stack, EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> original) {
                net.minecraft.client.model.HumanoidModel<?> model = net.satisfy.foundation.client.armor.ArmorModels.getModel(stack, slot, original);
                return model != null ? model : original;
            }
        });
    }

    /** Port: Forge's texture hook stands in for upstream's armor renderer picking {@link #getTexture()}. */
    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String path = texture.getPath();
        if (!path.startsWith("textures/")) path = "textures/" + path;
        if (!path.endsWith(".png")) path = path + ".png";
        return texture.getNamespace() + ":" + path;
    }
}
