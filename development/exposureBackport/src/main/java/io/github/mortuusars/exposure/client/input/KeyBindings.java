package io.github.mortuusars.exposure.client.input;

import java.util.ArrayList;
import java.util.Arrays;

public class KeyBindings {
   public static final KeyBindings EMPTY = new KeyBindings();
   protected final ArrayList<KeyBinding> bindings = new ArrayList<>();

   public void add(KeyBinding... bindings) {
      this.bindings.addAll(Arrays.asList(bindings));
   }

   public void add(int index, KeyBinding binding) {
      this.bindings.add(index, binding);
   }

   public boolean remove(KeyBinding binding) {
      return this.bindings.remove(binding);
   }

   public void clear() {
      this.bindings.clear();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (KeyBinding binding : this.bindings) {
         if (binding.matches(keyCode, scanCode, 1, modifiers) && binding.handler().get()) {
            return true;
         }
      }

      return false;
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      for (KeyBinding binding : this.bindings) {
         if (binding.matches(keyCode, scanCode, 0, modifiers) && binding.handler().get()) {
            return true;
         }
      }

      return false;
   }

   public static KeyBindings of(KeyBinding... bindings) {
      KeyBindings list = new KeyBindings();
      list.add(bindings);
      return list;
   }
}
