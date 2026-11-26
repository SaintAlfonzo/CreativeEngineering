// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/ValidateRange.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

import java.util.Optional;

public interface ValidateRange<T extends Comparable<T>> extends ValidateSet<T>, StepRange<T>, SliderValueSet<T> {
   default T defaultValue() {
      return this.minInclusive();
   }

   default Optional<T> validateValue(T value) {
      if (value.compareTo(this.minInclusive()) < 0) {
         return Optional.of(this.minInclusive());
      } else {
         return value.compareTo(this.maxInclusive()) > 0 ? Optional.of(this.maxInclusive()) : Optional.of(value);
      }
   }
}
