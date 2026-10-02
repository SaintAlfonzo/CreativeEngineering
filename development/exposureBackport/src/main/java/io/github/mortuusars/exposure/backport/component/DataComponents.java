package io.github.mortuusars.exposure.backport.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The vanilla 1.20.5+ data components Exposure uses, mapped onto where 1.19.2 keeps the same data:
 * <ul>
 * <li>{@link #CUSTOM_NAME}: the {@code display.Name} hover name</li>
 * <li>{@link #CUSTOM_DATA}: the stack's root tag. Setting merges keys in (it never clears the root tag, which also
 * holds the other components)</li>
 * <li>{@link #ENTITY_DATA}: {@code EntityTag}, read by {@code EntityType.updateCustomEntityTag}</li>
 * </ul>
 */
public final class DataComponents {
    private DataComponents() {
    }

    public static final DataComponentType<Component> CUSTOM_NAME = new DataComponentType<>(new ResourceLocation("custom_name"), BackportCodecs.COMPONENT) {
        @Override
        public boolean isStored(ItemStack stack) {
            return stack.hasCustomHoverName();
        }

        @Override
        protected @Nullable Component getStored(ItemStack stack) {
            return stack.hasCustomHoverName() ? stack.getHoverName() : null;
        }

        @Override
        protected void setStored(ItemStack stack, Component value) {
            stack.setHoverName(value);
        }

        @Override
        protected void removeStored(ItemStack stack) {
            stack.resetHoverName();
        }
    };

    public static final DataComponentType<CustomData> CUSTOM_DATA = new DataComponentType<>(new ResourceLocation("custom_data"),
            CompoundTag.CODEC.xmap(CustomData::of, CustomData::copyTag)) {
        @Override
        public boolean isStored(ItemStack stack) {
            return stack.getTag() != null && !stack.getTag().isEmpty();
        }

        @Override
        protected @Nullable CustomData getStored(ItemStack stack) {
            return stack.getTag() != null ? CustomData.of(stack.getTag()) : null;
        }

        @Override
        protected void setStored(ItemStack stack, CustomData value) {
            stack.getOrCreateTag().merge(value.copyTag());
        }

        @Override
        protected void removeStored(ItemStack stack) {
            stack.setTag(null);
        }
    };

    public static final DataComponentType<CustomData> ENTITY_DATA = new DataComponentType<>(new ResourceLocation("entity_data"),
            CompoundTag.CODEC.xmap(CustomData::of, CustomData::copyTag)) {
        @Override
        public boolean isStored(ItemStack stack) {
            return stack.getTagElement("EntityTag") != null;
        }

        @Override
        protected @Nullable CustomData getStored(ItemStack stack) {
            CompoundTag entityTag = stack.getTagElement("EntityTag");
            return entityTag != null ? CustomData.of(entityTag) : null;
        }

        @Override
        protected void setStored(ItemStack stack, CustomData value) {
            stack.addTagElement("EntityTag", value.copyTag());
        }

        @Override
        protected void removeStored(ItemStack stack) {
            stack.removeTagKey("EntityTag");
        }
    };
}
