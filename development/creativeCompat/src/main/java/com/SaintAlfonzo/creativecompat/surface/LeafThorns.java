package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;

/**
 * Leaf thorns and mob pathing in leaf piles (plan_post_scratch_1115.md G4e, G7.13, G7.14), server side.
 *
 * <p>Thorns: a player (only players) in a leaf layer block ({@code #creativecompat:passthrough_layers_open}) of at
 * least {@code thornLayers} layers, moving at least {@code thornMinSpeed} x walking speed measured against the full
 * pile's slowdown ({@link LayerMotion#thornSpeed}), is checked once every {@code thornCooldownTicks}: with chance
 * {@code thornChance x (4 - armor pieces) / 4} ({@link LayerMotion#thornChance}) it takes half a heart of sweet berry
 * bush damage ({@code DamageSource.SWEET_BERRY_BUSH}, so death messages and armor match the bush). Creative and
 * spectator players and the exempt mobs never count (exempt entities have no counted cells). Reads the pass-through
 * result the player's own move left ({@link PassState#result}).
 *
 * <p>Pathing ({@code mixin/LeafPilePathMixin}, {@code McaLeafPilePathMixin}): a leaf pile of {@code pathBlockLayers} or
 * more is a sweet-berry-like node ({@code DAMAGE_OTHER}) for every mob, the exempt ones included.
 */
public final class LeafThorns {

	private LeafThorns() {}

	/** End of each server player tick. */
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;
		PassState st = PassThrough.state(p, false);
		double x = p.getX(), z = p.getZ();
		if (st == null) return;
		double dx = x - st.thornX, dz = z - st.thornZ;
		st.thornX = x;
		st.thornZ = z;
		if (!CompatConfig.SPEC.isLoaded()) return;
		CompatConfig.Surface.Passthrough c = CompatConfig.SURFACE.passthrough;
		double base = c.thornChance.get();
		if (base <= 0.0 || st.result.leafLayers < c.thornLayers.get()) return;
		if (p.isCreative() || p.isSpectator()) return;
		PassValues values = PassValues.local();
		if (!values.enabled || PassThrough.skipsEntity(p)) return;
		long now = p.level.getGameTime();
		if (now < st.thornNext) return;
		double speed = Math.sqrt(dx * dx + dz * dz);
		if (speed > 2.0) return; // a teleport, not a step
		double fullWalk = values.tier(PassTier.LAYERS).leafMaxSlow();
		if (!LayerMotion.thornSpeed(speed, fullWalk, c.thornMinSpeed.get())) return;
		st.thornNext = now + c.thornCooldownTicks.get();
		double chance = LayerMotion.thornChance(base, armorPieces(p));
		if (chance > 0.0 && p.getRandom().nextDouble() < chance) p.hurt(DamageSource.SWEET_BERRY_BUSH, 1.0F);
	}

	/** Items in the four armor slots (an elytra in the chest slot counts). */
	static int armorPieces(ServerPlayer p) {
		int n = 0;
		for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			if (!p.getItemBySlot(s).isEmpty()) n++;
		}
		return n;
	}

	/** A leaf layer block of {@code pathBlockLayers} or more (server values; system and switch on). */
	public static boolean blocksPath(BlockState state) {
		if (state.isAir() || !PassThrough.openLayers(state)) return false;
		if (!CompatConfig.SPEC.isLoaded()) return false;
		if (!PassValues.local().enabled) return false;
		int min = CompatConfig.SURFACE.passthrough.pathBlockLayers.get();
		return min <= 8 && PassGrowth.layers(state) >= min;
	}
}
