package com.SaintAlfonzo.creativecompat.surface;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.copycatsplus.copycats.foundation.copycat.ICopycatBlockEntity;
import com.copycatsplus.copycats.foundation.copycat.multistate.IMultiStateCopycatBlock;
import com.copycatsplus.copycats.foundation.copycat.multistate.IMultiStateCopycatBlockEntity;
import com.copycatsplus.copycats.foundation.copycat.multistate.MaterialItemStorage;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Copycats+ side of {@link CamoSpeed}. Only touched when Copycats+ is loaded. */
final class CopycatsPlusCamo {

    private CopycatsPlusCamo() {}

    /** The material to take the speed of, or {@code null} for none. */
    static BlockState camo(BlockEntity be) {
        if (be instanceof IMultiStateCopycatBlockEntity multi) {
            MaterialItemStorage storage = multi.getMaterialItemStorage();
            String last = ((LastInsertedMaterial) storage).creativeengineering$lastProperty();
            Map<String, BlockState> materials = storage.getMaterialMap();
            if (last != null && storage.hasCustomMaterial(last)) {
                return materials.get(last);
            }
            // newest part was emptied (or predates tracking): use what's left if it's all one material
            Set<BlockState> left = new HashSet<>();
            materials.forEach((part, state) -> {
                if (storage.hasCustomMaterial(part)) {
                    left.add(state);
                }
            });
            return left.size() == 1 ? left.iterator().next() : null;
        }
        if (be instanceof ICopycatBlockEntity copycat) {
            return copycat.hasCustomMaterial() ? copycat.getMaterial() : null;
        }
        return null;
    }

    static boolean isMultiMaterial(Block block) {
        return block instanceof IMultiStateCopycatBlock;
    }
}
