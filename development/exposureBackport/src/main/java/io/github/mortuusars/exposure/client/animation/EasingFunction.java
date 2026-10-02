package io.github.mortuusars.exposure.client.animation;

import java.util.function.Function;

public enum EasingFunction {
   LINEAR(x -> x),
   EASE_IN_SINE(x -> 1.0 - Math.cos(x * Math.PI / 2.0)),
   EASE_OUT_SINE(x -> Math.sin(x * Math.PI / 2.0)),
   EASE_IN_OUT_SINE(x -> -(Math.cos(Math.PI * x) - 1.0) / 2.0),
   EASE_IN_QUAD(x -> x * x),
   EASE_OUT_QUAD(x -> 1.0 - (1.0 - x) * (1.0 - x)),
   EASE_IN_OUT_QUAD(x -> x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0),
   EASE_IN_CUBIC(x -> x * x * x),
   EASE_OUT_CUBIC(x -> 1.0 - Math.pow(1.0 - x, 3.0)),
   EASE_IN_OUT_CUBIC(x -> x < 0.5 ? 4.0 * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 3.0) / 2.0),
   EASE_IN_QUART(x -> x * x * x * x),
   EASE_OUT_QUART(x -> 1.0 - Math.pow(1.0 - x, 4.0)),
   EASE_IN_OUT_QUART(x -> x < 0.5 ? 8.0 * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 4.0) / 2.0),
   EASE_IN_QUINT(x -> x * x * x * x * x),
   EASE_OUT_QUINT(x -> 1.0 - Math.pow(1.0 - x, 5.0)),
   EASE_IN_OUT_QUINT(x -> x < 0.5 ? 16.0 * x * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 5.0) / 2.0),
   EASE_IN_EXPO(x -> x == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * x - 10.0)),
   EASE_OUT_EXPO(x -> x == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * x)),
   EASE_IN_OUT_EXPO(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : (x < 0.5 ? Math.pow(2.0, 20.0 * x - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0))),
   EASE_IN_CIRCLE(x -> 1.0 - Math.sqrt(1.0 - Math.pow(x, 2.0))),
   EASE_OUT_CIRCLE(x -> Math.sqrt(1.0 - Math.pow(x - 1.0, 2.0))),
   EASE_IN_OUT_CIRCLE(x -> x < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0),
   EASE_IN_BACK(x -> {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return 2.70158 * x * x * x - 1.70158 * x * x;
   }),
   EASE_OUT_BACK(x -> {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return 1.0 + 2.70158 * Math.pow(x - 1.0, 3.0) + 1.70158 * Math.pow(x - 1.0, 2.0);
   }),
   EASE_IN_OUT_BACK(
      x -> {
         double c1 = 1.70158;
         double c2 = 2.5949095;
         return x < 0.5
            ? Math.pow(2.0 * x, 2.0) * (7.189819 * x - 2.5949095) / 2.0
            : (Math.pow(2.0 * x - 2.0, 2.0) * (3.5949095 * (x * 2.0 - 2.0) + 2.5949095) + 2.0) / 2.0;
      }
   ),
   EASE_IN_ELASTIC(x -> {
      double c4 = Math.PI * 2.0 / 3.0;
      return x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : -Math.pow(2.0, 10.0 * x - 10.0) * Math.sin((x * 10.0 - 10.75) * (Math.PI * 2.0 / 3.0)));
   }),
   EASE_OUT_ELASTIC(x -> {
      double c4 = Math.PI * 2.0 / 3.0;
      return x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * (Math.PI * 2.0 / 3.0)) + 1.0);
   }),
   EASE_IN_OUT_ELASTIC(
      x -> {
         double c5 = Math.PI * 4.0 / 9.0;
         return x == 0.0
            ? 0.0
            : (
               x == 1.0
                  ? 1.0
                  : (
                     x < 0.5
                        ? -(Math.pow(2.0, 20.0 * x - 10.0) * Math.sin((20.0 * x - 11.125) * (Math.PI * 4.0 / 9.0))) / 2.0
                        : Math.pow(2.0, -20.0 * x + 10.0) * Math.sin((20.0 * x - 11.125) * (Math.PI * 4.0 / 9.0)) / 2.0 + 1.0
                  )
            );
      }
   ),
   EASE_OUT_BOUNCE(x -> {
      double n1 = 7.5625;
      double d1 = 2.75;
      if (x < 0.36363636363636365) {
         return 7.5625 * x * x;
      } else if (x < 0.7272727272727273) {
         // the standard easeOutBounce; both decompilers mangle upstream's `n1 * (x -= 1.5 / d1) * x + 0.75` form
         double t = x - 0.5454545454545454;
         return 7.5625 * t * t + 0.75;
      } else if (x < 0.9090909090909091) {
         double t = x - 0.8181818181818182;
         return 7.5625 * t * t + 0.9375;
      } else {
         double t = x - 0.9545454545454546;
         return 7.5625 * t * t + 0.984375;
      }
   }),
   EASE_IN_BOUNCE(x -> 1.0 - EASE_OUT_BOUNCE.ease(1.0 - x)),
   EASE_IN_OUT_BOUNCE(x -> x < 0.5 ? (1.0 - EASE_OUT_BOUNCE.ease(1.0 - 2.0 * x)) / 2.0 : (1.0 + EASE_OUT_BOUNCE.ease(2.0 * x - 1.0)) / 2.0);

   private final Function<Double, Double> function;

   private EasingFunction(Function<Double, Double> function) {
      this.function = function;
   }

   public double ease(double x) {
      return this.function.apply(x);
   }
}
