package io.github.mortuusars.exposure.util.supporter;

import java.util.UUID;
import net.minecraft.Util;

public record Supporter(String name, UUID uuid) {
   public boolean matches(UUID uuid) {
      return uuid.equals(Util.NIL_UUID) ? false : this.uuid().equals(uuid);
   }
}
