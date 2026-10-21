package net.satisfy.foundation.seat;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Invisible entity the player rides while sitting. Gets discarded on dismount
 * and puts the player back where they came from.
 */
public class ChairEntity extends Entity {
    public ChairEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity passenger) {
        if (passenger instanceof Player p) {
            BlockPos pos = SeatUtil.getPreviousPlayerPosition(p, this);
            if (pos != null) {
                this.discard();
                return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            }
        }

        this.discard();
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        SeatUtil.removeChairEntity(this.level, this.blockPosition());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag nbt) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
    }

    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
