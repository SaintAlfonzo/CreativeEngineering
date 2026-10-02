package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.client.util.PlayerClientHooks;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraRemoveS2CP;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes every player a {@link CameraHolder} and {@link CameraOperator}.
 * <p>
 * Port: upstream's ServerPlayerMixin and LocalPlayerMixin override set/removeActiveExposureCamera with a {@code super}
 * call into this mixin. That needs interface injection at compile time, so their extra work (sync packet on the
 * server, viewfinder on the client) and their tick hooks are done here.
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements CameraHolder, CameraOperator {
   @Unique
   @Nullable
   protected Camera activeExposureCamera;
   @Unique
   protected float oExposureCameraActionAnim = 0.0F;
   @Unique
   protected float exposureCameraActionAnim = 0.0F;

   @Shadow
   public abstract Inventory getInventory();

   protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
      super(entityType, level);
   }

   @Override
   public Optional<Player> getPlayerExecutingExposure() {
      return Optional.of((Player)(Object)this);
   }

   @Override
   public Optional<Player> getPlayerAwardedForExposure() {
      return Optional.of((Player)(Object)this);
   }

   @NotNull
   @Override
   public Entity getExposureAuthorEntity() {
      return this;
   }

   @Override
   public Optional<CameraOperator> getExposureCameraOperator() {
      return Optional.of(this);
   }

   @Nullable
   @Override
   public Camera getActiveExposureCamera() {
      return this.activeExposureCamera != null && !this.activeExposureCamera.isActive() ? null : this.activeExposureCamera;
   }

   @Override
   public void setActiveExposureCamera(@Nullable Camera camera) {
      this.activeExposureCamera = camera;
      if ((Object)this instanceof ServerPlayer && camera != null) {
         Packets.sendToAllClients(camera.createSyncPacket());
      } else if (this.level.isClientSide) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PlayerClientHooks.onActiveCameraSet((Player)(Object)this, camera));
      }
   }

   @Override
   public void removeActiveExposureCamera() {
      this.activeExposureCamera = null;
      if ((Object)this instanceof ServerPlayer) {
         Packets.sendToAllClients(new ActiveCameraRemoveS2CP(this.getId()));
      } else if (this.level.isClientSide) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PlayerClientHooks.onActiveCameraRemoved((Player)(Object)this));
      }
   }

   @Override
   public float getExposureCameraActionAnim(float partialTick) {
      float delta = this.exposureCameraActionAnim - this.oExposureCameraActionAnim;
      if (delta < 0.0F) {
         delta++;
      }

      return this.oExposureCameraActionAnim + delta * partialTick;
   }

   @Inject(method = "tick()V", at = @At("HEAD"))
   private void onTick(CallbackInfo ci) {
      if (!this.level.isClientSide() && this.activeExposureCamera != null && !this.activeExposureCamera.isActive()) {
         this.activeExposureCamera.deactivate();
         ((CameraOperator)this).removeActiveExposureCamera();
      }

      this.oExposureCameraActionAnim = this.exposureCameraActionAnim;
      long lastActionTime = -1L;
      if (this.activeExposureCamera != null) {
         lastActionTime = this.activeExposureCamera.map(CameraItem::getLastActionTime).orElse(-1L);
      } else if (this.getMainHandItem().getItem() instanceof CameraItem item) {
         lastActionTime = item.getLastActionTime(this.getMainHandItem());
      } else if (this.getOffhandItem().getItem() instanceof CameraItem item) {
         lastActionTime = item.getLastActionTime(this.getOffhandItem());
      }

      int actionTime = (int)(this.level.getGameTime() - lastActionTime);
      this.exposureCameraActionAnim = Mth.clamp((float)actionTime / 10.0F, 0.0F, 1.0F);
   }

   /** Upstream ServerPlayerMixin / LocalPlayerMixin tick (RETURN). */
   @Inject(method = "tick()V", at = @At("RETURN"))
   private void onTickReturn(CallbackInfo ci) {
      if ((Object)this instanceof ServerPlayer serverPlayer) {
         ServerEvents.playerTick(serverPlayer);
      } else if (this.level.isClientSide) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PlayerClientHooks.onLocalPlayerTick((Player)(Object)this));
         ((CameraOperator)this).getActiveExposureCameraOptional().ifPresent(camera -> {
            if (!camera.isActive()) {
               ((CameraOperator)this).removeActiveExposureCamera();
            }
         });
      }
   }

   @Inject(method = "blockActionRestricted(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/GameType;)Z", at = @At("HEAD"), cancellable = true)
   private void onBlockActionRestricted(Level level, BlockPos pos, GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
      if (((CameraOperator)this).getActiveExposureCamera() instanceof CameraOnStand) {
         cir.setReturnValue(true);
      }
   }
}
