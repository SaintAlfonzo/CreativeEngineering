package com.SaintAlfonzo.creativeworldgen.mixin.wiseoak;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;
import com.SaintAlfonzo.creativeworldgen.wiseoak.WiseOakGifts;
import com.SaintAlfonzo.creativeworldgen.wiseoak.WiseOakTrustAccess;
import net.mehvahdjukaar.mysticaloaktree.block.Relationship;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakBlock;
import net.mehvahdjukaar.mysticaloaktree.block.WiseOakTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

/**
 * The Wise Oak's sleep and anger loop (feature_writeup/wise_oak.md 4.1): awake by day, asleep by night or storm, with
 * timed tired sleep, timed anger and a timed sleep after the anger. Stock it sleeps by day and calms on a dice roll.
 * Timers are absolute game times (0 = none), so /time and beds do not move them.
 *
 * <p>The stock ticker reaches the {@code getGameTime} call only on the server and only while no blow is running, so
 * the 23-tick timer check hooks there. {@code randomTick} keeps the stock cadence behind that same gate. Trust, the
 * blow, dialogue, enchant power, loot and lightning growth stay the mod's own code.
 */
@Mixin(value = WiseOakTile.class, remap = false)
public abstract class WiseOakTileMixin implements WiseOakTrustAccess {

    @Unique private static final String creativeworldgen$SLEEP_UNTIL = "creativeworldgen_sleep_until";
    @Unique private static final String creativeworldgen$ANGRY_UNTIL = "creativeworldgen_angry_until";
    @Unique private static final long creativeworldgen$TIMER_INTERVAL = 23L;
    @Unique private static final int creativeworldgen$BLINK_TICKS = 5;

    @Unique private long creativeworldgen$sleepUntil;
    @Unique private long creativeworldgen$angryUntil;

    @Shadow @Final private Map<UUID, Relationship> playerRelationship;

    @Shadow
    private Relationship getRelationship(Player player) {
        throw new AssertionError();
    }

    @Shadow
    private boolean isTiredOfYou(BlockState state, ServerLevel level) {
        throw new AssertionError();
    }

    @Shadow
    private void goToSleep(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @Shadow
    private void wakeUp(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    /** Replaces the stock day-sleep loop. Blinking and the tired roll behave as in the stock method. */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeworldgen$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                             CallbackInfo ci) {
        ci.cancel();
        WiseOakBlock.State current = state.getValue(WiseOakBlock.STATE);
        boolean night = level.isNight();
        long now = level.getGameTime();
        if (current.canSleep() && (night || isTiredOfYou(state, level))) {
            goToSleep(level, pos, state);
            creativeworldgen$sleepUntil = night ? 0L : now + creativeworldgen$ticks(CWGConfig.WISE_OAK.sleepDurationSeconds.get());
            creativeworldgen$markChanged();
            return;
        }
        if (current == WiseOakBlock.State.SLEEPING && !night && now >= creativeworldgen$sleepUntil) {
            wakeUp(level, pos, state);
            return;
        }
        if (current.canBlink() && random.nextFloat() < 1.0F) {
            level.scheduleTick(pos, state.getBlock(), creativeworldgen$BLINK_TICKS);
            level.setBlock(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.getBlinking(current)), 3);
        }
    }

    /** Ends anger and timed sleeps. Cancels the rest of the tick after a change, so randomTick never sees a stale state. */
    @Inject(method = "tick", cancellable = true, remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_46467_()J", remap = false))
    private static void creativeworldgen$timerGate(Level level, BlockPos pos, BlockState state, WiseOakTile tile, CallbackInfo ci) {
        if (level.getGameTime() % creativeworldgen$TIMER_INTERVAL == 0L
                && ((WiseOakTileMixin) (Object) tile).creativeworldgen$endTimers(level, pos, state)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean creativeworldgen$endTimers(Level level, BlockPos pos, BlockState state) {
        long now = level.getGameTime();
        WiseOakBlock.State current = state.getValue(WiseOakBlock.STATE);
        if (current.isAngry()) {
            if (now < creativeworldgen$angryUntil) return false;
            goToSleep(level, pos, state);
            creativeworldgen$angryUntil = 0L;
            creativeworldgen$sleepUntil = now + creativeworldgen$ticks(CWGConfig.WISE_OAK.angrySleepDurationSeconds.get());
            creativeworldgen$markChanged();
            return true;
        }
        if (current == WiseOakBlock.State.SLEEPING && creativeworldgen$sleepUntil != 0L
                && now >= creativeworldgen$sleepUntil && !level.isNight()) {
            wakeUp(level, pos, state);
            return true;
        }
        return false;
    }

    /** Every wake path goes through wakeUp, which also resets the talk counter. */
    @Inject(method = "wakeUp", at = @At("HEAD"), remap = false)
    private void creativeworldgen$clearTimers(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        creativeworldgen$sleepUntil = 0L;
        creativeworldgen$angryUntil = 0L;
        creativeworldgen$markChanged();
    }

    /** Right-click on a sleeper: the stock wake ends in NONE, this ends it in ANGRY. Runs on both sides like the stock click. */
    @Inject(method = "onInteract", remap = false,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, remap = false, target = "Lnet/mehvahdjukaar/mysticaloaktree/block/WiseOakTile;wakeUp(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void creativeworldgen$wokenAngry(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                             CallbackInfoReturnable<InteractionResult> cir) {
        level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.ANGRY));
        if (!level.isClientSide) creativeworldgen$startAnger(level);
    }

    /** A talk that happened returns CONSUME on the server; angry trees and a talk on cooldown return PASS. canSleep = awake and not blowing. */
    @Inject(method = "onInteract", at = @At("RETURN"), remap = false)
    private void creativeworldgen$gift(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide || cir.getReturnValue() == InteractionResult.PASS) return;
        if (!state.getValue(WiseOakBlock.STATE).canSleep()) return;
        WiseOakGifts.roll(level, pos, state.getValue(HorizontalDirectionalBlock.FACING), getRelationship(player).getTrust());
    }

    /** The end of a blow is stock ANGRY; only the timer is new. */
    @Inject(method = "stopBlowing", at = @At("TAIL"), remap = false)
    private void creativeworldgen$blowEnded(Level level, BlockPos pos, BlockState state, WiseOakTile tile, CallbackInfo ci) {
        if (!level.isClientSide) creativeworldgen$startAnger(level);
    }

    @Unique
    private void creativeworldgen$startAnger(Level level) {
        creativeworldgen$angryUntil = level.getGameTime() + creativeworldgen$ticks(CWGConfig.WISE_OAK.angerDurationSeconds.get());
        creativeworldgen$markChanged();
    }

    @Unique
    private static long creativeworldgen$ticks(int seconds) {
        return seconds * 20L;
    }

    @Unique
    private void creativeworldgen$markChanged() {
        ((BlockEntity) (Object) this).setChanged();
    }

    @Override
    public int creativeworldgen$getTrust(UUID player) {
        Relationship relationship = playerRelationship.get(player);
        return relationship == null ? 0 : relationship.getTrust();
    }

    @Override
    public void creativeworldgen$setTrust(UUID player, int trust) {
        playerRelationship.put(player, new Relationship(trust));
    }

    @Inject(method = "m_183515_", at = @At("TAIL"), remap = false)
    private void creativeworldgen$saveTimers(CompoundTag tag, CallbackInfo ci) {
        if (creativeworldgen$sleepUntil != 0L) tag.putLong(creativeworldgen$SLEEP_UNTIL, creativeworldgen$sleepUntil);
        if (creativeworldgen$angryUntil != 0L) tag.putLong(creativeworldgen$ANGRY_UNTIL, creativeworldgen$angryUntil);
    }

    @Inject(method = "m_142466_", at = @At("TAIL"), remap = false)
    private void creativeworldgen$loadTimers(CompoundTag tag, CallbackInfo ci) {
        creativeworldgen$sleepUntil = tag.getLong(creativeworldgen$SLEEP_UNTIL);
        creativeworldgen$angryUntil = tag.getLong(creativeworldgen$ANGRY_UNTIL);
    }
}
