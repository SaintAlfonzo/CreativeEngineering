// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/SyncPortalSettingsPacket.kt. Translated to Java
// for Forge 1.19.2 (SimpleChannel message; nullable fields as optionals on the wire, as nullableFieldOf; Java 21's
// Math.clamp -> Mth.clamp).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.context.WorkstationContext;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.common.data.PortalPlacementComponent;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record SyncPortalSettingsPacket(float xOffset, float yOffset, float zOffset, int angle, boolean isVertical,
                                       @Nullable ResourceLocation providerId, @Nullable UUID id, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(xOffset);
        buf.writeFloat(yOffset);
        buf.writeFloat(zOffset);
        buf.writeInt(angle);
        buf.writeBoolean(isVertical);
        buf.writeOptional(Optional.ofNullable(providerId), FriendlyByteBuf::writeResourceLocation);
        buf.writeOptional(Optional.ofNullable(id), FriendlyByteBuf::writeUUID);
        ctx.encode(buf);
    }

    public static SyncPortalSettingsPacket decode(FriendlyByteBuf buf) {
        return new SyncPortalSettingsPacket(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean(),
                buf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null), buf.readOptional(FriendlyByteBuf::readUUID).orElse(null),
                ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> target = ctx.getCtx(player);
        target.modify(stack -> {
            ModComponents.setPortalOffset(stack, new PortalPlacementComponent(
                    Mth.clamp(xOffset, -5f, 5f),
                    Mth.clamp(yOffset, -5f, 5f),
                    Mth.clamp(zOffset, -5f, 5f),
                    angle,
                    isVertical));
            if (providerId != null && id != null) {
                if (!(ctx.getCtx(player) instanceof WorkstationContext workstationCtx)) return;
                ModComponents.setSelectedPos(stack, new IndirectLocation(Objects.requireNonNull(Objects.requireNonNull(workstationCtx.getWorkstation()).owner),
                        Component.empty(), providerId, id));
            }
        });
    }
}
