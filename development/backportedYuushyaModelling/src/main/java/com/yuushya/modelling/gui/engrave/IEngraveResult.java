// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/engrave/IEngraveResult.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.engrave;

import net.minecraft.world.item.ItemStack;

public interface IEngraveResult {
   String getName();

   ItemStack getResultItem();
}
