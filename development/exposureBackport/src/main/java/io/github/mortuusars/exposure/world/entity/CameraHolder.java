package io.github.mortuusars.exposure.world.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface CameraHolder {
   default Optional<Player> getPlayerExecutingExposure() {
      throw new IllegalStateException("This method must be implemented, and should return a player that will render the image.");
   }

   default Optional<ServerPlayer> getServerPlayerExecutingExposure() {
      return this.getPlayerExecutingExposure().filter(pl -> pl instanceof ServerPlayer).map(pl -> (ServerPlayer)pl);
   }

   default Optional<Player> getPlayerAwardedForExposure() {
      throw new IllegalStateException(
         "This method must be implemented, and should return a player that will receive advancements or stats for exposure (if applicable)."
      );
   }

   default Optional<ServerPlayer> getServerPlayerAwardedForExposure() {
      return this.getPlayerAwardedForExposure().filter(pl -> pl instanceof ServerPlayer).map(pl -> (ServerPlayer)pl);
   }

   @NotNull
   default Entity getExposureAuthorEntity() {
      throw new IllegalStateException("This method must be implemented, and should return an entity that will be treated as an author of a photo.");
   }

   default Optional<CameraOperator> getExposureCameraOperator() {
      throw new IllegalStateException("This method must be implemented, and should return an operator (if present).");
   }

   default Entity asHolderEntity() {
      return (Entity)this;
   }
}
