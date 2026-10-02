package io.github.mortuusars.exposure.util.supporter;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.mortuusars.exposure.Exposure;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Supporters {
   private static final Gilded gilded = new Gilded();
   private static final Patreon patreon = new Patreon();

   public static void query() {
      gilded.query();
      patreon.query();
   }

   public static Gilded gilded() {
      return gilded;
   }

   public static Patreon patreon() {
      return patreon;
   }

   public static boolean hasAccessToGoldenSkin(UUID uuid) {
      return gilded().hasAccessToGoldenSkin(uuid) || patreon().hasAccessToGoldenSkin(uuid);
   }

   public static class Loader {
      @Nullable
      public String readFileFromURL(URI uri) {
         try {
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            String encoding = connection.getContentEncoding();
            Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);

            String var7;
            try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
               var7 = CharStreams.toString(reader);
            }

            return var7;
         } catch (Exception var11) {
            Exposure.LOGGER.warn("Cannot read file from '{}': {}", uri, var11.getMessage());
            return null;
         }
      }

      @NotNull
      public List<Supporter> parseSupporters(String json) {
         try {
            Gson gson = new Gson();
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();
            List<Supporter> supporters = new ArrayList<>();

            for (JsonElement element : array) {
               try {
                  // 1.19.2 ships Gson 2.8.9, which can't create records (and throws an Error, not an Exception)
                  com.google.gson.JsonObject obj = element.getAsJsonObject();
                  Supporter p = new Supporter(obj.get("name").getAsString(), java.util.UUID.fromString(obj.get("uuid").getAsString()));
                  supporters.add(p);
               } catch (Exception var8) {
                  Exposure.LOGGER.warn("Cannot parse supporter from '{}': {}", element, var8.getMessage());
               }
            }

            return supporters;
         } catch (Exception var9) {
            Exposure.LOGGER.warn("Cannot get list of supporters: {}", var9.getMessage());
            return Collections.emptyList();
         }
      }
   }
}
