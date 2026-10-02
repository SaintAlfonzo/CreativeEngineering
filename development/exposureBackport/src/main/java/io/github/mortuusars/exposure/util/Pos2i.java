package io.github.mortuusars.exposure.util;

import java.util.Objects;

public class Pos2i {
   public int x;
   public int y;

   public Pos2i(int x, int y) {
      this.x = x;
      this.y = y;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Pos2i pos2i = (Pos2i)o;
         return this.x == pos2i.x && this.y == pos2i.y;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }

   @Override
   public String toString() {
      return "Pos2i{x=" + this.x + ", y=" + this.y + "}";
   }
}
