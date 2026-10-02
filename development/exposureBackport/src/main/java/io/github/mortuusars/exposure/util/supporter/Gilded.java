package io.github.mortuusars.exposure.util.supporter;

import io.github.mortuusars.exposure.Exposure;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Gilded {
   private long lastQueryTime = -1L;
   @Nullable
   private List<Supporter> gildedSupporters = null;

   public boolean canQuery() {
      return System.currentTimeMillis() - this.lastQueryTime > 60000L;
   }

   @NotNull
   public List<Supporter> getOrQuery() {
      if (this.gildedSupporters != null) {
         return this.gildedSupporters;
      } else {
         return !this.canQuery() ? Collections.emptyList() : this.query();
      }
   }

   @NotNull
   public List<Supporter> query() {
      this.lastQueryTime = System.currentTimeMillis();

      try {
         Supporters.Loader loader = new Supporters.Loader();
         new Thread(() -> {
            String json = loader.readFileFromURL(this.getUuidsUri());
            if (json != null) {
               this.gildedSupporters = loader.parseSupporters(json);
            }
         }).start();
      } catch (Exception var2) {
         Exposure.LOGGER.warn("Cannot get list of supporters.", var2);
      }

      return this.gildedSupporters == null ? Collections.emptyList() : this.gildedSupporters;
   }

   protected URI getUuidsUri() {
      return URI.create("https://raw.githubusercontent.com/mortuusars/resources/refs/heads/main/supporters/uuids/gilded.json");
   }

   public boolean hasAccessToGoldenSkin(UUID uuid) {
      return this.getOrQuery().stream().anyMatch(s -> s.matches(uuid));
   }
}
