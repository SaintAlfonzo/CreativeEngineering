package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.corpse.CorpseDecay;
import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import com.SaintAlfonzo.creativecompat.corpse.CorpseState;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Corpse decay on world time ({@link CorpseDecay}; feature_writeup/plan_corpses.md 3.1). The Corpse mod's own
 * skeleton switch and both despawns in {@code tick} (m_8119_) are redirected away while {@code [corpses] enabled} is
 * on; once a second (and on the first tick after load) the corpse asks {@link CorpseDecay#target} for its stage and
 * re-syncs the worn equipment from its contents. Adds one synced byte of {@link CorpseState} flags and saves its
 * state in the {@link CorpseDecay#TAG} compound. Also names ancient corpses ({@code m_5446_}) and makes skeleton-stage
 * corpses sink in water and lava ({@code [corpses] skeletonsSink}, ruling 27). A second synced value carries the
 * corpse screen sections ({@link CorpseSections}; not saved, rebuilt from the contents). Applied only with the
 * Corpse mod (CompatMixinPlugin). SRG names, remap = false.
 */
@Mixin(value = CorpseEntity.class, remap = false)
public abstract class CorpseDecayMixin implements CorpseState {

	@Unique
	private static final EntityDataAccessor<Byte> creativecompat$FLAGS =
			SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BYTE);
	@Unique
	private static final EntityDataAccessor<CompoundTag> creativecompat$SECTIONS =
			SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.COMPOUND_TAG);

	@Shadow
	private int age;

	@Unique
	private long creativecompat$deathTime = -1L;
	@Unique
	private long creativecompat$emptiedAt = -1L;
	@Unique
	private int creativecompat$stage = CorpseDecay.PLAYER;
	@Unique
	private boolean creativecompat$checked;

	@Unique
	private CorpseEntity creativecompat$self() {
		return (CorpseEntity) (Object) this;
	}

	@Override
	public byte creativecompat$corpseFlags() {
		return creativecompat$self().getEntityData().get(creativecompat$FLAGS);
	}

	@Override
	public CompoundTag creativecompat$sections() {
		return creativecompat$self().getEntityData().get(creativecompat$SECTIONS);
	}

	@Unique
	private void creativecompat$setFlag(byte flag, boolean on) {
		byte old = creativecompat$corpseFlags();
		byte now = (byte) (on ? old | flag : old & ~flag);
		if (now != old) creativecompat$self().getEntityData().set(creativecompat$FLAGS, now);
	}

	@Inject(method = "m_8097_", at = @At("TAIL"), remap = false)
	private void creativecompat$defineFlags(CallbackInfo ci) {
		creativecompat$self().getEntityData().define(creativecompat$FLAGS, (byte) 0);
		creativecompat$self().getEntityData().define(creativecompat$SECTIONS, CorpseSections.defaultTag());
	}

	/**
	 * Corpse screen sections (plan_corpses.md 3.6 items 2 and 6): once a second the server refreshes the synced section
	 * tag (curio slot cap, the cosmetic stacks for the model), independent of {@code [corpses] enabled}.
	 */
	@Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
	private void creativecompat$sectionsTick(CallbackInfo ci) {
		CorpseEntity corpse = creativecompat$self();
		if (corpse.level.isClientSide || corpse.isRemoved()) return;
		if (corpse.tickCount > 1 && (corpse.tickCount + corpse.getId()) % 20 != 0) return;
		CompoundTag now = CorpseSections.buildTag(corpse);
		if (!now.equals(creativecompat$sections())) corpse.getEntityData().set(creativecompat$SECTIONS, now);
	}

	@Redirect(method = "m_8119_", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/entities/CorpseEntity;setIsSkeleton(Z)V"), remap = false)
	private void creativecompat$upstreamSkeleton(CorpseEntity corpse, boolean skeleton) {
		if (!CompatConfig.Corpses.on()) corpse.setIsSkeleton(skeleton);
	}

	@Redirect(method = "m_8119_", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/entities/CorpseEntity;m_146870_()V"), remap = false)
	private void creativecompat$upstreamDespawn(CorpseEntity corpse) {
		if (!CompatConfig.Corpses.on()) corpse.discard();
	}

	@Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
	private void creativecompat$decay(CallbackInfo ci) {
		CorpseEntity corpse = creativecompat$self();
		if (corpse.level.isClientSide || corpse.isRemoved() || !CompatConfig.Corpses.on()) return;
		if (creativecompat$checked && (corpse.tickCount + corpse.getId()) % 20 != 0) return;
		creativecompat$checked = true;

		long now = corpse.level.getGameTime();
		if (creativecompat$deathTime < 0) creativecompat$deathTime = now - Math.max(0, age);

		if (CompatConfig.CORPSES.followContents.get()) {
			NonNullList<ItemStack> shown = CorpseDecay.shown(corpse.getDeath());
			if (!CorpseDecay.sameShown(shown, corpse.getEquipment())) corpse.setEquipment(shown);
		}

		// Ancient Remains (Visceral Heap): a fixed skeleton, never decays or despawns
		if (creativecompat$has(FLAG_REMAINS)) {
			corpse.setIsSkeleton(true);
			return;
		}

		boolean empty = corpse.isEmpty();
		if (empty && creativecompat$emptiedAt < 0) creativecompat$emptiedAt = now;
		else if (!empty) creativecompat$emptiedAt = -1L;

		CorpseDecay.Kind kind = CorpseDecay.kind(corpse.level.dimension().location().toString());
		CorpseDecay.Result r = CorpseDecay.target(kind, CorpseDecay.Times.fromConfig(Main.SERVER_CONFIG.corpseForceDespawnTime.get()),
				now, creativecompat$deathTime, creativecompat$emptiedAt);
		if (r.remove()) {
			corpse.discard();
			return;
		}
		creativecompat$stage = Math.max(creativecompat$stage, r.target());
		corpse.setIsSkeleton(creativecompat$stage >= CorpseDecay.SKELETON);
		creativecompat$setFlag(FLAG_WITHERED, creativecompat$stage >= CorpseDecay.WITHERED);
	}

	/**
	 * Ruling 27: the Corpse mod lifts a corpse in water or lava (y speed + 0.01 up to 0.03); a skeleton-stage corpse
	 * instead accelerates down to {@code sinkSpeed}. Replaces the y argument of the one {@code setDeltaMovement} in tick.
	 */
	@ModifyArg(method = "m_8119_", at = @At(value = "INVOKE",
			target = "Lde/maxhenkel/corpse/entities/CorpseEntity;m_20334_(DDD)V"), index = 1, remap = false)
	private double creativecompat$sink(double y) {
		CorpseEntity corpse = creativecompat$self();
		if (!CompatConfig.Corpses.on() || !CompatConfig.CORPSES.skeletonsSink.get() || !corpse.isSkeleton()) return y;
		if (!corpse.isInWater() && !corpse.isInLava()) return y;
		double old = corpse.getDeltaMovement().y;
		return Math.max(-CompatConfig.CORPSES.sinkSpeed.get(), Math.min(old, 0.0) - 0.02);
	}

	@Inject(method = "m_7380_", at = @At("TAIL"), remap = false)
	private void creativecompat$save(CompoundTag compound, CallbackInfo ci) {
		CompoundTag tag = new CompoundTag();
		tag.putLong("DeathTime", creativecompat$deathTime);
		tag.putLong("EmptiedAt", creativecompat$emptiedAt);
		tag.putByte("Stage", (byte) creativecompat$stage);
		tag.putBoolean("Ancient", creativecompat$has(FLAG_ANCIENT));
		tag.putBoolean("Remains", creativecompat$has(FLAG_REMAINS));
		compound.put(CorpseDecay.TAG, tag);
	}

	@Inject(method = "m_7378_", at = @At("TAIL"), remap = false)
	private void creativecompat$load(CompoundTag compound, CallbackInfo ci) {
		if (!compound.contains(CorpseDecay.TAG)) return;
		CompoundTag tag = compound.getCompound(CorpseDecay.TAG);
		creativecompat$deathTime = tag.contains("DeathTime") ? tag.getLong("DeathTime") : -1L;
		creativecompat$emptiedAt = tag.contains("EmptiedAt") ? tag.getLong("EmptiedAt") : -1L;
		boolean ancient = tag.getBoolean("Ancient");
		boolean remains = tag.getBoolean("Remains");
		int stage = tag.getByte("Stage");
		if (ancient || remains) stage = Math.max(stage, CorpseDecay.SKELETON);
		creativecompat$stage = stage;
		creativecompat$setFlag(FLAG_ANCIENT, ancient);
		creativecompat$setFlag(FLAG_REMAINS, remains);
		creativecompat$setFlag(FLAG_WITHERED, stage >= CorpseDecay.WITHERED);
		creativecompat$self().setIsSkeleton(stage >= CorpseDecay.SKELETON);
		creativecompat$checked = false;
	}

	/** "Ancient Corpse" / "Ancient Remains" instead of "Corpse" for a corpse saved with the Ancient / Remains flag. */
	@Inject(method = "m_5446_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecompat$ancientName(CallbackInfoReturnable<Component> cir) {
		if (creativecompat$has(FLAG_REMAINS)) cir.setReturnValue(Component.translatable("entity.creativecompat.ancient_remains"));
		else if (creativecompat$has(FLAG_ANCIENT)) cir.setReturnValue(Component.translatable("entity.creativecompat.ancient_corpse"));
	}
}
