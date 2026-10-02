package io.github.mortuusars.exposure.client.input;

import java.util.function.Supplier;

public record KeyBinding(Key matcher, Supplier<Boolean> handler) {
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.matchesPress(keyCode, scanCode, modifiers) && this.handler().get();
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.matchesRelease(keyCode, scanCode, modifiers) && this.handler().get();
   }

   public boolean matches(int keyCode, int scanCode, int press, int modifiers) {
      return this.matcher.matches(keyCode, scanCode, press, modifiers);
   }

   public boolean matchesPress(int keyCode, int scanCode, int modifiers) {
      return this.matcher.matches(keyCode, scanCode, 1, modifiers);
   }

   public boolean matchesRelease(int keyCode, int scanCode, int modifiers) {
      return this.matcher.matches(keyCode, scanCode, 0, modifiers);
   }
}
