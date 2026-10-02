package io.github.mortuusars.exposure.util.supporter;

import io.github.mortuusars.exposure.Exposure;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Patreon {
   private long lastQueryTime = -1L;
   @Nullable
   private Map<Patreon.Tier, List<Supporter>> patrons = null;

   public boolean canQuery() {
      return System.currentTimeMillis() - this.lastQueryTime > 60000L;
   }

   @NotNull
   public Map<Patreon.Tier, List<Supporter>> getOrQuery() {
      if (this.patrons != null) {
         return this.patrons;
      } else {
         return !this.canQuery() ? Collections.emptyMap() : this.query();
      }
   }

   @NotNull
   public Map<Patreon.Tier, List<Supporter>> query() {
      this.lastQueryTime = System.currentTimeMillis();

      try {
         Supporters.Loader loader = new Supporters.Loader();
         if (this.patrons != null) {
            this.patrons.clear();
         }

         new Thread(() -> {
            for (Patreon.Tier patreonTier : Patreon.Tier.values()) {
               String json = loader.readFileFromURL(patreonTier.getUuidsUri());
               if (json == null) {
                  return;
               }

               List<Supporter> parsedSupporters = loader.parseSupporters(json);
               if (this.patrons == null) {
                  this.patrons = new HashMap<>();
               }

               this.patrons.put(patreonTier, parsedSupporters);
            }
         }).start();
      } catch (Exception var2) {
         Exposure.LOGGER.warn("Cannot get list of supporters.", var2);
      }

      return this.patrons == null ? Collections.emptyMap() : this.patrons;
   }

   public boolean hasAccessToGoldenSkin(UUID uuid) {
      Map<Patreon.Tier, List<Supporter>> patrons = this.getOrQuery();
      return patrons.getOrDefault(Patreon.Tier.GOLD, Collections.emptyList()).stream().anyMatch(s -> s.matches(uuid))
         || patrons.getOrDefault(Patreon.Tier.DIAMOND, Collections.emptyList()).stream().anyMatch(s -> s.matches(uuid));
   }

   public static enum Tier {
      COPPER,
      IRON,
      GOLD,
      DIAMOND;

      public URI getUuidsUri() {
         return URI.create(
            "https://raw.githubusercontent.com/mortuusars/resources/refs/heads/main/supporters/patreon/uuids/" + this.name().toLowerCase() + ".json"
         );
      }
   }
}
