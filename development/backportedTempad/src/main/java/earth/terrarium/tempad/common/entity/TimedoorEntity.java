// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/entity/TimedoorEntity.kt.
// Translated to Java for Forge 1.19.2. 1.21 -> 1.19.2: DimensionTransition -> forge/Teleports.changeDimension (an
// ITeleporter); teleportTo(level, ..., RelativeMovement.ALL, ...) -> forge/Teleports.teleportSameLevel; onAddedToLevel
// -> onAddedToWorld; Holder.direct(sound) packets -> the SoundEvent overloads; NeoForge event bus -> Forge's; the
// age_until_allowed_through_timedoor attachment (no serializer upstream, so never saved or copied) -> a weak in-memory
// map with the same lifetime.
package earth.terrarium.tempad.common.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.event.TimedoorEvent;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.sizing.DoorType;
import earth.terrarium.tempad.api.sizing.DynamicAngledPlacement;
import earth.terrarium.tempad.api.sizing.FloorPlacementSettings;
import earth.terrarium.tempad.api.sizing.TimedoorPlacementSettings;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.network.s2c.RotatePlayerMomentumPacket;
import earth.terrarium.tempad.common.registries.ModEntities;
import earth.terrarium.tempad.common.registries.ModNetworking;
import earth.terrarium.tempad.common.registries.ModSounds;
import earth.terrarium.tempad.common.registries.ModTags;
import earth.terrarium.tempad.common.utils.CodecUtils;
import earth.terrarium.tempad.common.utils.Color;
import earth.terrarium.tempad.forge.Teleports;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import com.mojang.math.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class TimedoorEntity extends Entity {
    public static final int IDLE_BEFORE_START = 26;
    public static final int ANIMATION_LENGTH = 5;

    private static final EntityDataAccessor<Integer> closingTimeAccessor = SynchedEntityData.defineId(TimedoorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Color> colorAccessor = SynchedEntityData.defineId(TimedoorEntity.class, ModEntities.COLOR_SERIALIZER);
    private static final EntityDataAccessor<Vec3> targetPosAccessor = SynchedEntityData.defineId(TimedoorEntity.class, ModEntities.VEC3_SERIALIZER);
    private static final EntityDataAccessor<ResourceKey<Level>> targetDimAccessor = SynchedEntityData.defineId(TimedoorEntity.class, ModEntities.DIMENSION_KEY_SERIALIZER);
    private static final EntityDataAccessor<TimedoorPlacementSettings> sizingAccessor = SynchedEntityData.defineId(TimedoorEntity.class, ModEntities.SIZING_SERIALIZER);
    private static final EntityDataAccessor<Boolean> glitchingAccessor = SynchedEntityData.defineId(TimedoorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> offsetAccessor = SynchedEntityData.defineId(TimedoorEntity.class, EntityDataSerializers.INT);

    // feedback
    public static final Component fail = Component.translatable("entity.tempad.timedoor.fail");
    public static final Component posFail = Component.translatable("entity.tempad.timedoor.fail.pos");
    public static final Component interDimFail = Component.translatable("entity.tempad.timedoor.fail.interdimensional");
    public static final Component intraDimAllFail = Component.translatable("entity.tempad.timedoor.fail.intradimensional_all");
    // Fix (TODO gameplay/tnt-and-portal #9 item 2): upstream uses the interdimensional key for intraDimFail and the
    // missing key "interdimensional_all" for intraDimAllFail.
    public static final Component intraDimFail = Component.translatable("entity.tempad.timedoor.fail.intradimensional");
    public static final Component enteringFail = Component.translatable("entity.tempad.timedoor.fail.entering");
    public static final Component leavingFail = Component.translatable("entity.tempad.timedoor.fail.leaving");
    public static final Component noChrononsFail = Component.translatable("entity.tempad.timedoor.fail.no_chronons");

    /** Upstream attachment age_until_allowed_through_timedoor (server thread only; never saved, as upstream). */
    private static final Map<Entity, Integer> AGE_UNTIL_ALLOWED = new WeakHashMap<>();

    public int beganClosing = 0;
    public float targetAngle = 0f;
    @Nullable
    public UUID owner = null;
    @Nullable
    public UUID linkedPortalId = null;
    public boolean original = true;

    public TimedoorEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    // ---------------------------------------------------------------- opening

    @Nullable
    public static Component openTimedoor(Player player, SyncableContext<?> ctx, @Nullable ResourceLocation provider,
                                         @Nullable UUID locationId, NamedGlobalVec3 location) {
        return openTimedoor(player, ctx, provider, locationId, location, door -> { });
    }

    @Nullable
    public static Component openTimedoor(Player player, SyncableContext<?> ctx, @Nullable ResourceLocation provider,
                                         @Nullable UUID locationId, NamedGlobalVec3 location, Consumer<TimedoorEntity> onOpen) {
        var stack = ctx.getStack();
        int cost = CommonConfig.TimeDoor.costPerDoor();
        ChrononHandler chronons = ChrononHandler.of(stack);
        if (!player.isCreative() && (chronons == null ? 0 : chronons.extract(cost, ActionType.Simulate)) < cost) return noChrononsFail;
        Either<TimedoorEntity, Component> result = getTimedoor(player.level, location, false);
        if (result.right().isPresent()) return result.right().get();
        TimedoorEntity timedoor = result.left().orElse(null);
        if (timedoor == null) return fail;
        timedoor.owner = player.getUUID();
        timedoor.setSizing(player.getXRot() > 45 ? new FloorPlacementSettings() : new DynamicAngledPlacement());
        timedoor.getSizing().placeTimedoor(DoorType.ENTRY, player.position(), player.getYRot(), timedoor);
        TimedoorEvent.OpenWithItem event = new TimedoorEvent.OpenWithItem(timedoor, player.getGameProfile(), ctx, provider, locationId);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return event.errorMessage != null ? event.errorMessage : fail;
        else logTimedoorOpen(player.getName().getString(), location, timedoor);
        if (!player.isCreative()) {
            ChrononHandler handler = ChrononHandler.of(stack);
            if (handler != null) handler.extract(cost, ActionType.Execute);
            player.getCooldowns().addCooldown(stack.getItem(), 40);
        }
        player.level.addFreshEntity(timedoor);
        onOpen.accept(timedoor);
        timedoor.tryInitReceivingPortal();
        return null;
    }

    @Nullable
    public static Component openTimedoor(GameProfile player, BlockEntity block, @Nullable ResourceLocation provider,
                                         @Nullable UUID locationId, NamedGlobalVec3 location) {
        return openTimedoor(player, block, provider, locationId, location, new DynamicAngledPlacement(), door -> { });
    }

    @Nullable
    public static Component openTimedoor(GameProfile player, BlockEntity block, @Nullable ResourceLocation provider,
                                         @Nullable UUID locationId, NamedGlobalVec3 location, TimedoorPlacementSettings sizing,
                                         Consumer<TimedoorEntity> onOpen) {
        int cost = CommonConfig.TimeDoor.costPerDoor();
        ChrononHandler chronons = ChrononHandler.of(block);
        if ((chronons == null ? 0 : chronons.extract(cost, ActionType.Simulate)) < cost) return noChrononsFail;
        Level level = block.getLevel();
        if (level == null) return fail;
        Either<TimedoorEntity, Component> result = getTimedoor(level, location, false);
        if (result.right().isPresent()) return result.right().get();
        TimedoorEntity timedoor = result.left().orElse(null);
        if (timedoor == null) return fail;
        timedoor.owner = player.getId();
        timedoor.setSizing(sizing);
        timedoor.getSizing().placeTimedoor(DoorType.ENTRY, Vec3.atCenterOf(block.getBlockPos()).add(0.0, -1.5, 0.0),
                block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180, timedoor);
        TimedoorEvent.OpenWithBlock event = new TimedoorEvent.OpenWithBlock(timedoor, player, block, provider, locationId);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return event.errorMessage != null ? event.errorMessage : fail;
        else logTimedoorOpen(player.getName(), location, timedoor);
        ChrononHandler handler = ChrononHandler.of(block);
        if (handler != null) handler.extract(cost, ActionType.Execute);
        block.setChanged();
        level.sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
        level.addFreshEntity(timedoor);
        onOpen.accept(timedoor);
        timedoor.tryInitReceivingPortal();
        return null;
    }

    /**
     * Fix (TODO gameplay/tnt-and-portal #9 item 3): the Vec3.yRot angle, in radians, that turns velocity going into this
     * door into velocity along the exit facing. Entry doors face the one who walks in (yaw + 180), so travel into the
     * door points along getYRot() + 180 and must come out along targetAngle. Upstream passes getYRot() - targetAngle in
     * degrees to the radian yRot and zeroes the player's velocity on the client.
     */
    public float momentumRotation() {
        return Mth.wrapDegrees(this.getYRot() + 180.0f - targetAngle) * Mth.DEG_TO_RAD;
    }

    public static Either<TimedoorEntity, Component> getTimedoor(Level level, NamedGlobalVec3 location, boolean ignoreRestrictions) {
        if (level.getServer() == null) return Either.right(posFail);
        ServerLevel target = level.getServer().getLevel(location.dimension());
        if (target == null) return Either.right(posFail);
        Holder<DimensionType> targetHolder = target.dimensionTypeRegistration();
        Holder<DimensionType> sourceHolder = level.dimensionTypeRegistration();
        if (!ignoreRestrictions) {
            if (level.dimension() != location.dimension()) {
                if (!CommonConfig.allowInterdimensionalTravel()) return Either.right(interDimFail);
                if (sourceHolder.is(ModTags.leavingNotSupported)) return Either.right(leavingFail);
                if (targetHolder.is(ModTags.enteringNotSupported)) return Either.right(enteringFail);
            } else {
                if (!CommonConfig.allowIntradimensionalTravel()) return Either.right(intraDimAllFail);
                if (sourceHolder.is(ModTags.intradimensionalTravelNotSupported)) return Either.right(intraDimFail);
            }
        }
        TimedoorEntity timedoor = new TimedoorEntity(ModEntities.timedoor.get(), level);
        timedoor.setLocation(location);
        return Either.left(timedoor);
    }

    private static void logTimedoorOpen(String player, NamedGlobalVec3 location, TimedoorEntity timedoor) {
        if (CommonConfig.TimeDoor.logWhenOpen()) {
            Tempad.LOGGER.debug("Player {} opened a timedoor at {} in dimension {} to {} in dimension {}",
                    player, timedoor.blockPosition(), timedoor.level.dimension(), location.name(), timedoor.getTargetDimension());
        }
    }

    // ---------------------------------------------------------------- state

    public Vec3 getTargetPos() {
        return entityData.get(targetPosAccessor);
    }

    public void setTargetPos(Vec3 pos) {
        entityData.set(targetPosAccessor, pos);
    }

    public ResourceKey<Level> getTargetDimension() {
        return entityData.get(targetDimAccessor);
    }

    public void setTargetDimension(ResourceKey<Level> dimension) {
        entityData.set(targetDimAccessor, dimension);
    }

    public Color getColor() {
        return entityData.get(colorAccessor);
    }

    public void setColor(Color color) {
        entityData.set(colorAccessor, color);
    }

    public int getClosingTime() {
        return entityData.get(closingTimeAccessor);
    }

    public void setClosingTime(int closingTime) {
        entityData.set(closingTimeAccessor, closingTime);
    }

    public boolean isGlitching() {
        return entityData.get(glitchingAccessor);
    }

    public void setGlitching(boolean glitching) {
        entityData.set(glitchingAccessor, glitching);
    }

    public int getAnimationOffset() {
        return entityData.get(offsetAccessor);
    }

    public void setAnimationOffset(int offset) {
        entityData.set(offsetAccessor, offset);
    }

    public TimedoorPlacementSettings getSizing() {
        return entityData.get(sizingAccessor);
    }

    public void setSizing(TimedoorPlacementSettings sizing) {
        entityData.set(sizingAccessor, sizing);
        this.refreshDimensions();
    }

    @Nullable
    public TimedoorEntity getLinkedPortalEntity() {
        ServerLevel targetLevel = getTargetLevel();
        if (linkedPortalId == null || targetLevel == null) return null;
        return targetLevel.getEntity(linkedPortalId) instanceof TimedoorEntity door ? door : null;
    }

    @Nullable
    private ServerLevel getTargetLevel() {
        return level.getServer() == null ? null : level.getServer().getLevel(getTargetDimension());
    }

    private NamedGlobalVec3 getSelfLocation() {
        return new NamedGlobalVec3(getName(), NamedGlobalVec3.offsetLocation(position(), getYRot()), level.dimension(), getYRot(), getColor());
    }

    /** Upstream {@code Entity.ageUntilAllowedThroughTimedoor}. */
    @Nullable
    public static Integer getAgeUntilAllowedThroughTimedoor(Entity entity) {
        return AGE_UNTIL_ALLOWED.get(entity);
    }

    public static void setAgeUntilAllowedThroughTimedoor(Entity entity, @Nullable Integer age) {
        if (age == null) AGE_UNTIL_ALLOWED.remove(entity);
        else AGE_UNTIL_ALLOWED.put(entity, age);
    }

    private boolean canTeleport(Entity entity, Level targetLevel) {
        Integer allowedAt = getAgeUntilAllowedThroughTimedoor(entity);
        return !(entity instanceof TimedoorEntity)
                && getSizing().isInside(this, entity)
                && !entity.getType().is(ModTags.teleportingNotSupport)
                // 1.21 canChangeDimensions(from, to); 1.19.2 has no level arguments
                && entity.canChangeDimensions()
                && !entity.isPassenger()
                && (allowedAt == null || entity.tickCount > allowedAt);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(closingTimeAccessor, CommonConfig.TimeDoor.timeInWorld());
        entityData.define(colorAccessor, Tempad.ORANGE);
        entityData.define(targetPosAccessor, Vec3.ZERO);
        entityData.define(targetDimAccessor, Level.OVERWORLD);
        entityData.define(sizingAccessor, new DynamicAngledPlacement());
        entityData.define(glitchingAccessor, false);
        entityData.define(offsetAccessor, 0);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compound) {
        CompoundTag tag = super.saveWithoutId(compound);
        tag.putInt("Age", tickCount);
        tag.putInt("ClosingTime", getClosingTime());
        tag.putFloat("TargetAngle", targetAngle);
        tag.putBoolean("IsGlitching", isGlitching());
        tag.putBoolean("IsOriginal", original);
        tag.putString("LinkedPortalId", String.valueOf(linkedPortalId));
        tag.put("Color", getColor().save());
        tag.put("TargetPos", CodecUtils.saveVec3(getTargetPos()));
        tag.putString("TargetDimension", getTargetDimension().location().toString());
        tag.put("PlacementSettings", TimedoorPlacementSettings.save(getSizing()));
        return tag;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        tickCount = compound.getInt("Age");
        setClosingTime(compound.getInt("ClosingTime"));
        targetAngle = compound.getFloat("TargetAngle");
        setGlitching(compound.getBoolean("IsGlitching"));
        original = compound.getBoolean("IsOriginal");
        String linked = compound.getString("LinkedPortalId");
        linkedPortalId = null;
        if (!linked.isBlank() && !linked.equals("null")) {
            try {
                linkedPortalId = UUID.fromString(linked);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (compound.contains("Color")) setColor(Color.load(compound.get("Color")));
        if (compound.contains("TargetPos")) setTargetPos(CodecUtils.loadVec3(compound.get("TargetPos")));
        if (compound.contains("TargetDimension")) setTargetDimension(CodecUtils.loadDimension(compound.getString("TargetDimension")));
        if (compound.contains("PlacementSettings")) {
            TimedoorPlacementSettings sizing = TimedoorPlacementSettings.load(compound.getCompound("PlacementSettings"));
            if (sizing != null) setSizing(sizing);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return getSizing().getDimensions();
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            EntityDimensions dimensions = getSizing().getDimensions();
            if (dimensions.width != getBbWidth() || dimensions.height != getBbHeight()) {
                this.refreshDimensions();
                this.setBoundingBox(makeBoundingBox());
            }
            double percent = tickCount / (double) IDLE_BEFORE_START;
            if (tickCount < IDLE_BEFORE_START && random.nextDouble() < percent) {
                percent *= 0.25;
                double y = this.getY() + getBbHeight() / 2.0;
                Color color = getColor();
                level.addParticle(
                        new DustParticleOptions(new Vector3f(color.getIntRed() / 255f, color.getIntGreen() / 255f, color.getIntBlue() / 255f), 1.0f),
                        true,
                        getX() + random.nextDouble() * percent - percent / 2,
                        y + random.nextDouble() * percent - percent / 2,
                        getZ() + random.nextDouble() * percent - percent / 2,
                        0.0, 0.0, 0.0);
            }
            return;
        }
        setClosingTime(getClosingTime() - 1);
        tryClose();
        if (tickCount < IDLE_BEFORE_START + ANIMATION_LENGTH || getClosingTime() < ANIMATION_LENGTH) {
            return;
        }
        ServerLevel targetLevel = getTargetLevel();
        if (targetLevel == null) return;
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, getBoundingBox(), it -> canTeleport(it, targetLevel));
        if (!entities.isEmpty()) {
            tryInitReceivingPortal();
            BlockPos pos = new BlockPos(getTargetPos());
            targetLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 3, pos);
        }
        for (Entity entity : entities) {
            TimedoorEvent.Enter event = new TimedoorEvent.Enter(this, entity);
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSoundEntityPacket(ModSounds.timedoorEnterMono.get(), getSoundSource(), this, 1.0f, 1.0f, 0));
                level.playSound(player, this, ModSounds.timedoorEnterStereo.get(), getSoundSource(), 1.0f, 1.0f);
            }
            Vec3 targetPos = getTargetPos();
            if (entity.level.dimension() == targetLevel.dimension()) {
                float momentumRotation = momentumRotation();
                entity.setDeltaMovement(entity.getDeltaMovement().yRot(momentumRotation));
                Teleports.teleportSameLevel(entity, targetPos.x, targetPos.y, targetPos.z, targetAngle, entity.getXRot());
                entity.hasImpulse = true;
                if (entity instanceof ServerPlayer player) {
                    ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RotatePlayerMomentumPacket(momentumRotation));
                }
                setAgeUntilAllowedThroughTimedoor(entity, entity.tickCount + 30);
            } else {
                Teleports.changeDimension(entity, targetLevel, targetPos, entity.getDeltaMovement(), targetAngle, 0.0f);
                setAgeUntilAllowedThroughTimedoor(entity, entity.tickCount + 60);
            }
            TimedoorEntity linked = getLinkedPortalEntity();
            if (linked != null) MinecraftForge.EVENT_BUS.post(new TimedoorEvent.Exit(linked, entity));
        }
    }

    public void tryInitReceivingPortal() {
        if ((getClosingTime() <= 0 && getClosingTime() != -1) || !original) return;
        ServerLevel targetLevel = getTargetLevel();
        if (targetLevel == null) return;
        if (getLinkedPortalEntity() != null) return;
        TimedoorEntity targetPortal = new TimedoorEntity(ModEntities.timedoor.get(), targetLevel);
        targetPortal.linkedPortalId = this.getUUID();
        targetPortal.setClosingTime(this.getClosingTime());
        targetPortal.setLocation(getSelfLocation());
        targetPortal.setSizing(this.getSizing());
        targetPortal.setGlitching(this.isGlitching());
        targetPortal.original = false;
        getSizing().placeTimedoor(DoorType.EXIT, getTargetPos(), targetAngle + 180f, targetPortal);
        linkedPortalId = targetPortal.getUUID();
        targetLevel.addFreshEntity(targetPortal);
    }

    private void tryClose() {
        if (getClosingTime() <= 0 && getClosingTime() != -1) {
            MinecraftForge.EVENT_BUS.post(new TimedoorEvent.Close(this));
            TimedoorEntity linked = getLinkedPortalEntity();
            if (linked != null) linked.linkedPortalId = null;
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (original && !level.isClientSide) {
            TimedoorEntity linked = getLinkedPortalEntity();
            if (linked != null) linked.remove(reason);
        }
    }

    public void setLocation(NamedGlobalVec3 location) {
        setTargetPos(location.pos());
        setTargetDimension(location.dimension());
        setCustomName(location.name());
        this.targetAngle = location.angle();
        setColor(location.color());
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level.isClientSide) return;
        level.playSound(null, blockPosition(), ModSounds.timedoorOpen.get(), getSoundSource(), 1.0f, 1.0f);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
