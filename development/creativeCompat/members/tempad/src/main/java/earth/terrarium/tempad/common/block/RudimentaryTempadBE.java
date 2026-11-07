// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/RudimentaryTempadBE.kt (the timedoor
// projector's block entity), with the timedoor_projector entries of Tempad.kt's RegisterCapabilitiesEvent block (chronons:
// BlockChrononContent.projector; upgrades: RudimentaryUpgradeHandler). Translated to Java for Forge 1.19.2:
// - the owner attachment (AttachmentHolder.owner, GameProfile) -> a field saved under "owner" {id, name} in the block
//   entity's NBT only when set (WorkstationBE pattern); the update tag carries only upstream's own keys.
// - NeoForge block capabilities registered from Tempad.kt -> getCapability here, a fresh handler per query.
// - ServerLevel.entities.get(id) -> ServerLevel.getEntity(id); the timedoor id is not saved (as upstream).
package earth.terrarium.tempad.common.block;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.sizing.DynamicAngledPlacement;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.api.tva_device.impl.BlockChrononContent;
import earth.terrarium.tempad.api.tva_device.impl.RudimentaryUpgradeHandler;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RudimentaryTempadBE extends BlockEntity {
    public static final String CHRONONS = "Chronons";
    public static final String PORTAL_TARGET = "PortalTarget";
    /** Port key for upstream's {@code tempad:owner} block entity attachment. */
    public static final String OWNER = "owner";

    @Nullable
    public UUID timedoorId = null;
    public int chrononContent = 0;
    @Nullable
    public LocationGetter portalTarget = null;
    /** Upstream {@code BlockEntity.owner} (attachment, optional). */
    @Nullable
    public GameProfile owner = null;

    public RudimentaryTempadBE(BlockPos pos, BlockState state) {
        super(ModBlocks.timedoorProjectorBE.get(), pos, state);
    }

    /** Upstream saveAdditional (without the attachments). */
    private void writeOwnData(CompoundTag tag) {
        tag.putInt(CHRONONS, chrononContent);
        if (portalTarget != null) tag.put(PORTAL_TARGET, portalTarget.save());
        else tag.remove(PORTAL_TARGET);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeOwnData(tag);
        if (owner != null) tag.put(OWNER, CodecUtils.saveGameProfile(owner));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        chrononContent = tag.getInt(CHRONONS);
        portalTarget = tag.contains(PORTAL_TARGET, Tag.TAG_COMPOUND) ? LocationGetter.load(tag.getCompound(PORTAL_TARGET)) : null;
        // Attachments are read only when present, keeping the current value otherwise (update packets).
        if (tag.contains(OWNER, Tag.TAG_COMPOUND)) owner = CodecUtils.loadGameProfile(tag.getCompound(OWNER));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        writeOwnData(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void openTimedoor() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        List<ServerPlayer> nearby = serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(getBlockPos()).inflate(5.0));
        if (timedoorId != null && serverLevel.getEntity(timedoorId) != null) {
            error(nearby, Component.translatable("tempad.error.timedoor_already_open").withStyle(orange()));
            return;
        }
        LocationGetter pos = portalTarget;
        UpgradeHandler upgrades = UpgradeHandler.of(this);
        ChrononHandler chronons = ChrononHandler.of(this);
        GameProfile player = owner;
        if (pos == null || upgrades == null || chronons == null || player == null) return;
        NamedGlobalVec3 location = pos.get(upgrades, chronons);
        if (location == null) return;
        ResourceLocation provider = pos instanceof IndirectLocation indirect ? indirect.provider() : null;
        UUID id = pos instanceof IndirectLocation indirect ? indirect.id() : null;
        Component msg = TimedoorEntity.openTimedoor(player, this, provider, id, location, new DynamicAngledPlacement(), door -> {
            this.timedoorId = door.getUUID();
            door.setYRot(door.getYRot() + 180);
            door.setGlitching(true);
        });
        if (msg != null) error(nearby, msg);
    }

    private static void error(List<ServerPlayer> players, Component msg) {
        players.forEach(it -> it.displayClientMessage(msg, true));
    }

    static Style orange() {
        return Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ChrononHandler.CAPABILITY) {
            ChrononHandler handler = BlockChrononContent.projector(this, CommonConfigCache.RudimentaryTempad.capacity());
            if (handler == null) return LazyOptional.empty();
            return LazyOptional.of(() -> handler).cast();
        }
        if (cap == UpgradeHandler.CAPABILITY) {
            return LazyOptional.of(() -> RudimentaryUpgradeHandler.INSTANCE).cast();
        }
        return super.getCapability(cap, side);
    }
}
