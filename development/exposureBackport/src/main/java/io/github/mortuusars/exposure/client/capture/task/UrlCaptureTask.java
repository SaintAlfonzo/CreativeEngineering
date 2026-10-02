package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedBufferedImage;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.awt.image.BufferedImage;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class UrlCaptureTask extends Task<Result<Image>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final TranslatableError ERROR_CANNOT_READ = new TranslatableError("error.exposure.capture.url.cannot_read", "ERR_CANNOT_READ");
   public static final TranslatableError ERROR_NO_HTTP_PREFIX = new TranslatableError("error.exposure.capture.url.no_http_prefix", "ERR_NO_HTTP_PREFIX");
   public static final TranslatableError ERROR_INVALID_URL = new TranslatableError("error.exposure.capture.url.invalid_url", "ERR_INVALID_URL");
   public static final TranslatableError ERROR_TIMED_OUT = new TranslatableError("error.exposure.capture.url.timed_out", "ERR_TIMED_OUT");
   protected final URI uri;
   protected final CompletableFuture<Result<Image>> future = new CompletableFuture<>();

   public UrlCaptureTask(URI uri) {
      this.uri = uri;
   }

   public URI getUri() {
      return this.uri;
   }

   public boolean isUrlValid() {
      String scheme = this.uri.getScheme();
      if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
         String host = this.uri.getHost();
         if (host == null) {
            return false;
         } else if (Minecrft.get().hasSingleplayerServer()) {
            return true;
         } else {
            host = IDN.toASCII(host.toLowerCase(Locale.ROOT));

            return switch ((UrlLoading)Config.Client.URL_LOADING.get()) {
               case ALL -> {
                  boolean var15 = true;
                  yield var15;
               }
               case ONLY_ALLOWED_DOMAINS -> {
                  if (!(Config.Client.URL_LOADING_ALLOWED_DOMAINS.get()).contains(host)
                     && (Config.Client.URL_LOADING_ALLOWED_SUBDOMAINS.get()).stream().noneMatch(host::endsWith)) {
                     LOGGER.error("Domain is not allowed for image loading. URL: {}", this.uri);
                     Minecrft.player()
                        .displayClientMessage(Component.literal("Domain is not allowed for image projecting.").withStyle(ChatFormatting.RED), false);
                     boolean var14 = false;
                     yield var14;
                  } else {
                     boolean var12;
                     label52: {
                        try {
                           InetAddress[] addresses = InetAddress.getAllByName(host);

                           for (InetAddress addr : addresses) {
                              if (addr.isAnyLocalAddress()
                                 || addr.isLoopbackAddress()
                                 || addr.isLinkLocalAddress()
                                 || addr.isSiteLocalAddress()
                                 || addr.isMulticastAddress()) {
                                 LOGGER.error("Domain is pointing to local address. URL: {}", this.uri);
                                 var12 = false;
                                 break label52;
                              }
                           }
                        } catch (UnknownHostException var9) {
                           LOGGER.error("Domain is unknown. URL: {}", this.uri);
                           var12 = false;
                           yield var12;
                        }

                        var12 = true;
                        yield var12;
                     }

                     yield var12;
                  }
               }
               case NONE -> {
                  LOGGER.error("URL image loading is disabled. URL: {}", this.uri);
                  boolean var3 = false;
                  yield var3;
               }
            };
         }
      } else {
         return false;
      }
   }

   @Override
   public CompletableFuture<Result<Image>> execute() {
      return this.future.completeAsync(() -> {
         if (!this.isUrlValid()) {
            LOGGER.error("URL '{}' is not valid. Image would not be loaded.", this.uri);
            return Result.error(ERROR_INVALID_URL);
         } else {
            LOGGER.info("Attempting to load image from URL: '{}'", this.uri.toString());

            try {
               BufferedImage image = ImageIO.read(this.uri.toURL());
               if (image == null) {
                  LOGGER.error("Cannot load image from URL '{}'", this.uri);
                  return Result.error(ERROR_CANNOT_READ);
               } else if (image.getWidth() <= 10000 && image.getHeight() <= 10000) {
                  return Result.success(new WrappedBufferedImage(image));
               } else {
                  LOGGER.error("Cannot load image from URL '{}': image is too large.", this.uri);
                  return Result.error(ERROR_CANNOT_READ);
               }
            } catch (Exception var2) {
               LOGGER.error("Cannot load image from URL: ", var2);
               return Result.error(ERROR_CANNOT_READ);
            }
         }
      }).completeOnTimeout(Result.error(ERROR_TIMED_OUT), (long)((Integer)Config.Server.PROJECT_TIMEOUT_TICKS.get() * 50), TimeUnit.MILLISECONDS);
   }
}
