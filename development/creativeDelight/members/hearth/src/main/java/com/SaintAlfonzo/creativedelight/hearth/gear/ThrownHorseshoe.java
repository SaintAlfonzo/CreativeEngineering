package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** Thrown horseshoe, 3.5 base damage; a hit on a fence is a ringer and lights redstone lamps touching that fence for a moment. */
public class ThrownHorseshoe extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ThrownHorseshoe.class, EntityDataSerializers.ITEM_STACK);
    private boolean creativeThrown;

    public ThrownHorseshoe(EntityType<? extends ThrownHorseshoe> type, Level level) {
        super(type, level);
    }

    public ThrownHorseshoe(Level level, LivingEntity shooter, ItemStack stack) {
        super(HHEntities.THROWN_HORSESHOE.get(), shooter, level);
        ItemStack one = stack.copy();
        one.setCount(1);
        entityData.set(DATA_ITEM, one);
        setBaseDamage(3.5D);
        pickup = Pickup.ALLOWED;
        creativeThrown = shooter instanceof Player player && player.getAbilities().instabuild;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getShoe() {
        return entityData.get(DATA_ITEM);
    }

    /** Client read of the stuck state; AbstractArrow's inGround field is protected. */
    public boolean isStuck() {
        return inGround;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = result.getBlockPos();
        BlockState hit = server.getBlockState(pos);
        if (!hit.is(BlockTags.FENCES)) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockPos side = pos.relative(dir);
            BlockState state = server.getBlockState(side);
            if (state.is(Blocks.REDSTONE_LAMP) && !state.getValue(BlockStateProperties.LIT)) {
                server.setBlock(side, state.setValue(BlockStateProperties.LIT, true), 3);
                server.scheduleTick(side, state.getBlock(), 20);
            }
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return !creativeThrown && super.tryPickup(player);
    }

    @Override
    protected ItemStack getPickupItem() {
        return getShoe().copy();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("HorseshoeItem", getShoe().save(new CompoundTag()));
        tag.putBoolean("CreativeThrown", creativeThrown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_ITEM, ItemStack.of(tag.getCompound("HorseshoeItem")));
        creativeThrown = tag.getBoolean("CreativeThrown");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
