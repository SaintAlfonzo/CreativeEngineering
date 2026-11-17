package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassArrow;
import com.SaintAlfonzo.creativecompat.surface.PassArrows;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Arrows in pass-through webs and their despawn (plan_passthrough.md 5.3, 5.4):
 * <ul>
 *   <li>{@link PassArrow}: the web hang puts the arrow into vanilla's stuck state ({@code inGround} {@code f_36703_},
 *       {@code lastState} {@code f_36696_} = the web at its position, motion zero, {@code shakeTime} 7, no crit), so
 *       it is picked up as usual ({@code playerTouch} / {@code tryPickup}, its own {@code getPickupItem}) and falls when
 *       the web breaks ({@code lastState} differs and {@code shouldFall}); its block-hit code never runs;</li>
 *   <li>HEAD of {@code tickDespawn} ({@code m_6901_}): {@code surface.PassArrows} picks the limit (10 minutes for a
 *       pickable arrow, vanilla's minute when demoted by the per-chunk cap); -1 leaves vanilla's code to run.</li>
 * </ul>
 * SRG names, {@code remap = false}.
 */
@Mixin(value = AbstractArrow.class, remap = false)
public abstract class ArrowPassMixin implements PassArrow {

    @Shadow
    private int f_36697_; // life

    @Shadow
    protected boolean f_36703_; // inGround

    @Shadow
    private BlockState f_36696_; // lastState

    @Shadow
    protected abstract ItemStack m_7941_(); // getPickupItem

    @Inject(method = "m_6901_", at = @At("HEAD"), cancellable = true)
    private void creativecompat$despawn(CallbackInfo ci) {
        int limit;
        try {
            limit = PassArrows.despawnLimit((AbstractArrow) (Object) this);
        } catch (Throwable t) {
            limit = -1;
        }
        if (limit < 0) return;
        ci.cancel();
        if (++f_36697_ >= limit) ((Entity) (Object) this).discard();
    }

    @Override
    public void creativecompat$hangInWeb() {
        AbstractArrow self = (AbstractArrow) (Object) this;
        f_36703_ = true;
        f_36696_ = self.level.getBlockState(self.blockPosition());
        self.setDeltaMovement(Vec3.ZERO);
        self.shakeTime = 7;
        self.setCritArrow(false);
    }

    @Override
    public boolean creativecompat$inGround() {
        return f_36703_;
    }

    @Override
    public BlockState creativecompat$lastState() {
        return f_36696_;
    }

    @Override
    public int creativecompat$life() {
        return f_36697_;
    }

    @Override
    public ItemStack creativecompat$pickupItem() {
        return m_7941_();
    }
}
