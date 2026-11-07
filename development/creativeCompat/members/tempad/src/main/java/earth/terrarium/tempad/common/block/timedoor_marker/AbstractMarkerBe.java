// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/timedoor_marker/AbstractMarkerBe.kt, with
// the block entity attachments of common/registries/ModAttachments.kt the markers use. Translated to Java for Forge 1.19.2:
// - NeoForge attachments -> fields, saved in the block entity's NBT under the attachment's upstream path: owner {id, name},
//   color (Color NBT), anchor_id (UUID string; upstream path "id", renamed because BlockEntity.saveWithId writes the
//   block entity type under "id" after saveAdditional and so overwrote it, TODO gameplay/tnt-and-portal #11), access (id), can_access, name (component JSON), yoffset, angle; owner, id and
//   name only when set (optional attachments), the rest always. load keeps the current value of a missing key, as
//   attachment deserialisation does.
// - the synced color attachment (DataSyncSerializer) -> "color" in the update tag plus a block update on every
//   server-side setColor; the other attachments stay server-only, as upstream (the update tag is "Locked" + color).
// - attachment writes mark the block entity changed (setColor, setPosName).
// - Vec3.bottomCenter / relative(dir, 1.0) (1.21) -> the same arithmetic.
package earth.terrarium.tempad.common.block.timedoor_marker;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.player_access.PlayerAccessApi;
import earth.terrarium.tempad.common.utils.CodecUtils;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractMarkerBe extends BlockEntity implements Nameable {
    public static final String LOCKED = "Locked";
    // Port keys for the upstream attachments (their registry paths).
    public static final String OWNER = "owner";
    public static final String COLOR = "color";
    public static final String ID = "anchor_id";
    /** Key the anchor id had before #11; read only when it holds a UUID (the type id is always there too). */
    public static final String LEGACY_ID = "id";
    public static final String ACCESS = "access";
    public static final String CAN_ACCESS = "can_access";
    public static final String NAME = "name";
    public static final String Y_OFFSET = "yoffset";
    public static final String ANGLE = "angle";

    public boolean locked = true;
    /** Upstream {@code owner} (optional attachment). */
    @Nullable
    public GameProfile owner = null;
    /** Upstream {@code color} (synced attachment, default ORANGE with alpha 0). */
    private Color color = Tempad.ORANGE.withAlpha(0);
    /** Upstream {@code id} (optional attachment): the anchor id in AnchorPointsData. */
    @Nullable
    public UUID id = null;
    /** Upstream {@code accessId} (attachment {@code access}, default PlayerAccessApi.noAccess). */
    public ResourceLocation accessId = PlayerAccessApi.NO_ACCESS;
    /** Upstream {@code canAccess} (attachment {@code can_access}, default false). */
    public boolean canAccess = false;
    /** Upstream attachment {@code name}; null = absent. */
    @Nullable
    private Component name = null;
    /** Upstream {@code yOffset} (attachment {@code yoffset}, default 0). */
    public float yOffset = 0f;
    /** Upstream {@code angle} (attachment, default 0). */
    public int angle = 0;

    protected AbstractMarkerBe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public Component getPosName() {
        return name != null ? name : getBlockState().getBlock().getName();
    }

    public void setPosName(Component value) {
        this.name = value;
        setChanged();
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    /** Upstream extension {@code BlockEntity.facingAngle}. */
    public float getFacingAngle() {
        return getFacing().toYRot();
    }

    /** Upstream {@code Vec3.atBottomCenterOf(blockPos)} (1.21 {@code bottomCenter}). */
    protected Vec3 bottomCenter() {
        BlockPos pos = getBlockPos();
        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public Vec3 getLandingPosition() {
        Direction facing = getFacing();
        Vec3 relative = bottomCenter().add(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        return getBlockState().getValue(BlockStateProperties.UP) ? relative.add(0.0, 0.5, 0.0) : relative.add(0.0, -2.0, 0.0);
    }

    public float getLandingAngle() {
        return getFacingAngle();
    }

    public NamedGlobalVec3 getNamedGlobalVec3() {
        return new NamedGlobalVec3(getPosName(), getLandingPosition(), level.dimension(), getLandingAngle(), color);
    }

    public abstract void openScreen(Player player);

    public abstract boolean canAccess(GameProfile player);

    @Nullable
    private static UUID parseUuid(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_STRING)) return null;
        try {
            return UUID.fromString(tag.getString(key));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(LOCKED, locked);
        if (owner != null) tag.put(OWNER, CodecUtils.saveGameProfile(owner));
        tag.put(COLOR, color.save());
        if (id != null) tag.putString(ID, id.toString());
        tag.putString(ACCESS, accessId.toString());
        tag.putBoolean(CAN_ACCESS, canAccess);
        if (name != null) tag.putString(NAME, CodecUtils.saveComponent(name));
        tag.putFloat(Y_OFFSET, yOffset);
        tag.putInt(ANGLE, angle);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        locked = tag.getBoolean(LOCKED);
        if (tag.contains(OWNER, Tag.TAG_COMPOUND)) owner = CodecUtils.loadGameProfile(tag.getCompound(OWNER));
        if (tag.contains(COLOR)) color = Color.load(tag.get(COLOR));
        UUID loaded = parseUuid(tag, ID);
        if (loaded == null) loaded = parseUuid(tag, LEGACY_ID);
        if (loaded != null) id = loaded;
        if (tag.contains(ACCESS, Tag.TAG_STRING)) {
            ResourceLocation access = ResourceLocation.tryParse(tag.getString(ACCESS));
            if (access != null) accessId = access;
        }
        if (tag.contains(CAN_ACCESS, Tag.TAG_BYTE)) canAccess = tag.getBoolean(CAN_ACCESS);
        if (tag.contains(NAME, Tag.TAG_STRING)) name = CodecUtils.loadComponent(tag.getString(NAME));
        if (tag.contains(Y_OFFSET, Tag.TAG_FLOAT)) yOffset = tag.getFloat(Y_OFFSET);
        if (tag.contains(ANGLE, Tag.TAG_INT)) angle = tag.getInt(ANGLE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LOCKED, locked);
        tag.put(COLOR, color.save());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return name;
    }

    @Override
    public Component getName() {
        return getPosName();
    }
}
