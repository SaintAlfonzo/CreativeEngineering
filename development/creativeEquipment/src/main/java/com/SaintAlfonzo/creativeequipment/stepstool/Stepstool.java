package com.SaintAlfonzo.creativeequipment.stepstool;

import com.SaintAlfonzo.creativeequipment.CEQConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Stepstools: an item from {@code #creativeengineering:stepstools} in the Curios feet slot lets a player stand on it,
 * one block higher (feature_writeup/stepstool.md).
 *
 * <ul>
 *   <li>The client asks to step up ({@code stepstool.client.StepstoolClient}: hold the keys while standing still); the server checks
 *       and owns the state. It records the stool where the player stood, tells the player and everyone tracking them
 *       ({@link Sync}), then moves the player up a block.</li>
 *   <li>The stool is a one-block collision box for its owner only ({@code mixin.EntityStepstoolCollisionMixin}), on
 *       both sides, so the player stands on it and the server's movement checks agree. The server's "flying" kick
 *       skips a player on a stool ({@code mixin.ServerGamePacketListenerStepstoolMixin}).</li>
 *   <li>Stepping off: the client on move, crouch or jump; the server when the player drifts off, takes damage, mounts,
 *       sleeps, flies, glides, swims, unequips the stool, a block fills the stool's space, dies, changes dimension or
 *       logs out. The player then falls the one block (no fall damage). Nothing is saved.</li>
 * </ul>
 *
 * Only registered when Curios is loaded; the mixins only read {@link #anchor}, which needs no Curios class.
 */
public final class Stepstool {

    public static final TagKey<Item> TAG = ItemTags.create(new ResourceLocation("creativeengineering", "stepstools"));

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(com.SaintAlfonzo.creativeequipment.CreativeEquipment.MOD_ID, "stepstool"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    /** Where a player's stool stands: its bottom centre (the player's feet when they stepped up) and the stool item. */
    public record Anchor(double x, double y, double z, ItemStack stool) {
        /** The whole block the stool fills: what its owner stands on, and what is drawn. */
        public AABB box() {
            return new AABB(x - 0.5, y, z - 0.5, x + 0.5, y + 1.0, z + 0.5);
        }

        /** The space the player stood in before stepping up: a block here steps them off. */
        AABB footprint() {
            return new AABB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.0, z + 0.3).deflate(1.0E-3);
        }
    }

    // Keyed by entity id; one map per logical side (the integrated server shares the JVM with its client).
    static final Map<Integer, Anchor> SERVER = new ConcurrentHashMap<>();
    public static final Map<Integer, Anchor> CLIENT = new ConcurrentHashMap<>();

    private static final Map<Item, BlockState> STOOL_BLOCKS = new ConcurrentHashMap<>();

    private Stepstool() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, Request.class, Request::encode, Request::decode, Request::handle);
        CHANNEL.registerMessage(1, Sync.class, Sync::encode, Sync::decode, Sync::handle);
    }

    /** The stool this player stands on, on the entity's own side, or null. */
    public static Anchor anchor(Entity entity) {
        if (!(entity instanceof Player)) {
            return null;
        }
        Map<Integer, Anchor> map = entity.level.isClientSide ? CLIENT : SERVER;
        return map.isEmpty() ? null : map.get(entity.getId());
    }

    /** Entity.collide's entity-collision list, plus the stool when {@code entity} stands on one. */
    public static List<VoxelShape> withStool(Entity entity, List<VoxelShape> shapes) {
        Anchor a = anchor(entity);
        if (a == null) {
            return shapes;
        }
        List<VoxelShape> out = new ArrayList<>(shapes.size() + 1);
        out.addAll(shapes);
        out.add(Shapes.create(a.box()));
        return out;
    }

    /**
     * The Another Furniture tall stool a stepstool looks like: {@code <colour>_stepstool} draws
     * {@code another_furniture:<colour>_tall_stool}. Null when there is none.
     */
    public static BlockState stoolBlock(ItemStack stack) {
        BlockState state = STOOL_BLOCKS.computeIfAbsent(stack.getItem(), item -> {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || !id.getPath().endsWith("_stepstool")) {
                return Blocks.AIR.defaultBlockState();
            }
            String colour = id.getPath().substring(0, id.getPath().length() - "_stepstool".length());
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("another_furniture", colour + "_tall_stool"));
            return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
        });
        return state.isAir() ? null : state;
    }

    /** True when nothing stops {@code player} stepping up right now (both sides check; the server's word counts). */
    public static boolean canStepUp(Player player) {
        return CEQConfig.STEPSTOOL.enabled.get()
                && player.isOnGround()
                && player.isAlive()
                && !player.isSpectator()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isFallFlying()
                && !player.getAbilities().flying
                && !player.isSwimming()
                && !player.isShiftKeyDown()
                && !StepstoolCurios.find(player).isEmpty()
                && player.level.noCollision(player, player.getBoundingBox().move(0.0, 1.0, 0.0));
    }

    // -------------------------------------------------------------------------
    // Server
    // -------------------------------------------------------------------------

    private static void stepUp(ServerPlayer player) {
        if (SERVER.containsKey(player.getId()) || !canStepUp(player)) {
            return;
        }
        ItemStack stool = StepstoolCurios.find(player).copy();
        stool.setCount(1);
        Anchor a = new Anchor(player.getX(), player.getY(), player.getZ(), stool);
        SERVER.put(player.getId(), a);
        // The anchor goes out before the move: the client must already stand on the stool when it lands on top.
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), Sync.on(player.getId(), a));
        player.connection.teleport(a.x(), a.y() + 1.0, a.z(), 0.0F, 0.0F,
                EnumSet.of(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT, ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT));
        player.fallDistance = 0.0F;
        BlockState state = stoolBlock(stool);
        if (state != null) {
            SoundType sound = state.getSoundType();
            player.level.playSound(null, a.x(), a.y() + 1.0, a.z(), sound.getPlaceSound(), SoundSource.PLAYERS,
                    (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        }
    }

    private static void stepOff(ServerPlayer player) {
        if (SERVER.remove(player.getId()) != null) {
            CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), Sync.off(player.getId()));
        }
    }

    /** True when the server steps the player off by itself. */
    private static boolean mustStepOff(ServerPlayer player, Anchor a) {
        double dx = player.getX() - a.x();
        double dz = player.getZ() - a.z();
        double drift = CEQConfig.STEPSTOOL.maxDrift.get();
        if (dx * dx + dz * dz > drift * drift) {
            return true;
        }
        double top = a.y() + 1.0;
        if (player.getY() < top - 0.05 || player.getY() > top + 0.6) {
            return true;
        }
        if (!CEQConfig.STEPSTOOL.enabled.get() || !player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.isSleeping() || player.isFallFlying() || player.getAbilities().flying || player.isSwimming()
                || player.isShiftKeyDown()) {
            return true;
        }
        if (!StepstoolCurios.find(player).is(a.stool().getItem())) {
            return true;
        }
        return blocked(player.level, a);
    }

    private static boolean blocked(Level level, Anchor a) {
        if (!level.isLoaded(new BlockPos(a.x(), a.y(), a.z()))) {
            return false;
        }
        for (VoxelShape shape : level.getBlockCollisions(null, a.footprint())) {
            if (!shape.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || SERVER.isEmpty()) {
            return;
        }
        Anchor a = SERVER.get(player.getId());
        if (a != null && mustStepOff(player, a)) {
            stepOff(player);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CEQConfig.STEPSTOOL.stepOffWhenHurt.get()) {
            stepOff(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stepOff(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stepOff(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stepOff(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stepOff(player);
        }
    }

    /** A player coming into view who already stands on a stool. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer tracker && event.getTarget() instanceof ServerPlayer target) {
            Anchor a = SERVER.get(target.getId());
            if (a != null) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> tracker), Sync.on(target.getId(), a));
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SERVER.clear();
    }

    // -------------------------------------------------------------------------
    // Packets
    // -------------------------------------------------------------------------

    /** Client to server: step up ({@code up}) or step off. */
    public record Request(boolean up) {

        void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(up);
        }

        static Request decode(FriendlyByteBuf buf) {
            return new Request(buf.readBoolean());
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ctx.get().enqueueWork(() -> {
                    if (up) {
                        stepUp(player);
                    } else {
                        stepOff(player);
                    }
                });
            }
            ctx.get().setPacketHandled(true);
        }
    }

    /** Server to client: entity {@code id} now stands on {@code anchor}, or (null) no longer does. */
    public record Sync(int id, Anchor anchor) {

        static Sync on(int id, Anchor anchor) {
            return new Sync(id, anchor);
        }

        static Sync off(int id) {
            return new Sync(id, null);
        }

        void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(id);
            buf.writeBoolean(anchor != null);
            if (anchor != null) {
                buf.writeDouble(anchor.x());
                buf.writeDouble(anchor.y());
                buf.writeDouble(anchor.z());
                buf.writeItem(anchor.stool());
            }
        }

        static Sync decode(FriendlyByteBuf buf) {
            int id = buf.readVarInt();
            if (!buf.readBoolean()) {
                return off(id);
            }
            return on(id, new Anchor(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readItem()));
        }

        void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (anchor == null) {
                    CLIENT.remove(id);
                } else {
                    CLIENT.put(id, anchor);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
