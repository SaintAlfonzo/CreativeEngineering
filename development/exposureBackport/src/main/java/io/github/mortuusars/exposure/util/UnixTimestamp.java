package io.github.mortuusars.exposure.util;

public class UnixTimestamp {
   public static class Milliseconds {
      public static long now() {
         return System.currentTimeMillis();
      }
   }

   public static class Seconds {
      public static long now() {
         return System.currentTimeMillis() / 1000L;
      }

      public static long fromNow(int seconds) {
         return now() + (long)seconds;
      }
   }
}
