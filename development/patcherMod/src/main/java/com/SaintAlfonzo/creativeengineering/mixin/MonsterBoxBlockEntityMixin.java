package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.mobs.MonsterBoxVault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.arl.block.be.ARLBlockEntity;
import vazkii.quark.content.world.block.be.MonsterBoxBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Quark monster boxes as vaults ({@link MonsterBoxVault}). The box keeps its phase, cooldown end and wave in
 * AutoRegLib's shared NBT, which {@code ARLBlockEntity} both saves and puts in the update tag; Quark's box has no
 * update packet of its own, so one is added for phase changes.
 */
@Mixin(value = MonsterBoxBlockEntity.class, remap = false)
public abstract class MonsterBoxBlockEntityMixin extends ARLBlockEntity implements MonsterBoxVault.Box {

    @Shadow private int breakProgress;

    @Unique private byte creativeengineering$phase = MonsterBoxVault.ARMED;
    @Unique private long creativeengineering$until;
    @Unique private final List<UUID> creativeengineering$wave = new ArrayList<>();

    protected MonsterBoxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void creativeengineering$vaultTick(Level level, BlockPos pos, BlockState state, MonsterBoxBlockEntity be, CallbackInfo ci) {
        if (MonsterBoxVault.tick(level, pos, (MonsterBoxVault.Box) be)) ci.cancel();
    }

    // Level#removeBlock after spawnMobs; SRG name, no refmap.
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_7471_(Lnet/minecraft/core/BlockPos;Z)Z"), require = 1, remap = false)
    private static boolean creativeengineering$keepBox(Level level, BlockPos pos, boolean isMoving) {
        return !MonsterBoxVault.onTriggered(level, pos) && level.removeBlock(pos, isMoving);
    }

    @Override
    public void writeSharedNBT(CompoundTag tag) {
        super.writeSharedNBT(tag);
        MonsterBoxVault.write(this, tag);
    }

    @Override
    public void readSharedNBT(CompoundTag tag) {
        super.readSharedNBT(tag);
        MonsterBoxVault.read(this, tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public byte creativeengineering$phase() {
        return creativeengineering$phase;
    }

    @Override
    public void creativeengineering$setPhase(byte phase) {
        creativeengineering$phase = phase;
    }

    @Override
    public long creativeengineering$until() {
        return creativeengineering$until;
    }

    @Override
    public void creativeengineering$setUntil(long until) {
        creativeengineering$until = until;
    }

    @Override
    public List<UUID> creativeengineering$wave() {
        return creativeengineering$wave;
    }

    @Override
    public void creativeengineering$setBreakProgress(int progress) {
        breakProgress = progress;
    }

    @Override
    public void creativeengineering$changed() {
        setChanged();
        sync();
    }
}
