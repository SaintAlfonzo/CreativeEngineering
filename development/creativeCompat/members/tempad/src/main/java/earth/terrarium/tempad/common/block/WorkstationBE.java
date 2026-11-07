// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/WorkstationBE.kt, with the workstation entries of
// Tempad.kt's RegisterCapabilitiesEvent block (chronons: WorkstationChrononHandler while the docked stack has chronons;
// upgrades: the docked stack's handler). Translated to Java for Forge 1.19.2:
// - NeoForge ItemStackHandler -> Forge ItemStackHandler (same "Items" key).
// - the owner attachment (AttachmentHolder.owner, GameProfile) -> a field saved under "owner" {id, name} in the block
//   entity's own NBT (written only when set, as an absent attachment).
// - NeoForge block capabilities registered from Tempad.kt -> getCapability here, a fresh handler per query as upstream's
//   providers build one per lookup.
// - ServerLevel.entities.get(id) -> ServerLevel.getEntity(id).
package earth.terrarium.tempad.common.block;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Vector3f;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.sizing.FloorPlacementSettings;
import earth.terrarium.tempad.api.sizing.TimedoorPlacementSettings;
import earth.terrarium.tempad.api.sizing.VerticalPlacementSettings;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.api.tva_device.impl.WorkstationChrononHandler;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.data.PortalPlacementComponent;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WorkstationBE extends BlockEntity {
    public static final String DOWNLOAD_TIME = "DownloadTime";
    public static final String MAX_DOWNLOAD_TIME = "MaxDownloadTime";
    public static final String RECIPE = "Recipe";
    public static final String INVENTORY = "Items";
    public static final String TIMEDOOR = "TimedoorId";
    public static final String LEFT = "Left";
    public static final String RIGHT = "Right";
    /** Port key for upstream's {@code tempad:owner} block entity attachment. */
    public static final String OWNER = "owner";

    public final ItemStackHandler inventory = new ItemStackHandler(1);
    public int downloadTime = 0;
    public int maxDownloadTime = 0;
    @Nullable
    public ResourceLocation recipe = null;
    @Nullable
    public UUID timedoorId = null;
    public boolean activeLeft = false;
    public boolean activeRight = false;
    public int age = 0;
    /** Upstream {@code BlockEntity.owner} (attachment, optional). */
    @Nullable
    public GameProfile owner = null;

    public WorkstationBE(BlockPos pos, BlockState state) {
        super(ModBlocks.workstationBE.get(), pos, state);
    }

    public boolean isActive() {
        return activeLeft || activeRight;
    }

    public Direction getDirection() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY, inventory.serializeNBT());
        tag.putInt(DOWNLOAD_TIME, downloadTime);
        tag.putInt(MAX_DOWNLOAD_TIME, maxDownloadTime);
        tag.putBoolean(LEFT, activeLeft);
        tag.putBoolean(RIGHT, activeRight);
        if (recipe != null) tag.putString(RECIPE, recipe.toString());
        if (timedoorId != null) tag.putString(TIMEDOOR, timedoorId.toString());
        if (owner != null) tag.put(OWNER, CodecUtils.saveGameProfile(owner));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound(INVENTORY));
        downloadTime = tag.getInt(DOWNLOAD_TIME);
        maxDownloadTime = tag.getInt(MAX_DOWNLOAD_TIME);
        activeLeft = tag.getBoolean(LEFT);
        activeRight = tag.getBoolean(RIGHT);
        recipe = ResourceLocation.tryParse(tag.getString(RECIPE));
        String door = tag.getString(TIMEDOOR);
        timedoorId = door.isEmpty() ? null : UUID.fromString(door);
        // Upstream deserialises attachments only when present, keeping the current value otherwise (update packets).
        if (tag.contains(OWNER, Tag.TAG_COMPOUND)) owner = CodecUtils.loadGameProfile(tag.getCompound(OWNER));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put(INVENTORY, inventory.serializeNBT());
        tag.putInt(DOWNLOAD_TIME, downloadTime);
        tag.putInt(MAX_DOWNLOAD_TIME, maxDownloadTime);
        return tag;
    }

    public void activateLeft() {
        activeLeft = true;
    }

    public void deactivateLeft() {
        activeLeft = false;
    }

    public void activateRight() {
        activeRight = true;
    }

    public void deactivateRight() {
        activeRight = false;
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        age++;
        BlockPos blockPos = getBlockPos();
        if (maxDownloadTime > 0) {
            downloadTime--;
            if (downloadTime == 0) {
                maxDownloadTime = 0;
                Block.popResource(serverLevel, blockPos, new ItemStack(Items.DRIED_KELP, serverLevel.random.nextInt(3)));
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getX() + 0.5, blockPos.getY() + 0.25, blockPos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
                UpgradeHandler upgrades = UpgradeHandler.of(inventory.getStackInSlot(0));
                if (upgrades != null && recipe != null) {
                    upgrades.install(recipe);
                    setChanged();
                }
                serverLevel.setBlock(blockPos, getBlockState().setValue(WorkstationBlock.HAS_TAPE, false), Block.UPDATE_ALL);
            } else {
                setChanged();
                serverLevel.sendBlockUpdated(blockPos, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                serverLevel.sendParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.25, blockPos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.0);
            }
        }
        if (isActive()) {
            TimedoorEntity timedoor = getTimedoor();
            if (timedoor == null && age > 10) {
                openTimedoor();
            } else if (timedoor != null && age % 20 == 0) {
                ChrononHandler chronons = ChrononHandler.of(this);
                if (chronons != null) {
                    chronons.drainAndExecute(CommonConfig.TimeDoor.costToPersist(), () -> {
                        timedoor.setClosingTime(timedoor.getClosingTime() + 20);
                        TimedoorEntity linked = timedoor.getLinkedPortalEntity();
                        if (linked != null) linked.setClosingTime(linked.getClosingTime() + 20);
                    });
                }
            }
        }
    }

    public void openTimedoor() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        List<ServerPlayer> nearby = serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(getBlockPos()).inflate(5.0));
        IndirectLocation pos = ModComponents.getSelectedPos(inventory.getStackInSlot(0));
        UpgradeHandler upgrades = UpgradeHandler.of(this);
        ChrononHandler chronons = ChrononHandler.of(this);
        GameProfile player = owner;
        if (pos == null || upgrades == null || chronons == null || player == null) return;
        NamedGlobalVec3 location = pos.get(upgrades, chronons);
        if (location == null) return;
        Component msg = TimedoorEntity.openTimedoor(player, this, pos.provider(), pos.id(), location, getSizing(), door -> {
            this.timedoorId = door.getUUID();
            door.setYRot(ModComponents.getPortalOffset(inventory.getStackInSlot(0)).angle() + getDirection().toYRot() + 180);
        });
        if (msg != null) error(nearby, msg);
    }

    @Nullable
    public TimedoorEntity getTimedoor() {
        if (timedoorId == null || !(level instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getEntity(timedoorId) instanceof TimedoorEntity door ? door : null;
    }

    public TimedoorPlacementSettings getSizing() {
        PortalPlacementComponent offset = ModComponents.getPortalOffset(inventory.getStackInSlot(0));
        Vector3f vec = offset.calcOffset(getDirection());
        if (offset.isUpright()) {
            return new VerticalPlacementSettings(vec.x(), vec.y(), vec.z());
        } else {
            return new FloorPlacementSettings(vec.x(), vec.y(), vec.z());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static void error(List<ServerPlayer> players, Component msg) {
        players.forEach(it -> it.displayClientMessage(msg, true));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ChrononHandler.CAPABILITY) {
            if (ChrononHandler.of(inventory.getStackInSlot(0)) == null) return LazyOptional.empty();
            ChrononHandler handler = new WorkstationChrononHandler(this);
            return LazyOptional.of(() -> handler).cast();
        }
        if (cap == UpgradeHandler.CAPABILITY) {
            UpgradeHandler handler = UpgradeHandler.of(inventory.getStackInSlot(0));
            if (handler == null) return LazyOptional.empty();
            return LazyOptional.of(() -> handler).cast();
        }
        return super.getCapability(cap, side);
    }
}
