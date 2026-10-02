package io.github.mortuusars.exposure.client.input;

import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;

@FunctionalInterface
public interface Key {
   boolean matches(int var1, int var2, int var3, int var4);

   default Key or(Key anotherKey) {
      return (key, code, action, mods) -> this.matches(key, code, action, mods) || anotherKey.matches(key, code, action, mods);
   }

   default Key.KeyWithPredicate onlyIf(Supplier<Boolean> predicate) {
      return new Key.KeyWithPredicate(this, predicate);
   }

   default KeyBinding executes(Supplier<Boolean> handler) {
      return new KeyBinding(this, handler);
   }

   default KeyBinding executes(Runnable runnable) {
      return new KeyBinding(this, () -> {
         runnable.run();
         return true;
      });
   }

   static boolean actionMatches(int definedAction, int action) {
      return definedAction != 1 && definedAction != 2 ? definedAction == action : action == 1 || action == 2;
   }

   static Key press(int keyCode) {
      return press(0, keyCode);
   }

   static Key release(int keyCode) {
      return release(0, keyCode);
   }

   static Key press(int modifiers, int keyCode) {
      return (key, code, action, mods) -> actionMatches(1, action) && keyCode == key && mods == modifiers;
   }

   static Key release(int modifiers, int keyCode) {
      return (key, code, action, mods) -> actionMatches(0, action) && keyCode == key && mods == modifiers;
   }

   static Key press(KeyMapping keyMapping) {
      return (key, code, action, mods) -> actionMatches(1, action) && keyMapping.matches(key, code);
   }

   static Key release(KeyMapping keyMapping) {
      return (key, code, action, mods) -> actionMatches(0, action) && keyMapping.matches(key, code);
   }

   public static record KeyWithPredicate(Key key, Supplier<Boolean> predicate) implements Key {
      @Override
      public boolean matches(int keyCode, int scanCode, int action, int modifiers) {
         return this.key.matches(keyCode, scanCode, action, modifiers) && this.predicate.get();
      }
   }
}
