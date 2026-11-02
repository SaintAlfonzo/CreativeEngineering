package com.SaintAlfonzo.creativedelight.mixin.canned_goods;

import com.SaintAlfonzo.creativedelight.compat.DyeableCannedGolem;
import net.mcreator.cannedgoods.entity.CannedGolemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Right-click a canned golem with any dye (vanilla or #forge:dyes/<color>) to recolour it. The
 * colour is synced entity data so the client renderer can pick the texture, and is saved as
 * NBT "Color" (DyeColor id, default red). CannedGolemEntity overrides none of these methods, so
 * they are added straight onto it. See compat.DyeableCannedGolem.
 */
@Mixin(value = CannedGolemEntity.class, remap = false)
public abstract class CannedGolemColorMixin extends PathfinderMob implements DyeableCannedGolem {

    @Unique
    private static final EntityDataAccessor<Byte> CE_COLOR =
            SynchedEntityData.defineId(CannedGolemEntity.class, EntityDataSerializers.BYTE);

    protected CannedGolemColorMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public DyeColor ce$getColor() {
        return DyeColor.byId(this.entityData.get(CE_COLOR));
    }

    @Override
    public void ce$setColor(DyeColor color) {
        this.entityData.set(CE_COLOR, (byte) color.getId());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CE_COLOR, (byte) DEFAULT_COLOR.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Color", (byte) ce$getColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Color", Tag.TAG_ANY_NUMERIC)) {
            ce$setColor(DyeColor.byId(tag.getByte("Color")));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DyeColor dye = DyeColor.getColor(stack);
        if (dye == null || dye == ce$getColor()) {
            return super.mobInteract(player, hand);
        }
        if (!this.level.isClientSide) {
            ce$setColor(dye);
            this.level.playSound(null, this, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }
}
