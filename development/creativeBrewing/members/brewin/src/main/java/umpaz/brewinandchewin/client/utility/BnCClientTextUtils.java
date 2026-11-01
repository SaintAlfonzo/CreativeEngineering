package umpaz.brewinandchewin.client.utility;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

/**
 * Client-side scrambling of chat, signs and name tags (alcohol.md 4.3: clients only, with client thresholds).
 * <p>
 * Chat: the server sends MakeNextPlayerChatTipsy (the sender's Tipsy level and a seed) just before each Tipsy player's
 * message to every receiver. The receiver scrambles that message when the sender's level, or its own Tipsy level if
 * higher, reaches its {@code levelChatScramble}. The same seed gives the same scramble on every client.
 * Signs and name tags scramble from the viewer's own Tipsy.
 */
public class BnCClientTextUtils {
   public static int tipsyMessageLevel = 0;
   public static boolean generatedRandom = false;
   public static int clearDelayAmount = 0;
   public static long randomSeed = 0L;

   /** Scrambles the literal text of a component tree; styles stay. `amplifier` is the Tipsy amplifier. */
   public static Component modifyComponents(Component original, long randomSeed, int amplifier, int threshold) {
      String text = original.getContents().visit(Optional::of).orElse("");
      MutableComponent component;
      if (!text.isEmpty()) {
         RandomSource random = RandomSource.create(randomSeed);
         component = Component.literal(BnCTextUtils.scramble(text, amplifier - threshold, random)).withStyle(original.getStyle());
      } else {
         component = original.plainCopy().withStyle(original.getStyle());
      }

      for (Component sibling : original.getSiblings()) {
         component.append(modifyComponents(sibling, randomSeed, amplifier, threshold));
      }

      return component;
   }

   private static int ownTipsy(@Nullable Player player) {
      if (player == null) {
         return -1;
      }
      MobEffectInstance tipsy = player.getEffect(BnCEffects.TIPSY);
      return tipsy == null ? -1 : tipsy.getAmplifier();
   }

   /**
    * The scrambled chat message content, or null to leave it alone. Consumes the pending sender level and seed.
    */
   @Nullable
   public static Component scrambleChatContent(Component content) {
      BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
      int level = generatedRandom ? tipsyMessageLevel : -1;
      level = Math.max(level, ownTipsy(Minecraft.getInstance().player));
      long seed = generatedRandom ? randomSeed : (Minecraft.getInstance().player == null ? 0L : Minecraft.getInstance().player.getRandom().nextLong());
      clearPending();
      if (!config.scrambleChat() || level < config.levelChatScramble()) {
         return null;
      }
      Component scrambled = modifyComponents(content, seed, level, config.levelChatScramble());
      return scrambled.equals(content) ? null : scrambled;
   }

   private static void clearPending() {
      if (clearDelayAmount <= 0) {
         tipsyMessageLevel = 0;
         randomSeed = 0L;
         generatedRandom = false;
      } else {
         clearDelayAmount--;
      }
   }

   public static Component nameTagRenderer(Component original) {
      BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
      int level = ownTipsy(Minecraft.getInstance().player);
      return config.scrambleName() && level >= config.levelNameScramble() ? modifyComponents(original, 0L, level, config.levelNameScramble()) : original;
   }

   /** Whether signs should currently render scrambled for this client. */
   public static boolean shouldScrambleSigns() {
      BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
      return config.scrambleSign() && ownTipsy(Minecraft.getInstance().player) >= config.levelSignScramble();
   }

   public static Component signLine(Component line) {
      BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
      return modifyComponents(line, 0L, ownTipsy(Minecraft.getInstance().player), config.levelSignScramble());
   }
}
