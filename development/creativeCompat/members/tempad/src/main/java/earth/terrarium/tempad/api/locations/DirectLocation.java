// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/LocationGetter.kt (DirectLocation). Translated to Java
// for Forge 1.19.2.
package earth.terrarium.tempad.api.locations;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.function.Consumer;

public record DirectLocation(NamedGlobalVec3 location) implements LocationGetter {
    @Override
    public NamedGlobalVec3 get(UpgradeHandler upgrades, ChrononHandler energy) {
        return location;
    }

    @Override
    public void addToTooltip(Consumer<Component> tooltipAdder, boolean shiftDown) {
        tooltipAdder.accept(MutableComponent.create(location.name().getContents())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF))));
        tooltipAdder.accept(location.dimensionText().withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.literal("X: " + location.x()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipAdder.accept(Component.literal("Y: " + location.y()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipAdder.accept(Component.literal("Z: " + location.z()).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public CompoundTag save() {
        return location.save();
    }

    public static DirectLocation load(CompoundTag tag) {
        return new DirectLocation(NamedGlobalVec3.load(tag));
    }
}
