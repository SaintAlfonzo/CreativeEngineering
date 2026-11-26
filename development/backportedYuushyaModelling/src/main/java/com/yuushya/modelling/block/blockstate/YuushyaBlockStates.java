// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 block/blockstate/YuushyaBlockStates.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.block.blockstate;

import com.yuushya.modelling.blockentity.BlockShape;
import net.minecraft.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class YuushyaBlockStates {
   public static final IntegerProperty LIT = IntegerProperty.create("lit", 0, 15);
   public static final EnumProperty<BlockShape> SHAPES = EnumProperty.create("shapes", BlockShape.class);

   public static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean doGetPre) {
      return (BlockState)blockState.setValue(property, getRelative(property.getPossibleValues(), blockState.getValue(property), doGetPre));
   }

   public static <T> T getRelative(Iterable<T> iterable, @Nullable T object, boolean doGetPre) {
      return (T)(doGetPre ? Util.findPreviousInIterable(iterable, object) : Util.findNextInIterable(iterable, object));
   }
}
