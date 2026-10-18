package umpaz.brewinandchewin.common.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

// Port: upstream's setupChatMessageServer (the server rewrote tipsy chat) is gone; scrambling happens only on clients
// (alcohol.md 4.3). scramble() is the shared letter-swapping both upstream copies used.
public class BnCTextUtils {
   public static MutableComponent getTranslation(String key, Object... args) {
      return Component.translatable("brewinandchewin." + key, args);
   }

   /**
    * Swaps neighbouring letters inside words longer than 3 characters. `amplifier` is the Tipsy amplifier minus the
    * scramble threshold (0 at the threshold).
    */
   public static String scramble(String original, int amplifier, RandomSource random) {
      StringBuilder textBuilder = new StringBuilder(original);
      int amnt = (int)((float)(amplifier + 1) * ((float)textBuilder.length() / 6.0F)) + random.nextIntBetweenInclusive(amplifier, amplifier + 1) - 1;

      for (int i = 0; i < amnt; i++) {
         List<String> globalWords = Arrays.stream(textBuilder.toString().split(" ")).collect(Collectors.toCollection(ArrayList::new));
         List<String> validWords = globalWords.stream().filter(s -> s.length() > 3).collect(Collectors.toCollection(ArrayList::new));
         if (validWords.isEmpty()) {
            break;
         }

         int wordIndex = random.nextInt(validWords.size());
         String word = validWords.get(wordIndex);
         int globalWordLength = globalWords.subList(0, globalWords.indexOf(word)).stream().mapToInt(value -> value.length() + 1).sum();
         int index = globalWordLength + random.nextIntBetweenInclusive(1, word.length() - 3);
         int newIndex = Mth.clamp(index + (random.nextBoolean() ? 1 : -1), globalWordLength + 1, globalWordLength + word.length() - 2);
         char temp = textBuilder.charAt(index);
         textBuilder.setCharAt(index, textBuilder.charAt(newIndex));
         textBuilder.setCharAt(newIndex, temp);
      }

      return textBuilder.toString();
   }
}
