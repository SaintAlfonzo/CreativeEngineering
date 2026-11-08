// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/LocationGetter.kt (IndirectLocation). Translated to Java
// for Forge 1.19.2: Codec -> NBT {accessor {id, name}, info (component JSON), provider, id}.
package earth.terrarium.tempad.api.locations;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.utils.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public record IndirectLocation(GameProfile accessor, Component info, ResourceLocation provider, UUID id) implements LocationGetter {
    @Nullable
    @Override
    public NamedGlobalVec3 get(@Nullable UpgradeHandler upgrades, @Nullable ChrononHandler energy) {
        TempadLocations.LocationProvider locationProvider = TempadLocations.get(provider);
        return locationProvider == null ? null : locationProvider.create(accessor, upgrades, energy).get(id);
    }

    @Override
    public void addToTooltip(Consumer<Component> tooltipAdder, boolean shiftDown) {
        tooltipAdder.accept(info);
        tooltipAdder.accept(Component.translatable("item.tempad.location_card.created_by",
                Component.literal(accessor.getName()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        if (!shiftDown) {
            tooltipAdder.accept(Component.translatable("misc.tempad.shift_key_info",
                    Component.translatable("key.keyboard.left.shift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipAdder.accept(Component.translatable("item.tempad.location_card.id", id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("accessor", CodecUtils.saveGameProfile(accessor));
        tag.putString("info", CodecUtils.saveComponent(info));
        tag.putString("provider", provider.toString());
        tag.put("id", NbtUtils.createUUID(id));
        return tag;
    }

    public static IndirectLocation load(CompoundTag tag) {
        ResourceLocation provider = ResourceLocation.tryParse(tag.getString("provider"));
        return new IndirectLocation(CodecUtils.loadGameProfile(tag.getCompound("accessor")), CodecUtils.loadComponent(tag.getString("info")),
                provider == null ? new ResourceLocation("tempad", "default") : provider, CodecUtils.uuidOrNil(tag.get("id")));
    }

    public void write(FriendlyByteBuf buf) {
        CodecUtils.writeGameProfile(buf, accessor);
        buf.writeComponent(info);
        buf.writeResourceLocation(provider);
        buf.writeUUID(id);
    }

    public static IndirectLocation read(FriendlyByteBuf buf) {
        return new IndirectLocation(CodecUtils.readGameProfile(buf), buf.readComponent(), buf.readResourceLocation(), buf.readUUID());
    }
}
