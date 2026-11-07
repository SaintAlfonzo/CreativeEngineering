package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.data.CamoContainer;
import xfacthd.framedblocks.common.blockentity.FramedDoubleBlockEntity;
import xfacthd.framedblocks.common.data.BlockType;

/** FramedBlocks side of {@link CamoSpeed}. Only touched when FramedBlocks is loaded. */
final class FramedCamo {

    private FramedCamo() {}

    /** The camo to take the speed of, or {@code null} for none (no camo / not a framed block). */
    static BlockState camo(BlockEntity be) {
        if (be instanceof FramedDoubleBlockEntity dbe) {
            boolean lastSecond = ((LastInsertedCamo) dbe).creativeengineering$lastCamoIsSecond();
            CamoContainer newest = lastSecond ? dbe.getCamoTwo() : dbe.getCamo();
            CamoContainer other = lastSecond ? dbe.getCamo() : dbe.getCamoTwo();
            return newest.isEmpty() ? state(other) : state(newest);
        }
        return be instanceof FramedBlockEntity fbe ? state(fbe.getCamo()) : null;
    }

    /** Double blocks, plus the slab and panel: right-clicking a second one onto them makes a double ("hybrid"). */
    static boolean isMultiMaterial(Block block) {
        if (!(block instanceof IFramedBlock framed)) {
            return false;
        }
        return framed.getBlockType().isDoubleBlock()
                || framed.getBlockType() == BlockType.FRAMED_SLAB || framed.getBlockType() == BlockType.FRAMED_PANEL;
    }

    private static BlockState state(CamoContainer camo) {
        return camo.isEmpty() ? null : camo.getState();
    }
}
