package io.github.mortuusars.exposure.forge.integration;

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.entity.player.Player;

public class CreateIntegration {
   public static boolean isDeployer(Player player) {
      return player instanceof DeployerFakePlayer;
   }
}
