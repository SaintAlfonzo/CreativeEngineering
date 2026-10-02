package io.github.mortuusars.exposure.client.gui.screen.element.textbox;

public enum HorizontalAlignment {
   LEFT,
   CENTER,
   RIGHT;

   public int align(int areaWidth, int contentWidth) {
      if (this == CENTER) {
         return areaWidth / 2 - contentWidth / 2;
      } else {
         return this == RIGHT ? areaWidth - contentWidth : 0;
      }
   }
}
