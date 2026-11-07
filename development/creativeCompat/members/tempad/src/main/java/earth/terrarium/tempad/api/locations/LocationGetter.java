// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/locations/LocationGetter.kt. Translated to Java for Forge 1.19.2:
// Codec.either(Direct, Indirect) -> NBT (Direct if the tag has "pos", else Indirect); ByteCodec.either -> a leading
// boolean (true = Direct). Used by the location card and projector (phase 2); the classes are API, kept in phase 1.
package earth.terrarium.tempad.api.locations;

import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public sealed interface LocationGetter permits DirectLocation, IndirectLocation {
    @Nullable
    NamedGlobalVec3 get(@Nullable UpgradeHandler upgrades, @Nullable ChrononHandler energy);

    /** Upstream TooltipProvider.addToTooltip; the client passes Screen.hasShiftDown() (1.19.2 TooltipFlag has no shift). */
    void addToTooltip(Consumer<Component> tooltipAdder, boolean shiftDown);

    CompoundTag save();

    static LocationGetter load(CompoundTag tag) {
        if (tag.contains("pos")) return DirectLocation.load(tag);
        return IndirectLocation.load(tag);
    }

    static void write(FriendlyByteBuf buf, LocationGetter getter) {
        if (getter instanceof DirectLocation direct) {
            buf.writeBoolean(true);
            direct.location().write(buf);
        } else {
            buf.writeBoolean(false);
            ((IndirectLocation) getter).write(buf);
        }
    }

    static LocationGetter read(FriendlyByteBuf buf) {
        return buf.readBoolean() ? new DirectLocation(NamedGlobalVec3.read(buf)) : IndirectLocation.read(buf);
    }
}
