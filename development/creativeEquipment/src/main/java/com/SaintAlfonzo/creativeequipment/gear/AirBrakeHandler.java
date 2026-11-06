package com.SaintAlfonzo.creativeequipment.gear;

import java.lang.reflect.Method;

import com.SaintAlfonzo.creativeequipment.CEQConfig;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * creativeengineering:air_brake - the item itself (paper texture, bow-style hold-to-use
 * duration) is registered entirely in KubeJS ({@code startup_scripts/items/air_brake.js});
 * what KubeJS can't do is touch a player's velocity mid-flight, so the actual slowdown
 * lives here.
 *
 * <p>Registered on the plain Forge event bus (no Mixin needed) via
 * {@code TickEvent.PlayerTickEvent} - Forge fires this once per tick for whichever
 * {@code Player} entity is actually ticking on each physical side: the server's own copy
 * for every connected {@code ServerPlayer}, AND the client's copy for its local player.
 * Elytra movement is client-predicted (vanilla is deliberately lenient about a gliding
 * player's reported position - see {@code isFallFlying()}'s carve-out in
 * {@code ServerGamePacketListenerImpl}'s movement sanity checks), so the deceleration below
 * only actually shows up in-game because this same, deterministic code runs on the CLIENT
 * tick too. A purely server-side change (e.g. a KubeJS {@code server_scripts}
 * {@code PlayerEvents.tick}, which only ever runs on the logical server) would be silently
 * overwritten by the client's next movement packet before a player ever saw it slow down.
 *
 * <p><b>2026-09-13 bug:</b> the brake did nothing at all on first release. This class was
 * fine - {@code isUsingItem()} simply never became true, because right-clicking a plain
 * KubeJS item does NOT call {@code startUsingItem()} on its own (that's specific to items
 * like {@code BowItem} that override {@code use()} to do it). KubeJS's own {@code ItemMixin}
 * only starts the use-item phase when the item's builder has a {@code .use(...)} callback
 * that returns true - see {@code items/air_brake.js}'s {@code .use(...)} call, added
 * alongside this note. {@code useAnimation}/{@code useDuration} alone only describe what
 * happens once a use is already in progress; something still has to start one.
 *
 * <p>All the actual deceleration numbers, and the stall effect below, are config knobs -
 * see {@link CEQConfig.AirBrake}.
 */
public final class AirBrakeHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ITEM_ID = new ResourceLocation("creativeengineering", "air_brake");

    // Entity's own FLAG_FALL_FLYING bit index (see Entity#isFallFlying's shared-flag lookup) -
    // there's no public API to clear it, so stopFallFlying() below reaches Entity#setSharedFlag
    // (protected) via one reflective Method handle, resolved once and cached.
    private static final int FLAG_FALL_FLYING = 7;
    private static final Method SET_SHARED_FLAG;

    static {
        // Reflection strings are plain data, not a bytecode symbolic reference - reobfJar
        // happily rewrites every ordinary compiled call in this class (isFallFlying(),
        // getDeltaMovement(), ...) from their Mojmap ("official") source names to whatever
        // this runtime actually uses, but it can't touch a String literal handed to
        // getDeclaredMethod(). This runtime turns out to still be SRG-obfuscated underneath
        // (same lesson [[patchermod-coremod]] already learned for Mixin targets) - confirmed
        // by a NoSuchMethodException on "setSharedFlag" at game load, then looking up its
        // real name in build/reobfJar/mappings.tsrg. If a future MCP/mapping update changes
        // this again, the fix is the same: grep mappings.tsrg for the Mojmap name.
        Method m;
        try {
            m = Entity.class.getDeclaredMethod("m_20115_", int.class, boolean.class); // setSharedFlag
            m.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[air_brake] couldn't resolve Entity#setSharedFlag (m_20115_) - stall effect will do nothing", e);
            m = null;
        }
        SET_SHARED_FLAG = m;
    }

    private static Item item;
    private static boolean lookedUp;

    private AirBrakeHandler() {}

    private static Item item() {
        // Lazy + cached: KubeJS's item RegisterEvent hasn't fired yet at mod construction -
        // same reasoning as MagicResistanceEffect's effect() lookup.
        if (!lookedUp) {
            item = ForgeRegistries.ITEMS.getValue(ITEM_ID);
            lookedUp = true;
        }
        return item;
    }

    /** Drops the player out of elytra flight (the brake's configurable "stall" effect). */
    private static void stopFallFlying(Player player) {
        if (SET_SHARED_FLAG == null) return;
        try {
            SET_SHARED_FLAG.invoke(player, FLAG_FALL_FLYING, false);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[air_brake] Entity#setSharedFlag invocation failed", e);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!CEQConfig.AIR_BRAKE.enabled.get()) return;

        Item brake = item();
        if (brake == null) return;

        Player player = event.player;
        if (!player.isUsingItem() || player.getUseItem().getItem() != brake) return;
        if (CEQConfig.AIR_BRAKE.requireFallFlying.get() && !player.isFallFlying()) return;

        Vec3 v = player.getDeltaMovement();

        // Horizontal (X/Z): steady flat deceleration, direction preserved, floored at a
        // configurable minimum speed rather than allowed to reverse.
        double horizSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
        double minHoriz = CEQConfig.AIR_BRAKE.minHorizontalSpeed.get();
        double newHorizSpeed = Math.max(minHoriz, horizSpeed - CEQConfig.AIR_BRAKE.horizontalDecelerationPerTick.get());
        double scale = horizSpeed > 1.0E-6 ? newHorizSpeed / horizSpeed : 0.0;

        double newY;
        if (v.y > 0.0) {
            // Climbing: same flat-deceleration treatment as horizontal.
            newY = Math.max(CEQConfig.AIR_BRAKE.minUpwardSpeed.get(), v.y - CEQConfig.AIR_BRAKE.upwardDecelerationPerTick.get());
        } else {
            // Falling: only slowed slightly, and multiplicatively (a fraction of current
            // speed per tick) rather than a flat subtraction, so it trims a dive instead of
            // cancelling it outright. terminalDownwardSpeed (if set) caps speed first.
            double terminal = CEQConfig.AIR_BRAKE.terminalDownwardSpeed.get();
            double capped = terminal > 0.0 ? Math.max(v.y, -terminal) : v.y;
            newY = capped * (1.0 - CEQConfig.AIR_BRAKE.downwardSlowFactorPerTick.get());
        }

        player.setDeltaMovement(v.x * scale, newY, v.z * scale);
        // Velocity changed outside vanilla's own movement code - flag so a server-side
        // player's new motion actually gets synced to observing clients this tick.
        player.hurtMarked = true;

        // Stall: brake hard enough, for long enough, and the glide can't sustain itself -
        // drop the player out of fall-flying outright. Gated on isFallFlying() itself
        // (not requireFallFlying's config value above) - there's nothing to stall out of
        // if the player was never gliding in the first place.
        if (CEQConfig.AIR_BRAKE.stallEnabled.get() && player.isFallFlying()
                && newHorizSpeed <= CEQConfig.AIR_BRAKE.stallSpeedThreshold.get()) {
            stopFallFlying(player);
        }
    }
}
