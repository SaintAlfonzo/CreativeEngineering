package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.block.FacePaperBlock;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.block.PieceHit;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;

/**
 * Block outline for face_paper (sticky_notes.md 3.2, BRIEF 4 "Highlight"): only the hovered piece's box is drawn
 * (a note, the label, or the whole panel of a covered face when the hit lands between its notes); the vanilla
 * outline of the whole cell shape is cancelled. Forge bus.
 */
public final class FacePaperHighlight {

    private FacePaperHighlight() {}

    public static void onHighlight(RenderHighlightEvent.Block event) {
        BlockHitResult hit = event.getTarget();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || hit == null) return;
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FacePaperBlock)) return;
        FacePaperBlockEntity be = FacePaperBlock.entity(level, pos);
        if (be == null) return;
        PieceHit piece = PieceHit.resolve(state, be, hit.getLocation());
        if (piece == null) return;
        Vec3 cam = event.getCamera().getPosition();
        AABB box = piece.box(be.faces()).move(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(event.getPoseStack(), lines, box, 0f, 0f, 0f, 0.4f);
        event.setCanceled(true);
    }
}
