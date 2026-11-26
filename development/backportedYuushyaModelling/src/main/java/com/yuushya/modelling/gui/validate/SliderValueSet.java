// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/SliderValueSet.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

interface SliderValueSet<T> {
   double toSliderValue(T var1);

   T fromSliderValue(double var1);
}
